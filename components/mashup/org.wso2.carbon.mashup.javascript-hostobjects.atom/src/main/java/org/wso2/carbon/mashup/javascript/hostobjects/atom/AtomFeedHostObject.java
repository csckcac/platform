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
package org.wso2.carbon.mashup.javascript.hostobjects.atom;

import org.apache.abdera.Abdera;
import org.apache.abdera.factory.Factory;
import org.apache.abdera.i18n.iri.IRISyntaxException;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Link;
import org.apache.abdera.parser.stax.util.FOMHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.NativeArray;
import org.wso2.javascript.xmlimpl.XML;
import org.wso2.carbon.mashup.javascript.hostobjects.feed.IFeed;
import org.wso2.carbon.mashup.javascript.hostobjects.file.FileHostObject;
import org.wso2.carbon.CarbonException;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * <p>This is a Mozilla Rhino Host Object implementation which tries to provide a javascript representation
 * of the AtomFeed defined in the ATOM specification</p>
 * <p/>
 * <p/>
 * <p>Per RFC4287:</p>
 * <p/>
 * <p/>
 * <pre>
 * The "atom:feed" element is the document (i.e., top-level) element of
 * an Atom Feed Document, acting as a container for metadata and data
 * associated with the feed.  Its element children consist of metadata
 * elements followed by zero or more atom:entry child elements.
 * <p/>
 * atomFeed =
 *   element atom:feed {
 *       atomCommonAttributes,
 *       (atomAuthor*
 *        &amp; atomCategory*
 *        &amp; atomContributor*
 *        &amp; atomGenerator?
 *        &amp; atomIcon?
 *        &amp; atomId
 *        &amp; atomLink*
 *        &amp; atomLogo?
 *        &amp; atomRights?
 *        &amp; atomSubtitle?
 *        &amp; atomTitle
 *        &amp; atomUpdated
 *        &amp; extensionElement*),
 *       atomEntry*
 *   }
 * <p/>
 * This specification assigns no significance to the order of atom:entry
 * elements within the feed.
 * <p/>
 * The following child elements are defined by this specification (note
 * that the presence of some of these elements is required):
 * <p/>
 * o  atom:feed elements MUST contain one or more atom:author elements,
 *    unless all of the atom:feed element's child atom:entry elements
 *    contain at least one atom:author element.
 * o  atom:feed elements MAY contain any number of atom:category
 *    elements.
 * o  atom:feed elements MAY contain any number of atom:contributor
 *    elements.
 * o  atom:feed elements MUST NOT contain more than one atom:generator
 *    element.
 * o  atom:feed elements MUST NOT contain more than one atom:icon
 *    element.
 * o  atom:feed elements MUST NOT contain more than one atom:logo
 *    element.
 * o  atom:feed elements MUST contain exactly one atom:id element.
 * o  atom:feed elements SHOULD contain one atom:link element with a rel
 *    attribute value of "self".  This is the preferred URI for
 *    retrieving Atom Feed Documents representing this Atom feed.
 * o  atom:feed elements MUST NOT contain more than one atom:link
 *    element with a rel attribute value of "alternate" that has the
 *    same combination of type and hreflang attribute values.
 * o  atom:feed elements MAY contain additional atom:link elements
 *    beyond those described above.
 * o  atom:feed elements MUST NOT contain more than one atom:rights
 *    element.
 * o  atom:feed elements MUST NOT contain more than one atom:subtitle
 *    element.
 * o  atom:feed elements MUST contain exactly one atom:title element.
 * o  atom:feed elements MUST contain exactly one atom:updated element.
 * <p/>
 * If multiple atom:entry elements with the same atom:id value appear in
 * an Atom Feed Document, they represent the same entry.  Their
 * atom:updated timestamps SHOULD be different.  If an Atom Feed
 * Document contains multiple entries with the same atom:id, Atom
 * Processors MAY choose to display all of them or some subset of them.
 * One typical behavior would be to display only the entry with the
 * latest atom:updated timestamp.
 * </pre>
 */
public class AtomFeedHostObject extends ScriptableObject implements IFeed {

    private Feed feed;

    private static Log log = LogFactory.getLog(AtomFeedHostObject.class);


    public AtomFeedHostObject() {
        super();
    }

    /**
     * Constructor the user will be using inside javaScript
     */
    public Scriptable jsConstructor() {
        Abdera abdera = new Abdera();
        Factory factory = abdera.getFactory();
        feed = factory.newFeed();
        return this;
    }

    public String getClassName() {
        return "AtomFeed";
    }

    public void jsSet_id(Object id) {
        if (id instanceof String) {
            feed.setId((String) (id));
        } else {
            feed.setId(FOMHelper.generateUuid());
        }
    }

    public String jsGet_id() {
        if (feed != null)
            return feed.getId().toASCIIString();
        return null;
    }

    public void jsSet_author(Object author) {
        feed.addAuthor(String.valueOf(author));
    }

    public String jsGet_author() {
        return feed.getAuthor().getName();
    }

    public void jsSet_updated(Object updated) throws CarbonException {
        Date date;

        if (updated instanceof Date) {
            date = (Date) updated;
        } else {
            date = (Date) Context.jsToJava(updated, Date.class);
        }

        if (date != null) {
            feed.setUpdated(date);
        } else {
            throw new CarbonException("Invalid parameter");
        }

    }

    public String jsGet_updated() {
        if (feed != null)
            return feed.getUpdated().toString();
        return null;
    }

    public void jsSet_category(Object category) {
        feed.addCategory(String.valueOf(category));
    }

    public String jsGet_category() {
        if (feed != null)
            return feed.getCategories().get(0).toString();
        return null;
    }

    public void jsSet_contributor(Object contributor) {
        feed.addContributor(String.valueOf(contributor));
    }

    public String jsGet_contributor() {
        if (feed != null)
            return feed.getContributors().get(0).toString();
        return null;
    }

    public void jsSet_link(Object link) {
        feed.addLink(String.valueOf(link));
    }

    public NativeArray jsGet_link() {
        if (feed != null) {
            NativeArray nativeArray = new NativeArray(0);
            List list = feed.getLinks();
            int size = list.size();
            for (int i = 0; i < size; i++) {
                Link element = (Link) list.get(i);
                nativeArray.put(i, nativeArray, element.getHref().toString());
            }
            return nativeArray;
        }
        return null;
    }

    public void jsSet_title(Object title) {
        if (title instanceof XML) {
            feed.setTitleAsXhtml(title.toString());
        } else {
            feed.setTitle(String.valueOf(title));
        }
    }

    public String jsGet_title() {
        if (feed != null)
            return feed.getTitle();
        return null;
    }

    public void jsSet_rights(Object rights) {
        if (rights instanceof XML) {
            feed.setRightsAsXhtml(rights.toString());
        } else {
            feed.setRights(String.valueOf(rights));
        }
    }

    public String jsGet_rights() {
        if (feed != null)
            return feed.getRights();
        return null;
    }

    /**
     * Returns the E4X XML contents of this AtomFeed object
     *
     * @return E4X XML
     */
    public Scriptable jsGet_XML() {
        Context cx = Context.getCurrentContext();
        if (feed != null) {
            Object[] objects = { feed };
            Scriptable xmlHostObject = cx.newObject(this, "XML", objects);
            return xmlHostObject;
        }
        return null;
    }

    /**
     * <p/>
     * Adds a new Entry to the <i>end</i> of the Feeds collection of entries
     * </p>
     *
     * @param cx        Provides the current context
     * @param thisObj   Gives an instance of the js object from which this method was called
     * @param arguments Argumets can be either an AtomEntryHostObject or None
     * @param funObj
     * @return Scriptable AtomEntryHostObject
     * @throws IOException
     */
    public static Scriptable jsFunction_addEntry(Context cx, Scriptable thisObj, Object[] arguments,
                                                 Function funObj) {

        AtomFeedHostObject feedObject = (AtomFeedHostObject) thisObj;
        AtomEntryHostObject retScriptObject = null;

        if (arguments[0] instanceof AtomEntryHostObject) {
            AtomEntryHostObject atomEntryHostObject = (AtomEntryHostObject) arguments[0];
            feedObject.feed.addEntry(atomEntryHostObject.getEntry());
            retScriptObject = atomEntryHostObject;
        } else if (arguments.length == 0) {
            Entry newEntry = feedObject.feed.addEntry();
            retScriptObject =
                    (AtomEntryHostObject) cx.newObject(feedObject, "AtomEntry", new Object[0]);
            retScriptObject.setEntry(newEntry);
        }
        return retScriptObject;
    }


    /**
     * <p>Adds a new Entry to the <i>start</i> of the Feeds collection of entries</p>
     *
     * @param cx        Provides the current context
     * @param thisObj   Gives an instance of the js object from which this method was called
     * @param arguments Argumets can be either an AtomEntryHostObject or None
     * @param funObj
     * @return Scriptable AtomEntryHostObject
     * @throws IOException
     */
    public static Scriptable jsFunction_insertEntry(Context cx, Scriptable thisObj,
                                                    Object[] arguments,
                                                    Function funObj) {

        AtomFeedHostObject feedObject = (AtomFeedHostObject) thisObj;
        AtomEntryHostObject retScriptObject = null;

        if (arguments[0] instanceof AtomEntryHostObject) {
            AtomEntryHostObject atomEntryHostObject = (AtomEntryHostObject) arguments[0];
            feedObject.feed.insertEntry(atomEntryHostObject.getEntry());
            retScriptObject = atomEntryHostObject;
        } else if (arguments.length == 0) {
            Entry newEntry = feedObject.feed.insertEntry();
            retScriptObject =
                    (AtomEntryHostObject) cx.newObject(feedObject, "AtomEntry", new Object[0]);
            retScriptObject.setEntry(newEntry);
        }
        return retScriptObject;
    }


    /**
     * <p>Retrieves the first entry in the feed with the given atom:id value</p>
     *
     * @param cx        Provides the current context
     * @param thisObj   Gives an instance of the js object from which this method was called
     * @param arguments A String representation of the Atom Entry's id
     * @param funObj
     * @return Scriptable AtomEntryHostObject
     * @throws IOException
     */
    public static Scriptable jsFunction_getEntry(Context cx, Scriptable thisObj, Object[] arguments,
                                                 Function funObj) throws CarbonException {

        AtomFeedHostObject feedObject = (AtomFeedHostObject) thisObj;
        AtomEntryHostObject retScriptObject = null;

        if (arguments[0] instanceof String) {
            try {
                Entry entry = feedObject.feed.getEntry((String) arguments[0]);

                if (entry != null) {
                    retScriptObject = (AtomEntryHostObject) cx
                            .newObject(feedObject, "AtomEntry", new Object[0]);
                    retScriptObject.setEntry(entry);
                } else {
                    throw new CarbonException("Invalid Atom Entry Id");
                }

            } catch (IRISyntaxException e) {
                throw new CarbonException(e);
            }
        } else {
            throw new CarbonException("Invalid parameter");
        }

        return retScriptObject;
    }


    /**
     * <p>Returns the complete set of entries contained in this feed</p>
     *
     * @param cx        Provides the current context
     * @param thisObj   Gives an instance of the js object from which this method was called
     * @param arguments No arguments required
     * @param funObj
     * @return AtomEntryHostObject[]    An Array of AtomEntryHostObjects contained in this feed
     * @throws IOException
     */
    public static AtomEntryHostObject[] jsFunction_getEntries(Context cx, Scriptable thisObj,
                                                              Object[] arguments,
                                                              Function funObj) {
        AtomFeedHostObject feedObject = (AtomFeedHostObject) thisObj;
        AtomEntryHostObject[] retEntries = null;

        //Retrieving the entries from our feed
        List tempEntries = feedObject.feed.getEntries();
        Iterator tempEntryIterator = tempEntries.iterator();

        //Creating a list to store converted Entries
        ArrayList convertedEntries = new ArrayList();

        Entry currentEntry;

        //Converting the list of Abdera Entries to AtomEntryHostObjects
        while (tempEntryIterator.hasNext()) {
            currentEntry = (Entry) tempEntryIterator.next();
            AtomEntryHostObject newAtomEntry =
                    (AtomEntryHostObject) cx.newObject(feedObject, "AtomEntry", new Object[0]);
            newAtomEntry.setEntry(currentEntry);
            convertedEntries.add(newAtomEntry);
        }

        retEntries = new AtomEntryHostObject[convertedEntries.size()];
        convertedEntries.toArray(retEntries);
        return retEntries;
    }


    /**
     * Serializes the Feed to a given local file
     *
     * @param cx        Provides the current context
     * @param thisObj   Gives an instance of the js object from which this method was called
     * @param arguments The expected argument is a String representing the file path
     * @param funObj
     * @throws IOException
     */
    public static Scriptable jsFunction_writeTo(Context cx, Scriptable thisObj, Object[] arguments,
                                                Function funObj) throws CarbonException {

        AtomFeedHostObject feedObject = (AtomFeedHostObject) thisObj;
        FileHostObject fileHostObject;
        FileWriter fileWriter = null;
        try {
            if (arguments[0] instanceof String) {
                fileHostObject = (FileHostObject) cx.newObject(feedObject, "File", arguments);
                fileWriter = new FileWriter(fileHostObject.getFile());
                feedObject.feed.writeTo(fileWriter);
                fileWriter.flush();
            } else if (arguments[0] instanceof FileHostObject) {
                fileHostObject = (FileHostObject) arguments[0];
                fileWriter = new FileWriter(fileHostObject.getFile());
                feedObject.feed.writeTo(fileWriter);
                fileWriter.flush();
            } else {
                throw new CarbonException("Invalid parameter");
            }
            return feedObject;
        } catch (IOException e) {
            throw new CarbonException(e);
        } finally {
            if(fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    log.warn("Error closing the stream", e);
                }
            }

        }
    }


    Feed getFeed() {
        return this.feed;
    }

    void setFeed(Feed newFeed) {
        this.feed = newFeed;
    }

}
