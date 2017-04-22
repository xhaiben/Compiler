package cacher.lexer;

/*
 * Created by xhaiben on 2017/3/11.
 */

public class Token {
    private Tag tag;

    public Token(Tag tag) {
        this.tag = tag;
    }

    public Tag getTag() {
        return tag;
    }

    @Override
    public String toString() {
        return "Token{" +
                "tag=" + tag +
                '}';
    }
}
