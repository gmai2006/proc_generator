package com.datascience9.tsql.transform;

public class Column {
    String name;
    String type;
    String constraint;

    public Column(String name, String type, String constraint) {
        this.name = name;
        this.type = type;
        this.constraint = constraint;
    }

    public String getCamelCase() {
        return TsqlUtils.convertWord2CamelCaseWord(this.name, false);
    }

    public String getFieldName() {
        final String str = getCamelCase();
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
    public String getName() {
        return name;
    }

    public String getType() {
        final String localType = type.indexOf("(") > 0 ? type.substring(0, type.indexOf("(")) : type;
        return TsqlUtils.MAPPER.getOrDefault(localType, "String");
    }

    public String getConstraint() {
        return constraint;
    }

    public boolean getUuidType() {
        return type.toLowerCase().contains("uniqueidentifier");
    }
    public boolean getDateTimeType() {
        return type.toLowerCase().contains("datetime");
    }

    public boolean getIntegerType() {
        return type.toLowerCase().contains("int");
    }

    public boolean getNonString() {
        return !(type.toLowerCase().contains("nvachar") || type.toLowerCase().contains("varchar"));
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
