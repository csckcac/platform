package org.wso2.licensemgt.model
{
import mx.collections.ArrayCollection;
import mx.controls.DataGrid;
import mx.controls.dataGridClasses.DataGridItemRenderer;
import mx.events.ListEvent;
import mx.rpc.events.ResultEvent;
import mx.controls.Alert;

[Bindable]
public class Utils
{
    public function Utils()
    {
    }

    public function xmlToArray(xmlData:XML):void {//ArrayCollection{
        var nsVar:Namespace = xmlData.namespace();
        var childList:XMLList = xmlData.children();
        var tmpToShow:String = "";
        var returnArray:ArrayCollection = new ArrayCollection();

        for each(var t:XML in xmlData.children()) {
            var nsT:Namespace = t.namespace();
            tmpToShow += "tag name: " + t.name().localName + " tag content " + t.text();
        }
        //Alert.show(tmpToShow);
        //return returnArray;
    }
    public function deleteToolTip(obj:Object,dgItems:DataGrid):void {
        dgItems.toolTip = null;
    }
    public function createToolTip(event:ListEvent,dgItems:DataGrid):void {

        if(event.columnIndex == 0 || event.columnIndex == 1 ){     //This number has to be changed according to the number of columns have in listing page.
            return;
        }

        dgItems.toolTip = DataGridItemRenderer(event.itemRenderer).data.description;

    }
}
}