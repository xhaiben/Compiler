package cacher.parser;

import cacher.error.Error;
import cacher.lexer.Id;
import cacher.lexer.Lexer;
import cacher.lexer.Tag;
import cacher.lexer.Token;

import static cacher.error.Error.ParserError.synterror;

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
        } else {
            dec();
            program();
        }
    }

    private void dec() {
        if (token.getTag() == Tag.TK_SEMICOLON) {
            return;
        } else if (token.getTag() == Tag.KW_EXTERN) {
            nextToken();
            type();
            nextToken();
            if (token.getTag() != Tag.TK_IDENT) {
                synterror(Error.ParserError.identlost, lexer.getLine_num());
                back();
            } else {
                //声明标识符
            }
            nextToken();
            if (token.getTag() != Tag.TK_SEMICOLON) {
                if (token.getTag() == Tag.KW_EXTERN || token.getTag() == Tag.KW_VOID || token.getTag() == Tag.KW_INT || token.getTag() == Tag.KW_CHAR || token.getTag() == Tag.KW_STRING) {
                    //丢失分号
                    synterror(Error.ParserError.semiconlost, lexer.getLine_num());
                    back();
                } else {
                    //分号错误
                    synterror(Error.ParserError.semiconwrong, lexer.getLine_num());
                }
            }
        } else {
            String dec_name = "";
            Tag dec_type = type();
            nextToken();
            if (token.getTag() != Tag.TK_IDENT) {
                //error
                synterror(Error.ParserError.identlost, lexer.getLine_num());
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
                synterror(Error.ParserError.typelost, lexer.getLine_num());
                back();
                break;
            default:
                //error
                synterror(Error.ParserError.typewrong, lexer.getLine_num());
                break;
        }
        return null;
    }

    private void dectail(Tag dec_type, String dec_name) {
        nextToken();
        switch (token.getTag()) {
            case TK_SEMICOLON:
                break;
            case TK_OPEN_PA:
                para();
                funtail(dec_type, dec_name);
                break;
            default:
                varlist(dec_type);
                break;
        }
    }

    private int fun_level = 0;

    private void funtail(Tag dec_type, String dec_name) {
        nextToken();
        if (token.getTag() == Tag.TK_SEMICOLON) { //函数声明
            return;
        } else if (token.getTag() == Tag.TK_LBRACE) { //函数定义
            back();
            block(0, 0, 0);
            fun_level = 0;
            return;
        } else if (token.getTag() == Tag.TK_IDENT || token.getTag() == Tag.KW_IF || token.getTag() == Tag.KW_WHILE || token.getTag() == Tag.KW_RETURN || token.getTag() == Tag.KW_BREAK || token.getTag() == Tag.KW_CONTINUE || token.getTag() == Tag.TK_RBRACE) {
            back();
            block(0, 0, 0);
            fun_level = 0;
            return;
        } else if (token.getTag() == Tag.KW_VOID || token.getTag() == Tag.KW_INT || token.getTag() == Tag.KW_CHAR) {
            synterror(Error.ParserError.semiconlost, lexer.getLine_num());
            back();
            return;
        } else {
            synterror(Error.ParserError.semiconwrong, lexer.getLine_num());
            return;
        }
    }

    private void varlist(Tag dec_type) {
        if (token.getTag() == Tag.TK_COMMA) {
            nextToken();
            if (token.getTag() != Tag.TK_IDENT) {
                //错误
                back();
                synterror(Error.ParserError.identlost, lexer.getLine_num());
            } else {
                String dec_name = "";
                dec_name += ((Id) token).getName();

            }
            nextToken();
            varlist(dec_type);
        } else if (token.getTag() == Tag.TK_SEMICOLON) {
            return;
        } else if (token.getTag() == Tag.TK_IDENT) {
            //错误
            synterror(Error.ParserError.commalost, lexer.getLine_num());
            nextToken();
            varlist(dec_type);
        } else if (token.getTag() == Tag.KW_INT || token.getTag() == Tag.KW_VOID || token.getTag() == Tag.KW_CHAR || token.getTag() == Tag.KW_STRING) {
            //错误
            synterror(Error.ParserError.semiconlost, lexer.getLine_num());
            back();
        } else {
            //错误
            synterror(Error.ParserError.semiconwrong, lexer.getLine_num());
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
                    synterror(Error.ParserError.paralost, lexer.getLine_num());
                    back();
                } else {
                    para_name += ((Id) token).getName();
                    //形式参数
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
                synterror(Error.ParserError.paralost, lexer.getLine_num());
                back();
            } else {
                para_name += ((Id) token).getName();
                //形式参数
            }
            paralist();
        } else if (token.getTag() == Tag.TK_LBRACE || token.getTag() == Tag.TK_SEMICOLON) {
            // 缺失右括号
            synterror(Error.ParserError.rparenlost, lexer.getLine_num());
            back();
        } else if (token.getTag() == Tag.TK_CLOSE_PA) {
            return;
        } else if (token.getTag() == Tag.KW_INT || token.getTag() == Tag.KW_VOID || token.getTag() == Tag.KW_CHAR || token.getTag() == Tag.KW_STRING) {
            // 逗号缺失
            synterror(Error.ParserError.commalost, lexer.getLine_num());
            nextToken();
            if (token.getTag() != Tag.TK_IDENT) {
                // paralost
                synterror(Error.ParserError.paralost, lexer.getLine_num());
                back();
            }
            paralist();
        }
    }

    private void block(int initvar_num, int lopId, int blockAddr) {
        nextToken();
        if (token.getTag() != Tag.TK_LBRACE) {
            //左花括号缺失
            synterror(Error.ParserError.lbraclost, lexer.getLine_num());
            back();
        }
        int var_num = initvar_num;

        childprogram();

    }

    private int r_brac_is_lost = 0;

    private void childprogram() {
        nextToken();
        if (token.getTag() == Tag.TK_SEMICOLON || token.getTag() == Tag.KW_WHILE || token.getTag() == Tag.KW_IF || token.getTag() == Tag.KW_RETURN || token.getTag() == Tag.TK_IDENT || token.getTag() == Tag.KW_BREAK || token.getTag() == Tag.KW_CONTINUE || token.getTag() == Tag.KW_IN || token.getTag() == Tag.KW_OUT) {
            statement();
            childprogram();
        } else if (token.getTag() == Tag.KW_VOID || token.getTag() == Tag.KW_INT || token.getTag() == Tag.KW_CHAR || token.getTag() == Tag.KW_STRING) {
            localdec();
            if (r_brac_is_lost == 1) {
                r_brac_is_lost = 0;
            } else {
                childprogram();
            }
        } else if (token.getTag() == Tag.TK_RBRACE) { //复合语句结尾
            return;
        } else if (token == null) {
            //右花括号丢失
            synterror(Error.ParserError.rbraclost, lexer.getLine_num());
        } else {
            //语句异常
            synterror(Error.ParserError.statementexcp, lexer.getLine_num());
        }
    }

    private void localdec() {
        type();
        nextToken();
        if (token.getTag() != Tag.TK_IDENT) {
            //标识符不匹配
            synterror(Error.ParserError.identlost, lexer.getLine_num());
            back();
        } else {
            //定义局部变量
        }
        nextToken();
        localdectail();
    }

    private void localdectail() {
        if (token.getTag() == Tag.TK_COMMA) {
            nextToken();
            if (token.getTag() != Tag.TK_IDENT) {
                back();
                synterror(Error.ParserError.localidentlost, lexer.getLine_num());
            } else {
                //定义局部变量
            }
            nextToken();
            localdectail();
        } else if (token.getTag() == Tag.TK_SEMICOLON) {
            return;
        } else if (token.getTag() == Tag.TK_IDENT) {
            //逗号缺失
            synterror(Error.ParserError.commalost, lexer.getLine_num());
            nextToken();
            localdectail();
        } else if (token.getTag() == Tag.KW_INT || token.getTag() == Tag.KW_VOID || token.getTag() == Tag.KW_CHAR || token.getTag() == Tag.KW_STRING || token.getTag() == Tag.TK_RBRACE) {
            synterror(Error.ParserError.semiconlost, lexer.getLine_num());
            back();
        } else if (token.getTag() == Tag.TK_OPEN_PA) {
            r_brac_is_lost = 1;
            synterror(Error.ParserError.rbraclost, lexer.getLine_num());
            para();
            block(0, 0, 0);
        } else {
            synterror(Error.ParserError.semiconwrong, lexer.getLine_num());
        }
    }

    private void statement() {
        switch (token.getTag()) {
            case TK_SEMICOLON:
                break;
            case KW_WHILE:
                whilestat();
                break;
            case KW_IF:
                ifstat();
                break;
            case KW_BREAK:
                nextToken();
                if (token.getTag() == Tag.TK_IDENT || token.getTag() == Tag.KW_WHILE || token.getTag() == Tag.KW_IF || token.getTag() == Tag.KW_RETURN || token.getTag() == Tag.KW_BREAK || token.getTag() == Tag.KW_CONTINUE || token.getTag() == Tag.KW_IN || token.getTag() == Tag.KW_OUT || token.getTag() == Tag.TK_RBRACE) {
                    //分号缺失
                    synterror(Error.ParserError.semiconlost, lexer.getLine_num());
                    back();
                } else if (token.getTag() != Tag.TK_SEMICOLON) {
                    //分号错误
                    synterror(Error.ParserError.semiconwrong, lexer.getLine_num());
                }
                //生成break
                break;
            case KW_CONTINUE:
                nextToken();
                if (token.getTag() == Tag.TK_IDENT || token.getTag() == Tag.KW_WHILE || token.getTag() == Tag.KW_IF || token.getTag() == Tag.KW_RETURN || token.getTag() == Tag.KW_BREAK || token.getTag() == Tag.KW_CONTINUE || token.getTag() == Tag.KW_IN || token.getTag() == Tag.KW_OUT || token.getTag() == Tag.TK_RBRACE) {
                    //分号缺失
                    synterror(Error.ParserError.semiconlost, lexer.getLine_num());
                    back();
                } else if (token.getTag() != Tag.TK_SEMICOLON) {
                    //分号错误
                    synterror(Error.ParserError.semiconwrong, lexer.getLine_num());
                }
                //生成continue
                break;
            case KW_RETURN:
                retstat();
                break;
            case KW_IN:
                nextToken();
                if (token.getTag() != Tag.TK_IN_PUT) {
                    //输入错误
                    synterror(Error.ParserError.input_err, lexer.getLine_num());
                }
                nextToken();
                if (token.getTag() != Tag.TK_IDENT) {
                    //无效输入
                    synterror(Error.ParserError.na_input, lexer.getLine_num());
                } else {
                    //获取输入
                }
                nextToken();
                if (token.getTag() != Tag.TK_SEMICOLON) {
                    synterror(Error.ParserError.semiconlost, lexer.getLine_num());
                    back();
                }
                break;
            case KW_OUT:
                nextToken();
                if (token.getTag() != Tag.TK_OUT_PUT) {
                    //输出错误
                    synterror(Error.ParserError.output_err, lexer.getLine_num());
                }
                //输出
                expr();
                nextToken();
                if (token.getTag() != Tag.TK_SEMICOLON) {
                    //无效输出
                    synterror(Error.ParserError.semiconlost, lexer.getLine_num());
                    back();
                }
                break;
            case TK_IDENT:
                idtail();
                nextToken();
                if (token.getTag() != Tag.TK_SEMICOLON) {
                    synterror(Error.ParserError.semiconlost, lexer.getLine_num());
                    back();
                }
                break;
        }
    }

    private void whilestat() {
        nextToken();
        if (token.getTag() != Tag.TK_OPEN_PA) {
            if (token.getTag() == Tag.TK_IDENT || token.getTag() == Tag.TK_C_NUM || token.getTag() == Tag.TK_C_STR || token.getTag() == Tag.TK_C_CHAR || token.getTag() == Tag.TK_OPEN_PA) {
                //左括号缺失
                synterror(Error.ParserError.lparenlost, lexer.getLine_num());
                back();
            } else {
                //符号错误
                synterror(Error.ParserError.lparenwrong, lexer.getLine_num());
            }
        }
        nextToken();
        if (token.getTag() != Tag.TK_CLOSE_PA) {
            if (token.getTag() == Tag.TK_LBRACE || token.getTag() == Tag.TK_SEMICOLON || token.getTag() == Tag.KW_WHILE || token.getTag() == Tag.KW_IF || token.getTag() == Tag.KW_RETURN || token.getTag() == Tag.KW_BREAK || token.getTag() == Tag.KW_CONTINUE || token.getTag() == Tag.KW_IN || token.getTag() == Tag.KW_OUT || token.getTag() == Tag.TK_IDENT || token.getTag() == Tag.KW_VOID || token.getTag() == Tag.KW_INT || token.getTag() == Tag.KW_CHAR || token.getTag() == Tag.KW_STRING) {
                // 右括号缺失
                synterror(Error.ParserError.staterparenlost, lexer.getLine_num());
                back();
            } else {
                //无效字符
                synterror(Error.ParserError.rparenwrong, lexer.getLine_num());
            }
        }
        block(0, 0, 0);

    }

    private void ifstat() {
        nextToken();
        if (token.getTag() != Tag.TK_OPEN_PA) {
            if (token.getTag() == Tag.TK_IDENT || token.getTag() == Tag.TK_C_NUM || token.getTag() == Tag.TK_C_CHAR || token.getTag() == Tag.TK_C_STR || token.getTag() == Tag.TK_OPEN_PA) {
                //左括号丢失
                synterror(Error.ParserError.lparenlost, lexer.getLine_num());
                back();
            } else {
                //无效字符
                synterror(Error.ParserError.lparenwrong, lexer.getLine_num());
            }
        }
        nextToken();
        if (token.getTag() != Tag.TK_CLOSE_PA) {
            if (token.getTag() == Tag.TK_LBRACE || token.getTag() == Tag.TK_SEMICOLON || token.getTag() == Tag.KW_WHILE || token.getTag() == Tag.KW_IF || token.getTag() == Tag.KW_RETURN || token.getTag() == Tag.KW_BREAK || token.getTag() == Tag.KW_CONTINUE || token.getTag() == Tag.KW_IN || token.getTag() == Tag.KW_OUT || token.getTag() == Tag.TK_IDENT || token.getTag() == Tag.KW_VOID || token.getTag() == Tag.KW_INT || token.getTag() == Tag.KW_CHAR || token.getTag() == Tag.KW_STRING) {
                // 右括号缺失
                synterror(Error.ParserError.staterparenlost, lexer.getLine_num());
                back();
            } else {
                //无效字符
                synterror(Error.ParserError.rparenwrong, lexer.getLine_num());
            }
        }
        block(0, 0, 0);
        nextToken();
        if (token.getTag() != Tag.KW_ELSE) {
            if (token.getTag() == Tag.TK_LBRACE || token.getTag() == Tag.TK_SEMICOLON || token.getTag() == Tag.KW_WHILE || token.getTag() == Tag.KW_IF || token.getTag() == Tag.KW_RETURN || token.getTag() == Tag.KW_BREAK || token.getTag() == Tag.KW_CONTINUE || token.getTag() == Tag.KW_IN || token.getTag() == Tag.KW_OUT || token.getTag() == Tag.KW_VOID || token.getTag() == Tag.KW_INT || token.getTag() == Tag.KW_CHAR || token.getTag() == Tag.KW_STRING) {
                //else丢失
                synterror(Error.ParserError.elselost, lexer.getLine_num());
                back();
            } else if (token.getTag() == Tag.TK_IDENT) {
                // else 拼写错误
                synterror(Error.ParserError.elsespelterr, lexer.getLine_num());
            } else {
                //非法字符
                synterror(Error.ParserError.elsewrong, lexer.getLine_num());
            }
        } else {

        }
        block(0, 0, 0);
    }

    private void retstat() {
        returntail();
        nextToken();
        if (token.getTag() != Tag.TK_SEMICOLON) {
            if (token.getTag() == Tag.TK_RBRACE) {
                //分号丢失
                synterror(Error.ParserError.semiconlost, lexer.getLine_num());
                back();
            }
        }
    }

    private void returntail() {
        nextToken();
        if (token.getTag() == Tag.TK_IDENT || token.getTag() == Tag.TK_C_NUM || token.getTag() == Tag.TK_C_CHAR || token.getTag() == Tag.TK_C_STR || token.getTag() == Tag.TK_OPEN_PA) {
            back();
            expr();

        } else if (token.getTag() == Tag.TK_SEMICOLON) {
            back();
            return;
        } else if (token.getTag() == Tag.TK_RBRACE) {
            back();
            return;
        } else {
            synterror(Error.ParserError.returnwrong, lexer.getLine_num());
        }
    }

    private void idtail() {
        nextToken();
        if (token.getTag() == Tag.TK_ASSIGN) {
            expr();

        } else if (token.getTag() == Tag.TK_OPEN_PA) {
            realarg();
            nextToken();
            if (token.getTag() != Tag.TK_CLOSE_PA) {
                //右括号缺失
                synterror(Error.ParserError.rparenlost, lexer.getLine_num());
                back();
            }
        } else if (ident_in_expr == 1) {
            ident_in_expr = 0;
            back();
        } else {
            synterror(Error.ParserError.idtaillost, lexer.getLine_num());
        }
    }

    private void realarg() {
        nextToken();
        if (token.getTag() == Tag.TK_IDENT || token.getTag() == Tag.TK_C_NUM || token.getTag() == Tag.TK_C_CHAR || token.getTag() == Tag.TK_C_STR || token.getTag() == Tag.TK_OPEN_PA) {
            back();
            arglist();
        } else if (token.getTag() == Tag.TK_CLOSE_PA || token.getTag() == Tag.TK_SEMICOLON) {
            back();
        } else if (token.getTag() == Tag.TK_COMMA) {
            synterror(Error.ParserError.arglost, lexer.getLine_num());
            back();
            arglist();
        } else {
            //错误
            synterror(Error.ParserError.argwrong, lexer.getLine_num());
        }
    }

    private void arglist() {
        nextToken();
        if (token.getTag() == Tag.TK_COMMA) {
            nextToken();
            if (token.getTag() == Tag.TK_IDENT || token.getTag() == Tag.TK_C_NUM || token.getTag() == Tag.TK_C_CHAR || token.getTag() == Tag.TK_C_STR || token.getTag() == Tag.TK_OPEN_PA) {
                back();
                arglist();
            } else if (token.getTag() == Tag.TK_COMMA) {
                synterror(Error.ParserError.arglost, lexer.getLine_num());
                back();
                arglist();
            } else if (token.getTag() == Tag.TK_SEMICOLON || token.getTag() == Tag.TK_CLOSE_PA) {
                synterror(Error.ParserError.arglost, lexer.getLine_num());
                back();
                return;
            } else {
                //错误
                synterror(Error.ParserError.argwrong, lexer.getLine_num());
            }
        } else if (token.getTag() == Tag.TK_CLOSE_PA || token.getTag() == Tag.TK_SEMICOLON) {
            back();
            return;
        } else if (token.getTag() == Tag.TK_IDENT || token.getTag() == Tag.TK_C_NUM || token.getTag() == Tag.TK_C_CHAR || token.getTag() == Tag.TK_C_STR || token.getTag() == Tag.TK_OPEN_PA) {
            //错误
            synterror(Error.ParserError.commalost, lexer.getLine_num());
            back();
            expr();
            arglist();
        } else {
            //错误
            synterror(Error.ParserError.arglistwrong, lexer.getLine_num());
        }
    }

    private void expr() {
        aloexp();
        exptail();
    }

    private void exptail() {
        nextToken();
        if (token.getTag() == Tag.TK_GT || token.getTag() == Tag.TK_GEQ || token.getTag() == Tag.TK_LT || token.getTag() == Tag.TK_LEQ || token.getTag() == Tag.TK_EQ || token.getTag() == Tag.TK_NEQ) {
            expr();
        } else if (token.getTag() == Tag.TK_IDENT || token.getTag() == Tag.TK_C_NUM || token.getTag() == Tag.TK_C_CHAR || token.getTag() == Tag.TK_C_STR || token.getTag() == Tag.TK_OPEN_PA) {
            //错误
            synterror(Error.ParserError.opplost, lexer.getLine_num());
            back();
            expr();
        } else if (token.getTag() == Tag.TK_SEMICOLON || token.getTag() == Tag.TK_CLOSE_PA || token.getTag() == Tag.TK_COMMA || token.getTag() == Tag.TK_RBRACE || token.getTag() == Tag.KW_RETURN || token.getTag() == Tag.KW_BREAK || token.getTag() == Tag.KW_CONTINUE || token.getTag() == Tag.KW_IN || token.getTag() == Tag.KW_OUT || token.getTag() == Tag.KW_WHILE || token.getTag() == Tag.KW_IF || token.getTag() == Tag.KW_INT || token.getTag() == Tag.KW_VOID || token.getTag() == Tag.KW_CHAR || token.getTag() == Tag.KW_STRING) {
            back();
        } else {
            nextToken();
            if (token.getTag() == Tag.TK_SEMICOLON || token.getTag() == Tag.TK_CLOSE_PA || token.getTag() == Tag.TK_COMMA) {
                //error
                synterror(Error.ParserError.oppwrong, lexer.getLine_num());
                back();
            } else if (token.getTag() == Tag.TK_IDENT || token.getTag() == Tag.TK_C_NUM || token.getTag() == Tag.TK_C_CHAR || token.getTag() == Tag.TK_C_STR || token.getTag() == Tag.TK_OPEN_PA) {
                //error
                synterror(Error.ParserError.oppwrong, lexer.getLine_num());
                back();
                expr();
            } else {
                //error
                synterror(Error.ParserError.oppwrong, lexer.getLine_num());
            }
        }
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

    private void aloexp() {
        item();
        itemtail();
    }

    private void itemtail() {
        nextToken();
        if (token.getTag() == Tag.TK_PLUS || token.getTag() == Tag.TK_MINUS) {
            aloexp();
        } else if (token.getTag() == Tag.TK_IDENT || token.getTag() == Tag.TK_C_NUM || token.getTag() == Tag.TK_C_CHAR || token.getTag() == Tag.TK_C_STR || token.getTag() == Tag.TK_OPEN_PA) {
            //error
            synterror(Error.ParserError.opplost, lexer.getLine_num());
            back();
            aloexp();
        } else {
            back();
        }
    }

    private void adds() {
        switch (token.getTag()) {
            case TK_PLUS:
                break;
            case TK_MINUS:
                break;
        }
    }

    private void item() {
        factor();
        factortail();
    }

    private void factortail() {
        nextToken();
        if (token.getTag() == Tag.TK_STAR || token.getTag() == Tag.TK_DIVIDE) {
            item();
        } else if (token.getTag() == Tag.TK_IDENT || token.getTag() == Tag.TK_C_NUM || token.getTag() == Tag.TK_C_CHAR || token.getTag() == Tag.TK_C_STR || token.getTag() == Tag.TK_OPEN_PA) {
            //error
            synterror(Error.ParserError.opplost, lexer.getLine_num());
            back();
            item();
        } else {
            back();
            return;
        }
        return;
    }

    private void muls() {
        switch (token.getTag()) {
            case TK_STAR:
                break;
            case TK_DIVIDE:
                break;
        }
    }


    private void factor() {
        nextToken();
        switch (token.getTag()) {
            case TK_IDENT:
                ident_in_expr = 1;
                idtail();
                break;
            case TK_C_NUM:
                break;
            case TK_C_CHAR:
                break;
            case TK_OPEN_PA:
                nextToken();
                if (token.getTag() != Tag.TK_CLOSE_PA) {
                    //错误
                    synterror(Error.ParserError.exprparenlost, lexer.getLine_num());
                    back();
                }
                break;
            case TK_C_STR:
                break;
            default:
                if (token.getTag() == Tag.TK_CLOSE_PA || token.getTag() == Tag.TK_SEMICOLON || token.getTag() == Tag.TK_COMMA || token.getTag() == Tag.TK_GT || token.getTag() == Tag.TK_GEQ || token.getTag() == Tag.TK_LT || token.getTag() == Tag.TK_LEQ || token.getTag() == Tag.TK_EQ || token.getTag() == Tag.TK_NEQ || token.getTag() == Tag.TK_PLUS || token.getTag() == Tag.TK_MINUS || token.getTag() == Tag.TK_STAR || token.getTag() == Tag.TK_DIVIDE) {
                    //错误
                    synterror(Error.ParserError.exprlost, lexer.getLine_num());
                    back();
                } else {
                    //错误
                    synterror(Error.ParserError.exprwrong, lexer.getLine_num());
                }
        }
        return;
    }

}

