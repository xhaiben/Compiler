package cacher;

/**
 * Created by xhaiben on 2017/2/12.
 */
public class Compiler {
    public static void main(String[] args) {
        Lexer lexer = new Lexer();
        BasicParser basicParser = new BasicParser(lexer);
        basicParser.statements();
    }
}
