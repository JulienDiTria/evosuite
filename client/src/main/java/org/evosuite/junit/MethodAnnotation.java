package org.evosuite.junit;

public class MethodAnnotation implements Comparable<MethodAnnotation> {

    private final String annotation;

    public MethodAnnotation(String annotation){
        this.annotation = annotation;
    }

    @Override
    public int compareTo(MethodAnnotation o) {
        return annotation.compareTo(o.annotation);
    }

    public String toCode() {
        return annotation;
    }
}
