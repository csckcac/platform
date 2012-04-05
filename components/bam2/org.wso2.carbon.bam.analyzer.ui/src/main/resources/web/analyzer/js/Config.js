function Config(configXML){
    this.configXML = configXML;

    this.getMainNodes = function(){
        var mainNodes = new Array();
        var nodesWithPatterns = this.getNodesWithPatterns();
        for(var i=0;i<nodesWithPatterns.length;i++){
            var pattern = eval('(' + this.getAttribute(nodesWithPatterns[i],"validatePattern") + ')');
            if(pattern.nodeType == "mainNode"){
                mainNodes.push(nodesWithPatterns[i]);
            }
        }
        return mainNodes;
    };
    this.getRootNode = function(){

        var rootNode = null;
        var nodesWithPatterns = this.getNodesWithPatterns();
        for(var i=0;i<nodesWithPatterns.length;i++){
            var pattern = eval('(' + this.getAttribute(nodesWithPatterns[i],"validatePattern") + ')');
            if(pattern.nodeType == "root"){
                rootNode = nodesWithPatterns[i];
            }
        }
        return rootNode;
    };
    this.getNodesWithPatterns = function(){
        var allNodes = this.configXML.getElementsByTagName("*");
        var nodesWithPatterns = new Array();
        for(var i=0;i<allNodes.length;i++){

            if(this.hasAttribute(allNodes[i],"validatePattern")){
                nodesWithPatterns.push(allNodes[i]);
            }
        }
        return nodesWithPatterns;
    };
    this.getPatternForNode = function(node){
        var pattern = null;
        var configNode = this.getConfigNode(node);
        var patternForNode = this.getAttribute(configNode,"validatePattern");
        if(patternForNode != null){
            pattern = eval('(' + patternForNode + ')');
        }
        return pattern;
    };
    this.getSubNodes = function(node){
        return node.childNodes;        
    };
    this.getConfigNode_From_NodeName= function(nodeName){
        var mainNodes = this.getMainNodes();
        for(var i=0;i<mainNodes.length;i++){
            if(mainNodes[i].nodeName == nodeName){
                return mainNodes[i];
            }
        }
    };
    this.getConfigNode = function(node){
        //this.getPathString(node);
        if(typeof(node) == "string"){
            node = this.getConfigNode_From_NodeName(node);
        }

        if(node.getAttribute("validatePattern") == null){//node is not a config node ... so continue..
            var configNode = this.getNodeInConfig(this.configXML,this.getPathString(node));
        }else{
            configNode = node;
        }

        return configNode;
    };
    /* -- helper functions for the getConfigNode */
     this.getPathString = function(xmlNode){

        var pathString = xmlNode.nodeName;
        while(xmlNode.parentNode != null){
            xmlNode = xmlNode.parentNode;
            pathString+=("/"+xmlNode.nodeName);
        }
        return pathString;
    };
    this.getNodeInConfig = function(configNode,str){
        if(this.getPathString(configNode) == str){
            return configNode;
        }else{
            var children = this.removeCluter(configNode.childNodes);
            var rightConfig = null;
            for(var i=0;i<children.length;i++){
                rightConfig = this.getNodeInConfig(children[i],str);
                if(rightConfig !=null){
                    break;
                }
            }
            return rightConfig;
        }
    };
    /* END helper functions for the getConfigNode */

    this.hasAttribute = function (xmlNode,attribName){
        var attribs = xmlNode.attributes;
        var found = false;
        for(var i=0;i<attribs.length;i++){
            if(attribs[i].name == attribName){
                found = true;
            }
        }
        return found;
    };
    this.getAttribute = function (xmlNode,attribName){
        var attribs = xmlNode.attributes;
        var attribValue = false;
        for(var i=0;i<attribs.length;i++){
            if(attribs[i].name == attribName){
                attribValue = attribs[i].value;
            }
        }
        return attribValue;
    };
     this.removeCluter = function(cluteredChildren) {
        var pureChildren = new Array();
        for (var k=0;k<cluteredChildren.length;k++) {
            if (cluteredChildren[k].nodeName != undefined && cluteredChildren[k].nodeName != "#text" && cluteredChildren[k].nodeName != "#comment") {
                pureChildren.push(cluteredChildren[k]);
            }
        }

        return pureChildren;
    };
    this.findMainNodesParent = function(rootNode){
        var children = this.removeCluter(rootNode.childNodes);
        var mainNodes = this.mainNodes;
        var mainNodesParent = null;
        for(var i =0;i< children.length;i++){
            for(var j = 0;j< mainNodes.length;j++){
                if(children[i].nodeName == mainNodes[j].nodeName){
                    mainNodesParent = children[i].parentNode;
                }
            }
        }
        if(mainNodesParent == null && children.length > 0){
            for(var i=0;i<children.length;i++){
                if(mainNodesParent==null){
                    mainNodesParent = this.findMainNodesParent(children[i]);
                }
            }
        }
        return mainNodesParent;

    };
    this.createInitialXML = function() {
        var root = this.getRootNode();
        var pattern = this.getPatternForNode(root);
        var xmlString = '<'+root.nodeName + '/>';
        var xmlDoc = newXMLDocuemnt(xmlString);
        var rootElm = xmlDoc.childNodes[0];
        var atbs = pattern.atb;
        for(var i=0;i<atbs.length;i++){
            var defaultValue = "";
            if(atbs[i]['default'] != undefined){
                defaultValue = atbs[i]['default'];
            }
            rootElm.setAttribute(atbs[i].name,defaultValue);    
        }
        var children = this.removeCluter(root.childNodes);
        for (var i = 0; i < children.length;i++) {
            pattern = this.getPatternForNode(children[i]);
            if(pattern.nodeType == "rootElements" || pattern == false){
                var newElm = xmlDoc.createElement(children[i].nodeName);
                if(pattern != false) {
                    atbs = pattern.atb;
                    for (var j = 0; j < atbs.length; j++) {
                        defaultValue = "";
                        if (atbs[j]['default'] != undefined) {
                            defaultValue = atbs[j]['default'];
                        }
                        newElm.setAttribute(atbs[j].name, defaultValue);
                    }
                }
                rootElm.appendChild(newElm);
            }
        }
        return xmlDoc;
    };
    this.init = function(){
        this.mainNodes = this.getMainNodes();
        this.rootNode = this.getRootNode();
        this.mainNodesParent = this.findMainNodesParent(this.rootNode);
    };
}