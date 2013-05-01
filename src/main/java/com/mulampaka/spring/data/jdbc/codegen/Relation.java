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

public class Relation
{
	
	private String parent;
	private String child;
	private RelationType type;

	public Relation ()
	{
		
	}
	
	public enum RelationType
	{
		ONE_TO_MANY ("OneToMany"),
		ONE_TO_ONE ("OneToOne"),
		UNKNOWN ("Unknown");
		
		private String name;
		
		private RelationType (String name)
		{
			this.name = name;
		}
		
		public String getName ()
		{
			return this.name;
		}
		
		public void setName (String name)
		{
			this.name = name;
		}
		
		public static RelationType getByName (String name)
		{
			for (RelationType t : RelationType.values ())
			{
				if (t.getName ().equalsIgnoreCase (name))
					return t;
			}
			return UNKNOWN;
		}

	}
	
	public String getParent ()
	{
		return this.parent;
	}
	
	public void setParent (String parent)
	{
		this.parent = parent;
	}
	
	public String getChild ()
	{
		return this.child;
	}
	
	public void setChild (String child)
	{
		this.child = child;
	}
	
	public RelationType getType ()
	{
		return this.type;
	}
	
	public void setType (RelationType type)
	{
		this.type = type;
	}
	

}
