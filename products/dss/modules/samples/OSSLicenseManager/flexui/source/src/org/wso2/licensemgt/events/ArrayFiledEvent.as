package org.wso2.licensemgt.events {
import flash.events.Event;

import mx.collections.ArrayCollection;

public class ArrayFiledEvent extends Event{
    public var compList:ArrayCollection;

    public function ArrayFiledEvent(type:String, compList:ArrayCollection) {
        super(type);
        this.compList = compList;
    }
}
}