package cacher.lexer;

import java.util.Scanner;

/*
 * Created by xhaiben on 2017/3/18.
 */
public class InputSystem {
    private StringBuilder input_content;
    private int ch_point;

    public int getLine_no() {
        return line_no;
    }

    private int line_no;

    public InputSystem() {
        this.input_content = new StringBuilder();
    }

    public void readFromConsole() {
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.equals("end")) {
                break;
            }
            input_content.append(line);
        }
        scanner.close();
//        System.out.println(input_content);
    }

    public boolean hasNext() {
        return ch_point <= input_content.length() - 1;
    }

    public char nextChar() {
        if (ch_point < input_content.length()) {
            char ch = input_content.charAt(ch_point++);
            if (ch == '\n') {
                this.line_no++;
            }
            return ch;
        } else {
            throw new StringIndexOutOfBoundsException(ch_point);
        }
    }

    public boolean nextChar(char need) {
        if (ch_point < input_content.length()) {
            char ch = input_content.charAt(ch_point);
            if (ch == '\n') {
                this.line_no++;
            }
            if (ch == need) {
                ch_point++;
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
