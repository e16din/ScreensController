package com.e16din.sc.processor.code

import com.e16din.sc.annotations.OnResult
import com.e16din.sc.processor.ViewControllerProcessor
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import java.util.*
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement

object OnActivityResultGenerator {

    private val CLS_INTENT = ClassName("android.content", "Intent")

    private val requestCodesMap = HashMap<String, List<Int>>()// <controller name , request code>
    private val resultMethodsMap = HashMap<Int, String>()// <request code , result method>


    fun process(roundEnvironment: RoundEnvironment): FunSpec? {
        val elementsAnnotatedWithOnMenuItemClick = roundEnvironment.getElementsAnnotatedWith(OnResult::class.java)
        for (element in elementsAnnotatedWithOnMenuItemClick) {
            if (element.kind != ElementKind.METHOD) {
                return null
            }

            val execElement = element as ExecutableElement

            val annotation = element.getAnnotation(OnResult::class.java)

            val requestCode = annotation.requestCode

            val viewControllerName = execElement.enclosingElement.toString()
            var ids: MutableList<Int>? = requestCodesMap[viewControllerName] as MutableList<Int>?
            if (ids == null) {
                ids = ArrayList()
            }
            ids.add(requestCode)
            requestCodesMap[viewControllerName] = ids
            resultMethodsMap[requestCode] = execElement.simpleName.toString()
        }

        return FunSpec
                .builder("onActivityResult")
                .addModifiers(KModifier.PUBLIC)
                .addParameter("vc", Any::class)
                .addParameter("requestCode", Int::class)
                .addParameter("resultCode", Int::class)
                .addParameter("data", CLS_INTENT)
                .addCode(createCode())
                .build()
    }

    private fun createCode(): String {
        val viewControllers = requestCodesMap.keys
        val code = StringBuilder()
        code.append("if (vc != null) {\n")
        code.append("    var vcName = " + ViewControllerProcessor.SC_PACKAGE + ".Utils.getClassDefaultName(vc);\n")
        code.append("    when (vcName) {\n")

        for (vcName in viewControllers) {
            code.append("            \"")
                    .append(vcName)
                    .append("\"->\n")

            val ids = requestCodesMap[vcName]!!

            code.append("    when (requestCode) {\n")

            for (id in ids) {

                code.append("             ")
                        .append(Integer.toString(id))
                        .append("->\n")

                val methodName = resultMethodsMap[id]
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
                        .append(");\n")
            }
            code.append("            }\n") // switch
        }

        code.append("        }\n") // switch
        code.append("    }\n")// if

        return code.toString()
    }
}
