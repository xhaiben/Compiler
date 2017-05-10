package cacher.lexer;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Scanner;

import cacher.error.Error;

/*
 * Created by xhaiben on 2017/3/11.
 */

public class Lexer {

    private static int line_num = 0;//行号
    private int line_col = 0;//行内列号
    private char cur_char = ' ';//当前字符
    private char old_char = ' ';//前一个字符
    private String cur_line = "";//当前行
    private Scanner scanner;

    public static String id; //当前标识符
    public static int num; //当前int
    public static char aChar;//当前char
    public static String string;//当前String

    public static int getLine_num() {
        return line_num;
    }

    public int getLine_col() {
        return line_col;
    }

    public Lexer(File file) {
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
            this.scanner = new Scanner(inputStreamReader);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getNextLine() {
        if (scanner.hasNextLine()) {
            cur_line = scanner.nextLine() + "\n";
        } else {
            cur_line = "";
        }
    }

    private void getChar() {
        if (line_col >= cur_line.length()) {
            line_num++;
            line_col = 0;
            getNextLine();
        }
        try {
            old_char = cur_char;
            cur_char = cur_line.charAt(line_col);
            line_col++;
        } catch (IndexOutOfBoundsException e) {
            cur_char = 0;
        }
    }

    private boolean getChar(char need) {
        getChar();
        if (cur_char != need) {
            return false;
        }
        getChar();
        return true;
    }

    public Token lex() {
        while (cur_char != 0) {
            Token token = null;
            //忽略空白符
            while (cur_char == ' ' || cur_char == '\n' || cur_char == '\t') {
                getChar();
            }

            if (Character.isAlphabetic(cur_char) || cur_char == '_') { //标识符或关键字
                StringBuilder name = new StringBuilder();
                do {
                    name.append(cur_char);
                    getChar();
                }
                while (Character.isAlphabetic(cur_char) || Character.isDigit(cur_char) || cur_char == '_');
                Tag tag = Keywords.getTag(name.toString());
                if (tag == Tag.TK_IDENT) {
                    // 标识符
                    id = name.toString();
                    token = new Id(name.toString());
                } else {
                    // 关键字
                    token = new Token(tag);
                }
            } else if (Character.isDigit(cur_char)) { // 数字
                int val = 0;
                if (cur_char != '0') {//十进制
                    do {
                        val = (val * 10 + (cur_char - 48));
                        getChar();
                    } while (Character.isDigit(cur_char));
                } else {
                    getChar();
                    if (cur_char == 'x') {//十六进制
                        getChar();
                        if (Character.isDigit(cur_char) || cur_char >= 'A' && cur_char <= 'F' || cur_char >= 'a' && cur_char <= 'f') {
                            do {
                                val = val * 16 + cur_char;
                                if (Character.isDigit(cur_char)) {
                                    val -= '0';
                                } else if (cur_char >= 'A' && cur_char <= 'F') {
                                    val -= 'A';
                                } else if (cur_char >= 'a' && cur_char <= 'f') {
                                    val -= 'a';
                                }
                                getChar();
                            }
                            while (Character.isDigit(cur_char) || cur_char >= 'A' && cur_char <= 'F' || cur_char >= 'a' && cur_char <= 'f');
                        } else {
                            //0x后无数据
                            Error.LexError.lexError(Error.LexError.NUM_HEX_TYPE, line_num, line_col);
                            token = new Token(Tag.ERR);
                        }
                    } else if (cur_char == 'b') { // 二进制
                        getChar();
                        if (cur_char >= '0' && cur_char <= '1') {
                            do {
                                val = val * 2 + cur_char - '0';
                                getChar();
                            } while (cur_char >= '0' && cur_char <= '1');
                        } else {
                            // 0b后无数据
                            Error.LexError.lexError(Error.LexError.NUM_BIN_TYPE, line_num, line_col);
                            token = new Token(Tag.ERR);
                        }
                    } else if (cur_char >= '0' && cur_char <= '7') {
                        do {
                            val = val * 8 + cur_char - '0';
                            getChar();
                        } while (cur_char >= '0' && cur_char <= '7');
                    }
                }
                if (token == null) {
                    num = val;
                    token = new Num(val);
                }
            } else if (cur_char == '\'') { //字符常量
                char c = 0;
                getChar();
                if (cur_char == '\\') { //如果是转义符
                    getChar();
                    if (cur_char == 'n') c = '\n';
                    else if (cur_char == '\\') c = '\\';
                    else if (cur_char == 't') c = '\t';
                    else if (cur_char == '0') c = '\0';
                    else if (cur_char == '\'') c = '\'';
                    else if (cur_char == '\n' || cur_char == 0) {
                        Error.LexError.lexError(Error.LexError.CHAR_NO_R_QUTION, line_num, line_col);
                        token = new Token(Tag.ERR);
                    } else {
                        c = cur_char;
                    }
                } else if (cur_char == '\n' || cur_char == 0) {
                    token = new Token(Tag.ERR);
                    Error.LexError.lexError(Error.LexError.CHAR_NO_R_QUTION, line_num, line_col);
                } else if (cur_char == '\'') { //空数据
                    token = new Token(Tag.ERR);
                    Error.LexError.lexError(Error.LexError.CHAR_NO_DATA, line_num, line_col);
                    getChar();
                } else {
                    c = cur_char;
                }
                if (token == null) {
                    if (getChar('\'')) {
                        aChar = c;
                        token = new Char(c);
                    } else {
                        token = new Token(Tag.ERR);
                        Error.LexError.lexError(Error.LexError.CHAR_NO_R_QUTION, line_num, line_col);
                    }
                }
            } else if (cur_char == '"') { //字符串常量
                StringBuilder stringBuilder = new StringBuilder();
                while (!getChar('"')) {
                    if (cur_char == '\\') {
                        getChar();
                        if (cur_char == 'n') stringBuilder.append('\n');
                        else if (cur_char == '\\') stringBuilder.append('\\');
                        else if (cur_char == 't') stringBuilder.append('\t');
                        else if (cur_char == '"') stringBuilder.append('"');
                        else if (cur_char == '0') stringBuilder.append("\0");
                        else if (cur_char == '\n') ;
                        else if (cur_char == 0) {
                            Error.LexError.lexError(Error.LexError.STR_NO_R_QUTION, line_num, line_col);
                            token = new Token(Tag.ERR);
                            break;
                        } else {
                            stringBuilder.append(cur_char);
                        }
                    } else if (cur_char == '\n' || cur_char == 0) {
                        Error.LexError.lexError(Error.LexError.STR_NO_R_QUTION, line_num, line_col);
                        token = new Token(Tag.ERR);
                        break;
                    } else {
                        stringBuilder.append(cur_char);
                    }
                }
                if (token == null) {
                    string = stringBuilder.toString();
                    token = new Str(stringBuilder.toString());
                }
            } else {
                switch (cur_char) {
                    case '+':
                        token = new Token(getChar('+') ? Tag.TK_INC : Tag.TK_PLUS);
                        break;
                    case '-':
                        token = new Token(getChar('-') ? Tag.TK_DEC : Tag.TK_MINUS);
                        break;
                    case '*':
                        token = new Token(Tag.TK_STAR);
                        getChar();
                        break;
                    case '/':
                        getChar();
                        if (cur_char == '/') {
                            while (cur_char != '\n' && cur_char != 0) {
                                getChar();
                            }
                            token = new Token(Tag.ERR);
                        } else if (cur_char == '*') {
                            while (!getChar('\0')) {
                                if (cur_char == '*') {
                                    if (getChar('/')) {
                                        break;
                                    }
                                }
                            }
                            if (cur_char == 0) {
                                Error.LexError.lexError(Error.LexError.COMMENT_NO_END, line_num, line_col);
                                token = new Token(Tag.ERR);
                            }
                        } else {
                            token = new Token(Tag.TK_DIVIDE);
                        }
                        break;
                    case '%':
                        token = new Token(Tag.TK_MOD);
                        getChar();
                        break;
                    case '>':
                        getChar();
                        switch (cur_char) {
                            case '=':
                                token = new Token(Tag.TK_GEQ);
                                getChar();
                                break;
                            case '>':
                                token = new Token(Tag.TK_IN_PUT);
                                getChar();
                                break;
                            default:
                                token = new Token(Tag.TK_GT);
                                break;
                        }
                        break;
//                        if (getChar('=')) {
//                            token = new Token(Tag.TK_GEQ);
//                        } else if (getChar('>')) {
//                            token = new Token(Tag.TK_IN_PUT);
//                        } else {
//                            token = new Token(Tag.TK_GT);
//                        }
//                        break;
                    case '<':
                        getChar();
                        switch (cur_char) {
                            case '=':
                                token = new Token(Tag.TK_LEQ);
                                getChar();
                                break;
                            case '<':
                                token = new Token(Tag.TK_OUT_PUT);
                                getChar();
                                break;
                            default:
                                token = new Token(Tag.TK_LT);
                                break;
                        }
//                        if (getChar('=')) {
//                            token = new Token(Tag.TK_LEQ);
//                        } else if (getChar('<')) {
//                            token = new Token(Tag.TK_OUT_PUT);
//                        } else {
//                            token = new Token(Tag.TK_LT);
//                        }
                        break;
                    case '=':
                        token = new Token(getChar('=') ? Tag.TK_EQ : Tag.TK_ASSIGN);
                        break;
//                    case '&':
//                        token = new Token(getChar('&') ? Tag.TK_AND : Tag.TK_LEA);
//                        break;
//                    case '|':
//                        token = new Token(inputSystem.nextChar('|') ? Tag.TK_OR : Tag.ERR);
//                        if (token.getTag() == Tag.ERR) {
//                            lexError(LexError.OR_NO_PAIR);
//                            return;
//                        }
//                        break;
                    case '!':
                        token = new Token(getChar('=') ? Tag.TK_NEQ : Tag.TK_NOT);
                        break;
                    case ':':
                        token = new Token(Tag.TK_COLON);
                        getChar();
                        break;
                    case ',':
                        token = new Token(Tag.TK_COMMA);
                        getChar();
                        break;
                    case ';':
                        token = new Token(Tag.TK_SEMICOLON);
                        getChar();
                        break;
                    case '(':
                        token = new Token(Tag.TK_OPEN_PA);
                        getChar();
                        break;
                    case ')':
                        token = new Token(Tag.TK_CLOSE_PA);
                        getChar();
                        break;
                    case '[':
                        token = new Token(Tag.TK_OPEN_BR);
                        getChar();
                        break;
                    case ']':
                        token = new Token(Tag.TK_CLOSE_BR);
                        getChar();
                        break;
                    case '{':
                        token = new Token(Tag.TK_LBRACE);
                        getChar();
                        break;
                    case '}':
                        token = new Token(Tag.TK_RBRACE);
                        getChar();
                        break;
//                    case '.':
//                        token = new Token(Tag.TK_DOT);
//                        break;
                    case 0:
                        getChar();
                        break;
                    default:
                        token = new Token(Tag.ERR);
                        Error.LexError.lexError(Error.LexError.TOKEN_NO_EXIST, line_num, line_col);
                        getChar();
                        break;
                }
            }
            if (token != null && token.getTag() != Tag.ERR) {
                return token;
            }
        }
        return new Token(Tag.TK_EOF);
    }

}


