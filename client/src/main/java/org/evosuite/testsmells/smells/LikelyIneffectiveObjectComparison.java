package org.evosuite.testsmells.smells;

import org.evosuite.Properties;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.Statement;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testsmells.AbstractNormalizedTestCaseSmell;

import java.util.List;

public class LikelyIneffectiveObjectComparison extends AbstractNormalizedTestCaseSmell {

    public LikelyIneffectiveObjectComparison() {
        super("TestSmellLikelyIneffectiveObjectComparison");
    }

    @Override
    public long computeNumberOfTestSmells(TestChromosome chromosome) {
        int size = chromosome.size();
        long count = 0;

        Statement currentStatement;

        for (int i = 0; i < size; i++){
            currentStatement = chromosome.getTestCase().getStatement(i);

            if(currentStatement instanceof MethodStatement){

                String curr = ((MethodStatement) currentStatement).getMethodName();
                String className = ((MethodStatement) currentStatement).getDeclaringClassName();
                VariableReference callee = ((MethodStatement) currentStatement).getCallee();

                if(curr.equals("equals") && !className.equals(Properties.TARGET_CLASS) && callee != null){
                    List<VariableReference> parameters = ((MethodStatement) currentStatement).getParameterReferences();

                    if(parameters.size() == 1 && callee.getStPosition() == parameters.get(0).getStPosition()){
                        count++;
                    }
                }
            }
        }

        return count;
    }
}
