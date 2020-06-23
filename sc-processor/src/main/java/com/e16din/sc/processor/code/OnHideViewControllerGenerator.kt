package com.e16din.sc.processor.code

import com.e16din.sc.annotations.OnHide
import com.e16din.sc.processor.ViewControllerProcessor
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import java.util.*
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement

object OnHideViewControllerGenerator {

    private val methodsMap = HashMap<String, String>()// <controller name , result method>


    fun process(roundEnvironment: RoundEnvironment): FunSpec? {
        val elementsAnnotatedWithOnMenuItemClick = roundEnvironment.getElementsAnnotatedWith(OnHide::class.java)
        for (element in elementsAnnotatedWithOnMenuItemClick) {
            if (element.kind != ElementKind.METHOD) {
                return null
            }

            val execElement = element as ExecutableElement

            val viewControllerName = execElement.enclosingElement.toString()
            val methodName = execElement.simpleName.toString()

            methodsMap[viewControllerName] = methodName
        }

        return FunSpec
                .builder("onHideViewController")
                .addModifiers(KModifier.OVERRIDE, KModifier.PUBLIC)
                .addParameter("vc", Any::class)
                .addCode(createCode())
                .build()

    }

    private fun createCode(): String {
        val viewControllers = methodsMap.keys

        val code = StringBuilder()

        code.append("    var vcName = " + ViewControllerProcessor.SC_PACKAGE + ".Utils.getClassDefaultName(vc);\n")
        code.append("    when (vcName) {\n")

        for (vcName in viewControllers) {
            code.append("            \"")
                    .append(vcName)
                    .append("\"->\n")
                    .append("(vc as $vcName).")
                    .append(methodsMap[vcName])
                    .append("();\n")
        }

        code.append("}\n")

        return code.toString()
    }
}
