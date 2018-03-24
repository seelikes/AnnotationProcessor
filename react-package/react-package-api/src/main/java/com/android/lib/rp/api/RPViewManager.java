package com.android.lib.rp.api;

/**
 * Created by liutiantian on 2018/3/24.
 */

public @interface RPViewManager {
    /**
     * 当前ViewManager注册的目标包名称
     * 最终类名为 返回值 + Package
     * @return 目标包名称
     */
    String value();
}
