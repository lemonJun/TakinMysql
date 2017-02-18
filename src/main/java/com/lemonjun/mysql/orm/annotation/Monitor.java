package com.lemonjun.mysql.orm.annotation;

import java.lang.annotation.*;

/**
 * 增加方法监控功能
 *
 * @author WangYazhou
 * @date  2016年12月22日 下午3:27:31
 * @see
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface Monitor {
}
