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
import mx.utils.ObjectUtil;

private var modelLocator:ModelLocator = ModelLocator.getInstance();
public var utils:Utils = new Utils();

//Get all components in an array collection
[Bindable]
public var myDP:ArrayCollection = new ArrayCollection();


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
    searchTextFiltered = "%" + searchText.text + "%";
    componentsGetter.sendSearchRequest(searchTextFiltered);

}

function checkKey(event:KeyboardEvent):void
{
    if (event.charCode == 13) {
        validateForm();
    }
}
private function setComponentDG(event:ArrayFiledEvent):void {
    dgComponents.dataProvider = event.compList;
    dgComponents.height = event.compList.length * 50 + dgComponents.headerHeight;
}

public function sendRequest():void
{
    componentsGetter.getArray();
    init();
}

public function goToNewComponent():void {
    modelLocator.viewState = ModelLocator.LIBRARY_NEW;
    modelLocator.selectedComponentState = "new";
    Application.application.fillLibraryNewData();
}
public function goToTypes():void {
    Application.application.getTypes('library');
}
private function showDetails(e:ListEvent):void {
    var tmpArray:Object = e.itemRenderer.data;
    modelLocator.selectedComponentState = "view";
    Application.application.fillLibraryViewData(tmpArray.id);
}




