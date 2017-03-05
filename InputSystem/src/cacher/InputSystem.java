package cacher;

/*
 * Created by xhaiben on 2017/3/5.
 */
public class InputSystem {
    private Input input = new Input();

    public static void main(String[] args) {
        InputSystem inputSystem = new InputSystem();
        inputSystem.runStdinExample();
    }

    public void runStdinExample() {
        input.ii_newFile(null); //控制台输入
        input.ii_mark_start();
        printWord();
        input.ii_mark_end();
        input.ii_mark_prev();
        /*
         *   执行上面语句后，缓冲区及相关指针情况如下图
    	 *       sMark
    	 *         |
    	 *       pMark     eMark
		 *         |        |
    	 *       Start_buf Next                                   Danger   End_buf
		 *         |        |                                        |       |
		 *         V        V                                        V       V
		 *         +---------------------------------------------------------+---------+
		 *         | typedef|          未读取的区域                     |       | 浪费的区域|
		 *         +--------------------------------------------------------------------
		 *         |<-------------------------BUFSIZE---------------------------------->|
    	 *
    	 */
        input.ii_mark_start();
        printWord();
        input.ii_mark_end();
        /*
         *   执行上面语句后，缓冲区及相关指针情况如下图
    	 *                 sMark
    	 *                  |
    	 *       pMark      |   eMark
		 *         |        |    |
    	 *       Start_buf  |   Next                               Danger   End_buf
		 *         |        |    |                                   |       |
		 *         V        V    V                                   V       V
		 *         +---------------------------------------------------------+---------+
		 *         | typedef|int|      未读取区域                      |       | 浪费的区域|
		 *         +--------------------------------------------------------------------
		 *         |<-------------------------BUFSIZE---------------------------------->|
    	 *
    	 */
        System.out.println("prev word: " + input.ii_ptext());//打印typedef
        System.out.println("current word: " + input.ii_text());//打印当前字符串
    }

    private void printWord() {
        byte c;
        while ((c = input.ii_advance()) != ' ') {
            byte[] buf = new byte[1];
            buf[0] = c;
            try {
                String s = new String(buf, "UTF-8");
                System.out.print(s);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("");
    }

}
