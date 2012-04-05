import mx.controls.Alert;
import mx.rpc.events.FaultEvent;
import mx.rpc.soap.mxml.Operation;
import mx.collections.XMLListCollection;
import mx.controls.Alert;
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
[Bindable]
private var validatorArr:Array;
[Bindable]
public var selectedToDelete:String;
[Bindable]
public var oldType:String;
public var typeType:String = "";
public var typeId:String = "";
[Bindable]
public var myDP:ArrayCollection = new ArrayCollection();

public function feedTypesIn(event:ArrayFiledEvent):void {
    myDP = event.compList;

    dgTypes.dataProvider = myDP;
    dgTypes.height = myDP.length * (dgTypes.rowHeight + 8 ) + dgTypes.headerHeight;
    //showNewForm();
}


public function initModes(data:Object):void {
    this.typeType = data.typeType;
    init();
    typeGetter.getArray(typeType);
    if (modelLocator.selectedComponentState == "edit") {
        typeNew.text = data.name;
        this.typeId = data.id;
        this.oldType = data.name;

        typeAddButton.label = "Update";
        newTypeButton.includeInLayout = true;
        newTypeButton.visible = true;
    } else if (modelLocator.selectedComponentState == "new") {
        typeNew.text = "";
        //outerDocument.oldType = tmpXMLList[0];

        typeAddButton.label = "Add";
        newTypeButton.includeInLayout = false;
        newTypeButton.visible = false;
    }
}
private function init():void {
    validatorArr = new Array();
    validatorArr.push(strV1);
    formls.addEventListener(KeyboardEvent.KEY_DOWN, checkKey);
    if (typeType == 'component') {
        headerText.text = "Component Types";
    } else if (typeType == 'library') {
        headerText.text = "Library Types";
    }
}
function checkKey(event:KeyboardEvent):void
{
    if (event.charCode == 13) {
        validateForm();
    }
}


private function validateForm():void {
    var validatorErrorArray:Array = Validator.validateAll(validatorArr);
    ;
    var isValidForm:Boolean = validatorErrorArray.length == 0;

    var myPattern:RegExp = /\*/g;

    if (isValidForm) {
        if (typeAddButton.label == "Add") {
            typeGetter.addTypeSend(typeType,typeNew.text);
        } else {
            typeGetter.editTypeSend(typeType,typeNew.text,oldType,typeId);
        }

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
private function showNewForm():void {
    typeAddButton.label = "Add";
    newTypeButton.includeInLayout = false;
    newTypeButton.visible = false;
    typeNew.text = "";
    typeTitle.text = "New Type"
}



