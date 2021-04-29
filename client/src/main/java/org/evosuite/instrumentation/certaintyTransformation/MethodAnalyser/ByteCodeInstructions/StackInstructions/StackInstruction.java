package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.StackInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.Results.Variables.VariableTable;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.TypeStackManipulation;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public abstract class StackInstruction extends ByteCodeInstruction {
    public StackInstruction(String className, String methodName, int lineNumber,String methodDescriptor, String label, int instructionNumber,
                            int opcode) {
        super(className, methodName, lineNumber, methodDescriptor, label, instructionNumber, opcode);
    }

    @Override
    public List<StackTypeSet> consumedFromStack() {
        throw new UnsupportedOperationException("Consumed types on the Stack are dependent on the current stack");
    }

    @Override
    public StackTypeSet pushedToStack() {
        throw new UnsupportedOperationException("Pushed types on the Stack are dependent on the current stack");
    }

    @Override
    public abstract TypeStackManipulation getStackManipulation(VariableTable table,
                                                               ByteCodeInstruction instruction);

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
