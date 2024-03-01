package org.evosuite.junit;

import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class ClassExtensions {

    private SortedSet<Class<?>> superClasses = new TreeSet<>(Comparator.comparing(Class::getCanonicalName));

    public ClassExtensions() {

    }

    public String toCode() {
        StringBuilder stringBuilder = new StringBuilder();

        if(!superClasses.isEmpty()) {
            String concatenated = superClasses.stream().map(Class::getName).collect(Collectors.joining(", "));
            stringBuilder.append(" ").append(concatenated);
        }

        return stringBuilder.toString();
    }
}
