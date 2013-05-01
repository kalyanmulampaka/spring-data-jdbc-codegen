Spring Data Jdbc Codegen Tool
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
  1. Domain class with optional JSR-303 validation annotations.
  2. Repository class with @Repository annotation.
  3. Database helper class with RowMapper and RowUnMapper, table and column details. All table and column information is encapsulated in this class.
  

Sample code
-----------
  If the table, **Comments** is defined as below
  
      CREATE SEQUENCE comment_seq;

      CREATE TABLE IF NOT EXISTS **COMMENTS** (
        id INT PRIMARY KEY DEFAULT nextval('comment_seq'),
      	user_name varchar(256) REFERENCES USERS,
      	contents varchar(1000),
      	created_time TIMESTAMP NOT NULL,
      	favourite_count INT NOT NULL
      );
      
  the following classes are generated,


  **Domain Class:**
  
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

  
  **Repository Class:**
  
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
    
  **Helper Class:**
  
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

