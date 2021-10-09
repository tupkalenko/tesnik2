package base;

import net.openhft.compiler.CompilerUtils;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Factory {

    private static final String PCKG_BASE = "base";
    private static final String PCKG_IMPL = "impl";
    private static final String GENERATOR_PARENT_NAME = "JsonGenerator";

    private static final String QUOTE = "\\\"";
    private static final String PLUS = " + ";
    private static final String NL = "\\n";
    private static final String COL = ": ";

    private static final char QUOTE_CHAR = '\"';
    private static final char NL_CHAR = '\n';
    private static final char TAB = '\t';
    private static final char COMMA = ',';
    private static final char LBR = '{';
    private static final char RBR = '}';
    private static final char SMC = ';';

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

    public <R> JsonGenerator<R> createJsonGenerator(Class<? extends R> clazz) {
        Map<Field, String> fieldGetter = Arrays.stream(clazz.getDeclaredFields())
                .collect(Collectors.toMap(Function.identity(),
                        key -> "object.get" + capitalizeString(key.getName()) + "()"
                ));

        String jsonBody = fieldGetter.entrySet()
                .stream()
                .sorted(Comparator.comparing(
                        it -> LazyHolder.UNSAFE.objectFieldOffset(it.getKey())
                ))
                .map(it -> wrapQuotes(TAB + QUOTE + it.getKey().getName() + QUOTE) +
                        PLUS + wrapQuotes(COL + QUOTE) + PLUS +
                        it.getValue() + PLUS +
                        wrapQuotes(QUOTE + COMMA + NL) + PLUS
                )
                .collect(Collectors.joining());

        int lastCommaInd = jsonBody.lastIndexOf(COMMA);
        String finalJsonBody = jsonBody.substring(0, lastCommaInd) + jsonBody.substring(lastCommaInd + 1);
        String res = "String body = " + wrapQuotes(LBR + NL) + PLUS + finalJsonBody + wrapQuotes(RBR + NL) + SMC;

        final String className = clazz.getSimpleName() + GENERATOR_PARENT_NAME;

        StringBuilder javaCode = new StringBuilder();

        javaCode.append("package ").append(PCKG_IMPL).append(SMC).append(NL_CHAR)
                .append("import ").append(PCKG_BASE).append(".").append(GENERATOR_PARENT_NAME).append(SMC).append(NL_CHAR)
                .append("import ").append(clazz.getCanonicalName()).append(SMC).append(NL_CHAR)
                .append("import java.nio.file.Files").append(SMC).append(NL_CHAR)
                .append("import java.nio.file.Path").append(SMC).append(NL_CHAR)
                .append("import java.nio.file.Paths").append(SMC).append(NL_CHAR)
                .append("import java.io.File").append(SMC).append(NL_CHAR)
                .append("import java.io.IOException").append(SMC).append(NL_CHAR)
                .append("import java.util.Comparator").append(SMC).append(NL_CHAR)
                .append("public class ").append(className).append(" implements ")
                .append(GENERATOR_PARENT_NAME).append("<").append(clazz.getSimpleName()).append(">")
                .append(" ").append(LBR).append(NL_CHAR)
                .append(TAB).append("public File generateAndGetFile(")
                .append(clazz.getCanonicalName()).append(" object) ").append(LBR).append(NL_CHAR)
                .append(res)
                .append("String pathBase = \"src/main/resources/\"").append(SMC).append(NL_CHAR)
                .append("Path pathBaseP = Paths.get(pathBase)").append(SMC).append(NL_CHAR)
                .append("String pathFile = pathBase + ").append("\"JSON\" + ").append(wrapQuotes(clazz.getSimpleName()))
                .append(" + String.valueOf(object.hashCode()) ").append( "+ \".json\"").append(SMC).append(NL_CHAR)
                .append("Path pathFileP = Paths.get(pathFile)").append(SMC).append(NL_CHAR)
                .append("try { \n " +
                        "   Files.walk(pathBaseP)\n" +
                        "       .sorted(Comparator.reverseOrder())\n" +
                        "       .map(Path::toFile)\n" +
                        "       .forEach(it -> {if (!it.isDirectory()) {it.delete();}}) ; \n" +
                        "   pathFileP = Files.createFile(pathFileP); \n" +
                        "   Files.write(pathFileP, body.getBytes()); \n" +
                        " } catch (IOException e) {\n" +
                        "   e.printStackTrace();\n" +
                        " }\n")
                .append("return pathFileP.toFile()").append(SMC).append(NL_CHAR)
                .append(RBR).append(NL_CHAR)
                .append(RBR).append(NL_CHAR);

        JsonGenerator<R> jsonGenerator = null;
        try {
            Class aClass = CompilerUtils.CACHED_COMPILER.loadFromJava(PCKG_IMPL + "." + className, javaCode.toString());
            jsonGenerator = (JsonGenerator<R>) aClass.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return jsonGenerator;
    }

    private String capitalizeString(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private String wrapQuotes(String str) {
        return QUOTE_CHAR + str + QUOTE_CHAR;
    }

}
