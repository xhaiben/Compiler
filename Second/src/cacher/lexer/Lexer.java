package cacher.lexer;

import java.util.Scanner;

/*
 * Created by xhaiben on 2017/3/11.
 */
public class Lexer {

    private StringBuffer input_buffer;
    private String current;
    private int ch_point = 0;

    Lexer() {
        this.input_buffer = new StringBuffer();
    }

    private Token lex() {
        while (true) {
            while (current == null) {
                Scanner scanner = new Scanner(System.in);
                while (true) {
                    String line = scanner.nextLine();
                    if (line.equals("end")) {
                        break;
                    }
                    input_buffer.append(line);
                }
                scanner.close();
                if (input_buffer.length() == 0) {
                    return new Token(Tag.TK_EOF);
                }
                current = input_buffer.toString().trim();
                if (current.isEmpty()) {
                    return new Token(Tag.TK_EOF);
                }
                Token token = null;
                char ch = current.charAt(ch_point);
                if (Character.isAlphabetic(ch) || ch == '_') { //标识符或关键字
                    StringBuilder name = new StringBuilder();
                    do {
                        name.append(ch);
                        ch = current.charAt(++ch_point);
                    } while (Character.isAlphabetic(ch) || Character.isDigit(ch) || ch == '_');
                    Tag tag = Keywords.getTag(name.toString());
                    if (tag == Tag.TK_IDENT) {
                        // 标识符
                        token = new Id(name.toString());
                    } else {
                        // 关键字
                        token = new Token(tag);
                    }
                } else if (Character.isDigit(ch)) { // 数字
                    int val = 0;
                    if (ch != '0') {//十进制
                        do {
                            val += val * 10 + ch - '0';
                            ch = current.charAt(++ch_point);
                        } while (Character.isDigit(ch));
                    } else {
                        ch = current.charAt(++ch_point);
                        if (ch == 'x') {//十六进制
                            ch = current.charAt(++ch_point);
                            if (Character.isDigit(ch) || ch >= 'A' && ch <= 'F' || ch >= 'a' && ch <= 'f') {
                                do {
                                    val = val * 16 + ch;
                                    if (Character.isDigit(ch)) {
                                        val -= '0';
                                    } else if (ch >= 'A' && ch <= 'F') {
                                        val -= 'A';
                                    } else if (ch >= 'a' && ch <= 'f') {
                                        val -= 'a';
                                    }
                                    ch = current.charAt(++ch_point);
                                } while (Character.isDigit(ch) || ch >= 'A' && ch <= 'F' || ch >= 'a' && ch <= 'f');
                            } else {
                                //0x后无数据
                                token = new Token(Tag.ERR);
                            }
                        } else if (ch == 'b') { // 二进制
                            ch = current.charAt(++ch_point);
                            if (ch >= '0' && ch <= '1') {
                                do {
                                    val = val * 2 + ch - '0';
                                    ch = current.charAt(++ch_point);
                                } while (ch >= '0' && ch <= '1');
                            } else {
                                // 0b后无数据
                                token = new Token(Tag.ERR);
                            }
                        } else if (ch >= '0' && ch <= '7') {
                            do {
                                val = val * 8 + ch - '0';
                                ch = current.charAt(++ch_point);
                            } while (ch >= '0' && ch <= '7');
                        }
                    }
                    token = new Num(val);
                } else if (ch == '\'') { //字符常量
                    char c = 0;
                    ch = current.charAt(++ch_point);
                    if (ch == '\\') { //如果是转义符
                        ch = current.charAt(++ch_point);
                        if (ch == 'n') c = '\n';
                        else if (ch == '\\') c = '\\';
                        else if (ch == 't') c = '\t';
                        else if (ch == '0') c = '\0';
                        else if (ch == '\'') c = '\'';
                        else if (ch == '\n') {
                            token = new Token(Tag.ERR);
                        } else {
                            c = ch;
                        }
                    } else if (ch == '\n') {
                        token = new Token(Tag.ERR);
                    } else if (ch == '\'') { //空数据
                        token = new Token(Tag.ERR);
                        ch_point++;
                    } else {
                        c = ch;
                    }
                    if (token == null) {
                        ch = current.charAt(++ch_point);
                        if (ch == '\'' && c != 0) {
                            token = new Char(c);
                        } else {
                            token = new Token(Tag.ERR);
                        }
                    }
                } else if (ch == '"') { //字符串常量

                }
            }
        }
    }
}
