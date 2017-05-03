import cacher.generator.Generator;
import cacher.lexer.InputSystem;
import cacher.lexer.Lexer;
import cacher.parser.Parser;

import java.io.File;
import java.io.FileOutputStream;

/*
 * Created by xhaiben on 2017/3/18.
 */

public class Main {
    private static Generator gener = Generator.getInstance();

    public static void genCommonFile() {
        File file = new File("common.as");
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write("section .text\n".getBytes());
            fileOutputStream.write("@str2long:\n\tmov edx,@str_2long_data_len\n\tmov ecx,@str_2long_data\n\tmov ebx, 1\n\tmov eax, 4\n\tint 128\n".getBytes());
            fileOutputStream.write("\tmov ebx, 0\n\tmov eax, 1\n\tint 128\n\tret\n".getBytes());
            fileOutputStream.write("@procBuf:\n".getBytes());
            fileOutputStream.write("\tmov esi,@buffer\n\tmov edi,0\n\tmov ecx,0\n\tmov eax,0\n\tmov ebx,10\n".getBytes());
            fileOutputStream.write("@cal_buf_len:\n".getBytes());
            fileOutputStream.write("\tmov cl,[esi+edi]\n\tcmp ecx,10\n\tje @cal_buf_len_exit\n".getBytes());
            fileOutputStream.write("\tinc edi\n\timul ebx\n\tadd eax,ecx\n\tsub eax,48\n\tjmp @cal_buf_len\n".getBytes());
            fileOutputStream.write("@cal_buf_len_exit:\n\tmov ecx,edi\n\tmov [@buffer_len],cl\n\tmov bl,[esi]\n".getBytes());
            fileOutputStream.write("\tret\n".getBytes());
            fileOutputStream.write("global _start\n_start:\n".getBytes());
            fileOutputStream.write("\tcall main\n".getBytes());
            fileOutputStream.write("\tmov ebx, 0\n\tmov eax, 1\n\tint 128\n".getBytes());
            fileOutputStream.write("section .data\n\t@str_2long_data db \"字符串长度溢出！\",10,13\n\t@str_2long_data_len equ 26\n".getBytes());
            fileOutputStream.write("\t@buffer times 255 db 0\n\t@buffer_len db 0\n".getBytes());
            fileOutputStream.write("\t@s_esp dd @s_base\n\t@s_ebp dd 0\n".getBytes());
            fileOutputStream.write("\t@s_stack times 65536 db 0\nsection .bss\n@s_base:\n".getBytes());
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) throws Exception {
        InputSystem inputSystem = new InputSystem();
        File file = new File("D:\\IdeaProjects\\Compiler\\test2.c");
        inputSystem.readFromFile(file);
        Lexer lexer = new Lexer(file);
        Parser parser = new Parser(lexer);

        genCommonFile();

        gener.set_out_file(new File("out.as"));
        gener.out_code("section .text\n");
        parser.parse();
        gener.out_code("section .bss\n");
    }
}
