
var _xmlDoc = "";
var xmlObj, _xmlObj;
var xmlDoc = {
    createNewDoc : function(w){
        if(!w){
            _xmlDoc = "<bamConfiguration><CFIndexes></CFIndexes></bamConfiguration>";
            _xmlObj = jQuery.parseXML(_xmlDoc);
            //alert(_xmlObj);
            //_xmlObj = jQuery(xmlObj);
        }
        else{
            _xmlDoc = "<bamConfiguration><CFIndexes>" + w +
                    "</CFIndexes></bamConfiguration>";
        }
        //alert(_xmlDoc);
        //jQuery("#testText").val(_xmlDoc);
        //xmlObj = jQuery.parseXML(_xmlDoc.toString());
        //_xmlObj = jQuery(xmlObj);
    },
    creteNode : function(name,dCf,gan,rk,xmlDoc){
        var newEl = xmlDoc.createElement("ColumnFamily"),
        _gan = xmlDoc.createElement("granularity"),
        _IRKey = xmlDoc.createElement("indexRowKey"),
        _ganTxt = xmlDoc.createTextNode(gan),
        _IRKeyTxt = xmlDoc.createTextNode(rk);
        newEl.setAttribute("name",name);
        newEl.setAttribute("defaultCF",dCf);
        _gan.appendChild(_ganTxt);
        newEl.appendChild(_gan);
        _IRKey.appendChild(_IRKeyTxt);
        newEl.appendChild(_IRKey);
        x=xmlDoc.getElementsByTagName("CFIndexes")[0];
        x.appendChild(newEl);
        CreateHtml(xmlDoc);
    },
    deleteCFNode : function(nodeContainer){

    }
}

function addNewPart(c){
    jQuery("#"+c+" li ul.secondLevel").append("<li class='rowKey'>Part Name : " +
            "<input type='text' name='txt_PartName' value=''/>&nbsp;&nbsp; " +
            "Store Index : <input type='checkbox' name='cb_StoreIndex' value=''/></li>");
}
function showHideCFParts(pc){
    var partCont = jQuery("#" + pc + "").find("ul.secondLevel");
    if(partCont.is(":hidden")){
        partCont.show();
    }
    else{
        partCont.hide();
    }
}
function CreateHtml(xml){
    var i=1;
    var htmlString = "";
    jQuery(xml).find("ColumnFamily").each(function(){
        htmlString += "<ul id='ul_" + jQuery(this).attr("name").toString() + "'><li>";
        htmlString += "CF Name : " +
                "<input type='text' name='txt_CFName' value='" + jQuery(this).attr("name").toString() + "'/>";
        if(jQuery(this).attr("defaultCF")){
            if(jQuery(this).attr("defaultCF").toString()=="true"){
                htmlString += "&nbsp;&nbsp; isDefault : " +
                        "<input name='cb_isDefault' type='checkbox' id='defaultCF' checked='true' />";
            }
            else{
                htmlString +=  "&nbsp;&nbsp; isDefault : <input name='cb_isDefault' type='checkbox' id='defaultCF'/>";
            }
        }
        else{
            htmlString += "&nbsp;&nbsp; isDefault : <input name='cb_isDefault' type='checkbox' id='defaultCF' />";
        }
        if (jQuery(this).children("granularity")){
            htmlString += "&nbsp;&nbsp; Granularity : " +
                    "<input type='text' name='txt_Granularity' value='" + jQuery(this).children("granularity").text() + "'/>";
        }
        if (jQuery(this).children("indexRowKey")){
            htmlString += "&nbsp;&nbsp; Index Row Key : " +
                    "<input type='text' name='txt_IRKey' value='" + jQuery(this).children("indexRowKey").text() + "'/>";
        }

        htmlString += '<a href='+'#'+' class='+'deleteNodeLink'+' onClick='+
                'deleteNode("ul_' + String(jQuery(this).attr("name")) + '");'+'>Delete Node</a>';
        htmlString += '<a href='+'#'+' class='+'showHideParts'+' onClick=' + 'showHideCFParts("ul_' + jQuery(this).attr("name") + '");' + '>show / hide parts</a>';
        htmlString += "<ul class='secondLevel'>";
        htmlString += '<li><a href='+'#'+' class='+'addPartsLink'+' onClick='+
                'addNewPart("ul_' + String(jQuery(this).attr("name")) + '");'+'>add part</a></li>';
        jQuery(this).find("rowKey").find("part").each(function(){
            htmlString +=
                    "<li class='rowKey'>Part Name : <input type='text' name='txt_PartName' id='" +
                    jQuery(this).parent().parent().attr("name").toString() + "$"
                    + jQuery(this).attr("name").toString() + "' value='"+ jQuery(this).attr("name").toString() + "'/>";
            if(jQuery(this).attr("storeIndex")){
                if(jQuery(this).attr("storeIndex").toString()=="true"){
                    htmlString += "&nbsp;&nbsp; Store Index : " +
                            "<input type='checkbox' name='cb_StoreIndex' value='' checked='true'/></li>";
                }
            }
        });
        htmlString += '</ul></li></ul>';
    });
    htmlString += "<div class='saveXmlBtn'> <input type='button' id='btn_saveXML' value='Save XMl' onClick='saveXML()' /></div>";
    if(htmlString.length > 0){
	    jQuery("#accordion").html(htmlString);
    }
}

function deleteNode(c){
     jQuery("#"+c+"").remove();
}

function saveXML(){
    var htmlCont = jQuery("#accordion ul[id^='ul_']");
    var allParentNodes = htmlCont.filter("[id^='ul_']");
    var toXMLString = "";
    allParentNodes.each(function(){
        toXMLString += '<ColumnFamily name="' + jQuery(this).find("input[name='txt_CFName']").val() + '" defaultCF="' +
                jQuery(this).find("input[name='cb_isDefault']").is(":checked") + '">';
        toXMLString += "\n";
        toXMLString += '<granularity>' + jQuery(this).find("input[name='txt_Granularity']").val() + '</granularity>';
        toXMLString += "\n";
        toXMLString += '<rowKey>';
        toXMLString += "\n";
        jQuery(this).find("li[class='rowKey']").each(function(){
             toXMLString += '<part name="' + jQuery(this).find("input[name='txt_PartName']").val() + '" ' +
                     'storeIndex="' + jQuery(this).find("input[name='cb_StoreIndex']").is(":checked") + '">';
             toXMLString += '\n';
        });
        toXMLString += '</rowKey>';
        toXMLString += "\n";
        toXMLString += '<indexRowKey>' + jQuery(this).find("input[name='txt_IRKey']").val() + '</indexRowKey>';
        toXMLString += "\n";
        toXMLString += '</ColumnFamily>';
        toXMLString += "\n";
    });

    xmlDoc.createNewDoc(toXMLString);
}



var _xmlDoc = "";
var xmlObj, _xmlObj;
var xmlDoc = {
    createNewDoc : function(w){
        if(!w){
            _xmlDoc = "<bamConfiguration><CFIndexes></CFIndexes></bamConfiguration>";
            _xmlObj = jQuery.parseXML(_xmlDoc);
            //alert(_xmlObj);
            //_xmlObj = jQuery(xmlObj);
        }
        else{
            _xmlDoc = "<bamConfiguration><CFIndexes>" + w +
                    "</CFIndexes></bamConfiguration>";
        }
        //alert(_xmlDoc);
        //jQuery("#testText").val(_xmlDoc);
        //xmlObj = jQuery.parseXML(_xmlDoc.toString());
        //_xmlObj = jQuery(xmlObj);
    },
    creteNode : function(name,dCf,gan,rk,xmlDoc){
        var newEl = xmlDoc.createElement("ColumnFamily"),
        _gan = xmlDoc.createElement("granularity"),
        _IRKey = xmlDoc.createElement("indexRowKey"),
        _ganTxt = xmlDoc.createTextNode(gan),
        _IRKeyTxt = xmlDoc.createTextNode(rk);
        newEl.setAttribute("name",name);
        newEl.setAttribute("defaultCF",dCf);
        _gan.appendChild(_ganTxt);
        newEl.appendChild(_gan);
        _IRKey.appendChild(_IRKeyTxt);
        newEl.appendChild(_IRKey);
        x=xmlDoc.getElementsByTagName("CFIndexes")[0];
        x.appendChild(newEl);
        CreateHtml(xmlDoc);
    },
    deleteCFNode : function(nodeContainer){

    }
}

function addNewPart(c){
    jQuery("#"+c+" li ul.secondLevel").append("<li class='rowKey'>Part Name : " +
            "<input type='text' name='txt_PartName' value=''/>&nbsp;&nbsp; " +
            "Store Index : <input type='checkbox' name='cb_StoreIndex' value=''/></li>");
}
function showHideCFParts(pc){
    var partCont = jQuery("#" + pc + "").find("ul.secondLevel");
    if(partCont.is(":hidden")){
        partCont.show();
    }
    else{
        partCont.hide();
    }
}
function CreateHtml(xml){
    var i=1;
    var htmlString = "";
    jQuery(xml).find("ColumnFamily").each(function(){
        htmlString += "<ul id='ul_" + jQuery(this).attr("name").toString() + "'><li>";
        htmlString += "CF Name : " +
                "<input type='text' name='txt_CFName' value='" + jQuery(this).attr("name").toString() + "'/>";
        if(jQuery(this).attr("defaultCF")){
            if(jQuery(this).attr("defaultCF").toString()=="true"){
                htmlString += "&nbsp;&nbsp; isDefault : " +
                        "<input name='cb_isDefault' type='checkbox' id='defaultCF' checked='true' />";
            }
            else{
                htmlString +=  "&nbsp;&nbsp; isDefault : <input name='cb_isDefault' type='checkbox' id='defaultCF'/>";
            }
        }
        else{
            htmlString += "&nbsp;&nbsp; isDefault : <input name='cb_isDefault' type='checkbox' id='defaultCF' />";
        }
        if (jQuery(this).children("granularity")){
            htmlString += "&nbsp;&nbsp; Granularity : " +
                    "<input type='text' name='txt_Granularity' value='" + jQuery(this).children("granularity").text() + "'/>";
        }
        if (jQuery(this).children("indexRowKey")){
            htmlString += "&nbsp;&nbsp; Index Row Key : " +
                    "<input type='text' name='txt_IRKey' value='" + jQuery(this).children("indexRowKey").text() + "'/>";
        }

        htmlString += '<a href='+'#'+' class='+'deleteNodeLink'+' onClick='+
                'deleteNode("ul_' + String(jQuery(this).attr("name")) + '");'+'>Delete Node</a>';
        htmlString += '<a href='+'#'+' class='+'showHideParts'+' onClick=' + 'showHideCFParts("ul_' + jQuery(this).attr("name") + '");' + '>show / hide parts</a>';
        htmlString += "<ul class='secondLevel'>";
        htmlString += '<li><a href='+'#'+' class='+'addPartsLink'+' onClick='+
                'addNewPart("ul_' + String(jQuery(this).attr("name")) + '");'+'>add part</a></li>';
        jQuery(this).find("rowKey").find("part").each(function(){
            htmlString +=
                    "<li class='rowKey'>Part Name : <input type='text' name='txt_PartName' id='" +
                    jQuery(this).parent().parent().attr("name").toString() + "$"
                    + jQuery(this).attr("name").toString() + "' value='"+ jQuery(this).attr("name").toString() + "'/>";
            if(jQuery(this).attr("storeIndex")){
                if(jQuery(this).attr("storeIndex").toString()=="true"){
                    htmlString += "&nbsp;&nbsp; Store Index : " +
                            "<input type='checkbox' name='cb_StoreIndex' value='' checked='true'/></li>";
                }
            }
        });
        htmlString += '</ul></li></ul>';
    });
    htmlString += "<div class='saveXmlBtn'> <input type='button' id='btn_saveXML' value='Save XMl' onClick='saveXML()' /></div>";
    if(htmlString.length > 0){
	    jQuery("#accordion").html(htmlString);
    }
}

function deleteNode(c){
     jQuery("#"+c+"").remove();
}

function saveXML(){
    var htmlCont = jQuery("#accordion ul[id^='ul_']");
    var allParentNodes = htmlCont.filter("[id^='ul_']");
    var toXMLString = "";
    allParentNodes.each(function(){
        toXMLString += '<ColumnFamily name="' + jQuery(this).find("input[name='txt_CFName']").val() + '" defaultCF="' +
                jQuery(this).find("input[name='cb_isDefault']").is(":checked") + '">';
        toXMLString += "\n";
        toXMLString += '<granularity>' + jQuery(this).find("input[name='txt_Granularity']").val() + '</granularity>';
        toXMLString += "\n";
        toXMLString += '<rowKey>';
        toXMLString += "\n";
        jQuery(this).find("li[class='rowKey']").each(function(){
             toXMLString += '<part name="' + jQuery(this).find("input[name='txt_PartName']").val() + '" ' +
                     'storeIndex="' + jQuery(this).find("input[name='cb_StoreIndex']").is(":checked") + '">';
             toXMLString += '\n';
        });
        toXMLString += '</rowKey>';
        toXMLString += "\n";
        toXMLString += '<indexRowKey>' + jQuery(this).find("input[name='txt_IRKey']").val() + '</indexRowKey>';
        toXMLString += "\n";
        toXMLString += '</ColumnFamily>';
        toXMLString += "\n";
    });

    xmlDoc.createNewDoc(toXMLString);
}


