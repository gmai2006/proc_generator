# Code Generator
## Prerequisites
- The parser and lexer were generated using JDK version 17.  If your JDK is different then you MUST generate the parser and lexer again
- Copy your TSql script to a file named tsql.sql in the resources directory
- Copy your header/banner to the file called header.txt in the resources directory
- Copy your custom / extra import statements to the file called import.txt in the resources directory
- Review the file  <type>_template to see if you need to change anything.  <type> = model | service | service_interface | dao.  If needed you can 
  update the template.

## Generate the source codes
- Run gradle generateCode.  It should generate the following source codes
1. a stored procedure file called stored.sql in the project directory
2. all model, dao, services classes in the src/main/java

## TSql validator
- Go to the test/resources and override the test1.sql your Tsql script and run the ParserInspector. It should open the parse tree

## license MIT

## Version beta 1.0.1

## Todo
- Will add REST API generator

