package org.wso2.licensemgt.model
{
import com.adobe.cairngorm.model.IModelLocator;

import mx.core.Application;
import mx.controls.TextArea;

[Bindable]
public class ModelLocator implements IModelLocator {
    // Single Instance of Our ModelLocator
    private static var instance:ModelLocator;

    public function ModelLocator(enforcer:SingletonEnforcer) {
        if (enforcer == null) {
            throw new Error("You Can Only Have One ModelLocator");
        }
    }

    // Returns the Single Instance
    public static function getInstance() : ModelLocator {
        if (instance == null) {
            instance = new ModelLocator(new SingletonEnforcer);
        }
        return instance;
    }

    public static function setUpTextArea(param:TextArea,initHeight:Number):void {
        param.width = Application.application.screen.width - 500;
        param.height = initHeight;

        //param.height = Application.application.screen.height - (Application.application.screen.height / 3) * 2;
    }

    public static function setDownTextArea(param:TextArea,initHeight:Number):void {
        param.width = 400;
        param.height = initHeight;
    }

    //DEFINE YOUR VARIABLES HERE
    public var viewState:uint = 0;
    public var selectedComponent:XML;
    public var selectedComponentState:String;
    public var selectedComponentValid:Boolean;
    public var selectedToDelete:String;
    //Variables for component detail page
    public var selectedName:String;

    //CONSTRAINTS
    public static const HOME:uint = 0;
    public static const LICENSE_LIST:uint = 1;
    public static const COMPONENT_LIST:uint = 2;
    public static const LIBRARY_LIST:uint = 3;
    public static const LICENSE_NEW:uint = 4;
    public static const COMPONENT_NEW:uint = 5;
    public static const LIBRARY_NEW:uint = 6;
    public static const TYPES:uint = 7;


    //To fix the auto complete bug
    public var compComplete:int = 0;

}
}
// Utility Class to Deny Access to Constructor
class SingletonEnforcer {
}