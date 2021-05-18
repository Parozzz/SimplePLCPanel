package parozzz.github.com.simpleplcpanel.util;
import parozzz.github.com.simpleplcpanel.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Paros
 */
public class ReflectionUtil
{
    public static <T> Constructor<T> getConstructor(final Class<T> clazz, final Class<?>... types)
    {
        try
        {
            var constructor = clazz.getDeclaredConstructor(types);
            constructor.trySetAccessible();
            return constructor;
        }
        catch(SecurityException | NoSuchMethodException ex)
        {
            var classNames = Stream.of(types).map(Class::getSimpleName).collect(Collectors.joining(","));
            throw new NullPointerException("Constructor with paramenters " + classNames + " does not exist in class " + clazz.getName());
        }
    }

    public static Field getField(final Class<?> clazz, final String name)
    {
        try
        {
            Field f = clazz.getDeclaredField(name);
            f.trySetAccessible();
            return f;
        }
        catch(NoSuchFieldException | SecurityException ex)
        {
            throw new NullPointerException("Field " + name + " does not exist in class " + clazz.getName());
        }
    }

    public static Method getMethod(final Class<?> clazz, final String name, final Class<?>... args)
    {
        try
        {
            var method = clazz.getDeclaredMethod(name, args);
            method.trySetAccessible();
            return method;
        }
        catch(NoSuchMethodException e)
        {
            var classNames = Stream.of(args).map(Class::getSimpleName).collect(Collectors.joining(","));
            throw new NullPointerException("Method " + name + " does not exist in class " + clazz.getName() + " with paramenters " + classNames);
        }
    }

    public static Class getClass(String name)
    {
        try
        {
            return Class.forName(name);
        }
        catch(ClassNotFoundException e)
        {
            throw new NullPointerException("Class named " + name + "does not exists.");
        }
    }

    public static boolean hasAnnotation(AnnotatedElement annotatedElement, Class<? extends Annotation> annotationClass)
    {
        try
        {
            return annotatedElement.getAnnotation(annotationClass) != null;
        }
        catch(Exception e)
        {
            return false;
        }
    }

    @Nullable
    public static <T> T getFieldValue(Field field, Object obj, Class<T> tClass)
    {
        try
        {
            var fieldValue = field.get(obj);
            return tClass.isInstance(fieldValue) ? tClass.cast(fieldValue) : null;
        }
        catch(IllegalAccessException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    public static <T> T invokeMethod(Method method, Object instance, Class<T> returnClass, Object... params)
    {
        try
        {
            var returnValue = method.invoke(instance, params);
            return returnClass.isInstance(returnValue) ? returnClass.cast(returnValue) : null;
        }
        catch(IllegalAccessException | InvocationTargetException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    public static void invokeMethod(Method method, Object instance, Object... params)
    {
        try
        {
            method.invoke(instance, params);
        }
        catch(IllegalAccessException | InvocationTargetException e)
        {
            e.printStackTrace();
        }
    }
}