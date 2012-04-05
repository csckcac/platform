var registryOps = new function() {

    this.registry = new Registry('SYSTEM_CONFIGURATION');

    this.put = function() {
        var resource = this.registry.newResource();
        resource.content = 'Gadget-1';
        resource.addProperty('gadgetUrl', 'http://nuwanbando.com/ig/soa.xml');
        resource.addProperty("company", "WSO2 Inc.");
        this.registry.put('jssp/gadgets/clock-gadget', resource);
    };

    this.get = function(){
         return this.registry.get("jssp/gadgets/clock-gadget").getProperty('gadgetUrl').replace(" ", "");
    };
}
