import cacher.generator.generator;
import cacher.lexer.InputSystem;
import cacher.lexer.Lexer;
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
        Parser parser = new Parser(lexer);
        generator gener = generator.getInstance();
        gener.set_out_file(new File("out.as"));
        parser.parse();
    }
}
