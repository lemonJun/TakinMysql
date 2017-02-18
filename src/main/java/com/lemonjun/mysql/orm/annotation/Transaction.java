package com.lemonjun.mysql.orm.annotation;

import java.lang.annotation.*;

/**
 * 增加事务功能
 * 
 * @author WangYazhou
 * @date  2016年12月22日 下午3:26:44
 * @see
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface Transaction {
}
