package com.datascience9.tsql;

import com.datascience9.tsql.parse.TSqlLexer;
import com.datascience9.tsql.parse.TSqlParser;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Scanner;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

public class TsqlUtils {

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
}
