/* CREATE THE geosearch TABLES IN THE LOCAL PERSISTENCE DATABASE */
if exists (select * from INFORMATION_SCHEMA.TABLES where table_name = 'geosearch_ads')
begin
    DROP TABLE testtable1
END
GO
CREATE TABLE testtable1 (
    ads nvarchar(128) NOT NULL,
    haha nvarchar(512) NOT NULL,
    testme nvarchar(128) NULL,
    test2 nvarchar(128) NOT NULL,
    test3 nvarchar(128) NULL,
    CONSTRAINT pk_geosearch_ads PRIMARY KEY(ads)
)
GO

if exists (select * from INFORMATION_SCHEMA.TABLES where table_name = 'geosearch_column')
BEGIN
    DROP TABLE testtabl2
END
GO

CREATE TABLE testtabl2 (
    id uniqueidentifier NOT NULL,
    ads nvarchar(128) NOT NULL,
    dddd nvarchar(512) NOT NULL,
    ggg nvarchar(128) NOT NULL,
    dddeee nvarchar(128) NOT NULL,
    CONSTRAINT pk_geosearch_column PRIMARY KEY(id)
)
GO

if exists (select * from INFORMATION_SCHEMA.TABLES where table_name = 'geosearch_dictionary')
BEGIN
    DROP TABLE geosearch_dictionary
END
GO

CREATE TABLE geosearch_dictionary (
    id uniqueidentifier NOT NULL,
    ads nvarchar(128) NOT NULL,
    column_name nvarchar(512) NOT NULL,
    value nvarchar(512) NOT NULL,
    translation VARCHAR(5000) NOT NULL,
    CONSTRAINT pk_geosearch_dictionary PRIMARY KEY(id)
)
GO

if exists (select * from INFORMATION_SCHEMA.TABLES where table_name = 'geosearch_query')
begin
    DROP TABLE geosearch_query
END
GO
CREATE TABLE geosearch_query (
    queryId uniqueidentifier NOT NULL,
    name nvarchar(128) NULL,
    startIndex int NULL,
    pageSize int NULL,
    CONSTRAINT geosearch_query_primary PRIMARY KEY(queryId)
)
GO

if exists (select * from INFORMATION_SCHEMA.TABLES where table_name = 'geosearch_attribute_filter')
begin
    DROP TABLE geosearch_attribute_filter
END
GO
CREATE TABLE geosearch_attribute_filter (
    id uniqueidentifier NOT NULL,
    queryId uniqueidentifier NOT NULL,
    ads nvarchar(24) NOT NULL,
    logic nvarchar(24) NULL,
    columnName nvarchar(128) NULL,
    value nvarchar(128) NULL,
    dataType nvarchar(64) NULL,
    operation nvarchar(24) NULL,
    CONSTRAINT geosearch_attribute_filter_primary PRIMARY KEY(id)
)
GO

if exists (select * from INFORMATION_SCHEMA.TABLES where table_name = 'geosearch_circle_filter')
begin
    DROP TABLE geosearch_circle_filter
END
GO
CREATE TABLE geosearch_circle_filter (
    id uniqueidentifier NOT NULL,
    queryId uniqueidentifier NOT NULL,
    center nvarchar(128) NOT NULL,
    periphery nvarchar(128) NOT NULL,
    CONSTRAINT geosearch_circle_filter_primary PRIMARY KEY(id)
)
GO

if exists (select * from INFORMATION_SCHEMA.TABLES where table_name = 'geosearch_polygon_filter')
begin
    DROP TABLE geosearch_polygon_filter
END
GO
CREATE TABLE geosearch_polygon_filter (
    id uniqueidentifier NOT NULL,
    queryId nvarchar(36) NOT NULL,
    coordinates nvarchar(512) NOT NULL,
    CONSTRAINT geosearch_polygon_filter_primary PRIMARY KEY(id)
)
GO