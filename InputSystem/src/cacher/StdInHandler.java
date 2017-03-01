package cacher;

import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * Created by xhaiben on 2017/2/23.
 */
public class StdInHandler implements FileHandler {
    private String input_buffer = "";
    private int curPos = 0;

    @Override
    public void Open() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String line = scanner.nextLine();
            if (line.equals("end")) {
                break;
            }
            input_buffer += line;
        }
        scanner.close();
    }

    @Override
    public int Close() {
        return 0;
    }

    @Override
    public int Read(byte[] buf, int begin, int len) {
        if (curPos >= input_buffer.length()) {
            return 0;
        }
        int readCnt = 0;
        try {
            byte[] inputBuf = input_buffer.getBytes("UTF-8");
            while (curPos + readCnt < input_buffer.length() && readCnt < len) {
                buf[begin + readCnt] = inputBuf[curPos + readCnt];
                readCnt++;
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return readCnt;
    }
}
