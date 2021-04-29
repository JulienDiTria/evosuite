package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.InvokeInstructions;

import static org.objectweb.asm.Opcodes.INVOKESPECIAL;

public class InvokeSpecialInstruction extends InvokeInstruction {

    public InvokeSpecialInstruction(String className, String methodName, int line,String methodDescriptor, String owner, String name,
                                    String descriptor, int instructionNumber) {
        super(className, methodName, line,methodDescriptor, INVOKATION_TYPE.INVOKESPECIAL, owner, name, descriptor,
                instructionNumber, INVOKESPECIAL);
    }
}
