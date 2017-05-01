package cacher.lexer;

/*
 * Created by xhaiben on 2017/3/11.
 */

import java.io.Serializable;

public class Token implements Serializable {
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
