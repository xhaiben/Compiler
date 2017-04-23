package cacher.semantic;

import cacher.Utils.BeanUtil;
import cacher.error.Error;
import cacher.lexer.Tag;

import java.util.HashMap;
import java.util.LinkedList;

/*
 * Created by xhaiben on 17-4-23.
 */
public class Table {
    public HashMap<String, var_record> var_map;
    public HashMap<String, fun_record> fun_map;
    public LinkedList<String> stringTable;
    public LinkedList<var_record> real_args_list;

    public Table() {
        var_map = new HashMap<>();
        fun_map = new HashMap<>();
        stringTable = new LinkedList<>();
        real_args_list = new LinkedList<>();
    }

    public int add_string() {

    }

    public String get_string(int index) {

    }

    public void add_var() {

    }

    public void add_var(var_record v_r) {

    }

    public var_record get_var(String name) {

    }

    public int has_name(String id_name) {

    }

    public void del_var(String var_name) {

    }

    public void add_fun() {

    }

    public void add_real_arg(var_record arg, int[] var_num) {
        if (Error.synerr != 0) {
            return;
        }
        if (arg.type.getTag() == Tag.KW_STRING) {
            var_record emp_str = BeanUtil.cloneTo(arg);
            arg = semantic.gener.gen_exp(emp_str, Tag.TK_PLUS, arg, var_num);
        }
        real_args_list.add(arg);
    }

    public var_record gen_Call(String fname, int[] var_num) {

    }

    public void over() {

    }

    public void clear() {

    }
}