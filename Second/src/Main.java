import cacher.lexer.InputSystem;
import cacher.lexer.Lexer;
import cacher.lexer.Token;

/*
 * Created by xhaiben on 2017/3/18.
 */

public class Main {
    public static void main(String[] args) {
        InputSystem inputSystem = new InputSystem();
        inputSystem.readFromConsole();
        Lexer lexer = new Lexer(inputSystem);
        lexer.lex();
        for (Token token : lexer.getTokenList()) {
            System.out.println(token.getTag());
        }
    }
}
