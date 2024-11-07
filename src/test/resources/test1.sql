IF NOT EXISTS(SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'owner' AND TABLE_SCHEMA = 'dbo')
        BEGIN
            CREATE TABLE dbo.owner
            (
                id uniqueidentifier NOT NULL,
                name nvarchar(150) NOT NULL,
                description nvarchar(250) NULL,
                created_by nvarchar(150) NOT NULL,
                created_at datetime NOT NULL,
                modified_by nvarchar(150) NULL,
                modified_at datetime NULL,
                deleted_by nvarchar(150) NULL,
                deleted_at datetime NULL,
                CONSTRAINT PK_owner PRIMARY KEY (id) WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
            )
            ON [PRIMARY]
        END
GO