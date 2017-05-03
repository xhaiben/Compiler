package cacher.semantic;

import cacher.Utils.BeanUtil;
import cacher.error.Error;
import cacher.generator.Generator;
import cacher.lexer.Tag;
import cacher.lexer.Token;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static cacher.semantic.Semantic.tfun;
import static cacher.semantic.Semantic.tvar;

/*
 * Created by xhaiben on 17-4-23.
 */
public class Table {
    public HashMap<String, Var_record> var_map;
    public HashMap<String, Fun_record> fun_map;
    public LinkedList<String> stringTable;
    public LinkedList<Var_record> real_args_list;

    private static Table table = null;

    private Generator gener;

    static {
        if (table == null) {
            table = new Table();
        }
    }

    public static Table getInstance() {
        return Table.table;
    }

    private Table() {
        var_map = new HashMap<>();
        fun_map = new HashMap<>();
        stringTable = new LinkedList<>();
        real_args_list = new LinkedList<>();
        gener = Generator.getInstance();
    }

    private int stringId = 0;

    public int add_string(String str) {
        if (Error.synerr != 0) {
            return 0;
        }
        stringId++;
        String ps = "";
        ps += str;
        stringTable.addLast(ps);
        return stringId;
    }

    public String get_string(int index) {
        if (Error.synerr != 0) {
            return "";
        }
        if (index > 0 && index <= stringTable.size()) {
            return stringTable.get(index - 1);
        } else {
            return "";
        }
    }

    public void add_var() {
        if (Error.synerr != 0) {
            return;
        }
        if (!var_map.containsKey(tvar.name)) {
            Var_record p_var = BeanUtil.cloneTo(tvar);
            var_map.put(tvar.name, p_var);
        } else {
            var_map.put(tvar.name, tvar);
            Error.SemError.semerror(Error.SemError.var_redef);
        }
    }

    public void add_var(Var_record v_r) {
        if (Error.synerr != 0) {
            return;
        }
        if (!var_map.containsKey(v_r.name)) {
            var_map.put(v_r.name, v_r);
        } else {
            Error.SemError.semerror(Error.SemError.var_redef);
        }
    }

    public Var_record get_var(String name) {
        if (Error.synerr != 0) {
            return null;
        }
        if (table.has_name(name)) {
            return var_map.get(name);
        } else {
            Error.SemError.semerror(Error.SemError.var_undec);
            return null;
        }
    }

    public boolean has_name(String id_name) {
        if (Error.synerr != 0) {
            return false;
        }
        return var_map.containsKey(id_name) || tfun.hasname(id_name) != 0;
    }

    public void del_var(String var_name) {
        if (Error.synerr != 0) {
            return;
        }
        if (var_map.containsKey(var_name)) {
            var_map.remove(var_name);
        } else {

        }
    }

    public void add_fun() {
        if (Error.synerr != 0) {
            return;
        }
        if (!fun_map.containsKey(tfun.name)) {
            Fun_record p_fun = BeanUtil.cloneTo(tfun);
            fun_map.put(tfun.name, p_fun);

            if (p_fun.defined == 1) {
                tfun.flush_args();
                gener.gen_fun_head();
            }
        } else {
            Fun_record p_fun = fun_map.get(tfun.name);
            if (p_fun.equals(tfun)) {
                if (tfun.defined == 1) {
                    if (p_fun.defined == 1) {
                        Error.SemError.semerror(Error.SemError.fun_redef);
                        tfun.flush_args();
                    } else {
                        p_fun.defined = 1;
                        tfun.flush_args();
                        gener.gen_fun_head();
                    }
                }
                return;
            } else {
                Fun_record pfun = BeanUtil.cloneTo(tfun);
                fun_map.remove(tfun.name);
                fun_map.put(tfun.name, pfun);
                if (tfun.defined == 1) {
                    Error.SemError.semerror(Error.SemError.fun_def_err);
                    tfun.flush_args();
                } else {
                    Error.SemError.semerror(Error.SemError.fun_dec_err);
                }
            }
        }
    }

    public void add_real_arg(Var_record arg, int[] var_num) {
        if (Error.synerr != 0) {
            return;
        }
        if (arg.type.getTag() == Tag.KW_STRING) {
            Var_record emp_str = BeanUtil.cloneTo(arg);
            arg = Semantic.gener.gen_exp(emp_str, Tag.TK_PLUS, arg, var_num);
        }
        real_args_list.add(arg);
    }

    public Var_record gen_Call(String fname, int[] var_num) {
        Var_record rec = null;
        if (Error.errorNum != 0) {
            return null;
        }
        if (fun_map.containsKey(fname)) {
            Fun_record p_fun = fun_map.get(fname);
            if (real_args_list.size() >= p_fun.args.size()) {
                int l = real_args_list.size();
                int m = p_fun.args.size();
                for (int i = l - 1, j = m - 1; j >= 0; i--, j--) {
                    if (real_args_list.get(i).type.getTag() != p_fun.args.get(j)) {
                        Error.SemError.semerror(Error.SemError.real_args_err);
                        break;
                    } else {
                        Var_record ret = real_args_list.get(i);
                        if (Error.semerr != 0) {
                            break;
                        }
                        if (ret.type.getTag() == Tag.KW_STRING) {
                            gener.out_code("\tmov eax,[ebp%d]\n", ret.localAddr);
                        } else {
                            if (ret.localAddr == 0) {
                                gener.out_code("\tmov eax,[@var_%s]\n", ret.name);
                            } else {
                                if (ret.localAddr < 0) {
                                    gener.out_code("\tmov eax,[ebp%d]\n", ret.localAddr);
                                } else {
                                    gener.out_code("\tmov eax,[ebp+%d]\n", ret.localAddr);
                                }
                            }
                        }
                        gener.out_code("\tpush eax\n");
                    }
                }
                gener.out_code("\tcall %s\n", fname);
                gener.out_code("\tadd esp,%d\n", 4 * l);
                if (p_fun.type == Tag.KW_VOID) {
                    rec = tfun.create_tmp_var(new Token(p_fun.type), 0, var_num);
                    gener.out_code("\tmov [ebp%d],eax\n", rec.localAddr);
                    if (p_fun.type == Tag.KW_STRING) {
                        Var_record empStr = new Var_record();
                        String empname = "";
                        empStr.init(new Token(Tag.KW_STRING), empname);
                        rec = gener.gen_exp(empStr, Tag.TK_PLUS, rec, var_num);
                    }
                }
                while ((m--) != 0) {
                    real_args_list.removeLast();
                }
            } else {
                Error.SemError.semerror(Error.SemError.real_args_err);
            }
        } else {
            Error.SemError.semerror(Error.SemError.fun_undec);
        }
        return rec;
    }

    public void over() {
        if (Error.errorNum != 0) {
            return;
        }
        gener.out_code("section .data\n");
        for (Map.Entry<String, Var_record> recordEntry : var_map.entrySet()) {
            Var_record p_v = recordEntry.getValue();
            int isEx = 0;
            if (p_v.externed != 0) {
                isEx = 1;
            } else {
                if (p_v.type.getTag() == Tag.KW_STRING) {
                    gener.out_code("\t%s times 255 db %d\n", gener.gen_name("str", null, p_v.name), isEx);
                    gener.out_code("\t%s_len db %d\n", gener.gen_name("str", null, p_v.name), isEx);
                } else {
                    gener.out_code("\t%s dd %d\n", gener.gen_name("var", null, p_v.name), isEx);
                }
            }
        }
        String strBuf;
        int l;
        for (int i = 0; i < stringTable.size(); i++) {
            strBuf = stringTable.get(i);
            l = stringTable.get(i).length();
            gener.out_code("\t@str_%d db ", i + 1);
            int chpass = 0;
            for (int j = 0; j < l; j++) {
                if (strBuf.charAt(j) == 10 || strBuf.charAt(j) == 9 || strBuf.charAt(j) == '\"') {
                    if (chpass == 0) {
                        if (j != 0) {
                            gener.out_code(",");
                        }
                        gener.out_code("%d", strBuf.charAt(j));
                    } else {
                        gener.out_code("\",%d", strBuf.charAt(j));
                    }
                    chpass = 0;
                } else {
                    if (chpass == 0) {
                        if (j != 0) {
                            gener.out_code(",");
                        }
                        gener.out_code("%c", strBuf.charAt(j));
                        if (j == l - 1) {
                            gener.out_code("\"");
                        }
                    } else {
                        gener.out_code("%c", strBuf.charAt(j));
                        if (j == l - 1) {
                            gener.out_code("\"");
                        }
                    }
                    chpass = 1;
                }
            }
            if (l == 0) {
                gener.out_code("\"\"");
            }
            gener.out_code("\n");
            gener.out_code("\t@str_%d_len equ %d\n", i + 1, stringTable.get(i).length());
        }
    }

    public void clear() {
        var_map.clear();
        fun_map.clear();
        stringTable.clear();
        real_args_list.clear();
    }
}