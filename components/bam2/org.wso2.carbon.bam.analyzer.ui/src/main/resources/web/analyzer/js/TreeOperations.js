function init(data_XML, dataConfigXML){
    
    if(data_XML.getElementsByTagName(ROOT_NODE).length == 0){  //Handling a special case where the first node is not the root node
        var configRootNode = dataConfigXML.getElementsByTagName(ROOT_NODE)[0];
        //adding a root node to the data_XML
        var dataRootNodesParent = data_XML.getElementsByTagName(configRootNode.parentNode.nodeName)[0];
        var nodeToAdd = data_XML.createElement(ROOT_NODE);
        dataRootNodesParent.appendChild(nodeToAdd);
    }
    var tree = new Tree(data_XML, dataConfigXML);//Create a new tree instance
    tree.createTree(); //create the actual tree

    var toolbar = new Toolbar(); //Create new toolbar instance and initiate it
    toolbar.createToolbar(tree);
    $('.insertSourceButton').hide(); 
    //Load the main attributes to the UI
    //debugger;
    $('#mainAttributes').html(tree.makeAttributeUI({xmlNode:data_XML.documentElement,whereToPlace:'main'}));

    /*//Register events for the add button to add nodes
    $("#addButton").click(function() {
        var selectedNodeType = document.getElementById('nodeTypeSelect').options[document.getElementById('nodeTypeSelect').selectedIndex].value;

        //Add node to the UI
        $("#analizerTreeContainer").jstree("create",-1,false,selectedNodeType,false,true);

        //Add the relevent xml node to the xml dom according to the configuration xml
        

    });*/

    //Init tabs
    $('#tabs ul li a').click(function(){
        $('#tabs ul li').removeClass("ui-tabs-selected");
        this.parentNode.className = "ui-tabs-selected";
        if(this.id == "designTab"){
            loadDesign(dataConfigXML);
        }else{
            loadSource(tree);
        }
    });
    //Hide the source view @ start
    $('#sourceView').hide();

    //Initate the toggleble titles
    initSections("");

    //Init the saveAll button
    $('#saveAllButton').click(
            function(){
                var toSave = "";
                var mode = $.getUrlVar('mode');
                if($('#designView').is(':visible')){
                    tree.saveMainConfig();
                    toSave = $(tree.xml).xml();
                }else{
                    toSave = editAreaLoader.getValue('sourceTextArea');
                }
                $.ajax({
                    data:"xmlString="+toSave+"&mode="+mode,
                    url:'saveXML_ajaxprocessor.jsp',
                    type: "POST",
                    success:function(data){
                        CARBON.showInfoDialog('Analyzer Config Successfully Saved',function(){
                            window.location.href = "../analyzer/index.jsp";
                        });
                                                   
                    }
                });
            }
     );
    $('#cancelAllButton').click(
            function(){
                CARBON.showConfirmationDialog("Are you sure not to save current changes?",function(){window.location.href = "../analyzer/index.jsp?region=region1&item=bam_menu_analyzer&name=bam&ordinal=0";});

            }
            );
}

function loadDesign(dataConfigXML){
    $('#designView').show();
    $('#designView').show();
    $('#sourceView').hide();
    $('.insertSourceButton').hide();
    $('.analizer_node').show();
    var data_XML = editAreaLoader.getValue('sourceTextArea');
    //data_XML = convertToValidXMlString(data_XML);
    //data_XML = $.parseXML(data_XML);
    $('#analyzerXML').val(data_XML);
    document.getElementById('designSourceForm').submit();
}
function loadSourceOnError(strXML){
     $('#designView').hide();
    $('#sourceView').show();

    $('#analizer_nav').hide();

    $('#toolbar_help_design').hide();
    $('#toolbar_help_source').show();

    $('.insertSourceButton').show();
    $('.analizer_node').hide();
    var maxWidth = 0;
    $('.insertSourceButton').each(
            function(){
                if(maxWidth < $(this).width()){
                    maxWidth = $(this).width();
                }
            }
            )
    if(maxWidth != 0){
        $('.insertSourceButton').css("width",(maxWidth+20) + "px")
    }
    //Init tabs
    $('#tabs ul li a').click(function(){
        $('#tabs ul li').removeClass("ui-tabs-selected");
        this.parentNode.className = "ui-tabs-selected";
        if(this.id == "designTab"){
            loadDesign(strXML);
        }
    });
    $.ajax(
    {
        data:"xmlString="+strXML,
        url:"prettyPrinter_ajaxprocessor.jsp",
        type: "POST",
        success:function(data){
            $('#sourceTextArea').text(data);
            editAreaLoader.setValue('sourceTextArea',data);
            editAreaLoader.init({
                id : "sourceTextArea"		// textarea id
                ,syntax: "xml"			// syntax to be uses for highgliting
                ,start_highlight: true		// to display with highlight mode on start-up
            });
            data = data.replace(/<analyzers\/>/gi,"<analyzers></analyzers>");
            editAreaLoader.setValue('sourceTextArea',data);
        }
    });
}
function loadSource(tree){
    var treeXML = tree.xml;
    $('#designView').hide();
    $('#sourceView').show();

    $('#toolbar_help_design').hide();
    $('#toolbar_help_source').show();

    $('.insertSourceButton').show();
    $('.analizer_node').hide();
    var maxWidth = 0;
    $('.insertSourceButton').each(
            function(){
                if(maxWidth < $(this).width()){
                    maxWidth = $(this).width();
                }
            }
            )
    if(maxWidth != 0){
        $('.insertSourceButton').css("width",(maxWidth+20) + "px")    
    }
    tree.saveMainConfig();
    var rootNode = treeXML.getElementsByTagName(ROOT_NODE)[0];
    rootNode.parentNode.removeChild(rootNode);
    var strXML = $(treeXML).xml();
    $.ajax(
    {
        data:"xmlString="+strXML,
        url:"prettyPrinter_ajaxprocessor.jsp",
        type: "POST",
        success:function(data){
            $('#sourceTextArea').text(data);
            editAreaLoader.setValue('sourceTextArea',data);
            editAreaLoader.init({
                id : "sourceTextArea"		// textarea id
                ,syntax: "xml"			// syntax to be uses for highgliting
                ,start_highlight: true		// to display with highlight mode on start-up
            });
            data = data.replace(/<analyzers\/>/gi,"<analyzers></analyzers>");
            editAreaLoader.setValue('sourceTextArea',data);
        }
    });


}
function initSections(initHidden) {
   // $(".togglebleTitle").next().css("border","solid 1px #ccc");
    if (initHidden == "hidden") {
        $(".togglebleTitle").next().hide();
        $(".togglebleTitle").removeClass("contentHidden");
    } else {
         if( !($(".togglebleTitle").next().is(":visible"))){   //Ignore change if the style is set to display:none
            $(".togglebleTitle").addClass("contentHidden");
        }
    }
    $(".togglebleTitle").click(
            function() {
                if ($(this).next().is(":visible")) {
                    $(this).removeClass("contentHidden");
                } else {
                    $(this).addClass("contentHidden");
                }
                $(this).next().toggle("fast");
            }
    );
}
