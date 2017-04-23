package cacher.generator;

import cacher.error.Error;
import cacher.lexer.Tag;
import cacher.lexer.Token;
import cacher.semantic.semantic;
import cacher.semantic.var_record;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/*
 * Created by xhaiben on 17-4-23.
 */
public class generator {
    private FileOutputStream fileOutputStream;
    private static generator gener = null;
    private int ID = 0;

    static {
        if (gener == null) {
            gener = new generator();
        }
    }

    private generator() {

    }

    public static generator getInstance() {
        return generator.gener;
    }

    public void set_out_file(File _out_file) {
        if (fileOutputStream != null) {
            return;
        }
        if (!_out_file.exists()) {
            try {
                _out_file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            this.fileOutputStream = new FileOutputStream(_out_file, false);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 产生目标代码中的唯一的标签名
     * head——标题：tmp,var,str,fun,lab
     * type——类型: rsv_char,rsv_int,rsv_string
     * name——名称
     */
    public String gen_name(String head, Tag type, String name) {
        this.ID++;
        String retStr = "@" + head;
        if (type != null) {
            retStr += "_";
            retStr += type.name();
        }
        if (!name.equals("")) {
            retStr += "_";
            retStr += name;
        }
        if (!head.equals("str") && !head.equals("fun") && !head.equals("var")) {
            retStr += "_";
            retStr += String.valueOf(this.ID);
        }
        return retStr;
    }

    public void gen_local_var(int val) {
        if (Error.errorNum != 0) {
            return;
        }
        try {
            this.fileOutputStream.write(String.format("\tpush %d\n", val).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param factor1 左操作数
     * @param opp     操作符
     * @param factor2 右操作数
     * @param var_num 变量个数
     * @return var_record
     */
    public var_record gen_exp(var_record factor1, Tag opp, var_record factor2, int[] var_num) {
        if (Error.errorNum != 0) {
            return null;
        }
        if (factor1 == null || factor2 == null) {
            return null;
        }
        if (factor1.type.getTag() == Tag.KW_VOID || factor2.type.getTag() == Tag.KW_VOID) {
            //semerror
            return null;
        }
        Tag rsl_type = Tag.KW_INT;
        if (factor1.type.getTag() == Tag.KW_STRING || factor2.type.getTag() == Tag.KW_STRING) {
            if (opp == Tag.TK_PLUS) {
                rsl_type = Tag.KW_STRING;
            } else {
                //semerror
                return null;
            }
        } else {
            if (opp == Tag.TK_GT || opp == Tag.TK_GEQ || opp == Tag.TK_LT || opp == Tag.TK_LEQ || opp == Tag.TK_EQ || opp == Tag.TK_NEQ) {
                rsl_type = Tag.KW_CHAR;
            }
        }
        var_record rec = semantic.tfun.create_tmp_var(new Token(rsl_type), 0, var_num);
        String lab_lop, lab_ext;
        switch (rsl_type) {
            case KW_STRING:
                if (factor2.type.getTag() == Tag.KW_STRING) {
                    lab_lop = gen_name("lab", null, "cpystr2");
                    lab_ext = gen_name("lab", null, "cpystr2_exit");
                    if (factor2.strValID == -1) {
                        this.out_code(String.format(";----------生成动态string%s的代码----------\n", factor2.name));
                        this.out_code("\tmov eax,[@s_esp]\n\tmov [@s_esp],esp\n\tmov esp,eax\n");
                        if (factor2.localAddr < 0) {
                            this.out_code(String.format("\tmov ebx,[ebp%d]\n\tmov eax,0\n\tmov al,[ebx]\n", factor2.localAddr));
                        } else {
                            this.out_code(String.format("\tmov ebx,[ebp+%d]\n\tmov eax,0\n\tmov al,[ebx]\n", factor2.localAddr));
                        }
                        this.out_code("\tsub esp,1\n\tmov [esp],al;长度压入后再压入数据栈\n");

                    }
                }
        }
    }

    private void out_code(String code) {
        try {
            fileOutputStream.write(code.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
