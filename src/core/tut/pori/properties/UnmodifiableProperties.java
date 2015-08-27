/**
 * Copyright 2014 Tampere University of Technology, Pori Unit
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
package core.tut.pori.properties;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.InvalidPropertiesFormatException;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.collections4.map.UnmodifiableEntrySet;

/**
 * Implementation of an unmodifiable/immutable Properties class.
 * 
 * Attempting to call any setter methods of this class will results in UnsupportedOperationException
 */
public class UnmodifiableProperties extends Properties{
	/** for serialization */
	private static final long serialVersionUID = -449888750397691453L;
	private Properties _properties = null;
	
	/**
	 * 
	 */
	private UnmodifiableProperties() {
		super();
	}
	
	/**
	 * wrap the properties object to unmodifiable properties
	 * 
	 * @param properties
	 * @return new UnmodifiableProperties or null if null was passed
	 */
	public static UnmodifiableProperties unmodifiableProperties(Properties properties){
		if(properties == null){
			return null;
		}
		UnmodifiableProperties up = new UnmodifiableProperties();
		up._properties = properties;
		return up;
	}

	/* (non-Javadoc)
	 * @see java.util.Properties#setProperty(java.lang.String, java.lang.String)
	 */
	@Override
	public synchronized Object setProperty(String key, String value) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Not supported.");
	}

	/* (non-Javadoc)
	 * @see java.util.Properties#load(java.io.Reader)
	 */
	@Override
	public synchronized void load(Reader reader) throws IOException, UnsupportedOperationException {
		throw new UnsupportedOperationException("Not supported.");
	}

	/* (non-Javadoc)
	 * @see java.util.Properties#load(java.io.InputStream)
	 */
	@Override
	public synchronized void load(InputStream inStream) throws IOException, UnsupportedOperationException {
		throw new UnsupportedOperationException("Not supported.");
	}

	/* (non-Javadoc)
	 * @see java.util.Properties#save(java.io.OutputStream, java.lang.String)
	 */
	@Override
	public void save(OutputStream out, String comments) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Not supported.");
	}

	/* (non-Javadoc)
	 * @see java.util.Properties#loadFromXML(java.io.InputStream)
	 */
	@Override
	public synchronized void loadFromXML(InputStream in) throws IOException, InvalidPropertiesFormatException, UnsupportedOperationException {
		throw new UnsupportedOperationException("Not supported.");
	}

	/* (non-Javadoc)
	 * @see java.util.Hashtable#put(java.lang.Object, java.lang.Object)
	 */
	@Override
	public synchronized Object put(Object key, Object value) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Not supported.");
	}

	/* (non-Javadoc)
	 * @see java.util.Hashtable#remove(java.lang.Object)
	 */
	@Override
	public synchronized Object remove(Object key) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Not supported.");
	}

	/* (non-Javadoc)
	 * @see java.util.Hashtable#putAll(java.util.Map)
	 */
	@Override
	public synchronized void putAll(Map<? extends Object, ? extends Object> t) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Not supported.");
	}

	/* (non-Javadoc)
	 * @see java.util.Hashtable#clear()
	 */
	@Override
	public synchronized void clear() throws UnsupportedOperationException{
		throw new UnsupportedOperationException("Not supported.");
	}

	/* (non-Javadoc)
	 * @see java.util.Hashtable#keySet()
	 */
	@Override
	public Set<Object> keySet() {
		return Collections.unmodifiableSet(super.keySet());
	}

	/* (non-Javadoc)
	 * @see java.util.Hashtable#entrySet()
	 */
	@Override
	public Set<java.util.Map.Entry<Object, Object>> entrySet() {
		return UnmodifiableEntrySet.unmodifiableEntrySet(super.entrySet());
	}

	/* (non-Javadoc)
	 * @see java.util.Hashtable#values()
	 */
	@Override
	public Collection<Object> values() {
		return Collections.unmodifiableCollection(super.values());
	}

	/* (non-Javadoc)
	 * @see java.util.Properties#store(java.io.Writer, java.lang.String)
	 */
	@Override
	public void store(Writer writer, String comments) throws IOException {
		_properties.store(writer, comments);
	}

	/* (non-Javadoc)
	 * @see java.util.Properties#store(java.io.OutputStream, java.lang.String)
	 */
	@Override
	public void store(OutputStream out, String comments) throws IOException {
		_properties.store(out, comments);
	}

	/* (non-Javadoc)
	 * @see java.util.Properties#storeToXML(java.io.OutputStream, java.lang.String)
	 */
	@Override
	public void storeToXML(OutputStream os, String comment) throws IOException {
		_properties.storeToXML(os, comment);
	}

	/* (non-Javadoc)
	 * @see java.util.Properties#storeToXML(java.io.OutputStream, java.lang.String, java.lang.String)
	 */
	@Override
	public void storeToXML(OutputStream os, String comment, String encoding) throws IOException {
		_properties.storeToXML(os, comment, encoding);
	}

	/* (non-Javadoc)
	 * @see java.util.Properties#getProperty(java.lang.String)
	 */
	@Override
	public String getProperty(String key) {
		return _properties.getProperty(key);
	}

	/* (non-Javadoc)
	 * @see java.util.Properties#getProperty(java.lang.String, java.lang.String)
	 */
	@Override
	public String getProperty(String key, String defaultValue) {
		return _properties.getProperty(key, defaultValue);
	}

	/* (non-Javadoc)
	 * @see java.util.Properties#propertyNames()
	 */
	@Override
	public Enumeration<?> propertyNames() {
		return _properties.propertyNames();
	}

	/* (non-Javadoc)
	 * @see java.util.Properties#stringPropertyNames()
	 */
	@Override
	public Set<String> stringPropertyNames() {
		return _properties.stringPropertyNames();
	}

	/* (non-Javadoc)
	 * @see java.util.Properties#list(java.io.PrintStream)
	 */
	@Override
	public void list(PrintStream out) {
		_properties.list(out);
	}

	/* (non-Javadoc)
	 * @see java.util.Properties#list(java.io.PrintWriter)
	 */
	@Override
	public void list(PrintWriter out) {
		_properties.list(out);
	}

	/* (non-Javadoc)
	 * @see java.util.Hashtable#size()
	 */
	@Override
	public synchronized int size() {
		return _properties.size();
	}

	/* (non-Javadoc)
	 * @see java.util.Hashtable#isEmpty()
	 */
	@Override
	public synchronized boolean isEmpty() {
		return _properties.isEmpty();
	}

	/* (non-Javadoc)
	 * @see java.util.Hashtable#keys()
	 */
	@Override
	public synchronized Enumeration<Object> keys() {
		return _properties.keys();
	}

	/* (non-Javadoc)
	 * @see java.util.Hashtable#elements()
	 */
	@Override
	public synchronized Enumeration<Object> elements() {
		return _properties.elements();
	}

	/* (non-Javadoc)
	 * @see java.util.Hashtable#contains(java.lang.Object)
	 */
	@Override
	public synchronized boolean contains(Object value) {
		return _properties.contains(value);
	}

	/* (non-Javadoc)
	 * @see java.util.Hashtable#containsValue(java.lang.Object)
	 */
	@Override
	public boolean containsValue(Object value) {
		return _properties.containsValue(value);
	}

	/* (non-Javadoc)
	 * @see java.util.Hashtable#containsKey(java.lang.Object)
	 */
	@Override
	public synchronized boolean containsKey(Object key) {
		return _properties.containsKey(key);
	}

	/* (non-Javadoc)
	 * @see java.util.Hashtable#get(java.lang.Object)
	 */
	@Override
	public synchronized Object get(Object key) {
		return _properties.get(key);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@SuppressWarnings("sync-override")
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((_properties == null) ? 0 : _properties.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@SuppressWarnings("sync-override")
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		UnmodifiableProperties other = (UnmodifiableProperties) obj;
		if (_properties == null) {
			if (other._properties != null)
				return false;
		} else if (!_properties.equals(other._properties))
			return false;
		return true;
	}
}
