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
package service.tut.pori.contentanalysis;

import java.util.Properties;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import core.tut.pori.properties.SystemProperty;

/**
 * The properties for Content Analysis service.
 * 
 */
public class CAProperties extends SystemProperty {
	/** 
	 * value used for max task size when the limit is disabled.
	 * 
	 * @see #getMaxTaskSize()
	 */
	public static final int MAX_TASK_SIZE_DISABLED = -1;
	/** 
	 * value used for max task delay when the schedule delay is disabled.
	 * 
	 * @see #getScheduleTaskDelay()
	 */
	public static final int TASK_DELAY_DISABLED = -1;
	/* properties */
	private static final String PROPERTY_SERVICE_TUT_PORI_CA = PROPERTY_SERVICE_PORI+".contentanalysis";
	private static final String PROPERTY_SERVICE_TUT_PORI_CA_MAX_TASK_SIZE = PROPERTY_SERVICE_TUT_PORI_CA+".max_task_size";
	private static final String PROPERTY_SERVICE_TUT_PORI_CA_RESOLVE_FRIENDLY_KEYWORDS = PROPERTY_SERVICE_TUT_PORI_CA+".resolve_friendly_keywords";
	private static final String PROPERTY_SERVICE_TUT_PORI_CA_SCHEDULE_TASK_DELAY = PROPERTY_SERVICE_TUT_PORI_CA+".schedule_task_delay";
	private int _maxTaskSize = -1;
	private boolean _resolveFriendlyKeywords = false;
	private long _scheduleTaskDelay = -1;
	
	@Override
	public void initialize(Properties properties) throws IllegalArgumentException {
		String property = properties.getProperty(PROPERTY_SERVICE_TUT_PORI_CA_RESOLVE_FRIENDLY_KEYWORDS);
		if(StringUtils.isBlank(property)){
			throw new IllegalArgumentException("Bad "+PROPERTY_SERVICE_TUT_PORI_CA_RESOLVE_FRIENDLY_KEYWORDS);
		}
		_resolveFriendlyKeywords = BooleanUtils.toBoolean(property);
		
		try{
			_maxTaskSize = Integer.parseInt(properties.getProperty(PROPERTY_SERVICE_TUT_PORI_CA_MAX_TASK_SIZE));
		} catch (NumberFormatException ex){
			Logger.getLogger(getClass()).warn(ex, ex);
			throw new IllegalArgumentException("Bad "+PROPERTY_SERVICE_TUT_PORI_CA_MAX_TASK_SIZE);
		}
		if(_maxTaskSize != MAX_TASK_SIZE_DISABLED && _maxTaskSize < 1){
			Logger.getLogger(getClass()).debug("Max task size was < 1, reseting to default disabled value: "+MAX_TASK_SIZE_DISABLED);
			_maxTaskSize = MAX_TASK_SIZE_DISABLED;
		}
		
		try{
			_scheduleTaskDelay = Long.parseLong(properties.getProperty(PROPERTY_SERVICE_TUT_PORI_CA_SCHEDULE_TASK_DELAY));
		} catch (NumberFormatException ex){
			Logger.getLogger(getClass()).warn(ex, ex);
			throw new IllegalArgumentException("Bad "+PROPERTY_SERVICE_TUT_PORI_CA_SCHEDULE_TASK_DELAY);
		}
		if(_scheduleTaskDelay != TASK_DELAY_DISABLED && _scheduleTaskDelay < 1){
			Logger.getLogger(getClass()).debug("Task schedule delay was < 1, reseting to default disabled value: "+TASK_DELAY_DISABLED);
			_scheduleTaskDelay = TASK_DELAY_DISABLED;
		}
	}

	/**
	 * @return the resolveFriendlyKeywords
	 */
	public boolean isResolveFriendlyKeywords() {
		return _resolveFriendlyKeywords;
	}

	/**
	 * The recommended maximum photo count in a single task. Value is never 0, and less than 0 if the limit is disabled.
	 * 
	 * @return the maxTaskSize
	 */
	public int getMaxTaskSize() {
		return _maxTaskSize;
	}

	/**
	 * The default task schedule delay. The tasks should be scheduled to start immediately if the value is less than 1.
	 * 
	 * @return the scheduleTaskDelay
	 */
	public long getScheduleTaskDelay() {
		return _scheduleTaskDelay;
	}
	
	@Override
	public String getPropertyFilePath() {
		return CONFIGURATION_FILE_PATH+Definitions.PROPERTY_FILE;
	}
}
