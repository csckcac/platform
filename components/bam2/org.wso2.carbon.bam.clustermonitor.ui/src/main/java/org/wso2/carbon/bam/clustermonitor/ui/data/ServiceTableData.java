package org.wso2.carbon.bam.clustermonitor.ui.data;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ServiceTableData {

    private HashMap<String, List<OperationData>> serviceData = new HashMap<String, List<OperationData>>();

    public HashMap<String, List<OperationData>> getServiceData() {
        return serviceData;
    }

    public void setServiceData(String key, OperationData value) {
        List<OperationData> operationDataList = serviceData.get(key);
        if(operationDataList==null){
            operationDataList = new ArrayList<OperationData>();
        }
        operationDataList.add(value);
        serviceData.put(key, operationDataList);
    }
}
