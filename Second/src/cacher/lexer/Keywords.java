package cacher.lexer;

import java.util.HashMap;

/*
 * Created by xhaiben on 2017/3/11.
 */
public class Keywords {
    private static HashMap<String, Tag> keywordMap;
    static {
        keywordMap = new HashMap<>();
        keywordMap.put("char", Tag.KW_CHAR);
//        keywordMap.put("short", Tag.KW_SHORT);
        keywordMap.put("int", Tag.KW_INT);
        keywordMap.put("void", Tag.KW_VOID);
//        keywordMap.put("struct", Tag.KW_STRUCT);
        keywordMap.put("if", Tag.KW_IF);
        keywordMap.put("else", Tag.KW_ELSE);
        keywordMap.put("for", Tag.KW_FOR);
        keywordMap.put("continue", Tag.KW_CONTINUE);
        keywordMap.put("break", Tag.KW_BREAK);
        keywordMap.put("return", Tag.KW_RETURN);
//        keywordMap.put("sizeof", Tag.KW_SIZEOF);
        keywordMap.put("switch", Tag.KW_SWITCH);
        keywordMap.put("case", Tag.KW_CASE);
        keywordMap.put("default", Tag.KW_DEFAULT);
        keywordMap.put("do", Tag.KW_DO);
        keywordMap.put("while", Tag.KW_WHILE);
        keywordMap.put("extern",Tag.KW_EXTERN);
    }

    public static Tag getTag(String name) {
        return keywordMap.containsKey(name) ? keywordMap.get(name) : Tag.TK_IDENT;
    }
}
