package cacher.lexer;

/*
 * Created by xhaiben on 2017/3/11.
 */
public class Str extends Token{
    private String str;

    public String getStr() {
        return str;
    }

    public Str(String str) {
        super(Tag.TK_C_STR);
        this.str = str;
    }

    @Override
    public String toString() {
        return "Str{" +
                "str='" + str + '\'' +
                '}';
    }
}
