function Tree(xml,configXML){
    this.domNode = document.createElement("DIV");

    this.xml = xml;
    this.configXML = configXML;
    this.nodeName = xml.nodeName;
    this.attributeUIString = "";
    this.tabIndex = 1;
    this.currentPopupXML = null;

    this.config = new Config(configXML);
    this.config.init();


    this.createAddChildUI = function(node){
        //create the outside div element
        var divElm = document.createElement("DIV");

        //create the select element
        var selectNode = document.createElement("SELECT");
        var attbsUI = "";
        var childNodes = this.config.removeCluter(this.config.getSubNodes(node));
        var mainNodesParent = this.config.mainNodesParent;
        for(var i=0;i<childNodes.length;i++){
            var pattern = this.config.getPatternForNode(childNodes[i]);
            if((pattern.repeat == undefined || pattern.repeat > 1) && mainNodesParent.nodeName !=childNodes[i].nodeName ){
                var pathStr = this.config.getPathString(childNodes[i]);
                attbsUI += ('<option value="'+pathStr+'">'+childNodes[i].nodeName+'</option>');
            }
        }
        if(attbsUI== ""){
            return divElm;            
        }
        selectNode.innerHTML = attbsUI;
        divElm.appendChild(selectNode);

        var inputElm = document.createElement("INPUT");
        inputElm.value = "Add";
        inputElm.type = "button";
        inputElm.className = "button";
        var clickHandler = function(node,tree){  //Do the closure thing to get access to the node

            var tmpNode = node;
            var tmpTree = tree;

            return function(){
                var node = tmpNode;
                var tree = tmpTree;
                var outerElem = this.parentNode.parentNode.parentNode.parentNode;
                tree.tabIndex = parseInt(this.parentNode.id);
                var pathStr = $(this).prev().val();

                var configXMLNode = tree.config.getNodeInConfig(tree.configXML,pathStr);//Fix this to get the uniqe itme when there are multiple items.
                var newNode =tree.makeAttributeUI({xmlNode:configXMLNode,initShow:true});
                outerElem.appendChild(newNode);
                var pattern = tree.config.getPatternForNode(configXMLNode);
                 if(pattern.collapsible != undefined && pattern.collapsible) {
                     //Hide all the other sections and only show this element.
                     var sectionsToHide = $('> .shiftingLeft', outerElem);
                     for (var i = 0; i < sectionsToHide.length; i++) {
                         $('> .attributeRow', sectionsToHide[i]).hide('slow');
                         $('> .shiftingLeft', sectionsToHide[i]).hide('slow');
                         $('> ul.addLinkList li.mainLabel img').remove();
                         $('> ul.addLinkList li.mainLabel').each(
                                 function() {
                                     this.innerHTML += ' <img src="../admin/images/arrow-down.png" alt="" />';
                                 }
                                 );
                     }
                     $('> .attributeRow', newNode).show('slow');
                     $('> .shiftingLeft', newNode).show('slow');


                 }
                //If the parent section is hidden .. show it.
                $('> .attributeRow', outerElem).show('slow');
                $('> .shiftingLeft', outerElem).show('slow');

                //Adding the xml node to the XML document object
                tree.updateXML({configNode:configXMLNode,xmlNode:tree.currentPopupXML});

            };
        };
        $(inputElm).click(clickHandler(node,this));
        divElm.appendChild(inputElm);
        divElm.id = (this.tabIndex + 1);
        
        return divElm;
    };
    this.updateXML = function(params){
        var configNode = params.configNode;
        var xmlNode = params.xmlNode;
        var nodeName = xmlNode.nodeName;
        xmlNode.appendChild(this.addNewXMLNodeHelper(xmlNode,nodeName,this.getUniqueId(nodeName,xmlNode.parentNode)));
    };
   
    this.getNodeLevel = function(node){
        var nodeLevel = 1;
        while(node.className != "xmlTreeAttributes"){
            if(node.className == "shiftingLeft"){
                nodeLevel++;
            }
            node = node.parentNode;
        }
        return nodeLevel;
    };
    this.makeAttributeUI = function(params){
        var xmlNode = params.xmlNode;
        var whereToPlace = params.whereToPlace;
        if(xmlNode.nodeName == "root"){
            return null;
        }
        var childNodes = this.config.removeCluter(xmlNode.childNodes);
        var configNode = this.config.getConfigNode(xmlNode);
        var pattern = this.config.getPatternForNode(configNode);
        if(pattern.overrideUI != undefined){
           return window[pattern.overrideUI]({xmlNode:xmlNode,configNode:configNode,tree:this});    //executing the override method
        }
        if(whereToPlace == "main"){   //rootAttributes
            if(!(pattern.nodeType == "rootElements" || pattern.nodeType == "root")){
                return null;               
            }
        }
        var configChilds = this.config.removeCluter(configNode.childNodes);
        //print the attributes
        var configAttribs = pattern.atb;
        var xmlAttribs = xmlNode.attributes;

        var tmpDiv = document.createElement("DIV");
        tmpDiv.className = "shiftingLeft";
        //create the uls
        var theUL = document.createElement("UL");
        theUL.className = "addLinkList";

        var li1 = document.createElement("LI");
        var cellWidth = 200-20*(this.tabIndex) + 'px';
        $(li1).css("width",cellWidth);
        li1.className =  'mainLabel mainLabel'+this.tabIndex;
        li1.innerHTML =   xmlNode.nodeName;
        li1.title =   xmlNode.nodeName;
        if (pattern.collapsible != undefined && pattern.collapsible) {
            li1.innerHTML += ' <img src="../admin/images/arrow-up.png" alt="" />';
            $(li1).css("cursor", "pointer");
            $(li1).click(function() {
                $('img', this).remove();
                var allNodesToHide = $('> .shiftingLeft', this.parentNode.parentNode);
                var visible = false;
                for (var i = 0; i < allNodesToHide.length; i++) {
                    if ($(allNodesToHide[i]).is(":visible")) {
                        visible = true;
                    }
                }

                if (visible) {
                    $('> .attributeRow', this.parentNode.parentNode).hide('slow');
                    $('> .shiftingLeft', this.parentNode.parentNode).hide('slow');
                    this.innerHTML += ' <img src="../admin/images/arrow-down.png" alt="" />';
                } else {
                    $('> .attributeRow', this.parentNode.parentNode).show('slow');
                    $('> .shiftingLeft', this.parentNode.parentNode).show('slow');
                    this.innerHTML += ' <img src="../admin/images/arrow-up.png" alt="" />';
                }

            });
        }
        theUL.appendChild(li1);

        //If the node suppose to hold content other than the children, print the UI elements
        if(pattern.content != undefined){
            var li2 = document.createElement("LI");
            var inputElm =  document.createElement("INPUT");
            inputElm.type = "text";
            if(childNodes.length > 0){
                //This is wrong need to handle the errors
            }
            inputElm.value = xmlNode.textContent;
            li2.appendChild(inputElm);
            theUL.appendChild(li2);
        }else if(configNode !=null && configChilds.length >0){
            var li2 = document.createElement("LI");
            var atbUI = this.createAddChildUI(configNode);

            li2.appendChild(atbUI);
            theUL.appendChild(li2);
        }

        tmpDiv.appendChild(theUL);

        var clearDiv = document.createElement("DIV");
        $(clearDiv).css("clear","both");

        tmpDiv.appendChild(clearDiv);

        var attribRow = document.createElement("DIV");
        attribRow.className = "attributeRow";

        if(configAttribs!=null){
            for(var i=0;i<configAttribs.length;i++){
                if(configAttribs[i].name != "validatePattern"){
                    var attValue = "";
                    for(var j=0;j<xmlAttribs.length;j++){
                        if(xmlAttribs[j].nodeName == configAttribs[i].name){
                            attValue = xmlAttribs[j].value;
                        }
                    }
                    var nodeDisplayName = configAttribs[i].name;
                    if(NODE_MAP[nodeDisplayName]!=undefined){
                        nodeDisplayName = NODE_MAP[nodeDisplayName];
                    }
                    attribRow.innerHTML  += '<div><label style="width:'+(205-20*(this.tabIndex))+'px" class="elmName" title="'+configAttribs[i].name+'">'+nodeDisplayName+'</label> <input class="'+configAttribs[i].validation+'" name="'+configAttribs[i].name+j+i+'" type="text" value="'+attValue+'" /><div style="clear:both"></div></div>';
                }
            }
            if(configAttribs.length ==0){
                attribRow.style.display = "none";   
            }
        }
        tmpDiv.appendChild(attribRow);
        var prevIndex = this.tabIndex;
        if(childNodes.length > 0){
            this.tabIndex++;
            for(var i=0;i<childNodes.length;i++){
                    var newNode = this.makeAttributeUI({xmlNode:childNodes[i],whereToPlace:whereToPlace});
                    if(newNode != null){
                     tmpDiv.appendChild(newNode);
                    }
            }
        }
        this.tabIndex = prevIndex;
        if(pattern.collapsible != undefined && pattern.collapsible){
            $('> .attributeRow',tmpDiv).hide();
            $('> .shiftingLeft',tmpDiv).hide();
        }
        return tmpDiv;
    };


    this.createTreeNode = function(htmlString, node,row) {
        var nodeId = node.nodeName+row;
        var nodeDisplayName = node.nodeName;
        if(NODE_MAP[nodeDisplayName]!=undefined){
            nodeDisplayName = NODE_MAP[nodeDisplayName];
        }
        htmlString += '<li><a id="'+nodeId+'"><ins class="' + nodeDisplayName + '">&nbsp;</ins>' + nodeDisplayName + '</a>';
        node.id =nodeId;
        var children = this.config.removeCluter(node.childNodes);   //TODO handle multiple level of children at startup.
        /*for(var i = 0;i<children.length;i++) {
            for (var j=0;j<this.config.mainNodes.length;j++) {
                if (children[i].nodeName == this.config.mainNodes[j].nodeName) {
                    htmlString += "<ul>";
                    console.info(htmlString);
                    htmlString += this.createTreeNode(htmlString, children[i]);
                    console.info(htmlString);
                    htmlString += "</ul>";
                }
            }
        }*/

        htmlString += "</li>";

        return htmlString;
    };


    this.xmlToTreeHTML = function() {
        var htmlString = "";
        var rootNode = this.config.getRootNode();
        var nodeCount = {};
        if(rootNode == null){
            alert('no root node');
        }
        var mainNodesParent = this.config.mainNodesParent; //This is set to rootNode if the mainNodes are directly under the root node
        mainNodesParent = this.xml.getElementsByTagName(mainNodesParent.nodeName);
        if(mainNodesParent.length>1){
            alert('has many main node"s parents');
        }

        var nodesWithMainNodeName = this.config.removeCluter(mainNodesParent[0].childNodes);
        var k=0;
        for (var i = 0; i < nodesWithMainNodeName.length; i++) {
            if(nodesWithMainNodeName[i].nodeName == ROOT_NODE){
                continue;
            }
            for (var j = 0; j < this.config.mainNodes.length; j++) {
                if (nodesWithMainNodeName[i].nodeName == this.config.mainNodes[j].nodeName) {
                    var nodeName = nodesWithMainNodeName[i].nodeName;
                    var countOfNodes = 0;
                    if(nodeCount[nodeName] != undefined){
                        countOfNodes = parseInt(nodeCount[nodeName]);
                        countOfNodes++;
                        nodeCount[nodeName] = countOfNodes;
                    }else{
                        nodeCount[nodeName] = countOfNodes;
                    }
                    htmlString += this.createTreeNode("", nodesWithMainNodeName[i],countOfNodes);
                    k++;
                }
            }
        }
        if(htmlString.search(/root0/) == -1){
            htmlString = '<ul><li id="root"><a id="root0">'+NODE_MAP['root']+'</a><ul>'+htmlString+'</ul></li></ul>';
        }
        return htmlString;
    };
    this.getXMLFromId = function(id) {
        var nodes = this.xml.getElementsByTagName("*");
        var toReturn = "";
        for (var i = 0; i < nodes.length; i++) {
            if (nodes[i].id == id) toReturn = nodes[i];
        }
        return toReturn;
    };
    this.showNodeData = function(linkObj){
        var xml = this.getXMLFromId(linkObj.id);
        this.attributeUIString = "";
        this.tabIndex = 1;
        if(xml.nodeName == "root"){
            var msg = document.createElement("DIV");
            msg.innerHTML = '<h1>Add nodes by dragging items from the left or using the "Add" button.</h1>';
            popup(msg);
            $('.dialogButtons').hide();
        }else{
            var attributeUI = this.makeAttributeUI({xmlNode:xml});
            popup(attributeUI);
            $('#cancelButton').click(
                    function(){
                        $('#dialog-overlay').hide();
                        $('#dialog-box').hide();
                        $(window).unbind('resize');            
                    }
            );
            //Add validations
            this.addValidations(attributeUI);
            this.currentPopupXML = xml;
            $('.dialogButtons').show();
        }

    };
    this.addValidations=function(){
        var tree = this;
        var submitFunction = function(tree){ //Closure scope handling to get the tree boject to the call back method
            var tmpTree = tree;
            return function(){
                tree.saveUItoXMLNode();
            };
        };
        var v = $("#attributeForm").validate({
            submitHandler: submitFunction(tree)
        });
    };
    this.saveMainConfig = function(){
        var pattern = this.config.getPatternForNode(this.config.getRootNode());
         if(pattern.overrideSave != undefined){
            window[pattern.overrideSave]({xmlNode:this.xml,configNode:this.config.getRootNode(),tree:this});    //executing the override method
            return;
        }
        var xml = this.xml.childNodes[0];
        var MainAttributes = $("#mainAttributes div.attributeRow:first label.elmName");
        for (var i = 0; i < MainAttributes.length; i++) {
            var attbValue = $(MainAttributes[i]).next().val();
            //Do the validation here
            if(attbValue!=""){
                xml.setAttribute(MainAttributes[i].title, attbValue);
            }
        }
        var allNodes = this.config.removeCluter(xml.childNodes);

        //get all child Nodes
        var nodeData = $('#mainAttributes');
        var children = $("div.shiftingLeft:first > div.shiftingLeft",nodeData);
        for (i = 0; i < children.length; i++) {
            var domElement = children[i];
            var childNodeName = $('ul.addLinkList:first li:first', domElement)[0].title;
            xml = getChildWithNodeName(allNodes,childNodeName);



            var nextNode = $('ul.addLinkList:first li:nth-child(2) input[type="text"]', domElement);
            if (nextNode.length > 0) {
                //node has a value
                xml.appendChild(this.xml.createTextNode(nextNode[0].value));
                continue;
            }
            //set the attributes
            var attributes = $("div.attributeRow:first label.elmName", domElement);
            for (var j = 0; j < attributes.length; j++) {
                if($(attributes[j]).next().val()!=""){
                    xml.setAttribute(attributes[j].title, $(attributes[j]).next().val());
                }
            }
        }

    };
    this.saveUItoXMLNode=function() {
        var xml = this.currentPopupXML;

        
        var configNode = this.config.getConfigNode(xml);
        var pattern = this.config.getPatternForNode(configNode);
        if(pattern.overrideSave != undefined){
            window[pattern.overrideSave]({xmlNode:xml,configNode:configNode,tree:this});    //executing the override method
            $('#dialog-overlay').hide();
            $('#dialog-box').hide();
            $(window).unbind('resize');
            return;
        }


        var mainAttributes = $("#nodeData div.attributeRow:first label.elmName");
        for (var i = 0; i < mainAttributes.length; i++) {
            var attbValue = $(mainAttributes[i]).next().val();
            //Do the validation here
            xml.setAttribute(mainAttributes[i].title, attbValue);
        }
         //Remove all the elements from the previous node and add new ones with the values coming from the UI
        var allNodes = this.config.removeCluter(xml.childNodes);
        for (var i = 0; i < allNodes.length; i++) {
            xml.removeChild(allNodes[i]);
        }

        //get all child Nodes
        var nodeData = $('#nodeData');
        var children = $("div.shiftingLeft:first > div.shiftingLeft",nodeData);
        for (i = 0; i < children.length; i++) {
            xml.appendChild(this.saveUItoXMLNodeHelper(children[i]));
        }
        $('#dialog-overlay').hide();
        $('#dialog-box').hide();
        $(window).unbind('resize');


    };
    this.saveUItoXMLNodeHelper = function(domElement) {
        var childNodeName = $('ul.addLinkList:first li:first', domElement)[0].title;
        var newElm = this.xml.createElement(childNodeName);
          

        var nextNode = $('ul.addLinkList:first li:nth-child(2) input[type="text"]', domElement);
        if (nextNode.length > 0) {
            //node has a value
            newElm.appendChild(this.xml.createTextNode(nextNode[0].value));
            return newElm;
        }
        //set the attributes
        var attributes = $("div.attributeRow:first label.elmName", domElement);
        for (var j = 0; j < attributes.length; j++) {
            newElm.setAttribute(attributes[j].title, $(attributes[j]).next().val());
        }


        var subChildren = $("> div.shiftingLeft", domElement);
        for (var i = 0; i < subChildren.length; i++) {
            newElm.appendChild(this.saveUItoXMLNodeHelper(subChildren[i]));
        }
        return newElm;
    };
    this.getConfigNode2XMLNode = function(configNode){
        var nodeName = configNode;
        if(typeof(configNode) == "object"){
            nodeName = configNode.nodeName;
        }
        return this.xml.getElementsByTagName(nodeName)[0];

    };
    this.addNewXMLNode = function(nodeName,nodeId){
        //get the main node parent
        var mainNodesParent = this.config.mainNodesParent;
        mainNodesParent = this.getConfigNode2XMLNode(mainNodesParent);
        var configNode = this.config.getConfigNode_From_NodeName(nodeName);
        this.addNewXMLNodeHelper(mainNodesParent,configNode,nodeId);
    };
    this.addNewXMLNodeHelper = function(parent,configNode,nodeId){

        if(configNode == undefined){
            return configNode;
        }
        var pattern = this.config.getPatternForNode(configNode);

        var mainNode = this.xml.createElement(configNode.nodeName);
        if(nodeId != ""){
            mainNode.id = nodeId;
        }
        var attbs = pattern.atb;
        for(var i=0;i<attbs.length;i++){
            mainNode.setAttribute(attbs[i].name,"");
        }

        var subNodes = this.config.removeCluter(configNode.childNodes);
        for(i=0;i<subNodes.length;i++){
            var subNodePattern = this.config.getPatternForNode(subNodes[i]);
             if(subNodePattern.required){
                var newNode = this.addNewXMLNodeHelper(mainNode,subNodes[i].nodeName,"");
                if(newNode != undefined){
                    parent.appendChild(newNode);
                }
            }

        }
        parent.appendChild(mainNode);
        return parent;
    };
    this.getChildrenWithNodeName = function(nodeName,parentNode){                        
        var allChildren = parentNode.childNodes;
        var children = new Array();
        for(var i=0;i<allChildren.length;i++){
            if(allChildren[i].nodeName == nodeName){
                children.push(allChildren[i]);
            }
        }

        return children;
    };
    this.getUniqueId = function(nodeName,parentNode){
        var numberofNodes = this.getChildrenWithNodeName(nodeName,parentNode);
        return "" + nodeName + numberofNodes++;
    };
    this.createTree = function() {
        var tree=this;
        $("#analizerTreeContainer").bind("loaded.jstree", function (event, data) {
                $('.jstree-leaf a').click(
                    function(){
                        tree.showNodeData(this);
                    }
                );
        }).jstree({
            "ui" : {
                "select_limit" : 2,
                "select_multiple_modifier" : "alt",
                "selected_parent_close" : "select_parent",
                "initially_select" : [ "root" ]
            },
            "core" : { "initially_open" : [ "root" ] },
            "html_data" : {
                "data" : this.xmlToTreeHTML()
            },
            "dnd" : {
                "drop_finish" : function () {
                    //alert("DROP");
                },
                "drop_check":function (data) {
                    return true;
                },
                "drag_check" : function (data) {
                    var dropElementId = $('> a',data.r)[0].id;
                    
                    if(dropElementId == (ROOT_NODE + "0")){
                    }else{
                        return false;
                    }
                    
                    return {
                        after : false,
                        before : false,
                        inside : true
                    };
                },
                "drag_finish" : function (data) {
                    var nodeName = data.o.title;
                    var nodeId = tree.getUniqueId(nodeName,tree.getConfigNode2XMLNode(tree.config.mainNodesParent));
                        $("#analizerTreeContainer").jstree("create", data.r, false, data.o.innerHTML, function(treeElm){
                            $('a:first',treeElm[0])[0].id = nodeId;
                            $('a:first',treeElm[0])[0].title = data.o.title;
                            $('a:first ins',treeElm[0])[0].className = data.o.title;
                            //var controlPanel = document.createElement("span");
                            //controlPanel.innerHTML = "fooooooo";
                            //treeElm[0].appendChild(controlPanel);
                        }, true);
                      
                    tree.addNewXMLNode(nodeName,nodeId);// Add a xml node with the default settings
                    $('.jstree-leaf a').click(
                        function(){
                            tree.showNodeData(this);
                        }
                    );
                   


                }
            },
            "plugins" : [ "themes", "html_data","dnd","crrm","ui" ]
        });
    };
    
}