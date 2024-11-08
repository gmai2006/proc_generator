package com.datascience9.tsql.transform;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Table {
    String tableName;
    String key;
    List<Column> columns;

    public String getCamelCase() {
        return TsqlUtils.convertWord2CamelCaseWord(this.tableName, false);
    }

    //@name, description=@description, modified_by=@modified_by, modified_at=@modified_at
    public String generateListOfPairs() {
        return columns.stream()
                .filter(c -> !c.name.equals(key))
                .map(c -> c.name + "=@" + c.name).collect(Collectors.joining(", "));
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
        final Column id = getPrimaryKey();
        return id.name + " " + id.type;
    }

    public String getPrimaryType() {
        final Column id = getPrimaryKey();
        return TsqlUtils.MAPPER.getOrDefault(id.type, "String");
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

    public Table(String name, String key, List<Column> cols) {
        this.tableName = name;
        this.key = key;
        this.columns = cols;
    }

    public Column getPrimaryKey() {
        Predicate<Column> isKey = c -> c.name.equals(key);
        List<Column> keys = columns.stream().filter(isKey).toList();
        return keys.get(0);
    }
}
