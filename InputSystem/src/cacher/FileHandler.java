package cacher;

/**
 * Created by xhaiben on 2017/2/23.
 */
public interface FileHandler {
    void Open();

    int Close();

    int Read(byte[] buf, int begin, int len);
}
