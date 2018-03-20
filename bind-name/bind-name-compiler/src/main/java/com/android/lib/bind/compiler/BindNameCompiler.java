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
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

@AutoService(Processor.class)
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

            MethodSpec.Builder injectBuilder = MethodSpec.methodBuilder("bind")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .addParameter(TypeName.get(enClosing.getEnClosingElement().getElement().asType()), "host", Modifier.FINAL)
                    .addParameter(BindModule.class, "module", Modifier.FINAL);

            ClassName View = ClassName.bestGuess("android.view.View");

            for (AnnotatedElement<? extends Element> annotated : enClosing) {
                if (annotated.getElement() instanceof VariableElement) {
                    if (annotated.getElement().getAnnotation(BindName.class) != null) {
                        injectBuilder.addStatement("$T.getInstance().setField(host.getClass(), \"$N\", host, module.provideTarget(\"$L\"))", ReflectManager.class, annotated.getElement().getSimpleName(), annotated.getElement().getAnnotation(BindName.class).value());
                    }
                }
                else if (annotated.getElement() instanceof ExecutableElement) {
                    if (annotated.getElement().getAnnotation(OnClick.class) != null) {
                        injectBuilder.addStatement("Object view = module.provideTarget(\"$L\")", annotated.getElement().getAnnotation(OnClick.class).value());
                        injectBuilder.addCode("if (view instanceof $T) {\n", View);
                        injectBuilder.addCode("    (($T) view).setOnClickListener(new $T.OnClickListener() {\n", View, View);
                        injectBuilder.addCode("        @$T\n", Override.class);
                        injectBuilder.addCode("        public void onClick($T v) {\n", View);
                        injectBuilder.addCode("            try {\n");
                        injectBuilder.addCode("                $T.getInstance().invoke(host.getClass(), \"$N\", host, module.provideTarget(\"$L\"));\n", ReflectManager.class, annotated.getElement().getSimpleName(), annotated.getElement().getAnnotation(OnClick.class).value());
                        injectBuilder.addCode("            }\n");
                        injectBuilder.addCode("            catch ($T e) {\n", NoSuchMethodException.class);
                        injectBuilder.addCode("\n");
                        injectBuilder.addCode("            }\n");
                        injectBuilder.addCode("            catch ($T e) {\n", InvocationTargetException.class);
                        injectBuilder.addCode("\n");
                        injectBuilder.addCode("            }\n");
                        injectBuilder.addCode("            catch ($T e) {\n", IllegalAccessException.class);
                        injectBuilder.addCode("\n");
                        injectBuilder.addCode("            }\n");
                        injectBuilder.addCode("        }\n");
                        injectBuilder.addCode("    });\n");
                        injectBuilder.addCode("}\n");
                    }
                }
            }

            TypeSpec binderClass = TypeSpec.classBuilder(enClosing.getEnClosingElement().getElement().getSimpleName() + "Binder")
                    .addJavadoc("PLEASE DO NOT EDIT THIS CLASS, IT IS AUTO GENERATED, REFRESH FROM BUILD TO BUILD!\n")
                    .addModifiers(Modifier.PUBLIC)
                    .addMethod(injectBuilder.build())
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
