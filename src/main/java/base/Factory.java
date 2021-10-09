package base;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

public abstract class Factory {

    private static final char QUOTE_CHAR = '\"';

    private static class LazyHolder {
        public static Unsafe UNSAFE;

        static {
            Field theUnsafe = null;
            try {
                theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
                theUnsafe.setAccessible(true);
                UNSAFE = (Unsafe) theUnsafe.get(null);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    protected Unsafe getUnsafe() {
        return LazyHolder.UNSAFE;
    }

    public abstract <R> Generator<R> createGenerator(Class<R> clazz);

    protected String capitalizeString(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    protected String wrapQuotes(String str) {
        return QUOTE_CHAR + str + QUOTE_CHAR;
    }
}
