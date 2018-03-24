package com.android.lib.rp.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface RPNativeModule {
    /**
     * 当前module注册的目标包名称
     * 最终类名为返回值 + Package
     * @return 目标包名称
     */
    String value();
}
