package com.android.lib.bind.api.annotation;

/**
 * Created by liutiantian on 2018/3/18.
 */

public interface BindModule {
    /**
     * 获得被注解的实例的宿主实例
     * @param name 目标的名字
     * @return 被注解的实例的宿主实例
     */
    Object provideTarget(String name);
}
