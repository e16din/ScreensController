package com.e16din.sc.processor.code

import com.e16din.sc.annotations.OnBind
import com.e16din.sc.processor.ViewControllerProcessor
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.asTypeName
import java.util.*
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.type.MirroredTypeException

object OnBindViewControllerGenerator {

    private val CLS_VIEW = ClassName("android.view", "View")

    private val methodsMap = HashMap<String, String>()// <controller name , result method>
    private val dataMap = HashMap<String, String>()// <controller name , data name>


    fun process(roundEnvironment: RoundEnvironment): FunSpec? {
        val elementsAnnotatedWithOnMenuItemClick = roundEnvironment.getElementsAnnotatedWith(OnBind::class.java)
        for (element in elementsAnnotatedWithOnMenuItemClick) {
            if (element.kind != ElementKind.METHOD) {
                return null
            }

            val execElement = element as ExecutableElement

            val annotation = element.getAnnotation(OnBind::class.java)

            val viewControllerName = execElement.enclosingElement.toString()
            val methodName = execElement.simpleName.toString()

            methodsMap[viewControllerName] = methodName

            var dataName: String
            try {
                dataName = annotation.dataType.qualifiedName!!
            } catch (mte: MirroredTypeException) {
                dataName = mte.typeMirror.toString()
            }

            dataMap[viewControllerName] = if (dataName == "java.lang.Object") "Any?" else dataName
        }

        return FunSpec
                .builder("onBindViewController")
                .addModifiers(KModifier.OVERRIDE, KModifier.PUBLIC)
                .addParameter("vc", Any::class)
                .addParameter("v", CLS_VIEW)
                .addParameter("data", Any::class.asTypeName().copy(nullable = true))
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
                    .append("(this, v, data as ${dataMap[vcName]})")
        }

        code.append("}\n")

        return code.toString()
    }
}
