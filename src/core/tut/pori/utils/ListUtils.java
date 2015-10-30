/**
 * Copyright 2014 Tampere University of Technology, Pori Department
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package core.tut.pori.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;

/**
 * 
 * Utility class for List utils
 */
public final class ListUtils {
	private static final Logger LOGGER = Logger.getLogger(ListUtils.class);
	/**
	 * 
	 */
	private ListUtils(){
		// nothing needed
	}

	/**
	 * Create a new list with n copies of new instances of the given class
	 * 
	 * @param n
	 * @param cls
	 * @return new list or null if n &lt; 1 or cls == null
	 * @throws IllegalArgumentException if the classes cannot be instantiated
	 */
	public static <T> List<T> createList(int n, Class<T> cls){
		if(n < 1 || cls == null){
			return null;
		}
		List<T> list = new ArrayList<>(n);
		try {
			for(int i=0;i<n;++i){
				list.add(cls.newInstance());
			}
		} catch (InstantiationException | IllegalAccessException ex) {
			LOGGER.error(ex, ex);
			throw new IllegalArgumentException("Could not create instance of "+cls.toString());
		}
		return list;
	}
	
	/**
	 * 
	 * @param items
	 * @return true if items is null or empty
	 */
	public static boolean isEmpty(Collection<?> items){
		return (items == null || items.isEmpty());
	}
	
	/**
	 * 
	 * @param values
	 * @return new list or null if null or empty array was passed
	 */
	public static List<Integer> createList(int[] values){
		if(ArrayUtils.isEmpty(values)){
			return null;
		}
		List<Integer> iValues = new ArrayList<>(values.length);
		for(int i=0;i<values.length;++i){
			iValues.add(Integer.valueOf(values[i]));
		}
		return iValues;
	}
}
