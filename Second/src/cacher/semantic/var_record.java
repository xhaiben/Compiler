package cacher.semantic;

import cacher.error.Error;
import cacher.lexer.Tag;
import cacher.lexer.Token;

import java.io.Serializable;

/*
 * Created by xhaiben on 17-4-23.
 */
public class var_record implements Serializable {
    public Token type; //变量类型
    public String name;

    public int intVal;
    public char charVal;
    public int voidVal;
    public int strValID;

    public int localAddr;
    public int externed;

    public var_record() {
        this.type = null;
        this.name = "";

        this.intVal = 0;
        this.charVal = 0;
        this.voidVal = 0;
        this.strValID = 0;

        this.localAddr = 0;
        this.externed = 0;
    }


    public void init(Token dec_type, String dec_name) {
        if (Error.synerr != 0) {
            return;
        }
        this.type = dec_type;
        this.name = dec_name;
        this.intVal = 0;
        this.charVal = 0;
        this.voidVal = 0;
        this.strValID = 0;
        this.localAddr = 0;
        this.externed = 0;
    }
}
