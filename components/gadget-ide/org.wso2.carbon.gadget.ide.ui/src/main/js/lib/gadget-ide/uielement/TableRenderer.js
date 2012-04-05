goog.provide('gadgetide.uielement.TableRenderer');

goog.require('gadgetide.uielement.UIElementRenderer');
goog.require('goog.ui.registry');


/**
 * @constructor
 * @extends {gadgetide.uielement.UIElementRenderer}
 */
gadgetide.uielement.TableRenderer = function() {
  gadgetide.uielement.UIElementRenderer.call(this);
};
goog.inherits(gadgetide.uielement.TableRenderer,
  gadgetide.uielement.UIElementRenderer);
goog.addSingletonGetter(gadgetide.uielement.TableRenderer);

gadgetide.uielement.TableRenderer.CSS_CLASS = 'gc-table';

/**
 * @inheritDoc
 */
gadgetide.uielement.TableRenderer.prototype.createDom = function(control) {
  return control.getDomHelper().createDom('div',
    this.getClassNames(control).join(' '));
};

/**
 * @inheritDoc
 */
gadgetide.uielement.TableRenderer.prototype.getCssClass = function(control) {
  return gadgetide.uielement.TableRenderer.CSS_CLASS;
};

/**
 * @inheritDoc
 */
gadgetide.uielement.TableRenderer.prototype.canDecorate = function(element) {
  return element.tagName === 'DIV';
};

/**
 * @inheritDoc
 */
gadgetide.uielement.TableRenderer.prototype.updateUiData = function(uiElm, input) {
  var j;
  if (input && goog.isArray(input['rows'])) {
    var rows = input['rows'];
    if (rows.length > 0) {

      var dom = uiElm.getDomHelper();
      var columnHeaders = [];
      for (var colHead in rows[0]) {
        if (!goog.string.startsWith(colHead, '@')) {
          columnHeaders.push(colHead);
        }
      }

      var tbody,thead;
      var table = dom.createDom('table', undefined,
        dom.createDom('thead',undefined,
          thead = dom.createDom('tr')
        ),
        tbody = dom.createDom('tbody')
      );
      for (j = 0; j < columnHeaders.length; j++) {
        var headCellDom = dom.createDom('th', undefined, columnHeaders[j]);
        thead.appendChild(headCellDom);
      }

      for (var i = 0; i < rows.length; i++) {
        var row = rows[i];
        var rowDom = dom.createDom('tr');
        tbody.appendChild(rowDom);
        for (j = 0; j < columnHeaders.length; j++) {
          var colHeadName = columnHeaders[j];
          var cell = row[colHeadName];
          var val = cell ? cell['$'] : '{N/A}';
          var cellDom = dom.createDom('td', undefined, val);
          rowDom.appendChild(cellDom);
        }
      }

      var el = uiElm.getElement();
      if (el.firstChild) {
        el.removeChild(el.firstChild);
      }
      el.appendChild(table);
    }
  }
};

