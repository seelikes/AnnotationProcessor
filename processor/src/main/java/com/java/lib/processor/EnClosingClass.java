package com.java.lib.processor;

import com.java.lib.oil.GlobalMethods;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

/**
 * 被注解元素的宿主类
 *
 * Created by liutiantian on 2018/3/18.
 */

public class EnClosingClass implements Iterable<AnnotatedElement<? extends Element>>, Serializable {
    /**
     * 注解预处理上下文
     * @see AbstractProcessor#processingEnv
     */
    private ProcessingEnvironment processingEnv;

    /**
     * 存储宿主类中被注解的元素
     * 注意此处不区分被注解元素的实际类型
     */
    private List<AnnotatedElement<? extends Element>> children;

    public EnClosingClass(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
    }

    /**
     * 获取此宿主类所代表的父类对象
     * @return 此宿主类所代表的父类对象
     */
    public AnnotatedElement<TypeElement> getEnClosingElement() {
        if (children == null || children.isEmpty()) {
            return null;
        }
        return new AnnotatedElement<>((TypeElement) children.get(0).getElement().getEnclosingElement(), processingEnv);
    }

    /**
     * 添加被注解的元素
     * @param element 被注解的元素
     * @param <T> 被注解元素的类型
     * @return true 添加子元素成功；false 添加子元素失败
     */
    public <T extends Element> boolean addChild(T element) {
        if (children == null) {
            children = new ArrayList<>();
        }
        if (element.getEnclosingElement() instanceof TypeElement) {
            if (children.isEmpty()) {
                children.add(new AnnotatedElement<>(element, processingEnv));
                return true;
            }
            AnnotatedElement<? extends Element> child = children.get(0);
            if (GlobalMethods.getInstance().checkEqual(child.getElement().getEnclosingElement(), element.getEnclosingElement())) {
                AnnotatedElement<T> annotated = new AnnotatedElement<>(element, processingEnv);
                if (children.contains(annotated)) {
                    return true;
                }
                children.add(annotated);
                return true;
            }
        }
        return false;
    }

    /**
     * 判断被注解的元素是否是此宿主的子元素
     * @param element 被注解的元素
     * @param <T> 被注解元素的类型
     * @return true 被注解的元素是否是此宿主的子元素；false 被注解的元素不是否是此宿主的子元素
     */
    public <T extends Element> boolean contains(T element) {
        if (children == null || children.isEmpty()) {
            return false;
        }
        for (AnnotatedElement annotated : children) {
            if (GlobalMethods.getInstance().checkEqual(annotated.getElement(), element)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断被注解的元素是否是此宿主的子元素
     * @param element 被注解的元素
     * @param <T> 被注解元素的类型
     * @return true 被注解的元素是此宿主的子元素；false 被注解的元素不是此宿主的子元素
     */
    public <T extends Element> boolean enClosingTo(T element) {
        if (children == null || children.isEmpty()) {
            return true;
        }
        AnnotatedElement<? extends Element> child = children.get(0);
        if (GlobalMethods.getInstance().checkEqual(child.getEnclosingElement(), element.getEnclosedElements())) {
            return true;
        }
        return false;
    }

    /**
     * 查询宿主类是否包含子元素
     * @return true 宿主类包含子元素；false 宿主类不包含子元素
     */
    public boolean isEmpty() {
        return children.isEmpty();
    }

    /**
     * 获取子元素个数
     * @return 子元素个数
     */
    public int size() {
        return children != null ? children.size() : 0;
    }

    @Override
    public Iterator<AnnotatedElement<? extends Element>> iterator() {
        return new Iterator<AnnotatedElement<? extends Element>>() {
            int i = 0;

            @Override
            public boolean hasNext() {
                return i < size();
            }

            @Override
            public AnnotatedElement<? extends Element> next() {
                if (hasNext()) {
                    return children.get(i++);
                }
                return null;
            }
        };
    }
}
