package com.discoveryone.processor

import com.google.auto.common.BasicAnnotationProcessor
import javax.lang.model.SourceVersion

class DestinationProcessor : BasicAnnotationProcessor() {

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()

    override fun initSteps(): Iterable<ProcessingStep> {
        val env = processingEnv
        return listOf(
            ActivityDestinationGenerationProcessingStep(env),
            FragmentDestinationGenerationProcessingStep(env)
        )
    }
}
