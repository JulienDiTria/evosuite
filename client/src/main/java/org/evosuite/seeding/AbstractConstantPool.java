package org.evosuite.seeding;

import org.evosuite.utils.Randomness;
import org.evosuite.utils.generic.GenericClass;

/**
 * Abstract class for constant pool, implementing getRandomValue for other constant pool classes to use
 *
 * @see DynamicConstantPool
 * @see StaticConstantPool
 */
public abstract class AbstractConstantPool implements ConstantPool {

    @Override
    public Object getRandomValue(GenericClass<?> klass){

        Class<?> clazz = klass.getRawClass();

        if (clazz == boolean.class) {
            return Randomness.nextBoolean();
        } else if (clazz == int.class) {
            return getRandomInt();
        } else if (clazz == char.class) {
            return Randomness.nextChar();
        } else if (clazz == long.class) {
            return getRandomLong();
        } else if (clazz.equals(double.class)) {
            return getRandomDouble();
        } else if (clazz == float.class) {
            return getRandomFloat();
        } else if (clazz == short.class) {
            return (short) Randomness.nextInt();
        } else if (clazz == byte.class) {
            return (byte) Randomness.nextInt();
        } else if (clazz.equals(String.class)) {
            return getRandomString();
        } else {
            throw new IllegalArgumentException("Unsupported type: " + clazz);
        }
    }

}
