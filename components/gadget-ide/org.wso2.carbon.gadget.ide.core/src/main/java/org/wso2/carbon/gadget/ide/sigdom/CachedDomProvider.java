package org.wso2.carbon.gadget.ide.sigdom;

import javax.xml.transform.dom.DOMSource;
import java.util.LinkedHashMap;
import java.util.Map;

public class CachedDomProvider implements DomProvider{

    private Map<String, DOMSource> cache;
    private DomProvider provider;
    private final int cacheMaxSize;

    public CachedDomProvider(DomProvider provider, int cacheMaxSize) {
        this.cacheMaxSize = cacheMaxSize;
        this.provider = provider;
    }

    private void crateCache() {
        cache = new LinkedHashMap<String, DOMSource>() {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, DOMSource> eldest) {
                return size() > cacheMaxSize;
            }
        };
    }

    private DOMSource recover(String keyURI) {
        if(cache==null){
            return null;
        }
        return cache.get(keyURI);
    }

    private synchronized void admit(String keyURI, DOMSource data) {
        if(cache==null){
            crateCache();
        }
        cache.put(keyURI, data);
    }


    public int getMaxSize() {
        return cacheMaxSize;
    }

    public DOMSource getSigDom(String uri) {
        DOMSource domSource = recover(uri);
        if(domSource==null){
            domSource = provider.getSigDom(uri);
            if(domSource!=null){
                admit(uri,domSource);
            }
        }
        return domSource;
    }
}
