package cacher.lexer;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/*
 * Created by xhaiben on 2017/3/11.
 */

public class Lexer {

    private InputSystem inputSystem;

    private List<Token> tokenList;

    private File source_file;
    private Scanner source_scanner;
    private char old_char;
    private char cur_char;
    private int line_num = 0;//行号
    private int char_at_line = 0;//字符在列的位置
    private int line_len = 0;//当前行的长度

    private String cur_line;

    public Lexer(InputSystem inputSystem) {
        this.inputSystem = inputSystem;
        tokenList = new ArrayList<>();
    }

    public Lexer(File file) {
        try {
            this.source_file = file;
            InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(this.source_file), "UTF-8");
            this.source_scanner = new Scanner(inputStreamReader);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Token> getTokenList() {
        return tokenList;
    }

    private int next_line() {
        if (source_scanner.hasNextLine()) {
            cur_line = source_scanner.nextLine() + "\n";
            line_len = cur_line.length();
            return 0;
        } else {
            return -1;
        }
    }

    private int getChar() {
        if (char_at_line >= line_len) {
            char_at_line = 0;
            line_len = 0;
            line_num++;
            cur_char = '\0';
            if (next_line() == -1) {  //文件结束
                cur_line = "";
            }
        }
        old_char = cur_char;
        try {
            cur_char = cur_line.charAt(char_at_line);
            char_at_line++;
            return 0;
        } catch (IndexOutOfBoundsException e) {
            return -1;
        }
    }


    public void lex() {
        while (inputSystem.hasNext()) {
            Token token = null;
            char ch = inputSystem.nextChar();
            //忽略空白符
            while (ch == ' ' || ch == '\n' || ch == '\t') {
                ch = inputSystem.nextChar();
            }

            if (Character.isAlphabetic(ch) || ch == '_') { //标识符或关键字
                StringBuilder name = new StringBuilder();
                do {
                    name.append(ch);
                    ch = inputSystem.nextChar();
                }
                while ((Character.isAlphabetic(ch) || Character.isDigit(ch) || ch == '_') && inputSystem.hasNext());
                Tag tag = Keywords.getTag(name.toString());
                if (tag == Tag.TK_IDENT) {
                    // 标识符
                    token = new Id(name.toString());
                } else {
                    // 关键字
                    token = new Token(tag);
                }
                tokenList.add(token);
            } else if (Character.isDigit(ch)) { // 数字
                int val = 0;
                if (ch != '0') {//十进制
                    if (inputSystem.hasNext()) {
                        do {
                            val += val * 10 + ch - '0';
                            ch = inputSystem.nextChar();
                        } while (Character.isDigit(ch) && inputSystem.hasNext());
                    }
                } else if (inputSystem.hasNext()) {
                    ch = inputSystem.nextChar();
                    if (ch == 'x') {//十六进制
                        if (inputSystem.hasNext()) {
                            ch = inputSystem.nextChar();
                            if ((Character.isDigit(ch) || ch >= 'A' && ch <= 'F' || ch >= 'a' && ch <= 'f') && inputSystem.hasNext()) {
                                do {
                                    val = val * 16 + ch;
                                    if (Character.isDigit(ch)) {
                                        val -= '0';
                                    } else if (ch >= 'A' && ch <= 'F') {
                                        val -= 'A';
                                    } else if (ch >= 'a' && ch <= 'f') {
                                        val -= 'a';
                                    }
                                    ch = inputSystem.nextChar();
                                }
                                while ((Character.isDigit(ch) || ch >= 'A' && ch <= 'F' || ch >= 'a' && ch <= 'f') && inputSystem.hasNext());
                            } else {
                                //0x后无数据
                                lexError(LexError.NUM_HEX_TYPE);
                                return;
//                            token = new Token(Tag.ERR);
//                            tokenList.add(token);
                            }
                        } else {
                            //0x后无数据
                            lexError(LexError.NUM_HEX_TYPE);
                            return;
                        }
                    } else if (ch == 'b' && inputSystem.hasNext()) { // 二进制
                        ch = inputSystem.nextChar();
                        if (ch >= '0' && ch <= '1' && inputSystem.hasNext()) {
                            do {
                                val = val * 2 + ch - '0';
                                ch = inputSystem.nextChar();
                            } while (ch >= '0' && ch <= '1' && inputSystem.hasNext());
                        } else {
                            // 0b后无数据
                            lexError(LexError.NUM_BIN_TYPE);
                            return;
//                            token = new Token(Tag.ERR);
//                            tokenList.add(token);
                        }
                    } else if (ch >= '0' && ch <= '7' && inputSystem.hasNext()) {
                        do {
                            val = val * 8 + ch - '0';
                            ch = inputSystem.nextChar();
                        } while (ch >= '0' && ch <= '7' && inputSystem.hasNext());
                    }
                }
                token = new Num(val);
                tokenList.add(token);
                if (!inputSystem.hasNext()) {
                    tokenList.add(new Token(Tag.TK_EOF));
                    break;
                }
            } else if (ch == '\'') { //字符常量
                char c = 0;
                if (inputSystem.hasNext()) {
                    ch = inputSystem.nextChar();
                    if (ch == '\\' && inputSystem.hasNext()) { //如果是转义符
                        ch = inputSystem.nextChar();
                        if (ch == 'n') c = '\n';
                        else if (ch == '\\') c = '\\';
                        else if (ch == 't') c = '\t';
                        else if (ch == '0') c = '\0';
                        else if (ch == '\'') c = '\'';
                        else if (ch == '\n') {
                            lexError(LexError.CHAR_NO_R_QUTION);
                            return;
                        } else {
                            c = ch;
                        }
                    } else if (ch == '\n') {
                        token = new Token(Tag.ERR);
                        lexError(LexError.CHAR_NO_R_QUTION);
                        return;
                    } else if (ch == '\'') { //空数据
//                        token = new Token(Tag.ERR);
                        lexError(LexError.CHAR_NO_DATA);
                        return;
//                        if (inputSystem.hasNext()) {
//                            ch = inputSystem.nextChar();
//                        }
                    } else {
                        c = ch;
                    }
                    if (inputSystem.hasNext()) {
                        ch = inputSystem.nextChar();
                        if (ch == '\'' && c != 0) {
                            token = new Char(c);
                        } else {
//                            token = new Token(Tag.ERR);
                            lexError(LexError.CHAR_NO_R_QUTION);
                            return;
                        }
                    } else {
//                            token = new Token(Tag.ERR);
                        lexError(LexError.CHAR_NO_R_QUTION);
                        return;
                    }
                } else {
//                    token = new Token(Tag.ERR);
                    lexError(LexError.CHAR_NO_R_QUTION);
                    return;
                }
                tokenList.add(token);
                if (!inputSystem.hasNext()) {
                    tokenList.add(new Token(Tag.TK_EOF));
                    break;
                }
            } else if (ch == '"') { //字符串常量
                if (inputSystem.hasNext()) {
                    StringBuilder stringBuilder = new StringBuilder();
                    ch = inputSystem.nextChar();
                    while (ch != '"' && inputSystem.hasNext()) {
                        if (ch == '\\') {
                            ch = inputSystem.nextChar();
                            if (ch == 'n') stringBuilder.append('\n');
                            else if (ch == '\\') stringBuilder.append('\\');
                            else if (ch == 't') stringBuilder.append('\t');
                            else if (ch == '"') stringBuilder.append('"');
                            else if (ch == '0') stringBuilder.append("\0");
                            else if (ch == '\n') ;
                            else if (!inputSystem.hasNext()) {
                                lexError(LexError.STR_NO_R_QUTION);
                                return;
//                                token = new Token(Tag.ERR);
//                                tokenList.add(token);

                            }
                        } else if (ch == '\n' || !inputSystem.hasNext()) {
                            lexError(LexError.STR_NO_R_QUTION);
                            return;
//                            token = new Token(Tag.ERR);
//
//                            tokenList.add(token);

                        } else {
                            stringBuilder.append(ch);
                        }
                        ch = inputSystem.nextChar();
                    }
                    token = new Str(stringBuilder.toString());
                    tokenList.add(token);
                }
                if (inputSystem.hasNext()) {
                    ch = inputSystem.nextChar();
                } else {
                    tokenList.add(new Token(Tag.TK_EOF));
                    break;
                }
            }
            switch (ch) {
                case '+':
                    token = new Token(inputSystem.nextChar('+') ? Tag.TK_INC : Tag.TK_PLUS);
                    break;
                case '-':
                    token = new Token(inputSystem.nextChar('-') ? Tag.TK_DEC : Tag.TK_MINUS);
                    break;
                case '*':
                    token = new Token(Tag.TK_STAR);
                    break;
                case '/':
                    if (inputSystem.hasNext()) {
                        ch = inputSystem.nextChar();
                        if (ch == '/') {
                            while (ch != '\n' && inputSystem.hasNext()) {
                                ch = inputSystem.nextChar();
                            }
                            token = new Token(Tag.ERR);
                        } else if (ch == '*') {
                            while (inputSystem.hasNext()) {
                                ch = inputSystem.nextChar();
                                if (ch == '*') {
                                    while (inputSystem.nextChar('*') && inputSystem.hasNext()) ;
                                    if (inputSystem.hasNext()) {
                                        ch = inputSystem.nextChar();
                                        if (ch == '/') { //注释正常结束
                                            token = new Token(Tag.ERR);
                                            tokenList.add(token);
                                            break;
                                        }
                                    }
                                }
                            }
                            if (token == null) {
                                lexError(LexError.COMMENT_NO_END);
                                return;
//                                token = new Token(Tag.ERR);
                            }
                        } else {
                            token = new Token(Tag.TK_DIVIDE);
                        }
                    } else {
                        token = new Token(Tag.TK_DIVIDE);
                    }
                    break;
                case '%':
                    token = new Token(Tag.TK_MOD);
                    break;
                case '>':
                    if (inputSystem.nextChar('=')) {
                        token = new Token(Tag.TK_GEQ);
                    } else if (inputSystem.nextChar('>')) {
                        token = new Token(Tag.TK_IN_PUT);
                    } else {
                        token = new Token(Tag.TK_GT);
                    }
                    break;
                case '<':
                    if (inputSystem.nextChar('=')) {
                        token = new Token(Tag.TK_LEQ);
                    } else if (inputSystem.nextChar('<')) {
                        token = new Token(Tag.TK_OUT_PUT);
                    } else {
                        token = new Token(Tag.TK_LT);
                    }
                    break;
                case '=':
                    token = new Token(inputSystem.nextChar('=') ? Tag.TK_EQ : Tag.TK_ASSIGN);
                    break;
                case '&':
                    token = new Token(inputSystem.nextChar('&') ? Tag.TK_AND : Tag.TK_LEA);
                    break;
                case '|':
                    token = new Token(inputSystem.nextChar('|') ? Tag.TK_OR : Tag.ERR);
                    if (token.getTag() == Tag.ERR) {
                        lexError(LexError.OR_NO_PAIR);
                        return;
                    }
                    break;
                case '!':
                    token = new Token(inputSystem.nextChar('=') ? Tag.TK_NEQ : Tag.TK_NOT);
                    break;
                case ':':
                    token = new Token(Tag.TK_COLON);
                    break;
                case ',':
                    token = new Token(Tag.TK_COMMA);
                    break;
                case ';':
                    token = new Token(Tag.TK_SEMICOLON);
                    break;
                case '(':
                    token = new Token(Tag.TK_OPEN_PA);
                    break;
                case ')':
                    token = new Token(Tag.TK_CLOSE_PA);
                    break;
                case '[':
                    token = new Token(Tag.TK_OPEN_BR);
                    break;
                case ']':
                    token = new Token(Tag.TK_CLOSE_BR);
                    break;
                case '{':
                    token = new Token(Tag.TK_LBRACE);
                    break;
                case '}':
                    token = new Token(Tag.TK_RBRACE);
                    break;
                case '.':
                    token = new Token(Tag.TK_DOT);
                    break;
                case ' ':

                case '\n':

                case '\t':
                    token = null;
                    break;
                default:
//                    token = new Token(Tag.ERR);
                    lexError(LexError.TOKEN_NO_EXIST);
                    return;
//                    break;
            }
            if (token != null) {
                tokenList.add(token);
            }
            if (!inputSystem.hasNext()) {
                tokenList.add(new Token(Tag.TK_EOF));
            }
        }
    }

    private void lexError(LexError lexError) {
        String[] lexErrorTable = {
                "字符串丢失右引号",
                "二进制数没有实体数据",
                "十六进制数没有实体数据",
                "字符没有右引号",
                "不支持空字符",
                "错误的“或”运算符",
                "多行注释没有正常结束",
                "词法记号不存在"
        };
        System.out.println(new StringBuilder().append("第 ").append(inputSystem.getLine_no()).append(" 行"));
        System.out.println(lexErrorTable[lexError.ordinal()]);
    }
}

enum LexError {
    STR_NO_R_QUTION, //字符串没有右引号
    NUM_BIN_TYPE,    //二进制数没有实体数据
    NUM_HEX_TYPE,    //十六进制数没有实体数据
    CHAR_NO_R_QUTION,//字符没有右引号
    CHAR_NO_DATA,    //字符没有数据
    OR_NO_PAIR,      // || 只有一个 |
    COMMENT_NO_END,  // 多行注释没有正常结束
    TOKEN_NO_EXIST   // 不存在的词法记号
}
