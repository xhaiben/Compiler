package cacher.lexer;

import java.util.ArrayList;
import java.util.List;

/*
 * Created by xhaiben on 2017/3/11.
 */

public class Lexer {

    private InputSystem inputSystem;

    private List<Token> tokenList;

    public Lexer(InputSystem inputSystem) {
        this.inputSystem = inputSystem;
        tokenList = new ArrayList<>();
    }

    public List<Token> getTokenList() {
        return tokenList;
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
                if (ch != '0' && inputSystem.hasNext()) {//十进制
                    do {
                        val += val * 10 + ch - '0';
                        ch = inputSystem.nextChar();
                    } while (Character.isDigit(ch) && inputSystem.hasNext());
                } else if (inputSystem.hasNext()) {
                    ch = inputSystem.nextChar();
                    if (ch == 'x' && inputSystem.hasNext()) {//十六进制
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
                            token = new Token(Tag.ERR);
                            tokenList.add(token);
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
                            token = new Token(Tag.ERR);
                            lexError(LexError.NUM_BIN_TYPE);
                            tokenList.add(token);
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
                        } else {
                            c = ch;
                        }
                    } else if (ch == '\n') {
                        token = new Token(Tag.ERR);
                        lexError(LexError.CHAR_NO_R_QUTION);
                    } else if (ch == '\'') { //空数据
                        token = new Token(Tag.ERR);
                        lexError(LexError.CHAR_NO_DATA);
                        if (inputSystem.hasNext()) {
                            ch = inputSystem.nextChar();
                        }
                    } else {
                        c = ch;
                    }
                    if (token == null && inputSystem.hasNext()) {
                        ch = inputSystem.nextChar();
                        if (ch == '\'' && c != 0) {
                            token = new Char(c);
                        } else {
                            token = new Token(Tag.ERR);
                            lexError(LexError.CHAR_NO_R_QUTION);
                        }
                    }
                } else {
                    token = new Token(Tag.ERR);
                    lexError(LexError.CHAR_NO_R_QUTION);
                }
                tokenList.add(token);
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
                            else {
                                token = new Token(Tag.ERR);
                                lexError(LexError.STR_NO_R_QUTION);
                                tokenList.add(token);
                                break;
                            }
                        } else {
                            stringBuilder.append(ch);
                        }
                        ch = inputSystem.nextChar();
                    }
                    ch=inputSystem.nextChar();
                    token = new Str(stringBuilder.toString());
                    tokenList.add(token);
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
                        } else if (ch == '*' && inputSystem.hasNext()) {
                            while (inputSystem.hasNext()) {
                                ch = inputSystem.nextChar();
                                if (ch == '*') {
                                    while (inputSystem.nextChar('*') && inputSystem.hasNext()) ;
                                    if (inputSystem.hasNext()) {
                                        ch = inputSystem.nextChar();
                                        if (ch == '/') {
                                            token = new Token(Tag.ERR);
                                            tokenList.add(token);
                                            break;
                                        }
                                    }
                                }
                            }
                            if (token == null) {
                                lexError(LexError.COMMENT_NO_END);
                                token = new Token(Tag.ERR);
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
                    token = new Token(inputSystem.nextChar('=') ? Tag.TK_GEQ : Tag.TK_GT);
                    break;
                case '<':
                    token = new Token(inputSystem.nextChar('=') ? Tag.TK_LEQ : Tag.TK_LT);
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
                    token = new Token(Tag.ERR);
                    lexError(LexError.TOKEN_NO_EXIST);
                    break;
            }
            if(token!=null){
                tokenList.add(token);
            }

        }
        tokenList.add(new Token(Tag.TK_EOF));
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
