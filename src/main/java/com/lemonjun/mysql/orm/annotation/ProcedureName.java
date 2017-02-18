package com.lemonjun.mysql.orm.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ProcedureName {
    public String delete();

    public String insert();

    public String update();

    public String updateByID();

    public String load();

    public String locaByPage();
}