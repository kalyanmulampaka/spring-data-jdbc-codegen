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

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mulampaka.spring.data.jdbc.codegen.util.CodeGenUtil;

/**
 * Class to represent the generated Java bean Class. Class name is same as the
 * table name in singular form. e.g For employees table , Employee.java is
 * generated
 * 
 * @author Kalyan Mulampaka
 * 
 */
public class DomainClass extends BaseClass
{
	
	final static Logger logger = LoggerFactory.getLogger (DomainClass.class);
	private static String PK_CLASS = "static com.blogspot.nurkiewicz.jdbcrepository.JdbcRepository.pk";

	private boolean generateJsr303Annotations = false;
	private List<String> jsr303InsertGroups = new ArrayList<String> ();
	private List<String> jsr303UpdateGroups = new ArrayList<String> ();

	public DomainClass ()
	{
		super.setInterfaceName ("org.springframework.data.domain.Persistable");
		this.addImports ();
	}
	
	public List<String> getJsr303InsertGroups ()
	{
		return this.jsr303InsertGroups;
	}
	
	public void setJsr303InsertGroups (List<String> jsr303InsertGroups)
	{
		this.jsr303InsertGroups = jsr303InsertGroups;
	}
	
	public List<String> getJsr303UpdateGroups ()
	{
		return this.jsr303UpdateGroups;
	}
	
	public void setJsr303UpdateGroups (List<String> jsr303UpdateGroups)
	{
		this.jsr303UpdateGroups = jsr303UpdateGroups;
	}

	@Override
	protected void addImports ()
	{
		this.imports.add ("org.apache.commons.lang.builder.ToStringBuilder");
	}
	
	public boolean isGenerateJsr303Annotations ()
	{
		return this.generateJsr303Annotations;
	}
	
	public void setGenerateJsr303Annotations (boolean generateJsr303Annotations)
	{
		this.generateJsr303Annotations = generateJsr303Annotations;
		if (this.generateJsr303Annotations)
		{
			this.imports.add ("javax.validation.constraints.Null");
			this.imports.add ("javax.validation.constraints.NotNull");
			this.imports.add ("javax.validation.constraints.Size");
			this.imports.add ("org.hibernate.validator.constraints.NotEmpty");
		}
	}

	@Override
	protected void printClassImplements ()
	{
		if (StringUtils.isNotBlank (interfaceName))
		{
			String implementsClass = this.interfaceName.substring (StringUtils.lastIndexOf (this.interfaceName, ".") + 1);
			sourceBuf.append (" implements " + implementsClass);
			if (!this.pkeys.isEmpty ())
			{
				if (pkeys.size () == 1)
				{
					sourceBuf.append ("<" + this.pkeys.values ().iterator ().next ().getName () + ">");
				}
				else
				{
					sourceBuf.append ("<Object[]>");
				}
			}
		}
		sourceBuf.append ("\n");
	}
	
	protected void printFields ()
	{
		sourceBuf.append ("\tprivate static final long serialVersionUID = 1L;\n\n");

		for (Field field : fields)
		{

			String type = field.getType ().getName ();
			if (field.isPrimitive ())
			{
				type = field.getType ().getPrimitiveName ();
			}

			String fieldName = CodeGenUtil.normalize (field.getName ());
			StringBuffer modifiers = new StringBuffer ("");
			if (!field.getModifiers ().isEmpty ())
			{
				for (String modifier : field.getModifiers ())
				{
					modifiers.append (modifier + " ");
				}
			}

			if (this.generateJsr303Annotations)
			{
				// generate the jsr303 annotations
				if (!field.isNullable ())
				{
					if (field.getType () == ParameterType.STRING)
					{
						sourceBuf.append ("\t@NotEmpty\n");
					}
					else if (field.getName ().equalsIgnoreCase ("id"))
					{
						//update groups
						sourceBuf.append ("\t@NotNull (groups = { ");
						int i = this.jsr303UpdateGroups.size ();
						for (String name : this.jsr303UpdateGroups)
						{
							sourceBuf.append (name + ".class");
							if (--i > 0)
								sourceBuf.append (", ");
						}
						sourceBuf.append (" })\n");
						
						// insert groups
						sourceBuf.append ("\t@Null (groups = { ");
						i = this.jsr303InsertGroups.size ();
						for (String name : this.jsr303InsertGroups)
						{
							sourceBuf.append (name + ".class");
							if (--i > 0)
								sourceBuf.append (", ");
						}
						sourceBuf.append (" })\n");
					}
					else if (field.getName ().endsWith ("id"))
					{
						//update groups
						sourceBuf.append ("\t@NotNull (groups = { ");
						int i = this.jsr303UpdateGroups.size ();
                        i = this.jsr303InsertGroups.size ();
                        if (i > 0)
                        {
                            for (String name : this.jsr303InsertGroups)
                            {
                                sourceBuf.append (name + ".class");
                                if (--i > 0)
                                    sourceBuf.append (", ");
                            }
                            if (!this.jsr303UpdateGroups.isEmpty ())
                            {
                                sourceBuf.append (", ");
                            }
                        }
						for (String name : this.jsr303UpdateGroups)
						{
							sourceBuf.append (name + ".class");
							if (--i > 0)
								sourceBuf.append (", ");
						}

						sourceBuf.append (" })\n");
					}
					else
					{
						sourceBuf.append ("\t@NotNull\n");
					}
				}
				if (field.getSize () > 0)
				{
					if (field.getType () == ParameterType.STRING)
					{
						sourceBuf.append ("\t@Size (max = " + field.getSize () + ")\n");
					}
				}

			}
			sourceBuf.append ("\tprivate " + modifiers.toString () + type + " " + fieldName);
			if (StringUtils.isNotBlank (field.getDefaultValue ()))
			{
				logger.debug ("Found default value:{}", field.getDefaultValue ());
				if (this.pkeys.containsKey (field.getName ()))
				{
					// this is a pk so ignore
					sourceBuf.append (";\n\n");
				}
				else
				{
					ParameterType t = field.getType ();
					String val = field.getDefaultValue ();
					switch (t)
					{
					case BOOLEAN:
						sourceBuf.append (" = " + val + ";\n\n");
						break;
					case INTEGER:
						sourceBuf.append (" = " + Integer.parseInt (val) + ";\n\n");
						break;
					case LONG:
						sourceBuf.append (" = " + Long.parseLong (val) + "L;\n\n");
						break;
					case DOUBLE:
						sourceBuf.append (" = " + Float.parseFloat (val) + "D;\n\n");
						break;
					case FLOAT:
						sourceBuf.append (" = " + Float.parseFloat (val) + "F;\n\n");
						break;
					case DATE:
					case TIMESTAMP:
						if (val.equalsIgnoreCase ("now()"))
						{
							sourceBuf.append (" = new Date ();\n\n");
						}
						else
						{
							sourceBuf.append (";\n\n");
						}
						break;
					case STRING:
					case CHAR:
						DATABASE d = DATABASE.getByName (this.getDbProductName ());
						logger.debug ("Database:{}", d);
						switch (d)
						{
						case POSTGRESQL:
							String[] tokens = StringUtils.split (val, "::"); // usual form 'value' :: character varying
							if (tokens != null && tokens.length > 0)
							{
								sourceBuf.append (" = \"" + tokens[0].substring (1, tokens[0].length () - 1) + "\";\n\n");
							}
							else
							{
								sourceBuf.append (";\n\n");
							}
							break;
						case MYSQL:
							sourceBuf.append (" = \"" + val + "\";\n\n");
							break;
						default:
							sourceBuf.append (";\n\n");
							break;
						}
						break;
					default:
						sourceBuf.append (";\n\n");
					}
				}
			}
			else
			{
				sourceBuf.append (";\n\n");
			}
		}
	}
	
	protected void printFKeyClassFields ()
	{
		// add composite classes from foreign pkeys
		if (!this.fkeys.isEmpty ())
		{
			for (String fkColName : this.fkeys.keySet ())
			{
				ForeignKey fkey = this.fkeys.get (fkColName);
				String refObj = WordUtils.capitalize (CodeGenUtil.normalize (fkey.getRefTableName ()));
				sourceBuf.append ("\tprivate " + refObj + " " + CodeGenUtil.normalize (fkey.getFieldName ()) + ";\n");
				Method method = new Method ();
				methods.add (method);
				method.setName (fkey.getFieldName ());
				Parameter parameter = new Parameter (fkey.getFieldName (), fkey.getRefTableName (), ParameterType.OBJECT);
				method.setParameter (parameter);
			}
		}
		sourceBuf.append ("\n");
	}
	
	protected void printRelations ()
	{
		if (!this.relations.isEmpty ())
		{
			// get the relations where this table is a parent
			List<Relation> relations = this.relations.get (this.name);
			if (relations != null && !relations.isEmpty ())
			{
				for (Relation relation : relations)
				{
					switch (relation.getType ())
					{
					case ONE_TO_ONE:
						String child = CodeGenUtil.normalize (relation.getChild ());
						sourceBuf.append ("\tprivate " + WordUtils.capitalize (child));
						sourceBuf.append (" " + child + ";\n");
						
						Method method = new Method ();
						methods.add (method);
						method.setName (child);
						Parameter parameter = new Parameter (child, ParameterType.OBJECT);
						method.setParameter (parameter);

						break;
					case ONE_TO_MANY:
						child = CodeGenUtil.normalize (relation.getChild ());
						String fieldName = child;
						if (!StringUtils.endsWith (fieldName, "s"))
						{
							fieldName += "s";
						}
						sourceBuf.append ("\tprivate List<" + WordUtils.capitalize (child));
						sourceBuf.append ("> " + fieldName + " = new ArrayList<" + WordUtils.capitalize (child) + "> ();\n");
						
						method = new Method ();
						methods.add (method);
						method.setName (child);
						parameter = new Parameter (fieldName, ParameterType.LIST);
						method.setParameter (parameter);

						break;
					case UNKNOWN:
						break;
					}
				}
				sourceBuf.append ("\n");
			}
			
		}

	}

	protected void printInterfaceImpl ()
	{
		// add the interface impl methods
		
		if (this.pkeys.size () > 1)
		{
			sourceBuf.append ("\tpublic Object[] getId()\n");
			super.printOpenBrace (1, 1);
			sourceBuf.append ("\t\treturn pk(");
			int i = this.pkeys.size ();
			for (String key : this.pkeys.keySet ())
			{
				sourceBuf.append ("this." + CodeGenUtil.normalize (key));
				if (--i > 0)
				{
					sourceBuf.append (", ");
				}
			}
			sourceBuf.append (");\n");
			super.printCloseBrace (1, 2);

			sourceBuf.append ("\tpublic boolean isNew ()\n");
			super.printOpenBrace (1, 1);
			sourceBuf.append ("\t\treturn !persisted;\n");
			super.printCloseBrace (1, 2);
		}
		else if (this.pkeys.size () == 1)
		{
			String key = this.pkeys.keySet ().iterator ().next ();
			ParameterType keyType = this.pkeys.get (key);
			sourceBuf.append ("\tpublic " + keyType.getName () + " getId ()\n");
			super.printOpenBrace (1, 1);
			sourceBuf.append ("\t\treturn this." + CodeGenUtil.normalize (key) + ";\n");
			super.printCloseBrace (1, 2);
			
			sourceBuf.append ("\tpublic boolean isNew ()\n");
			sourceBuf.append ("\t{\n");
			sourceBuf.append ("\t\treturn this." + CodeGenUtil.normalize (key) + " == null;\n");
			super.printCloseBrace (1, 2);
			if ("id".equalsIgnoreCase (key))
			{
				sourceBuf.append ("\tpublic void setId(" + keyType.getName () + " id)\n");
				sourceBuf.append ("\t{\n");
				sourceBuf.append ("\t\tthis.id = id;\n");
				super.printCloseBrace (1, 2);
			}

		}
		else
		{
			sourceBuf.append ("\tpublic boolean isNew ()\n");
			super.printOpenBrace (1, 1);
			sourceBuf.append ("\t\treturn !persisted;\n");
			super.printCloseBrace (1, 2);
		}
	}
	
	protected void printMethods ()
	{
		for (Method method : methods)
		{
			String methodName = WordUtils.capitalize (CodeGenUtil.normalize (method.getName ()));
			String paramName = CodeGenUtil.normalize (method.getParameter ().getName ());
			String paramType = "";
			ParameterType pType = method.getParameter ().getType ();
			if (pType == ParameterType.OBJECT)
			{
				String name = method.getParameter ().getClassName ();
				if (StringUtils.isBlank (name))
					name = method.getParameter ().getName ();
				paramType = WordUtils.capitalize (CodeGenUtil.normalize (name));
			}
			else
			{
				paramType = pType.getName ();
			}
			String fieldName = method.getParameter ().getName ();

			if (fieldName.equalsIgnoreCase ("id") && this.pkeys.containsKey (fieldName))
			{
				// id 
				logger.debug ("Found id as pk, it is handled in the pk section, so not adding setter and getter");
				continue;
			}

			// setter
			if (method.isGenerateSetter ())
			{
				if (pType == ParameterType.LIST)
				{
					String mName = methodName;
					if (!StringUtils.endsWith (methodName, "s"))
					{
						// pluralize
						mName += "s";
					}
					sourceBuf.append ("\tpublic void set" + mName + " (");
					sourceBuf.append ("List<" + methodName + "> " + paramName);
					sourceBuf.append (")\n");
					
					// implementation
					super.printOpenBrace (1, 1);
					sourceBuf.append ("\t\tthis." + paramName + " = " + paramName + ";\n");
					super.printCloseBrace (1, 2);
				}
				else
				{
					sourceBuf.append ("\tpublic void set" + methodName + " (");
					sourceBuf.append (paramType + " " + paramName);
					sourceBuf.append (")\n");
					
					// implementation
					super.printOpenBrace (1, 1);
					sourceBuf.append ("\t\tthis." + paramName + " = " + paramName + ";\n");
					super.printCloseBrace (1, 2);
				}
			}

			// getter
			if (method.isGenerateGetter ())
			{
				if (pType == ParameterType.LIST)
				{
					String mName = methodName;
					if (!StringUtils.endsWith (methodName, "s"))
					{
						// pluralize
						mName += "s";
					}
					sourceBuf.append ("\tpublic List<" + methodName + "> get" + mName + " ()\n");
					super.printOpenBrace (1, 1);
					sourceBuf.append ("\t\treturn this." + paramName + ";\n");
					super.printCloseBrace (1, 2);
				}
				else
				{
					sourceBuf.append ("\tpublic " + paramType + " get" + methodName + " ()\n");
					super.printOpenBrace (1, 1);
					sourceBuf.append ("\t\treturn this." + paramName + ";\n");
					super.printCloseBrace (1, 2);
				}
			}
		}
	}
	
	protected void preprocess ()
	{
		if (this.pkeys.size () > 0)
		{
			if (this.pkeys.size () > 1)
			{
				if (!this.imports.contains (PK_CLASS))
					this.imports.add (PK_CLASS);
			}
			Field persistedField = new Field ();
			persistedField.setName ("persisted");
			persistedField.setPrimitive (true);
			persistedField.setPersistable (false);// this is not saved in db
			persistedField.setType (ParameterType.BOOLEAN);
			persistedField.getModifiers ().add ("transient");
			fields.add (persistedField);
			Method method = new Method ();
			method.setName (persistedField.getName ());
			method.setParameter (new Parameter (persistedField.getName (), ParameterType.BOOLEAN));
			methods.add (method);
		}
		
	}

	public void generateSource ()
	{
		this.preprocess ();

		super.printPackage ();
		super.printImports ();
		super.printClassComments ();
		super.printClassDefn ();
		this.printClassImplements ();

		super.printOpenBrace (0, 2);
		
		this.printFields ();
		
		this.printFKeyClassFields ();
		
		this.printRelations ();

		super.printCtor ();
		
		this.printInterfaceImpl ();
		
		this.printMethods ();

		super.printToString ();

		super.printUserSourceCode ();

		super.printCloseBrace (0, 0); // end of class
		//logger.debug ("Printing Class file content:\n" + sourceBuf.toString ());

	}

}
