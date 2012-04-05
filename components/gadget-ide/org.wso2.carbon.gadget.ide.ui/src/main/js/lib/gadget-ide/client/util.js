goog.provide('gadgetide.client.util');

goog.require('goog.json');


/**
 *
 * @param {!Object} json
 */
gadgetide.client.util.json2XML = function (json) {
    var xml = [],
        str;
    function startTag(tag){
        xml.push('<');
        xml.push(tag);
        xml.push('>');
    }
    function endTag(tag){
        xml.push('</');
        xml.push(tag);
        xml.push('>');
    }
    function addNode(o){
          for (var key in o) {
            var value = o[key];
            if(goog.isArray(value)){
                for (var j = 0; j < value.length; j++) {
                    addNonArrayNode(key,value[j]);
                }
            }else{
                addNonArrayNode(key,value);
            }
        }
    }

    function addNonArrayNode(key,value){
        startTag(key);
        if(value===null){
            xml.push("null");
        }else if(typeof value==="object"){
            addNode(value);
        }else{
            str = String(value);
            if(typeof value==="function"){
                str = str.replace(/\s*\n\s*/g," ");
            }
            if(str.indexOf("<")>=0){
                str = '<![CDATA[' + str + "]]>";
            }
            xml.push(str);
        }
        endTag(key);
    }
    addNode(json);
    return xml.join('');
};

/**
 * for legacy/versioning reasons google is not using the native json parser.
 * but we can use it for additional speed gain.
 * @const
 * @type {function(string):!Object}
 */
gadgetide.client.util.JSON_PARSE = (goog.global['JSON'] &&
  goog.global['JSON']['parse']) || goog.json.parse;

/**
 * @const
 * @type {function(Object):string}
 */
gadgetide.client.util.JSON_SERIALIZE = (goog.global['JSON'] &&
  goog.global['JSON']['stringify']) || goog.json.serialize;

/**
 * @see http://ruchirawageesha.blogspot.com/2011/01/xml-to-badgerfish-converter-in.html
 * @param {Document|Element} node
 */
gadgetide.client.util.xml2bf = function (node) {
    var json = {};
    var cloneNS = function(ns) {
        var nns = {};
        for (var n in ns) {
            if (ns.hasOwnProperty(n)) {
                nns[n] = ns[n];
            }
        }
        return nns;
    };
    var process = function (node, obj, ns) {
        if (node.nodeType === 3) {
            if (!node.nodeValue.match(/[\S]+/)) return;
            if (obj["$"] instanceof Array) {
                obj["$"].push(node.nodeValue);
            } else if (obj["$"] instanceof Object) {
                obj["$"] = [obj["$"], node.nodeValue];
            } else {
                obj["$"] = node.nodeValue;
            }
        } else if (node.nodeType === 1) {
            var p = {};
            var nodeName = node.nodeName;
            for (var i = 0; node.attributes && i < node.attributes.length; i++) {
                var attr = node.attributes[i];
                var name = attr.nodeName;
                var value = attr.nodeValue;
                if (name === "xmlns") {
                    ns["$"] = value;
                } else if (name.indexOf("xmlns:") === 0) {
                    ns[name.substr(name.indexOf(":") + 1)] = value;
                } else {
                    p["@" + name] = value;
                }
            }
            for (var prefix in ns) {
                if (ns.hasOwnProperty(prefix)) {
                    p["@xmlns"] = p["@xmlns"] || {};
                    p["@xmlns"][prefix] = ns[prefix];
                }
            }
            if (obj[nodeName] instanceof Array) {
                obj[nodeName].push(p);
            } else if (obj[nodeName] instanceof Object) {
                obj[nodeName] = [obj[nodeName], p];
            } else {
                obj[nodeName] = p;
            }
            for (var j = 0; j < node.childNodes.length; j++) {
                process(node.childNodes[j], p, cloneNS(ns));
            }
        } else if (node.nodeType === 9) {
            for (var k = 0; k < node.childNodes.length; k++) {
                process(node.childNodes[k], obj, cloneNS(ns));
            }
        }
    };
    process(node, json, {});
    return json;
}
