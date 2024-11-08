package com.datascience9.parse;

import com.datascience9.tsql.transform.TsqlUtils;
import com.datascience9.tsql.parse.TSqlParser;
import java.util.logging.Logger;
import org.antlr.v4.gui.Trees;
import org.antlr.v4.runtime.tree.ParseTree;

public class ParserInspector {
	private final static Logger logger = Logger.getLogger(ParserInspector.class.getName());

	public static void main(String[] args) throws Exception {
		String fileName = "/test1.sql";
		TSqlParser parser = TsqlUtils.getTsqlParser(ParserInspector.class.getResourceAsStream(fileName));
		ParseTree tree = parser.tsql_file();
		Trees.inspect(tree, parser);
	}
}