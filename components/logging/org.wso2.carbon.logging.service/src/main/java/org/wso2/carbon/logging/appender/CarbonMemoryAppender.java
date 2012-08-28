package org.wso2.carbon.logging.appender;

/* 
 * Copyright 2005,2006 WSO2, Inc. http://www.wso2.org
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */


import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.logging.appenders.CircularBuffer;
import org.wso2.carbon.utils.logging.TenantAwareLoggingEvent;
import org.wso2.carbon.utils.multitenancy.CarbonApplicationContextHolder;
import org.wso2.carbon.utils.multitenancy.CarbonContextHolder;


/**
 * This appender will be used to capture the logs and later send to clients, if requested via the
 * logging web service.
 * This maintains a circular buffer, of some fixed amount (say 100).
 */
public class CarbonMemoryAppender extends AppenderSkeleton {

    private CircularBuffer circularBuffer;
    private int bufferSize = -1;
    private String columnList;

	public String getColumnList() {
		return columnList;
	}

	public void setColumnList(String columnList) {
		this.columnList = columnList;
	}

	public CarbonMemoryAppender() {
        
    }

    public CarbonMemoryAppender(CircularBuffer circularBuffer) {
        this.circularBuffer = circularBuffer;
    }

    protected void append(LoggingEvent loggingEvent) {
        int tenantId = CarbonContext.getCurrentContext().getTenantId();
        String appName = CarbonApplicationContextHolder.getThreadLocalCarbonApplicationContextHolder().getApplicationName();
    	Logger logger = Logger.getLogger(loggingEvent.getLoggerName());
		TenantAwareLoggingEvent tenantEvent;
		if (loggingEvent.getThrowableInformation() != null) {
			tenantEvent = new TenantAwareLoggingEvent(loggingEvent.fqnOfCategoryClass, logger,
					loggingEvent.timeStamp, loggingEvent.getLevel(), loggingEvent.getMessage(), loggingEvent
							.getThrowableInformation().getThrowable());
		} else {
			tenantEvent = new TenantAwareLoggingEvent(loggingEvent.fqnOfCategoryClass, logger,
					loggingEvent.timeStamp, loggingEvent.getLevel(), loggingEvent.getMessage(), null);
		}
	     tenantEvent.setTenantId(Integer.toString(tenantId));
            tenantEvent.setServiceName(appName);
        if (circularBuffer != null) {
            circularBuffer.append(tenantEvent);
        }
    }

    public void close() {
        // do we need to do anything here. I hope we do not need to reset the queue
        // as it might still be exposed to others
    }

    public boolean requiresLayout() {
    	 return true;
    }

    public CircularBuffer getCircularQueue(){
        return circularBuffer;
    }

    public void setCircularBuffer(CircularBuffer circularBuffer) {
        this.circularBuffer = circularBuffer;
    }

    public void activateOptions() {
        if (bufferSize < 0) {
            if (circularBuffer == null) {
                this.circularBuffer = new CircularBuffer();
            }
        } else {
            this.circularBuffer = new CircularBuffer(bufferSize);
        }
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }
}

