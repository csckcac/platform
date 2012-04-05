var Toolbar = function(){
this.createToolbar = function(tree) {
    var config = tree.config;
    var mainNodes = config.getMainNodes();
    var analizer_toolbar = document.getElementById('analizer_toolbar');
   // var nodeTypeSelect = document.getElementById('nodeTypeSelect');


    for(var i=0;i<mainNodes.length;i++){
         if(mainNodes[i].nodeName == "root"){
            continue;
        }
        //Populate the analizer toolbar section
        var newDiv = document.createElement("DIV");
        newDiv.className = "analizer_node";
        var newDivInDiv = document.createElement("DIV");
        newDivInDiv.className = "jstree-draggable " + mainNodes[i].nodeName;


        //Creating the help div
        var helpDiv = null;
        if(HELP_MAP[mainNodes[i].nodeName]!=undefined) {
            helpDiv = document.createElement("DIV");
            helpDiv.innerHTML = HELP_MAP[mainNodes[i].nodeName];
        }

        var nodeDisplayName = mainNodes[i].nodeName;
        if(NODE_MAP[nodeDisplayName]!=undefined){
            nodeDisplayName = NODE_MAP[nodeDisplayName];
        }

        newDivInDiv.innerHTML = nodeDisplayName;
        newDivInDiv.title = mainNodes[i].nodeName;
        newDiv.appendChild(newDivInDiv);
        analizer_toolbar.appendChild(newDiv);
        if(helpDiv != null){
            analizer_toolbar.appendChild(helpDiv);               
        }


        //Create elements for source view
        var buttonDiv = document.createElement("DIV");
        var button = document.createElement("INPUT");
        button.value=nodeDisplayName;
        button.title=mainNodes[i].nodeName;
        button.type = "button";
        button.className = "insertSourceButton";
        var btnClickHandler = function(tree){
            var tmp_btnObj = button;
            var tmp_tree = tree;
            return function(){            //Closure thing to fix the var scope..
                var btnObj = tmp_btnObj;
                var tree = tmp_tree;
                var newNode = tree.xml.createElement(btnObj.title);
                var configNode = tree.config.getConfigNode_From_NodeName(btnObj.title);
                tree.addNewXMLNodeHelper(newNode,configNode,"");
                var strXML = $(newNode).xml();
                $.ajax(
                {
                    data:"xmlString="+strXML,
                    url:"prettyPrinter_ajaxprocessor.jsp",
                    type: "POST",
                    success:function(data){
                        editAreaLoader.setSelectedText('sourceTextArea', data);
                    }
                });
            }
        };
        $(button).click(btnClickHandler(tree));
        buttonDiv.appendChild(button);
        analizer_toolbar.appendChild(buttonDiv);
        

        //Populate the select input element
        var opt =  document.createElement("OPTION");
        opt.value = mainNodes[i].nodeName;
        opt.innerHTML = mainNodes[i].nodeName;
        //nodeTypeSelect.appendChild(opt);
    }

};
}