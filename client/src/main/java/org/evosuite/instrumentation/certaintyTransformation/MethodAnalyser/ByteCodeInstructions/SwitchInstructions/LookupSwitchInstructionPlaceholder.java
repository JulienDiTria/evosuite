package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.SwitchInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;

import java.util.Collections;
import java.util.List;

import static org.objectweb.asm.Opcodes.LOOKUPSWITCH;

public class LookupSwitchInstructionPlaceholder extends SwitchInstructionPlaceholder {
    private final int[] keys;

    public LookupSwitchInstructionPlaceholder(String className, String methodName, int lineNumber,String methodDescriptor,
                                              int instructionNumber, int[] keys) {
        super(className, methodName, lineNumber, methodDescriptor,"LOOKUPSWITCH", instructionNumber, LOOKUPSWITCH);
        this.keys = keys;
    }

    @Override
    public LookupSwitchInstruction setDestinations(ByteCodeInstruction def, List<ByteCodeInstruction> destinations) {
        return new LookupSwitchInstruction(className, methodName, lineNumber,methodDescriptor, instructionNumber, keys, def, destinations);
    }

    @Override
    public List<StackTypeSet> consumedFromStack() {
        return Collections.singletonList(StackTypeSet.INT);
    }

    @Override
    public StackTypeSet pushedToStack() {
        return StackTypeSet.VOID;
    }
}
