package cacher.parser;

import cacher.error.Error;
import cacher.lexer.Id;
import cacher.lexer.Lexer;
import cacher.lexer.Tag;
import cacher.lexer.Token;
import cacher.semantic.Var_record;

import static cacher.error.Error.ParserError.synterror;
import static cacher.semantic.Semantic.*;

/*
 * Created by xhaiben on 2017/4/8.
 */
public class Parser {
    private Token old_token, token;
    private int wait = 0;
    private int p_token = 0;
    private int compileOK = 0;
    private int errorNum = 0;
    private int synerr = 0;
    private Lexer lexer;

    private int ident_in_expr = 0;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }

    private void back() {
//        token = old_token;
//        old_token = null;
        wait = 1;
    }

    private int nextToken() {
        if (wait == 1) {
            wait = 0;
            return 0;
        }
        Token cur_token = lexer.lex();
        if (cur_token.getTag() == Tag.TK_EOF) {
            old_token = token;
            token = new Token(Tag.NULL);
            return -1;
        }
        old_token = token;
        token = cur_token;
        return 0;
    }

    public void parse() {
        program();
    }

    private void program() {
        if (nextToken() == -1) {
            //到达文件末尾
            table.over();
            if (Error.errorNum == 0) {
                System.out.printf("编译完成\n");
            } else {
                System.out.println("编译失败\n");
            }
        } else {
            dec();
            program();
        }
    }

    private void dec() {
        if (token.getTag() == Tag.TK_SEMICOLON) {
            return;
        } else if (token.getTag() == Tag.KW_EXTERN) {
            String dec_name = "";
            nextToken();
            Tag dec_type = type();
            nextToken();
            if (token.getTag() != Tag.TK_IDENT) {
                synterror(Error.ParserError.identlost, Lexer.getLine_num());
                back();
            } else {
                //声明标识符
                dec_name += ((Id) token).getName();
                tvar.init(new Token(dec_type), dec_name);
                tvar.externed = 1;
                if (dec_type == Tag.KW_STRING) {
                    tvar.strValID = -2;
                }
                table.add_var();
            }
            nextToken();
            if (token.getTag() != Tag.TK_SEMICOLON) {
                if (token.getTag() == Tag.KW_EXTERN || token.getTag() == Tag.KW_VOID || token.getTag() == Tag.KW_INT || token.getTag() == Tag.KW_CHAR || token.getTag() == Tag.KW_STRING) {
                    //丢失分号
                    synterror(Error.ParserError.semiconlost, Lexer.getLine_num());
                    back();
                } else {
                    //分号错误
                    synterror(Error.ParserError.semiconwrong, Lexer.getLine_num());
                }
            }
        } else {
            String dec_name = "";
            Tag dec_type = type();
            nextToken();
            if (token.getTag() != Tag.TK_IDENT) {
                //error
                synterror(Error.ParserError.identlost, Lexer.getLine_num());
                back();
            } else {
                dec_name += ((Id) token).getName();
            }
            dectail(dec_type, dec_name);
        }
    }

    private Tag type() {
        switch (token.getTag()) {
            case KW_INT:
                return Tag.KW_INT;
            case KW_CHAR:
                return Tag.KW_CHAR;
            case KW_VOID:
                return Tag.KW_VOID;
            case KW_STRING:
                return Tag.KW_STRING;
            case TK_IDENT:
                //error
                synterror(Error.ParserError.typelost, Lexer.getLine_num());
                back();
                break;
            default:
                //error
                synterror(Error.ParserError.typewrong, Lexer.getLine_num());
                break;
        }
        return null;
    }

    private void dectail(Tag dec_type, String dec_name) {
        nextToken();
        switch (token.getTag()) {
            case TK_SEMICOLON:
                tvar.init(new Token(dec_type), dec_name);
                if (dec_type == Tag.KW_STRING) {
                    tvar.strValID = -2;
                }
                table.add_var();
                break;
            case TK_OPEN_PA:
                tfun.init(dec_type, dec_name);
                para();
                funtail(dec_type, dec_name);
                break;
            default:
                tvar.init(new Token(dec_type), dec_name);
                if (dec_type == Tag.KW_STRING) {
                    tvar.strValID = -2;
                }
                table.add_var();
                varlist(dec_type);
                break;
        }
    }

    private int fun_level = 0;

    private void funtail(Tag dec_type, String dec_name) {
        nextToken();
        if (token.getTag() == Tag.TK_SEMICOLON) { //函数声明
            table.add_fun();
            return;
        } else if (token.getTag() == Tag.TK_LBRACE) { //函数定义
            tfun.defined = 1;
            table.add_fun();
            back();
            block(0, 0, 0);
            fun_level = 0;
            tfun.pop_local_vars(-1);
            gener.gen_fun_tail();
            return;
        } else if (token.getTag() == Tag.TK_IDENT || token.getTag() == Tag.KW_IF || token.getTag() == Tag.KW_WHILE || token.getTag() == Tag.KW_RETURN || token.getTag() == Tag.KW_BREAK || token.getTag() == Tag.KW_CONTINUE || token.getTag() == Tag.TK_RBRACE) {
            back();
            block(0, 0, 0);
            fun_level = 0;
            return;
        } else if (token.getTag() == Tag.KW_VOID || token.getTag() == Tag.KW_INT || token.getTag() == Tag.KW_CHAR) {
            synterror(Error.ParserError.semiconlost, Lexer.getLine_num());
            back();
            return;
        } else {
            synterror(Error.ParserError.semiconwrong, Lexer.getLine_num());
            return;
        }
    }

    private void varlist(Tag dec_type) {
        if (token.getTag() == Tag.TK_COMMA) {
            nextToken();
            if (token.getTag() != Tag.TK_IDENT) {
                //错误
                back();
                synterror(Error.ParserError.identlost, Lexer.getLine_num());
            } else {
                String dec_name = "";
                dec_name += ((Id) token).getName();
                tvar.init(new Token(dec_type), dec_name);
                if (dec_type == Tag.KW_STRING) {
                    tvar.strValID = -2;
                }
                table.add_var();
            }
            nextToken();
            varlist(dec_type);
        } else if (token.getTag() == Tag.TK_SEMICOLON) {
            return;
        } else if (token.getTag() == Tag.TK_IDENT) {
            //错误
            synterror(Error.ParserError.commalost, Lexer.getLine_num());
            nextToken();
            varlist(dec_type);
        } else if (token.getTag() == Tag.KW_INT || token.getTag() == Tag.KW_VOID || token.getTag() == Tag.KW_CHAR || token.getTag() == Tag.KW_STRING) {
            //错误
            synterror(Error.ParserError.semiconlost, Lexer.getLine_num());
            back();
        } else {
            //错误
            synterror(Error.ParserError.semiconwrong, Lexer.getLine_num());
        }
    }

    private void para() {
        nextToken();
        switch (token.getTag()) {
            case TK_CLOSE_PA:
                break;
            default:
                Tag para_type = type();
                String para_name = "";
                nextToken();
                if (token.getTag() != Tag.TK_IDENT) {
                    // paralost
                    synterror(Error.ParserError.paralost, Lexer.getLine_num());
                    back();
                } else {
                    para_name += ((Id) token).getName();
                    //形式参数
                    boolean msg_back = table.has_name(para_name);
                    if (msg_back) {
                        Error.SemError.semerror(Error.SemError.para_redef);
                    } else {
                        tvar.init(new Token(para_type), para_name);
                        tfun.addarg();
                    }
                }
                paralist();
                break;
        }
    }

    private void paralist() {
        nextToken();
        if (token.getTag() == Tag.TK_COMMA) {
            nextToken();
            Tag para_type = type();
            String para_name = "";
            nextToken();
            if (token.getTag() != Tag.TK_IDENT) {
                //paralost
                synterror(Error.ParserError.paralost, Lexer.getLine_num());
                back();
            } else {
                para_name += ((Id) token).getName();
                boolean msg_back = table.has_name(para_name);
                //形式参数
                if (msg_back) {
                    Error.SemError.semerror(Error.SemError.para_redef);
                } else {
                    tvar.init(new Token(para_type), para_name);
                    tfun.addarg();
                }
            }
            paralist();
        } else if (token.getTag() == Tag.TK_LBRACE || token.getTag() == Tag.TK_SEMICOLON) {
            // 缺失右括号
            synterror(Error.ParserError.rparenlost, Lexer.getLine_num());
            back();
        } else if (token.getTag() == Tag.TK_CLOSE_PA) {
            return;
        } else if (token.getTag() == Tag.KW_INT || token.getTag() == Tag.KW_VOID || token.getTag() == Tag.KW_CHAR || token.getTag() == Tag.KW_STRING) {
            // 逗号缺失
            synterror(Error.ParserError.commalost, Lexer.getLine_num());
            nextToken();
            if (token.getTag() != Tag.TK_IDENT) {
                // paralost
                synterror(Error.ParserError.paralost, Lexer.getLine_num());
                back();
            }
            paralist();
        }
    }

    private void block(int initvar_num, int lopId, int blockAddr) {
        nextToken();
        if (token.getTag() != Tag.TK_LBRACE) {
            //左花括号缺失
            synterror(Error.ParserError.lbraclost, Lexer.getLine_num());
            back();
        }
        int[] var_num = {initvar_num};
        fun_level++;
        childprogram(var_num, lopId, blockAddr);
        fun_level--;
        tfun.pop_local_vars(var_num[0]);
    }

    private int r_brac_is_lost = 0;

    private void childprogram(int[] var_num, int lopId, int blockAddr) {
        nextToken();
        if (token.getTag() == Tag.TK_SEMICOLON || token.getTag() == Tag.KW_WHILE || token.getTag() == Tag.KW_IF || token.getTag() == Tag.KW_RETURN || token.getTag() == Tag.TK_IDENT || token.getTag() == Tag.KW_BREAK || token.getTag() == Tag.KW_CONTINUE || token.getTag() == Tag.KW_IN || token.getTag() == Tag.KW_OUT) {
            statement(var_num, lopId, blockAddr);
            childprogram(var_num, lopId, blockAddr);
        } else if (token.getTag() == Tag.KW_VOID || token.getTag() == Tag.KW_INT || token.getTag() == Tag.KW_CHAR || token.getTag() == Tag.KW_STRING) {
            localdec(var_num);
            if (r_brac_is_lost == 1) {
                r_brac_is_lost = 0;
            } else {
                childprogram(var_num, lopId, blockAddr);
            }
        } else if (token.getTag() == Tag.TK_RBRACE) { //复合语句结尾
            return;
        } else if (token == null) {
            //右花括号丢失
            synterror(Error.ParserError.rbraclost, Lexer.getLine_num());
        } else {
            //语句异常
            synterror(Error.ParserError.statementexcp, Lexer.getLine_num());
        }
    }

    private void localdec(int[] var_num) {
        Tag local_type;
        String local_name = "";
        local_type = type();
        nextToken();
        if (token.getTag() != Tag.TK_IDENT) {
            //标识符不匹配
            synterror(Error.ParserError.identlost, Lexer.getLine_num());
            back();
        } else {
            //定义局部变量
            local_name = ((Id) token).getName();
            boolean msg_back = table.has_name(local_name);
            if (msg_back) {
                Error.SemError.semerror(Error.SemError.localvar_redef);
            } else {
                tvar.init(new Token(local_type), local_name);
                tfun.push_local_val();
                var_num[0]++;
            }
        }
        nextToken();
        localdectail(var_num, local_type);
    }

    private void localdectail(int[] var_num, Tag local_type) {
        if (token.getTag() == Tag.TK_COMMA) {
            nextToken();
            if (token.getTag() != Tag.TK_IDENT) {
                back();
                synterror(Error.ParserError.localidentlost, Lexer.getLine_num());
            } else {
                //定义局部变量
                String local_name = ((Id) token).getName();
                boolean msg_back = table.has_name(local_name);
                if (msg_back) {
                    Error.SemError.semerror(Error.SemError.localvar_redef);
                } else {
                    tvar.init(new Token(local_type), local_name);
                    tfun.push_local_val();
                    var_num[0]++;
                }
            }
            nextToken();
            localdectail(var_num, local_type);
        } else if (token.getTag() == Tag.TK_SEMICOLON) {
            return;
        } else if (token.getTag() == Tag.TK_IDENT) {
            //逗号缺失
            synterror(Error.ParserError.commalost, Lexer.getLine_num());
            nextToken();
            localdectail(var_num, local_type);
        } else if (token.getTag() == Tag.KW_INT || token.getTag() == Tag.KW_VOID || token.getTag() == Tag.KW_CHAR || token.getTag() == Tag.KW_STRING || token.getTag() == Tag.TK_RBRACE) {
            synterror(Error.ParserError.semiconlost, Lexer.getLine_num());
            back();
        } else if (token.getTag() == Tag.TK_OPEN_PA) {
            r_brac_is_lost = 1;
            synterror(Error.ParserError.rbraclost, Lexer.getLine_num());
            para();
            block(0, 0, 0);
        } else {
            synterror(Error.ParserError.semiconwrong, Lexer.getLine_num());
        }
    }

    private void statement(int[] var_num, int lopId, int blockAddr) {
        String refName = "";
        switch (token.getTag()) {
            case TK_SEMICOLON:
                break;
            case KW_WHILE:
                whilestat(var_num);
                break;
            case KW_IF:
                ifstat(var_num, lopId, blockAddr);
                break;
            case KW_BREAK:
                nextToken();
                if (token.getTag() == Tag.TK_IDENT || token.getTag() == Tag.KW_WHILE || token.getTag() == Tag.KW_IF || token.getTag() == Tag.KW_RETURN || token.getTag() == Tag.KW_BREAK || token.getTag() == Tag.KW_CONTINUE || token.getTag() == Tag.KW_IN || token.getTag() == Tag.KW_OUT || token.getTag() == Tag.TK_RBRACE) {
                    //分号缺失
                    synterror(Error.ParserError.semiconlost, Lexer.getLine_num());
                    back();
                } else if (token.getTag() != Tag.TK_SEMICOLON) {
                    //分号错误
                    synterror(Error.ParserError.semiconwrong, Lexer.getLine_num());
                }
                //生成break
                if (lopId != 0) {
                    gener.gen_block(blockAddr);
                    gener.out_code("\tjmp @while_%d_exit\n", lopId);
                } else {
                    Error.SemError.semerror(Error.SemError.break_nin_while);
                }
                break;
            case KW_CONTINUE:
                nextToken();
                if (token.getTag() == Tag.TK_IDENT || token.getTag() == Tag.KW_WHILE || token.getTag() == Tag.KW_IF || token.getTag() == Tag.KW_RETURN || token.getTag() == Tag.KW_BREAK || token.getTag() == Tag.KW_CONTINUE || token.getTag() == Tag.KW_IN || token.getTag() == Tag.KW_OUT || token.getTag() == Tag.TK_RBRACE) {
                    //分号缺失
                    synterror(Error.ParserError.semiconlost, Lexer.getLine_num());
                    back();
                } else if (token.getTag() != Tag.TK_SEMICOLON) {
                    //分号错误
                    synterror(Error.ParserError.semiconwrong, Lexer.getLine_num());
                }
                //生成continue
                if (lopId != 0) {
                    gener.gen_block(blockAddr);
                    gener.out_code("\tjmp @while_%d_lop\n", lopId);
                } else {
                    Error.SemError.semerror(Error.SemError.continue_nin_while);
                }
                break;
            case KW_RETURN:
                retstat(var_num);
                break;
            case KW_IN:
                nextToken();
                if (token.getTag() != Tag.TK_IN_PUT) {
                    //输入错误
                    synterror(Error.ParserError.input_err, Lexer.getLine_num());
                }
                nextToken();
                if (token.getTag() != Tag.TK_IDENT) {
                    //无效输入
                    synterror(Error.ParserError.na_input, Lexer.getLine_num());
                } else {
                    //获取输入
                    refName += ((Id) token).getName();
                    gener.gen_input(table.get_var(refName), var_num);
                }
                nextToken();
                if (token.getTag() != Tag.TK_SEMICOLON) {
                    synterror(Error.ParserError.semiconlost, Lexer.getLine_num());
                    back();
                }
                break;
            case KW_OUT:
                nextToken();
                if (token.getTag() != Tag.TK_OUT_PUT) {
                    //输出错误
                    synterror(Error.ParserError.output_err, Lexer.getLine_num());
                }
                //输出
                gener.gen_output(expr(var_num), var_num);
                nextToken();
                if (token.getTag() != Tag.TK_SEMICOLON) {
                    //无效输出
                    synterror(Error.ParserError.semiconlost, Lexer.getLine_num());
                    back();
                }
                break;
            case TK_IDENT:
                refName = ((Id) token).getName();
                idtail(refName, var_num);
                nextToken();
                if (token.getTag() != Tag.TK_SEMICOLON) {
                    synterror(Error.ParserError.semiconlost, Lexer.getLine_num());
                    back();
                }
                break;
        }
    }

    private int lopId = 0;

    private void whilestat(int[] var_num) {
        lopId++;
        int id = lopId;
        nextToken();
        if (token.getTag() != Tag.TK_OPEN_PA) {
            if (token.getTag() == Tag.TK_IDENT || token.getTag() == Tag.TK_C_NUM || token.getTag() == Tag.TK_C_STR || token.getTag() == Tag.TK_C_CHAR || token.getTag() == Tag.TK_OPEN_PA) {
                //左括号缺失
                synterror(Error.ParserError.lparenlost, Lexer.getLine_num());
                back();
            } else {
                //符号错误
                synterror(Error.ParserError.lparenwrong, Lexer.getLine_num());
            }
        }
        gener.out_code("@while_%d_lop:\n", id);
        int blockAddr = gener.gen_block(-1);
        int[] initvar_num_while = {0};
        Var_record cond = expr(initvar_num_while);
        gener.gen_condition(cond);
        gener.out_code("\tje @while_%d_exit\n", id);

        nextToken();
        if (token.getTag() != Tag.TK_CLOSE_PA) {
            if (token.getTag() == Tag.TK_LBRACE || token.getTag() == Tag.TK_SEMICOLON || token.getTag() == Tag.KW_WHILE || token.getTag() == Tag.KW_IF || token.getTag() == Tag.KW_RETURN || token.getTag() == Tag.KW_BREAK || token.getTag() == Tag.KW_CONTINUE || token.getTag() == Tag.KW_IN || token.getTag() == Tag.KW_OUT || token.getTag() == Tag.TK_IDENT || token.getTag() == Tag.KW_VOID || token.getTag() == Tag.KW_INT || token.getTag() == Tag.KW_CHAR || token.getTag() == Tag.KW_STRING) {
                // 右括号缺失
                synterror(Error.ParserError.staterparenlost, Lexer.getLine_num());
                back();
            } else {
                //无效字符
                synterror(Error.ParserError.rparenwrong, Lexer.getLine_num());
            }
        }
        block(initvar_num_while[0], lopId, blockAddr);
        gener.gen_block(blockAddr);
        gener.out_code("\tjmp @while_%d_lop\n", id);
        gener.out_code("@while_%d_exit:\n", id);
    }

    private int ifId = 0;

    private void ifstat(int[] var_num, int lopId, int blockAddr) {
        ifId++;
        int id = ifId;
        nextToken();
        if (token.getTag() != Tag.TK_OPEN_PA) {
            if (token.getTag() == Tag.TK_IDENT || token.getTag() == Tag.TK_C_NUM || token.getTag() == Tag.TK_C_CHAR || token.getTag() == Tag.TK_C_STR || token.getTag() == Tag.TK_OPEN_PA) {
                //左括号丢失
                synterror(Error.ParserError.lparenlost, Lexer.getLine_num());
                back();
            } else {
                //无效字符
                synterror(Error.ParserError.lparenwrong, Lexer.getLine_num());
            }
        }
        int blockAddr1 = gener.gen_block(-1);
        int[] initvar_num_if = {0};
        Var_record cond = expr(initvar_num_if);
        gener.gen_condition(cond);
        gener.out_code("\tje @if_%d_middle\n", id);

        nextToken();
        if (token.getTag() != Tag.TK_CLOSE_PA) {
            if (token.getTag() == Tag.TK_LBRACE || token.getTag() == Tag.TK_SEMICOLON || token.getTag() == Tag.KW_WHILE || token.getTag() == Tag.KW_IF || token.getTag() == Tag.KW_RETURN || token.getTag() == Tag.KW_BREAK || token.getTag() == Tag.KW_CONTINUE || token.getTag() == Tag.KW_IN || token.getTag() == Tag.KW_OUT || token.getTag() == Tag.TK_IDENT || token.getTag() == Tag.KW_VOID || token.getTag() == Tag.KW_INT || token.getTag() == Tag.KW_CHAR || token.getTag() == Tag.KW_STRING) {
                // 右括号缺失
                synterror(Error.ParserError.staterparenlost, Lexer.getLine_num());
                back();
            } else {
                //无效字符
                synterror(Error.ParserError.rparenwrong, Lexer.getLine_num());
            }
        }
        block(initvar_num_if[0], lopId, blockAddr);
        gener.gen_block(blockAddr1);
        gener.out_code("\tjmp @if_%d_end\n", id);
        gener.out_code("@if_%d_middle:\n", id);
        gener.gen_block(blockAddr1);

        nextToken();
        if (token.getTag() != Tag.KW_ELSE) {
            if (token.getTag() == Tag.TK_LBRACE || token.getTag() == Tag.TK_SEMICOLON || token.getTag() == Tag.KW_WHILE || token.getTag() == Tag.KW_IF || token.getTag() == Tag.KW_RETURN || token.getTag() == Tag.KW_BREAK || token.getTag() == Tag.KW_CONTINUE || token.getTag() == Tag.KW_IN || token.getTag() == Tag.KW_OUT || token.getTag() == Tag.KW_VOID || token.getTag() == Tag.KW_INT || token.getTag() == Tag.KW_CHAR || token.getTag() == Tag.KW_STRING) {
                //else丢失
                synterror(Error.ParserError.elselost, Lexer.getLine_num());
                back();
            } else if (token.getTag() == Tag.TK_IDENT) {
                // else 拼写错误
                synterror(Error.ParserError.elsespelterr, Lexer.getLine_num());
            } else {
                //非法字符
                synterror(Error.ParserError.elsewrong, Lexer.getLine_num());
            }
        } else {

        }
        block(0, lopId, blockAddr);
        gener.gen_block(blockAddr1);
        gener.out_code("@if_%d_end:\n", id);
    }

    private void retstat(int[] var_num) {
        returntail(var_num);
        nextToken();
        if (token.getTag() != Tag.TK_SEMICOLON) {
            if (token.getTag() == Tag.TK_RBRACE) {
                //分号丢失
                synterror(Error.ParserError.semiconlost, Lexer.getLine_num());
                back();
            }
        }
    }

    private void returntail(int[] var_num) {
        if (fun_level == 1) {
            tfun.had_ret = 1;
        }

        nextToken();
        if (token.getTag() == Tag.TK_IDENT || token.getTag() == Tag.TK_C_NUM || token.getTag() == Tag.TK_C_CHAR || token.getTag() == Tag.TK_C_STR || token.getTag() == Tag.TK_OPEN_PA) {
            back();
            Var_record ret = expr(var_num);
            if (ret != null && ret.type.getTag() != tfun.type) {
                Error.SemError.semerror(Error.SemError.ret_type_err);
            }
            gener.gen_return(ret, var_num);

        } else if (token.getTag() == Tag.TK_SEMICOLON) {
            back();
            if (tfun.type != Tag.KW_VOID) {
                Error.SemError.semerror(Error.SemError.ret_type_err);
            }
            gener.gen_return(null, var_num);
            return;
        } else if (token.getTag() == Tag.TK_RBRACE) {
            back();
            return;
        } else {
            synterror(Error.ParserError.returnwrong, Lexer.getLine_num());
        }
    }

    private Var_record idtail(String refname, int[] var_num) {
        nextToken();
        if (token.getTag() == Tag.TK_ASSIGN) {
            Var_record src = expr(var_num);
            Var_record des = table.get_var(refname);
            return gener.gen_assign(des, src, var_num);
        } else if (token.getTag() == Tag.TK_OPEN_PA) {
            realarg(refname, var_num);
            Var_record var_ret = table.gen_Call(refname, var_num);
            nextToken();
            if (token.getTag() != Tag.TK_CLOSE_PA) {
                //右括号缺失
                synterror(Error.ParserError.rparenlost, Lexer.getLine_num());
                back();
            }
            return var_ret;
        } else if (ident_in_expr == 1) {
            ident_in_expr = 0;
            back();
            return table.get_var(refname);
        } else {
            synterror(Error.ParserError.idtaillost, Lexer.getLine_num());
            back();
        }
        return null;
    }

    private void realarg(String refname, int[] var_num) {
        nextToken();
        if (token.getTag() == Tag.TK_IDENT || token.getTag() == Tag.TK_C_NUM || token.getTag() == Tag.TK_C_CHAR || token.getTag() == Tag.TK_C_STR || token.getTag() == Tag.TK_OPEN_PA) {
            back();
            table.add_real_arg(expr(var_num), var_num);
            arglist(var_num);
        } else if (token.getTag() == Tag.TK_CLOSE_PA || token.getTag() == Tag.TK_SEMICOLON) {
            back();
            return;
        } else if (token.getTag() == Tag.TK_COMMA) {
            synterror(Error.ParserError.arglost, Lexer.getLine_num());
            back();
            arglist(var_num);
        } else {
            //错误
            synterror(Error.ParserError.argwrong, Lexer.getLine_num());
        }
    }

    private void arglist(int[] var_num) {
        nextToken();
        if (token.getTag() == Tag.TK_COMMA) {
            nextToken();
            if (token.getTag() == Tag.TK_IDENT || token.getTag() == Tag.TK_C_NUM || token.getTag() == Tag.TK_C_CHAR || token.getTag() == Tag.TK_C_STR || token.getTag() == Tag.TK_OPEN_PA) {
                back();
                table.add_real_arg(expr(var_num), var_num);
                arglist(var_num);
            } else if (token.getTag() == Tag.TK_COMMA) {
                synterror(Error.ParserError.arglost, Lexer.getLine_num());
                back();
                arglist(var_num);
            } else if (token.getTag() == Tag.TK_SEMICOLON || token.getTag() == Tag.TK_CLOSE_PA) {
                synterror(Error.ParserError.arglost, Lexer.getLine_num());
                back();
                return;
            } else {
                //错误
                synterror(Error.ParserError.argwrong, Lexer.getLine_num());
            }
        } else if (token.getTag() == Tag.TK_CLOSE_PA || token.getTag() == Tag.TK_SEMICOLON) {
            back();
            return;
        } else if (token.getTag() == Tag.TK_IDENT || token.getTag() == Tag.TK_C_NUM || token.getTag() == Tag.TK_C_CHAR || token.getTag() == Tag.TK_C_STR || token.getTag() == Tag.TK_OPEN_PA) {
            //错误
            synterror(Error.ParserError.commalost, Lexer.getLine_num());
            back();
            expr(var_num);
            arglist(var_num);
        } else {
            //错误
            synterror(Error.ParserError.arglistwrong, Lexer.getLine_num());
        }
    }

    private Var_record expr(int[] var_num) {
        Var_record factor1 = aloexp(var_num);
        Var_record factor2 = exptail(factor1, var_num);
        if (factor2 == null) {
            return factor1;
        } else {
            return factor2;
        }
    }

    private Var_record exptail(Var_record factor1, int[] var_num) {
        nextToken();
        if (token.getTag() == Tag.TK_GT || token.getTag() == Tag.TK_GEQ || token.getTag() == Tag.TK_LT || token.getTag() == Tag.TK_LEQ || token.getTag() == Tag.TK_EQ || token.getTag() == Tag.TK_NEQ) {
            Tag opp = token.getTag();
            Var_record factor2 = expr(var_num);
            return gener.gen_exp(factor1, opp, factor2, var_num);
        } else if (token.getTag() == Tag.TK_IDENT || token.getTag() == Tag.TK_C_NUM || token.getTag() == Tag.TK_C_CHAR || token.getTag() == Tag.TK_C_STR || token.getTag() == Tag.TK_OPEN_PA) {
            //错误
            synterror(Error.ParserError.opplost, Lexer.getLine_num());
            back();
            expr(var_num);
        } else if (token.getTag() == Tag.TK_SEMICOLON || token.getTag() == Tag.TK_CLOSE_PA || token.getTag() == Tag.TK_COMMA || token.getTag() == Tag.TK_RBRACE || token.getTag() == Tag.KW_RETURN || token.getTag() == Tag.KW_BREAK || token.getTag() == Tag.KW_CONTINUE || token.getTag() == Tag.KW_IN || token.getTag() == Tag.KW_OUT || token.getTag() == Tag.KW_WHILE || token.getTag() == Tag.KW_IF || token.getTag() == Tag.KW_INT || token.getTag() == Tag.KW_VOID || token.getTag() == Tag.KW_CHAR || token.getTag() == Tag.KW_STRING) {
            back();
            return null;
        } else {
            nextToken();
            if (token.getTag() == Tag.TK_SEMICOLON || token.getTag() == Tag.TK_CLOSE_PA || token.getTag() == Tag.TK_COMMA) {
                //error
                synterror(Error.ParserError.oppwrong, Lexer.getLine_num());
                back();
                return null;
            } else if (token.getTag() == Tag.TK_IDENT || token.getTag() == Tag.TK_C_NUM || token.getTag() == Tag.TK_C_CHAR || token.getTag() == Tag.TK_C_STR || token.getTag() == Tag.TK_OPEN_PA) {
                //error
                synterror(Error.ParserError.oppwrong, Lexer.getLine_num());
                back();
                expr(var_num);
            } else {
                //error
                synterror(Error.ParserError.oppwrong, Lexer.getLine_num());
                return null;
            }
        }
        return null;
    }

    private void cmps() {
        switch (token.getTag()) {
            case TK_GT:
                break;
            case TK_GEQ:
                break;
            case TK_LT:
                break;
            case TK_LEQ:
                break;
            case TK_EQ:
                break;
            case TK_NEQ:
                break;
        }
    }

    private Var_record aloexp(int[] var_num) {
        Var_record factor1 = item(var_num);
        Var_record factor2 = itemtail(factor1, var_num);
        if (factor2 == null) {
            return factor1;
        } else {
            return factor2;
        }
    }

    private Var_record itemtail(Var_record factor1, int[] var_num) {
        nextToken();
        if (token.getTag() == Tag.TK_PLUS || token.getTag() == Tag.TK_MINUS) {
            Tag opp = token.getTag();
            Var_record factor2 = aloexp(var_num);
            return gener.gen_exp(factor1, opp, factor2, var_num);
        } else if (token.getTag() == Tag.TK_IDENT || token.getTag() == Tag.TK_C_NUM || token.getTag() == Tag.TK_C_CHAR || token.getTag() == Tag.TK_C_STR || token.getTag() == Tag.TK_OPEN_PA) {
            //error
            synterror(Error.ParserError.opplost, Lexer.getLine_num());
            back();
            aloexp(var_num);
        } else {
            back();
            return null;
        }
        return null;
    }

    private void adds() {
        switch (token.getTag()) {
            case TK_PLUS:
                break;
            case TK_MINUS:
                break;
        }
    }

    private Var_record item(int[] var_num) {
        Var_record factor1 = factor(var_num);
        Var_record factor2 = factortail(factor1, var_num);
        if (factor2 == null) {
            return factor1;
        } else {
            return factor2;
        }
    }

    private Var_record factortail(Var_record factor1, int[] var_num) {
        nextToken();
        if (token.getTag() == Tag.TK_STAR || token.getTag() == Tag.TK_DIVIDE) {
            Tag opp = token.getTag();
            Var_record factor2 = item(var_num);
            return gener.gen_exp(factor1, opp, factor2, var_num);
        } else if (token.getTag() == Tag.TK_IDENT || token.getTag() == Tag.TK_C_NUM || token.getTag() == Tag.TK_C_CHAR || token.getTag() == Tag.TK_C_STR || token.getTag() == Tag.TK_OPEN_PA) {
            //error
            synterror(Error.ParserError.opplost, Lexer.getLine_num());
            back();
            item(var_num);
        } else {
            back();
            return null;
        }
        return null;
    }

    private void muls() {
        switch (token.getTag()) {
            case TK_STAR:
                break;
            case TK_DIVIDE:
                break;
        }
    }


    private Var_record factor(int[] var_num) {
        nextToken();
        Var_record p_tmpvar = null;
        String refname = "";
        switch (token.getTag()) {
            case TK_IDENT:
                ident_in_expr = 1;
                refname = ((Id) token).getName();
                p_tmpvar = idtail(refname, var_num);
                break;
            case TK_C_NUM:
                p_tmpvar = tfun.create_tmp_var(new Token(Tag.KW_INT), 1, var_num);
                break;
            case TK_C_CHAR:
                p_tmpvar = tfun.create_tmp_var(new Token(Tag.KW_CHAR), 1, var_num);
                break;
            case TK_OPEN_PA:
                p_tmpvar = expr(var_num);
                nextToken();
                if (token.getTag() != Tag.TK_CLOSE_PA) {
                    //错误
                    synterror(Error.ParserError.exprparenlost, Lexer.getLine_num());
                    back();
                }
                break;
            case TK_C_STR:
                p_tmpvar = tfun.create_tmp_var(new Token(Tag.KW_STRING), 1, var_num);
                break;
            default:
                if (token.getTag() == Tag.TK_CLOSE_PA || token.getTag() == Tag.TK_SEMICOLON || token.getTag() == Tag.TK_COMMA || token.getTag() == Tag.TK_GT || token.getTag() == Tag.TK_GEQ || token.getTag() == Tag.TK_LT || token.getTag() == Tag.TK_LEQ || token.getTag() == Tag.TK_EQ || token.getTag() == Tag.TK_NEQ || token.getTag() == Tag.TK_PLUS || token.getTag() == Tag.TK_MINUS || token.getTag() == Tag.TK_STAR || token.getTag() == Tag.TK_DIVIDE) {
                    //错误
                    synterror(Error.ParserError.exprlost, Lexer.getLine_num());
                    back();
                } else {
                    //错误
                    synterror(Error.ParserError.exprwrong, Lexer.getLine_num());
                }
        }
        return p_tmpvar;
    }

}

