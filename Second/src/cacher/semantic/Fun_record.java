package cacher.semantic;

import cacher.Utils.BeanUtil;
import cacher.error.Error;
import cacher.lexer.*;

import java.io.Serializable;
import java.util.LinkedList;

/*
 * Created by xhaiben on 17-4-23.
 */
public class Fun_record implements Serializable {
    public Tag type;//返回值类型
    public String name;//函数名
    public LinkedList<Tag> args; //参数列表
    public LinkedList<Var_record> local_vars;//局部变量表
    public int defined;//函数是否已有定义
    public int flushed;
    public int had_ret;//函数是否有 return 语句

    public Fun_record() {
        this.type = null;
        this.name = "";
        this.args = new LinkedList<>();
        this.args.clear();
        this.local_vars = new LinkedList<>();
        this.defined = 0;
        this.flushed = 0;
        this.had_ret = 0;
    }

    public void init(Tag dec_type, String dec_name) {
        if (Error.synerr != 0) {
            return;
        }
        this.type = dec_type;
        this.name = dec_name;
        this.args.clear();
        this.local_vars.clear();
        this.defined = 0;
        this.flushed = 0;
        this.had_ret = 0;
    }

    public void addarg() {
        if (Error.synerr != 0) {
            return;
        }
        this.args.add(Semantic.tvar.type.getTag());
        this.push_local_val();
    }

    public int hasname(String id_name) {
        if (Error.synerr != 0) {
            return -1;
        }
        for (Var_record var : this.local_vars) {
            if (var.name.equals(id_name)) {
                return 1;
            }
        }
        return 0;
    }

    public void push_local_val() {
        if (Error.synerr != 0) {
            return;
        }
        if (this.defined == 0) {
            this.local_vars.add(BeanUtil.cloneTo(Semantic.tvar));
        } else {
            Var_record rec = BeanUtil.cloneTo(Semantic.tvar);
            this.local_vars.addLast(rec);
            Semantic.table.add_var(rec);
            int args_len = this.args.size();
            int local_var_len = this.local_vars.size();
            rec.localAddr = -4 * (local_var_len - args_len);
            Semantic.gener.gen_local_var(0);
        }
    }

    public int getCurAddr() {
        int args_len = this.args.size();
        int local_var_len = this.local_vars.size();
        return -4 * (local_var_len - args_len);
    }

    public void flush_args() {
        if (Error.synerr != 0) {
            return;
        }
        int args_len = this.args.size();
        for (int i = args_len - 1; i >= 0; i--) {
            Var_record rec = this.local_vars.get(i);
            rec.localAddr = 4 * (i + 2);
            if (rec.type.getTag() == Tag.KW_STRING) {
                rec.strValID = -1;
            }
            Semantic.table.add_var(rec);
        }
        flushed = 1;
    }

    public void pop_local_vars(int num) {
        if (Error.synerr != 0) {
            return;
        }
        for (int i = 0; i < num; i++) {
            Semantic.table.del_var(this.local_vars.getLast().name);
            this.local_vars.removeLast();
        }
        if (num == -1) {
            int args_len = this.args.size();
            int local_var_len = this.local_vars.size();
            for (int i = 0; i < args_len; i++) {
                Semantic.table.del_var(this.local_vars.get(i).name);
            }
            this.local_vars.clear();
        }
    }

    // val_num  1个长度的数组
    public Var_record create_tmp_var(Token type, int has_val, int[] var_num) {
        if (Error.synerr != 0) {
            return null;
        }
        Var_record p_temp_var = new Var_record();
        switch (type.getTag()) {
            case KW_INT:
                if (has_val != 0) {
                    p_temp_var.intVal = Lexer.num;
                }
                break;
            case KW_CHAR:
                if (has_val != 0) {
                    p_temp_var.charVal = Lexer.aChar;
                }
                break;
            case KW_STRING:
                if (has_val != 0) {
                    p_temp_var.strValID = Semantic.table.add_string(Lexer.string);
                } else {
                    p_temp_var.strValID = -1;
                }
                break;
        }
        p_temp_var.name = Semantic.gener.gen_name("tmp", type.getTag(), "");
        p_temp_var.type = type;
        var_num[0]++;
        this.local_vars.add(p_temp_var);
        Semantic.table.add_var(p_temp_var);
        int args_len = this.args.size();
        int local_var_len = this.local_vars.size();
        p_temp_var.localAddr = -4 * (local_var_len - args_len);

//        Semantic.gener.gen_local_var(Math.max(p_temp_var.intVal, Math.max(p_temp_var.charVal, Math.max(p_temp_var.voidVal, p_temp_var.strValID))));
        switch (type.getTag()) {
            case KW_INT:
                Semantic.gener.gen_local_var(p_temp_var.intVal);
                break;
            case KW_CHAR:
                Semantic.gener.gen_local_var(p_temp_var.charVal);
                break;
            case KW_STRING:
                Semantic.gener.gen_local_var(p_temp_var.strValID);
                break;
        }
        return p_temp_var;
    }

    public boolean equals(Fun_record fun_record) {
        if (Error.synerr != 0) {
            return false;
        }
        boolean ret = true;
        if (args.size() == fun_record.args.size()) {
            for (int i = 0; i < args.size(); i++) {
                if (args.get(i) != fun_record.args.get(i)) {
                    ret = false;
                    break;
                }
            }
        } else {
            ret = false;
        }
        return type == fun_record.type && name.equals(fun_record.name) && ret;
    }
}