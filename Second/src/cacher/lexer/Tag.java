package cacher.lexer;
/*
 * Created by xhaiben on 2017/3/11.
 */
public enum Tag {
    /*运算符及分隔符*/

    TK_PLUS, // + 加号
    TK_MINUS, // - 减号

    TK_INC, // ++ 自加
    TK_DEC, // -- 自减

    TK_STAR, //* 星号
    TK_DIVIDE, // /除号
    TK_MOD, // % 取余
    TK_EQ, // == 等于号
    TK_NOT, //  ! 取非
    TK_NEQ, // != 不等号
    TK_LT, // < 小于号
    TK_LEQ, // <= 小于等于
    TK_GT, // > 大于号
    TK_GEQ, // >= 大于等于
    TK_ASSIGN, // = 赋值

    TK_IN_PUT, // >> 输入
    TK_OUT_PUT, // << 输出

    TK_POINT_TO, // -> 指向结构体成员运算符
    TK_DOT, // . 结构体成员运算符

//    TK_LEA, // & 地址与运算符
    TK_AND, // && 与
    TK_OR, // || 或
    TK_OPEN_PA, // ( 左圆括号
    TK_CLOSE_PA, // ) 右圆括号
    TK_OPEN_BR, // [ 左中括号
    TK_CLOSE_BR, // ] 右中括号
    TK_LBRACE, // { 左大括号
    TK_RBRACE, // } 右大括号
    TK_SEMICOLON, // ; 分号
    TK_COMMA, // , 逗号
    TK_ELLIPSIS, // ... 省略号
    TK_COLON, //  : 冒号
    TK_EOF, // 文件结束符

    /*常量*/

    TK_C_NUM, //数字常量
    TK_C_CHAR, //字符常量
    TK_C_STR, //字符串常量

    /*关键字*/

    KW_CHAR, // char
//    KW_SHORT, //short
    KW_INT, // INT
    KW_VOID, // void
    KW_STRING, //string
//    KW_STRUCT, //struct
    KW_IF, // if
    KW_ELSE, //else
    KW_FOR, // for
    KW_CONTINUE, // continue
    KW_BREAK, //break
    KW_RETURN, // return
//    KW_SIZEOF, // sizeof
    KW_SWITCH, // switch
    KW_CASE, // case
    KW_DEFAULT, // default
    KW_DO, // do
    KW_WHILE, // while
    KW_EXTERN, // extern
    KW_IN, // input
    KW_OUT, //output

    KW_ALIGN, // __align
    KW_CDECL, // __cdecl
    KW_STD_CALL, // __stdcall

    /*标识符*/

    TK_IDENT,

    /*错误*/
    ERR,
    NULL
}
