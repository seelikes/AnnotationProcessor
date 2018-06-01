package com.android.lib.rp;

import com.android.lib.rp.api.RPNativeModule;
import com.android.lib.rp.api.RPViewManager;
import com.google.auto.service.AutoService;
import com.java.lib.oil.GlobalMethods;
import com.java.lib.oil.lang.StringManager;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedAnnotationTypes({"com.android.lib.rp.api.RPNativeModule", "com.android.lib.rp.api.RPViewManager"})
public class RPCompiler extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations == null || annotations.isEmpty()) {
            return false;
        }

        for (TypeElement element : annotations) {
            if (!GlobalMethods.getInstance().checkIn(element.getQualifiedName().toString(), "com.android.lib.rp.api.RPNativeModule", "com.android.lib.rp.api.RPViewManager")) {
                return false;
            }
        }

        List<String> names = new ArrayList<>();

        List<? extends Element> modules = new ArrayList<>(roundEnv.getElementsAnnotatedWith(RPNativeModule.class));
        if (!modules.isEmpty()) {
            for (int i = 0; i < modules.size(); ++i) {
                if (names.contains(modules.get(i).getAnnotation(RPNativeModule.class).value())) {
                    continue;
                }
                names.add(modules.get(i).getAnnotation(RPNativeModule.class).value());
            }
        }
        List<? extends Element> managers = new ArrayList<>(roundEnv.getElementsAnnotatedWith(RPViewManager.class));
        if (!managers.isEmpty()) {
            for (int i = 0; i < managers.size(); ++i) {
                if (names.contains(managers.get(i).getAnnotation(RPViewManager.class).value())) {
                    continue;
                }
                names.add(managers.get(i).getAnnotation(RPViewManager.class).value());
            }
        }

        for (String name : names) {
            if (StringManager.getInstance().isEmpty(name)) {
                continue;
            }

            List<Element> nameModules = new ArrayList<>();
            for (int i = 0; i < modules.size(); ++i) {
                if (GlobalMethods.getInstance().checkEqual(name, modules.get(i).getAnnotation(RPNativeModule.class).value())) {
                    nameModules.add(modules.get(i));
                }
            }
            List<Element> nameManagers = new ArrayList<>();
            for (int i = 0; i < managers.size(); ++i) {
                if (GlobalMethods.getInstance().checkEqual(name, managers.get(i).getAnnotation(RPViewManager.class).value())) {
                    nameManagers.add(managers.get(i));
                }
            }
            if (nameModules.isEmpty() && nameManagers.isEmpty()) {
                continue;
            }

            String className = name;
            if (className.startsWith("React")) {
                className = className.substring(5);
            }
            if (className.isEmpty()) {
                continue;
            }
            className = className.substring(0, 1).toUpperCase() + className.substring(1);
            if (!className.endsWith("Package")) {
                className += "Package";
            }

            Element packageElement = nameModules.get(0).getEnclosingElement();
            for (Element element : nameModules) {
                while (true) {
                    if (packageElement != null && !GlobalMethods.getInstance().checkEqual(packageElement, element.getEnclosingElement())) {
                        packageElement = packageElement.getEnclosingElement();
                        continue;
                    }
                    break;
                }
            }
            for (Element element : nameManagers) {
                while (true) {
                    if (packageElement != null && !GlobalMethods.getInstance().checkEqual(packageElement, element.getEnclosingElement())) {
                        packageElement = packageElement.getEnclosingElement();
                        continue;
                    }
                    break;
                }
            }

            ClassName ReactPackage = ClassName.bestGuess("com.facebook.react.ReactPackage");
            ClassName NativeModule = ClassName.bestGuess("com.facebook.react.bridge.NativeModule");
            ClassName ReactApplicationContext = ClassName.bestGuess("com.facebook.react.bridge.ReactApplicationContext");
            ClassName ViewManager = ClassName.bestGuess("com.facebook.react.uimanager.ViewManager");

            MethodSpec.Builder createNativeModulesBuilder = MethodSpec.methodBuilder("createNativeModules")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(ReactApplicationContext, "reactContext")
                    .returns(ParameterizedTypeName.get(ClassName.get(List.class), NativeModule));

            if (nameModules.isEmpty()) {
                createNativeModulesBuilder.addStatement("return $T.emptyList()", Collections.class);
            }
            else {
                createNativeModulesBuilder.addCode("return $T.<$T>asList(\n", Arrays.class, NativeModule);
                for (int i = 0; i < nameModules.size(); ++i) {
                    if (i == 0) {
                        createNativeModulesBuilder.addCode("    ");
                    }
                    else {
                        createNativeModulesBuilder.addCode(", ");
                    }
                    createNativeModulesBuilder.addCode("new $T(reactContext)", nameModules.get(i).asType());
                    if (i == nameModules.size() - 1) {
                        createNativeModulesBuilder.addCode("\n");
                    }
                }
                createNativeModulesBuilder.addCode(");\n");
            }

            MethodSpec.Builder createViewManagersBuilder = MethodSpec.methodBuilder("createViewManagers")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(ReactApplicationContext, "reactContext")
                    .returns(ParameterizedTypeName.get(ClassName.get(List.class), ViewManager));

            if (nameManagers.isEmpty()) {
                createViewManagersBuilder.addStatement("return $T.emptyList()", Collections.class);
            }
            else {
                createViewManagersBuilder.addCode("return $T.<$T>asList(\n", Arrays.class, ViewManager);
                for (int i = 0; i < nameManagers.size(); ++i) {
                    if (i == 0) {
                        createViewManagersBuilder.addCode("    ");
                    }
                    else {
                        createViewManagersBuilder.addCode(", ");
                    }
                    createViewManagersBuilder.addCode("new $T()", nameManagers.get(i).asType());
                    if (i == nameManagers.size() - 1) {
                        createViewManagersBuilder.addCode("\n");
                    }
                }
                createViewManagersBuilder.addCode(");\n");
            }

            TypeSpec rpClass = TypeSpec.classBuilder(className)
                    .addJavadoc("PLEASE DO NOT EDIT THIS CLASS, IT IS AUTO GENERATED, REFRESH FROM BUILD TO BUILD!\n")
                    .addModifiers(Modifier.PUBLIC)
                    .addSuperinterface(ReactPackage)
                    .addMethod(createNativeModulesBuilder.build())
                    .addMethod(createViewManagersBuilder.build())
                    .build();
            try {
                if (packageElement != null) {
                    JavaFile.builder(((PackageElement) packageElement).getQualifiedName().toString(), rpClass).indent("    ").build().writeTo(processingEnv.getFiler());
                }
                else {
                    JavaFile.builder("com.android.lib.rp", rpClass).indent("    ").build().writeTo(processingEnv.getFiler());
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
}
