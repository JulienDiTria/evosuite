package org.evosuite.junit;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ImportHelper {

    //region comparators

    public static String classComparison(Class<?> klass){
        return klass.getCanonicalName();
    }

    public static String methodComparison(Method method){
        return method.getDeclaringClass().getTypeName() + '.' + method.getName();
    }

    public static String fieldComparison(Field field){
        return field.getDeclaringClass().getTypeName() + '.' + field.getName();
    }

    //endregion
}
