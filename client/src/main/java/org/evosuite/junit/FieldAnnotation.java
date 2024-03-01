package org.evosuite.junit;

import org.evosuite.junit.writer.LineIndent;

public class FieldAnnotation {

    private String annotation;

    public FieldAnnotation(String annotation){
        this.annotation = annotation;
    }

    public String toCode(LineIndent lineIndent){
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(lineIndent).append("@").append(annotation);

        return stringBuilder.toString();
    }
}
