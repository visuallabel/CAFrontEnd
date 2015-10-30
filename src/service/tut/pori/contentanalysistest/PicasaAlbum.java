/**
 * Copyright 2015 Tampere University of Technology, Pori Department
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
package service.tut.pori.contentanalysistest;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import service.tut.pori.contentanalysis.Definitions;

/**
 * Contains details of a single picasa album
 */
@Deprecated
@XmlRootElement(name="album")
@XmlAccessorType(XmlAccessType.NONE)
public class PicasaAlbum {
	@XmlElement(name = "albumId")
	private String _id = null;
	@XmlElement(name = "name")
	private String _name = null;
	@XmlElement(name = Definitions.ELEMENT_GUID)
	@XmlElementWrapper(name = "UIDList")
	private List<String> _guids = null;
	/**
	 * @return the id
	 */
	public String getId() {
		return _id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		_id = id;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return _name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		_name = name;
	}
	/**
	 * @return the guids
	 */
	public List<String> getGUIDs() {
		return _guids;
	}
	/**
	 * @param guids the guids to set
	 */
	public void setGUIDs(List<String> guids) {
		_guids = guids;
	}
}
