package info.breezes.orm.expressions;

/**
 * Created by Qiao on 2014/10/20.
 */
public class Where {
    public String condition;
    public String column;
    public Object value;
    public String operation;

    public Where(String column, Object value, String operation) {
        this.column = column;
        this.value = value;
        this.operation = operation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Where where = (Where) o;

        if (!column.equals(where.column)) return false;
        if (!operation.equals(where.operation)) return false;
        if (!value.equals(where.value)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = column.hashCode();
        result = 31 * result + value.hashCode();
        result = 31 * result + operation.hashCode();
        return result;
    }
}