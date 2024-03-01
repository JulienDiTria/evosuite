package org.evosuite.junit;

import java.util.List;
import org.evosuite.junit.writer.LineIndent;

import static org.evosuite.junit.writer.TestSuiteWriterUtils.NEWLINE;
import static org.evosuite.junit.writer.TestSuiteWriterUtils.addLine;

public class ClassField {

    private final Class<?> type;
    private final List<FieldAnnotation> annotations;

    private LineIndent lineIndent = new LineIndent();

    public ClassField(Class<?> type, List<FieldAnnotation> annotations) {
        this.type = type;
        this.annotations = annotations;
    }

    public String toCode(LineIndent lineIndent) {
        this.lineIndent = lineIndent;
        StringBuilder stringBuilder = new StringBuilder();

        addAnnotations(stringBuilder);
        addDefinition(stringBuilder);

        return stringBuilder.toString();
    }

    private void addAnnotations(StringBuilder stringBuilder){
        if(!annotations.isEmpty()){
            for (FieldAnnotation annotation:annotations){
                addLine(stringBuilder, lineIndent, annotation.toCode(lineIndent));
            }
        }
    }

    private void addDefinition(StringBuilder stringBuilder){
        String name = type.getSimpleName();
        String varName = name.substring(0, 1).toLowerCase() + name.substring(1);
        stringBuilder.append(lineIndent).append("private ").append(name).append(" ").append(varName).append(";").append(NEWLINE);
    }
}
