package com.e16din.sc.processor.code;

import com.e16din.sc.annotations.OnResult;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;

public class OnActivityResultGenerator {

    private static final ClassName CLS_INTENT = ClassName.get("android.content", "Intent");

    private static Map<String, List<Integer>> requestCodesMap = new HashMap<>();// <controller name , request code>
    private static Map<Integer, String> resultMethodsMap = new HashMap<>();// <request code , result method>


    public static MethodSpec process(RoundEnvironment roundEnvironment) {
        Set<? extends Element> elementsAnnotatedWithOnMenuItemClick =
                roundEnvironment.getElementsAnnotatedWith(OnResult.class);
        for (Element element : elementsAnnotatedWithOnMenuItemClick) {
            if (element.getKind() != ElementKind.METHOD) {
                return null;
            }

            ExecutableElement execElement = (ExecutableElement) element;

            OnResult annotation = element.getAnnotation(OnResult.class);

            int requestCode = annotation.requestCode();

            final String viewControllerName = execElement.getEnclosingElement().toString();
            List<Integer> ids = requestCodesMap.get(viewControllerName);
            if (ids == null) {
                ids = new ArrayList<>();
            }
            ids.add(requestCode);
            requestCodesMap.put(viewControllerName, ids);
            resultMethodsMap.put(requestCode, execElement.getSimpleName().toString());
        }

        return MethodSpec
                .methodBuilder("onActivityResult")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Object.class, "vc")
                .addParameter(TypeName.INT, "requestCode")
                .addParameter(TypeName.INT, "resultCode")
                .addParameter(CLS_INTENT, "data")
                .addCode(createCode())
                .build();
    }

    private static String createCode() {
        Set<String> viewControllers = requestCodesMap.keySet();
        StringBuilder code = new StringBuilder();
        code.append("if (vc != null) {\n");
        code.append("    final String vcName = com.e16din.sc.Utils.getClassDefaultName(vc);\n");
        code.append("    switch (vcName) {\n");

        for (String vcName : viewControllers) {
            code.append("            case \"")
                    .append(vcName)
                    .append("\":\n");

            List<Integer> ids = requestCodesMap.get(vcName);

            code.append("    switch (requestCode) {\n");

            for (int id : ids) {

                code.append("            case ")
                        .append(Integer.toString(id))
                        .append(":\n");

                String methodName = resultMethodsMap.get(id);
                code.append("            (")
                        .append("(")
                        .append(vcName)
                        .append(")")
                        .append("\n            getViewController(\"")
                        .append(vcName)
                        .append("\"))")
                        .append("\n            .")
                        .append(methodName)
                        .append("(")
                        .append("requestCode, resultCode, data")
                        .append(");\n");
                code.append("            break;\n");
            }
            code.append("            }\n"); // switch
            code.append("            break;\n");
        }

        code.append("        }\n"); // switch
        code.append("    }\n");// if

        return code.toString();
    }
}
