package info.breezes.orm.model;

import info.breezes.orm.annotation.Column;
import info.breezes.orm.annotation.Table;

@Table(name = "orm.db")
public class Db {
    @Column(name = "version")
    public int version;
    @Column(name="table",primaryKey = true)
    public String table;
}
