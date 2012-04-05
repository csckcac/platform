/*
 * Copyright 2007 WSO2, Inc. http://www.wso2.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

function enableTooltips(id) {
    var links,i,h;
    if (!document.getElementById || !document.getElementsByTagName) return;
    h = document.createElement("span");
    h.id = "btc";
    h.setAttribute("id", "btc");
    h.style.position = "absolute";
    document.getElementsByTagName("body")[0].appendChild(h);
    if (id == null) links = document.getElementsByTagName("a");
    else links = document.getElementById(id).getElementsByTagName("a");
    for (i = 0; i < links.length; i++) {
        Prepare(links[i]);
    }
}

function Prepare(el) {
    var tooltip,t,b,s;
    t = el.getAttribute("title");
    if (!(t == null || t.length == 0)) {
        el.removeAttribute("title");
        tooltip = CreateEl("span", "tooltip");
        s = CreateEl("span", "top");
        s.appendChild(document.createTextNode(t));
        tooltip.appendChild(s);
        b = CreateEl("b", "bottom");
        tooltip.appendChild(b);
        setOpacity(tooltip);
        el.tooltip = tooltip;
        el.onmouseover = showTooltip;
        el.onmouseout = hideTooltip;
        el.onmousemove = Locate;
    }
}

function forceShowTooltip(el) {
    document.getElementById("btc").appendChild(el.tooltip);
    var pos = findPos(el);
    document.getElementById("btc").style.top = (pos[1] + 10) + "px";
    document.getElementById("btc").style.left = (pos[0] - 20) + "px";

    //Making sure the tooltip is cleared after a while
    window.setTimeout("hideTooltip();", 10 * 1000);
}

function showTooltip(e) {
    var el = document.getElementById("btc");
    el.innerHTML = "";
    el.appendChild(this.tooltip);
    Locate(e);
}

function hideTooltip() {
    var d = document.getElementById("btc");
    if (d.childNodes.length > 0) d.removeChild(d.firstChild);
}

function setOpacity(el) {
    el.style.filter = "alpha(opacity:95)";
    el.style.KHTMLOpacity = "0.95";
    el.style.MozOpacity = "0.95";
    el.style.opacity = "0.95";
}

function CreateEl(t, c) {
    var x = document.createElement(t);
    x.className = c;
    x.style.display = "block";
    return(x);
}

function Locate(e) {
    var posx = 0,posy = 0;
    if (e == null) e = window.event;
    if (e.pageX || e.pageY) {
        posx = e.pageX;
        posy = e.pageY;
    }
    else if (e.clientX || e.clientY) {
        if (document.documentElement.scrollTop) {
            posx = e.clientX + document.documentElement.scrollLeft;
            posy = e.clientY + document.documentElement.scrollTop;
        }
        else {
            posx = e.clientX + document.body.scrollLeft;
            posy = e.clientY + document.body.scrollTop;
        }
    }
    document.getElementById("btc").style.top = (posy + 10) + "px";
    document.getElementById("btc").style.left = (posx - 20) + "px";
}

function findPos(obj) {
    var curleft = 0;
    var curtop = 0;
    if (obj.offsetParent) {
        do {
            curleft += obj.offsetLeft;
            curtop += obj.offsetTop;
        } while (obj = obj.offsetParent);
    }

    return [curleft,curtop];
}        
