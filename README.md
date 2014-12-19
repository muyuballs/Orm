Orm [![Build Status](https://travis-ci.org/xbreezes/Orm.svg?branch=master)](https://travis-ci.org/xbreezes/Orm)
-----

零配置android简单rom框架

#### 功能简介

 * 支持CURD
 * 支持事务 CUD默认开启事务
 * 可通过注解自定义表名，列名，唯一性约束，NOT NULL约束
 * 支持链式表达查询，更直观的查询语义
 * 通过QueryAble查询获得的Cursor支持CursorAdapter的AutoReload

#### 开始使用 

1.模型定义

```java
    //model
    @Table(name="demo")
    class Demo{
        @Column(primaryKey=true,name="_id",autoincrement=true)
        public int id;
        @Column(name="name")
        public String name;
        @Column
        public int age;
    }
```

2.创建表

 * 一气呵成
 
```java
    SimpleOrmSQLiteHelper helper=new SimpleOrmSQLiteHelper(context,"demo.db",1,Demo.class);
```

 * 在onCreate中创建
 
```java
    class DemoHelper extends SimpleOrmSQLiteHelper{
     ...
        public void onCreate(SQLiteDatabase database){
            TableUtils.createTable(database,Demo.class);
        }
     ...
    }
```

3.CURD 操作

 * 插入

```java
    Demo demo=new Demo();
    demo.name="hello"
    demo.arg=12;
    helper.insert(demo);
```

 * 批量插入

 ```java
    ArrayList<Demo> demos=new ArrayList<>();
    for(int i=0;i<10;i++){
       Demo demo=new Demo();
       demo.name="hello"
       demo.arg=12;
       demos.add(demo);
    }
    helper.insertAll(demos.toArray());
 ```

 * 查询

    ```java
    //获取第一个
     Demo demo=helper.query(Demo.class).execute().first();
     
     //for循环遍历
     for(Demo demo:helper.query(Demo.class).execute()){
         //do somethings
     }
     
     //条件查询
     Demo demo=helper.query(Demo.class).where("age",12,"=").execute().first();
    ```

 * 删除

 ```java
 Demo demo=new Demo();
 demo.id=1;
 demo.name="hello";
 
 //按主键删除
 helper.delete(demo);
 
 //按自定义字段删除
 helper.deleteBy(demo,"name);
 ```

 * 修改

 ```java
 Demo demo=helper.query(Demo.class).execute().first();
 demo.name="hello1"
 demo.age=19;
 helper.insertOrUpdate(demo);
 ```

#### 版本更新

  * 2014-10-22
  
    >提升ORM的查询和插入效率
  
    >Column中的排序属性number修改为order

    >insertAll拆分为insertAll和insertOrUpdateAll
    
