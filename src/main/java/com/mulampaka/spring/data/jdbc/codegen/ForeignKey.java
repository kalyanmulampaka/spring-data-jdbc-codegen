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

/**
 * Class to represent a foreign key
 * 
 * @author kmulampaka
 * 
 */
public class ForeignKey
{
	private String fkName;
	private String fkTableName;
	private String fkColumnName;
	private String refTableName;
	private String refColumnName;
	
	public ForeignKey ()
	{
		
	}
	
	public String getFkName ()
	{
		return this.fkName;
	}
	
	public void setFkName (String fkName)
	{
		this.fkName = fkName;
	}
	
	public String getFkTableName ()
	{
		return this.fkTableName;
	}
	
	public void setFkTableName (String fkTableName)
	{
		this.fkTableName = fkTableName;
	}
	
	public String getFkColumnName ()
	{
		return this.fkColumnName;
	}
	
	public void setFkColumnName (String fkColumnName)
	{
		this.fkColumnName = fkColumnName;
	}
	
	public String getRefTableName ()
	{
		return this.refTableName;
	}
	
	public void setRefTableName (String refTableName)
	{
		this.refTableName = refTableName;
	}
	
	public String getRefColumnName ()
	{
		return this.refColumnName;
	}
	
	public void setRefColumnName (String refColumnName)
	{
		this.refColumnName = refColumnName;
	}
	

}
