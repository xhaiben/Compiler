package cacher.error;

/*
 * Created by xhaiben on 17-4-23.
 */

import cacher.lexer.Lexer;

public class Error {

    public static int errorNum = 0;
    public static int warnNum = 0;
    public static int synerr = 0;
    public static int semerr = 0;


    public enum LexError {
        STR_NO_R_QUTION, //字符串没有右引号
        NUM_BIN_TYPE,    //二进制数没有实体数据
        NUM_HEX_TYPE,    //十六进制数没有实体数据
        CHAR_NO_R_QUTION,//字符没有右引号
        CHAR_NO_DATA,    //字符没有数据
        OR_NO_PAIR,      // || 只有一个 |
        COMMENT_NO_END,  // 多行注释没有正常结束
        TOKEN_NO_EXIST;  // 不存在的词法记号

        private static String[] lexErrorTable = {
                "字符串丢失右引号",
                "二进制数没有实体数据",
                "十六进制数没有实体数据",
                "字符没有右引号",
                "不支持空字符",
                "错误的“或”运算符",
                "多行注释没有正常结束",
                "词法记号不存在"
        };

        public static void lexError(Error.LexError lexError, int line_num, int line_col) {
            errorNum++;
            System.out.print(new StringBuilder().append("第 ").append(line_num).append(" 行"));
            System.out.println(new StringBuilder("，第 ").append(line_col).append(" 列"));
            System.out.println(lexErrorTable[lexError.ordinal()]);
        }
    }

    public enum ParserError {
        semiconlost, commalost, typelost, identlost, semiconwrong, typewrong,//变量声明部分的错误类型
        paralost, rparenlost, lbraclost, rbraclost,//函数定义部分的错误类型
        statementexcp, localidentlost, lparenlost, lparenwrong, staterparenlost, rparenwrong, elselost, elsespelterr, elsewrong,//复合语句部分的错误类型
        idtaillost, returnwrong, arglost, argwrong, arglistwrong, na_input, input_err, output_err,
        opplost, oppwrong, exprlost, exprparenlost, exprwrong;

        public static void synterror(ParserError error, int line_num) {
            errorNum++;
            synerr++;
            System.out.printf("在第 %d 行，语法分析错误   ", line_num);
            switch (error) {
                case semiconlost:
                    System.out.println("符号 ; 缺失");
                    break;
                case commalost:
                    System.out.println("符号 , 缺失");
                    break;
                case typelost:
                    System.out.println("类型错误");
                    break;
                case identlost:
                    System.out.println("缺失变量名");
                    break;
                case semiconwrong:
                    System.out.println("符号 ; 错误");
                    break;
                case typewrong:
                    System.out.println("类型错误");
                    break;
                case paralost:
                    System.out.println("缺失参数名");
                    break;
                case rparenlost:
                    System.out.println("缺失 )");
                    break;
                case lbraclost:
                    System.out.println("缺失 {");
                    break;
                case rbraclost:
                    System.out.println("缺失 }");
                    break;
                case statementexcp:
                    System.out.println("无效的语句");
                    break;
                case localidentlost:
                    System.out.println("缺失变量名");
                    break;
                case lparenlost:
                    System.out.println("缺失 (");
                    break;
                case lparenwrong:
                    System.out.println(" ( 错误");
                    break;
                case staterparenlost:
                    System.out.println("缺失 )");
                    break;
                case rparenwrong:
                    System.out.println(" ) 错误");
                    break;
                case elselost:
                    System.out.println("可能缺失 else");
                    break;
                case elsespelterr:
                    System.out.println("错误的 else");
                    break;
                case elsewrong:
                    System.out.println("else 错误");
                    break;
                case idtaillost:
                    System.out.println("变量名错误");
                    break;
                case returnwrong:
                    System.out.println("返回类型错误");
                    break;
                case arglost:
                    System.out.println("参数缺失");
                    break;
                case argwrong:
                    System.out.println("参数错误");
                    break;
                case opplost:
                    System.out.println("操作符缺失");
                    break;
                case oppwrong:
                    System.out.println("错误的操作符");
                    break;
                case exprlost:
                    System.out.println("表达式缺失");
                    break;
                case exprparenlost:
                    System.out.println("表达式错误");
                    break;
                case exprwrong:
                    System.out.println("表达式错误");
                    break;
                case na_input:
                    System.out.println("无效的输入");
                    break;
                case input_err:
                    System.out.println("输入错误");
                    break;
                case output_err:
                    System.out.println("输出错误");
                    break;
            }

        }
    }

    public enum SemError {
        var_redef, para_redef, localvar_redef, fun_redef, fun_def_err, fun_dec_err,//声明类错误
        str_nadd, void_ncal,//表达式类错误
        void_nassi, assi_ncomtype, ret_type_err, fun_undec, var_undec, real_args_err, str_nb_cond, void_nb_cond, break_nin_while, continue_nin_while, void_nin;

        public static void semerror(SemError error) {
            if (synerr != 0) {
                return;
            }
            errorNum++;
            semerr++;
            System.out.printf("在第 %d 行，语义分析错误   ", Lexer.getLine_num());
            switch (error) {
                case var_redef:
                    System.out.printf("全局变量名称重定义。\n");
                    break;
                case para_redef:
                    System.out.printf("参数名称重定义。\n");
                    break;
                case localvar_redef:
                    System.out.printf("局部变量名称重定义。\n");
                    break;
                case fun_redef:
                    System.out.printf("函数重定义。\n");
                    break;
                case fun_def_err:
                    System.out.printf("函数定义和先前的声明不一致。\n");
                    break;
                case fun_dec_err:
                    System.out.printf("函数的多次声明不一致。\n");
                    break;
                case fun_undec:
                    System.out.printf("函数被调用之前没有合法的声明。\n");
                    break;
                case var_undec:
                    System.out.printf("变量在使用之前没有合法的声明。\n");
                    break;
                case str_nadd:
                    System.out.printf("字符串不能运用于除了加法以外的运算。\n");
                    break;
                case void_ncal:
                    System.out.printf("void类型不能参加表达式运算。\n");
                    break;
                case void_nassi:
                    System.out.printf("void类型不能参加赋值运算。\n");
                    break;
                case assi_ncomtype:
                    System.out.printf("赋值类型不匹配。\n");
                    break;
                case ret_type_err:
                    System.out.printf("函数返回值与函数类型不匹配。\n");
                    break;
                case real_args_err:
                    System.out.printf("函数实参的类型不能与函数的形参声明严格匹配。\n");
                    break;
                case str_nb_cond:
                    System.out.printf("字符串类型不能作为条件。\n");
                    break;
                case void_nb_cond:
                    System.out.printf("void类型不能作为条件。\n");
                    break;
                case break_nin_while:
                    System.out.printf("break语句不能出现在while之外。\n");
                    break;
                case continue_nin_while:
                    System.out.printf("continue语句不能出现在while之外。\n");
                    break;
                case void_nin:
                    System.out.printf("void类型不能作为输入的对象。\n");
                    break;
            }
        }
    }
}
