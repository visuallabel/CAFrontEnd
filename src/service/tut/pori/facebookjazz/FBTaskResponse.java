package service.tut.pori.facebookjazz;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import service.tut.pori.contentanalysis.Definitions;
import service.tut.pori.contentanalysis.PhotoTaskResponse;
import service.tut.pori.contentanalysis.MediaObjectList;

/**
 * A summarization task response received from an analysis back-end.
 * 
 * <h2>Conditional Elements</h>
 * <ul>
 *  <li>{@value service.tut.pori.contentanalysis.Definitions#ELEMENT_PHOTOLIST}</li>
 *  <li>{@value service.tut.pori.contentanalysis.Definitions#ELEMENT_MEDIA_OBJECTLIST}</li>
 * </ul>
 * 
 * At least one of the conditional elements must be present, and contain valid data.
 * 
 * <h3>XML Example</h3>
 * 
 * {@doc.restlet service="[service.tut.pori.facebookjazz.reference.Definitions#SERVICE_FBJ_REFERENCE_EXAMPLE]" method="[service.tut.pori.contentanalysis.Definitions#ELEMENT_TASK_RESULTS]" type="GET" query="" body_uri=""}
 * 
 * @see service.tut.pori.contentanalysis.PhotoList
 * @see service.tut.pori.contentanalysis.MediaObjectList
 *
 */
@XmlRootElement(name=Definitions.ELEMENT_TASK_RESULTS)
@XmlAccessorType(XmlAccessType.NONE)
public class FBTaskResponse extends PhotoTaskResponse {
	@XmlElement(name = Definitions.ELEMENT_MEDIA_OBJECTLIST)
	private MediaObjectList _mediaObjects = null;

	/**
	 * @return the mediaObjects
	 * @see #setMediaObjects(MediaObjectList)
	 */
	public MediaObjectList getMediaObjects() {
		return _mediaObjects;
	}

	/**
	 * @param mediaObjects the mediaObjects to set
	 * @see #getMediaObjects()
	 */
	public void setMediaObjects(MediaObjectList mediaObjects) {
		_mediaObjects = mediaObjects;
	}
}
