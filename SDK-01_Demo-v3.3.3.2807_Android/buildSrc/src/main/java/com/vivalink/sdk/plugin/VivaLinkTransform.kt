package com.vivalink.sdk.plugin

import com.android.build.api.instrumentation.*
import org.objectweb.asm.ClassVisitor

abstract class VivaLinkTransform: AsmClassVisitorFactory<InstrumentationParameters.None> {
    override fun createClassVisitor(classContext: ClassContext, nextClassVisitor: ClassVisitor): ClassVisitor {
        return VivaLinkClassVisitor(nextClassVisitor,classContext.currentClassData.className)
    }

    override fun isInstrumentable(classData: ClassData): Boolean {
        if(classData.className.endsWith(".R\$")
            || classData.className.endsWith(".R")
            || classData.className.endsWith(".BuildConfig")
        ) {
            return false
        }
        return (classData.className.contains("com.vivalnk.sdk"))
    }
}
