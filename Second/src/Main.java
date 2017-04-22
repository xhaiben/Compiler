import cacher.lexer.InputSystem;
import cacher.lexer.Lexer;
import cacher.lexer.Tag;
import cacher.lexer.Token;
import cacher.parser.Parser;

import java.io.File;

/*
 * Created by xhaiben on 2017/3/18.
 */

public class Main {
    public static void main(String[] args) throws Exception {
        InputSystem inputSystem = new InputSystem();
        File file = new File("D:\\IdeaProjects\\Compiler\\test2.c");
        inputSystem.readFromFile(file);
        Lexer lexer = new Lexer(file);
//        while (true) {
//            Token token = lexer.lex();
//            System.out.println(token.getTag());
//            if (token.getTag() == Tag.TK_EOF) {
//                break;
//            }
//        }
        Parser parser = new Parser(lexer);
        parser.parse();
    }
}
