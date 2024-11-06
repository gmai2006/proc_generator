IF NOT EXISTS(SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'test' AND TABLE_SCHEMA = 'dbo')
        BEGIN
            CREATE TABLE dbo.test
            (
                id uniqueidentifier NOT NULL,
                name nvarchar(150) NOT NULL,
                description nvarchar(250) NULL,
                created_by nvarchar(150) NOT NULL,
                created_at datetime NOT NULL,
                CONSTRAINT PK_owner PRIMARY KEY CLUSTERED (id ASC) WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
            )
            ON [PRIMARY]
        END
GO

IF NOT EXISTS(SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'test2' AND TABLE_SCHEMA = 'dbo')
        BEGIN
            CREATE TABLE dbo.test2
            (
                id uniqueidentifier NOT NULL,
                hahaha nvarchar(150) NOT NULL,
                hohoho nvarchar(250) NULL,
                created_by nvarchar(150) NOT NULL,
                created_at datetime NOT NULL,
                CONSTRAINT PK_owner PRIMARY KEY CLUSTERED (id ASC) WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
            )
            ON [PRIMARY]
        END
GO