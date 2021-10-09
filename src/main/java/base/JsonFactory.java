package base;

import java.util.Comparator;
import java.util.stream.Collectors;

public class JsonFactory extends Factory<JsonGenerator<?>> {

    private static final String PCKG_BASE = "base";
    private static final String GENERATOR_PARENT_NAME = "JsonGenerator";

    private static final String QUOTE = "\\\"";
    private static final String PLUS = " + ";
    private static final String NL = "\\n";
    private static final String COL = ": ";

    private static final char NL_CHAR = '\n';
    private static final char TAB = '\t';
    private static final char COMMA = ',';
    private static final char LBR = '{';
    private static final char RBR = '}';
    private static final char SMC = ';';

    @Override
    public <R> String generatorClassBody(Class<? extends R> clazz) {
        String jsonBody = collectGetters(clazz).entrySet()
                .stream()
                .sorted(Comparator.comparing(
                        it -> getUnsafe().objectFieldOffset(it.getKey())
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
                .append("public class ").append(fullClassName(clazz)).append(" implements ")
                .append(GENERATOR_PARENT_NAME).append("<").append(clazz.getSimpleName()).append(">")
                .append(" ").append(LBR).append(NL_CHAR)
                .append(TAB).append("public File generateAndGetFile(")
                .append(clazz.getCanonicalName()).append(" object) ").append(LBR).append(NL_CHAR)
                .append(res)
                .append("String pathBase = \"src/main/resources/\"").append(SMC).append(NL_CHAR)
                .append("Path pathBaseP = Paths.get(pathBase)").append(SMC).append(NL_CHAR)
                .append("String pathFile = pathBase + ").append("\"JSON\" + ").append(wrapQuotes(clazz.getSimpleName()))
                .append(" + String.valueOf(object.hashCode()) ").append("+ \".json\"").append(SMC).append(NL_CHAR)
                .append("Path pathFileP = Paths.get(pathFile)").append(SMC).append(NL_CHAR)
                .append("try { \n " +
                        "   Files.deleteIfExists(pathFileP); \n" +
                        "   pathFileP = Files.createFile(pathFileP); \n" +
                        "   Files.write(pathFileP, body.getBytes()); \n" +
                        " } catch (IOException e) {\n" +
                        "   e.printStackTrace();\n" +
                        " }\n")
                .append("return pathFileP.toFile()").append(SMC).append(NL_CHAR)
                .append(RBR).append(NL_CHAR)
                .append(RBR).append(NL_CHAR);

        return javaCode.toString();
    }

    @Override
    public <R> String fullClassName(Class<? extends R> clazz) {
        return clazz.getSimpleName() + GENERATOR_PARENT_NAME;
    }
}
