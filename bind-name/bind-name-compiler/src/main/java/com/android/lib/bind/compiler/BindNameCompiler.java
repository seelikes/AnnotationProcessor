package com.android.lib.bind.compiler;

import com.android.lib.bind.api.annotation.BindModule;
import com.android.lib.bind.api.annotation.BindName;
import com.android.lib.bind.api.annotation.OnClick;
import com.google.auto.service.AutoService;
import com.java.lib.oil.lang.reflect.ReflectManager;
import com.java.lib.processor.AnnotatedElement;
import com.java.lib.processor.EnClosingClass;
import com.java.lib.processor.utils.EnClosingClassCache;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedAnnotationTypes({"com.android.lib.bind.api.annotation.BindName", "com.android.lib.bind.api.annotation.OnClick"})
public class BindNameCompiler extends AbstractProcessor {
    private EnClosingClassCache cache;

    public BindNameCompiler() {
        cache = new EnClosingClassCache();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> names = roundEnv.getElementsAnnotatedWith(BindName.class);
        if (!names.isEmpty()) {
            for (Element name : names) {
                cache.add(name, processingEnv);
            }
        }

        Set<? extends Element> clicks = roundEnv.getElementsAnnotatedWith(OnClick.class);
        if (!clicks.isEmpty()) {
            for (Element click : clicks) {
                cache.add(click, processingEnv);
            }
        }

        if (cache.isEmpty()) {
            return false;
        }

        for (EnClosingClass enClosing : cache) {
            if (enClosing.isEmpty()) {
                continue;
            }

            ClassName View = ClassName.bestGuess("android.view.View");

            MethodSpec.Builder initClickBuilder = MethodSpec.methodBuilder("initClick")
                    .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                    .addParameter(TypeName.get(enClosing.getEnClosingElement().getElement().asType()), "host", Modifier.FINAL)
                    .addParameter(Object.class, "view", Modifier.FINAL)
                    .addParameter(String.class, "method", Modifier.FINAL);

            initClickBuilder.addCode("if (host == null) {\n");
            initClickBuilder.addCode("    return;\n");
            initClickBuilder.addCode("}\n");
            initClickBuilder.addCode("if (view instanceof $T) {\n", View);
            initClickBuilder.addCode("    (($T) view).setOnClickListener(new $T.OnClickListener() {\n", View, View);
            initClickBuilder.addCode("        @$T\n", Override.class);
            initClickBuilder.addCode("        public void onClick($T v) {\n", View);
            initClickBuilder.addCode("            try {\n");
            initClickBuilder.addCode("                $T.getInstance().invoke(host.getClass(), method, host, view);\n", ReflectManager.class);
            initClickBuilder.addCode("            }\n");
            initClickBuilder.addCode("            catch ($T e) {\n", NoSuchMethodException.class);
            initClickBuilder.addCode("\n");
            initClickBuilder.addCode("            }\n");
            initClickBuilder.addCode("            catch ($T e) {\n", InvocationTargetException.class);
            initClickBuilder.addCode("\n");
            initClickBuilder.addCode("            }\n");
            initClickBuilder.addCode("            catch ($T e) {\n", IllegalAccessException.class);
            initClickBuilder.addCode("\n");
            initClickBuilder.addCode("            }\n");
            initClickBuilder.addCode("        }\n");
            initClickBuilder.addCode("    });\n");
            initClickBuilder.addCode("}\n");

            MethodSpec.Builder injectBuilder = MethodSpec.methodBuilder("bind")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .addParameter(TypeName.get(enClosing.getEnClosingElement().getElement().asType()), "host", Modifier.FINAL)
                    .addParameter(BindModule.class, "module", Modifier.FINAL);

            for (AnnotatedElement<? extends Element> annotated : enClosing) {
                if (annotated.getElement() instanceof VariableElement) {
                    if (annotated.getElement().getAnnotation(BindName.class) != null) {
                        injectBuilder.addStatement("$T.getInstance().setField(host.getClass(), \"$N\", host, module.provideTarget(\"$L\"))", ReflectManager.class, annotated.getElement().getSimpleName(), annotated.getElement().getAnnotation(BindName.class).value());
                    }
                }
                else if (annotated.getElement() instanceof ExecutableElement) {
                    if (annotated.getElement().getAnnotation(OnClick.class) != null) {
                        injectBuilder.addStatement("initClick(host, module.provideTarget(\"$N\"), \"$N\")", annotated.getElement().getAnnotation(OnClick.class).value(), annotated.getElement().getSimpleName());
                    }
                }
            }

            TypeSpec binderClass = TypeSpec.classBuilder(enClosing.getEnClosingElement().getElement().getSimpleName() + "Binder")
                    .addJavadoc("PLEASE DO NOT EDIT THIS CLASS, IT IS AUTO GENERATED, REFRESH FROM BUILD TO BUILD!\n")
                    .addModifiers(Modifier.PUBLIC)
                    .addMethod(injectBuilder.build())
                    .addMethod(initClickBuilder.build())
                    .build();
            try {
                JavaFile.builder(enClosing.getEnClosingElement().getPackage(), binderClass).indent("    ").build().writeTo(processingEnv.getFiler());
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        return true;
    }
}
