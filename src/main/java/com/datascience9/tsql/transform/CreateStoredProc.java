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

public class CreateStoredProc {
    public static void main(String[] args) throws Exception {
        final List<Table> tables = TableExtractor.generateTablesFromTsql("/tsql.sql");

//        final String stored = tables.stream().map(CreateStoredProc::generateStoredProcedure).collect(Collectors.joining("\n"));
//        final Path storedOutput = Paths.get("./", "stored.sql");
//        writeStringWithoutFormatToFile(storedOutput, stored);
//        System.out.println(stored);


        tables.stream().forEach(t -> {
            createDao(t);
            createModel(t);
//            createService(t);
        });
    }

    static void createDao(Table t) {
        STGroup  stGroup = getStGroupFromClient(CreateStoredProc.class, "dao.stg", '$', '$');
        String dao = generateDao(t, stGroup);
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
        ST st = stGroup.getInstanceOf("params");
        st.add("cols", t.columns.stream().map(Column::getFieldName).collect(Collectors.toList()));
        return source.replace("@@PARAMS@@", st.render());
    }

    static void createModel(Table t) {
        STGroup  stGroup = getStGroupFromClient(CreateStoredProc.class, "model.stg", '$', '$');
        String result = generateModel(t, stGroup);
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

        ST st = stGroup.getInstanceOf("members");
        st.add("cols", t.columns);
        source = source.replace("@@MEMBERS@@", st.render());

        ST st1 = stGroup.getInstanceOf("getMethods");
        st1.add("cols", t.columns);
        source = source.replace("@@GETMETHODS@@", st1.render());

        ST st2 = stGroup.getInstanceOf("setMethods");
        st2.add("cols", t.columns);
        return source.replace("@@SETMETHODS@@", st2.render());
    }

    static void createService(Table t) {
        String result = generateService(t);
        final Path output = Paths.get("./src/main/java/Default", buildCamelCase(t.tableName) + "Service.java");
        try {
            writeStringToJava(output, result);
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
