package com.e16din.sc.processor.code;

import com.e16din.sc.annotations.OnMenuItemClick;
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

public class OnMenuItemClickGenerator {

    private static final ClassName CLS_MENU_ITEM = ClassName.get("android.view", "MenuItem");


    private static Map<String, List<Integer>> menuItemsMap = new HashMap<>();// <controller name , menu item id>
    private static Map<Integer, String> menuMethodsMap = new HashMap<>();// <menu item id , menu method>


    public static MethodSpec process(RoundEnvironment roundEnvironment) {
        Set<? extends Element> elementsAnnotatedWithOnMenuItemClick =
                roundEnvironment.getElementsAnnotatedWith(OnMenuItemClick.class);
        for (Element element : elementsAnnotatedWithOnMenuItemClick) {
            if (element.getKind() != ElementKind.METHOD) {
                return null;
            }

            ExecutableElement execElement = (ExecutableElement) element;

            OnMenuItemClick annotation = element.getAnnotation(OnMenuItemClick.class);

            int menuItemId = annotation.itemId();

            final String viewControllerName = execElement.getEnclosingElement().toString();
            List<Integer> ids = menuItemsMap.get(viewControllerName);
            if (ids == null) {
                ids = new ArrayList<>();
            }
            ids.add(menuItemId);
            menuItemsMap.put(viewControllerName, ids);
            menuMethodsMap.put(menuItemId, execElement.getSimpleName().toString());
        }

        return MethodSpec
                .methodBuilder("onMenuItemClick")
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.BOOLEAN)
                .addParameter(Object.class, "vc")
                .addParameter(CLS_MENU_ITEM, "item")
                .addCode(createOnMenuItemClickCode())
                .build();
    }

    private static String createOnMenuItemClickCode() {
        Set<String> viewControllers = menuItemsMap.keySet();
        StringBuilder code = new StringBuilder();
        code.append("if (vc != null) {\n");
        code.append("    final String vcName = com.e16din.sc.Utils.getClassDefaultName(vc);\n");
        code.append("    switch (vcName) {\n");

        for (String vcName : viewControllers) {
            code.append("            case \"")
                    .append(vcName)
                    .append("\":\n");

            List<Integer> ids = menuItemsMap.get(vcName);

            code.append("    switch (item.getItemId()) {\n");

            for (int id : ids) {

                code.append("            case ")
                        .append(Integer.toString(id))
                        .append(":\n");

                String methodName = menuMethodsMap.get(id);
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
                        .append("item")
                        .append(");\n");
                code.append("            break;\n");
            }
            code.append("            }\n"); // switch
            code.append("            break;\n");
        }

        code.append("        }\n"); // switch
        code.append("    }\n");// if

        code.append("\nreturn super.onMenuItemClick(vc, item);\n ");
        return code.toString();
    }
}
