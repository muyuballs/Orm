package info.breezes.orm.expressions;

/**
 * Created by Qiao on 2014/10/20.
 */
public class OrderBy{
    public String column;
    public String type;

    public OrderBy(String column, String type) {
        this.column = column;
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OrderBy orderBy = (OrderBy) o;

        if (!column.equals(orderBy.column)) return false;
        if (!type.equals(orderBy.type)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = column.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }
}