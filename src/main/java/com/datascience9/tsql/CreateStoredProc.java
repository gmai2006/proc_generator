package com.datascience9.tsql;

import com.datascience9.tsql.parse.TSqlParser;
import com.datascience9.tsql.transform.TSqlQueryBasedVisitor;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.xpath.XPath;

public class CreateStoredProc {
    public static void main(String[] args) throws Exception {
        TSqlParser parser = TsqlUtils.getTsqlParser(CreateStoredProc.class.getResourceAsStream("/tsql.sql"));
        ParseTree tree = parser.tsql_file();

        final String xpath = "//create_table";
        final Collection<ParseTree> subtree = XPath.findAll(tree, xpath, parser);
        if (null == subtree || subtree.isEmpty()) throw new IllegalArgumentException("NO table FOUND!!!");

        List<Table> tables = subtree.stream().map(table -> {
            String tableName = extractTableName(table, parser);
            List<Column> cols = extractColumns(table, parser);
            return new Table(tableName, cols);
        }).collect(Collectors.toList());

        String stored = tables.stream().map(CreateStoredProc::generateStoredProcedure).collect(Collectors.joining("\n"));
        System.out.println(stored);

    }

    static String generateStoredProcedure(Table t) {
        String source = TsqlUtils.loadResource2String("/template.txt");
        source = source.replace("$$TABLE_NAME$$", t.tableName);
        source = source.replace("$$LIST$$", t.generateListOfParameters());
        source = source.replace("$$LISTVALUE$$", t.generateListOfValues());
        source = source.replace("$$LISTSEPARATED$$", t.generateListOfSeparatedParameters());
        source = source.replace("$$PAIRS$$", t.generateId());
        return source.replace("$$ID$$", t.generateListOfPairs());
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

    private static class Table {
        String tableName;
        List<Column> columns;

        //@name, description=@description, modified_by=@modified_by, modified_at=@modified_at
        public String generateListOfPairs() {
            return columns.stream().map(c -> c.name + "=@" + c.name).collect(Collectors.joining(", "));
        }
        //id, name, description, created_by, created_at
        public String generateListOfParameters() {
            return columns.stream().map(Column::getName).collect(Collectors.joining(", "));
        }

        //@id, @name, @description, @created_by, @created_at
        public String generateListOfValues() {
            return columns.stream().map(c -> "@" + c.name).collect(Collectors.joining(", "));
        }

        public String generateId() {
            final Column id =getPrimaryKey();
            return id.name + " " + id.type;
        }

        /**
         * @id uniqueidentifier,
         *     @name nvarchar(150),
         *     @description nvarchar(250),
         *     @created_by nvarchar(150),
         *     @created_at datetime
         * @return
         */
        public String generateListOfSeparatedParameters() {
            return columns.stream().map(col -> "@" + col.name + " " + col.type).collect(Collectors.joining(",\n"));
        }

        public Table(String name, List<Column> cols) {
            this.tableName = name;
            this.columns = cols;
        }

        public Column getPrimaryKey() {
            Predicate<Column> isKey = c -> c.type.contains("uniqueidentifier");
            List<Column> keys = columns.stream().filter(isKey).collect(Collectors.toList());
            return keys.get(0);
        }


    }
    private static class Column {
        String name;
        String type;
        String constraint;

        public Column(String name, String type, String constraint) {
            this.name = name;
            this.type = type;
            this.constraint = constraint;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public String getConstraint() {
            return constraint;
        }

        @Override
        public String toString() {
            return "Column{" +
                    "name='" + name + '\'' +
                    ", type='" + type + '\'' +
                    ", constraint='" + constraint + '\'' +
                    '}';
        }
    }
}
