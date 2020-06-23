package com.e16din.sc.processor.code

import com.e16din.sc.annotations.OnMenuItemClick
import com.e16din.sc.processor.ViewControllerProcessor.Companion.SC_PACKAGE
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import java.util.*
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement

object OnMenuItemClickGenerator {

    private val CLS_MENU_ITEM = ClassName("android.view", "MenuItem")


    private val menuItemsMap = HashMap<String, List<Int>>()// <controller name , menu item id>
    private val menuMethodsMap = HashMap<Int, String>()// <menu item id , menu method>


    fun process(roundEnvironment: RoundEnvironment): FunSpec? {
        val elementsAnnotatedWithOnMenuItemClick = roundEnvironment.getElementsAnnotatedWith(OnMenuItemClick::class.java)
        for (element in elementsAnnotatedWithOnMenuItemClick) {
            if (element.kind != ElementKind.METHOD) {
                return null
            }

            val execElement = element as ExecutableElement

            val annotation = element.getAnnotation(OnMenuItemClick::class.java)

            val menuItemId = annotation.itemId

            val viewControllerName = execElement.enclosingElement.toString()
            var ids: MutableList<Int>? = menuItemsMap[viewControllerName] as MutableList<Int>?
            if (ids == null) {
                ids = ArrayList()
            }
            ids.add(menuItemId)
            menuItemsMap[viewControllerName] = ids
            menuMethodsMap[menuItemId] = execElement.simpleName.toString()
        }

        return FunSpec
                .builder("onMenuItemClick")
                .addModifiers(KModifier.OVERRIDE, KModifier.PUBLIC)
                .returns(Boolean::class)
                .addParameter("vc", Any::class)
                .addParameter("item", CLS_MENU_ITEM)
                .addCode(createOnMenuItemClickCode())
                .build()
    }

    private fun createOnMenuItemClickCode(): String {
        val viewControllers = menuItemsMap.keys
        val code = StringBuilder()
        code.append("if (vc != null) {\n")
        code.append("    var vcName = $SC_PACKAGE.Utils.getClassDefaultName(vc);\n")
        code.append("    when (vcName) {\n")

        for (vcName in viewControllers) {
            code.append("            \"")
                    .append(vcName)
                    .append("\"->\n")

            val ids = menuItemsMap[vcName]!!

            code.append("    when (item.getItemId()) {\n")

            for (id in ids) {

                code.append("            ")
                        .append(Integer.toString(id))
                        .append("->\n")

                val methodName = menuMethodsMap[id]
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
                        .append(");\n")
            }
            code.append("            }\n") // switch
        }

        code.append("        }\n") // switch
        code.append("    }\n")// if

        code.append("\nreturn super.onMenuItemClick(vc, item);\n ")
        return code.toString()
    }
}
