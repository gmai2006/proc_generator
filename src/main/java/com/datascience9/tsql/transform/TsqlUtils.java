package com.datascience9.tsql.transform;

import com.datascience9.tsql.parse.TSqlLexer;
import com.datascience9.tsql.parse.TSqlParser;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.stream.Collectors;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

public class TsqlUtils {

  public static final Map<String, String> MAPPER = Map.of(
          "nvarchar", "String",
          "uniqueidentifier",  "UUID",
          "int", "Integer");

  public static TSqlLexer getTsqlLexer(Path path) throws Exception {
    try {
      CharStream s = CharStreams.fromPath(path);
      CaseChangingCharStream upper = new CaseChangingCharStream(s, true);
      return new TSqlLexer(upper);
    } catch (Exception ex) {
      throw new Exception(ex);
    }
  }

  public static TSqlLexer getTsqlLexer(InputStream in) throws Exception {
    try {
      CharStream s = CharStreams.fromStream(in);
      CaseChangingCharStream upper = new CaseChangingCharStream(s, true);
      return new TSqlLexer(upper);
    } catch (Exception ex) {
      throw new Exception(ex);
    }
  }

  public static TSqlParser getTsqlParser(InputStream in) throws Exception {
    try {
      CommonTokenStream tokens = new CommonTokenStream(getTsqlLexer(in));
      return new TSqlParser(tokens);
    } catch (Exception ex) {
      throw new Exception(ex);
    }
  }

  public static String loadResource2String(String fileName) {
    Objects.requireNonNull(fileName, "resource CANNOT be null");
    if (!fileName.startsWith("/")) {
      fileName = File.separator + fileName;
    }
    return new Scanner(Objects.requireNonNull(TsqlUtils.class.getResourceAsStream(fileName)), String.valueOf(StandardCharsets.UTF_8)).useDelimiter("\\A").next();
  }

  /**
   * Recursive function to convert a string to camel case.
   * @param word - the input string.
   * @param isUpper - Check if the previous character is upper case.
   * @return a string in camel case.
   */
  public static String convertWord2CamelCaseWord(String word, boolean isUpper) {
    if (word.length() <= 1) {
      if (isUpper) {
        return word.toLowerCase();
      } else {
        return word;
      }
    }

    if (isAllUpperCase(word)) {
      return word.substring(0, 1)
              .toUpperCase()
              .concat(word.substring(1).toLowerCase());
    }

    if (isUpper) {
      return word.substring(0, 1)
              .toLowerCase()
              .concat(convertWord2CamelCaseWord(word.substring(1), false));
    }
    return word.substring(0, 1)
            .concat(convertWord2CamelCaseWord(word.substring(1),
                    Character.isUpperCase(word.charAt(0))));
  }

  public static boolean isAllUpperCase(String word) {
    if (word.length() == 0) {
      return false;
    }
    word = word.trim();
    if (word.length() == 1) {
      return Character.isUpperCase(word.charAt(0));
    }
    return Character.isUpperCase(word.charAt(0)) && isAllUpperCase(word.substring(1));
  }

  public static String convertWord2CamelCaseWord(String word) {
    word = word.trim();
    if (word.length() <= 1) {
      return word;
    }

    if (isAllUpperCase(word)) {
      return word.substring(0, 1)
              .toUpperCase()
              .concat(word.substring(1).toLowerCase());
    }

    return word.substring(0, 1)
            .toUpperCase()
            .concat(convertWord2CamelCaseWord(word.substring(1), true));
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

  /**
   * Write string to a file.
   * @param path - the output file.
   * @param s the input string.
   */
  public static void writeStringWithoutFormatToFile(Path path, String s) {
    if (null == path) {
      throw new IllegalArgumentException("Input file required!");
    }
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(path.toFile()))) {
      writer.write(s);
    } catch (IOException e) {
        e.printStackTrace();
    }
  }
}
