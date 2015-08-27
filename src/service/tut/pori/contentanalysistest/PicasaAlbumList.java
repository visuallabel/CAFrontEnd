/**
 * Copyright 2015 Tampere University of Technology, Pori Unit
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
import javax.xml.bind.annotation.XmlRootElement;

import core.tut.pori.http.ResponseData;

/**
 * Contains a collection of picasa albums
 */
@Deprecated
@XmlRootElement(name="albumList")
@XmlAccessorType(XmlAccessType.NONE)
public class PicasaAlbumList extends ResponseData {
	@XmlElement(name = "album")
	private List<PicasaAlbum> _albums = null;

	/**
	 * @return the albums
	 */
	public List<PicasaAlbum> getAlbums() {
		return _albums;
	}

	/**
	 * @param albums the albums to set
	 */
	public void setAlbums(List<PicasaAlbum> albums) {
		_albums = albums;
	}
}
