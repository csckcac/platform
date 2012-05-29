import flash.events.Event;

import flash.events.FocusEvent;
import flash.events.KeyboardEvent;
import flash.events.DataEvent;

import flash.events.MouseEvent;

import mx.controls.CheckBox;
import mx.controls.ComboBox;
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
[Bindable] var allTypes:ArrayCollection = new ArrayCollection();
public var deleteVisible:Boolean = true;
var key:String = "";

/* ---------------------------------- Init methods ----------------- */


public function init():void {
    validatorArr = new Array();
    validatorArr.push(strV1);
    validatorArr.push(strV2);
    validatorArr.push(strV3);
    validatorArr.push(strV4);
    validatorArr.push(strV5);
    validatorArr.push(strV6);

    formls.addEventListener(KeyboardEvent.KEY_DOWN, checkKey);
    librarySearch.addEventListener(KeyboardEvent.KEY_UP, getLibraries);

}
public function initEditMode(key:String):void {
    deleteVisible = true;             //Hide/show the delete button on selected libs grid
    headingText.text = "Edit Component - " + key;
    licenseGetter.getArray();
    libraryGetter.getArray();
    typeGetter.getArray('component');
    componentsGetter.getComponentDetails(key);
    libraryGetter.getCompLibs(key);
    licenseGetter.getCompLicense(key);
    backPanelView.visible = false; //Buttons show hide
    backPanelView.includeInLayout = false; //Buttons show hide
    backPanelEditNew.visible = true;
    backPanelEditNew.includeInLayout = true;
    hideGetLibraries();                      //Hide the libaries auto filing thing
    librarySearch.text = "";
}
public function initViewMode(key:String):void {
    deleteVisible = false;             //Hide/show the delete button on selected libs grid
    headingText.text = "Component - " + key;
    validatorArr = new Array();
    licenseGetter.getArray();
    libraryGetter.getArray();
    typeGetter.getArray('component');
    componentsGetter.getComponentDetails(key);
    libraryGetter.getCompLibs(key);
    licenseGetter.getCompLicense(key);
    backPanelView.visible = true;     //Buttons show hide
    backPanelView.includeInLayout = true;     //Buttons show hide
    backPanelEditNew.visible = false;
    backPanelEditNew.includeInLayout = false;
    hideGetLibraries();                      //Hide the libaries auto filing thing
    librarySearch.text = "";
}

public function initNewMode():void {
    deleteVisible = true;             //Hide/show the delete button on selected libs grid
    headingText.text = "New Component";
    resetForm();
    licenseGetter.getArray();
    libraryGetter.getArray();
    typeGetter.getArray('component');
    makeUnEditable(workArea);
    backPanelView.visible = false;         //Buttons show hide
    backPanelView.includeInLayout = false;         //Buttons show hide
    backPanelEditNew.visible = true;
    backPanelEditNew.includeInLayout = true;
    hideGetLibraries();                      //Hide the libaries auto filing thing
    librarySearch.text = "";
}
public function resetForm():void {
    selectedLibraries = new ArrayCollection();
    selectedLicenses = new ArrayCollection();

    while (licenseAddBox.numChildren > 0) {
        licenseAddBox.removeChildAt(licenseAddBox.numChildren - 1);
    }
    selectedLibrariesDG.dataProvider = selectedLibraries;
    componentName.text = "";
    componentName.errorString = "";

    componentVersion.text = "";
    componentVersion.errorString = "";

    componentFilename.text = "";
    componentFilename.errorString = "";

    componentVendor.text = "";
    componentVendor.errorString = "";

    componentUrl.text = "";
    componentUrl.errorString = "";

    componentDescription.text = "";
    componentDescription.errorString = "";
}
/* ----------------------- Validation and form submition ----------------  */
private function validateForm():void {

    var validatorErrorArray:Array = Validator.validateAll(validatorArr);
    var isValidForm:Boolean = validatorErrorArray.length == 0;
    if (isValidForm) {
        var compDetails:ArrayCollection = new ArrayCollection();
        compDetails.addItem({name:componentName.text,type:componentType.selectedItem.label,version:componentVersion.text,vendor:componentVendor.text,
            url:componentUrl.text,description:componentDescription.text,filename:componentFilename.text});
        componentsGetter.insertComponent(compDetails, selectedLibraries, selectedLicenses);
        modelLocator.viewState = ModelLocator.COMPONENT_LIST;
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

/* ------------------ Geting/processing data for types -----------------------------*/
function feedTypesIn(event:ArrayFiledEvent):void {
    componentType.dataProvider = event.compList;
    allTypes = event.compList;
}
/* ------------------ Geting/processing data for form content------------------------*/
function feedComponentData(event:ArrayFiledEvent):void {
    var compData:ArrayCollection = event.compList;
    key = compData[0].key;
    componentName.text = compData[0].name;
    componentVersion.text = compData[0].version;
    componentFilename.text = compData[0].filename;

    var tmpString:String = "";
    tmpString += compData[0].type;
    var pattern:RegExp = new RegExp(tmpString, "i");

    var selectedComboItem:int = 0;
    for (var i:int; i < allTypes.length; i++) {

        if (allTypes[i].label.search(pattern) != -1) {
            selectedComboItem = i;
        }
    }

    componentType.selectedIndex = selectedComboItem;

    componentVendor.text = compData[0].vendor;
    componentUrl.text = compData[0].url;
    componentDescription.text = compData[0].description;
}
/* ------------------ Geting/processing data for libraries -----------------------------*/
function feedSelectedLibrariesIn(event:ArrayFiledEvent):void {
    selectedLibraries = new ArrayCollection();
    selectedLibraries = event.compList;
    selectedLibrariesDG.dataProvider = selectedLibraries;
    selectedLibrariesDG.height = selectedLibraries.length * 50 + selectedLibrariesDG.headerHeight;

}
function feedLibrariesIn(event:ArrayFiledEvent):void {
    var compList:ArrayCollection = event.compList;
    allLibraries = compList;
}
function hideGetLibraries():void {
    librariesAddBox.visible = false;
    librariesAddBox.includeInLayout = false;
}
function getLibraries(event:KeyboardEvent):void
{
    if (event.keyCode == 8 || event.keyCode == 46) {
        if (librarySearch.text.length <= 1) {
            librariesAddBox.visible = false;
            librariesAddBox.includeInLayout = false;
        }
    }
    var pattern:RegExp = new RegExp(librarySearch.text, "i");
    filteredLibraries = new ArrayCollection();
    for (var i:int = 0; i < allLibraries.length; i++) {
        if (allLibraries[i].name.search(pattern) != -1) {
            filteredLibraries.addItem(allLibraries[i]);
        }
    }
    var filteredLibrariesList:ArrayCollection = new ArrayCollection();
    if (librarySearch.text == "*") {
        filteredLibraries = allLibraries;
    }
    for (i = 0; i < filteredLibraries.length; i++) {
        filteredLibrariesList.addItem({label:filteredLibraries[i].name,data:filteredLibraries[i].id});
    }

    librariesAddBox.dataProvider = filteredLibrariesList;
    librariesAddBox.selectedItem = librariesAddBox.getChildAt(0);

    if ((filteredLibraries.length == 0 || librarySearch.text.length == 0) && librarySearch.text != "*") {
        librariesAddBox.visible = false;
        librariesAddBox.includeInLayout = false;    
    } else {
        librariesAddBox.visible = true;
        librariesAddBox.includeInLayout = true;
    }
}
private function closeHandler(event:Event):void {
    var selectedItem:Object = librariesAddBox.selectedItem;
    librariesAddBox.visible = false;
    librariesAddBox.includeInLayout = false;
    for (var i:int = 0; i < allLibraries.length; i++) {
        if (allLibraries[i].id == selectedItem.data) {
            selectedLibraries.addItem(allLibraries[i]);
        }
    }
    selectedLibrariesDG.dataProvider = selectedLibraries;
    selectedLibrariesDG.height = selectedLibraries.length * 50 + selectedLibrariesDG.headerHeight;
}
/* -------------------- Get Process data for License  ------------------*/
function feedSelectedLicenseIn(event:ArrayFiledEvent):void {

    var tmpSelected:ArrayCollection = event.compList;

    for each (var childCb in licenseAddBox.getChildren()) {
        if (childCb is CheckBox) {
            for each(var license in tmpSelected) {
                if (license.key == childCb.id) {
                    childCb.selected = true;
                }
            }


        }
    }
    selectedLicenses = new ArrayCollection();
    for each(license in tmpSelected) {
        selectedLicenses.addItem({key:license.key});
    }
    ///Finaly make all the input items disabled or enabled

    makeUnEditable(workArea);

}
function feedLicenseIn(event:ArrayFiledEvent):void {
    var compList:ArrayCollection = event.compList;
    while (licenseAddBox.numChildren > 0) {
        licenseAddBox.removeChildAt(licenseAddBox.numChildren - 1);
    }
    for (var i:int = 0; i < compList.length; i++) {
        var cb:CheckBox = new CheckBox();
        cb.label = compList[i].name;
        cb.id = compList[i].key;

        cb.addEventListener(MouseEvent.CLICK, function(event:MouseEvent):void {
            selectedLicenses = new ArrayCollection();
            for each (var childCb in licenseAddBox.getChildren()) {
                if (childCb is CheckBox) {
                    if (childCb.selected == true) {
                        selectedLicenses.addItem({key:childCb.id});
                    }

                }
            }
        });


        licenseAddBox.addChild(cb);
    }
}

/* ------------------------ Changing modes ------------------------------*/
private function goToEditMode():void {
    modelLocator.selectedComponentState = "edit";
    modelLocator.viewState = ModelLocator.COMPONENT_NEW;

    Application.application.getComponentDetails(key);

}
private function cancelRequest():void {
    resetForm();
    modelLocator.viewState = ModelLocator.COMPONENT_LIST;
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
        } else if (item is ComboBox || item is CheckBox) {
            if (modelLocator.selectedComponentState == "view") {
                item.enabled = false;
            } else {
                item.enabled = true;
            }
        }
        if (item is VBox || item is Form || item is FormItem || item is HBox || item is Tile) {
            makeUnEditable(item);
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