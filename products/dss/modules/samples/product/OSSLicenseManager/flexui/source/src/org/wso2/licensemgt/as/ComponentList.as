import mx.controls.Alert;
import mx.rpc.events.FaultEvent;
import mx.rpc.soap.mxml.Operation;
import mx.collections.XMLListCollection;
import mx.controls.Alert;
import mx.controls.LinkButton;
import mx.rpc.events.ResultEvent;
import mx.collections.ArrayCollection;
import mx.events.ListEvent;

import org.wso2.licensemgt.events.ArrayFiledEvent;
import org.wso2.licensemgt.model.ModelLocator;
import org.wso2.licensemgt.model.Utils;

import mx.validators.Validator;
import mx.events.ValidationResultEvent;
import mx.core.Application;
import mx.events.ItemClickEvent;
import mx.controls.Button;
import mx.utils.ObjectUtil;

/* ------variables for pagination -------------*/
[Bindable]
public var myDP:ArrayCollection = new ArrayCollection();
public var orgData:ArrayCollection = new ArrayCollection();
[Bindable]
public var nav:ArrayCollection = new ArrayCollection();
public var pageSize:uint = 10;
public var navSize:uint = 10;
private var index:uint = 0;
private var navPage:uint = 1;
/* -------------------------------------------*/
private var modelLocator:ModelLocator = ModelLocator.getInstance();
public var utils:Utils = new Utils();

default xml namespace = "http://ws.wso2.org/dataservice"
;
//necessary to access the xml elements easily

[Bindable]
private var _xmlResult:XML;      //holds the result xml
[Bindable]
private var _xlDayData:XMLList;  //dataProvider for the component data grid
[Bindable]
public var selectedToDelete:String;

private function init():void {
   
    formls.addEventListener(KeyboardEvent.KEY_DOWN, checkKey);
}

[Bindable] var searchTextFiltered:String = new String();

private function validateForm():void {
     var isValidForm:Boolean = false ;
    if (searchText.text == "") {
        sendRequest();
        return;
    }
    //var myPattern:RegExp = /\*/g;
    searchTextFiltered = "%" + searchText.text + "%";
    //searchTextFiltered = searchTextFiltered.replace(myPattern, "%");
    componentsGetter.sendSearchRequest(searchTextFiltered);
}

function checkKey(event:KeyboardEvent):void
{
    if (event.charCode == 13) {
        validateForm();
    }
}
private function setComponentDG(event:ArrayFiledEvent):void {
    InitApp(event.compList);
    var listLength:int = event.compList.length;
    if(listLength>=10) {
        listLength = 10;
    }else{
        listLength = event.compList.length;
    }
    dgComponents.height = listLength * 40 + dgComponents.headerHeight;
}
public function sendRequest():void
{
    componentsGetter.getArray();
    init();
}
public function goToNewComponent():void {
    modelLocator.viewState = ModelLocator.COMPONENT_NEW;
    modelLocator.selectedComponentState = "new";
    Application.application.fillComponentNewData();
}
public function goToTypes():void {
    Application.application.getTypes('component');
}
private function showDetails(e:ListEvent):void {

    var tmpArray:Object = e.itemRenderer.data;
    modelLocator.selectedComponentState = "view";
    Application.application.fillComponentViewData(tmpArray.key);
}

/* -------------------------------------------*/
/* --------- pagination Functions ------------*/
private function InitApp(dp:ArrayCollection):void
{
    orgData = dp;
    refreshDataProvider(index);
    createNavBar(Math.ceil(orgData.length / pageSize));
}

private function createNavBar(pages:uint = 1, set:uint = 0):void
{
    nav.removeAll();
    if (pages > 1)
    {
        if (set != 0)
        {
            nav.addItem({label:"<<",data:0});
            if ((set - navSize ) >= 0)
            {
                nav.addItem({label:"<",data:set - navSize});
            }
            else
            {
                nav.addItem({label:"<",data:0});
            }
        }

        for (var x:uint = 0; x < navSize; x++)
        {
            var pg:uint = x + set;
            nav.addItem({label: pg + 1,data: pg});
        }
        if (pg < pages - 1)
        {
            nav.addItem({label:">",data:pg + 1});
            nav.addItem({label:">>",data:pages - pageSize});
        }
    }
}

private function navigatePage(event:ItemClickEvent):void
{
    refreshDataProvider(event.item.data);
    var lb:String = event.item.label.toString();
    if (lb.indexOf("<") > -1 || lb.indexOf(">") > -1)
    {
        createNavBar(Math.ceil(orgData.length / pageSize), event.item.data);
        if (event.item.data == 0)
        {
            pageNav.selectedIndex = 0;
        }
        else
        {
            pageNav.selectedIndex = 2;
        }
    }

}

private function refreshDataProvider(start:uint):void
{
    myDP = new ArrayCollection(orgData.source.slice((start * pageSize), (start * pageSize) + pageSize));
}
/* ------------------------------------------------------------------*/



