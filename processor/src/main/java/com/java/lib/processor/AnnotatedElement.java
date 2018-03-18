package com.java.lib.processor;

import com.java.lib.oil.GlobalMethods;

import java.io.Serializable;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;

/**
 * 此类目标为封装被注解的类，提供便利的方法用以操作被注解的类
 */
public class AnnotatedElement<T extends Element> implements Serializable {
    /**
     * 被注解的元素
     * @see RoundEnvironment#getElementsAnnotatedWith(TypeElement)
     */
    private T element;

    /**
     * 注解预处理上下文
     * @see AbstractProcessor#processingEnv
     */
    private ProcessingEnvironment processingEnv;

    public AnnotatedElement(T element, ProcessingEnvironment processingEnv) {
        this.element = element;
        this.processingEnv = processingEnv;
    }

    public T getElement() {
        return element;
    }

    /**
     * 获取注解元素所在的包元素
     * @return 注解元素所在的包元素
     */
    public PackageElement getPackageElement() {
        return processingEnv.getElementUtils().getPackageOf(element);
    }

    /**
     * 获取注解元素所在的包名
     * @return 注解元素所在的包名
     */
    public Name getPackageName() {
        return getPackageElement().getQualifiedName();
    }

    /**
     * 获取注解元素所在的包
     * @return 注解元素所在的包
     */
    public String getPackage() {
        return getPackageName().toString();
    }

    /**
     * 获取包含被注解元素的直接父元素的名称
     * @return 被注解元素的直接父元素的名称
     */
    public Name getEnclosingElementName() {
        return element.getEnclosingElement().getSimpleName();
    }

    /**
     * 获取包含被注解元素的直接父元素的名称
     * @return 被注解元素的直接父元素的名称
     */
    public String getEnclosingElement() {
        return getEnclosingElementName().toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof AnnotatedElement) {
            AnnotatedElement annotated = (AnnotatedElement) obj;
            if (!GlobalMethods.getInstance().checkEqual(element, annotated.element)) {
                return false;
            }
            return true;
        }
        return false;
    }
}
