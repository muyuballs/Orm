Orm [![Build Status](https://travis-ci.org/xbreezes/Orm.svg?branch=master)](https://travis-ci.org/xbreezes/Orm)
-----

#### 功能简介

 * 支持CURD
 * 支持事务 CUD默认开启事务
 * 可通过注解自定义表名，列名，唯一性约束，NOT NULL约束
 * 支持链式表达查询，更直观的查询语义
 * 通过QueryAble查询获得的Cursor支持CursorAdapter的AutoReload

#### 版本更新

  * 2014-10-22
  
    >提升ORM的查询和插入效率
  
    >Column中的排序属性number修改为order

    >insertAll拆分为insertAll和insertOrUpdateAll
    
