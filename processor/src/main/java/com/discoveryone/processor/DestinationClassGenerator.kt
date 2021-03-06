package com.discoveryone.processor

import com.discoveryone.destination.AbstractDestination
import com.discoveryone.destination.ActivityDestination
import com.discoveryone.destination.ActivityNavigationDestination
import com.discoveryone.destination.DestinationArgument
import com.discoveryone.destination.FragmentDestination
import com.discoveryone.destination.FragmentNavigationDestination
import com.discoveryone.destination.InternalDestinationArgumentMarker
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypeException
import kotlin.reflect.KClass

object DestinationClassGenerator {

    fun generateActivityDestinationClass(
        env: ProcessingEnvironment,
        typeElement: TypeElement
    ) {
        val annotation =
            typeElement.getAnnotation(ActivityNavigationDestination::class.java)
        val destinationClassName = annotation.name
        val packageName = typeElement.asClassName().packageName
        val arguments = annotation.arguments.toList()

        val classTypeSpec = typeElement
            .commonClassTypeBuilder(annotation.name, arguments, ActivityDestination::class)
            .build()

        FileSpec.builder(packageName, destinationClassName)
            .addType(classTypeSpec)
            .build()
            .writeTo(env.filer)
    }

    fun generateFragmentDestinationClass(
        env: ProcessingEnvironment,
        typeElement: TypeElement
    ) {
        val annotation =
            typeElement.getAnnotation(FragmentNavigationDestination::class.java)
        val arguments = annotation.arguments.toList()
        val destinationClassName = annotation.name
        val packageName = typeElement.asClassName().packageName
        val containerIdProperty =
            PropertySpec.builder("containerId", Int::class, KModifier.OVERRIDE)
                .initializer(annotation.containerId.toString())
                .build()

        val classTypeSpec = typeElement
            .commonClassTypeBuilder(annotation.name, arguments, FragmentDestination::class)
            .addProperty(containerIdProperty)
            .build()

        FileSpec.builder(packageName, "$destinationClassName.kt")
            .addType(classTypeSpec)
            .build()
            .writeTo(env.filer)
    }

    private fun TypeElement.commonClassTypeBuilder(
        destinationName: String,
        arguments: List<DestinationArgument>,
        destinationSupertype: KClass<out AbstractDestination>
    ): TypeSpec.Builder {
        val classProperty = PropertySpec.builder(
            "clazz",
            KClass::class.asClassName().parameterizedBy(STAR),
            KModifier.OVERRIDE
        )
            .initializer("${this.asClassName().simpleName}::class")
            .build()

        return if (arguments.isEmpty()) {
            TypeSpec.objectBuilder(destinationName)
                .addSuperinterface(destinationSupertype)
                .addProperty(classProperty)
        } else {
            val constructor = FunSpec.constructorBuilder().run {
                arguments.forEach { arg ->
                    addParameter(arg.name, arg.getArgumentTypeName())
                }
                build()
            }
            val properties = arguments.map { arg ->
                PropertySpec.builder(arg.name, arg.getArgumentTypeName()).initializer(arg.name)
                    .addAnnotation(InternalDestinationArgumentMarker::class)
                    .build()
            }
            return TypeSpec.classBuilder(destinationName)
                .addModifiers(KModifier.DATA)
                .addProperties(properties)
                .addSuperinterface(destinationSupertype)
                .addProperty(classProperty)
                .primaryConstructor(constructor)
        }
    }

    private fun DestinationArgument.getArgumentTypeName(): TypeName {
        return try {
            type.java.asTypeName().javaToKotlinType()
        } catch (mte: MirroredTypeException) {
            mte.typeMirror.asTypeName().javaToKotlinType()
        }
    }
}