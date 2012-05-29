import flash.events.Event;

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
var today:Date = new Date();
[Bindable] var thisYear:Number = today.getFullYear();

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

}
public function initEditMode(key:String):void {
    deleteVisible = true;             //Hide/show the delete button on selected libs grid
    copyrightStack.selectedChild = addNewButton;
    iconButton.enabled = true;        //Set the button states
    init();
    licenseGetter.getArray();
    typeGetter.getArray('library');
    libraryGetter.getLibraryDetails(key);
    licenseGetter.getLibLicense(key);
    copyrightGetter.getArray(key);
    backPanelView.visible = false; //Buttons show hide
    backPanelView.includeInLayout = false; //Buttons show hide
    backPanelEditNew.visible = true;
    backPanelEditNew.includeInLayout = true;
}
public function initViewMode(key:String):void {
    deleteVisible = false;             //Hide/show the delete button on selected libs grid
    copyrightStack.selectedChild = addNewButton;
    iconButton.enabled = false;        //Set the button states
    init();
    licenseGetter.getArray();
    typeGetter.getArray('library');
    libraryGetter.getLibraryDetails(key);
    licenseGetter.getLibLicense(key);
    copyrightGetter.getArray(key);
    backPanelView.visible = true;     //Buttons show hide
    backPanelView.includeInLayout = true;     //Buttons show hide
    backPanelEditNew.visible = false;
    backPanelEditNew.includeInLayout = false;
}

public function initNewMode():void {
    deleteVisible = true;             //Hide/show the delete button on selected libs grid
    headingText.text = "New Library";
    iconButton.enabled = true;        //Set the button states
    resetForm();
    init();
    licenseGetter.getArray();
    typeGetter.getArray('library');
    makeUnEditable(workArea);
    backPanelView.visible = false;         //Buttons show hide
    backPanelView.includeInLayout = false;         //Buttons show hide
    backPanelEditNew.visible = true;
    backPanelEditNew.includeInLayout = true;
}
public function resetForm():void {

    selectedLicenses = new ArrayCollection();
    selectedCopyrights = new ArrayCollection();
    copyrightsDG.dataProvider = selectedCopyrights;
    copyrightsDG.visible = false;
   copyrightsDG.includeInLayout = false;
    while (licenseAddBox.numChildren > 0) {
        licenseAddBox.removeChildAt(licenseAddBox.numChildren - 1);
    }
    libraryName.text = "";
    libraryName.errorString = "";

    version.text = "";
    version.errorString = "";

    filename.text = "";
    filename.errorString = "";

    vendor.text = "";
    vendor.errorString = "";

    LibUrl.text = "";
    LibUrl.errorString = "";

    LibDescription.text = "";
    LibDescription.errorString = "";

    copyrightOwner.text = "";
    statement.text = "";

    copyrightStack.selectedChild = addNewButton; 
}
/* ----------------------- Validation and form submition ----------------  */
private function validateForm():void {

    var validatorErrorArray:Array = Validator.validateAll(validatorArr);
    var isValidForm:Boolean = validatorErrorArray.length == 0;
    var noCPmsg:String = "";
    var noLCmsg:String = "";
    if(selectedCopyrights.length<1){
       isValidForm = false;
       noCPmsg = "Please add atleast one Copyright.";
    }
   /* if(selectedLicenses.length<1){
       isValidForm = false;
       noLCmsg = "Please add atleast one License.";
    }*/
    if (isValidForm) {
        var compDetails:ArrayCollection = new ArrayCollection();
        compDetails.addItem({id:key,name:libraryName.text,type:libType.selectedItem.label,version:version.text,vendor:vendor.text,
            url:LibUrl.text,description:LibDescription.text,filename:filename.text});
        libraryGetter.insertComponent(compDetails, selectedLicenses,selectedCopyrights);
        resetForm();
        modelLocator.viewState = ModelLocator.LIBRARY_LIST;
    } else {
        var err:ValidationResultEvent;
        var errorMessageArray:Array = [];
        for each (err in validatorErrorArray) {
            var errField:String = FormItem(err.currentTarget.source.parent.parent).label
            errorMessageArray.push(errField + ": " + err.message);
        }
        errorMessageArray.push(noCPmsg);
//        errorMessageArray.push(noLCmsg);
        Alert.show(errorMessageArray.join("\n\n"), "Invalid form...", Alert.OK);
    }

}

/* ------------------ Geting/processing data for types -----------------------------*/
function feedTypesIn(event:ArrayFiledEvent):void {
    libType.dataProvider = event.compList;
    allTypes=event.compList;
}
/* ------------------ Geting/processing data for form content------------------------*/
function feedLibraryData(event:ArrayFiledEvent):void {
    var compData:ArrayCollection = event.compList;
    key = compData[0].id;
    if (modelLocator.selectedComponentState == "edit") {
        headingText.text = "Edit Library - " + compData[0].name;
    } else if (modelLocator.selectedComponentState == "view") {
        headingText.text = "Library - " + compData[0].name;
    }
    libraryName.text = compData[0].name;
    version.text = compData[0].version;
    filename.text = compData[0].filename;

    var tmpString:String = "";
    tmpString += compData[0].type;
    var pattern:RegExp = new RegExp(tmpString, "i");

    var selectedComboItem:int = 0;
    for (var i:int; i < allTypes.length; i++) {

        if (allTypes[i].label.search(pattern) != -1) {
            selectedComboItem = i;
        }
    }

    libType.selectedIndex = selectedComboItem;

    vendor.text = compData[0].vendor;
    LibUrl.text = compData[0].url;
    LibDescription.text = compData[0].description;
}

/* -------------------- Get Process data for License  ------------------*/
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
/* ------------------------ Changing modes ------------------------------*/
private function goToEditMode():void {
    modelLocator.selectedComponentState = "edit";
    modelLocator.viewState = ModelLocator.LIBRARY_NEW;

    Application.application.getLibraryDetails(key);

}
private function cancelRequest():void {
    resetForm();
    modelLocator.viewState = ModelLocator.COMPONENT_LIST;
}

/*-------------------------- Copyright related methods -------------------*/
[Bindable] var selectedCopyrights:ArrayCollection = new ArrayCollection();
private function addNewCopyright():void {
    var error:String = "";
    copyrightsDG.dataProvider=selectedCopyrights;
    if (copyrightOwner.text == "") {
        error += "Owner field is empty\n\n";
    }
    if (statement.text == "") {
        error += "Statement field is empty\n\n";
    }
    if (error == "") {
        selectedCopyrights.addItem({statement:statement.text,owner:copyrightOwner.text,year:yearCopyright.value});
    } else {
        Alert.show(error);
    }
    if(selectedCopyrights.length >0){
        copyrightsDG.visible = true;
        copyrightsDG.includeInLayout = true;
        copyrightsDG.height = selectedCopyrights.length*50 + copyrightsDG.headerHeight;
    }else{
        copyrightsDG.visible = false;
        copyrightsDG.includeInLayout = false;
    }
}
private function newCopyright():void {
    copyrightStack.selectedChild = formCp;
    copyrightAddButon.label = "Add";
}
private function resetCopyright():void {
    copyrightStack.selectedChild = addNewButton;
    copyrightOwner.text = "";
    statement.text = "";
    copyrightAddButon.label = "Add";
}
private function feedCopyrightData(event:ArrayFiledEvent):void{
    selectedCopyrights = event.compList;

    if(selectedCopyrights.length >0){
        copyrightsDG.visible = true;
        copyrightsDG.includeInLayout = true;
        copyrightsDG.height = selectedCopyrights.length*50 + copyrightsDG.headerHeight;
        copyrightsDG.dataProvider = selectedCopyrights;
    }else{
        copyrightsDG.visible = false;
        copyrightsDG.includeInLayout = false;
    }
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