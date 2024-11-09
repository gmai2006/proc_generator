package com.datascience9.tsql.transform;

import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.FormatterException;
import java.io.BufferedWriter;
import java.io.File;
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
    static final String OUTPUTDIR = "./generated";
    public static void main(String[] args) throws Exception {
        final File dir = new File(OUTPUTDIR);
        if (!dir.exists()) dir.mkdir();
        else cleanUp(dir);

        final List<Table> tables = TableExtractor.generateTablesFromTsql("/tsql.sql");
        final String header = TsqlUtils.loadResource2String("/header.txt");
        final String importText = TsqlUtils.loadResource2String("/import.txt");

        final String stored = tables.stream().map(CodeGenerator::generateStoredProcedure).collect(Collectors.joining("\n"));
        final Path storedOutput = Paths.get(OUTPUTDIR, "stored.sql");
        TsqlUtils.writeStringWithoutFormatToFile(storedOutput, stored);

        tables.stream().forEach(t -> {
            createDao(t, header, importText);
            createModel(t, header, importText);
            createService(t, header, importText);
            createRest(t, header, importText);
        });
    }

    static void cleanUp(File dir) {
        for(File file: dir.listFiles())
            if (!file.isDirectory())
                file.delete();
    }
    static void createDao(Table t, String header, String importText) {
        final STGroup  stGroup = getStGroupFromClient(CodeGenerator.class, "dao.stg", '$', '$');
        String dao = generateDao(t, stGroup);
        dao = dao.replace("@@HEADER@@", header);
        dao = dao.replace("@@IMPORTEXTRAC@@", importText);
        final Path output = Paths.get(OUTPUTDIR, buildCamelCase(t.tableName) + "Dao.java");
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
        final STGroup  stGroup = getStGroupFromClient(CodeGenerator.class, "model.stg", '$', '$');
        String result = generateModel(t, stGroup);
        result = result.replace("@@HEADER@@", header);
        result = result.replace("@@IMPORTEXTRAC@@", importText);
        final Path output = Paths.get(OUTPUTDIR, buildCamelCase(t.tableName) + ".java");
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

        final ST memberSt = stGroup.getInstanceOf("members");
        memberSt.add("cols", t.columns);
        source = source.replace("@@MEMBERS@@", memberSt.render());

        final ST getMethodSt = stGroup.getInstanceOf("getMethods");
        getMethodSt.add("cols", t.columns);
        source = source.replace("@@GETMETHODS@@", getMethodSt.render());

        final ST caseSt = stGroup.getInstanceOf("caseStatements");
        caseSt.add("cols", t.columns);
        source = source.replace("@@CASES@@", caseSt.render());

        final ST setMethodSt = stGroup.getInstanceOf("setMethods");
        setMethodSt.add("cols", t.columns);
        return source.replace("@@SETMETHODS@@", setMethodSt.render());
    }

    static void createService(Table t, String header, String importText) {
        String result = generateCommonClasses(t, "/service_template.txt");
        result = result.replace("@@HEADER@@", header);
        result = result.replace("@@IMPORTEXTRAC@@", importText);

        String interfaceResult = generateCommonClasses(t, "/service_interface_template.txt");
        interfaceResult = interfaceResult.replace("@@HEADER@@", header);

        final Path output = Paths.get(OUTPUTDIR, "Default" + buildCamelCase(t.tableName) + "Service.java");
        final Path interfaceOutput = Paths.get(OUTPUTDIR, buildCamelCase(t.tableName) + "Service.java");
        try {
            writeStringToJava(output, result);
            writeStringToJava(interfaceOutput, interfaceResult);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static void createRest(Table t, String header, String importText) {
        String result = generateCommonClasses(t, "/rest_template.txt");
        result = result.replace("@@HEADER@@", header);
        result = result.replace("@@IMPORTEXTRAC@@", importText);
        final Path output = Paths.get(OUTPUTDIR, buildCamelCase(t.tableName) + "RestService.java");

        try {
            writeStringToJava(output, result);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static String generateCommonClasses(Table t, String templateFile) {
        String source = TsqlUtils.loadResource2String(templateFile);
        source = source.replace("@@beanName@@", buildCamelCase(t.tableName));
        source = source.replace("@@beanName_lowercase@@", buildCamelCase(t.tableName).toLowerCase(Locale.ROOT));
        return source.replace("@@TYPE@@", t.getPrimaryType());
    }

    public static STGroup getStGroupFromClient(Class clazz, String templateFile, char openToken, char endToken) {
        final STGroup stGroup = new STGroupFile(Objects.requireNonNull(clazz.getClassLoader().getResource(templateFile)), "UTF-8", openToken, endToken);
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
