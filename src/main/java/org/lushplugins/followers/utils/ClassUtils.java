package org.lushplugins.followers.utils;

public class ClassUtils {

    public static boolean areAnyAssignable(Class<?> clazz, Class<?>... classes) {
        for (Class<?> c : classes) {
            if (c.isAssignableFrom(clazz)) {
                return true;
            }
        }

        return false;
    }
}
