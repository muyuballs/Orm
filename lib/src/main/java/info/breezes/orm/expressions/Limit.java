package info.breezes.orm.expressions;

/**
 * Created by Qiao on 2014/10/20.
 */
public class Limit {
    public int start;
    public int count;

    public Limit(int start, int count) {
        this.start = start;
        this.count = count;
    }
}