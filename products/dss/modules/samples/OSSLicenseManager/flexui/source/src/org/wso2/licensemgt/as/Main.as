import mx.collections.ArrayCollection;
import mx.controls.Alert;

import mx.events.MenuEvent;

import org.wso2.licensemgt.events.ArrayFiledEvent;
import org.wso2.licensemgt.model.ModelLocator;

private var modelLocator:ModelLocator = ModelLocator.getInstance();
/* --------------- Component methods -----------------*/
public function getAllComponents():void {
    modelLocator.viewState = ModelLocator.COMPONENT_LIST;
    compList.sendRequest();
}
public function deleteComponent():void {
    componentsGetter.deleteComponent();
    getAllComponents();
}
public function fillComponentNewData():void {
    modelLocator.viewState = ModelLocator.COMPONENT_NEW;
    compNew.initNewMode();
}
public function fillComponentViewData(key:String):void {
    modelLocator.viewState = ModelLocator.COMPONENT_NEW;
    compNew.initViewMode(key);
}
public function getComponentDetails(key:String):void {
    modelLocator.viewState = ModelLocator.COMPONENT_NEW;
    compNew.initEditMode(key);
}
/*---------------- Library methods ------------------*/
public function fillLibraryNewData():void {
    modelLocator.viewState = ModelLocator.LIBRARY_NEW;
    libNew.initNewMode();
}

public function fillLibraryViewData(key:String):void {
    modelLocator.viewState = ModelLocator.LIBRARY_NEW;
    libNew.initViewMode(key);
}
public function deleteLibrary():void {
    libraryGetter.deleteLibrary();
    getAllLibraries();
}
public function getAllLibraries():void {
    modelLocator.viewState = ModelLocator.LIBRARY_LIST;
    libList.sendRequest();
}
public function getLibraryDetails(key:String):void {
    modelLocator.viewState = ModelLocator.LIBRARY_NEW;
    libNew.initEditMode(key);

}
/*---------------- License methods -------------------*/
public function fillLicenseNewData():void {
    modelLocator.viewState = ModelLocator.LICENSE_NEW;
    licenseNew.initNewMode();
}
public function fillLicenseViewData(key:String):void {
    modelLocator.viewState = ModelLocator.LICENSE_NEW;
    licenseNew.initViewMode(key);
}
public function getAllLicense():void {
    modelLocator.viewState = ModelLocator.LICENSE_LIST;
    licenseList.sendRequest();
}
public function getLicenseDetails(key:String):void {
    modelLocator.viewState = ModelLocator.LICENSE_NEW;
    licenseNew.initEditMode(key);
}
/* ------------------- Type methods ------------------*/ 
public function getTypes(typeType:String):void {
    modelLocator.selectedComponentState = "new";
    modelLocator.viewState = ModelLocator.TYPES;
    var data:Object = new Object();
    data = {typeType:typeType};
    types.initModes(data);
}
public function getTypeDetails(data:Object):void {
    modelLocator.viewState = ModelLocator.TYPES;
    types.initModes(data);
}
public function deleteType(typeType:String):void {
    typeGetter.deleteType(typeType);
    getTypes(typeType);
}
/* ------------ Loader functions -----------------*/
private function manageProgress():void{
    componentsGetter.getArray();
}
private function dataLoaded(event:ArrayFiledEvent):void{
    bar.visible = false;
    bar.includeInLayout = false;

    pageContent.visible = true;
    pageContent.includeInLayout = true;
}