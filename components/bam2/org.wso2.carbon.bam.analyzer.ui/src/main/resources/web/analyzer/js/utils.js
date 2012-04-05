function popup(txt) {
    // get the screen height and width
    var minObjectHeight = 300;
    var scrollTop = $(window).scrollTop();
    var scrollLeft = $(window).scrollLeft();
    var windowWidth = $(window).width();
    var windowHeight = $(window).height();
    var objectHeight = windowHeight - 200;
     if(objectHeight < minObjectHeight){
         objectHeight = minObjectHeight;
     }
    var objectWidth = windowWidth - 200;


    var maskHeight = $(document).height();
    var maskWidth = $(document).width();
    // calculate the values for center alignment
    var dialogTop = scrollTop + (windowHeight / 2)-(objectHeight/2);//(maskHeight/3) - (jQuery('#dialog-box').height());
    var dialogLeft = scrollLeft + (windowWidth / 2)-(objectWidth/2);//(maskHeight/3) - (jQuery('#dialog-box').height());

    var contentDiv = document.createElement("DIV");
    contentDiv.id = "nodeData";
    contentDiv.className = "xmlTreeAttributes";
    contentDiv.appendChild(txt);

    // assign values to the overlay and dialog box
    if (!$.browser.msie) {
        $('#dialog-overlay').css({height:maskHeight, width:maskWidth}).show();
    }
    $('#dialog-overlay').click(function(){
            $('#dialog-overlay').hide();
            $('#dialog-box').hide();
            $(window).unbind('resize');
        });
    $('.closeButton').click(
            function(){
                $('#dialog-overlay').hide();
                $('#dialog-box').hide();
                $(window).unbind('resize');    
            }
            );
    $('#dialog-box').css({top:dialogTop, left:dialogLeft,width:objectWidth,height:objectHeight}).show();
    $('#dialog-message').css({width:objectWidth,height:(objectHeight-50),overflow:'auto'}).show();
    $('#dialog-message').html('');
    $('#dialog-message').append(contentDiv);
    $(window).resize(
            function() {
                // get the screen height and width
                var scrollTop = $(window).scrollTop();
                var scrollLeft = $(window).scrollLeft();
                var windowWidth = $(window).width();
                var windowHeight = $(window).height();
                var objectHeight = windowHeight - 200;
                var objectWidth = windowWidth - 200;


                var maskHeight = $(document).height();
                var maskWidth = $(document).width();
                // calculate the values for center alignment
                var dialogTop = scrollTop + (windowHeight / 2) - (objectHeight / 2);//(maskHeight/3) - (jQuery('#dialog-box').height());
                var dialogLeft = scrollLeft + (windowWidth / 2) - (objectWidth / 2);//(maskHeight/3) - (jQuery('#dialog-box').height());

                if (!$.browser.msie) {
                    $('#dialog-overlay').css({height:maskHeight, width:maskWidth});
                }

                $('#dialog-box').css({top:dialogTop, left:dialogLeft});
            }
     );

    // display the message
    //jQuery('#dialog-message').html(message);

}

jQuery.fn.xml = function(all) {
    var s = "";
    if( this.length )((( typeof all != 'undefined' ) && all ) ? this :jQuery(this[0]).contents()).each(
        function() {
           s += window.ActiveXObject ? this.xml:(new XMLSerializer()).serializeToString(this);
        }
    );
    return	s;
};
function newXMLDocuemnt(text) {
    if (window.DOMParser)
    {
        parser = new DOMParser();
        xmlDoc = parser.parseFromString(text, "text/xml");
    }
    else // Internet Explorer
    {
        xmlDoc = new ActiveXObject("Microsoft.XMLDOM");
        xmlDoc.async = "false";
        xmlDoc.loadXML(text);
    }
    return xmlDoc;
}
function convertToValidXMlString(originalStr) {
    //Replace all the correct code with invalid code
    var convertedStr = relpaceString(originalStr, "&amp;", "&");
    convertedStr = relpaceString(convertedStr, "&lt;", "<");
    convertedStr = relpaceString(convertedStr, "&gt;", ">");
    convertedStr = relpaceString(convertedStr, "&quot;", '"');
    //    convertedStr = relpaceString(convertedStr, "&#39;", "'");

    //Replace all the invalid code with correct code
    convertedStr = relpaceString(convertedStr, "&", "&amp;");
    convertedStr = relpaceString(convertedStr, "<", "&lt;");
    convertedStr = relpaceString(convertedStr, ">", "&gt;");
    convertedStr = relpaceString(convertedStr, '"', "&quot;");
    //    convertedStr = relpaceString(convertedStr, "'", "&#39;");
    return convertedStr;

}

function relpaceString(originalStr, originalword, relaceword) {
    if (originalStr == undefined || originalStr == null) {
        return null;
    }
    return originalStr.replace(originalword, relaceword);
}
function getChildWithNodeName(children,nodeName){
    var correctOne = false;
    for(var i=0;i<children.length;i++){
        if(children[i].nodeName == nodeName){
            correctOne = children[i];
        }
    }
    return correctOne;
}
$.extend({
  getUrlVars: function(){
    var vars = [], hash;
    var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
    for(var i = 0; i < hashes.length; i++)
    {
      hash = hashes[i].split('=');
      vars.push(hash[0]);
      vars[hash[0]] = hash[1];
    }
    return vars;
  },
  getUrlVar: function(name){
    return $.getUrlVars()[name];
  }
});

