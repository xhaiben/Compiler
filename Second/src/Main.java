import cacher.lexer.InputSystem;
import cacher.lexer.Lexer;
import cacher.lexer.Token;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Scanner;

/*
 * Created by xhaiben on 2017/3/18.
 */

public class Main {
    public static void main(String[] args) throws Exception {
        InputSystem inputSystem = new InputSystem();
        inputSystem.readFromFile();
        Lexer lexer = new Lexer(inputSystem);
        lexer.lex();
        for (Token token : lexer.getTokenList()) {
            System.out.println(token.getTag());
        }

    }
}
