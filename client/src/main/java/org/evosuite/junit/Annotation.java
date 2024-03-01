package org.evosuite.junit;

import javax.sound.sampled.Line;
import org.evosuite.junit.writer.LineIndent;

import static org.evosuite.junit.writer.TestSuiteWriterUtils.NEWLINE;
import static org.evosuite.junit.writer.TestSuiteWriterUtils.addLine;

public class Annotation implements Comparable<Annotation> {

    private final String annotation;
    private LineIndent lineIndent;

    public Annotation(String annotation) {
        this(annotation, new LineIndent());
    }

    public Annotation(String annotation, LineIndent lineIndent){
        this.annotation = annotation;
        this.lineIndent = lineIndent;
    }

    @Override
    public int compareTo(Annotation o) {
        return annotation.compareTo(o.annotation);
    }

    public String toCode(LineIndent lineIndent) {
        this.lineIndent = lineIndent;
        StringBuilder stringBuilder = new StringBuilder();
        for(String line : annotation.split(NEWLINE)){
            addLine(stringBuilder, lineIndent, line);
        }

        return stringBuilder.toString();
    }
}
