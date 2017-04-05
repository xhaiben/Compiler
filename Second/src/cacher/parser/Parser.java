package cacher.parser;

import cacher.lexer.Tag;
import cacher.lexer.Token;

import java.util.List;

/*
 * Created by xhaiben on 2017/4/3.
 */
public class Parser {
    private List<Token> tokenList;
    private boolean compileOK = false;
    private int errorNum = 0;
    private StringBuilder errorMsg;

    private int wait = 0;

    private int p_token = 0;

    private Token old_token, token;

    public Parser(List<Token> tokenList) {
        this.tokenList = tokenList;
        this.errorMsg = new StringBuilder();
    }

    private Token getToken() {
        if (p_token < tokenList.size()) {
            return tokenList.get(p_token++);
        } else {
            return null;
        }
    }

    private int nextToken() {
        if (wait == 1) {
            wait = 0;
            return 0;
        }
        Token c_token;
        while (true) {
            c_token = getToken();
            if (c_token.getTag() == Tag.TK_EOF) {
                if (!compileOK) {
                    System.err.println("语法分析失败");
                }
                System.exit(0);
                return -2;
            }
            if (c_token == null) {
                old_token = token;
                token = null;
                return -1;
            }
            if (c_token.getTag() == Tag.ERR) {
                //跳过中间的错误||无效字符
            } else {
                old_token = token;
                token = c_token;
                return 0;
            }
        }

    }

    public void parse() {
        program();
    }

    private void program() {
        if (nextToken() < 0) {
            System.out.println("语法分析完毕");
        } else {
            nextToken();
            segment();
            program();
            compileOK = true;
        }

    }

    private void segment() {
        if (token.getTag() == Tag.KW_EXTERN) {

        } else {
            type();
            nextToken();
            def();
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
            case TK_IDENT:
                break;
            default:
                System.err.println("type");
        }
        return null;
    }

    private void def() {
        if (token.getTag() == Tag.TK_IDENT) {
            nextToken();
            idtail();
        } else if (token.getTag() == Tag.TK_STAR) {
            nextToken();
            if (token.getTag() == Tag.TK_IDENT) {
                nextToken();
                init();
                deflist();
            } else {
                //出错
                System.err.println("def");
            }
        } else {
            //出错
            System.err.println("def");
        }
    }

    private void idtail() {
        if (token.getTag() == Tag.TK_OPEN_PA) {
            nextToken();
            para();
            if (token.getTag() == Tag.TK_CLOSE_PA) {
                nextToken();
                funtail();
            } else {
                //出错
                System.err.println("idtail");
            }
        } else {
            varrdef();
            deflist();
        }
    }

    private void varrdef() {
        if (token.getTag() == Tag.TK_OPEN_BR) {
            nextToken();
            if (token.getTag() == Tag.TK_C_NUM) {
                nextToken();
                if (token.getTag() == Tag.TK_CLOSE_BR) {

                } else {
                    errorNum++;
                }
            } else {
                errorNum++;
            }
        } else {
            init();
        }
    }

    private void init() {
        if (token.getTag() == Tag.TK_ASSIGN) {
            nextToken();
            expr();
        }
    }

    private void deflist() {
        if (token.getTag() == Tag.TK_COMMA) {
            nextToken();
            defdata();
            deflist();
        } else if (token.getTag() == Tag.TK_SEMICOLON) {
            return;
        } else {
            //出错
            errorNum++;
            System.err.println("deflist");
        }
    }

    private void defdata() {
        if (token.getTag() == Tag.TK_IDENT) {
            nextToken();
            varrdef();
        } else if (token.getTag() == Tag.TK_STAR) {
            nextToken();
            if (token.getTag() == Tag.TK_IDENT) {
                nextToken();
                init();
            }
        } else {
            errorNum++;
            System.err.println("defdata");
        }
    }

    private void para() {
        if (token.getTag() == Tag.KW_VOID || token.getTag() == Tag.KW_INT || token.getTag() == Tag.KW_CHAR) {
            nextToken();
            paradata();
            paralist();
        } else {
            errorNum++;
        }
    }

    private void funtail() {
        if (token.getTag() == Tag.TK_SEMICOLON) {
            return;
        }
        block();
    }

    private void paradata() {
        if (token.getTag() == Tag.TK_STAR) {
            nextToken();
            if (token.getTag() == Tag.TK_IDENT) {
                return;
            } else {
                errorNum++;
            }
        } else if (token.getTag() == Tag.TK_IDENT) {
            nextToken();
            paradatatail();
        } else {
            errorNum++;
        }
    }

    private void paralist() {
        if (token.getTag() == Tag.TK_COMMA) {
            nextToken();
            type();
            nextToken();
            paradata();
            paralist();
        }
    }

    private void paradatatail() {
        if (token.getTag() == Tag.TK_OPEN_BR) {
            nextToken();
            if (token.getTag() == Tag.TK_C_NUM) {
                nextToken();
                if (token.getTag() == Tag.TK_CLOSE_BR) {
                    nextToken();
                    return;
                } else {
                    //出错
                }
            } else {
                //出错
            }
        }
    }

    private void block() {
        if (token.getTag() == Tag.TK_LBRACE) {
            nextToken();
            subprogram();
            if (token.getTag() == Tag.TK_RBRACE) {
                nextToken();
            } else {
                //出错
                errorNum++;
            }
        } else {
            //出错
            errorNum++;
        }
    }

    private void subprogram() {
        if (token.getTag() == Tag.KW_VOID || token.getTag() == Tag.KW_INT || token.getTag() == Tag.KW_CHAR) {
            nextToken();
            localdef();
            subprogram();
        } else if (token.getTag() == Tag.KW_WHILE || token.getTag() == Tag.KW_FOR || token.getTag() == Tag.KW_DO || token.getTag() == Tag.KW_IF || token.getTag() == Tag.KW_SWITCH || token.getTag() == Tag.KW_BREAK || token.getTag() == Tag.KW_CONTINUE || token.getTag() == Tag.KW_RETURN) {
            nextToken();
            statement();
            subprogram();
        }


    }

    private void localdef() {
        defdata();
        deflist();
    }

    private void expr() {
        assexpr();
    }

    private void assexpr() {
        orexpr();
        nextToken();
        asstail();
    }

    private void asstail() {
        if (token.getTag() == Tag.TK_ASSIGN) {
            nextToken();
            orexpr();
            nextToken();
            asstail();
        }
    }

    private void orexpr() {
        andexpr();
        nextToken();
        ortail();
    }

    private void ortail() {
        if (token.getTag() == Tag.TK_OR) {
            nextToken();
            andexpr();
            nextToken();
            ortail();
        }
    }

    private void andexpr() {
        cmpexpr();
        nextToken();
        andtail();
    }

    private void andtail() {
        if (token.getTag() == Tag.TK_AND) {
            nextToken();
            cmpexpr();
            nextToken();
            andtail();
        }
    }

    private void cmpexpr() {
        aloexpr();
        nextToken();
        cmptail();
    }

    private void cmptail() {
        if (token.getTag() == Tag.TK_GT || token.getTag() == Tag.TK_GEQ || token.getTag() == Tag.TK_LT || token.getTag() == Tag.TK_LEQ || token.getTag() == Tag.TK_EQ || token.getTag() == Tag.TK_NEQ) {
            nextToken();
            aloexpr();
            nextToken();
            cmpexpr();
        }
    }

    private void aloexpr() {
        item();
        nextToken();
        alotail();
    }

    private void alotail() {
        if (token.getTag() == Tag.TK_PLUS || token.getTag() == Tag.TK_MINUS) {
            nextToken();
            item();
            nextToken();
            alotail();
        }
    }

    private void item() {
        factor();
        nextToken();
        itemtail();
    }

    private void itemtail() {
        if (token.getTag() == Tag.TK_STAR || token.getTag() == Tag.TK_DIVIDE || token.getTag() == Tag.TK_MOD) {
            nextToken();
            factor();
            nextToken();
            itemtail();
        }
    }

    private void factor() {
        if (token.getTag() == Tag.TK_NOT || token.getTag() == Tag.TK_MINUS || token.getTag() == Tag.TK_LEA || token.getTag() == Tag.TK_STAR || token.getTag() == Tag.TK_INC || token.getTag() == Tag.TK_DEC) {
            nextToken();
            factor();
        } else if (token.getTag() == Tag.TK_IDENT || token.getTag() == Tag.TK_OPEN_PA || token.getTag() == Tag.TK_C_NUM || token.getTag() == Tag.KW_CHAR || token.getTag() == Tag.TK_C_STR) {
            val();
        } else {
            //出错
        }
    }

    private void val() {
        elem();
        nextToken();
        rop();
    }

    private void elem() {
        if (token.getTag() == Tag.TK_IDENT) {
            nextToken();
            idexpr();
        } else if (token.getTag() == Tag.TK_OPEN_PA) {
            nextToken();
            expr();
            if (token.getTag() != Tag.TK_CLOSE_PA) {
                //出错
            }
        } else if (token.getTag() != Tag.TK_C_NUM && token.getTag() != Tag.TK_C_CHAR && token.getTag() != Tag.TK_C_STR) {

        } else {
            //出错
        }
    }

    private void rop() {
        if (token.getTag() != Tag.TK_INC && token.getTag() != Tag.TK_DEC) {
            //出错
        }
    }

    private void idexpr() {
        if (token.getTag() == Tag.TK_OPEN_BR) {
            nextToken();
            expr();
            if (token.getTag() != Tag.TK_CLOSE_BR) {
                //出错
            }
        } else if (token.getTag() == Tag.TK_OPEN_PA) {
            nextToken();
            realarg();
            if (token.getTag() != Tag.TK_CLOSE_PA) {
                //出错
            }
        }
    }

    private void realarg() {
        if (token.getTag() == Tag.TK_NOT || token.getTag() == Tag.TK_MINUS || token.getTag() == Tag.TK_LEA || token.getTag() == Tag.TK_STAR || token.getTag() == Tag.TK_INC || token.getTag() == Tag.TK_DEC || token.getTag() == Tag.TK_IDENT || token.getTag() == Tag.TK_OPEN_PA || token.getTag() == Tag.TK_C_NUM || token.getTag() == Tag.KW_CHAR || token.getTag() == Tag.TK_C_STR) {
            nextToken();
            arg();
            nextToken();
            arglist();
        }
    }

    private void arg() {
        expr();
    }

    private void arglist() {
        if (token.getTag() == Tag.TK_COMMA) {
            nextToken();
            arg();
            nextToken();
        }
    }

    private void altexpr() {
        if (token.getTag() == Tag.TK_NOT || token.getTag() == Tag.TK_MINUS || token.getTag() == Tag.TK_LEA || token.getTag() == Tag.TK_STAR || token.getTag() == Tag.TK_INC || token.getTag() == Tag.TK_DEC || token.getTag() == Tag.TK_IDENT || token.getTag() == Tag.TK_OPEN_PA || token.getTag() == Tag.TK_C_NUM || token.getTag() == Tag.KW_CHAR || token.getTag() == Tag.TK_C_STR) {
            expr();
        }
    }

    private void statement() {
        switch (token.getTag()) {
            case KW_WHILE:
                nextToken();
                whilestat();
                break;
            case KW_FOR:
                nextToken();
                forstat();
                break;
            case KW_DO:
                nextToken();
                dowhilestat();
                break;
            case KW_IF:
                nextToken();
                ifstat();
                break;
            case KW_SWITCH:
                nextToken();
                switchstat();
                break;
            case KW_BREAK:
                nextToken();
                if (token.getTag() != Tag.TK_SEMICOLON) {
                    //出错
                }
                break;
            case KW_CONTINUE:
                nextToken();
                if (token.getTag() != Tag.TK_SEMICOLON) {
                    //出错
                }
                break;
            case KW_RETURN:
                nextToken();
                altexpr();
                if (token.getTag() != Tag.TK_SEMICOLON) {
                    //出错
                }
                break;
            default:
                altexpr();
                if (token.getTag() != Tag.TK_SEMICOLON) {
                    //出错
                }
        }
    }

    private void whilestat() {
        if (token.getTag() != Tag.TK_CLOSE_PA) {
            //出错
        }
        nextToken();
        altexpr();
        if (token.getTag() != Tag.TK_CLOSE_PA) {
            //出错
        }
        nextToken();
        block();
    }

    private void dowhilestat() {
        block();
        if (token.getTag() != Tag.KW_WHILE) {
            //出错
        }
        nextToken();
        if (token.getTag() != Tag.TK_OPEN_PA) {
            //出错
        }
        nextToken();
        altexpr();
        if (token.getTag() != Tag.TK_CLOSE_PA) {
            //出错
        }
        nextToken();
        if (token.getTag() != Tag.TK_SEMICOLON) {
            //出错
        }
    }

    private void forstat() {
        if (token.getTag() != Tag.TK_OPEN_PA) {
            //出错
        }
        nextToken();
        forinit();
        altexpr();
        if (token.getTag() != Tag.TK_SEMICOLON) {
            //出错
        }
        nextToken();
        altexpr();
        if (token.getTag() != Tag.TK_CLOSE_PA) {
            //出错
        }
        nextToken();
        block();
    }

    private void forinit() {
        if (token.getTag() == Tag.KW_VOID || token.getTag() == Tag.KW_INT || token.getTag() == Tag.KW_CHAR) {
            localdef();
        } else {
            altexpr();
            if (token.getTag() != Tag.TK_SEMICOLON) {
                //出错
            }
        }
    }

    private void ifstat() {
        if (token.getTag() != Tag.TK_OPEN_PA) {
            //出错
        }
        nextToken();
        expr();
        if (token.getTag() != Tag.TK_CLOSE_PA) {
            //出错
        }
        nextToken();
        block();
        elsestat();
    }

    private void elsestat() {
        if (token.getTag() == Tag.KW_ELSE) {
            nextToken();
            block();
        }
    }

    private void switchstat() {
        if (token.getTag() != Tag.TK_OPEN_PA) {
            //出错
        }
        nextToken();
        expr();
        if (token.getTag() != Tag.TK_CLOSE_PA) {
            //出错
        }
        nextToken();
        if (token.getTag() != Tag.TK_LBRACE) {
            //出错
        }
        nextToken();
        casestat();
        if (token.getTag() != Tag.TK_RBRACE) {
            //出错
        }
    }

    private void casestat() {
        if (token.getTag() == Tag.KW_CASE) {
            nextToken();
            caselabel();
            if (token.getTag() != Tag.TK_COLON) {
                //出错
            }
            nextToken();
            subprogram();
            casestat();
        } else if (token.getTag() == Tag.KW_DEFAULT) {
            nextToken();
            if (token.getTag() != Tag.TK_COLON) {
                //出错
            }
            nextToken();
            subprogram();
        }
    }

    private void caselabel() {
        if (token.getTag() != Tag.TK_C_NUM && token.getTag() != Tag.TK_C_CHAR && token.getTag() != Tag.TK_C_STR) {
            //出错
        }
    }
}
