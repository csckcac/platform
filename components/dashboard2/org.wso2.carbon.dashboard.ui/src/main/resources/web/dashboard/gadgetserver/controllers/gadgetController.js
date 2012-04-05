wso2.ui.dashboard.GadgetController = new function() {

    this.getGadget = function(gadgetId) {
    }

    this.addGadget = function(gadget) {
        wso2.ui.dashboard.util.makeRequest(config.gadgetServlet, '', '');
    }

    this.deleteGadget = function(gadget) {
    }

    this.getGadgetsForTab = function(tabId, callback) {
        var content = 'tabId=' + tabId + '&op=get_gadgets_for_tab';
        wso2.ui.dashboard.Util.makeRequest(config.gadgetServlet, content, callback);
    }

    this.renderGadgets = function(gadgets, optCallback) {

    };
}

