import cacher.lexer.InputSystem;
import cacher.lexer.Lexer;
import cacher.lexer.Tag;
import cacher.lexer.Token;

import java.io.EOFException;
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
        do {
            Token token = lexer.getToken();
            if(token==null||token.getTag()==Tag.TK_EOF){
                break;
            }
            System.out.println(token.getTag());
        } while (true);
//        for (Token token : lexer.getTokenList()) {
//            System.out.println(token.getTag());
//        }
//        Parser parser = new Parser(lexer.getTokenList());
//        parser.parse();

    }
}
