package com.android.lib.bind.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by liutiantian on 2018/3/20.
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface OnClick {
    /**
     * 视图资源id的名字
     * @return 资源id的名字
     */
    String value();
}
