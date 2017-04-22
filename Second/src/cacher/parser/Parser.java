package cacher.parser;

import cacher.lexer.Id;
import cacher.lexer.Lexer;
import cacher.lexer.Tag;
import cacher.lexer.Token;

import java.util.List;

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
                synterror(ParserError.identlost);
                back();
            } else {
                //声明标识符
            }
            nextToken();
            if (token.getTag() != Tag.TK_SEMICOLON) {
                if (token.getTag() == Tag.KW_EXTERN || token.getTag() == Tag.KW_VOID || token.getTag() == Tag.KW_INT || token.getTag() == Tag.KW_CHAR || token.getTag() == Tag.KW_STRING) {
                    //丢失分号
                    synterror(ParserError.semiconlost);
                    back();
                } else {
                    //分号错误
                    synterror(ParserError.semiconwrong);
                }
            }
        } else {
            String dec_name = "";
            Tag dec_type = type();
            nextToken();
            if (token.getTag() != Tag.TK_IDENT) {
                //error
                synterror(ParserError.identlost);
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
                synterror(ParserError.typelost);
                back();
                break;
            default:
                //error
                synterror(ParserError.typewrong);
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
            synterror(ParserError.semiconlost);
            back();
            return;
        } else {
            synterror(ParserError.semiconwrong);
            return;
        }
    }

    private void varlist(Tag dec_type) {
        if (token.getTag() == Tag.TK_COMMA) {
            nextToken();
            if (token.getTag() != Tag.TK_IDENT) {
                //错误
                back();
                synterror(ParserError.identlost);
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
            synterror(ParserError.commalost);
            nextToken();
            varlist(dec_type);
        } else if (token.getTag() == Tag.KW_INT || token.getTag() == Tag.KW_VOID || token.getTag() == Tag.KW_CHAR || token.getTag() == Tag.KW_STRING) {
            //错误
            synterror(ParserError.semiconlost);
            back();
        } else {
            //错误
            synterror(ParserError.semiconwrong);
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
                    synterror(ParserError.paralost);
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
                synterror(ParserError.paralost);
                back();
            } else {
                para_name += ((Id) token).getName();
                //形式参数
            }
            paralist();
        } else if (token.getTag() == Tag.TK_LBRACE || token.getTag() == Tag.TK_SEMICOLON) {
            // 缺失右括号
            synterror(ParserError.rparenlost);
            back();
        } else if (token.getTag() == Tag.TK_CLOSE_PA) {
            return;
        } else if (token.getTag() == Tag.KW_INT || token.getTag() == Tag.KW_VOID || token.getTag() == Tag.KW_CHAR || token.getTag() == Tag.KW_STRING) {
            // 逗号缺失
            synterror(ParserError.commalost);
            nextToken();
            if (token.getTag() != Tag.TK_IDENT) {
                // paralost
                synterror(ParserError.paralost);
                back();
            }
            paralist();
        }
    }

    private void block(int initvar_num, int lopId, int blockAddr) {
        nextToken();
        if (token.getTag() != Tag.TK_LBRACE) {
            //左花括号缺失
            synterror(ParserError.lbraclost);
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
            synterror(ParserError.rbraclost);
        } else {
            //语句异常
            synterror(ParserError.statementexcp);
        }
    }

    private void localdec() {
        type();
        nextToken();
        if (token.getTag() != Tag.TK_IDENT) {
            //标识符不匹配
            synterror(ParserError.identlost);
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
                synterror(ParserError.localidentlost);
            } else {
                //定义局部变量
            }
            nextToken();
            localdectail();
        } else if (token.getTag() == Tag.TK_SEMICOLON) {
            return;
        } else if (token.getTag() == Tag.TK_IDENT) {
            //逗号缺失
            synterror(ParserError.commalost);
            nextToken();
            localdectail();
        } else if (token.getTag() == Tag.KW_INT || token.getTag() == Tag.KW_VOID || token.getTag() == Tag.KW_CHAR || token.getTag() == Tag.KW_STRING || token.getTag() == Tag.TK_RBRACE) {
            synterror(ParserError.semiconlost);
            back();
        } else if (token.getTag() == Tag.TK_OPEN_PA) {
            r_brac_is_lost = 1;
            synterror(ParserError.rbraclost);
            para();
            block(0, 0, 0);
        } else {
            synterror(ParserError.semiconwrong);
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
                    synterror(ParserError.semiconlost);
                    back();
                } else if (token.getTag() != Tag.TK_SEMICOLON) {
                    //分号错误
                    synterror(ParserError.semiconwrong);
                }
                //生成break
                break;
            case KW_CONTINUE:
                nextToken();
                if (token.getTag() == Tag.TK_IDENT || token.getTag() == Tag.KW_WHILE || token.getTag() == Tag.KW_IF || token.getTag() == Tag.KW_RETURN || token.getTag() == Tag.KW_BREAK || token.getTag() == Tag.KW_CONTINUE || token.getTag() == Tag.KW_IN || token.getTag() == Tag.KW_OUT || token.getTag() == Tag.TK_RBRACE) {
                    //分号缺失
                    synterror(ParserError.semiconlost);
                    back();
                } else if (token.getTag() != Tag.TK_SEMICOLON) {
                    //分号错误
                    synterror(ParserError.semiconwrong);
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
                    synterror(ParserError.input_err);
                }
                nextToken();
                if (token.getTag() != Tag.TK_IDENT) {
                    //无效输入
                    synterror(ParserError.na_input);
                } else {
                    //获取输入
                }
                nextToken();
                if (token.getTag() != Tag.TK_SEMICOLON) {
                    synterror(ParserError.semiconlost);
                    back();
                }
                break;
            case KW_OUT:
                nextToken();
                if (token.getTag() != Tag.TK_OUT_PUT) {
                    //输出错误
                    synterror(ParserError.output_err);
                }
                //输出
                expr();
                nextToken();
                if (token.getTag() != Tag.TK_SEMICOLON) {
                    //无效输出
                    synterror(ParserError.semiconlost);
                    back();
                }
                break;
            case TK_IDENT:
                idtail();
                nextToken();
                if (token.getTag() != Tag.TK_SEMICOLON) {
                    synterror(ParserError.semiconlost);
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
                synterror(ParserError.lparenlost);
                back();
            } else {
                //符号错误
                synterror(ParserError.lparenwrong);
            }
        }
        nextToken();
        if (token.getTag() != Tag.TK_CLOSE_PA) {
            if (token.getTag() == Tag.TK_LBRACE || token.getTag() == Tag.TK_SEMICOLON || token.getTag() == Tag.KW_WHILE || token.getTag() == Tag.KW_IF || token.getTag() == Tag.KW_RETURN || token.getTag() == Tag.KW_BREAK || token.getTag() == Tag.KW_CONTINUE || token.getTag() == Tag.KW_IN || token.getTag() == Tag.KW_OUT || token.getTag() == Tag.TK_IDENT || token.getTag() == Tag.KW_VOID || token.getTag() == Tag.KW_INT || token.getTag() == Tag.KW_CHAR || token.getTag() == Tag.KW_STRING) {
                // 右括号缺失
                synterror(ParserError.staterparenlost);
                back();
            } else {
                //无效字符
                synterror(ParserError.rparenwrong);
            }
        }
        block(0, 0, 0);

    }

    private void ifstat() {
        nextToken();
        if (token.getTag() != Tag.TK_OPEN_PA) {
            if (token.getTag() == Tag.TK_IDENT || token.getTag() == Tag.TK_C_NUM || token.getTag() == Tag.TK_C_CHAR || token.getTag() == Tag.TK_C_STR || token.getTag() == Tag.TK_OPEN_PA) {
                //左括号丢失
                synterror(ParserError.lparenlost);
                back();
            } else {
                //无效字符
                synterror(ParserError.lparenwrong);
            }
        }
        nextToken();
        if (token.getTag() != Tag.TK_CLOSE_PA) {
            if (token.getTag() == Tag.TK_LBRACE || token.getTag() == Tag.TK_SEMICOLON || token.getTag() == Tag.KW_WHILE || token.getTag() == Tag.KW_IF || token.getTag() == Tag.KW_RETURN || token.getTag() == Tag.KW_BREAK || token.getTag() == Tag.KW_CONTINUE || token.getTag() == Tag.KW_IN || token.getTag() == Tag.KW_OUT || token.getTag() == Tag.TK_IDENT || token.getTag() == Tag.KW_VOID || token.getTag() == Tag.KW_INT || token.getTag() == Tag.KW_CHAR || token.getTag() == Tag.KW_STRING) {
                // 右括号缺失
                synterror(ParserError.staterparenlost);
                back();
            } else {
                //无效字符
                synterror(ParserError.rparenwrong);
            }
        }
        block(0, 0, 0);
        nextToken();
        if (token.getTag() != Tag.KW_ELSE) {
            if (token.getTag() == Tag.TK_LBRACE || token.getTag() == Tag.TK_SEMICOLON || token.getTag() == Tag.KW_WHILE || token.getTag() == Tag.KW_IF || token.getTag() == Tag.KW_RETURN || token.getTag() == Tag.KW_BREAK || token.getTag() == Tag.KW_CONTINUE || token.getTag() == Tag.KW_IN || token.getTag() == Tag.KW_OUT || token.getTag() == Tag.KW_VOID || token.getTag() == Tag.KW_INT || token.getTag() == Tag.KW_CHAR || token.getTag() == Tag.KW_STRING) {
                //else丢失
                synterror(ParserError.elselost);
                back();
            } else if (token.getTag() == Tag.TK_IDENT) {
                // else 拼写错误
                synterror(ParserError.elsespelterr);
            } else {
                //非法字符
                synterror(ParserError.elsewrong);
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
                synterror(ParserError.semiconlost);
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
            synterror(ParserError.returnwrong);
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
                synterror(ParserError.rparenlost);
                back();
            }
        } else if (ident_in_expr == 1) {
            ident_in_expr = 0;
            back();
        } else {
            synterror(ParserError.idtaillost);
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
            synterror(ParserError.arglost);
            back();
            arglist();
        } else {
            //错误
            synterror(ParserError.argwrong);
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
                synterror(ParserError.arglost);
                back();
                arglist();
            } else if (token.getTag() == Tag.TK_SEMICOLON || token.getTag() == Tag.TK_CLOSE_PA) {
                synterror(ParserError.arglost);
                back();
                return;
            } else {
                //错误
                synterror(ParserError.argwrong);
            }
        } else if (token.getTag() == Tag.TK_CLOSE_PA || token.getTag() == Tag.TK_SEMICOLON) {
            back();
            return;
        } else if (token.getTag() == Tag.TK_IDENT || token.getTag() == Tag.TK_C_NUM || token.getTag() == Tag.TK_C_CHAR || token.getTag() == Tag.TK_C_STR || token.getTag() == Tag.TK_OPEN_PA) {
            //错误
            synterror(ParserError.commalost);
            back();
            expr();
            arglist();
        } else {
            //错误
            synterror(ParserError.arglistwrong);
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
            synterror(ParserError.opplost);
            back();
            expr();
        } else if (token.getTag() == Tag.TK_SEMICOLON || token.getTag() == Tag.TK_CLOSE_PA || token.getTag() == Tag.TK_COMMA || token.getTag() == Tag.TK_RBRACE || token.getTag() == Tag.KW_RETURN || token.getTag() == Tag.KW_BREAK || token.getTag() == Tag.KW_CONTINUE || token.getTag() == Tag.KW_IN || token.getTag() == Tag.KW_OUT || token.getTag() == Tag.KW_WHILE || token.getTag() == Tag.KW_IF || token.getTag() == Tag.KW_INT || token.getTag() == Tag.KW_VOID || token.getTag() == Tag.KW_CHAR || token.getTag() == Tag.KW_STRING) {
            back();
        } else {
            nextToken();
            if (token.getTag() == Tag.TK_SEMICOLON || token.getTag() == Tag.TK_CLOSE_PA || token.getTag() == Tag.TK_COMMA) {
                //error
                synterror(ParserError.oppwrong);
                back();
            } else if (token.getTag() == Tag.TK_IDENT || token.getTag() == Tag.TK_C_NUM || token.getTag() == Tag.TK_C_CHAR || token.getTag() == Tag.TK_C_STR || token.getTag() == Tag.TK_OPEN_PA) {
                //error
                synterror(ParserError.oppwrong);
                back();
                expr();
            } else {
                //error
                synterror(ParserError.oppwrong);
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
            synterror(ParserError.opplost);
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
            synterror(ParserError.opplost);
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
                    synterror(ParserError.exprparenlost);
                    back();
                }
                break;
            case TK_C_STR:
                break;
            default:
                if (token.getTag() == Tag.TK_CLOSE_PA || token.getTag() == Tag.TK_SEMICOLON || token.getTag() == Tag.TK_COMMA || token.getTag() == Tag.TK_GT || token.getTag() == Tag.TK_GEQ || token.getTag() == Tag.TK_LT || token.getTag() == Tag.TK_LEQ || token.getTag() == Tag.TK_EQ || token.getTag() == Tag.TK_NEQ || token.getTag() == Tag.TK_PLUS || token.getTag() == Tag.TK_MINUS || token.getTag() == Tag.TK_STAR || token.getTag() == Tag.TK_DIVIDE) {
                    //错误
                    synterror(ParserError.exprlost);
                    back();
                } else {
                    //错误
                    synterror(ParserError.exprwrong);
                }
        }
        return;
    }

    private void synterror(ParserError error) {
        errorNum++;
        synerr++;
        System.out.printf("在第 %d 行，语法分析错误   ", this.lexer.getLine_num());
        switch (error) {
            case semiconlost:
                System.out.println("符号 ; 缺失");
                break;
            case commalost:
                System.out.println("符号 , 缺失");
                break;
            case typelost:
                System.out.println("类型错误");
                break;
            case identlost:
                System.out.println("缺失变量名");
                break;
            case semiconwrong:
                System.out.println("符号 ; 错误");
                break;
            case typewrong:
                System.out.println("类型错误");
                break;
            case paralost:
                System.out.println("缺失参数名");
                break;
            case rparenlost:
                System.out.println("缺失 )");
                break;
            case lbraclost:
                System.out.println("缺失 {");
                break;
            case rbraclost:
                System.out.println("缺失 }");
                break;
            case statementexcp:
                System.out.println("无效的语句");
                break;
            case localidentlost:
                System.out.println("缺失变量名");
                break;
            case lparenlost:
                System.out.println("缺失 (");
                break;
            case lparenwrong:
                System.out.println(" ( 错误");
                break;
            case staterparenlost:
                System.out.println("缺失 )");
                break;
            case rparenwrong:
                System.out.println(" ) 错误");
                break;
            case elselost:
                System.out.println("可能缺失 else");
                break;
            case elsespelterr:
                System.out.println("错误的 else");
                break;
            case elsewrong:
                System.out.println("else 错误");
                break;
            case idtaillost:
                System.out.println("变量名错误");
                break;
            case returnwrong:
                System.out.println("返回类型错误");
                break;
            case arglost:
                System.out.println("参数缺失");
                break;
            case argwrong:
                System.out.println("参数错误");
                break;
            case opplost:
                System.out.println("操作符缺失");
                break;
            case oppwrong:
                System.out.println("错误的操作符");
                break;
            case exprlost:
                System.out.println("表达式缺失");
                break;
            case exprparenlost:
                System.out.println("表达式错误");
                break;
            case exprwrong:
                System.out.println("表达式错误");
                break;
            case na_input:
                System.out.println("无效的输入");
                break;
            case input_err:
                System.out.println("输入错误");
                break;
            case output_err:
                System.out.println("输出错误");
                break;
        }

    }
}

enum ParserError {
    semiconlost, commalost, typelost, identlost, semiconwrong, typewrong,//变量声明部分的错误类型
    paralost, rparenlost, lbraclost, rbraclost,//函数定义部分的错误类型
    statementexcp, localidentlost, lparenlost, lparenwrong, staterparenlost, rparenwrong, elselost, elsespelterr, elsewrong,//复合语句部分的错误类型
    idtaillost, returnwrong, arglost, argwrong, arglistwrong, na_input, input_err, output_err,
    opplost, oppwrong, exprlost, exprparenlost, exprwrong
}