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

import static cacher.semantic.semantic.tfun;

/*
 * Created by xhaiben on 17-4-23.
 */
public class generator {
    private FileOutputStream fileOutputStream;
    private static generator gener = null;
    private int ID = 0;
    private int convert_buffer = 0;

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
        var_record rec = tfun.create_tmp_var(new Token(rsl_type), 0, var_num);
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
                        this.out_code("\tsub esp,1\n\tmov [esp],al;长度压入后再压入数据栈\n");
                        this.out_code(String.format("\tmov [ebp%d],esp\n", rec.localAddr));//存入数据指针

                        this.out_code("\tcmp eax,0\n");
                        this.out_code(String.format("\tje %s\n", lab_ext));
                        this.out_code("\tmov ecx,0\n");
                        this.out_code("\tmov esi,ebx\n\tsub esi,1\n");
                        this.out_code("\tneg eax\n");
                        this.out_code(String.format("%s:\n", lab_lop));
                        this.out_code("\tcmp ecx,eax\n");
                        this.out_code(String.format("\tje %s\n", lab_ext));
                        this.out_code("\tmov dl,[esi+ecx]\n");
                        this.out_code("\tsub esp,1\n\tmov [esp],dl\n");
                        this.out_code("\tdec ecx\n");
                        this.out_code(String.format("\tjmp %s\n", lab_lop));
                        this.out_code(String.format("%s:\n", lab_ext));
                        this.out_code("\tmov eax,[@s_esp]\n\tmov [@s_esp],esp\n\tmov esp,eax\n");//esp<=>[@s_esp]
                    } else if (factor2.strValID > 0) {
                        this.out_code(";----------生成常量string%s的代码----------\n", factor2.name);
                        this.out_code("\tmov eax,[@s_esp]\n\tmov [@s_esp],esp\n\tmov esp,eax\n");//esp<=>[@s_esp]
                        this.out_code("\tmov eax,@str_%d_len\n\tsub esp,1\n\tmov [esp],al;长度压入后再压入数据栈\n", factor2.strValID);
                        this.out_code("\tmov [ebp%d],esp\n", rec.localAddr);//存入数据指针

                        this.out_code("\tcmp eax,0\n");//测试长度是否是0
                        this.out_code("\tje %s\n", lab_ext);
                        this.out_code("\tmov ecx,@str_%d_len\n\tdec ecx\n", factor2.strValID);//
                        this.out_code("\tmov esi,@str_%d\n", factor2.strValID);//取得首地址
                        this.out_code("%s:\n", lab_lop);
                        this.out_code("\tcmp ecx,-1\n");
                        this.out_code("\tje %s\n", lab_ext);
                        this.out_code("\tmov al,[esi+ecx]\n");
                        this.out_code("\tsub esp,1\n\tmov [esp],al\n");
                        this.out_code("\tdec ecx\n");
                        this.out_code("\tjmp %s\n", lab_lop);
                        this.out_code("%s:\n", lab_ext);
                        this.out_code("\tmov eax,[@s_esp]\n\tmov [@s_esp],esp\n\tmov esp,eax\n");
                    } else if (factor2.strValID == -2) {
                        this.out_code(";----------生成全局string%s的代码----------\n", factor2.name);
                        this.out_code("\tmov eax,[@s_esp]\n\tmov [@s_esp],esp\n\tmov esp,eax\n");//esp<=>[@s_esp]
                        if (this.convert_buffer == 0)
                            this.out_code("\tmov eax,0\n\tmov al,[@str_%s_len]\n\tsub esp,1\n\tmov [esp],al;长度压入后再压入数据栈\n", factor2.name);
                        else
                            this.out_code("\tmov eax,0\n\tmov al,[%s_len]\n\tsub esp,1\n\tmov [esp],al;长度压入后再压入数据栈\n", factor2.name);
                        this.out_code("\tmov [ebp%d],esp\n", rec.localAddr);//存入数据指针

                        this.out_code("\tcmp eax,0\n");//测试长度是否是0
                        this.out_code("\tje %s\n", lab_ext);
                        this.out_code("\tsub eax,1\n\tmov ecx,eax\n");
                        if (convert_buffer == 0)
                            this.out_code("\tmov esi,@str_%s\n", factor2.name);//取得首地址
                        else
                            this.out_code("\tmov esi,%s\n", factor2.name);//取得首地址
                        this.out_code("%s:\n", lab_lop);
                        this.out_code("\tcmp ecx,-1\n");
                        this.out_code("\tje %s\n", lab_ext);
                        this.out_code("\tmov al,[esi+ecx]\n");
                        this.out_code("\tsub esp,1\n\tmov [esp],al\n");
                        this.out_code("\tdec ecx\n");
                        this.out_code("\tjmp %s\n", lab_lop);
                        this.out_code("%s:\n", lab_ext);
                        this.out_code("\tmov eax,[@s_esp]\n\tmov [@s_esp],esp\n\tmov esp,eax\n");//esp<=>[@s_esp]
                    } else if (factor2.strValID == 0) {
                        this.out_code("\tmov eax,[@s_esp]\n\tmov [@s_esp],esp\n\tmov esp,eax\n");//esp<=>[@s_esp]
                        this.out_code("\tmov eax,0\n\tsub esp,1\n\tmov [esp],al;长度压入后再压入数据栈\n");
                        this.out_code("\tmov [ebp%d],esp\n", rec.localAddr);//存入数据指针
                        this.out_code("\tmov eax,[@s_esp]\n\tmov [@s_esp],esp\n\tmov esp,eax\n");//esp<=>[@s_esp]
                    }
                } else if (factor2.type.getTag() == Tag.KW_INT) {
                    lab_lop = gen_name("lab", null, "numtostr1");
                    lab_ext = gen_name("lab", null, "numtostr1_exit");
                    String labNumSign = gen_name("lab", null, "numsign1");
                    String labNumSignExt = gen_name("lab", null, "numsign1_exit");
                    String lab2long = gen_name("lab", null, "numsign1_add");
                    this.out_code(";----------生成number%s的string代码----------\n", factor1.name);
                    this.out_code("\tmov eax,[ @s_esp]\n\tmov[ @s_esp],esp\n\tmov esp, eax\n");
                    if (factor1.localAddr == 0) {
                        this.out_code("\tmov eax,[@var_%s]\n", factor1.name);
                    } else {
                        if (factor1.localAddr < 0) {
                            this.out_code("\tmov eax,[ebp%d]\n", factor1.localAddr);
                        } else {
                            this.out_code("\tmov eax,[ebp+%d]\n", factor1.localAddr);
                        }
                    }
                    this.out_code("\tmov esi,[ebp%d];\n", rec.localAddr);//将临时字符串的长度地址记录下来

                    //确定数字的正负
                    this.out_code("\tmov edi,0\n");//保存eax符号：0+ 1-
                    this.out_code("\tcmp eax,0\n");
                    this.out_code("\tjge %s\n", labNumSignExt);
                    this.out_code("%s:\n", labNumSign);
                    this.out_code("\tneg eax\n");
                    this.out_code("\tmov edi,1\n");
                    this.out_code("%s:\n", labNumSignExt);

                    //累加长度，压入数据
                    this.out_code("\tmov ebx,10\n");
                    this.out_code("%s:\n", lab_lop);
                    this.out_code("\tmov edx,0\n\tidiv ebx\n\tmov cl,[esi]\n\tinc cl\n\tmov [esi],cl\n\tsub esp,1\n\tadd dl,48\n\tmov [esp],dl\n\tcmp eax,0\n");
                    this.out_code("\tjne %s\n", lab_lop);

                    //添加符号
                    this.out_code("\tcmp edi,0\n");
                    this.out_code("\tje %s\n", lab2long);
                    this.out_code("\tsub esp,1\n\tmov ecx,%d\n\tmov [esp],cl\n", '-');
                    this.out_code("\tmov cl,[esi]\n\tinc cl\n\tmov [esi],cl\n");

                    this.out_code("%s:\n", lab2long);
                    //仅仅是测试字符串总长是否超过255，超出报错
                    this.out_code("\tcmp cl,255\n");
                    this.out_code("\tjna %s\n", lab_ext);
                    this.out_code("\tcall @str2long\n");
                    this.out_code("%s:\n", lab_ext);
                    this.out_code("\tmov eax,[ @s_esp]\n\tmov[ @s_esp],esp\n\tmov esp, eax\n ");
                } else if (factor1.type.getTag() == Tag.KW_CHAR) {
                    lab_ext = gen_name("lab", null, "chtostr2_exit");
                    this.out_code(";----------生成char%s的string代码----------\n", factor1.name);
                    this.out_code("\tmov eax,[@s_esp]\n\tmov [@s_esp],esp\n\tmov esp,eax\n");
                    if (factor1.localAddr == 0) {
                        this.out_code("\tmov eax,[@var_%s]\n", factor1.name);
                    } else {
                        if (factor1.localAddr < 0) {
                            this.out_code("\tmov eax,[ebp%d]\n", factor1.localAddr);
                        } else {
                            this.out_code("\tmov eax,[ebp+%d]\n", factor1.localAddr);
                        }
                    }
                    this.out_code("\tmov esi,[ebp%d];\n", rec.localAddr);
                    this.out_code("\tmov cl,[esi]\n\tinc cl\n\tmov [esi],cl\n\tsub esp,1\n\tmov [esp],al\n");
                    this.out_code("\tcmp cl,255\n");
                    this.out_code("\tjna %s\n", lab_ext);
                    this.out_code("\tcall @str2long\n");
                    this.out_code("%s:\n", lab_ext);
                    this.out_code("\tmov eax,[ @s_esp]\n\tmov[ @s_esp],esp\n\tmov esp, eax\n ");
                }
                this.out_code(";--------------------------------------------------\n");
                break;
            case KW_INT:
                if (factor1.localAddr == 0) {
                    this.out_code("\tmov eax,[@var_%s]\n", factor1.name);
                } else {
                    if (factor1.localAddr < 0) {
                        this.out_code("\tmov eax,[ebp%d]\n", factor1.localAddr);
                    } else {
                        this.out_code("\tmov eax,[ebp+%d]\n", factor1.localAddr);
                    }
                }
                if (factor2.localAddr == 0) {
                    this.out_code("\tmov ebx,[@var_%s]\n", factor2.name);
                } else {
                    if (factor2.localAddr < 0) {
                        this.out_code("\tmov ebx,[ebp%d]\n", factor2.localAddr);
                    } else {
                        this.out_code("\tmov ebx,[ebp+%d]\n", factor2.localAddr);
                    }
                }
                switch (opp) {
                    case TK_PLUS:
                        this.out_code("\tadd eax,ebx\n");
                        break;
                    case TK_MINUS:
                        this.out_code("\tsub eax,ebx\n");
                        break;
                    case TK_STAR:
                        this.out_code("\timul ebx\n");
                        break;
                    case TK_DIVIDE:
                        this.out_code("\tmov edx,0\n");
                        this.out_code("\tidiv ebx\n");
                        break;
                }
                this.out_code("\tmov [ebp%d],eax\n", rec.localAddr);
                break;
            case KW_CHAR:
                if (factor1.type.getTag() == Tag.KW_STRING) {

                } else {
                    lab_lop = gen_name("lab", null, "base_cmp");
                    lab_ext = gen_name("lab", null, "base_cmp_exit");
                    if (factor1.localAddr == 0) {
                        this.out_code("\tmov eax,[@var_%s]\n", factor1.name);
                    } else {
                        if (factor1.localAddr < 0) {
                            this.out_code("\tmov eax,[ebp%d]\n", factor1.localAddr);
                        } else {
                            this.out_code("\tmov eax,[ebp+%d]\n", factor1.localAddr);
                        }
                    }
                    if (factor2.localAddr == 0) {
                        this.out_code("\tmov ebx,[@var_%s]\n", factor2.name);
                    } else {
                        if (factor2.localAddr < 0) {
                            this.out_code("\tmov ebx,[ebp%d]\n", factor2.localAddr);
                        } else {
                            this.out_code("\tmov ebx,[ebp+%d]\n", factor2.localAddr);
                        }
                    }
                    this.out_code("\tcmp eax,ebx\n");
                    switch (opp) {
                        case TK_GT:
                            this.out_code("\tjg %s\n", lab_lop);
                            break;
                        case TK_GEQ:
                            this.out_code("\tjge %s\n", lab_lop);
                            break;
                        case TK_LT:
                            this.out_code("\tjl %s\n", lab_lop);
                            break;
                        case TK_LEQ:
                            this.out_code("\tjle %s\n", lab_lop);
                            break;
                        case TK_EQ:
                            this.out_code("\tje %s\n", lab_lop);
                            break;
                        case TK_NEQ:
                            this.out_code("\tjne %s\n", lab_lop);
                            break;
                    }
                    this.out_code("\tmov eax,0\n");
                    this.out_code("\tjmp %s\n", lab_ext);
                    this.out_code("%s:\n", lab_lop);
                    this.out_code("\tmov eax,1\n");
                    this.out_code("%s:\n", lab_ext);
                    this.out_code("\tmov [ebp%d],eax\n", rec.localAddr);
                }
                break;
        }
        return rec;
    }

    public var_record gen_assign(var_record des, var_record src, int[] var_num) {
        if (Error.errorNum != 0) {
            return null;
        }
        if (des.type.getTag() == Tag.KW_VOID) {
            Error.SemError.semerror(Error.SemError.void_nassi);
            return null;
        }
        if (des.type.getTag() == Tag.KW_STRING) {

        } else if (des.type.getTag() == Tag.KW_CHAR && src.type.getTag() == Tag.KW_INT) {

        } else if (des.type.getTag() == Tag.KW_INT && src.type.getTag() == Tag.KW_CHAR) {

        } else if (des.type.getTag() != src.type.getTag()) {
            Error.SemError.semerror(Error.SemError.assi_ncomtype);
            return null;
        }
        if (des.type.getTag() == Tag.KW_STRING) {
            if (src.strValID != -1) {
                var_record empstr = new var_record();
                String empname = "";
                empstr.init(new Token(Tag.KW_STRING), empname);
                src = gen_exp(empstr, Tag.TK_PLUS, src, var_num);
            }
            if (des.strValID == -2) {
                String lab_lop = gen_name("lab", null, "cpy2gstr");
                String lab_ext = gen_name("lab", null, "cpy2gstr_exit");
                if (src.localAddr < 0) {
                    this.out_code("\tmov ecx,0\n\tmov esi,[ebp%d]\n\tmov cl,[esi]\n", src.localAddr);
                } else {
                    this.out_code("\tmov ecx,0\n\tmov esi,[ebp+%d]\n\tmov cl,[esi]\n", src.localAddr);
                }
                this.out_code("\tcmp ecx,0\n\tje %s\n", lab_ext);
                this.out_code("\tmov [@str_%s_len],cl\n", des.name);//先复制长度
                this.out_code("\tsub esi,ecx\n");
                this.out_code("\tmov edi,@str_%s\n", des.name);
                this.out_code("\tmov edx,0\n");
                this.out_code("%s:\n", lab_lop);
                this.out_code("\tmov al,[esi+edx]\n\tmov [edi+edx],al\n");
                this.out_code("\tinc edx\n\tcmp edx,ecx\n\tje %s\n\tjmp %s\n", lab_ext, lab_lop);
                this.out_code("%s:\n", lab_ext);
            } else {
                des.strValID = -1;
                if (src.localAddr < 0) {
                    this.out_code("\tmov eax,[ebp%d]\n", src.localAddr);
                } else {
                    this.out_code("\tmov eax,[ebp+%d]\n", src.localAddr);
                }
                if (des.localAddr < 0) {
                    this.out_code("\tmov [ebp%d],eax\n", des.localAddr);
                } else {
                    this.out_code("\tmov [ebp+%d],eax\n", des.localAddr);
                }
            }
        } else {
            if (des.localAddr == 0) {
                this.out_code("\tmov eax,@var_%s\n", des.name);
            } else {
                if (des.localAddr < 0) {
                    this.out_code("\tlea eax,[ebp%d]\n", des.localAddr);
                } else {
                    this.out_code("\tlea eax,[ebp+%d]\n", des.localAddr);
                }
                if (src.localAddr == 0) {
                    this.out_code("\tmov ebx,[@var_%s]\n", src.name);
                } else {
                    if (src.localAddr < 0) {
                        this.out_code("\tmov ebx,[ebp%d]\n", src.localAddr);
                    } else {
                        this.out_code("\tmov ebx,[ebp+%d]\n", src.localAddr);
                    }
                }
                this.out_code("\tmov [eax],ebx\n");
            }
        }
        return des;
    }

    public void gen_return(var_record ret, int[] var_num) {
        if (Error.errorNum != 0) {
            return;
        }
        if (ret != null) {
            if (ret.type.getTag() == Tag.KW_STRING) {
                var_record empstr = new var_record();
                String empname = "";
                empstr.init(new Token(Tag.KW_STRING), empname);
                ret = gen_exp(empstr, Tag.TK_PLUS, ret, var_num);
            }
            if (ret.localAddr < 0) {
                this.out_code("\tmov eax,[ebp%d]\n", ret.localAddr);
            } else {
                this.out_code("\tmov eax,[ebp+%d]\n", ret.localAddr);
            }
        } else {
            if (ret.localAddr < 0) {
                this.out_code("\tmov eax,[ebp%d]\n", ret.localAddr);
            } else {
                this.out_code("\tmov eax,[ebp+%d]\n", ret.localAddr);
            }
        }
        this.out_code("\tmov ebx,[@s_ebp]\n\tmov [@s_esp],ebx\n");//s_leave
        this.out_code("\tmov ebx,[@s_esp]\n\tmov [@s_esp],esp\n\tmov esp,ebx\n");//esp<=>[@s_esp]
        this.out_code("\tpop ebx\n\tmov [@s_ebp],ebx\n");//s_ebp
        this.out_code("\tmov ebx,[@s_esp]\n\tmov [@s_esp],esp\n\tmov esp,ebx\n");//esp<=>[@s_esp]
        this.out_code("\tmov esp,ebp\n\tpop ebp\n\tret\n");//leave
    }

    public void gen_fun_head() {
        if (Error.errorNum != 0) {
            return;
        }
        this.out_code("%s:\n", tfun.name);
        this.out_code("\tpush ebp\n\tmov ebp,esp\n");//enter
        this.out_code("\tmov ebx,[@s_esp]\n\tmov [@s_esp],esp\n\tmov esp,ebx\n");//esp<=>[@s_esp]
        this.out_code("\tmov ebx,[@s_ebp]\n\tpush ebx\n\tmov [@s_ebp],esp\n");//s_enter
        this.out_code("\tmov ebx,[@s_esp]\n\tmov [@s_esp],esp\n\tmov esp,ebx\n");//esp<=>[@s_esp]
        this.out_code("\t;函数头\n");
    }

    public void gen_fun_tail() {
        if (Error.errorNum != 0) {
            return;
        }
        if (tfun.had_ret != 0) {
            return;
        }
        this.out_code("\t;函数尾\n");
        this.out_code("\tmov ebx,[@s_ebp]\n\tmov [@s_esp],ebx\n");//s_leave
        this.out_code("\tmov ebx,[@s_esp]\n\tmov [@s_esp],esp\n\tmov esp,ebx\n");//esp<=>[@s_esp]
        this.out_code("\tpop ebx\n\tmov [@s_ebp],ebx\n");//s_ebp
        this.out_code("\tmov ebx,[@s_esp]\n\tmov [@s_esp],esp\n\tmov esp,ebx\n");//esp<=>[@s_esp]
        this.out_code("\tmov esp,ebp\n\tpop ebp\n\tret\n");//leave
    }

    public void gen_loc_var(int val) {
        if (Error.errorNum != 0) {
            return;
        }
        this.out_code("\tpush %d\n", val);
    }

    public void gen_condition(var_record cond) {
        if (Error.errorNum != 0) {
            return;
        }
        if (cond == null) {
            return;
        }
        if (cond.type.getTag() == Tag.KW_STRING) {
            Error.SemError.semerror(Error.SemError.str_nb_cond);
            return;
        } else if (cond.type.getTag() == Tag.KW_VOID) {
            Error.SemError.semerror(Error.SemError.void_nb_cond);
            return;
        } else {
            if (cond.localAddr == 0) {
                this.out_code("\tmov eax,[@var_%s]\n", cond.name);
            } else {
                this.out_code("\tmov eax,[ebp+%d]\n", cond.name);
            }
            this.out_code("\tcmp eax,0\n");
        }
    }

    public int gen_block(int n) {
        if (Error.errorNum != 0) {
            return -1;
        }
        if (n == -1) {
            return tfun.getCurAddr();
        } else {
            if (n != 0) {
                this.out_code("\tlea esp,[ebp%d]\n", n);
            } else {
                this.out_code("\tmov esp,ebp\n");
            }
            return -2;
        }
    }

    public void gen_input(var_record p_i, int[] var_num) {
        if (Error.errorNum != 0) {
            return;
        }
        if (p_i == null) {
            return;
        }
        if (p_i.type.getTag() == Tag.KW_VOID) {
            Error.SemError.semerror(Error.SemError.void_nin);
            return;
        }
        this.out_code("\t;为%s产生输入代码\n", p_i.name);
        this.out_code("\tmov ecx,@buffer\n\tmov edx,255\n\tmov ebx,0\n\tmov eax,3\n\tint 128\n");
        this.out_code("\tcall @procBuf\n");
        if (p_i.type.getTag() == Tag.KW_STRING) {
            var_record gBuf = new var_record();
            String bname = "";
            bname += "@buffer";
            gBuf.init(new Token(Tag.KW_STRING), bname);
            gBuf.strValID = -2;
            convert_buffer = 1;
            gen_assign(p_i, gBuf, var_num);
            convert_buffer = 0;
        } else if (p_i.type.getTag() == Tag.KW_INT) {
            if (p_i.localAddr == 0) {
                this.out_code("\tmov [@var_%s],eax\n", p_i.name);
            } else {
                if (p_i.localAddr < 0) {
                    this.out_code("\tmov [ebp%d],eax\n", p_i.localAddr);
                } else {
                    this.out_code("\tmov [ebp+%d],eax\n", p_i.localAddr);
                }
            }
        } else {
            if (p_i.localAddr == 0) {
                this.out_code("\tmov [@var_%s],bl\n", p_i.name);
            } else {
                if (p_i.localAddr < 0) {
                    this.out_code("\tmov [ebp%d],bl\n", p_i.localAddr);
                } else {
                    this.out_code("\tmov [ebp+%d],bl\n", p_i.localAddr);
                }
            }
        }
    }

    public void gen_output(var_record p_o, int[] var_num) {
        if (Error.errorNum != 0) {
            return;
        }
        if (p_o == null) {
            return;
        }
        this.out_code("\t;为%s产生输出代码\n", p_o.name);
        var_record empStr = new var_record();
        String empname = "";
        empStr.init(new Token(Tag.KW_STRING), empname);
        p_o = gen_exp(empStr, Tag.TK_PLUS, p_o, var_num);
        this.out_code("\tmov ecx,[ebp%d]\n\tmov edx,0\n\tmov dl,[ecx]\n\tsub ecx,edx\n\tmov ebx,1\n\tmov eax,4\n\tint 128\n", p_o.localAddr);
    }

    public void out_code(String code) {
        try {
            fileOutputStream.write(code.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void out_code(String code, Object... args) {
        try {
            fileOutputStream.write(String.format(code, args).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
