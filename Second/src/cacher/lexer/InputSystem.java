package cacher.lexer;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Scanner;

/*
 * Created by xhaiben on 2017/3/18.
 */
public class InputSystem {
    private StringBuilder input_content;
    private int ch_point;    private int compileOK = 0;


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

    public void readFromFile(File file) {
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
            Scanner scanner = new Scanner(inputStreamReader);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                input_content.append(line);
                if(scanner.hasNext()){
                    input_content.append('\n');
                }
            }
            scanner.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
