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


    public static void main(String[] args) throws Exception {
        String in_file = args[0];

        InputSystem inputSystem = new InputSystem();
        File file = new File(in_file);
        if (!file.exists()) {
            System.out.println("源文件不存在\n");
            System.exit(-1);
        }

        inputSystem.readFromFile(file);
        Lexer lexer = new Lexer(file);
        Parser parser = new Parser(lexer);


        gener.set_out_file(new File("out.asm"));
        gener.out_code("section .text\n");
        gener.out_code("@str2long:\n\tmov edx,@str_2long_data_len\n\tmov ecx,@str_2long_data\n\tmov ebx, 1\n\tmov eax, 4\n\tint 128\n");
        gener.out_code("\tmov ebx, 0\n\tmov eax, 1\n\tint 128\n\tret\n");
        gener.out_code("@procBuf:\n");
        gener.out_code("\tmov esi,@buffer\n\tmov edi,0\n\tmov ecx,0\n\tmov eax,0\n\tmov ebx,10\n");
        gener.out_code("@cal_buf_len:\n");
        gener.out_code("\tmov cl,[esi+edi]\n\tcmp ecx,10\n\tje @cal_buf_len_exit\n");
        gener.out_code("\tinc edi\n\timul ebx\n\tadd eax,ecx\n\tsub eax,48\n\tjmp @cal_buf_len\n");
        gener.out_code("@cal_buf_len_exit:\n\tmov ecx,edi\n\tmov [@buffer_len],cl\n\tmov bl,[esi]\n");
        gener.out_code("\tret\n");
        gener.out_code("global _start\n_start:\n");
        gener.out_code("\tcall main\n");
        gener.out_code("\tmov ebx, 0\n\tmov eax, 1\n\tint 128\n");
        parser.parse();
        gener.out_code("\t@str_2long_data db \"字符串长度溢出！\",10,13\n\t@str_2long_data_len equ 26\n");
        gener.out_code("\t@buffer times 255 db 0\n\t@buffer_len db 0\n");
        gener.out_code("\t@s_esp dd @s_base\n\t@s_ebp dd 0\n");
        gener.out_code("\t@s_stack times 65536 db 0\n\t@s_base:\nsection .bss\n");
        gener.out_code("section .bss\n");
    }
}
