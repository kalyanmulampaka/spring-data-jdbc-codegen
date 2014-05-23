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
package com.mulampaka.spring.data.jdbc.codegen.util;

import java.io.File;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class used in code generation
 * 
 * @author Kalyan Mulampaka
 * 
 */
public class CodeGenUtil
{

	final static Logger logger = LoggerFactory.getLogger(CodeGenUtil.class);

	public CodeGenUtil ()
	{

	}

	/**
	 * Returns the java bean style name for the input database column name.
	 * assumes database column name words are separated by underscores ('_'). If
	 * there are no underscores the input string is returned as is. e.g
	 * created_at will return createdAt
	 * 
	 * @param name
	 * @return
	 */
	public static String normalize (String name)
	{
		StringBuilder strBuf = new StringBuilder("");
		// e,g created_at
		// should become createdAt

		char[] delimiter =
		{ '_' };
		if (name.indexOf("_") != -1)
		{
			name = WordUtils.capitalize(name, delimiter); // CreatedAt
			name = StringUtils.uncapitalize(name); // createdAt
			name = StringUtils.replace(name, "_", "");
		}
		else
		{
			name = StringUtils.uncapitalize (name); // id will return as id
		}
		strBuf.append(name);
		return strBuf.toString();
	}
	
	public static String removeTrailingId (String name)
	{
		if (StringUtils.endsWith (name, "id"))
		{
			name = name.substring (0, name.length () - 2);
		}
		name = CodeGenUtil.normalize (name);
		return name;
	}

	public static String createTableAlias (String tableName)
	{
		// device should return dev
		// device_to_application should return dta
		// 
		StringBuilder strBuf = new StringBuilder ("");
		String[] words = StringUtils.split (tableName, '_');
		if (words.length == 1)
		{
			strBuf.append (words[0].substring (0, 3));
		}
		else
		{
			for (String word : words)
			{
				strBuf.append (word.charAt (0));
			}
		}
		return strBuf.toString ();
	}

	/**
	 * Removes the packages folder structure if present
	 * 
	 * @param packageName
	 *            String
	 * @throws Exception
	 */
	public static void cleanup (String rootFolderPath, String packageName) throws Exception
	{
		String path = "";
		if (StringUtils.isNotBlank (packageName))
		{
			path = StringUtils.replace (packageName, ".", "/");
			if (StringUtils.isNotBlank (rootFolderPath))
			{
				path = rootFolderPath + "/" + path;
			}
			logger.info ("Deleting dir structure:{}", path);
			File file = new File (path);
			if (file.exists ())
			{
				// remove the dir structure
				FileUtils.deleteQuietly (file);
				logger.debug ("Deleted directory structure:{}", path);
			}
			else
			{
				logger.info ("Package structure does not exist:" + path);
			}
		}
	}

	public static boolean checkIfSourceCodeExists (String rootFolderPath, String packageName) throws Exception
	{
		boolean exists = false;
		String path = "";
		if (StringUtils.isNotBlank (packageName))
		{
			path = StringUtils.replace (packageName, ".", "/");
			if (StringUtils.isNotBlank (rootFolderPath))
			{
				path = rootFolderPath + "/" + path;
			}
			logger.info ("Checking if dir structure:{} exists", path);
			File file = new File (path);
			if (file.exists () && file.list ().length > 0)
			{
				logger.debug ("Found package structure:{} with {} files", path, file.list ().length);
				exists = true;
			}
			else
			{
				logger.info ("Package structure does not exist:" + path);
				exists = false;
			}
		}
		return exists;
	}

	/**
	 * Creates the package folder structure if already not present
	 * 
	 * @param packageName
	 *            String
	 * @throws Exception
	 */
	public static void createPackage (String rootFolderPath, String packageName) throws Exception
	{
		String path = "";
		if (StringUtils.isNotBlank(packageName))
		{
			path = StringUtils.replace(packageName, ".", "/");
			if (StringUtils.isNotBlank(rootFolderPath))
			{
				path = rootFolderPath + "/" + path;
			}
			logger.info ("Generated code will be in folder:{}", path);
			File file = new File (path);
			if (!file.exists())
			{
				file.mkdirs();
				logger.info("Package structure created:" + path);
			}
			else
			{
				logger.info ("Package structure:{} exists.", path);
			}
		}
	}

	public static String pluralizeName (String name, String[] dontPluralizeWords)
	{
		StringBuffer buf = new StringBuffer (name);
		if (dontPluralizeWords != null && dontPluralizeWords.length > 0)
		{
			for (String word : dontPluralizeWords)
			{
				if (StringUtils.endsWith (name.toLowerCase (), word.trim ()))
					return name;
			}
		}
		if (StringUtils.endsWith (name, "y"))
		{
			buf = new StringBuffer (name.substring (0, name.length () - 1));
			buf.append ("ies");
		}
		else if (StringUtils.endsWith (name, "z"))
		{
			buf.append ("zes");
		}
		else if (!StringUtils.endsWith (name, "s")) // has to be the last condition
		{
			// pluralize
			buf.append ("s");
		}
		return buf.toString ();
		
	}

}