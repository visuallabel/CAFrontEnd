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

import java.util.Date;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * maps ISODate format to string and back
 * 
 * usage:
 * - annotate the class' member variable with: \@XmlJavaTypeAdapter(ISODateAdapter.class)
 *
 */
public class ISODateAdapter extends XmlAdapter<String, Date>{

	@Override
	public Date unmarshal(String v) throws Exception {
		return StringUtils.ISOStringToDate(v);
	}

	@Override
	public String marshal(Date v) throws Exception {
		return StringUtils.dateToISOString(v);
	}
}
