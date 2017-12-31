package com.e16din.sc.processor;

import com.e16din.sc.annotations.OnBackPressed;
import com.e16din.sc.annotations.OnBind;
import com.e16din.sc.annotations.OnHide;
import com.e16din.sc.annotations.OnMenuItemClick;
import com.e16din.sc.annotations.OnResult;
import com.e16din.sc.annotations.OnShow;
import com.e16din.sc.annotations.ViewController;
import com.e16din.sc.processor.code.EnabledGenerator;
import com.e16din.sc.processor.code.OnActivityResultGenerator;
import com.e16din.sc.processor.code.OnBindViewControllerGenerator;
import com.e16din.sc.processor.code.OnHideViewControllerGenerator;
import com.e16din.sc.processor.code.OnMenuItemClickGenerator;
import com.e16din.sc.processor.code.OnShowViewControllerGenerator;
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

    private static final ClassName CLS_INTENT = ClassName.get("android.content", "Intent");
    private static final ClassName CLS_APPLICATION = ClassName.get("android.app", "Application");

    private static final ClassName CLS_SCREENS_CONTROLLER = ClassName.get("com.e16din.sc", "ScreensController");
    private static final ArrayTypeName CLS_ARRAY_OF_OBJECT = ArrayTypeName.of(Object.class);

    private static final TypeName CLS_MAP_INTEGER_BOOLEAN = ParameterizedTypeName.get(ClassName.get(Map.class),
            ClassName.get(Integer.class), ClassName.get(Boolean.class));

    private static final String PARAM_SCREEN_NAME = "screenName";

    private Filer filer;
    private Messager messager;

    private Map<String, String> screensMap = new HashMap<>();// <simple controller name, screen name>
    private Map<String, Boolean> startOnceMap = new HashMap<>();// <controller name , startOnce>


    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        filer = processingEnvironment.getFiler();
        messager = processingEnvironment.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Set<? extends Element> elementsAnnotatedWithViewControllers =
                roundEnvironment.getElementsAnnotatedWith(ViewController.class);


        for (Element element : elementsAnnotatedWithViewControllers) {
            if (element.getKind() != ElementKind.CLASS) {
                printError("Can be applied to class.");
                return true;
            }

            TypeElement typeElement = (TypeElement) element;
            String controllerName = typeElement.getQualifiedName().toString();

            ViewController annotation = element.getAnnotation(ViewController.class);

            String screenName;
            try {
                screenName = annotation.screen().getName();
            } catch (MirroredTypeException mte) {
                screenName = mte.getTypeMirror().toString();
            }
            screensMap.put(controllerName, screenName);

            boolean startOnce;
            try {
                startOnce = annotation.startOnce();
            } catch (MirroredTypeException mte) {
                startOnce = Boolean.getBoolean(mte.getTypeMirror().toString());
            }

            startOnceMap.put(controllerName, startOnce);
        }

        MethodSpec onMenuItemClickMethod = OnMenuItemClickGenerator.process(roundEnvironment);

        MethodSpec onActivityResultMethod = OnActivityResultGenerator.process(roundEnvironment);

        MethodSpec onBindViewControllerMethod = OnBindViewControllerGenerator.process(roundEnvironment);
        MethodSpec onShowViewControllerMethod = OnShowViewControllerGenerator.process(roundEnvironment);
        MethodSpec onHideViewControllerMethod = OnHideViewControllerGenerator.process(roundEnvironment);

        MethodSpec enabledMethod = EnabledGenerator.process(roundEnvironment);

        String superResultCode =
                "    for (Object vc : currentViewControllers) {\n" +
                        "        onActivityResult(vc, requestCode, resultCode, data);\n" +
                        "    }\n" +
                        "    for (Object vc : startViewControllers) {\n" +
                        "        onActivityResult(vc, requestCode, resultCode, data);\n" +
                        "    }\n";

        MethodSpec onSuperResultMethod = MethodSpec
                .methodBuilder("onActivityResult")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(TypeName.INT, "requestCode")
                .addParameter(TypeName.INT, "resultCode")
                .addParameter(CLS_INTENT, "data")
                .addCode(superResultCode)
                .build();


        MethodSpec buildViewControllersMethod = MethodSpec
                .methodBuilder("buildViewControllers")
                .addModifiers(Modifier.PUBLIC)
                .returns(CLS_ARRAY_OF_OBJECT)
                .addParameter(String.class, "screenName")
                .addCode(createCode())
                .build();

        MethodSpec isStartOnceControllerMethod = MethodSpec
                .methodBuilder("once")
                .addModifiers(Modifier.PUBLIC)
                .returns(ClassName.BOOLEAN)
                .addParameter(String.class, "vcName")
                .addCode(createIsStartOnceCode())
                .build();

        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(CLS_APPLICATION, "app")
                .addStatement("super(app)")
                .build();

        TypeSpec controllersBuilderClass = TypeSpec
                .classBuilder("GeneratedScreensController")
                .superclass(CLS_SCREENS_CONTROLLER)
                .addModifiers(Modifier.PUBLIC)
                .addMethod(buildViewControllersMethod)
                .addMethod(onBindViewControllerMethod)
                .addMethod(onShowViewControllerMethod)
                .addMethod(onHideViewControllerMethod)
                .addMethod(onMenuItemClickMethod)
                .addMethod(isStartOnceControllerMethod)
                .addMethod(enabledMethod)
                .addMethod(onSuperResultMethod)
                .addMethod(onActivityResultMethod)
                .addMethod(constructor)
                // .addField(CLS_MAP_INTEGER_BOOLEAN, "startControllersMap", Modifier.PROTECTED)
                .build();

        JavaFile file = JavaFile.builder("com.e16din.sc", controllersBuilderClass).build();

        try {
            file.writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    private void printError(String message) {
        messager.printMessage(Diagnostic.Kind.ERROR, message);
    }

    private void print(String message) {
        messager.printMessage(Diagnostic.Kind.WARNING, message);
    }


    private String createIsStartOnceCode() {
        String code = "switch(vcName) {";

        final Set<String> startOnceControllers = getKeysByValue(startOnceMap, true);
        for (String name : startOnceControllers) {
            code += "\n    case " + "\"" + name + "\": return true;";
        }

        code += "\n}\n\n" +
                "return false;\n";

        return code;
    }

    private String createCode() {
        StringBuilder cases = new StringBuilder();

        List<String> addedScreens = new ArrayList<>();

        String resultType = "Object";

        for (String screenName : screensMap.values()) {
            if (addedScreens.contains(screenName)) continue; // else {
            addedScreens.add(screenName);

            Set<String> controllers = getKeysByValue(screensMap, screenName);

            cases.append("case \"")
                    .append(screenName)
                    .append("\":\n    return new ")
                    .append(resultType)
                    .append("[]{");

            for (String controllerName : controllers) {
                cases.append("\n            new ")
                        .append(controllerName)
                        .append("(),");
            }

            cases.append("\n    };\n");
        }
        cases.append("default:\n    return new ")
                .append(resultType)
                .append("[]{};");

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
        annotations.add(OnMenuItemClick.class.getCanonicalName());
        annotations.add(OnResult.class.getCanonicalName());
        annotations.add(OnBind.class.getCanonicalName());
        annotations.add(OnShow.class.getCanonicalName());
        annotations.add(OnHide.class.getCanonicalName());
        annotations.add(OnBackPressed.class.getCanonicalName());

        return annotations;
    }
}
