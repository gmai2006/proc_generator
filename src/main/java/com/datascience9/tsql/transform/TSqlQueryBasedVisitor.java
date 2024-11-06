package com.datascience9.tsql.transform;

import com.datascience9.tsql.parse.TSqlParserBaseVisitor;
import org.antlr.v4.runtime.tree.TerminalNode;

public class TSqlQueryBasedVisitor extends TSqlParserBaseVisitor<String> {

  @Override
  public String visitTerminal(TerminalNode node) {
    String str = node.getText();
    return str;
  }

  @Override
  public String aggregateResult(String aggregate, String nextResult) {
    if (aggregate == null) {
      return nextResult;
    }

    if (nextResult == null) {
      return aggregate;
    }

    StringBuilder sb = new StringBuilder(aggregate);
    if (!aggregate.endsWith(".")
        && !(nextResult.startsWith("."))
        && (!(nextResult.startsWith("(")))) {
      sb.append(" ");
    }
    sb.append(nextResult);

    return sb.toString();
  }
}
