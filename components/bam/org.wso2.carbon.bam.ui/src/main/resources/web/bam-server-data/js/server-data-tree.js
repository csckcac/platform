//YUI browser detection
if (YAHOO.env.ua.gecko) {
    //if not IE
    var topPos = 200;
} else {
    //if IE
    topPos = 220;
}

var maxWidth = 200;
function hideTreeItem(type, args,me){
    me.style.display = "none";
}
function treeColapse(icon) {
    var parentNode = icon.parentNode;
    var allChildren = parentNode.childNodes;
    var todoOther = "";
    var attributes = "";
    //Do minimizing for the rest of the nodes
    for (var i = 0; i < allChildren.length; i++) {
        if (allChildren[i].nodeName == "UL") {

            if (allChildren[i].style.display == "none") {
                attributes = {
                    opacity: { to: 1 }
                };
                var anim = new YAHOO.util.Anim(allChildren[i], attributes);
                anim.animate();
                allChildren[i].style.display = "";
                YAHOO.util.Dom.removeClass(icon, "plus-icon");
                YAHOO.util.Dom.addClass(icon, "minus-icon");
                todoOther = "show";
                parentNode.style.height = "auto";
            }
            else {
                attributes = {
                    opacity: { to: 0 }
                };
                var anim = new YAHOO.util.Anim(allChildren[i], attributes);
                anim.duration = 0.3; 
                anim.onComplete.subscribe(hideTreeItem,allChildren[i]);

                anim.animate();

                YAHOO.util.Dom.removeClass(icon, "minus-icon");
                YAHOO.util.Dom.addClass(icon, "plus-icon");
                todoOther = "hide";
                //parentNode.style.height = "50px";

            }

        }


    }
    for (var i = 0; i < allChildren.length; i++) {
        if (allChildren[i].className == "branch-node") {
            if (todoOther == "hide") {
                attributes = {
                    opacity: { to: 0 }
                };
                anim = new YAHOO.util.Anim(allChildren[i], attributes);
                anim.duration = 0.3;
                anim.onComplete.subscribe(hideTreeItem,allChildren[i]);

                anim.animate();
                //allChildren[i].style.display = "none";
            } else {
                attributes = {
                    opacity: { to: 1 }
                };
                anim = new YAHOO.util.Anim(allChildren[i], attributes);
                anim.animate();
                allChildren[i].style.display = "";
            }
        }
    }
}
function sortNumber(a, b)
{
    return b - a;
}
function generateGraph(arr, objId, rowType) {
    //calculate the bar widths
    var tbody = "";
    var threeItems = new Array();
    var threeItemsSorted = new Array();
 
    if(rowType == "A"){
        bars = parseInt(maxBarWidhA);
        maxWidth = 190;
        YAHOO.util.Dom.setStyle(objId, 'width', (maxWidth + 75) + 'px');
    }else{
        bars = parseInt(maxBarWidhB);
        maxWidth = 250;
        YAHOO.util.Dom.setStyle(objId, 'width', (maxWidth + 75) + 'px');
    }
    var biggest_bar = -1;
    var biggestItem = null;
    var small_items = 0;
    for (var i=0;i<arr.length;i++){         
        var bar = parseInt(arr[i][1]) ;
        var barWidth = parseInt((bar / bars) * maxWidth);
        if (barWidth < 10 && bar >0) {
            if(biggest_bar<bar){
                biggest_bar = bar;
                biggestItem = i;
            }
            small_items++;
        }
    }

    if(threeItems.length > 1){
        threeItemsSorted.push({bar:threeItems[0]},{size:10});
        //threeItemsSorted.push({bar:threeItems[0]},{size:parseInt(10*(threeItems[1])/threeItems[0])});
        if(threeItems.length == 3){
            threeItemsSorted.push({bar:threeItems[0]},{size:parseInt(10*(threeItems[2])/threeItems[0])});
        }
    }

    for (i=0;i<arr.length;i++){
        bar = parseInt(arr[i][1]) ;
        barWidth = parseInt((bar / bars) * maxWidth);


        if (small_items == 1) {
            if (barWidth < 10 && bar > 0) {
                barWidth = 10;
            }
        } else if (small_items > 1) {
            if (barWidth < 10 && bar > 0) {
                if (i == biggestItem) {
            barWidth = 10;
                } else {
                    barWidth = parseInt(10 * (bar / biggest_bar))
        }
            }
        }

        var row = parseInt(i)+1;
        tbody += '<tr><td><div title="'+arr[i][0]+'"><div class="bar'+rowType+row+' bar" style="width:' + barWidth + 'px;"></div><div class="barData">' + arr[i][1] + '</div></div><div class="BarClear"></div></td></tr>';
    }


    var tmpObj = document.createElement('DIV');
    var tableData = '<table cellpadding="0" cellspacing="0" class="noBorders">' +
                    tbody+
                    '</table>';
    tmpObj.innerHTML = tableData;
    YAHOO.util.Event.onAvailable(objId, function() {
        document.getElementById(objId).appendChild(tmpObj);
    });

}

function showHideData(obj, str) {

    var toShow = false;
    if(obj.innerHTML.search(/up.png/g) == -1){
        toShow = true;
    }
    var all = obj.parentNode.childNodes;

    if (toShow) {
        obj.innerHTML = str + '<img src="images/up.png" alt="Hide Data" align="top" hspace="5"/>';
       for(var i=0;i<all.length;i++){
           if(all[i].nodeName == "DIV"){
            jQuery(all[i]).toggle("slow");    
           }
       }
    } else {
        obj.innerHTML = str + '<img src="images/down.png" alt="Show Data" align="top" hspace="5"/>';
        for(var i=0;i<all.length;i++){
           if(all[i].nodeName == "DIV"){
            jQuery(all[i]).toggle("slow");
           }
       }
    }

}
function positionLegendTop() {
    var scrollTop = YAHOO.util.Dom.getDocumentScrollTop();
    if (scrollTop > topPos) {
        YAHOO.util.Dom.setStyle('serverDataLegend', 'top', scrollTop + 'px');
    } else {
        YAHOO.util.Dom.setStyle('serverDataLegend', 'top', topPos + 'px');
    }
    //Make the legend Drabable
    var dd = new YAHOO.util.DD("serverDataLegend");
}
function toggleLegend(obj){
    var legend = document.getElementById('serverDataLegendInside');
    if(legend.style.display=="none"){
        legend.style.display = "";
        obj.innerHTML= 'Hide Legend<img src="images/up.png" alt="Toggle Legend" align="top" hspace="5"/>';
    }else{
        legend.style.display = "none";
        obj.innerHTML= 'Show Legend<img src="images/down.png" alt="Toggle Legend" align="top" hspace="5"/>';
    }
}
function serverDataInit(){
    positionLegendLeft();
    positionLegendTop();
}
function positionLegendLeft() {
    var viweWidth = YAHOO.util.Dom.getViewportWidth();
    YAHOO.util.Dom.setStyle('serverDataLegend', 'left', (viweWidth-643) + 'px');
}