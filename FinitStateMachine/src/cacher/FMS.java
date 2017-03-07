package cacher;

/*
 * Created by xhaiben on 2017/3/7.
 */
public interface FMS {
    final int STATE_FAILURE = -1;

    int yy_next(int state, byte c);

    boolean isAcceptState(int state);
}
