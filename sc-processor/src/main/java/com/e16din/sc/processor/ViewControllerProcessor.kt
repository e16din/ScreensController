package com.e16din.sc.processor

import com.e16din.sc.annotations.*
import com.e16din.sc.processor.ViewControllerProcessor.Companion.KAPT_KOTLIN_GENERATED_OPTION_NAME
import com.e16din.sc.processor.code.*
import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import java.io.File
import java.util.*
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypeException
import javax.tools.Diagnostic

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("*")
@SupportedOptions(KAPT_KOTLIN_GENERATED_OPTION_NAME)
@AutoService(Processor::class)
class ViewControllerProcessor : AbstractProcessor() {

    companion object {

        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"

        val SC_PACKAGE = "com.e16din.sc"

        private val CLS_INTENT = ClassName("android.content", "Intent")
        private val CLS_APPLICATION = ClassName("android.app", "Application")

        val CLS_SCREENS_CONTROLLER = ClassName(SC_PACKAGE, "ScreensController")

        private val PARAM_SCREEN_NAME = "screenName"
    }

    private var filer: Filer? = null
    private var messager: Messager? = null

    private val screens = ArrayList<ScreenContainer>()

    private var generated = false


    override fun init(processingEnvironment: ProcessingEnvironment) {
        super.init(processingEnvironment)
        filer = processingEnvironment.filer
        messager = processingEnvironment.messager
    }

    override fun process(set: Set<TypeElement>, roundEnvironment: RoundEnvironment): Boolean {
        if (generated) {
            return false
        }

        val elementsAnnotatedWithViewControllers = roundEnvironment.getElementsAnnotatedWith(ViewControllers::class.java)

        for (element in elementsAnnotatedWithViewControllers) {
            val typeElement = element as TypeElement
            val controllerName = typeElement.qualifiedName.toString()

            print("controllerName: $controllerName")

            val annotation = element.getAnnotation(ViewControllers::class.java)

            try {
                val controllers = annotation.value
                print("screenName size: " + controllers.size)
                for (controller in controllers) {
                    print("controller: $controller")
                    addScreenToMap(controllerName, controller)
                }

            } catch (mte: MirroredTypeException) {
                val screenName = mte.typeMirror.toString()
                print("screenName2: $screenName")
            }
        }

        val elementsAnnotatedWithViewController = roundEnvironment.getElementsAnnotatedWith(ViewController::class.java)

        for (element in elementsAnnotatedWithViewController) {
            if (element.kind != ElementKind.CLASS) {
                printError("Can be applied to class.")
                return true
            }

            val typeElement = element as TypeElement
            val controllerName = typeElement.qualifiedName.toString()

            val annotation = element.getAnnotation(ViewController::class.java)
            addScreenToMap(controllerName, annotation)
        }

        val onMenuItemClickMethod = OnMenuItemClickGenerator.process(roundEnvironment)

        val onActivityResultMethod = OnActivityResultGenerator.process(roundEnvironment)

        val onBindViewControllerMethod = OnBindViewControllerGenerator.process(roundEnvironment)
        val onShowViewControllerMethod = OnShowViewControllerGenerator.process(roundEnvironment)
        val onHideViewControllerMethod = OnHideViewControllerGenerator.process(roundEnvironment)


        val superResultCode = "    for (vc in currentViewControllers) {\n" +
                "        onActivityResult(vc, requestCode, resultCode, data)\n" +
                "    }\n" +
                "    for (vc in startViewControllers) {\n" +
                "        onActivityResult(vc, requestCode, resultCode, data)\n" +
                "    }\n"

        val onSuperResultMethod = FunSpec
                .builder("onActivityResult")
                .addModifiers(KModifier.OVERRIDE, KModifier.PUBLIC)
                .addParameter("requestCode", Int::class)
                .addParameter("resultCode", Int::class)
                .addParameter("data", CLS_INTENT)
                .addCode(superResultCode)
                .build()

        val buildViewControllersMethod = FunSpec
                .builder("buildViewControllers")
                .addModifiers(KModifier.OVERRIDE, KModifier.PUBLIC)
                .returns(ClassName("kotlin", "Array").parameterizedBy(Any::class.asTypeName()))
                .addParameter("screenName", String::class)
                .addStatement(createCode())
                .build()

        val isStartOnceControllerMethod = FunSpec
                .builder("once")
                .addModifiers(KModifier.OVERRIDE, KModifier.PUBLIC)
                .returns(Boolean::class)
                .addParameter("vcName", String::class)
                .addCode(createIsStartOnceCode())
                .build()

        val appParamName = "app"
        val constructor = FunSpec.constructorBuilder()
                .addParameter(appParamName, CLS_APPLICATION)
                .build()

        val controllersBuilderClass = TypeSpec
                .classBuilder("GeneratedScreensController")
                .superclass(CLS_SCREENS_CONTROLLER)
                .addModifiers(KModifier.PUBLIC)
                .addFunction(buildViewControllersMethod)
                .addFunction(onBindViewControllerMethod!!)
                .addFunction(onShowViewControllerMethod!!)
                .addFunction(onHideViewControllerMethod!!)
                .addFunction(onMenuItemClickMethod!!)
                .addFunction(isStartOnceControllerMethod)
                .addFunction(onSuperResultMethod)
                .addFunction(onActivityResultMethod!!)
                .primaryConstructor(constructor)
                .addSuperclassConstructorParameter(appParamName)
                .build()

        val fileName = "GeneratedScreensController"

        val generatedSourcesRoot = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
                .orEmpty()
        if (generatedSourcesRoot.isEmpty()) {
//            "Can't find the target directory for generated Kotlin files."
//                    .printError()
            return false
        }

        val file = File(generatedSourcesRoot).apply { mkdir() }

        FileSpec.builder(SC_PACKAGE, fileName)
                .addType(controllersBuilderClass)
                .build()
                .writeTo(file)

        generated = true
        return true
    }

    private fun addScreenToMap(controllerName: String, annotation: ViewController) {
        var screenName: String
        try {
            screenName = annotation.screen.qualifiedName!!
            print("screenName0: $screenName")
        } catch (mte: MirroredTypeException) {
            screenName = mte.typeMirror.toString()
            print("screenName3: $screenName")
        }

        var startOnce: Boolean
        try {
            startOnce = annotation.startOnce
            print("startOnce1: $startOnce")
        } catch (mte: MirroredTypeException) {
            startOnce = java.lang.Boolean.getBoolean(mte.typeMirror.toString())
            print("startOnce1: $startOnce")
        }

        var screen: ScreenContainer? = null

        for (s in screens) {
            if (s.name == screenName) {
                screen = s
                break
            }
        }

        val controller = ControllerContainer(controllerName, startOnce)

        if (screen == null) {
            screen = ScreenContainer(screenName, controller)
            screens.add(screen)
        } else {
            screen.controllers.add(controller)
        }
    }

    private fun printError(message: String) {
        messager!!.printMessage(Diagnostic.Kind.ERROR, message)
    }

    private fun print(message: String) {
        messager!!.printMessage(Diagnostic.Kind.WARNING, message)
    }


    private fun createIsStartOnceCode(): String {
        val code = StringBuilder("when(vcName) {")

        val startOnceControllers = HashSet<String>()
        for (screen in screens) {
            for (controller in screen.controllers) {
                if (controller.isStartOnce) {
                    startOnceControllers.add(controller.name!!)
                }
            }
        }
        for (name in startOnceControllers) {
            code.append("\n    " + "\"").append(name).append("\"-> return true")
        }

        code.append("\n}\n\n" + "return false\n")

        return code.toString()
    }

    private fun createCode(): String {
        val cases = StringBuilder()

        val resultType = "arrayOf"

        for (screen in screens) {
            val screenName = screen.name
            val controllers = screen.controllers

            cases.append("\"")
                    .append(screenName)
                    .append("\"->\n    ")
                    .append(resultType)
                    .append("(")

            controllers.forEachIndexed { index, controller ->
                cases.append("\n            ")
                        .append(controller.name)
                        .append("()")

                if (index < controllers.size - 1) {
                    cases.append(",")
                }
            }

            cases.append("\n    )\n")
        }
        cases.append("else ->\n    ")
                .append(resultType)
                .append("()")

        return "return when (" + PARAM_SCREEN_NAME + ") {\n" +
                cases +
                "}"
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    override fun getSupportedAnnotationTypes(): Set<String> {
        val annotations = HashSet<String>()
        annotations.add(ViewController::class.java.canonicalName)
        annotations.add(ViewControllers::class.java.canonicalName)
        annotations.add(OnMenuItemClick::class.java.canonicalName)
        annotations.add(OnResult::class.java.canonicalName)
        annotations.add(OnBind::class.java.canonicalName)
        annotations.add(OnShow::class.java.canonicalName)
        annotations.add(OnHide::class.java.canonicalName)
        annotations.add(OnBackPressed::class.java.canonicalName)

        return annotations
    }
}
