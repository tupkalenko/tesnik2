package base;

import net.openhft.compiler.CompilerUtils;
import org.slf4j.IMarkerFactory;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class Factory<U extends Generator<?>> {

    private static final char QUOTE_CHAR = '\"';
    public static final String PCKG_IMPL = "impl";

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

    protected <R> Map<Field, String> collectGetters(Class<? extends R> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .collect(Collectors.toMap(Function.identity(),
                        key -> "object.get" + capitalizeString(key.getName()) + "()"
                ));
    }

    protected Unsafe getUnsafe() {
        return LazyHolder.UNSAFE;
    }

    protected String capitalizeString(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    protected String wrapQuotes(String str) {
        return QUOTE_CHAR + str + QUOTE_CHAR;
    }

    public <R, Z extends Generator<R>> Z createGenerator(Class<R> clazz) {
        Z generator = null;
        try {
            Class aClass = CompilerUtils.CACHED_COMPILER.loadFromJava(PCKG_IMPL + "." + fullClassName(clazz), generatorClassBody(clazz));
            generator = (Z) aClass.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return generator;
    }

    public abstract <R> String generatorClassBody(Class<? extends R> clazz);

    public abstract <R> String fullClassName(Class<? extends R> clazz);

}
