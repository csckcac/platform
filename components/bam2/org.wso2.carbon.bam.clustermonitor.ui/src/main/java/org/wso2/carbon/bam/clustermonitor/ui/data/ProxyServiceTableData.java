package org.wso2.carbon.bam.clustermonitor.ui.data;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ProxyServiceTableData {

    private HashMap<String, List<ProxyServiceData>> proxyServiceData = new HashMap<String, List<ProxyServiceData>>();

    public HashMap<String, List<ProxyServiceData>> getServiceData() {
        return proxyServiceData;
    }

    public void setServiceData(String key, ProxyServiceData value) {
        List<ProxyServiceData> proxyDataList = proxyServiceData.get(key);
        if (proxyDataList == null) {
            proxyDataList = new ArrayList<ProxyServiceData>();
        }
        proxyDataList.add(value);
        proxyServiceData.put(key, proxyDataList);
    }
}
