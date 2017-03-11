package cacher;
/*
 * Created by xhaiben on 2017/3/11.
 */
public enum Tag {
    /*运算符及分隔符*/

    TK_PLUS, // * 加号
    TK_MINUS, // - 减号
    TK_STAR, //* 星号
    TK_DIVIDE, // /除号
    TK_MOD, // % 取余
    TK_EQ, // == 等于号
    TK_NEQ, // != 不等号
    TK_LT, // < 小于号
    TK_LEQ, // <= 小于等于
    TK_GT, // > 大于号
    TK_GRQ, // >= 大于等于
    TK_ASSIGN, // = 赋值
    TK_POINT_TO, // -> 指向结构体成员运算符
    TK_DOT, // . 结构体成员运算符
    TK_AND, // & 地址与运算符
    TK_OPEN_PA, // ( 左圆括号
    TK_CLOSE_PA, // ) 右圆括号
    TK_OPEN_BR, // [ 左中括号
    TK_CLOSE_BR, // ] 右中括号
    TK_BEGIN, // { 左大括号
    TK_END, // } 右大括号
    TK_SEMICOLON, // ; 分号
    TK_COMMA, // , 逗号
    TK_ELLIPSIS, // ... 省略号
    TK_EOF, // 文件结束符

    /*常量*/

    TK_C_INT, //整型常量
    TK_C_CHAR, //字符常量
    TK_C_STR, //字符串常量

    /*关键字*/

    KW_CHAR, // char
    KW_SHORT, //short
    KW_INT, // INT
    KW_VOID, // void
    KW_STRUCT, //struct
    KW_IF, // if
    KW_ELSE, //else
    KW_FOR, // for
    KW_CONTINUE, // continue
    KW_BREAK, //break
    KW_RETURN, // return
    KW_SIZEOF, // sizeof

    KW_ALIGN, // __align
    KW_CDECL, // __cdecl
    KW_STD_CALL, // __stdcall

    /*标识符*/
    TK_IDENT
}
