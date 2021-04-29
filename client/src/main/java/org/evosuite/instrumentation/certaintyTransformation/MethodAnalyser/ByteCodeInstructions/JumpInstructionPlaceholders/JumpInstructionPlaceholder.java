package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructionPlaceholders;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructions.JumpInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;
import org.objectweb.asm.Type;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public abstract class JumpInstructionPlaceholder extends ByteCodeInstruction {

    protected final JumpInstruction.JUMP_TYPE jumpType;

    public JumpInstructionPlaceholder(JumpInstruction.JUMP_TYPE jumpType, String className, String methodName, int lineNUmber,String methodDescriptor,
                                      int instructionNumber, int opcode) {
        super(className, methodName, lineNUmber, methodDescriptor, jumpType.toString(), instructionNumber, opcode);
        this.jumpType = jumpType;
    }

    @Override
    public StackTypeSet pushedToStack() {
        return StackTypeSet.of(Type.VOID);
    }

    public abstract JumpInstruction setDestination(ByteCodeInstruction instruction);

    @Override
    public List<Integer> getSuccessors() {
        throw new UnsupportedOperationException("Computation of Successor nodes on Placeholder nodes");
    }

    @Override
    public boolean writesVariable(int index){
        return false;
    }

    @Override
    public Set<Integer> readsVariables(){
        return Collections.emptySet();
    }
    @Override
    public Set<Integer> writesVariables(){
        return Collections.emptySet();
    }
}
