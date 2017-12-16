package com.e16din.sc.processor.code;

import com.e16din.sc.annotations.OnBind;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.MirroredTypeException;

public class OnBindViewControllerGenerator {

    private static final ClassName CLS_VIEW = ClassName.get("android.view", "View");

    private static Map<String, String> methodsMap = new HashMap<>();// <controller name , result method>
    private static Map<String, String> dataMap = new HashMap<>();// <controller name , data name>


    public static MethodSpec process(RoundEnvironment roundEnvironment) {
        Set<? extends Element> elementsAnnotatedWithOnMenuItemClick =
                roundEnvironment.getElementsAnnotatedWith(OnBind.class);
        for (Element element : elementsAnnotatedWithOnMenuItemClick) {
            if (element.getKind() != ElementKind.METHOD) {
                return null;
            }

            ExecutableElement execElement = (ExecutableElement) element;

            OnBind annotation = element.getAnnotation(OnBind.class);

            final String viewControllerName = execElement.getEnclosingElement().toString();
            final String methodName = execElement.getSimpleName().toString();

            methodsMap.put(viewControllerName, methodName);

            String dataName;
            try {
                dataName = annotation.dataType().getName();
            } catch (MirroredTypeException mte) {
                dataName = mte.getTypeMirror().toString();
            }

            dataMap.put(viewControllerName, dataName);
        }

        return MethodSpec
                .methodBuilder("onBindViewController")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Object.class, "vc")
                .addParameter(CLS_VIEW, "v")
                .addParameter(Object.class, "data")
                .addCode(createCode())
                .build();

    }

    private static String createCode() {
        Set<String> viewControllers = methodsMap.keySet();

        StringBuilder code = new StringBuilder();

        code.append("    final String vcName = com.e16din.sc.Utils.getClassDefaultName(vc);\n");
        code.append("    switch (vcName) {\n");


        for (String vcName : viewControllers) {
            code.append("            case \"")
                    .append(vcName)
                    .append("\":\n")
                    .append("((")
                    .append(vcName)
                    .append(") vc).")
                    .append(methodsMap.get(vcName))
                    .append("(this, v, (")
                    .append(dataMap.get(vcName))
                    .append(") data);\n")
                    .append("break;\n\n");
        }

        code.append("}\n");


        return code.toString();
    }
}
