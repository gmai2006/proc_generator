/* CREATE THE test TABLES IN THE LOCAL PERSISTENCE DATABASE */
if exists (select * from INFORMATION_SCHEMA.TABLES where table_name = 'testads')
begin
    DROP TABLE testads
END
GO
CREATE TABLE testads (
    ads nvarchar(128) NOT NULL,
    viewName nvarchar(512) NOT NULL,
    displayName nvarchar(128) NULL,
    CONSTRAINT pk_testads PRIMARY KEY(ads)
)
GO

if exists (select * from INFORMATION_SCHEMA.TABLES where table_name = 'testcolumn')
BEGIN
    DROP TABLE testcolumn
END
GO

CREATE TABLE testcolumn (
    id uniqueidentifier NOT NULL,
    ads nvarchar(128) NOT NULL,
    columnName nvarchar(512) NOT NULL,
    displayName nvarchar(128) NOT NULL,
    dataType nvarchar(128) NOT NULL,
    CONSTRAINT pk_testcolumn PRIMARY KEY(id)
)
GO

if exists (select * from INFORMATION_SCHEMA.TABLES where table_name = 'testdictionary')
BEGIN
    DROP TABLE testdictionary
END
GO

CREATE TABLE testdictionary (
    id uniqueidentifier NOT NULL,
    ads nvarchar(128) NOT NULL,
    columnName nvarchar(512) NOT NULL,
    value nvarchar(512) NOT NULL,
    translation VARCHAR(5000) NOT NULL,
    CONSTRAINT pk_testdictionary PRIMARY KEY(id)
)
GO

if exists (select * from INFORMATION_SCHEMA.TABLES where table_name = 'testquery')
begin
    DROP TABLE testquery
END
GO
CREATE TABLE testquery (
    queryId uniqueidentifier NOT NULL,
    name nvarchar(128) NULL,
    startIndex int NULL,
    pageSize int NULL,
    CONSTRAINT testquery_primary PRIMARY KEY(queryId)
)
GO

if exists (select * from INFORMATION_SCHEMA.TABLES where table_name = 'testattribute_name')
begin
    DROP TABLE testattribute_name
END
GO
CREATE TABLE testattribute_name (
    id uniqueidentifier NOT NULL,
    queryId uniqueidentifier NOT NULL,
    ads nvarchar(24) NOT NULL,
    logic nvarchar(24) NULL,
    columnName nvarchar(128) NULL,
    value nvarchar(128) NULL,
    dataType nvarchar(64) NULL,
    operation nvarchar(24) NULL,
    CONSTRAINT testattribute_name_primary PRIMARY KEY(id)
)
GO

if exists (select * from INFORMATION_SCHEMA.TABLES where table_name = 'testcircle_name')
begin
    DROP TABLE testcircle_name
END
GO
CREATE TABLE testcircle_name (
    id uniqueidentifier NOT NULL,
    queryId uniqueidentifier NOT NULL,
    center nvarchar(128) NOT NULL,
    periphery nvarchar(128) NOT NULL,
    CONSTRAINT testcircle_name_primary PRIMARY KEY(id)
)
GO

if exists (select * from INFORMATION_SCHEMA.TABLES where table_name = 'testpolygon_name')
begin
    DROP TABLE testpolygon_name
END
GO
CREATE TABLE testpolygon_name (
    id uniqueidentifier NOT NULL,
    queryId nvarchar(36) NOT NULL,
    coordinates nvarchar(512) NOT NULL,
    CONSTRAINT testpolygon_name_primary PRIMARY KEY(id)
)
GO