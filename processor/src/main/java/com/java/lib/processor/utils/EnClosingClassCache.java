package com.java.lib.processor.utils;

import com.java.lib.processor.EnClosingClass;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;

/**
 * 保存宿主类的缓存对象
 * 此类创建主要理由是我们几乎肯定需要此类，而其逻辑在外观上是极其相似的
 *
 * Created by liutiantian on 2018/3/18.
 */

public class EnClosingClassCache implements Iterable<EnClosingClass> {
    private List<EnClosingClass> cache;

    public EnClosingClassCache() {
        cache = new ArrayList<>();
    }

    /**
     * 查找被注解元素的宿主对象
     * @param element 被注解的元素
     * @param <T> 被注解的元素类型
     * @return 被注解元素的宿主对象
     */
    public <T extends Element> EnClosingClass get(T element) {
        if (cache.isEmpty()) {
            return null;
        }
        for (EnClosingClass host : cache) {
            if (host.contains(element)) {
                return host;
            }
        }
        return null;
    }

    /**
     * 添加被注解的元素到其宿主类对象中
     * @param element 被注解的元素
     * @param processingEnv 注解预处理上下文
     * @param <T> 被注解元素的类型
     */
    public <T extends Element> void add(T element, ProcessingEnvironment processingEnv) {
        if (cache.isEmpty()) {
            EnClosingClass host = new EnClosingClass(processingEnv);
            cache.add(host);
            host.addChild(element);
            return;
        }
        for (EnClosingClass host : cache) {
            if (host.addChild(element)) {
                return;
            }
        }
    }

    /**
     * 查看当前缓存是否为空
     * @return true 当前缓存为空；false 当前缓存不为空
     */
    public boolean isEmpty() {
        return cache.isEmpty();
    }

    /**
     * 查看当前缓存条目数
     * @return 当前缓存条目数
     */
    public int size() {
        return cache.size();
    }

    @Override
    public Iterator<EnClosingClass> iterator() {
        return new Iterator<EnClosingClass>() {
            int index = 0;

            @Override
            public boolean hasNext() {
                return index < cache.size();
            }

            @Override
            public EnClosingClass next() {
                if (hasNext()) {
                    return cache.get(index++);
                }
                return null;
            }
        };
    }
}
