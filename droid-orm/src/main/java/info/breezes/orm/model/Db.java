package info.breezes.orm.model;

import info.breezes.orm.annotation.Column;
import info.breezes.orm.annotation.Table;

@Table(name = "__orm_db_version__")
public class Db {
    @Column(name = "version")
    public int version;
    @Column(name="table_name",primaryKey = true)
    public String tableName;
    @Column(name="extra1")
    public String extra1;
    @Column(name="extra2")
    public String extra2;
    @Column(name="extra3")
    public String extra3;
    @Column(name="extra4")
    public String extra4;
    @Column(name="extra5")
    public String extra5;
}
