package info.breezes.orm;

public class Index {
    public enum IndexType {
        UNIQUE,
    }

    public IndexType type;
    public FCMap fcMap;
}
