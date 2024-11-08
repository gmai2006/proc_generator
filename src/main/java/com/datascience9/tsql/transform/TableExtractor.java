package com.datascience9.tsql.transform;

import com.datascience9.tsql.parse.TSqlParser;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.xpath.XPath;

public class TableExtractor {
    public static List<Table> generateTablesFromTsql(String fileName) throws Exception {
        final TSqlParser parser = TsqlUtils.getTsqlParser(TableExtractor.class.getResourceAsStream("/tsql.sql"));
        final ParseTree tree = parser.tsql_file();

        final String xpath = "//create_table";
        final Collection<ParseTree> subtree = XPath.findAll(tree, xpath, parser);
        if (null == subtree || subtree.isEmpty()) throw new IllegalArgumentException("NO table FOUND!!!");
        return subtree.stream().map(table -> createTable(table, parser)).toList();
    }

    static Table createTable(final ParseTree tree, final TSqlParser parser ) {
        String tableName = extractTableName(tree, parser);
        List<Column> cols = extractColumns(tree, parser);
        String key = extractPrimaryKey(tree, parser);
        return new Table(tableName, key, cols);
    }

    static String extractPrimaryKey(ParseTree tree, TSqlParser parser) {
        final String xpath = "//table_constraint/column_name_list_with_order";
        final Collection<ParseTree> subtree = XPath.findAll(tree, xpath, parser);
        if (null == subtree || subtree.isEmpty()) throw new IllegalArgumentException("NO primary key FOUND!!!");
        final TSqlParser.Column_name_list_with_orderContext nameList = (TSqlParser.Column_name_list_with_orderContext)subtree.iterator().next();
        final List<TSqlParser.Id_Context> names = nameList.id_();
        return names.stream().map(name -> new TSqlQueryBasedVisitor().visitId_(name)).toList().get(0);
    }
    static String extractTableName(ParseTree tree, TSqlParser parser) {
        final String xpath = "//table_name";
        final Collection<ParseTree> subtree = XPath.findAll(tree, xpath, parser);
        if (null == subtree || subtree.isEmpty()) throw new IllegalArgumentException("NO table name FOUND!!!");
        final TSqlParser.Table_nameContext tableName = (TSqlParser.Table_nameContext)subtree.iterator().next();
        final List<TSqlParser.Id_Context> names = tableName.id_();
        return names.stream().map(name -> new TSqlQueryBasedVisitor().visitId_(name)).collect(Collectors.joining("."));
    }

    static List<Column> extractColumns(ParseTree tree, TSqlParser parser) {
        final String xpath = "//column_definition";
        final Collection<ParseTree> subtree = XPath.findAll(tree, xpath, parser);
        if (null == subtree || subtree.isEmpty()) throw new IllegalArgumentException("NO column FOUND!!!");
        return subtree.stream().map(column -> {
            final TSqlParser.Column_definitionContext col = (TSqlParser.Column_definitionContext)column;
            final TSqlParser.Id_Context name = col.id_();
            final String colName = new TSqlQueryBasedVisitor().visitId_(name);
            final TSqlParser.Data_typeContext type = col.data_type();
            final String colType = new TSqlQueryBasedVisitor().visitData_type(type);
            final List<TSqlParser.Column_definition_elementContext> columnDefinitionElements = col.column_definition_element();

            final String constraint =
                    columnDefinitionElements.stream().map(c -> new TSqlQueryBasedVisitor().visitColumn_definition_element(c)).collect(
                            Collectors.joining(" "));

            return new Column(colName, colType, constraint);
        }).collect(Collectors.toList());
    }

}
