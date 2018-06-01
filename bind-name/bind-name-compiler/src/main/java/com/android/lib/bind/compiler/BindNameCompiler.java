package com.android.lib.bind.compiler;

import com.android.lib.bind.api.annotation.BindModule;
import com.android.lib.bind.api.annotation.BindView;
import com.android.lib.bind.api.annotation.OnClick;
import com.google.auto.service.AutoService;
import com.java.lib.oil.GlobalMethods;
import com.java.lib.processor.AnnotatedElement;
import com.java.lib.processor.EnClosingClass;
import com.java.lib.processor.utils.EnClosingClassCache;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.List;
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
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedAnnotationTypes({"com.android.lib.bind.api.annotation.BindView", "com.android.lib.bind.api.annotation.OnClick"})
public class BindNameCompiler extends AbstractProcessor {
    private EnClosingClassCache cache;

    public BindNameCompiler() {
        cache = new EnClosingClassCache();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "BindNameCompiler, process, annotations == null || annotations.isEmpty(): " + (annotations == null || annotations.isEmpty()));
        if (annotations == null || annotations.isEmpty()) {
            return false;
        }

        for (TypeElement element : annotations) {
            if (!GlobalMethods.getInstance().checkIn(element.getQualifiedName().toString(), "com.android.lib.bind.api.annotation.BindView", "com.android.lib.bind.api.annotation.OnClick")) {
                return false;
            }
        }

        Set<? extends Element> names = roundEnv.getElementsAnnotatedWith(BindView.class);
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
            return true;
        }

        for (EnClosingClass enClosing : cache) {
            if (enClosing.isEmpty()) {
                continue;
            }

            ClassName View = ClassName.bestGuess("android.view.View");

            MethodSpec.Builder injectBuilder = MethodSpec.methodBuilder("bind")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .addParameter(TypeName.get(enClosing.getEnClosingElement().getElement().asType()), "host", Modifier.FINAL)
                    .addParameter(BindModule.class, "module", Modifier.FINAL);

            for (AnnotatedElement<? extends Element> annotated : enClosing) {
                if (annotated.getElement() instanceof VariableElement) {
                    if (annotated.getElement().getAnnotation(BindView.class) != null) {
                        injectBuilder.addStatement("host.$N = ($T) module.provideTarget(\"$L\")", annotated.getElement().getSimpleName(), annotated.getElement().asType(), annotated.getElement().getAnnotation(BindView.class).value());
                    }
                }
                else if (annotated.getElement() instanceof ExecutableElement) {
                    if (annotated.getElement().getAnnotation(OnClick.class) != null) {
                        Name name = findName(names, annotated.getElement().getAnnotation(OnClick.class).value());
                        if (name != null) {
                            injectBuilder.addCode("host.$N.setOnClickListener(new $T.OnClickListener() {\n", name, View);
                            injectBuilder.addCode("    @$T\n", Override.class);
                            injectBuilder.addCode("    public void onClick($T v) {\n", View);
                            injectBuilder.addCode("         host.$N(host.$N);\n", annotated.getElement().getSimpleName(), name);
                            injectBuilder.addCode("    }\n");
                            injectBuilder.addCode("});\n");
                        }
                        else {
                            injectBuilder.addCode("(($T) module.provideTarget(\"$N\")).setOnClickListener(new $T.OnClickListener() {\n", View, annotated.getElement().getAnnotation(OnClick.class).value(), View);
                            injectBuilder.addCode("    @$T\n", Override.class);
                            injectBuilder.addCode("    public void onClick($T v) {\n", View);
                            List<? extends VariableElement> parameters = ((ExecutableElement) annotated.getElement()).getParameters();
                            if (parameters != null) {
                                injectBuilder.addCode("         host.$N((($T) module.provideTarget(\"$N\")));\n", annotated.getElement().getSimpleName(), parameters.get(0).asType(), annotated.getElement().getAnnotation(OnClick.class).value());
                            }
                            else {
                                injectBuilder.addCode("         host.$N();\n", annotated.getElement().getSimpleName());
                            }
                            injectBuilder.addCode("    }\n");
                            injectBuilder.addCode("});\n");
                        }
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

    /**
     * 查找被注解元素的名字
     * @param names 被注解元素的名字集合
     * @param element 被注解元素的资源名字
     * @return 被注解元素的名字
     */
    public Name findName(Set<? extends Element> names, String element) {
        if (names == null || names.isEmpty() || element == null) {
            return null;
        }
        for (Element name : names) {
            if (GlobalMethods.getInstance().checkEqual(name.getAnnotation(BindView.class).value(), element)) {
                return name.getSimpleName();
            }
        }
        return null;
    }
}
