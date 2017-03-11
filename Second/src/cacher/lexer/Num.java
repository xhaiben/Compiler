package cacher.lexer;

/*
 * Created by xhaiben on 2017/3/11.
 */
public class Num extends Token{
    private int val;

    public Num(int val) {
        super(Tag.TK_C_NUM);
        this.val = val;
    }

    public int getVal() {
        return val;
    }

    @Override
    public String toString() {
        return "Num{" +
                "val=" + val +
                '}';
    }
}
