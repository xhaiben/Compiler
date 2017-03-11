package cacher.lexer;

/*
 * Created by xhaiben on 2017/3/11.
 */
public class Id extends Token {
    private String name;

    public Id(String name) {
        super(Tag.TK_IDENT);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Id{" +
                "name='" + name + '\'' +
                '}';
    }
}
