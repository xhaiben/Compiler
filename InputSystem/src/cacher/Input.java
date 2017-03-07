package cacher;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/*
 * Created by xhaiben on 2017/2/23.
 */
public class Input {
    public static final int EOF = 0;//输入流中没有可以读取的信息
    private final int MAX_LOOK = 16;//look ahead 最多字符数
    private final int MAX_LEX = 1024;//分词后字符串的最大长度
    private final int BUF_SIZE = (MAX_LEX * 3) + (2 * MAX_LOOK);//缓冲区大小
    private int END_BUF = BUF_SIZE;//缓冲区的逻辑结束地址
    private final int DANGER = (END_BUF - MAX_LOOK);
    private final int END = BUF_SIZE;
    private final byte[] START_BUF = new byte[BUF_SIZE]; //缓冲区
    private int NEXT = END;//指向当前要读入的字符位置
    private int sMark = END;//当前被词法分析器分析的字符串位置
    private int eMark = END;//当前被词法分析器分析的字符串结束位置
    private int pMark = END;//上一个被词法分析器分析的字符串起始位置
    private int pLineno = 0;//上一个被词法分析器分析的字符串所在的行号
    private int pLength = 0;//上一个被词法分析器分析的字符串长度

    private FileHandler fileHandler = null;

    private int Lineno = 1;//当前被词法分析器分析的字符串的行号

    private int Mline = 1;

    private boolean EOF_READ = false;//输入流中是否还有可读信息

    private boolean noMoreChars() {
        return (EOF_READ && NEXT >= END_BUF);
    }

    public void ii_newFile(String fileName) {
        if (fileHandler != null) {
            fileHandler.Close();
        }
        fileHandler = getFileHandler(fileName);
        fileHandler.Open();

        EOF_READ = false;
        NEXT = END;
        sMark = END;
        eMark = END;
        END_BUF = END;
        Lineno = 1;
        Mline = 1;
    }

    public String ii_text() {
        byte[] str = Arrays.copyOfRange(START_BUF, sMark, sMark + ii_length());
        return new String(str, StandardCharsets.UTF_8);
    }

    public int ii_length() {
        return eMark - sMark;
    }

    public int ii_lineno() {
        return Lineno;
    }

    public String ii_ptext() {
        byte[] str = Arrays.copyOfRange(START_BUF, pMark, pMark + pLength);
        return new String(str, StandardCharsets.UTF_8);
    }

    public int ii_plength() {
        return pLength;
    }

    public int ii_plineno() {
        return pLineno;
    }

    public int ii_mark_start() {
        Mline = Lineno;
        eMark = sMark = NEXT;
        return sMark;
    }

    public int ii_mark_end() {
        Mline = Lineno;
        eMark = NEXT;
        return eMark;
    }

    public int ii_move_start() {
        if (sMark >= eMark) {
            return -1;
        } else {
            return sMark++;
        }
    }

    public int ii_to_mark() {
        Lineno = Mline;
        NEXT = eMark;
        return NEXT;
    }

    public int ii_mark_prev() {
        /*
        执行这个函数后，上一个被词法解析器解析的字符串将无法再缓冲区中找到
         */
        pMark = sMark;
        pLineno = Lineno;
        pLength = eMark - sMark;
        return pMark;
    }

    public byte ii_advance() {
        /*
        ii_advance() 是真正的获取输入的函数，它将数据从输入流中读入缓冲区，并从缓冲区中返回要读取的
        字符，并将NEXT加一，从而指向下一个要读取的字符，
        如果NEXT的位置距离缓冲区的逻辑末尾（END_BUF）不到MAXLOOK时，将会对缓冲区进行一个FLUSH操作。
         */
        if (noMoreChars()) {
            return 0;
        }
        if (!EOF_READ && ii_flush(false) < 0) {
            /*
            从输入流读入数据到缓冲区出错
             */
            return -1;
        }
        if (START_BUF[NEXT] == '\n') {
            Lineno++;
        }
        return START_BUF[NEXT++];
    }

    public static int NO_MORE_CHARS_TO_READ = 0;
    public static int FLUSH_OK = 1;
    public static int FLUSH_FAIL = -1;

    private int ii_flush(boolean force) {
        /*
         FLUSH 缓冲区，如果NEXT 没有越过Danger的话，就什么都不做
         要不然像上一节所说的一样将数据进行平移，并从输入流中读入数据，写入平移后所产生的空间
         *                            pMark                     DANGER
		 *                              |                          |
		 *     Start_buf              sMark         eMark          | Next  End_buf
		 *         |                    | |           |            |  |      |
		 *         V                    V V           V            V  V      V
		 *         +---------------------------------------------------------+---------+
		 *         | 已经读取的区域       |          未读取的区域              | 浪费的区域|
		 *         +--------------------------------------------------------------------
		 *         |<---shift_amt------>|<-----------copy_amt--------------->|
		 *         |<-------------------------BUFSIZE---------------------------------->|
		 *
		 * 未读取区域的左边界是pMark或sMark(两者较小的那个)，把未读取区域平移到最左边覆盖
		 * 已经读取区域，返回1
		 * 如果FLUSH操作成功，-1
		 * 如果操作失败，0
		 * 如果输入流中已经没有可以读取的多余字符。
		 * 如果force为true，那么不管NEXT有没有越过Danger，都会引发FLUSH操作
        */
        int copy_amt, shift_amt, left_edge;
        if (noMoreChars()) {
            return NO_MORE_CHARS_TO_READ;
        }
        if (EOF_READ) {
            return FLUSH_OK;
        }
        if (NEXT > DANGER || force) {
            left_edge = pMark < sMark ? pMark : sMark;
            shift_amt = left_edge;
            if (shift_amt < MAX_LEX) {
                if (!force) {
                    return FLUSH_FAIL;
                }
                left_edge = ii_mark_start();
                ii_mark_prev();
                shift_amt = left_edge;
            }
            copy_amt = END_BUF - left_edge;
            System.arraycopy(START_BUF, 0, START_BUF, left_edge, copy_amt);
            if (ii_fillbuf(copy_amt) == 0) {
                System.err.println("Internal Error,ii_flush: Buffer full, can't read");
            }
            if (pMark != 0) {
                pMark -= shift_amt;
            }
            sMark -= shift_amt;
            eMark -= shift_amt;
            NEXT -= shift_amt;
        }
        return FLUSH_OK;
    }

    private int ii_fillbuf(int starting_at) {
        /*
        从输入流中读取信息，填充缓冲区平移后的可用空间，可用空间的长度是从Starting_at一直到end_buf
        每次从输入流中读取的数据长度是MAX_LEX的整数倍
         */
        int need;
        int got = 0;
        need = ((END - starting_at) / MAX_LEX) * MAX_LEX;
        if (need < 0) {
            System.err.println("Internal Error (ii_fillbuf): Bad read-request starting addr.");
        }
        if (need == 0) {
            return 0;
        }
        if ((got = fileHandler.Read(START_BUF, starting_at, need)) == -1) {
            System.err.println("Can't read input file");
        }
        END_BUF = starting_at + got;
        if (got > need) {
            //输入流已经到末尾
            EOF_READ = true;
        }
        return got;
    }

    public boolean ii_pushback(int n) {
        /*
        把预读取的若干字符退回缓冲区
         */
        while (--n >= 0 && NEXT > sMark) {
            if (START_BUF[--NEXT] == '\n' || START_BUF[NEXT] == '\0') {
                --Lineno;
            }
        }
        if (NEXT < eMark) {
            eMark = NEXT;
            Mline = Lineno;
        }
        return (NEXT > sMark);
    }

    public byte ii_lookahead(int n) {
        /*
        预读取若干个字符
         */
        byte p = START_BUF[NEXT + n - 1];
        if (EOF_READ && NEXT + n - 1 >= END_BUF) {
            return EOF;
        }
        return (NEXT + n - 1 < 0 || NEXT + n - 1 >= END_BUF) ? 0 : p;
    }

    private FileHandler getFileHandler(String fileName) {
        if (fileName != null) {
            return new DiskFileHandler(fileName);
        } else {
            return new StdInHandler();
        }
    }
}
