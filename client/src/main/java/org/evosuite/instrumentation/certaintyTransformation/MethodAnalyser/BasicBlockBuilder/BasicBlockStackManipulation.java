package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.BasicBlockBuilder;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.Results.Graph.ControlFlowGraph;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.Results.Variables.VariableTable;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.FrameLayout;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.TypeStack;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.TypeStackManipulation;

import java.util.ArrayList;
import java.util.List;

public class BasicBlockStackManipulation extends TypeStackManipulation {
    private BasicBlock owner;

    public BasicBlockStackManipulation(BasicBlock owner, ControlFlowGraph controlFlowGraph, VariableTable table) {
        this.owner = owner;
        List<ByteCodeInstruction> instructions = owner.getInstructions();
        List<TypeStackManipulation> manipulations = new ArrayList<>(instructions.size()-1);
        int bound = instructions.size() - 1;
        for (int i = 0; i < bound; i++) {
            ByteCodeInstruction from = instructions.get(i);
            ByteCodeInstruction to = instructions.get(i + 1);
            manipulations.add(from.getStackManipulation(table, to));
        }
    }

    @Override
    public TypeStack apply(TypeStack s) {
        return null;
    }

    @Override
    public TypeStack applyBackwards(TypeStack s) {
        return null;
    }

    @Override
    public FrameLayout apply(FrameLayout frameLayout) {
        return null;
    }

    @Override
    public FrameLayout applyBackwards(FrameLayout frameLayout) {
        return null;
    }

    @Override
    public FrameLayout computeMinimalBefore() {
        return null;
    }

    @Override
    public FrameLayout computeMinimalAfter() {
        return null;
    }
}
