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
    private List<Token> tokenList;
    private Token old_token, token;
    private int wait = 0;
    private Token cur_token;
    private int p_token = 0;
    private int compileOK = 0;
    private Lexer lexer;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }

    public Parser(List<Token> tokenList) {
        this.tokenList = tokenList;
    }

    private void back() {
        token = old_token;
        old_token = null;
        wait = 1;
    }

    private int getToken() {
        if (p_token < tokenList.size()) {
            cur_token = tokenList.get(p_token++);
            return 1;
        } else {
            return -1;
        }
    }

    private int nextToken() {
        if (wait == 1) {
            wait = 0;
            return 0;
        }
        int flag = getToken();
        while (true) {
            if (cur_token.getTag() == Tag.ERR) {
                if (flag == -1) {
                    old_token = token;
                    token = null;
                    return -1;
                }
            } else {
                old_token = token;
                token = cur_token;
                return 0;
            }
        }
    }

    private void program() {
        if (nextToken() == -1) {

        } else {
            dec();
            program();
        }
    }

    private void dec() {
        if (token.getTag() == Tag.TK_SEMICOLON) {

        } else if (token.getTag() == Tag.KW_EXTERN) {

        } else {
            String dec_name = "";
            Tag dec_type = type();
            nextToken();
            if (token.getTag() != Tag.TK_IDENT) {
                //error
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
                back();
                break;
            default:
                //error
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
            block(0, 0, 0);
            fun_level = 0;
            return;
        } else if (token.getTag() == Tag.TK_IDENT || token.getTag() == Tag.KW_IF || token.getTag() == Tag.KW_WHILE || token.getTag() == Tag.KW_RETURN || token.getTag() == Tag.KW_BREAK || token.getTag() == Tag.KW_CONTINUE || token.getTag() == Tag.TK_RBRACE) {
            back();
            block(0, 0, 0);
            fun_level = 0;
            return;
        } else if (token.getTag() == Tag.KW_VOID || token.getTag() == Tag.KW_INT || token.getTag() == Tag.KW_CHAR) {
            return;
        } else {
            return;
        }
    }

    private void varlist(Tag dec_type) {
        if (token.getTag() == Tag.TK_COMMA) {
            nextToken();
            if (token.getTag() != Tag.TK_IDENT) {
                //错误
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
            nextToken();
            varlist(dec_type);
        } else if (token.getTag() == Tag.KW_INT || token.getTag() == Tag.KW_VOID || token.getTag() == Tag.KW_CHAR || token.getTag() == Tag.KW_STRING) {
            //错误
            back();
        } else {
            //错误
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
                back();
            } else {
                para_name += ((Id) token).getName();
                //形式参数
            }
            paralist();
        } else if (token.getTag() == Tag.TK_LBRACE || token.getTag() == Tag.TK_SEMICOLON) {
            // 缺失右括号
            back();
        } else if (token.getTag() == Tag.TK_CLOSE_PA) {
            return;
        } else if (token.getTag() == Tag.KW_INT || token.getTag() == Tag.KW_VOID || token.getTag() == Tag.KW_CHAR || token.getTag() == Tag.KW_STRING) {
            // 逗号缺失
            nextToken();
            if (token.getTag() != Tag.TK_IDENT) {
                // paralost
                back();
            }
            paralist();
        }
    }

    private void block(int initvar_num, int lopId, int blockAddr) {
        nextToken();
        if (token.getTag() != Tag.TK_LBRACE) {
            //左花括号缺失
            back();
        }
        int var_num = initvar_num;

        childprogram();

    }

    private void childprogram() {
        nextToken();
        if (token.getTag() == Tag.TK_SEMICOLON || token.getTag() == Tag.KW_WHILE || token.getTag() == Tag.KW_IF || token.getTag() == Tag.KW_RETURN || token.getTag() == Tag.TK_IDENT || token.getTag() == Tag.KW_BREAK || token.getTag() == Tag.KW_CONTINUE || token.getTag() == Tag.KW_IN || token.getTag() == Tag.KW_OUT) {
            statement();
            childprogram();
        } else if (token.getTag() == Tag.KW_VOID || token.getTag() == Tag.KW_INT || token.getTag() == Tag.KW_CHAR || token.getTag() == Tag.KW_STRING) {
            localdec();

        } else if (token.getTag() == Tag.TK_RBRACE) {
            return;
        } else {

        }
    }

    private void localdec() {
        nextToken();
        if (token.getTag() != Tag.TK_IDENT) {
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
            } else {
                //定义局部变量
            }
            nextToken();
            localdectail();
        } else if (token.getTag() == Tag.TK_SEMICOLON) {
            return;
        } else if (token.getTag() == Tag.TK_IDENT) {
            //逗号缺失
            nextToken();
            localdectail();
        } else if (token.getTag() == Tag.KW_INT || token.getTag() == Tag.KW_VOID || token.getTag() == Tag.KW_CHAR || token.getTag() == Tag.KW_STRING || token.getTag() == Tag.TK_RBRACE) {
            back();
        } else if (token.getTag() == Tag.TK_OPEN_PA) {
            para();
            block(0, 0, 0);
        } else {

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
                    back();
                } else if (token.getTag() != Tag.TK_SEMICOLON) {
                    //分号错误
                }
                break;
            case KW_CONTINUE:
                nextToken();
                if (token.getTag() == Tag.TK_IDENT || token.getTag() == Tag.KW_WHILE || token.getTag() == Tag.KW_IF || token.getTag() == Tag.KW_RETURN || token.getTag() == Tag.KW_BREAK || token.getTag() == Tag.KW_CONTINUE || token.getTag() == Tag.KW_IN || token.getTag() == Tag.KW_OUT || token.getTag() == Tag.TK_RBRACE) {
                    //分号缺失
                    back();
                } else if (token.getTag() != Tag.TK_SEMICOLON) {
                    //分号错误
                }
                break;
            case KW_RETURN:
                retstat();
                break;
            case KW_IN:
                nextToken();
                if (token.getTag() != Tag.TK_IN_PUT) {
                    //输入错误
                }
                nextToken();
                if (token.getTag() != Tag.TK_IDENT) {
                    //无效输入
                } else {
                    //获取输入
                }
                nextToken();
                if (token.getTag() != Tag.TK_SEMICOLON) {
                    back();
                }
                break;
            case KW_OUT:
                nextToken();
                if (token.getTag() != Tag.TK_OUT_PUT) {
                    //输出错误
                }
                //输出
                nextToken();
                if (token.getTag() != Tag.TK_SEMICOLON) {
                    //无效输出
                    back();
                }
                break;
            case TK_IDENT:
                idtail();
                nextToken();
                if (token.getTag() != Tag.TK_SEMICOLON) {
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
                back();
            } else {
                //符号错误
            }
        }
        nextToken();
        if (token.getTag() != Tag.TK_CLOSE_PA) {
            if (token.getTag() == Tag.TK_LBRACE || token.getTag() == Tag.TK_SEMICOLON || token.getTag() == Tag.KW_WHILE || token.getTag() == Tag.KW_IF || token.getTag() == Tag.KW_RETURN || token.getTag() == Tag.KW_BREAK || token.getTag() == Tag.KW_CONTINUE || token.getTag() == Tag.KW_IN || token.getTag() == Tag.KW_OUT || token.getTag() == Tag.TK_IDENT || token.getTag() == Tag.KW_VOID || token.getTag() == Tag.KW_INT || token.getTag() == Tag.KW_CHAR || token.getTag() == Tag.KW_STRING) {
                // 右括号缺失
                back();
            } else {
                //无效字符
            }
        }
        block(0, 0, 0);

    }

    private void ifstat() {
        nextToken();
        if (token.getTag() != Tag.TK_OPEN_PA) {
            if (token.getTag() == Tag.TK_IDENT || token.getTag() == Tag.TK_C_NUM || token.getTag() == Tag.TK_C_CHAR || token.getTag() == Tag.TK_C_STR || token.getTag() == Tag.TK_OPEN_PA) {
                //左括号丢失
                back();
            } else {
                //无效字符
            }
        }
        nextToken();
        if (token.getTag() != Tag.TK_CLOSE_PA) {
            if (token.getTag() == Tag.TK_LBRACE || token.getTag() == Tag.TK_SEMICOLON || token.getTag() == Tag.KW_WHILE || token.getTag() == Tag.KW_IF || token.getTag() == Tag.KW_RETURN || token.getTag() == Tag.KW_BREAK || token.getTag() == Tag.KW_CONTINUE || token.getTag() == Tag.KW_IN || token.getTag() == Tag.KW_OUT || token.getTag() == Tag.TK_IDENT || token.getTag() == Tag.KW_VOID || token.getTag() == Tag.KW_INT || token.getTag() == Tag.KW_CHAR || token.getTag() == Tag.KW_STRING) {
                // 右括号缺失
                back();
            } else {
                //无效字符
            }
        }
        block(0, 0, 0);
        nextToken();
        if (token.getTag() != Tag.KW_ELSE) {
            if (token.getTag() == Tag.TK_LBRACE || token.getTag() == Tag.TK_SEMICOLON || token.getTag() == Tag.KW_WHILE || token.getTag() == Tag.KW_IF || token.getTag() == Tag.KW_RETURN || token.getTag() == Tag.KW_BREAK || token.getTag() == Tag.KW_CONTINUE || token.getTag() == Tag.KW_IN || token.getTag() == Tag.KW_OUT || token.getTag() == Tag.KW_VOID || token.getTag() == Tag.KW_INT || token.getTag() == Tag.KW_CHAR || token.getTag() == Tag.KW_STRING) {
                //else丢失
                back();
            } else if (token.getTag() == Tag.TK_IDENT) {
                // else 拼写错误
            } else {
                //非法字符
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
                back();
            }
        } else {

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
            back();
            arglist();
        } else {
            //错误
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
                back();
                arglist();
            } else if (token.getTag() == Tag.TK_SEMICOLON || token.getTag() == Tag.TK_CLOSE_PA) {
                back();
                return;
            } else {
                //错误
            }
        } else if (token.getTag() == Tag.TK_CLOSE_PA || token.getTag() == Tag.TK_SEMICOLON) {
            back();
            return;
        } else if (token.getTag() == Tag.TK_IDENT || token.getTag() == Tag.TK_C_NUM || token.getTag() == Tag.TK_C_CHAR || token.getTag() == Tag.TK_C_STR || token.getTag() == Tag.TK_OPEN_PA) {
            //错误
            back();
            expr();
            arglist();
        } else {
            //错误
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
            back();
            expr();
        } else if (token.getTag() == Tag.TK_SEMICOLON || token.getTag() == Tag.TK_CLOSE_PA || token.getTag() == Tag.TK_COMMA || token.getTag() == Tag.TK_RBRACE || token.getTag() == Tag.KW_RETURN || token.getTag() == Tag.KW_BREAK || token.getTag() == Tag.KW_CONTINUE || token.getTag() == Tag.KW_IN || token.getTag() == Tag.KW_OUT || token.getTag() == Tag.KW_WHILE || token.getTag() == Tag.KW_IF || token.getTag() == Tag.KW_INT || token.getTag() == Tag.KW_VOID || token.getTag() == Tag.KW_CHAR || token.getTag() == Tag.KW_STRING) {
            back();
        } else {
            nextToken();
            if (token.getTag() == Tag.TK_SEMICOLON || token.getTag() == Tag.TK_CLOSE_PA || token.getTag() == Tag.TK_COMMA) {
                //error
                back();
            } else if (token.getTag() == Tag.TK_IDENT || token.getTag() == Tag.TK_C_NUM || token.getTag() == Tag.TK_C_CHAR || token.getTag() == Tag.TK_C_STR || token.getTag() == Tag.TK_OPEN_PA) {
                //error
                back();
                expr();
            } else {
                //error
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
                    back();
                }
                break;
            case TK_C_STR:
                break;
            default:
                if (token.getTag() == Tag.TK_CLOSE_PA || token.getTag() == Tag.TK_SEMICOLON || token.getTag() == Tag.TK_COMMA || token.getTag() == Tag.TK_GT || token.getTag() == Tag.TK_GEQ || token.getTag() == Tag.TK_LT || token.getTag() == Tag.TK_LEQ || token.getTag() == Tag.TK_EQ || token.getTag() == Tag.TK_NEQ || token.getTag() == Tag.TK_PLUS || token.getTag() == Tag.TK_MINUS || token.getTag() == Tag.TK_STAR || token.getTag() == Tag.TK_DIVIDE) {
                    //错误
                    back();
                } else {
                    //错误
                }
        }
        return;
    }
}
