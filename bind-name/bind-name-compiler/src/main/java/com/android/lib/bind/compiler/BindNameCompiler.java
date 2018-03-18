package com.android.lib.bind.compiler;

import com.android.lib.bind.api.annotation.BindModule;
import com.android.lib.bind.api.annotation.BindName;
import com.google.auto.service.AutoService;
import com.java.lib.oil.lang.reflect.ReflectManager;
import com.java.lib.processor.AnnotatedElement;
import com.java.lib.processor.EnClosingClass;
import com.java.lib.processor.utils.EnClosingClassCache;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

@AutoService(Processor.class)
@SupportedAnnotationTypes({"com.android.lib.bind.api.annotation.BindName"})
public class BindNameCompiler extends AbstractProcessor {
    private EnClosingClassCache cache;

    public BindNameCompiler() {
        cache = new EnClosingClassCache();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(BindName.class);
        if (!elements.isEmpty()) {
            for (Element element : elements) {
                cache.add(element, processingEnv);
            }

            if (cache.isEmpty()) {
                return false;
            }

            for (EnClosingClass enClosing : cache) {
                if (enClosing.isEmpty()) {
                    continue;
                }

                MethodSpec.Builder injectBuilder = MethodSpec.methodBuilder("bind")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .addParameter(TypeName.get(enClosing.getEnClosingElement().getElement().asType()), "host")
                        .addParameter(BindModule.class, "module");

                for (AnnotatedElement<? extends Element> annotated : enClosing) {
                    injectBuilder.addStatement("$T.getInstance().setField(host.getClass(), \"$N\", host, module.provideTarget(\"$L\"))", ReflectManager.class, annotated.getElement().getSimpleName(), annotated.getElement().getAnnotation(BindName.class).value());
                }

                TypeSpec binderClass = TypeSpec.classBuilder(enClosing.getEnClosingElement().getElement().getSimpleName() + "Binder")
                        .addModifiers(Modifier.PUBLIC)
                        .addMethod(injectBuilder.build())
                        .build();
                try {
                    JavaFile.builder(enClosing.getEnClosingElement().getPackage(), binderClass).build().writeTo(processingEnv.getFiler());
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }
}
