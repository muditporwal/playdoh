package com.lsoftware.playdoh;

import com.lsoftware.playdoh.generator.TypeValueGenerator;
import com.lsoftware.playdoh.generator.ValueGeneratorFactory;
import com.lsoftware.playdoh.generator.ValueGeneratorFactoryImpl;
import com.lsoftware.playdoh.util.Primitives;
import com.lsoftware.playdoh.util.ReflectionUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

@SuppressWarnings("unchecked")
public final class ObjectBuilder {

    private static ObjectBuilder SOLE_INSTANCE;

    private static final ValueGeneratorFactory valueGeneratorFactory = new ValueGeneratorFactoryImpl();

    public static ObjectBuilder getInstance() {
        if (SOLE_INSTANCE == null) {
            SOLE_INSTANCE = new ObjectBuilder();
        }
        return SOLE_INSTANCE;
    }

    public <T> ObjectBuilder registerGenerator(Class<T> type, TypeValueGenerator<T> generator) {
        valueGeneratorFactory.register(type, generator);
        return this;
    }

    public <T> T build(Class<T> type) {
        if(valueGeneratorFactory.existsForType(type)) {
            final TypeValueGenerator typeGenerator = valueGeneratorFactory.getFromType(type);
            if(type.isArray()) {
                return (T) typeGenerator.generateArray();
            } else {
                return (T) typeGenerator.generate();
            }
        } else if(type.isEnum()) {
            return buildEnum(type);
        } else {
            Object o = createInstance(type);
            populateFields(o);
            return (T) o;
        }
    }

    private <T> T buildEnum(Class<T> type) {
        final T[] enumConstants = type.getEnumConstants();
        return enumConstants[new Random().nextInt(enumConstants.length)];
    }

    private Object createInstance(Class type) {
        try {
            Constructor constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Unable to instantiate object of type " + type.getName()
                    + " (Does the object have a default constructor?)");
        } catch (Exception e) {
            throw new RuntimeException("Unable to instantiate object of type " + type.getName(), e);
        }
    }

    private void populateFields(Object object) {
        final Class<?> clazz = object.getClass();
        for (Field field : ReflectionUtils.getFieldsUpHierarchy(object.getClass())) {
            final Method setter = findSetter(clazz, field);
            setter.setAccessible(true);
            useSetterToPopulateField(object, field, setter);
        }
    }

    private void useSetterToPopulateField(Object object, Field field, Method setter) {
        try {
            final Object value = build(field.getType());

            //Handle primitive arrays
            if(Primitives.isPrimitiveArray(field.getType())) {
                setPrimitiveArrayValue(object, field, setter, value);
            } else {
                setter.invoke(object, value);
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to set value to field " + field.getName()
                    + " on object of type " + object.getClass().getName(), e);
        }
    }

    private void setPrimitiveArrayValue(Object object, Field field, Method setter, Object value) throws IllegalAccessException, InvocationTargetException {
        if(field.getType().equals(byte[].class)) {
            setter.invoke(object, ArrayUtils.toPrimitive((Byte[]) value));
        } else if(field.getType().equals(short[].class)) {
            setter.invoke(object, ArrayUtils.toPrimitive((Short[]) value));
        } else if(field.getType().equals(int[].class)) {
            setter.invoke(object, ArrayUtils.toPrimitive((Integer[]) value));
        } else if(field.getType().equals(long[].class)) {
            setter.invoke(object, ArrayUtils.toPrimitive((Long[]) value));
        } else if(field.getType().equals(float[].class)) {
            setter.invoke(object, ArrayUtils.toPrimitive((Float[]) value));
        } else if(field.getType().equals(double[].class)) {
            setter.invoke(object, ArrayUtils.toPrimitive((Double[]) value));
        } else if(field.getType().equals(boolean[].class)) {
            setter.invoke(object, ArrayUtils.toPrimitive((Boolean[]) value));
        } else if(field.getType().equals(char[].class)) {
            setter.invoke(object, ArrayUtils.toPrimitive((Character[]) value));
        }
    }

    private Method findSetter(Class clazz, Field field) {
        final String fieldName = field.getName();
        for (Method method : ReflectionUtils.getMethodsUpHierarchy(clazz)) {
            if (method.getName().equalsIgnoreCase("set" + fieldName)) {
                return method;
            }
        }

        throw new IllegalStateException("Unable to find setter for field " + field.getName()
                + " on object of type " + clazz.getName());
    }


}