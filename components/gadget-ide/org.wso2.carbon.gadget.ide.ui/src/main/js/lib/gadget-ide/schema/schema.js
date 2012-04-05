/**
 * @fileoverview defines and provide utilities related to DataSource Input and
 * Output Formats (DSSchema).
 *
 * @author rcmperea@gmail.com (Manuranga)
 */
goog.provide('gadgetide.schema');
goog.provide('gadgetide.schema.DSSchema');

goog.require('goog.array');


/**
 * Errors thrown by utility functions in {@code gadgetide.schema}.
 * @enum {string}
 */
gadgetide.schema.Error = {
  NO_ELEMENT_BY_PATH: 'no Element matches a ExpandedName in the path'
};

gadgetide.schema.fixMultipleToArrays = function() {

};


/**
 *
 * DSSchema is a simplified schema for XML like data.
 * schema itself can be characterized by an XML.
 *
 * <pre>
 *
 * example DSSchema:
 *  &lt;foo&gt;
 *     &lt;bar multiple="true"&gt;
 *        &lt;ns1:tar xmlns:ns1="http://schema.wso2.org" &gt;?&lt;/ns1:tar&gt;
 *        &lt;arr multiple="true"&gt;?&lt;/arr&gt;
 *     &lt;/bar&gt;
 *     &lt;lrr multiple="false"&gt;?&lt;/lrr&gt;
 *     &lt;what anytype="true" required="true"&gt;?&lt;/what&gt;
 *  &lt;/foo&gt;
 *
 * instance of the above DDSchema:
 *
 *  &lt;foo&gt;
 *     &lt;bar&gt;
 *        &lt;p:tar xmlns:p="http://schema.wso2.org" &gt;La Brea&lt;/ns1:tar&gt;
 *        &lt;arr&gt;the good&lt;/arr&gt;
 *        &lt;arr&gt;the bad&lt;/arr&gt;
 *        &lt;arr&gt;the ugly&lt;/arr&gt;
 *     &lt;/bar&gt;
 *     &lt;bar&gt;
 *        &lt;ns1:tar xmlns:ns1="http://schema.wso2.org" &gt;
 *            Carpinteria
 *        &lt;/ns1:tar&gt;
 *        &lt;arr&gt;beauty&lt;/arr&gt;
 *        &lt;arr&gt;the beast&lt;/arr&gt;
 *     &lt;/bar&gt;
 *     &lt;lrr&gt;omicron persei 8&lt;/lrr&gt;
 *     &lt;what&gt;
 *        &lt;soul&gt;
 *           &lt;soldier&gt;not&lt;/soldier&gt;
 *        &lt;/soul&gt;
 *     &lt;/what&gt;
 *  &lt;/foo&gt;
 *
 * </pre>
 *
 * here <b>multiple</b> means that, multiple occurrence of the tag may occur in
 * an instance; and <b>anytype</b> means that, children of the tag can have
 * arbitrary structure in an instance.if <b>anytype</b> is defined on an element
 * it must not have any children in the schema.<b>required<b> means this element
 * is required to be be linked to another link for the application to proceed.
 * this is often used with <b>anytype<b>,see
 * {@see gadgetide.datasource.DataSource#loadOutputFormat} for restrictions
 * on <b>required<b>
 *
 * if any of these attributes are missing in an element then they assumed
 * to be false.
 *
 * Instead of using a XML DOM object, equivalent <i>BadgerFish</i> JSON object
 * is used for convenience. but DSSchema is only valid if it's convertible back
 * to XML.( eg: {'a':{},'b':{}} is not valid, since XML can't have
 * two root level nodes)
 *
 * Limitations :
 *
 * DSSchema's are not capable of specifying XML attributes to be expected.
 * nor does it can specify exact upper/lower bounds on Element count
 * (maxOccurrence/minOccurrence)
 *
 *
 * @typedef {!Object}
 */
gadgetide.schema.DSSchema;


/**
 * we will define DataField data type to easily obtain information form
 * the DSSchema object.it will correspond to one XML Element in original XML.
 * here 'name' refers to LocalPart
 * {@see http://www.w3.org/TR/xml-names/#NT-LocalPart}
 *
 * @typedef {{
 *             name:string,
 *             xmlns:?string,
 *             path:!Array.<gadgetide.schema.ExpandedName>,
 *             hasChildren:boolean,
 *             isMultiple:boolean,
 *             isAnyType:boolean,
 *             isRequired:boolean
 *           }}
 */
gadgetide.schema.DataField;


/**
 * This is a tree like recursive data structure in which each child itself
 * is DataFieldTree. And since {@code DataFieldTree} object also agrees to
 * {@code DataField}'s interface, it can be parsed to any method that
 * expects a  {@code DataField}.
 *
 * @typedef {{
 *             name:string,
 *             xmlns:?string,
 *             path:!Array.<gadgetide.schema.ExpandedName>,
 *             hasChildren:boolean,
 *             isMultiple:boolean,
 *             isAnyType:boolean,
 *             isRequired:boolean,
 *             children:!Array.<gadgetide.schema.DataFieldTree>
 *           }}
 */
gadgetide.schema.DataFieldTree;
/**
 *
 * @see http://www.w3.org/TR/xml-names/#dt-expname
 * @typedef {{
 *              namespace:?string,
 *              localpart:string
 *           }}
 */
gadgetide.schema.ExpandedName;


/**
 * search all the immediate children Elements of the {@code dsSchemaFragment}
 * to find Element with matching {@code expandedName}.
 *
 * @private
 * @param {!Object} dsSchemaFragment part of a DSSchema, not necessarily a
 *     valid DSSchema.
 * @param {gadgetide.schema.ExpandedName} expandedName ExpandedName to be
 *     searched.
 * @throws {Error} If there is no matching Element.
 * @return {gadgetide.schema.DSSchema} this is a fragment to the
 *     original DSSchema, which also happens to be a valid DSSchema.
 */
gadgetide.schema.searchElementByExpandedName_ =
  function(expandedName, dsSchemaFragment) {

    if (expandedName.namespace) {
      for (var elementQName in dsSchemaFragment) {
        var elementExpandedName = gadgetide.schema.getExpandedName_(
          dsSchemaFragment[elementQName],
          elementQName
        );
        if (elementExpandedName.namespace === expandedName.namespace &&
          elementExpandedName.localpart === expandedName.localpart) {
          return dsSchemaFragment[elementQName];
        }
      }
    } else {
      return dsSchemaFragment[expandedName.localpart];
    }
    throw new Error(gadgetide.schema.Error.NO_ELEMENT_BY_PATH);
  };

/**
 *
 * @private
 * @param {!Object} dsSchemaFragment part of a DSSchema, not necessarily a
 *     valid DSSchema.
 * @param {gadgetide.schema.ExpandedName} expandedName ExpandedName to be
 *     searched.
 * @throws {Error} If there is no matching Element.
 * @return {string}
 */
gadgetide.schema.findPrefixedNameByExpandedName_ =
  function(dsSchemaFragment, expandedName) {

    if (expandedName.namespace) {
      for (var elementQName in dsSchemaFragment) {
        var elementExpandedName = gadgetide.schema.getExpandedName_(
          dsSchemaFragment[elementQName],
          elementQName
        );
        if (elementExpandedName.namespace === expandedName.namespace &&
          elementExpandedName.localpart === expandedName.localpart) {
          return elementQName;
        }
      }
    } else {
      return expandedName.localpart;
    }
    throw new Error(gadgetide.schema.Error.NO_ELEMENT_BY_PATH);
  };


/**
 *
 * @param {gadgetide.schema.DataField} dataField
 * @param {!Object} data
 * @return {{data:(!Object|!Array),inArray:boolean}}
 */
gadgetide.schema.filterPath = function(dataField, data) {
  var obj = data;
  var inArray = false;
  for (var i = 0; i < dataField.path.length; i++) {
    var pathPart = dataField.path[i];
    if (goog.isArray(obj)) {
      inArray = true;
      obj = goog.array.map(
        obj,
        goog.partial(gadgetide.schema.searchElementByExpandedName_, pathPart)
      );
    } else {
      obj = gadgetide.schema.searchElementByExpandedName_(pathPart, obj);
    }
  }
  return {data:obj,inArray:inArray};
};

/**
 *
 * @param {!Object|!Array} data
 * @param {boolean=} inArray
 * @return {*}
 */
gadgetide.schema.getValue = function(data, inArray) {
  if (goog.isArray(data)) {
    if (inArray) {
      return goog.array.map(data,gadgetide.schema.getValue);
    } else {
      return data;
    }
  } else {
    //TODO: properly handle any Type case
    return data['$'] || data;
  }
};

/**
 * @param {!Object} data
 * @param {string} key
 * @param {*} value
 */
gadgetide.schema.setValue = function(data, key, value) {
  if(!data.hasOwnProperty(key)){
    data[key] = {};
  }
  if (goog.isArray(value)) {
    var xmlns = data[key]['@xmlns'];
    data[key] = value;
    for (var i = 0; i < value.length; i++) {
      var valueItem = value[i];
      valueItem['@xmlns'] = xmlns;
    }
  } else {
    if(!data.hasOwnProperty(key)){
       data[key] = {'$':value};
    }else{
       data[key]['$'] = value;
    }

  }
};

/**
 *
 * @param {gadgetide.schema.DataField} dataField
 * @param {!Object} data
 * @return {{key:string,data:!Object}}
 */
gadgetide.schema.makePath = function(dataField, data) {
  var prefixes = {};
  var prefixesTrns = {};
  var prefixesCount = 0;
  var obj = data;
  var name;
  /** @type {!Object} */
  var objOld;
  for (var i = 0; i < dataField.path.length; i++) {
    var pathPart = dataField.path[i];
    var subObj;
    try {
      name = gadgetide.schema.findPrefixedNameByExpandedName_(obj, pathPart);
      if(obj[name]){
          objOld = obj;
          obj = obj[name]; //going deep on step in the json object.
      }
      else{
          name = pathPart.localpart;
          if (pathPart.namespace) {
            var prefix = prefixes[pathPart.namespace];
            if (!prefix) {
              prefix = 'ns' + ++prefixesCount;
              prefixes[pathPart.namespace] = prefix;
              prefixesTrns[prefix] = pathPart.namespace;
            }
            name = prefix + ":" + name;
          }
          obj[name] = {'@xmlns':prefixesTrns};
          objOld = obj;
          obj = obj[name];
      }

    } catch(ex) /*thrown when the path does not already exist.*/{
      //TODO: use the original prefix
      name = pathPart.localpart;
      if (pathPart.namespace) {
        var prefix = prefixes[pathPart.namespace];
        if (!prefix) {
          prefix = 'ns' + ++prefixesCount;
          prefixes[pathPart.namespace] = prefix;
          prefixesTrns[prefix] = pathPart.namespace;
        }
        name = prefix + ":" + name;
      }
      obj[name] = {'@xmlns':prefixesTrns};
      objOld = obj;
      obj = obj[name];
    }
  }
  //TODO: verify the integrity of the following cast
  return /** @type {{data:!Object,key:string}} */ {key:name,data:objOld};
};

/**
 *
 * if QName only has a local part and there is no default namespace then the
 * namespace of the returned {@code ExpandedName} will be  null
 *
 * @private
 * @param {!Object} dsSchemaFragment DSSchema fragment that will be used to
 *     lookup namespace URIs.
 * @param {string} qName {@see http://www.w3.org/TR/xml-names/#NT-QName}.
 * @return {gadgetide.schema.ExpandedName} ExpandedName of the fragment.
 */
gadgetide.schema.getExpandedName_ = function(dsSchemaFragment, qName) {
  var nameParts = qName.split(':');
  var namespaces = dsSchemaFragment['@xmlns'] || dsSchemaFragment[0] && dsSchemaFragment[0]['@xmlns'];
  /** @type {string} */
  var localPart;
  /** @type {?string} */
  var namespace;

  //dose it has a prefix part
  if (nameParts.length > 1) {
    localPart = nameParts[1];
    namespace = namespaces[nameParts[0]];
  } else {
    localPart = qName;
    //dose it has a default namespace
    if (goog.isDef(namespaces) && goog.isDef(namespaces['$'])) {
      namespace = namespaces['$'];
    } else {
      namespace = null;
    }
  }
  return {localpart: localPart, namespace: namespace};
};


/**
 * parse an entry of the {@code DSSchema} to {@code DataField} Object
 *
 * @param {gadgetide.schema.DSSchema} dsSchema DSSchema JSON object.
 * @param {Array.<gadgetide.schema.ExpandedName>=} opt_path Path to the entry
 *     to be parsed.if not provided one of the entry at root level will
 *     be parsed.If the root level has more than one entry, it's better not to
 *     leave this argument empty.
 * @return {gadgetide.schema.DataField} Parsed DataField.
 */
gadgetide.schema.parseDataField = function(dsSchema, opt_path) {


  /**
   * element to be parsed
   * @type {Object}
   */
  var dsSchemaElement;

  /** @type {!Array.<gadgetide.schema.ExpandedName>} */
  var path;

  if (goog.isDefAndNotNull(opt_path)) {
    path = opt_path;

    dsSchemaElement = dsSchema;
    //travel to the entry we want to parse
    for (var i = 0; i < opt_path.length; i++) {
      /** @type {gadgetide.schema.ExpandedName} */
      var pathFragment = opt_path[i];
      dsSchemaElement = gadgetide.schema.searchElementByExpandedName_(
        pathFragment,
        dsSchemaElement
      );
    }
  } else {
    /** @type {string} */
    var qName;

    /* selecting a random element.no need for hasOwnProperty since dsSchema
     * is a JSON.this loop is suppose to be iterated only ones, at most.
     */
    for (var entryKey in dsSchema) {
      qName = entryKey;
      dsSchemaElement = dsSchema[entryKey];
      break;
    }

    path = [gadgetide.schema.getExpandedName_(dsSchemaElement, qName)];
  }

  /**
   * Even the DSSchema has some children that doesn't mean the
   * original XML element has any children,because attributes
   * and namespaces will become chaldean.
   * the text child should be ignored too.
   * @type {boolean}
   */
  var hasChildren = false;
  for (var childKey in dsSchemaElement) {
    // does not (starts with '@' OR is '$')
    if (childKey.search(/^@|^\$$/) === -1) {
      hasChildren = true;
      break;
    }
  }

  var lastPathFragment = path[path.length - 1];
  return {
    name: lastPathFragment.localpart,
    xmlns: lastPathFragment.namespace,
    path: path,
    hasChildren: hasChildren,
    isMultiple: dsSchemaElement['@multiple'] === 'true',
    isAnyType: dsSchemaElement['@anytype'] === 'true',
    isRequired: dsSchemaElement['@required'] === 'true'
  };
};

/**
 * parse a whole {@code DSSchema} to {@code DataFieldTree} Object
 *
 * @param {gadgetide.schema.DSSchema} dsSchema DSSchema JSON object.
 * @param {Array.<gadgetide.schema.ExpandedName>=} opt_path Path to the root of
 *     the sub-tree to be parsed.if not provided one of the entry at root level
 *     will be parsed.If the root level has more than one entry, it's better
 *     not to leave this argument empty.
 * @return {gadgetide.schema.DataFieldTree} Parsed DataField.
 */
gadgetide.schema.parseDataFieldTree = function(dsSchema, opt_path) {
  var rootDataField = gadgetide.schema.parseDataField(dsSchema, opt_path);

  var populateChildren = function(dataField) {
    dataField.children = [];

    gadgetide.schema.forEachChild(
      dsSchema,
      dataField.path,
      function(childDataField) {
        populateChildren(childDataField);
        dataField.children.push(childDataField);
      }
    );
  };

  populateChildren(rootDataField);

  /* since we injected a {@code children} property, it is possible to
   * cast this {@code DataField} object to {@code DataFieldTree} object
   * without any run time errors.
   * this fact is true for each of it's children as well.
   */
  var dataFieldTree =
  /** @type {gadgetide.schema.DataFieldTree}*/rootDataField;

  return dataFieldTree;
};

/**
 *
 * @param {gadgetide.schema.DSSchema} dsSchema DSSchema JSON object.
 * @param {!Array.<gadgetide.schema.ExpandedName>} path Path to the entry to
 *     be iterated.
 * @param {function(gadgetide.schema.DataField)} loopBody The function to be
 *     called repeatedly with each child, parsed as a DataField Object.
 * @return {void} Nothing.
 */
gadgetide.schema.forEachChild = function(dsSchema, path, loopBody) {
  var dsSchemaElement = dsSchema;

  //travel to the entry we want to parse
  for (var i = 0; i < path.length; i++) {
    var pathFragment = path[i];
    dsSchemaElement = gadgetide.schema.searchElementByExpandedName_(
      pathFragment,
      dsSchemaElement
    );
  }

  for (var childName in dsSchemaElement) {
    // not (starts with '@' OR is '$')
    if (childName.search(/^@|^\$$/) === -1) {
      var childFieldPath = goog.array.concat(
        path,
        gadgetide.schema.getExpandedName_(
          dsSchemaElement[childName],
          childName
        )
      );
      loopBody(gadgetide.schema.parseDataField(
        dsSchema,
        childFieldPath
      ));
    }
  }
};

