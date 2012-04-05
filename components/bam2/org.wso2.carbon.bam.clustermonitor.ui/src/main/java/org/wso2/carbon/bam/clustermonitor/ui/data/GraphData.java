package org.wso2.carbon.bam.clustermonitor.ui.data;


import org.wso2.carbon.bam.clustermonitor.ui.Point;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public class GraphData {

    public  static Map<String,Queue<Point>> requestCountQueue = new HashMap<String,Queue<Point>>();
    public  static Map<String,Queue<Point>> responseCountQueue = new HashMap<String,Queue<Point>>();
    public  static Map<String,Queue<Point>> faultCountQueue = new HashMap<String,Queue<Point>>();
    public  static Map<String,Queue<Point>> responseTimeQueue = new HashMap<String,Queue<Point>>();
}
