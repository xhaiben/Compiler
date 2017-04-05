package cacher;

/*
 * Created by xhaiben on 2017/2/12.
 */
public class Compiler {
    public static void main(String[] args) {
        BasicParser parser = new BasicParser(new Lexer());
        parser.statements();
//        ImprovedParser parser = new ImprovedParser(new Lexer());
//        parser.statements();

    }
}
