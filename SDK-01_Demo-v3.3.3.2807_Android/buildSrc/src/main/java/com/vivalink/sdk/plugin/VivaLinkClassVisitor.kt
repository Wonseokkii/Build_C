package com.vivalink.sdk.plugin

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.AdviceAdapter

class VivaLinkClassVisitor(nextVisitor: ClassVisitor, private val className: String) :
    ClassVisitor(Opcodes.ASM7, nextVisitor) {
    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        val methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions)
        val newMethodVisitor =
            object : AdviceAdapter(Opcodes.ASM7, methodVisitor, access, name, descriptor) {

                @Override
                override fun onMethodEnter() {
                    // 方法开始
                    if (isNeedVisitorMethod() && descriptor != null) {
                        val parametersIdentifier = VivaLinkUtil.newParameterArrayList(mv, this)
                        VivaLinkUtil.fillParameterArray(
                            methodDesc, mv, parametersIdentifier, access
                        )
                        VivaLinkUtil.onMethodEnter(mv, className, name, parametersIdentifier)
                    }
                    super.onMethodEnter()
                }

                @Override
                override fun onMethodExit(opcode: Int) {
                    // 方法结束
                    if (isNeedVisitorMethod()) {
                        if ((opcode in IRETURN..RETURN) || opcode == ATHROW) {
                            when (opcode) {
                                in IRETURN..DRETURN -> {
                                    VivaLinkUtil.loadReturnData(mv, methodDesc)
                                    VivaLinkUtil.onMethodExit(mv, className, name, methodDesc)
                                }
                                ARETURN -> {
                                    mv.visitInsn(DUP)
                                    VivaLinkUtil.onMethodExit(mv, className, name, methodDesc)
                                }
                                RETURN -> {
                                    mv.visitLdcInsn("void")
                                    VivaLinkUtil.onMethodExit(mv, className, name, methodDesc)
                                }
                                else -> {
                                }
                            }
                        }
                    }
                    super.onMethodExit(opcode);
                }
            }
        return newMethodVisitor
    }

    private fun isNeedVisitorMethod(): Boolean {
        //TODO
        return false
    }
}