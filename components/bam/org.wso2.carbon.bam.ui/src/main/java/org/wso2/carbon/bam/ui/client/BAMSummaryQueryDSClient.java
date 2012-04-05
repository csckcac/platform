/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.bam.ui.client;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.stub.listadmin.types.carbon.MonitoredServerDTO;
import org.wso2.carbon.bam.stub.listadmin.types.carbon.OperationDO;
import org.wso2.carbon.bam.stub.listadmin.types.carbon.ServiceDO;
import org.wso2.carbon.bam.stub.statquery.Endpoint;
import org.wso2.carbon.bam.stub.statquery.ProxyService;
import org.wso2.carbon.bam.stub.statquery.Sequence;
import org.wso2.carbon.bam.stub.summaryquery.BAMSummaryQueryDSStub;
import org.wso2.carbon.bam.stub.summaryquery.MedSummaryStat;
import org.wso2.carbon.bam.stub.summaryquery.SummaryStat;
import org.wso2.carbon.bam.util.BAMCalendar;

import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Locale;

public class BAMSummaryQueryDSClient {
	private static final Log log = LogFactory.getLog(BAMSummaryQueryDSClient.class);

	private BAMSummaryQueryDSStub stub;

	public BAMSummaryQueryDSClient(String cookie, String backendServerURL, ConfigurationContext configCtx,
			Locale locale) throws AxisFault {
		String serviceURL = backendServerURL + "BAMSummaryQueryDS";
		stub = new BAMSummaryQueryDSStub(configCtx, serviceURL);
		ServiceClient client = stub._getServiceClient();
		Options option = client.getOptions();
		option.setManageSession(true);
		option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
	}

	public SummaryStat[] getServerStatHourlySummaries(MonitoredServerDTO server, Calendar startTime,
			Calendar endTime) throws RemoteException {
		return stub.getServerStatHourlySummaries(server.getServerId(), BAMCalendar.getInstance(startTime)
				.getBAMTimestamp(), BAMCalendar.getInstance(endTime).getBAMTimestamp());

	}

	public SummaryStat[] getServerStatDailySummaries(MonitoredServerDTO server, Calendar startTime,
			Calendar endTime) throws RemoteException {
		return stub.getServerStatDailySummaries(server.getServerId(), BAMCalendar.getInstance(startTime)
				.getBAMTimestamp(), BAMCalendar.getInstance(endTime).getBAMTimestamp());
	}

	public SummaryStat[] getServerStatMonthlySummaries(MonitoredServerDTO server, Calendar startTime,
			Calendar endTime) throws RemoteException {
		return stub.getServerStatMonthlySummaries(server.getServerId(), BAMCalendar.getInstance(startTime)
				.getBAMTimestamp(), BAMCalendar.getInstance(endTime).getBAMTimestamp());
	}

	public SummaryStat[] getServerStatQuarterlySummaries(MonitoredServerDTO server, Calendar startTime,
			Calendar endTime) throws RemoteException {
		return stub.getServerStatQuarterlySummaries(server.getServerId(), BAMCalendar.getInstance(startTime)
				.getBAMTimestamp(), BAMCalendar.getInstance(endTime).getBAMTimestamp());
	}

	public SummaryStat[] getServerStatYearlySummaries(MonitoredServerDTO server, Calendar startTime,
			Calendar endTime) throws RemoteException {
		return stub.getServerStatYearlySummaries(server.getServerId(), BAMCalendar.getInstance(startTime)
				.getBAMTimestamp(), BAMCalendar.getInstance(endTime).getBAMTimestamp());
	}

	public SummaryStat[] getServiceStatHourlySummaries(ServiceDO service, Calendar startTime,
			Calendar endTime) throws RemoteException {
		return stub.getServiceStatHourlySummaries(service.getId(), BAMCalendar.getInstance(startTime)
				.getBAMTimestamp(), BAMCalendar.getInstance(endTime).getBAMTimestamp());
	}

	public SummaryStat[] getServiceStatDailySummaries(ServiceDO service, Calendar startTime, Calendar endTime)
			throws RemoteException {
		return stub.getServiceStatDailySummaries(service.getId(), BAMCalendar.getInstance(startTime)
				.getBAMTimestamp(), BAMCalendar.getInstance(endTime).getBAMTimestamp());
	}

	public SummaryStat[] getServiceStatMonthlySummaries(ServiceDO service, Calendar startTime,
			Calendar endTime) throws RemoteException {
		return stub.getServiceStatMonthlySummaries(service.getId(), BAMCalendar.getInstance(startTime)
				.getBAMTimestamp(), BAMCalendar.getInstance(endTime).getBAMTimestamp());
	}

	public SummaryStat[] getServiceStatQuarterlySummaries(ServiceDO service, Calendar startTime,
			Calendar endTime) throws RemoteException {
		return stub.getServiceStatQuarterlySummaries(service.getId(), BAMCalendar.getInstance(startTime)
				.getBAMTimestamp(), BAMCalendar.getInstance(endTime).getBAMTimestamp());
	}

	public SummaryStat[] getServiceStatYearlySummaries(ServiceDO service, Calendar startTime,
			Calendar endTime) throws RemoteException {
		return stub.getServiceStatYearlySummaries(service.getId(), BAMCalendar.getInstance(startTime)
				.getBAMTimestamp(), BAMCalendar.getInstance(endTime).getBAMTimestamp());
	}

	public SummaryStat[] getServiceStatHourlySummaries(OperationDO op, Calendar startTime, Calendar endTime)
			throws RemoteException {
		return stub.getServiceStatHourlySummaries(op.getOperationID(), BAMCalendar.getInstance(startTime)
				.getBAMTimestamp(), BAMCalendar.getInstance(endTime).getBAMTimestamp());
	}

	public SummaryStat[] getOperationStatHourlySummaries(OperationDO operation, Calendar startTime,
			Calendar endTime) throws RemoteException {
		return stub.getOperationStatHourlySummaries(operation.getOperationID(), BAMCalendar.getInstance(startTime)
				.getBAMTimestamp(), BAMCalendar.getInstance(endTime).getBAMTimestamp());
	}

	public SummaryStat[] getOperationStatDailySummaries(OperationDO operation, Calendar startTime,
			Calendar endTime) throws RemoteException {
		return stub.getOperationStatDailySummaries(operation.getOperationID(), BAMCalendar.getInstance(startTime)
				.getBAMTimestamp(), BAMCalendar.getInstance(endTime).getBAMTimestamp());
	}

	public SummaryStat[] getOperationStatMonthlySummaries(OperationDO operation, Calendar startTime,
			Calendar endTime) throws RemoteException {
		return stub.getOperationStatMonthlySummaries(operation.getOperationID(), BAMCalendar.getInstance(startTime)
				.getBAMTimestamp(), BAMCalendar.getInstance(endTime).getBAMTimestamp());
	}

	public SummaryStat[] getOperationStatQuarterlySummaries(OperationDO operation, Calendar startTime,
			Calendar endTime) throws RemoteException {
		return stub.getOperationStatQuarterlySummaries(operation.getOperationID(), BAMCalendar.getInstance(startTime)
				.getBAMTimestamp(), BAMCalendar.getInstance(endTime).getBAMTimestamp());
	}

	public SummaryStat[] getOperationStatYearlySummaries(OperationDO operation, Calendar startTime,
			Calendar endTime) throws RemoteException {
		return stub.getOperationStatYearlySummaries(operation.getOperationID(), BAMCalendar.getInstance(startTime)
				.getBAMTimestamp(), BAMCalendar.getInstance(endTime).getBAMTimestamp());
	}

	public MedSummaryStat[] getEndpointStatHourlySummaries(int serverId, Endpoint endpoint,
			Calendar startTime, Calendar endTime) throws RemoteException {
		return stub
				.getEndpointStatHourlySummaries(serverId, endpoint.getEndpoint(), "In", BAMCalendar
						.getInstance(startTime).getBAMTimestamp(), BAMCalendar.getInstance(endTime)
						.getBAMTimestamp());
	}

	public MedSummaryStat[] getEndpointStatDailySummaries(int serverId, Endpoint endpoint,
			Calendar startTime, Calendar endTime) throws RemoteException {
		return stub
				.getEndpointStatDailySummaries(serverId, endpoint.getEndpoint(), "In", BAMCalendar
						.getInstance(startTime).getBAMTimestamp(), BAMCalendar.getInstance(endTime)
						.getBAMTimestamp());
	}

	public MedSummaryStat[] getEndpointStatMonthlySummaries(int serverId, Endpoint endpoint,
			Calendar startTime, Calendar endTime) throws RemoteException {
		return stub
				.getEndpointStatMonthlySummaries(serverId, endpoint.getEndpoint(), "In", BAMCalendar
						.getInstance(startTime).getBAMTimestamp(), BAMCalendar.getInstance(endTime)
						.getBAMTimestamp());
	}

	public MedSummaryStat[] getEndpointStatQuarterlySummaries(int serverId, Endpoint endpoint,
			Calendar startTime, Calendar endTime) throws RemoteException {
		return stub
				.getEndpointStatQuarterlySummaries(serverId, endpoint.getEndpoint(), "In", BAMCalendar
						.getInstance(startTime).getBAMTimestamp(), BAMCalendar.getInstance(endTime)
						.getBAMTimestamp());
	}

	public MedSummaryStat[] getEndpointStatYearlySummaries(int serverId, Endpoint endpoint,
			Calendar startTime, Calendar endTime) throws RemoteException {
		return stub
				.getEndpointStatQuarterlySummaries(serverId, endpoint.getEndpoint(), "In", BAMCalendar
						.getInstance(startTime).getBAMTimestamp(), BAMCalendar.getInstance(endTime)
						.getBAMTimestamp());
	}

	public MedSummaryStat[] getSequenceStatHourlySummaries(int serverId, Sequence sequence,
			Calendar startTime, Calendar endTime) throws RemoteException {
		return stub
				.getSequenceStatHourlySummaries(serverId, sequence.getSequence(), "In", BAMCalendar
						.getInstance(startTime).getBAMTimestamp(), BAMCalendar.getInstance(endTime)
						.getBAMTimestamp());
	}

	public MedSummaryStat[] getSequenceStatDailySummaries(int serverId, Sequence sequence,
			Calendar startTime, Calendar endTime) throws RemoteException {
		return stub
				.getSequenceStatDailySummaries(serverId, sequence.getSequence(), "In", BAMCalendar
						.getInstance(startTime).getBAMTimestamp(), BAMCalendar.getInstance(endTime)
						.getBAMTimestamp());
	}

	public MedSummaryStat[] getSequenceStatMonthlySummaries(int serverId, Sequence sequence,
			Calendar startTime, Calendar endTime) throws RemoteException {
		return stub
				.getSequenceStatMonthlySummaries(serverId, sequence.getSequence(), "In", BAMCalendar
						.getInstance(startTime).getBAMTimestamp(), BAMCalendar.getInstance(endTime)
						.getBAMTimestamp());
	}

	public MedSummaryStat[] getSequenceStatQuarterlySummaries(int serverId, Sequence sequence,
			Calendar startTime, Calendar endTime) throws RemoteException {
		return stub
				.getSequenceStatQuarterlySummaries(serverId, sequence.getSequence(), "In", BAMCalendar
						.getInstance(startTime).getBAMTimestamp(), BAMCalendar.getInstance(endTime)
						.getBAMTimestamp());
	}

	public MedSummaryStat[] getSequenceStatYearlySummaries(int serverId, Sequence sequence,
			Calendar startTime, Calendar endTime) throws RemoteException {
		return stub
				.getSequenceStatQuarterlySummaries(serverId, sequence.getSequence(), "In", BAMCalendar
						.getInstance(startTime).getBAMTimestamp(), BAMCalendar.getInstance(endTime)
						.getBAMTimestamp());
	}

	public MedSummaryStat[] getProxyStatHourlySummaries(int serverId, ProxyService proxy, Calendar startTime,
			Calendar endTime) throws RemoteException {
		return stub
				.getProxyStatHourlySummaries(serverId, proxy.getProxyService(), "In", BAMCalendar
						.getInstance(startTime).getBAMTimestamp(), BAMCalendar.getInstance(endTime)
						.getBAMTimestamp());
	}

	public MedSummaryStat[] getProxyStatDailySummaries(int serverId, ProxyService proxy, Calendar startTime,
			Calendar endTime) throws RemoteException {
		return stub
				.getProxyStatDailySummaries(serverId, proxy.getProxyService(), "In", BAMCalendar.getInstance(
						startTime).getBAMTimestamp(), BAMCalendar.getInstance(endTime).getBAMTimestamp());
	}

	public MedSummaryStat[] getProxyStatMonthlySummaries(int serverId, ProxyService proxy,
			Calendar startTime, Calendar endTime) throws RemoteException {
		return stub
				.getProxyStatMonthlySummaries(serverId, proxy.getProxyService(), "In", BAMCalendar
						.getInstance(startTime).getBAMTimestamp(), BAMCalendar.getInstance(endTime)
						.getBAMTimestamp());
	}

	public MedSummaryStat[] getProxyStatQuarterlySummaries(int serverId, ProxyService proxy,
			Calendar startTime, Calendar endTime) throws RemoteException {
		return stub
				.getProxyStatQuarterlySummaries(serverId, proxy.getProxyService(), "In", BAMCalendar
						.getInstance(startTime).getBAMTimestamp(), BAMCalendar.getInstance(endTime)
						.getBAMTimestamp());
	}

	public MedSummaryStat[] getProxyStatYearlySummaries(int serverId, ProxyService proxy, Calendar startTime,
			Calendar endTime) throws RemoteException {
		return stub
				.getProxyStatQuarterlySummaries(serverId, proxy.getProxyService(), "In", BAMCalendar
						.getInstance(startTime).getBAMTimestamp(), BAMCalendar.getInstance(endTime)
						.getBAMTimestamp());
	}

    public void cleanup() {
        try {
            stub._getServiceClient().cleanupTransport();
            stub._getServiceClient().cleanup();
            stub.cleanup();
        } catch (AxisFault axisFault) {
            if (log.isErrorEnabled()) {
                log.error("Stub cleanup failed: " + this.getClass().getName(), axisFault);
            }
        }
    }

}