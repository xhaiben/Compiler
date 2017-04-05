package cacher;

import java.util.Scanner;

/*
 * Created by xhaiben on 2017/2/12.
 */
public class Lexer {
    public static final int EOF = 0;//文件结束符
    public static final int SEMI = 1;//分号
    public static final int PLUS = 2;//加号
    public static final int TIMES = 3;//乘号
    public static final int LP = 4;//左括号
    public static final int RP = 5;//右括号
    public static final int NUM_OR_ID = 6;//常量或变量
    public static final int UNKNOWN_SYMBOL = 7;//非法字符

    private int lookAhead = -1;//向后多看一个字符

    public String yytext = "";
    public int yyleng = 0;
    public int yylineno = 0;

    private StringBuffer input_buffer;
    private String current;

    public Lexer() {
        this.input_buffer = new StringBuffer();
    }

    private boolean isAlnum(char c) {
        return Character.isAlphabetic(c) || Character.isDigit(c);
    }

    private int lex() {
        while (true) {
            while (current == null) {
                Scanner scanner = new Scanner(System.in);
                while (true) {
                    String line = scanner.nextLine();
                    if (line.equals("end")) {
                        break;
                    }
                    input_buffer.append(line.trim());
                }
                scanner.close();

                if (input_buffer.length() == 0) {
                    return EOF;
                }
                current = input_buffer.toString().trim();
                yylineno++;
            }
            if (current.isEmpty()) {
                return EOF;
            }
            for (int i = 0; i < current.length(); i++) {
                yyleng = 0;
                yytext = current.substring(0, 1);
                switch (current.charAt(i)) {
                    case ';':
                        current = current.substring(1);
                        return SEMI;
                    case '+':
                        current = current.substring(1);
                        return PLUS;
                    case '*':
                        current = current.substring(1);
                        return TIMES;
                    case '(':
                        current = current.substring(1);
                        return LP;
                    case ')':
                        current = current.substring(1);
                        return RP;

                    case '\n':
                    case '\t':
                    case ' ':
                        current = current.substring(1);
                        break;
                    default:
                        if (!isAlnum(current.charAt(i))) {
                            System.out.println("Ignoring illegal input: " + current.charAt(i));
                        } else {
                            while (isAlnum(current.charAt(i))) {
                                i++;
                                yyleng++;
                            }
                            yytext = current.substring(0, yyleng);
                            current = current.substring(yyleng);
                            return NUM_OR_ID;
                        }
                        break;
                }
            }
        }
    }

    public boolean match(int token) {
        if (lookAhead == -1) {
            lookAhead = lex();
        }
        return token == lookAhead;
    }

    public void advance() {
        lookAhead = lex();
    }

    public void runLexer() {
        while (!match(EOF)) {
            System.out.println("Token: " + token() + ",Symbol: " + yytext);
            advance();
        }
    }

    private String token() {
        String token = null;
        switch (lookAhead) {
            case EOF:
                token = "EOF";
                break;
            case PLUS:
                token = "PLUS";
                break;
            case TIMES:
                token = "TIMES";
                break;
            case NUM_OR_ID:
                token = "NUM_OR_ID";
                break;
            case SEMI:
                token = "SEMI";
                break;
            case LP:
                token = "LP";
                break;
            case RP:
                token = "RP";
                break;
        }
        return token;
    }
}
