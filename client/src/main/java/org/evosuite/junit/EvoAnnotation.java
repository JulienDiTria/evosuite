package org.evosuite.junit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.evosuite.junit.writer.LineIndent;

import static org.evosuite.junit.writer.TestSuiteWriterUtils.addLine;

public class EvoAnnotation implements Comparable<EvoAnnotation> {

    private final List<String> lines = new ArrayList<>();
    private LineIndent lineIndent;

    public EvoAnnotation(String oneLineAnnotation){
        lines.add(oneLineAnnotation);
    }

    public EvoAnnotation(List<String> lines){
        this.lines.addAll(lines);
    }

    public void add(String line){
        lines.add(line);
    }

    @Override
    public int compareTo(EvoAnnotation o) {
        return lines.toString().compareTo(o.lines.toString());
    }

    public String toCode() {
        return toCode(lineIndent);
    }

    public String toCode(LineIndent lineIndent) {
        this.lineIndent = lineIndent;
        StringBuilder stringBuilder = new StringBuilder();
        for(String line : lines){
            addLine(stringBuilder, lineIndent, line);
        }

        return stringBuilder.toString();
    }
}
