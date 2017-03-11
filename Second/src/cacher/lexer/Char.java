package cacher.lexer;

/*
 * Created by xhaiben on 2017/3/11.
 */
public class Char extends Token{
    private char ch;

    public Char(char ch) {
        super(Tag.TK_C_CHAR);
        this.ch = ch;
    }

    public char getCh() {
        return ch;
    }

    @Override
    public String toString() {
        return "Char{" +
                "aChar=" + ch +
                '}';
    }
}
