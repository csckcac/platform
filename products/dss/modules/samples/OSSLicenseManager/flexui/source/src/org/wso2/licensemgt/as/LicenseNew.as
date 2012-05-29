import flash.events.Event;

import flash.events.KeyboardEvent;
import flash.events.DataEvent;

import flash.events.MouseEvent;

import mx.controls.CheckBox;
import mx.controls.ComboBox;
import mx.events.CloseEvent;
import mx.rpc.events.FaultEvent;
import mx.rpc.soap.mxml.Operation;
import mx.collections.XMLListCollection;
import mx.controls.Alert;
import mx.rpc.events.ResultEvent;
import mx.collections.ArrayCollection;

import mx.states.AddChild;

import org.wso2.licensemgt.events.ArrayFiledEvent;
import org.wso2.licensemgt.model.ModelLocator;

import mx.validators.Validator;
import mx.events.ValidationResultEvent;
import mx.core.Application;

private var modelLocator:ModelLocator = ModelLocator.getInstance();

default xml namespace = "http://ws.wso2.org/dataservice"
;  //necessary to access the xml elements easily

[Bindable]
private var _xmlResult:XML;      //holds the result xml
[Bindable]
private var _xlDayData:XMLList;  //dataProvider for the day weather dataGrid
[Bindable]
private var validatorArr:Array;
[Bindable]
public     var libTypeList:ArrayCollection = new ArrayCollection();
[Bindable] var compNewState:String;

[Bindable]
public var myDP:ArrayCollection = new ArrayCollection();
[Bindable]
public var myDPName:ArrayCollection = new ArrayCollection();
[Bindable]
public var myDPVersion:ArrayCollection = new ArrayCollection();
[Bindable]
public var myDPVendor:ArrayCollection = new ArrayCollection();
[Bindable]
public var myDPUrl:ArrayCollection = new ArrayCollection();
[Bindable]
public var myDPFilename:ArrayCollection = new ArrayCollection();
public var selectedLicenses:ArrayCollection = new ArrayCollection();
var allLibraries:ArrayCollection = new ArrayCollection();
[Bindable] var filteredLibraries:ArrayCollection = new ArrayCollection();
[Bindable] var selectedLibraries:ArrayCollection = new ArrayCollection();
public var deleteVisible:Boolean = true;
var key:String = "";
[Bindable]
        var urlVisible:Boolean = true;
var today:Date = new Date();
[Bindable]
        var thisYear:Number = today.getFullYear();

[Bindable]
        var sources:ArrayCollection = new ArrayCollection([{label:"URL",data:"URL"},{label:"Local",data:"Local"}]);


/* ---------------------------------- Init methods ----------------- */


public function init():void {
    validatorArr = new Array();
    validatorArr.push(strV1);
    validatorArr.push(strV2);
    validatorArr.push(strV3);

    formls.addEventListener(KeyboardEvent.KEY_DOWN, checkKey);
}
public function initEditMode(key:String):void {
    deleteVisible = true;             //Hide/show the delete button on selected libs grid
    headingText.text = "Edit License - " + key;
    licenseGetter.getLicenseDetails(key);
    makeUnEditable(workArea);

    llView.visible = true;
    llView.includeInLayout = true;
    lcView.visible = true;
    lcView.includeInLayout = true;
    licenseLibrariesDP = new ArrayCollection();
    licenseComponentsDP = new ArrayCollection();
    backPanelView.visible = false; //Buttons show hide
    backPanelView.includeInLayout = false; //Buttons show hide
    backPanelEditNew.visible = true;
    backPanelEditNew.includeInLayout = true;
}
public function initViewMode(key:String):void {
    deleteVisible = false;             //Hide/show the delete button on selected libs grid
    headingText.text = "License - " + key;
    validatorArr = new Array();
    makeUnEditable(workArea);

    licenseGetter.getArray();
    licenseGetter.getLicenseDetails(key);

    llView.visible = true;
    llView.includeInLayout = true;
    lcView.visible = true;
    lcView.includeInLayout = true;
    licenseLibrariesDP = new ArrayCollection();
    licenseComponentsDP = new ArrayCollection();
    backPanelView.visible = true;     //Buttons show hide
    backPanelView.includeInLayout = true;     //Buttons show hide
    backPanelEditNew.visible = false;
    backPanelEditNew.includeInLayout = false;
}

public function initNewMode():void {
    deleteVisible = true;             //Hide/show the delete button on selected libs grid
    headingText.text = "New License";
    resetForm();

    makeUnEditable(workArea);
    licenseGetter.getArray();

    llView.visible = false;
    llView.includeInLayout = false;
    lcView.visible = false;
    lcView.includeInLayout = false;
    backPanelView.visible = false;         //Buttons show hide
    backPanelView.includeInLayout = false;         //Buttons show hide
    backPanelEditNew.visible = true;
    backPanelEditNew.includeInLayout = true;
}
public function resetForm():void {
    licenseName.text = "";

    licenseName.errorString = "";

    version.text = "";

    version.errorString = "";

    url.text = "";

    url.errorString = "";

    LicenseText.text = "";
    LicenseText.errorString = "";

}
/* ----------------------- Validation and form submition ----------------  */
private function validateForm():void {

    var validatorErrorArray:Array = Validator.validateAll(validatorArr);
    var isValidForm:Boolean = validatorErrorArray.length == 0;
    var tmpKey:String = "";
    if (modelLocator.selectedComponentState == "new") {
        tmpKey = licenseName.text;
    } else {
        tmpKey = this.key;
    }
    if (isValidForm) {
        var compDetails:ArrayCollection = new ArrayCollection();
        if (!(url.text == "" && LicenseText.text == "")) {
            if (url.text == "") {
                url.text = ".";
            }
            if (LicenseText.text == "") {
                LicenseText.text = ".";
            }
        }
        compDetails.addItem({key:tmpKey,name:licenseName.text,version:version.text,year:year.value,source:licenseSource.selectedItem.label,url:url.text,content:LicenseText.text});
        licenseGetter.insertComponent(compDetails);
        modelLocator.viewState = ModelLocator.LICENSE_LIST;
    } else {
        var err:ValidationResultEvent;
        var errorMessageArray:Array = [];
        for each (err in validatorErrorArray) {
            var errField:String = FormItem(err.currentTarget.source.parent.parent).label
            errorMessageArray.push(errField + ": " + err.message);
        }
        Alert.show(errorMessageArray.join("\n\n"), "Invalid form...", Alert.OK);
    }

}


/* ------------------ Geting/processing data for form content------------------------*/
[Bindable]
public var allLicense:ArrayCollection = new ArrayCollection();
function feedACData(event:ArrayFiledEvent):void {
    allLicense = new ArrayCollection();
    allLicense = event.compList;
    if (modelLocator.selectedComponentState == "new") {
        resetForm();

        //        validatorArr = new Array();
        //        licenseName.errorString = "";
        //        url.errorString = "";
        //        version.errorString = "";

    } else if (modelLocator.selectedComponentState == "edit") {

        Alert.show('editing');
    }

}
function feedLicenseData(event:ArrayFiledEvent):void {
    var compData:ArrayCollection = event.compList;
    key = compData[0].key;
    licenseName.text = compData[0].name;
    version.text = compData[0].version;
    year.value = compData[0].year;

    licenseSource.dataProvider = this.sources;
    if (compData[0].source == "URL") {
        licenseSource.selectedIndex = 0;
        urlVisible = true;
        validatorArr = new Array();
        validatorArr.push(strV1);
        validatorArr.push(strV2);
        validatorArr.push(strV3);
        if (LicenseText.text == "") {
            LicenseText.text = ".";
        }
    } else {
        licenseSource.selectedIndex = 1;
        urlVisible = false;
        validatorArr = new Array();
        validatorArr.push(strV1);
        validatorArr.push(strV2);
        validatorArr.push(strV4);
        if (url.text == "") {
            url.text = ".";
        }
    }
    url.text = compData[0].url;
    LicenseText.text = compData[0].content;
}


/* ------------------------ Changing modes ------------------------------*/
private function goToEditMode():void {
    modelLocator.selectedComponentState = "edit";
    modelLocator.viewState = ModelLocator.LICENSE_NEW;

    Application.application.getLicenseDetails(key);

}
private function cancelRequest():void {
    resetForm();
    modelLocator.viewState = ModelLocator.LICENSE_LIST
}

/*-------------------------- utility methods -----------------------------*/
private function makeUnEditable(items:Object):void {
    for each(var item in items.getChildren()) {
        if (item is TextInput || item is TextArea) {
            if (modelLocator.selectedComponentState == "view") {
                item.editable = false;
                item.styleName = "disabledText";
            } else {
                item.editable = true;
                item.styleName = "enabledText";
            }
        } else if (item is ComboBox || item is CheckBox || item is NumericStepper) {
            if (modelLocator.selectedComponentState == "view") {
                item.enabled = false;
            } else {
                item.enabled = true;
            }
        }
        if (item is VBox || item is Form || item is FormItem || item is HBox) {
            makeUnEditable(item);
        }
    }
}

private function closeHandler(event:Event):void {
    if (licenseSource.selectedItem.label == "URL") {
        urlVisible = true;
        validatorArr = new Array();
        validatorArr.push(strV1);
        validatorArr.push(strV2);
        validatorArr.push(strV3);
        if (LicenseText.text == "") {
            LicenseText.text = ".";
        }
    } else {
        urlVisible = false;
        validatorArr = new Array();
        validatorArr.push(strV1);
        validatorArr.push(strV2);
        validatorArr.push(strV4);
        if (url.text == "") {
            url.text = ".";
        }
    }
}
/*------------------ Form submition if Enter pressed -------------------------*/
function checkKey(event:KeyboardEvent):void
{
    if (event.charCode == 13) {
        validateForm();
    }
}
/*---------------------Refernce stuff -------------------------------------*/
[Bindable] var licenseLibrariesDP:ArrayCollection = new ArrayCollection();
[Bindable] var licenseComponentsDP:ArrayCollection = new ArrayCollection();

private function showLicenseLibraries():void {
    barLicenseLibraries.visible = true;
    barLicenseLibraries.includeInLayout = true;
    libraryGetter.getArrayLicenseLibs(key);
}
private function showLicenseComponents():void {
    barLicenseComponents.visible = true;
    barLicenseComponents.includeInLayout = true;
    componentGetter.getArrayLicenseComps(key);
}
//Listners
private function feedLicenseLibraries(event:ArrayFiledEvent):void {
    licenseLibrariesDP = event.compList;
    if (licenseLibrariesDP.length == 0) {
        Alert.show('No libraries are using this license')
    }
    licenseLibraries.dataProvider = licenseLibrariesDP;
    barLicenseLibraries.visible = false;
    barLicenseLibraries.includeInLayout = false;
}
private function feedLicenseComponents(event:ArrayFiledEvent):void {
    licenseComponentsDP = event.compList;
    if (licenseComponentsDP.length == 0) {
        Alert.show('No components are using this license')
    }
    licenseLibraries.dataProvider = licenseLibrariesDP;
    barLicenseComponents.visible = false;
    barLicenseComponents.includeInLayout = false;
}
private function refreshRefs(event:ArrayFiledEvent):void {
    showLicenseComponents();
    showLicenseLibraries();
}
private function removeLicenseLibraries():void {
    Alert.show("Are you sure?", "Delete", 3, null, function (event:CloseEvent):void {
        if (event.detail == Alert.YES) {
            licenseGetter.removeLibraryRefs(key);
        }
    });
}
private function removeLicenseComponents():void {
    Alert.show("Are you sure?", "Delete", 3, null, function (event:CloseEvent):void {
        if (event.detail == Alert.YES) {
            licenseGetter.removeComponentRefs(key);
        }
    });
}
//licenseGetter.removeLibraryRefs(key);
//licenseGetter.removeComponentRefs(key);
/*
 private function removeDuplicates(label:String):ArrayCollection {
 var newArrayCollection:ArrayCollection = new ArrayCollection();
 var tmpString:String = "";

 var hasItem:Boolean = false;
 for (var i:int = 0; i < myDP.length; i++) {
 hasItem = false;
 for (var j:int = 0; j < newArrayCollection.length; j++) {

 if (newArrayCollection[j][label] == myDP[i][label]) {
 hasItem = true;
 }

 }
 if (!hasItem) {
 tmpString = myDP[i][label];
 var arrayObj:Object = new Object();
 arrayObj[label] = tmpString;
 newArrayCollection.addItem(arrayObj);
 }
 }
 return newArrayCollection;
 }*/
