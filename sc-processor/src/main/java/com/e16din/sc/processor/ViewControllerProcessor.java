package com.e16din.sc.processor;

import com.e16din.sc.annotations.ViewController;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
public class ViewControllerProcessor extends AbstractProcessor {

    private static final String I_VIEW_CONTROLLER = "IViewController";

    private static final ClassName CLS_SCREENS_CONTROLLER = ClassName.get("com.e16din.sc", "ScreensController");
    private static final ClassName CLS_VIEW = ClassName.get("android.view", "View");
    private static final ClassName CLS_MENU_ITEM = ClassName.get("android.view", "MenuItem");
    private static final ArrayTypeName CLS_ARRAY_OF_OBJECT = ArrayTypeName.of(Object.class);
    private static final ClassName CLS_IVIEW_CONTROLLER = ClassName.get("com.e16din.sc", I_VIEW_CONTROLLER);
    private static final TypeName CLS_MAP_INTEGER_BOOLEAN = ParameterizedTypeName.get(ClassName.get(Map.class),
            ClassName.get(Integer.class), ClassName.get(Boolean.class));

    private static final ArrayTypeName CLS_ARRAY_OF_VC = ArrayTypeName.of(CLS_IVIEW_CONTROLLER);
    private static final String PARAM_THIS = "this";
    private static final String PARAM_SCREEN_NAME = "screenName";
    private static final String PARAM_VIEW = "view";
    private static final String PARAM_DATA = "data";

    private Filer filer;
    private Messager messager;

    private Map<String, String> screensMap = new HashMap<>();// <controller name, screen name>
    private Map<String, String> simpleMap = new HashMap<>();// <simple controller name, screen name>
    private Map<String, Boolean> startOnceMap = new HashMap<>();// <controller name , startOnce>


    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        filer = processingEnvironment.getFiler();
        messager = processingEnvironment.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Set<? extends Element> elementsAnnotatedWith =
                roundEnvironment.getElementsAnnotatedWith(ViewController.class);


        for (Element element : elementsAnnotatedWith) {
            if (element.getKind() != ElementKind.CLASS) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Can be applied to class.");
                return true;
            }

            TypeElement typeElement = (TypeElement) element;
            String controllerName = typeElement.getQualifiedName().toString();

            ViewController annotation = element.getAnnotation(ViewController.class);
            Map<String, String> map = (typeElement.getInterfaces() + "").contains(I_VIEW_CONTROLLER)
                    ? screensMap
                    : simpleMap;

            String screenName;
            try {
                screenName = annotation.screen().getName();
            } catch (MirroredTypeException mte) {
                screenName = mte.getTypeMirror().toString();
            }
            map.put(controllerName, screenName);

            boolean startOnce;
            try {
                startOnce = annotation.startOnce();
            } catch (MirroredTypeException mte) {
                startOnce = Boolean.getBoolean(mte.getTypeMirror().toString());
            }

            startOnceMap.put(controllerName, startOnce);
        }

        //todo: generate on select item listeners
        MethodSpec onMenuItemClickMethod = MethodSpec
                .methodBuilder("onMenuItemClick")
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.BOOLEAN)
                .addParameter(CLS_MENU_ITEM, "item")
                .addCode("return super.onMenuItemClick(item);\n ")
                .build();


        MethodSpec buildSimpleViewControllersMethod = MethodSpec
                .methodBuilder("buildSimpleViewControllers")
                .addModifiers(Modifier.PROTECTED)
                .returns(CLS_ARRAY_OF_OBJECT)
                .addParameter(Object.class, "data")
                .addParameter(CLS_VIEW, "view")
                .addParameter(String.class, "screenName")
                .addCode(createCode(true))
                .build();

        MethodSpec buildViewControllersMethod = MethodSpec
                .methodBuilder("buildViewControllers")
                .addModifiers(Modifier.PROTECTED)
                .returns(CLS_ARRAY_OF_VC)
                .addParameter(Object.class, "data")
                .addParameter(CLS_VIEW, "view")
                .addParameter(String.class, "screenName")
                .addCode(createCode(false))
                .build();

        MethodSpec isStartOnceControllerMethod = MethodSpec
                .methodBuilder("isStartOnceController")
                .addModifiers(Modifier.PROTECTED)
                .returns(ClassName.BOOLEAN)
                .addParameter(String.class, "controllerName")
                .addCode(createIsStartOnceCode())
                .build();

        TypeSpec controllersBuilderClass = TypeSpec
                .classBuilder("GeneratedScreensController")
                .superclass(CLS_SCREENS_CONTROLLER)
                .addModifiers(Modifier.PUBLIC)
                .addMethod(buildSimpleViewControllersMethod)
                .addMethod(buildViewControllersMethod)
                .addMethod(onMenuItemClickMethod)
                .addMethod(isStartOnceControllerMethod)
                .addField(CLS_MAP_INTEGER_BOOLEAN, "startControllersMap", Modifier.PROTECTED)
                .build();

        JavaFile file = JavaFile.builder("com.e16din.sc", controllersBuilderClass).build();

        try {
            file.writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    private String createIsStartOnceCode() {
        String code = "switch(controllerName) {";

        final Set<String> startOnceControllers = getKeysByValue(startOnceMap, true);
        for (String name : startOnceControllers) {
            code += "\n    case " + "\"" + name + "\": return true;";
        }

        code += "\n}\n\n" +
                "return false;\n";

        return code;
    }

    private String createCode(boolean simpleVc) {
        StringBuilder cases = new StringBuilder();

        List<String> addedScreens = new ArrayList<>();

        String resultType = simpleVc ? "Object" : I_VIEW_CONTROLLER;

        final Map<String, String> map = simpleVc ? simpleMap : screensMap;

        for (String screenName : map.values()) {
            if (addedScreens.contains(screenName)) continue; // else {
            addedScreens.add(screenName);

            Set<String> controllers = getKeysByValue(map, screenName);

            cases.append("case \"")
                    .append(screenName)
                    .append("\":\n    return new " + resultType + "[]{");

            for (String controllerName : controllers) {
                cases.append("\n            new ")
                        .append(controllerName)
                        .append("(");

                if (simpleVc) {
                    cases.append(PARAM_THIS)
                            .append(", ")
                            .append(PARAM_VIEW)
                            .append(", ")
                            .append(PARAM_DATA);
                }

                cases.append("),");
            }

            cases.append("\n    };\n");
        }
        cases.append("default:\n" + "    return new " + resultType + "[]{};");

        return "switch (" + PARAM_SCREEN_NAME + ") {\n" +
                cases +
                "}";
    }

    private static <T, E> Set<T> getKeysByValue(Map<T, E> map, E value) {
        Set<T> keys = new HashSet<>();
        for (Map.Entry<T, E> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                keys.add(entry.getKey());
            }
        }
        return keys;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new HashSet<>();
        annotations.add(ViewController.class.getCanonicalName());

        return annotations;
    }
}
