package cacher;

/**
 * Created by xhaiben on 2017/2/18.
 * <p>
 * 循环调用版本
 */
public class ImprovedParser {
    private Lexer lexer;
    private boolean isLegalStatement = true;

    public ImprovedParser(Lexer lexer) {
        this.lexer = lexer;
    }

    public void statements() {
        /**
         * 语法规则的递归地定义
         * a->bc|bd
         * a->b(c|d)
         * a->bA'
         * A'->c|d
         * statements -> expression ; | expression ; statements
         */
        while (!lexer.match(Lexer.EOF)) {
            expression();
            if (lexer.match(Lexer.SEMI)) {
                lexer.advance();
            } else {
                isLegalStatement = false;
                System.out.println("line: " + lexer.yylineno + " Missing semicolon");
            }
            if (isLegalStatement) {
                System.out.println("The statement is legal");
            }
        }
    }

    private void expression() {
        /**
         * expression -> term expression'
         */
        term();
//        expr_prime(); //expression'
        while (lexer.match(Lexer.PLUS)) {
            lexer.advance();
            term();
        }
        if (lexer.match(Lexer.UNKNOWN_SYMBOL)) {
            isLegalStatement = false;
            System.out.println("unknown symbol" + lexer.yytext);
            return;
        }else{
            /**
             * "空" 不再解析 直接返回
             */
            return;
        }
    }

    private void term() {
        /**
         * term -> factor term'
         */
        factor();
//        term_prime();
        while (lexer.match(Lexer.TIMES)){
            lexer.advance();
            factor();
        }
    }

//    private void expr_prime() {
//        /**
//         * expression' -> PLUS term expression' | '空'
//         */
//        if (lexer.match(Lexer.PLUS)) {
//            lexer.advance();
//            term();
//            expr_prime();
//        } else if (lexer.match(Lexer.UNKNOWN_SYMBOL)) {
//            isLegalStatement = false;
//            System.out.println("unknown symbol" + lexer.yytext);
//            return;
//        } else {
//            /**
//             * "空" 不再解析 直接返回
//             */
//            return;
//        }
//    }


//    private void term_prime() {
//        /*
//         * term' -> * factor term' | '空'
//         */
//        if (lexer.match(Lexer.TIMES)) {
//            lexer.advance();
//            factor();
//            term_prime();
//        } else {
//            /**
//             * 如果不是以 * 开头，那么执行 '空'
//             * 也就是不进一步解析，直接返回空
//             */
//            return;
//        }
//    }

    private void factor() {
        /**
         *  factor -> NUM_OR_ID | LP expression RP
         */
        if (lexer.match(Lexer.NUM_OR_ID)) {
            lexer.advance();
        } else if (lexer.match(Lexer.LP)) {
            lexer.advance();
            expression();
            if (lexer.match(Lexer.RP)) {
                lexer.advance();
            } else {
                /*
                 * 有左括 但没有右括号 错误
                 */
                isLegalStatement = false;
                System.out.println("line" + lexer.yylineno + " Missing ) ");
            }
        } else {
            /*
             * 非数字，解析错误
             *
             */
            isLegalStatement = false;
            System.out.println("illegal statements");
        }
    }

}
