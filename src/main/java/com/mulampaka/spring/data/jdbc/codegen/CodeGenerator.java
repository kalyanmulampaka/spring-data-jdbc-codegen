/**
 * 
 * Copyright 2013
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * @author Kalyan Mulampaka
 */
package com.mulampaka.spring.data.jdbc.codegen;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mulampaka.spring.data.jdbc.codegen.Relation.RelationType;
import com.mulampaka.spring.data.jdbc.codegen.util.CodeGenUtil;

/**
 * Code generator class which creates Java bean classes and interfaces for all
 * the tables in the input database schema. Reads the properties file named
 * generator.properties.
 * 
 * 
 * @author Kalyan Mulampaka
 * 
 */
public class CodeGenerator
{
	final static Logger logger = LoggerFactory.getLogger (CodeGenerator.class);
	private Properties properties;
	private List<String> ignoreColumnList = new ArrayList<String> ();
	private List<String> ignoreTableList = new ArrayList<String> ();
	private List<String> ignoreTableStartsWithPattern = new ArrayList<String> ();
	private List<String> ignoreTableEndsWithPattern = new ArrayList<String> ();
	private List<String> ignoreFKeys = new ArrayList<String> ();

	private String propertiesFile;

	public CodeGenerator ()
	{

	}

	public Properties getProperties ()
	{
		return properties;
	}

	public void setProperties (Properties properties)
	{
		this.properties = properties;
	}
	
	public String getPropertiesFile ()
	{
		return this.propertiesFile;
	}
	
	public void setPropertiesFile (String propertiesFile)
	{
		this.propertiesFile = propertiesFile;
	}
	
	private void loadProperties () throws Exception
	{
		if (StringUtils.isBlank (this.propertiesFile))
		{
			logger.error ("Properties file is not set");
			throw new Exception ("Properties file is not set");
		}
		logger.info ("Loading properties from file:" + propertiesFile);
		File f = new File (this.propertiesFile);
		if (!f.exists ())
		{
			throw new Exception (this.propertiesFile + " file does not exist");
		}
		FileInputStream propFile = new FileInputStream (propertiesFile);
		this.properties = new Properties ();
		this.properties.load (propFile);
		logger.debug ("Loaded properties:" + this.properties);
		propFile.close ();

	}

	public void generate ()
	{
		Connection conn = null;
		try
		{
			this.loadProperties ();

			if (this.properties == null || this.properties.isEmpty ())
			{
				throw new Exception ("Properties are not set.");
			}
			boolean codeGenerationEnabled = Boolean.parseBoolean (this.properties.getProperty ("codegeneration.enabled"));
			if (!codeGenerationEnabled)
			{
				logger.warn ("Code generation is disabled.");
				return;
			}

			String srcFolderPath = this.properties.getProperty ("src.folder.path");
			logger.info ("Generated Sources will be in folder:{}", srcFolderPath);
			String domainPackageName = this.properties.getProperty ("domain.package.name");
			String dbPackageName = this.properties.getProperty ("repository.db.package.name");
			String repositoryPackageName = this.properties.getProperty ("repository.package.name");

			conn = this.getConnection ();
			DatabaseMetaData metaData = conn.getMetaData ();

			// get the ignore column list
			String ignoreColumnListStr = this.properties.getProperty ("ignore.columnlist");
			if (StringUtils.isNotBlank (ignoreColumnListStr))
			{
				StringTokenizer strTok = new StringTokenizer (ignoreColumnListStr, ",");
				while (strTok.hasMoreTokens ())
				{
					this.ignoreColumnList.add (strTok.nextToken ().toLowerCase ().trim ());
				}
				logger.info ("Ignore column list:{}", this.ignoreColumnList);
			}
			
			String ignoreTableListStr = this.properties.getProperty ("ignore.tablelist");
			if (StringUtils.isNotBlank (ignoreTableListStr))
			{
				StringTokenizer strTok = new StringTokenizer (ignoreTableListStr, ",");
				while (strTok.hasMoreTokens ())
				{
					String token = strTok.nextToken ().toLowerCase ().trim ();
					if (StringUtils.startsWith (token, "*"))
					{
						this.ignoreTableEndsWithPattern.add (token.substring (1, token.length ()));
					}
					else if (StringUtils.endsWith (token, "*"))
					{
						this.ignoreTableStartsWithPattern.add (token.substring (0, token.length () - 1));
					}
					else
					{
						this.ignoreTableList.add (token);
					}
				}
				logger.info ("Ignore table list:{}", this.ignoreTableList);
				logger.info ("Ignore table Starts with pattern:{}", this.ignoreTableStartsWithPattern);
				logger.info ("Ignore table Ends with pattern:{}", this.ignoreTableEndsWithPattern);
			}
			// Add ignore fkeys
			String ignoreFKeys = this.properties.getProperty ("ignore.fkeys");
			String[] fkeys = StringUtils.split (ignoreFKeys, ",");
			for (String fkey : fkeys)
			{
				this.ignoreFKeys.add (fkey.trim ());
			}

			if (metaData != null)
			{

				CodeGenUtil.createPackage (srcFolderPath, domainPackageName);
				CodeGenUtil.createPackage (srcFolderPath, dbPackageName);
				CodeGenUtil.createPackage (srcFolderPath, repositoryPackageName);

                ResultSet rset = metaData.getTables (null, null, null, new String[] { "TABLE" });
				while (rset.next ())
				{
                    String tableName = rset.getString ("TABLE_NAME");
					logger.info ("Found Table:" + tableName);
					if (this.ignoreTable (tableName.toLowerCase ()))
					{
						logger.info ("Table:{} is in the ignore table list, not generating code for this table.", tableName);
						continue;
					}
					logger.debug ("DB Product name:{}", metaData.getDatabaseProductName ());
					logger.debug ("DB Product version:{}", metaData.getDatabaseProductVersion ());

					// for each table create the classes
					this.createClasses (metaData, tableName);

				}
			}
		}
		catch (Exception e)
		{
			logger.error ("Error occcured during code generation." + e);
			e.printStackTrace ();
		}
		finally
		{
			if (conn != null)
			{
				try
				{
					conn.close ();
				}
				catch (Exception e)
				{
					logger.warn ("Error closing db connection.{}", e);
				}
			}
		}
	}
	
	private boolean ignoreTable (String tableName)
	{
		
		// first do a actual match
		if (this.ignoreTableList.contains (tableName.toLowerCase ()))
		{
			return true;
		}
		// do a startswith check
		for (String ignoreStartsWithPattern : this.ignoreTableStartsWithPattern)
		{
			if (StringUtils.startsWith (tableName, ignoreStartsWithPattern))
			{
				return true;
			}
		}
		// do a startswith check
		for (String ignoreEndsWithPattern : this.ignoreTableEndsWithPattern)
		{
			if (StringUtils.endsWith (tableName, ignoreEndsWithPattern))
			{
				return true;
			}
		}
		return false;
	}

	private void createClasses (DatabaseMetaData metaData, String tableName) throws Exception
	{

		String rootFolderPath = this.properties.getProperty ("src.folder.path");
		String domainPackageName = this.properties.getProperty ("domain.package.name");
		String dbPackageName = this.properties.getProperty ("repository.db.package.name");
		String repositoryPackageName = this.properties.getProperty ("repository.package.name");

		String generateJsr303AnnotationsStr = this.properties.getProperty ("generate.jsr303.annotations");
		boolean generateJsr303Annotations = false;
		if (StringUtils.isNotBlank (generateJsr303AnnotationsStr))
		{
			generateJsr303Annotations = Boolean.parseBoolean (generateJsr303AnnotationsStr);
		}

		// Get the dont pluralize words
		String dontPluralizeWordsStr = this.properties.getProperty ("dont.pluralize.words");
        logger.debug ("Dont pluralize words:{}", dontPluralizeWordsStr);
        dontPluralizeWordsStr = StringUtils.replace (dontPluralizeWordsStr, " ", "");
		String[] dontPluralizeWords = StringUtils.split (dontPluralizeWordsStr, ",");
        logger.debug ("Don't Pluralize words:{}", new Object[] { dontPluralizeWords });
		List<Field> fields = new ArrayList<Field> ();
		List<Field> dbFields = new ArrayList<Field> ();
		List<Method> methods = new ArrayList<Method> ();
		
		DomainClass domainClass = new DomainClass ();
        domainClass.setDontPluralizeWords (dontPluralizeWords);
		domainClass.setDbProductName (metaData.getDatabaseProductName ());
		domainClass.setDbProductVersion (metaData.getDatabaseProductVersion ());
        domainClass.setName (tableName);
		domainClass.setRootFolderPath (rootFolderPath);
		domainClass.setPackageName (domainPackageName);
		domainClass.setFields (fields);
		domainClass.setMethods (methods);
		domainClass.setGenerateJsr303Annotations (generateJsr303Annotations);
		
		if (generateJsr303Annotations)
		{
			String insertGrpClass = this.properties.getProperty ("insert.group.class");
			if (StringUtils.isNotBlank (insertGrpClass))
			{
				String[] classes = StringUtils.split (insertGrpClass.trim (), ",");
				for (String c : classes)
				{
					if (!domainClass.getImports ().contains (c.trim ()))
						domainClass.getImports ().add (c.trim ());
					String[] classNameTokens = StringUtils.split (c.trim (), ".");
					// add only the class name and the FQ class name since the import is added
					domainClass.getJsr303InsertGroups ().add (classNameTokens[classNameTokens.length - 1]);
				}
			}

			String updateGrpClass = this.properties.getProperty ("update.group.class");
			if (StringUtils.isNotBlank (updateGrpClass))
			{
				String[] classes = StringUtils.split (updateGrpClass.trim (), ",");
				for (String c : classes)
				{
					if (!domainClass.getImports ().contains (c.trim ()))
						domainClass.getImports ().add (c.trim ());
					String[] classNameTokens = StringUtils.split (c.trim (), ".");
					// add only the class name and the FQ class name since the import is added
					domainClass.getJsr303UpdateGroups ().add (classNameTokens[classNameTokens.length - 1]);
				}
			}
		}

		// create the db class
		DBClass dbClass = new DBClass ();
		dbClass.getImports ().add (domainPackageName + "." + WordUtils.capitalize (CodeGenUtil.normalize (domainClass.getName ())));
		dbClass.setName (tableName);
		dbClass.setRootFolderPath (rootFolderPath);
		dbClass.setPackageName (dbPackageName);
		dbClass.setFields (dbFields);
        dbClass.setDontPluralizeWords (dontPluralizeWords);
		
		// create the repo class
		RepositoryClass repoClass = new RepositoryClass ();
		repoClass.setDontPluralizeWords (dontPluralizeWords);
		repoClass.createLogger ();
		repoClass.getImports ().add (dbPackageName + "." + WordUtils.capitalize (CodeGenUtil.normalize (dbClass.getName ())) + DBClass.DB_CLASSSUFFIX);
		repoClass.getImports ().add (domainPackageName + "." + WordUtils.capitalize (CodeGenUtil.normalize (domainClass.getName ())));
		repoClass.setName (tableName);
		repoClass.setRootFolderPath (rootFolderPath);
		repoClass.setPackageName (repositoryPackageName);

		ResultSet pkSet = metaData.getPrimaryKeys (null, null, tableName);
		while (pkSet.next ())
		{
			String pkColName = pkSet.getString ("COLUMN_NAME");
			String pkName = pkSet.getString ("PK_NAME");
			String keySeq = pkSet.getString ("KEY_SEQ");
			domainClass.getPkeys ().put (pkColName, null);
			logger.debug ("PK:ColName:{}, PKName:{}, Key Seq:{}", new Object[] { pkColName, pkName, keySeq });
		}

		repoClass.setPkeys (domainClass.getPkeys ());
		dbClass.setPkeys (domainClass.getPkeys ());
		String generateFKeyRefsStr = this.properties.getProperty ("generate.fkey.references");
		boolean generateFKeyRefs = Boolean.parseBoolean (generateFKeyRefsStr);
		if (generateFKeyRefs)
		{
			ResultSet fkSet = metaData.getImportedKeys (null, null, tableName);
			while (fkSet.next ())
			{
				String fkName = fkSet.getString ("FK_NAME");
				String pkTableName = fkSet.getString ("PKTABLE_NAME");
				logger.debug ("PK Table Name:{}", pkTableName);
				String pkColName = fkSet.getString ("PKCOLUMN_NAME");
				logger.debug ("PK Col Name:{}", pkColName);
				String fkTableName = fkSet.getString ("FKTABLE_NAME");
				logger.debug ("FK Table Name:{}", fkTableName);
				String fkColName = fkSet.getString ("FKCOLUMN_NAME");
				logger.debug ("FK Col Name:{}", fkColName);
				if (this.ignoreFKeys.contains (fkColName))
				{
					logger.debug ("Ignoring Fkey:{}", fkColName);
				}
				else
				{
					ForeignKey fkey = new ForeignKey ();
					fkey.setFkName (fkName);
					fkey.setFkTableName (fkTableName);
					fkey.setFkColumnName (fkColName);
					fkey.setRefTableName (pkTableName);
					fkey.setRefColumnName (pkColName);					
					fkey.setFieldName (CodeGenUtil.removeTrailingId (fkColName));

					domainClass.getFkeys ().put (fkColName, fkey);
					dbClass.getFkeys ().put (fkColName, fkey);
					repoClass.getFkeys ().put (fkColName, fkey);
					
					if (!repoClass.getImports ().contains ("java.util.List"))
						repoClass.getImports ().add ("java.util.List");

				}
			}
		}
		
		ResultSet childRset = metaData.getExportedKeys (null, null, tableName);
		while (childRset.next ())
		{
			String pkName = childRset.getString ("PK_NAME");
			logger.debug ("Child :PK Name:{}", pkName);
			String fkName = childRset.getString ("FK_NAME");
			logger.debug ("Child :FK Name:{}", fkName);
			String pkTableName = childRset.getString ("PKTABLE_NAME");
			logger.debug ("Child :PK Table Name:{}", pkTableName);
			String pkColumnName = childRset.getString ("PKCOLUMN_NAME");
			logger.debug ("Child :PK Column Name:{}", pkColumnName);
			
			String fkTableName = childRset.getString ("FKTABLE_NAME");
			logger.debug ("Child :FK Table Name:{}", fkTableName);
			String fkColumnName = childRset.getString ("FKCOLUMN_NAME");
			logger.debug ("Child :FK Column Name:{}", fkColumnName);
		}

		String relationsStr = this.properties.getProperty ("parent.child.relations");
		if (StringUtils.isNotBlank (relationsStr))
		{
			String[] relations = StringUtils.split (relationsStr, ",");
			for (String relationInfo : relations)
			{
				String[] relationTokens = StringUtils.split (relationInfo, ":");
				if (domainClass.getName ().equals (relationTokens[0]))
				{
					Relation relation = new Relation ();
                    relation.setParent (relationTokens[0].toLowerCase ());
                    relation.setChild (relationTokens[1].toLowerCase ());
					relation.setType (RelationType.getByName (relationTokens[2]));
					List<Relation> domainRelations = domainClass.getRelations ().get (relationTokens[0]);
					if (domainRelations == null)
					{
						domainRelations = new ArrayList<Relation> ();
						domainClass.getRelations ().put (relationTokens[0], domainRelations);// map key is the parent table name
						if (relation.getType () == RelationType.ONE_TO_MANY)
						{
							if (!domainClass.getImports ().contains ("java.util.List"))
								domainClass.getImports ().add ("java.util.List");
							if (!domainClass.getImports ().contains ("java.util.ArrayList"))
								domainClass.getImports ().add ("java.util.ArrayList");
						}
					}
					domainRelations.add (relation);
					
					List<Relation> dbRelations = dbClass.getRelations ().get (relationTokens[0]);
					if (dbRelations == null)
					{
						dbRelations = new ArrayList<Relation> ();
						dbClass.getRelations ().put (relationTokens[0], dbRelations);// map key is the parent table name						
					}
					dbRelations.add (relation);
					
					List<Relation> repoRelations = repoClass.getRelations ().get (relationTokens[0]);
					if (repoRelations == null)
					{
						repoRelations = new ArrayList<Relation> ();
						repoClass.getRelations ().put (relationTokens[0], repoRelations);// map key is the parent table name						
					}
					repoRelations.add (relation);
				}
			}

		}

		ResultSet cset = metaData.getColumns (null, null, tableName, null);
		while (cset.next ())
		{
			String colName = cset.getString ("COLUMN_NAME");
			logger.debug ("Found Column:" + colName);
			int colSize = cset.getInt ("COLUMN_SIZE");
			logger.debug ("Column size:{}", colSize);
			
			String defaultValue = cset.getString ("COLUMN_DEF");
			logger.debug ("Column Default value:{}", defaultValue);
			String nullable = cset.getString ("IS_NULLABLE");
			boolean isNullable = false;
			logger.debug ("Is nullable:{}", nullable);
			if ("YES".equalsIgnoreCase (nullable))
			{
				logger.debug ("{} is nullable", colName);
				isNullable = true;
			}
			else
			{
				logger.debug ("{} is not nullable", colName);
				isNullable = false;
			}

			int type = cset.getInt ("DATA_TYPE");
			logger.debug ("Column DataType:" + type);
			ParameterType fieldType = ParameterType.STRING;

			Parameter parameter = null;
			if ((type == Types.VARCHAR) || (type == Types.LONGVARCHAR) || (type == Types.CLOB))
			{
				fieldType = ParameterType.STRING;
				parameter = new Parameter (colName, ParameterType.STRING);
			}
			else if (type == Types.BIGINT)
			{
				fieldType = ParameterType.LONG;
				parameter = new Parameter (colName, ParameterType.LONG);
			}
			else if ((type == Types.DOUBLE) || (type == Types.NUMERIC))
			{
				fieldType = ParameterType.DOUBLE;
				parameter = new Parameter (colName, ParameterType.DOUBLE);
			}
			else if ((type == Types.FLOAT) || (type == Types.DECIMAL))
			{
				fieldType = ParameterType.FLOAT;
				parameter = new Parameter (colName, ParameterType.FLOAT);
			}
			else if ((type == Types.INTEGER) || (type == Types.SMALLINT) || (type == Types.TINYINT))
			{
				fieldType = ParameterType.INTEGER;
				parameter = new Parameter (colName, ParameterType.INTEGER);
			}
			else if ((type == Types.TIMESTAMP) || (type == Types.TIME) || (type == Types.DATE))
			{
				fieldType = ParameterType.DATE;
				if (!domainClass.getImports ().contains ("java.util.Date"))
				{
					domainClass.getImports ().add ("java.util.Date");
				}
				if (!dbClass.getImports ().contains ("java.sql.Timestamp"))
				{
					dbClass.getImports ().add ("java.sql.Timestamp");
				}
				parameter = new Parameter (colName, ParameterType.DATE);
			}
			else if ((type == Types.BIT) || (type == Types.BOOLEAN))
			{
				fieldType = ParameterType.BOOLEAN;
				parameter = new Parameter (colName, ParameterType.BOOLEAN);
			}
			else if (type == Types.CHAR)
			{
				fieldType = ParameterType.STRING;
				parameter = new Parameter (colName, ParameterType.STRING);
			}
			else
			{
				// no specific type found so set to generic object
				fieldType = ParameterType.OBJECT;
				parameter = new Parameter (colName, ParameterType.OBJECT);
			}
			
			//add to db fields only
			Field dbField = new Field ();
			dbFields.add (dbField);
			dbField.setName (colName);
			dbField.setType (fieldType);
			
			if (!this.ignoreColumnList.contains (colName.toLowerCase ()))
			{
				Method method = new Method ();
				methods.add (method);
				method.setName (colName);
				method.setParameter (parameter);
				
				Field field = new Field ();
				fields.add (field);
				field.setNullable (isNullable);
				field.setSize (colSize);
				field.setName (colName);
				field.setType (fieldType);
				field.setDefaultValue (defaultValue);
				
				boolean isPkCol = domainClass.getPkeys ().containsKey (colName);
				if (isPkCol)
				{
					domainClass.getPkeys ().put (colName, fieldType);
					logger.debug ("Found pk col:{} , type:{}", colName, fieldType.getName ());

				}
			}
			else
			{
				logger.debug ("ColName:{} is in ignore column list, so not adding to domain class", colName);
			}
		}
		
		domainClass.createFile ();
		dbClass.createFile ();
		repoClass.createFile ();
	}

	public Connection getConnection () throws SQLException
	{
		Connection conn = null;
		logger.info ("Trying to connect to db using the properties...");
		String url = this.properties.getProperty ("jdbc.url");
		String userName = this.properties.getProperty ("jdbc.username");
		String password = this.properties.getProperty ("jdbc.password");
		logger.info ("Connecting to database at:[" + url + "]" + " with username/password:[" + userName + "/" + password + "]");
        Properties connProps = new Properties ();
        if (userName == null && password == null)
        {
            conn = DriverManager.getConnection (url);
        }
        else
        {
            connProps.put ("user", userName);
            connProps.put ("password", password);
            conn = DriverManager.getConnection (url, connProps);
        }

		logger.info ("Connected to database");
		return conn;
	}

	




}
