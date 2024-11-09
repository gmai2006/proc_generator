package com.datascience9.tsql.transform;

import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.FormatterException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;
import org.stringtemplate.v4.StringRenderer;

public class CodeGenerator {
    public static void main(String[] args) throws Exception {
        final List<Table> tables = TableExtractor.generateTablesFromTsql("/tsql.sql");
        final String header = TsqlUtils.loadResource2String("/header.txt");
        final String importText = TsqlUtils.loadResource2String("/import.txt");

        final String stored = tables.stream().map(CodeGenerator::generateStoredProcedure).collect(Collectors.joining("\n"));
        final Path storedOutput = Paths.get("./", "stored.sql");
        TsqlUtils.writeStringWithoutFormatToFile(storedOutput, stored);

        tables.stream().forEach(t -> {
            createDao(t, header, importText);
            createModel(t, header, importText);
            createService(t, header, importText);
        });
    }

    static void createDao(Table t, String header, String importText) {
        STGroup  stGroup = getStGroupFromClient(CodeGenerator.class, "dao.stg", '$', '$');
        String dao = generateDao(t, stGroup);
        dao = dao.replace("@@HEADER@@", header);
        dao = dao.replace("@@IMPORTEXTRAC@@", importText);
        final Path output = Paths.get("./src/main/java/", buildCamelCase(t.tableName) + "Dao.java");
        try {
            writeStringToJava(output, dao);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    static String generateStoredProcedure(Table t) {
        String source = TsqlUtils.loadResource2String("/proc_template.txt");
        source = source.replace("$$TABLE_NAME$$", t.tableName);
        source = source.replace("$$LIST$$", t.generateListOfParameters());
        source = source.replace("$$LISTVALUE$$", t.generateListOfValues());
        source = source.replace("$$LISTSEPARATED$$", t.generateListOfSeparatedParameters());
        source = source.replace("$$PAIRS$$", t.generateListOfPairs());
        source = source.replace("$$KEY$$", t.key);
        return source.replace("$$ID$$", t.generateId());
    }

    static String generateDao(Table t, STGroup stGroup) {
        String source = TsqlUtils.loadResource2String("/dao_template.txt");
        source = source.replace("@@beanName@@", buildCamelCase(t.tableName));
        source = source.replace("@@beanName_lowercase@@", buildCamelCase(t.tableName).toLowerCase(Locale.ROOT));
        ST st = stGroup.getInstanceOf("parameters");
        st.add("cols", t.columns);
        return source.replace("@@PARAMS@@", st.render());
    }

    static void createModel(Table t, String header, String importText) {
        STGroup  stGroup = getStGroupFromClient(CodeGenerator.class, "model.stg", '$', '$');
        String result = generateModel(t, stGroup);
        result = result.replace("@@HEADER@@", header);
        result = result.replace("@@IMPORTEXTRAC@@", importText);
        final Path output = Paths.get("./src/main/java/", buildCamelCase(t.tableName) + ".java");
        try {
            writeStringToJava(output, result);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static String generateModel(Table t, STGroup stGroup) {
        String source = TsqlUtils.loadResource2String("/model_template.txt");
        source = source.replace("@@beanName@@", buildCamelCase(t.tableName));
        source = source.replace("@@beanName_lowercase@@", buildCamelCase(t.tableName).toLowerCase(Locale.ROOT));
        source = source.replace("@@LIST@@", t.generateListOfParameters());

        ST memberSt = stGroup.getInstanceOf("members");
        memberSt.add("cols", t.columns);
        source = source.replace("@@MEMBERS@@", memberSt.render());

        ST getMethodSt = stGroup.getInstanceOf("getMethods");
        getMethodSt.add("cols", t.columns);
        source = source.replace("@@GETMETHODS@@", getMethodSt.render());

        ST caseSt = stGroup.getInstanceOf("caseStatements");
        caseSt.add("cols", t.columns);
        source = source.replace("@@CASES@@", caseSt.render());

        ST setMethodSt = stGroup.getInstanceOf("setMethods");
        setMethodSt.add("cols", t.columns);
        return source.replace("@@SETMETHODS@@", setMethodSt.render());
    }

    static void createService(Table t, String header, String importText) {
        String result = generateService(t);
        result = result.replace("@@HEADER@@", header);
        result = result.replace("@@IMPORTEXTRAC@@", importText);

        String interfaceResult = generateServiceInterface(t);
        interfaceResult = interfaceResult.replace("@@HEADER@@", header);

        final Path output = Paths.get("./src/main/java/", "Default" + buildCamelCase(t.tableName) + "Service.java");
        final Path interfaceOutput = Paths.get("./src/main/java/", buildCamelCase(t.tableName) + "Service.java");
        try {
            writeStringToJava(output, result);
            writeStringToJava(interfaceOutput, interfaceResult);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static String generateService(Table t) {
        String source = TsqlUtils.loadResource2String("/service_template.txt");
        source = source.replace("@@beanName@@", buildCamelCase(t.tableName));
        source = source.replace("@@beanName_lowercase@@", buildCamelCase(t.tableName).toLowerCase(Locale.ROOT));
        return source.replace("@@TYPE@@", t.getPrimaryType());
    }

    static String generateServiceInterface(Table t) {
        String source = TsqlUtils.loadResource2String("/service_interface_template.txt");
        source = source.replace("@@beanName@@", buildCamelCase(t.tableName));
        source = source.replace("@@beanName_lowercase@@", buildCamelCase(t.tableName).toLowerCase(Locale.ROOT));
        return source.replace("@@TYPE@@", t.getPrimaryType());
    }

    public static STGroup getStGroupFromClient(Class clazz, String templateFile, char openToken, char endToken) {
        STGroup stGroup = new STGroupFile(Objects.requireNonNull(clazz.getClassLoader().getResource(templateFile)), "UTF-8", openToken, endToken);
        stGroup.registerRenderer(String.class, new StringRenderer());
        return stGroup;
    }

    public static void writeStringToJava(final Path path, String s) throws IOException {
        if (null == path) {
            throw new IllegalArgumentException("Input file required!");
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path.toFile()))) {
            writer.write(new Formatter().formatSource(s));
            writer.flush();
        } catch (FormatterException e) {
            System.err.println("Failed to format the java source code");
        }

    }

    public static String buildCamelCase(String oldName) {
        final String localName = oldName.replaceAll("[- ]", "_");
        final String[] tokens = localName.split("_");
        //exception: a variable starts with _
        if (oldName.startsWith("_") && tokens.length == 2) {
            return oldName;
        }
        return String.join("", Arrays.stream(tokens).map(TsqlUtils::convertWord2CamelCaseWord).collect(Collectors.toList()));
    }

}
