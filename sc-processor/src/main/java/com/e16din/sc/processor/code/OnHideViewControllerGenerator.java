package com.e16din.sc.processor.code;

import com.e16din.sc.annotations.OnHide;
import com.squareup.javapoet.MethodSpec;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;

public class OnHideViewControllerGenerator {

    private static Map<String, String> methodsMap = new HashMap<>();// <controller name , result method>


    public static MethodSpec process(RoundEnvironment roundEnvironment) {
        Set<? extends Element> elementsAnnotatedWithOnMenuItemClick =
                roundEnvironment.getElementsAnnotatedWith(OnHide.class);
        for (Element element : elementsAnnotatedWithOnMenuItemClick) {
            if (element.getKind() != ElementKind.METHOD) {
                return null;
            }

            ExecutableElement execElement = (ExecutableElement) element;

            final String viewControllerName = execElement.getEnclosingElement().toString();
            final String methodName = execElement.getSimpleName().toString();

            methodsMap.put(viewControllerName, methodName);
        }

        return MethodSpec
                .methodBuilder("onHideViewController")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Object.class, "vc")
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
                    .append("();\n")
                    .append("break;\n\n");
        }

        code.append("}\n");


        return code.toString();
    }
}
