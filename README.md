Spring Data JDBC Codegen Tool
=============================

This is a simple code generator tool to scaffold the spring data jdbc dao classes.
This tool uses Spring Data JDBC generic DAO implementation created by nurkiewicz.
https://github.com/nurkiewicz/spring-data-jdbc-repository

 
This tool creates the domain classes (POJO classes), JDBC repository classes and some helper classes by reading the database metadata (tables, primary keys, foreign keys etc).

Objectives
----------
  1. Avoid writing boiler plate code and use the tool to generate.
  2. Generate simple, clean code which can be extensible. 
  3. Preserve user written code.
  4. Extensible without compromising the generated code.

Features
--------
  1. Uses the Spring Data JDBC Generic Dao implementation created by **nurkiewicz**, 
     https://github.com/nurkiewicz/spring-data-jdbc-repository
  2. Domain class with optional **JSR-303** validation annotations.
  2. Repository class with **@Repository** annotation.
  3. Database helper class with RowMapper and RowUnMapper, table and column details. All table and column information is encapsulated in this class.
  

Sample code
-----------
  If the table, **Comments** is defined as below

    CREATE SEQUENCE comment_seq;
    
    CREATE TABLE IF NOT EXISTS  COMMENTS  (
    id INT PRIMARY KEY DEFAULT nextval('comment_seq'),
    user_name varchar(256) REFERENCES USERS,
    contents varchar(1000),
    created_time TIMESTAMP NOT NULL,
    favourite_count INT NOT NULL
    );


  the following classes are generated,


  **Domain Class:** A Simple POJO with the fields defined in the database table. Implements the Spring Data JDBC Persistable interface to enable persistence.
  
    public class Comments implements Persistable<Integer>
    {
    
       private static final long serialVersionUID = 1L;
       
       private Integer id;
       
       private String userName;
       
       private String contents;
       
       private Date createdTime;
       
       private Integer favouriteCount;
       
       private transient boolean persisted;
       
       private Users users;
    
       public Comments ()
       {
       
       }
       ...

  
  **Repository Class:** The repository dao class extending the JdbcRepository.
  
    @Repository
    public class CommentsRepository extends JdbcRepository<Comments, Integer>
    {
    
        final static Logger logger = LoggerFactory.getLogger (CommentsRepository.class);
    
       	public CommentsRepository()
       	{
       		super (CommentsDB.ROW_MAPPER, CommentsDB.ROW_UNMAPPER, CommentsDB.getTableName ());
       	}
       
       	public CommentsRepository(RowMapper<Comments> rowMapper, RowUnmapper<Comments> rowUnmapper, String idColumn)
       	{
       		super (CommentsDB.ROW_MAPPER, CommentsDB.ROW_UNMAPPER, CommentsDB.getTableName (), idColumn);
       	}
       
       	@Override
       	protected Comments postCreate(Comments entity, Number generatedId)
       	{
       		entity.setId(generatedId.intValue());
       		entity.setPersisted(true);
       		return entity;
       	}
       
       
       	public List<Comments> getCommentsByUserName (Long userName)
       	{
       		String sql = "select * from " + CommentsDB.getTableName() + " where " + CommentsDB.COLUMNS.USER_NAME.getColumnName() + " = ? ";
       		return this.getJdbcOperations ().query (sql, new Object[] { userName }, CommentsDB.ROW_MAPPER);
       	}
       ...
    
  **Helper Class:** A helper class with **all** the database table related information encapsulated. 
  
      public class CommentsDB
      {
      
          private static String TABLE_NAME = "COMMENTS";
      
         	private static String TABLE_ALIAS = "com";
         
         	public static String getTableName()
         	{
         		return TABLE_NAME;
         	}
         
         	public static String getTableAlias()
         	{
         		return TABLE_NAME + " as " + TABLE_ALIAS;
         	}
         
         	public enum COLUMNS
         	{
         		ID("id"),
         		USER_NAME("user_name"),
         		CONTENTS("contents"),
         		CREATED_TIME("created_time"),
         		FAVOURITE_COUNT("favourite_count"),
         		;
             ...
           }
        
   RowMapper and RowUnMapper classes to read and write the POJO class to database.
    
    	public static final class  CommentsRowMapper implements RowMapper<Comments>
    	{
    		public Comments mapRow(ResultSet rs, int rowNum) throws SQLException 
    		{
    			Comments obj = new Comments();
    			obj.setId(rs.getInt(COLUMNS.ID.getColumnName()));
    			obj.setUserName(rs.getString(COLUMNS.USER_NAME.getColumnName()));
    			obj.setContents(rs.getString(COLUMNS.CONTENTS.getColumnName()));
    			obj.setCreatedTime(rs.getTimestamp(COLUMNS.CREATED_TIME.getColumnName()));
    			obj.setFavouriteCount(rs.getInt(COLUMNS.FAVOURITE_COUNT.getColumnName()));
    			return obj;
    		}
    	}
    
    	
    	public static final class CommentsRowUnmapper implements RowUnmapper<Comments>
    	{
    		public Map<String, Object> mapColumns(Comments comments)
    		{
    			Map<String, Object> mapping = new LinkedHashMap<String, Object>();
    			mapping.put(COLUMNS.ID.getColumnName(), comments.getId());
    			mapping.put(COLUMNS.USER_NAME.getColumnName(), comments.getUserName());
    			mapping.put(COLUMNS.CONTENTS.getColumnName(), comments.getContents());
    			if (comments.getCreatedTime() != null)
    				mapping.put(COLUMNS.CREATED_TIME.getColumnName(), new Timestamp (comments.getCreatedTime().getTime()));
    			mapping.put(COLUMNS.FAVOURITE_COUNT.getColumnName(), comments.getFavouriteCount());
    			return mapping;
    		}
    	}
    ...

Code Generator Properties
-------------------------

The following properties created in a file named, codegenerator.properties is required to generate the code. This should be available in the classpath so typically stored in **src/main/resources** folder.


    # Enable code generation. If false nothing will happen even if CodeGenerator.generate() is called
    codegeneration.enabled=true
    
    # Folder path where the code will be generated
    src.folder.path=src/main/java
    
    #
    # DOMAIN CLASSES
    #
    # Package Name of the class and interfaces
    # Generated domain class and interfaces will be created with the following package name and folder structure
    domain.package.name=com.generated.code.domain
    
Domain classes can be generated with JSR-303 validation annotations. Validations are based on the column size, type, constraints.

    # Generate jsr-303 validation annotations
    generate.jsr303.annotations=true
   
    #
    # REPOSITORY CLASSES
    #
    # Repository package name
    repository.package.name=com.generated.code.repository
    # Generated Repository helper class and interfaces will be created with the following package name and folder structure
    repository.db.package.name=com.generated.code.repository.db
    
Ignore a list of tables and columns. code will not be generated for these lists.

    #
    # following tables will be ignored during code generation, comma separated table names or patterns e.g qrtz*  or *queue
    ignore.tablelist=
    # following columns will be ignored during code generation
    ignore.columnlist=
   
Parent-Child relationship objects can be created in the domain classes.   
   
    # Parent - Child relations. create child objects in parent object based on the relationship
    # relationship has to be in this format: ParentTableName:ChildTableName:OneToMany multiple sets should be comma separated
    # this creates the structure as follows:
    # ParentDomainClass: Field List<ChildDomainClass> children will be created in parent domain class to represent the relationship.
    # Repository class: A method to get the list of child rows by parent id will be created in the Child Repository class.
    # Db class: Alias Mapper in the Parent Db class will call the above method to set the list of child objects.
    # Valid relations are OneToOne, OneToMany
    parent.child.relations=
    
 Database configuration:
 
    #
    # DATABASE CONFIGURATION
    #
    # Postgres
    jdbc.driverClassName=org.postgresql.Driver
    jdbc.url=jdbc:postgresql://localhost:5432/test
    jdbc.username=postgres
    jdbc.password=postgres

Getting Started
================

Maven coordinates

```    
    	<dependency>
   			<groupId>com.mulampaka.spring.data.jdbc</groupId>
   			<artifactId>spring-data-jdbc-codegen</artifactId>
   			<version>1.0.0</version>
   		</dependency>
```
**Note: This project is not yet deployed to central maven repository so you need to install locally, as follows:**

```
$ git clone git://github.com/kalyanmulampaka/spring-data-jdbc-codegen.git
$ cd spring-data-jdbc-codegen
$ git checkout 1.0.0
$ mvn javadoc:jar source:jar install
```
