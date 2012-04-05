/*
 * Copyright 2006,2007 WSO2, Inc. http://www.wso2.org
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
package org.wso2.carbon.hostobjects.atom;

import org.apache.abdera.Abdera;
import org.apache.abdera.factory.Factory;
import org.apache.abdera.i18n.iri.IRI;
import org.apache.abdera.i18n.iri.IRISyntaxException;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Link;
import org.apache.abdera.parser.stax.util.FOMHelper;
import org.apache.abdera.protocol.Response;
import org.apache.abdera.protocol.client.AbderaClient;
import org.apache.abdera.protocol.client.ClientResponse;
import org.apache.abdera.protocol.client.RequestOptions;
import org.apache.abdera.protocol.client.util.BaseRequestEntity;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.wso2.carbon.scriptengine.exceptions.ScriptException;
import org.wso2.javascript.xmlimpl.XML;

import javax.activation.MimeTypeParseException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * A Mozilla Rhino based javascript host object to act as a client to an APP
 * server as defined in the <a
 * href="http://www.ietf.org/internet-drafts/draft-ietf-atompub-protocol-11.txt">APP
 * specification</a>.
 */
public class APPClientHostObject extends ScriptableObject {

    private static final long serialVersionUID = 3210576127148634481L;

    private Abdera abdera;

    private String usernameString;

    private String passwordString;

    private String authenticationType = "basic";

    private String serviceString;

    private NativeObject nativeCredentials;

    private RequestOptions options;

    private AbderaClient client;

    public APPClientHostObject() {
        super();
        abdera = new Abdera();
        client = new AbderaClient(abdera);
        options = client.getDefaultRequestOptions();
    }

    public void jsConstructor() {
    }

    public String getClassName() {
        return "APPClient";
    }

    /**
     * <p/>
     * This property can be used to provide a JavaScript object with the
     * username,password and authentication service credentials needed to communicate with
     * APPServer. <i>Currently this supports blogger authentication only</i>.
     * </p>
     * <p/>
     * <pre>
     * eg:
     * client.credentials={username:&quot;you@email.com&quot;,password:&quot;xxx&quot;,service:&quot;blogger&quot;,authtype:&quot;google&quot;};
     * </p>
     *
     * @param object
     */
    public void jsSet_credentials(Object object) throws ScriptException {
        if (object instanceof NativeObject) {
            nativeCredentials = (NativeObject) object;

            Object username = ScriptableObject.getProperty(nativeCredentials, "username");
            if (username instanceof String) {
                usernameString = (String) username;
            } else {
                throw new ScriptException("Username field needs to be a String.");
            }
            Object password = ScriptableObject.getProperty(nativeCredentials, "password");
            if (password instanceof String) {
                passwordString = (String) password;
            } else {
                throw new ScriptException("Password field needs to be a String.");
            }
            Object service = ScriptableObject.getProperty(nativeCredentials, "service");
            if (service instanceof String) {
                serviceString = (String) service;
            } else {
                throw new ScriptException("Service field needs to be a String.");
            }
            Object authType = ScriptableObject.getProperty(nativeCredentials, "authtype");
            if (authType instanceof String) {
                authenticationType = (String) authType;
            } else {
                throw new ScriptException("Authnetication Type field needs to be a String.");
            }
        } else {
            throw new ScriptException("Invalid parameter");
        }
    }

    /**
     * @return the credentials object or null
     */
    public Scriptable jsGet_credentials() {
        return nativeCredentials;
    }

    /**
     * Not implemented yet.
     *
     * @param object
     */
    public void jsSet_options(Object object) {
        //  options.set
    }

    /**
     * <p/>
     * Get an AtomEntry from the APPServer. Expects an Atom entry URI or an
     * AtomEntry object as the arguments.
     * </p>
     * <p/>
     * get(string AtomEntryURI|AtomEntry entryObject);
     * <p/>
     * <pre>
     * var entry2 = client.get(&quot;http://www.blogger.com/feeds/000/posts/full/000&quot;);
     * </pre>
     *
     * @return AtomEntry
     * @throws ScriptException
     */
    public static Scriptable jsFunction_get(Context cx, Scriptable thisObj, Object[] arguments,
                                            Function funObj) throws ScriptException {
        APPClientHostObject hostObject = (APPClientHostObject) thisObj;
        if (arguments.length == 0) {
            throw new ScriptException("Invalid parameter");
        }

        if (arguments[0] instanceof String) {

            ClientResponse clientResponse = hostObject.client.get((String) arguments[0],
                                                                  hostObject.options);
            Scriptable entryHostObject = cx.newObject(hostObject, "AtomEntry", new Object[0]);
            ((AtomEntryHostObject) entryHostObject).setEntry((Entry) clientResponse.getDocument()
                    .getRoot());
            return entryHostObject;

        } else if (arguments[0] instanceof AtomEntryHostObject) {
            AtomEntryHostObject atomEntryHostObject = (AtomEntryHostObject) arguments[0];
            Entry entry = atomEntryHostObject.getEntry();
            if (entry != null) {
                IRI editUri = null;
                try {
                    editUri = getEditURI(entry);
                    ClientResponse clientResponse = hostObject.client.get(editUri.toString());
                    Scriptable entryHostObject = cx.newObject(hostObject, "AtomEntry",
                                                              new Object[0]);
                    ((AtomEntryHostObject) entryHostObject).setEntry((Entry) clientResponse
                            .getDocument().getRoot());
                    return entryHostObject;
                } catch (IRISyntaxException e) {
                    throw new ScriptException(e);
                }
            }
        }
        return null;
    }


    /**
     * <p/>
     * Get an AtomFeed from the APPServer. Expects an Atom feed URI or an
     * AtomFeed object as the arguments.
     * </p>
     * <p/>
     * getFeed(string AtomFeedURI|AtomFeed feedObject);
     * <p/>
     * <pre>
     * var feed = client.getFeed(&quot;http://www.blogger.com/feeds/000/posts/full/000&quot;);
     * </pre>
     *
     * @return AtomFeed
     * @throws ScriptException
     */
    synchronized public static Scriptable jsFunction_getFeed(Context cx, Scriptable thisObj,
                                                             Object[] arguments,
                                                             Function funObj) throws ScriptException {
        APPClientHostObject hostObject = (APPClientHostObject) thisObj;
        if (arguments.length == 0) {
            throw new ScriptException("Invalid parameter");
        }

        if (arguments[0] instanceof String) {


            Feed feed = null;
            try {
                URL url = new URL((String) arguments[0]);
                feed = (Feed) Abdera.getNewParser().parse(url.openStream()).getRoot();
            } catch (IRISyntaxException e) {
                throw new ScriptException(e);
            } catch (MalformedURLException e) {
                throw new ScriptException(e);
            } catch (IOException e) {
                throw new ScriptException(e);
            }

            Scriptable feedHostObject = cx.newObject(hostObject, "AtomFeed", new Object[0]);
            ((AtomFeedHostObject) feedHostObject).setFeed(feed);
            return feedHostObject;

        } else if (arguments[0] instanceof AtomFeedHostObject) {
            AtomFeedHostObject atomFeedHostObject = (AtomFeedHostObject) arguments[0];
            Feed feed = atomFeedHostObject.getFeed();
            if (feed != null) {
                IRI editUri = null;
                try {
                    editUri = getEditURI(feed);
                    ClientResponse clientResponse = hostObject.client.get(editUri.toString());
                    Scriptable feedHostObject = cx.newObject(hostObject, "AtomFeed",
                                                             new Object[0]);
                    ((AtomFeedHostObject) feedHostObject).setFeed((Feed) clientResponse
                            .getDocument().getRoot());
                    return feedHostObject;
                } catch (IRISyntaxException e) {
                    throw new ScriptException(e);
                } catch (MimeTypeParseException e) {
                    throw new ScriptException(e);
                }
            }
        }
        return null;
    }


    /**
     * TODO: Do we really need this...?? (Thilina)
     */
    public static Scriptable jsFunction_getForEdit(Context cx, Scriptable thisObj,
                                                   Object[] arguments, Function funObj)
            throws ScriptException {
        APPClientHostObject hostObject = (APPClientHostObject) thisObj;
        if (arguments.length == 0) {
            throw new ScriptException("Invalid parameter");
        }

        if (arguments[0] instanceof AtomEntryHostObject) {
            AtomEntryHostObject atomEntryHostObject = (AtomEntryHostObject) arguments[0];
            Entry entry = atomEntryHostObject.getEntry();
            if (entry != null) {
                IRI editUri;
                try {
                    editUri = getEditURI(entry);
                    ClientResponse clientResponse = hostObject.client.get(editUri.toString());
                    Scriptable entryHostObject = cx.newObject(hostObject, "AtomEntry",
                                                              new Object[0]);
                    ((AtomEntryHostObject) entryHostObject).setEntry((Entry) clientResponse
                            .getDocument().getRoot());
                    return entryHostObject;
                } catch (IRISyntaxException e) {
                    throw new ScriptException(e);
                }
            }
            return null;
        }
        return null;
    }

    private static IRI getEditURI(Entry entry) throws ScriptException {
        IRI editUri = null;
        List editLinks = entry.getLinks("edit");
        for (Iterator iter = editLinks.iterator(); iter.hasNext();) {
            Link link = (Link) iter.next();
            // if there is more than one edit link, we should not automatically
            // assume that it's always going to point to an Atom document
            // representation.
            if (link.getMimeType() != null) {
                try {
                    if (link.getMimeType().match("application/atom+xml")) {
                        editUri = link.getResolvedHref();
                        break;
                    }
                } catch (MimeTypeParseException e) {
                    throw new ScriptException(e);
                }
            } else { // assume that an edit link with no type
                // attribute is the right one to use
                editUri = link.getResolvedHref();
                break;
            }
        }
        return editUri;
    }


    private static IRI getEditURI(Feed feed) throws MimeTypeParseException, IRISyntaxException {
        IRI editUri = null;
        List editLinks = feed.getLinks("edit");
        for (Iterator iter = editLinks.iterator(); iter.hasNext();) {
            Link link = (Link) iter.next();
            // if there is more than one edit link, we should not automatically
            // assume that it's always going to point to an Atom document
            // representation.
            if (link.getMimeType() != null) {
                if (link.getMimeType().match("application/atom+xml")) {
                    editUri = link.getResolvedHref();
                    break;
                }
            } else { // assume that an edit link with no type
                // attribute is the right one to use
                editUri = link.getResolvedHref();
                break;
            }
        }
        return editUri;
    }


    /**
     * <p/>
     * Posts an AtomEntry object in to an APP server using the given post URI as mentioned in the APP specification.
     * </p>
     * <p/>
     * post(string postURI, {AtomEntry entry|object entry});
     * </p>
     * <p/>
     * Entry to be posted can be given as a AtomEntry object as well as a
     * javascript object with following properties defined.
     * </p>
     * <ul>
     * <li>string author|string authors (comma separated list)</li>
     * <li>string category|string categories(comma separated list)</li>
     * <li>XML content|string content</li>
     * <li>string contributor|string contributors(comma separated list)</li>
     * <li>string id</li>
     * <li>string link|string links(comma separated list)</li>
     * <li>string published : can put the value as'now' to specify the current
     * time</li>
     * <li>XML rights|string rights</li>
     * <li>XML summary | string summary</li>
     * <li>XML title | string title</li>
     * <li>string updated : can put the value as'now' to specify the current
     * time</li>
     * </ul>
     *
     * @return posted AtomEntry
     * @throws IOException
     */
    public static Scriptable jsFunction_post(Context cx, Scriptable thisObj, Object[] arguments,
                                             Function funObj) throws ScriptException {
        APPClientHostObject hostObject = (APPClientHostObject) thisObj;
        Entry entry;
        if (arguments.length != 2) {
            throw new ScriptException("Invalid Number of Parameters");
        }
        if (arguments[1] instanceof AtomEntryHostObject) {
            AtomEntryHostObject atomEntryHostObject = (AtomEntryHostObject) arguments[1];
            entry = atomEntryHostObject.getEntry();
            Scriptable entryHostObject = post(cx, arguments, hostObject, entry);
            return entryHostObject;
        } else if (arguments[1] instanceof NativeObject) {
            try {
                entry = hostObject.createEntry((NativeObject) arguments[1]);

                Scriptable entryHostObject = post(cx, arguments, hostObject, entry);
                return entryHostObject;
            } catch (IRISyntaxException e) {
                throw new ScriptException(e);
            }
        } else {
            throw new ScriptException("Invalid parameter");
        }
    }

    /**
     * Do the actual heavy lifting of posting of an AtomEntry
     *
     * @param cx         - Rhino Context
     * @param arguments  - Should contain the string URI as the first argument
     * @param hostObject - This object
     * @param entry      - Entry to be posted
     * @return the posted entry returned by the server
     * @throws ScriptException
     */
    private static Scriptable post(Context cx, Object[] arguments, APPClientHostObject hostObject,
                                   Entry entry) throws ScriptException {

        performAuthentication(hostObject, (String) arguments[0]);
        Response response = hostObject.client.post((String) arguments[0],
                                                   new BaseRequestEntity(entry, false),
                                                   hostObject.options);

        // Check the response.
        if (response.getStatus() != 201) {
            throw new ScriptException("Posting Failed." + response.getStatusText());
        }

        Scriptable entryHostObject = cx.newObject(hostObject, "AtomEntry", new Object[0]);
        ((AtomEntryHostObject) entryHostObject).setEntry(entry);

        return entryHostObject;
    }


    /**
     * <p/>
     * Puts an AtomEntry object in to a APP server using the given edit URI as mentioned in the APP specification.
     * </p>
     * <p/>
     * put(optional string editURI, {AtomEntry entry|object entry});
     * </p>
     * <p/>
     * Entry to be put can be given as a AtomEntry object as well as a
     * javascript object with following properties defined.
     * </p>
     * <ul>
     * <li>string author|string authors (comma separated list)</li>
     * <li>string category|string categories(comma separated list)</li>
     * <li>XML content|string content</li>
     * <li>string contributor|string contributors(comma separated list)</li>
     * <li>string id</li>
     * <li>string link|string links(comma separated list)</li>
     * <li>string published : can put the value as'now' to specify the current
     * time</li>
     * <li>XML rights|string rights</li>
     * <li>XML summary | string summary</li>
     * <li>XML title | string title</li>
     * <li>string updated : can put the value as'now' to specify the current
     * time</li>
     * </ul>
     */
    public static void jsFunction_put(Context cx, Scriptable thisObj, Object[] arguments,
                                      Function funObj)
            throws ScriptException {
        APPClientHostObject hostObject = (APPClientHostObject) thisObj;
        Entry entry;
        String url;
        if (arguments.length == 2) {
            if (arguments[1] instanceof NativeObject) {
                try {
                    if (arguments[1] instanceof AtomEntryHostObject) {
                        AtomEntryHostObject atomEntryHostObject =
                                (AtomEntryHostObject) arguments[1];
                        entry = atomEntryHostObject.getEntry();
                    } else {
                        entry = hostObject.createEntry((NativeObject) arguments[1]);
                    }
                    url = (String) arguments[0];
                } catch (IRISyntaxException e) {
                    throw new ScriptException(e);
                }
            } else {
                throw new ScriptException("Invalid Parameter");
            }
        } else if (arguments.length == 1) {
            if (arguments[0] instanceof AtomEntryHostObject) {
                AtomEntryHostObject atomEntryHostObject = (AtomEntryHostObject) arguments[0];
                entry = atomEntryHostObject.getEntry();
                url = getEditURI(entry).toString();
            } else {
                throw new ScriptException("URI Parameter is Missing.");
            }

        } else {
            throw new ScriptException("Invalid Number of Parameters");
        }

        performAuthentication(hostObject, url);
        ClientResponse response = hostObject.client.put(url, entry, hostObject.options);

        if (response.getStatus() != 200)
            throw new ScriptException("Put Failed." + response.getStatusText());

    }

    /**
     * <p/>
     * Deletes the AtomEntry designated by the URI or by the AtomEntry object from the APPServer.
     * </p>
     * <p/>
     * deleteEntry(string AtomEntryURI|AtomEntry entryObject);
     * <p/>
     * <pre>
     * client.delete(&quot;http://www.blogger.com/feeds/000/posts/full/000&quot;);
     * </pre>
     *
     * @throws IOException
     */
    public static void jsFunction_deleteEntry(Context cx, Scriptable thisObj, Object[] arguments,
                                              Function funObj)
            throws ScriptException {
        APPClientHostObject hostObject = (APPClientHostObject) thisObj;
        Entry entry;
        String url;
        if (arguments.length == 1) {
            if (arguments[0] instanceof AtomEntryHostObject) {
                AtomEntryHostObject atomEntryHostObject = (AtomEntryHostObject) arguments[0];
                entry = atomEntryHostObject.getEntry();
                url = getEditURI(entry).toString();
            } else if (arguments[0] instanceof String) {
                url = (String) arguments[0];
            } else {
                throw new ScriptException("Invalid Parameter");
            }
        } else {
            throw new ScriptException("Invalid Number of Parameters");
        }


        performAuthentication(hostObject, url);
        if (url != null) {
            ClientResponse response = hostObject.client.delete(url, hostObject.options);
            if (response.getStatus() != 200)
                throw new ScriptException("Delete Failed." + response.getStatusText());
        } else {
            throw new ScriptException("Entry Cannot be deleted.");
        }
    }

    /**
     * Parse the properties given in the javascript object and creates a
     * AtomEntry object.
     *
     * @throws IRISyntaxException
     * @throws ScriptException
     */
    private Entry createEntry(NativeObject nativeEntry) throws ScriptException {
        Entry entry;
        Factory factory = abdera.getFactory();
        entry = factory.newEntry();

        // process authors
        // TODO persons
        Object authorProperty = ScriptableObject.getProperty(nativeEntry, "author");
        if (authorProperty instanceof String) {
            entry.addAuthor((String) (authorProperty));
        }
        Object authorsProperty = ScriptableObject.getProperty(nativeEntry, "authors");
        if (authorsProperty instanceof String) {
            String authorsString = (String) (authorProperty);
            String[] authors = authorsString.split(",");
            for (int i = 0; i < authors.length; i++) {
                entry.addAuthor(authors[i]);
            }
        }

        // process category
        // TODO category object
        Object categoryProperty = ScriptableObject.getProperty(nativeEntry, "category");
        if (categoryProperty instanceof String) {
            entry.addCategory((String) (categoryProperty));
        }
        Object categoriesProperty = ScriptableObject.getProperty(nativeEntry, "categories");
        if (categoriesProperty instanceof String) {
            String categoriesString = (String) (categoriesProperty);
            String[] categories = categoriesString.split(",");
            for (int i = 0; i < categories.length; i++) {
                entry.addCategory(categories[i]);
            }
        }

        // process content
        Object content = ScriptableObject.getProperty(nativeEntry, "content");
        if (content instanceof XML) {
            entry.setContentAsXhtml(content.toString());
        } else if (content instanceof String) {
            entry.setContent((String) content);
        } else {
            throw new ScriptException("Unsupported Content");
        }

        // process contributor
        // TODO person
        Object contributorProperty = ScriptableObject.getProperty(nativeEntry, "contributor");
        if (contributorProperty instanceof String) {
            entry.addContributor((String) (contributorProperty));
        }
        Object contributorsProperty = ScriptableObject.getProperty(nativeEntry, "contributors");
        if (contributorsProperty instanceof String) {
            String contributorsString = (String) (contributorsProperty);
            String[] contributors = contributorsString.split(",");
            for (int i = 0; i < contributors.length; i++) {
                entry.addContributor(contributors[i]);
            }
        }

        // process id
        Object idProperty = ScriptableObject.getProperty(nativeEntry, "id");
        if (idProperty instanceof String) {
            entry.setId((String) (idProperty));
        } else {
            entry.setId(FOMHelper.generateUuid());
        }

        // process link
        // TODO link object
        Object linkProperty = ScriptableObject.getProperty(nativeEntry, "link");
        if (linkProperty instanceof String) {
            entry.addLink((String) (linkProperty));
        }
        Object linksProperty = ScriptableObject.getProperty(nativeEntry, "links");
        if (linksProperty instanceof String) {
            String linksString = (String) (linksProperty);
            String[] links = linksString.split(",");
            for (int i = 0; i < links.length; i++) {
                entry.addLink(links[i]);
            }
        }

        // process published
        // TODO handle javascript date
        Object publishedProperty = ScriptableObject.getProperty(nativeEntry, "published");
        if (publishedProperty instanceof String) {
            if (publishedProperty.equals("now")) {
                entry.setPublished(new Date(System.currentTimeMillis()));
            } else {
                entry.setPublished((String) publishedProperty);
            }
        }

        // process rights
        // TODO support other forms of rights
        Object rights = ScriptableObject.getProperty(nativeEntry, "rights");
        if (rights instanceof XML) {
            entry.setRightsAsXhtml(rights.toString());
        } else if (rights instanceof String) {
            entry.setRights((String) rights);
        }

        // TODO do we need to support atom:source

        // process summary
        // TODO support other forms of summary
        Object summary = ScriptableObject.getProperty(nativeEntry, "summary");
        if (summary instanceof XML) {
            entry.setSummaryAsXhtml(summary.toString());
        } else if (summary instanceof String) {
            entry.setSummary((String) summary);
        }

        // process title
        // TODO support other forms of summery
        Object title = ScriptableObject.getProperty(nativeEntry, "title");
        if (title instanceof XML) {
            entry.setTitleAsXhtml(title.toString());
        } else if (title instanceof String) {
            entry.setTitle((String) (title));
        } else {
            throw new ScriptException("An Entry MUST have a title.");
        }

        // process updated
        // TODO handle javascript date
        Object updated = ScriptableObject.getProperty(nativeEntry, "updated");
        if (updated instanceof String) {
            if (updated.equals("now")) {
                entry.setUpdated(new Date(System.currentTimeMillis()));
            } else {
                entry.setUpdated((String) updated);
            }
        }
        return entry;
    }

    /**
     * At present supports Google services
     *
     * @param hostObject
     */
    private static void performAuthentication(APPClientHostObject hostObject, String uri)
            throws ScriptException {

        try {
            if (hostObject.authenticationType.compareToIgnoreCase("google") == 0) {
                String auth = GoogleLogin.getAuth(
                        hostObject.client, hostObject.serviceString, hostObject.usernameString,
                        hostObject.passwordString);

                RequestOptions options = hostObject.client.getDefaultRequestOptions();
                options.setAuthorization("GoogleLogin " + auth);

                hostObject.options = options;
            } else if (hostObject.authenticationType.compareToIgnoreCase("basic") == 0) {
                //Take basic authentication by default
                hostObject.client.addCredentials(uri, null, null, new UsernamePasswordCredentials(
                        hostObject.usernameString, hostObject.passwordString));
            }
        } catch (URISyntaxException e) {
            throw new ScriptException(e);
        }
    }
}
