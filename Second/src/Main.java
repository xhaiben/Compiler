import cacher.lexer.InputSystem;
import cacher.lexer.Lexer;
import cacher.lexer.Token;

import java.io.File;

/*
 * Created by xhaiben on 2017/3/18.
 */

public class Main {
    public static void main(String[] args) throws Exception {
        InputSystem inputSystem = new InputSystem();
        File file = new File("D:\\IdeaProjects\\Compiler\\test2.c");
        inputSystem.readFromFile(file);
        Lexer lexer = new Lexer(inputSystem);
        lexer.lex();
        for (Token token : lexer.getTokenList()) {
            System.out.println(token.getTag());
        }
//        Parser parser = new Parser(lexer.getTokenList());
//        parser.parse();
    }
}
