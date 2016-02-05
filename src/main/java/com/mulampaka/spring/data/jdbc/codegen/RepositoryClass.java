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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mulampaka.spring.data.jdbc.codegen.util.CodeGenUtil;

public class RepositoryClass extends BaseClass
{
	
	final static Logger logger = LoggerFactory.getLogger (RepositoryClass.class);
	private static String CLASS_SUFFIX = "Repository";

	private static String TBL_DESC_CLASS = "com.nurkiewicz.jdbcrepository.TableDescription";

	public RepositoryClass ()
	{
		this.classSuffix = CLASS_SUFFIX;
		super.setExtendsClassName ("com.nurkiewicz.jdbcrepository.JdbcRepository");
		this.addImports ();
	}
	
	@Override
	protected void addImports ()
	{		
		this.imports.add("org.springframework.beans.factory.annotation.Autowired");
		this.imports.add("org.springframework.jdbc.core.JdbcOperations");
		this.imports.add("org.springframework.stereotype.Repository");	
	}

	protected void printClassAnnotations ()
	{
		sourceBuf.append ("@Repository\n");
	}
	
	@Override
	protected void printClassExtends ()
	{
		super.printClassExtends ();

		if (StringUtils.isNotBlank (extendsClassName))
		{
			sourceBuf.append ("<");
			sourceBuf.append (this.name + ", ");
			if (this.pkeys.size () == 0)
			{
				sourceBuf.append ("String");
			}
			else if (pkeys.size () == 1)
			{
				sourceBuf.append (this.pkeys.values ().iterator ().next ().getName ());
			}
			else
			{
				sourceBuf.append ("Object[]");
			}
			sourceBuf.append (">");

		}
	}
	
	@Override
	protected void printCtor ()
	{
		if (this.pkeys.size () == 0)
		{
			// add ctor
			sourceBuf.append ("\tpublic " + this.name + this.classSuffix + "()\n");
			super.printOpenBrace (1, 1);
			sourceBuf.append ("\t\tsuper (" + this.name + DBClass.DB_CLASSSUFFIX + ".ROW_MAPPER, " + this.name + DBClass.DB_CLASSSUFFIX + ".ROW_UNMAPPER, "
					+ this.name + DBClass.DB_CLASSSUFFIX + ".getTableName (),");
			sourceBuf.append (this.name + DBClass.DB_CLASSSUFFIX + ".COLUMNS.ID.getColumnName()");
			sourceBuf.append (");\n");
			super.printCloseBrace (1, 2);
			
			// add postcreate
			sourceBuf.append ("\t@Override\n");
			sourceBuf.append ("\tprotected " + this.name + " postCreate(" + this.name + " entity, Number generatedId)\n");
			super.printOpenBrace (1, 1);
			sourceBuf.append ("\t\tentity.setId(generatedId.intValue());\n");
			sourceBuf.append ("\t\tentity.setPersisted(true);\n");
			sourceBuf.append ("\t\treturn entity;\n");
			super.printCloseBrace (1, 2);
		}
		else if (this.pkeys.size () == 1)
		{
			// add ctor
			sourceBuf.append ("\tpublic " + this.name + this.classSuffix + "()\n");
			super.printOpenBrace (1, 1);
			sourceBuf.append ("\t\tsuper (" + this.name + DBClass.DB_CLASSSUFFIX + ".ROW_MAPPER, " + this.name + DBClass.DB_CLASSSUFFIX + ".ROW_UNMAPPER, "
					+ this.name + DBClass.DB_CLASSSUFFIX + ".getTableName ());\n");
			super.printCloseBrace (1, 2);
			
			// add ctor2
			sourceBuf.append ("\tpublic " + this.name + this.classSuffix + "(");

			sourceBuf.append ("RowMapper<" + this.name + "> rowMapper, RowUnmapper<" + this.name + "> rowUnmapper, String idColumn)\n");
			super.printOpenBrace (1, 1);

			sourceBuf.append ("\t\tsuper (" + this.name + DBClass.DB_CLASSSUFFIX + ".ROW_MAPPER, " + this.name + DBClass.DB_CLASSSUFFIX + ".ROW_UNMAPPER, " + this.name + DBClass.DB_CLASSSUFFIX + ".getTableName (), idColumn);\n");
			super.printCloseBrace (1, 2);
			
			// add postcreate
			sourceBuf.append ("\t@Override\n");
			sourceBuf.append ("\tprotected " + this.name + " postCreate(" + this.name + " entity, Number generatedId)\n");
			super.printOpenBrace (1, 1);
			String key = this.pkeys.keySet ().iterator ().next ();
			ParameterType keyType = this.pkeys.values ().iterator ().next ();
			String keyTypeName = "";
			switch (keyType)
			{
			case INTEGER:
				keyTypeName = "int";
				break;
			case LONG:
				keyTypeName = "long";
				break;
			case DOUBLE:
				keyTypeName = "double";
				break;
			default:
				keyTypeName = "int";
			}
			if ("id".equalsIgnoreCase (key))
			{
				sourceBuf.append ("\t\tentity.setId(generatedId." + keyTypeName + "Value());\n");
			}
			sourceBuf.append ("\t\tentity.setPersisted(true);\n");
			sourceBuf.append ("\t\treturn entity;\n");
			super.printCloseBrace (1, 2);
		}
		else
		{
			// add ctor
			sourceBuf.append ("\tpublic " + this.name + this.classSuffix + "()\n");
			super.printOpenBrace (1, 1);
			sourceBuf.append ("\t\tsuper (" + this.name + DBClass.DB_CLASSSUFFIX + ".ROW_MAPPER, " + this.name + DBClass.DB_CLASSSUFFIX + ".ROW_UNMAPPER, "
					+ "new TableDescription(" + this.name + DBClass.DB_CLASSSUFFIX + ".getTableName (), null,");
			int i = this.pkeys.size ();
			for (String key : this.pkeys.keySet ())
			{
				sourceBuf.append (this.name + DBClass.DB_CLASSSUFFIX + ".COLUMNS." + key.toUpperCase () + ".getColumnName()");
				--i;
				if (i > 0)
					sourceBuf.append (",");
			}
			sourceBuf.append ("));\n");
			super.printCloseBrace (1, 2);
			
			// add postcreate
			sourceBuf.append ("\t@Override\n");
			sourceBuf.append ("\tprotected " + this.name + " postCreate(" + this.name + " entity, Number generatedId)\n");
			super.printOpenBrace (1, 1);
			sourceBuf.append ("\t\tentity.setPersisted(true);\n");
			sourceBuf.append ("\t\treturn entity;\n");
			super.printCloseBrace (1, 2);

		}
		sourceBuf.append ("\n");
	}
	

	protected void printFKeyMethods ()
	{
		logger.debug ("Generating foreign pkeys methods: # of FKeys:{} ", this.fkeys.size ());
		// add methods from foreign pkeys
		if (!this.fkeys.isEmpty ())
		{
			for (String fkColName : this.fkeys.keySet ())
			{
				ForeignKey fkey = this.fkeys.get (fkColName);
				String refObj = WordUtils.capitalize (CodeGenUtil.normalize (fkey.getFkTableName ()));
				String methodClassName = CodeGenUtil.pluralizeName (refObj, this.getDontPluralizeWords ());
				sourceBuf.append ("\tpublic List<" + refObj + "> get" + methodClassName + "By" + WordUtils.capitalize (CodeGenUtil.normalize (fkColName)) + " (Long " + CodeGenUtil.normalize (fkColName) + ")\n");
				this.printOpenBrace (1, 1);
				sourceBuf.append ("\t\tString sql = \"select * from \" + " + refObj + DBClass.DB_CLASSSUFFIX + ".getTableName() + " + "\" where \" + " + refObj + DBClass.DB_CLASSSUFFIX + ".COLUMNS." + fkColName.toUpperCase () + ".getColumnName() + \" = ? \";\n");
				sourceBuf.append ("\t\treturn this.jdbcOperations.query (sql, new Object[] { " + CodeGenUtil.normalize (fkColName) + " }, " + refObj + DBClass.DB_CLASSSUFFIX + ".ROW_MAPPER);\n");
				this.printCloseBrace (1, 2);
			}
		}
		sourceBuf.append ("\n");
	}

	protected void preprocess ()
	{
		if (this.pkeys.size () != 1)
		{
			if (!this.imports.contains (TBL_DESC_CLASS))
				this.imports.add (TBL_DESC_CLASS);
		}
		else
		{
			if (!this.imports.contains ("org.springframework.jdbc.core.RowMapper"))
				this.imports.add ("org.springframework.jdbc.core.RowMapper");
			if (!this.imports.contains ("com.nurkiewicz.jdbcrepository.RowUnmapper"))
				this.imports.add ("com.nurkiewicz.jdbcrepository.RowUnmapper");
		}
	}
	
	protected void printFields () 
	{
		this.sourceBuf.append("\t@Autowired\n\tprivate JdbcOperations jdbcOperations;\n\n");
	}
	
	protected void printMethods () 
	{
		this.sourceBuf.append("\tprotected JdbcOperations getJdbcOperations()\n");
		this.sourceBuf.append("\t{\n");
		this.sourceBuf.append("\t\treturn this.jdbcOperations;\n");
		this.sourceBuf.append("\t}\n\n");
	}

	@Override
	public void generateSource ()
	{
		this.preprocess ();

		this.name = WordUtils.capitalize (CodeGenUtil.normalize (this.name));
		
		super.printPackage ();
		super.printImports ();
		super.printClassComments ();
		
		this.printClassAnnotations ();
		super.printClassDefn ();
		this.printClassExtends ();
		super.printClassImplements ();

		super.printOpenBrace (0, 2);
		super.printLogger ();
		this.printFields();
		this.printMethods();
		this.printCtor ();
		
		this.printFKeyMethods ();
		
		super.printUserSourceCode ();

		super.printCloseBrace (0, 2);
	}
	
	

}
