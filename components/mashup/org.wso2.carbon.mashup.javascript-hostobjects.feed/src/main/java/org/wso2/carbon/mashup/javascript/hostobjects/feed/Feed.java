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
package org.wso2.carbon.mashup.javascript.hostobjects.feed;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.feed.synd.SyndLink;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedOutput;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.NativeArray;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.mashup.javascript.hostobjects.feed.internal.FeedServiceComponent;
import org.wso2.carbon.mashup.javascript.hostobjects.file.FileHostObject;
import org.wso2.carbon.mashup.utils.MashupConstants;
import org.wso2.carbon.mashup.javascript.hostobjects.hostobjectservice.service.HostObjectService;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * <p/>
 * The Feed host object is a generic host object capable of intelligently working with both Atom and RSS feeds. The underlying
 * protocol implementation is kept transparent. However, some data might be lost due to this being an abstraction of both
 * Atom and RSS feeds.
 * <p/>
 * Notes:
 * <p/>
 * 1. When instantiating a Feed object, feedType, title, description and link are mandatory according to the RSS specification
 * 2. Valid feed types to be passed in feed.feedType() are rss_0.90, rss_0.91, rss_0.92, rss_0.93, rss_0.94, rss_1.0 rss_2.0 or atom_0.3.
 * <p/>
 * </p>
 * <p/>
 * <pre>
 * eg:
 *     //Creating an RSS 2.0 feed and writing it to file.
 * <p/>
 *     var feed = new Feed();
 *     feed.feedType = "rss_2.0"; // Not necessary for RSS 2.0 since the Feed object defaults to this feed type.
 *     feed.title = "This is a test Feed";
 *     feed.description = "This feed demonsrates the use of Feed host object to create an RSS 2.0 feed.";
 *     feed.link = "http://mooshup.com/rss20.xml";
 * <p/>
 *     var entry = new Entry();
 *     entry.title = "This is a test entry.";
 *     entry.description = "This is a sample entry demonstrating the use of the Entry host object.";
 *     feed.insertEntry(entry);
 * <p/>
 *     var entry2 = new Entry();
 *     entry2.title = "This is another test entry.";
 *     entry2.description = "This is a sample entry demonstrating the use of the Entry host object.";
 *     feed.insertEntry(entry2);
 * <p/>
 *     var result = feed.writeTo("test-created-rss-feed.xml");
 * <p/>
 * </pre>
 * </p>
 */
public class Feed extends ScriptableObject implements IFeed {

    private SyndFeed feed;

    private static Log log = LogFactory.getLog(Feed.class);

    public Scriptable jsConstructor() {
        feed = new SyndFeedImpl();
        feed.setFeedType("rss_2.0");
        return this;
    }

    public String getClassName() {
        return "Feed";
    }

    public void jsSet_feedType(Object feedtype) {
        feed.setFeedType(String.valueOf(feedtype));
    }

    public String jsGet_feedType() {
        return feed.getFeedType();
    }

    public void jsSet_author(Object author) {
        feed.setAuthor(String.valueOf(author));
    }

    public String jsGet_author() {
        return feed.getAuthor();
    }

    public void jsSet_description(Object description) {
        feed.setDescription(String.valueOf(description));
    }

    public String jsGet_description() {
        return feed.getDescription();
    }

    public void jsSet_updated(Object updated) throws CarbonException {
        Date date;

        if (updated instanceof Date) {
            date = (Date) updated;
        } else {
            date = (Date) Context.jsToJava(updated, Date.class);
        }

        if (date != null) {
            feed.setPublishedDate(date);
        } else {
            throw new CarbonException("Invalid parameter");
        }

    }

    public String jsGet_updated() {
        if (feed.getPublishedDate() != null)
            return feed.getPublishedDate().toString();
        return null;
    }

    public void jsSet_category(Object category) {
        ArrayList categories = new ArrayList();
        categories.add(String.valueOf(category));
        feed.setCategories(categories);
    }

    public String jsGet_category() {
        ArrayList categories = (ArrayList) feed.getCategories();
        if (categories.get(0) != null) {
            return (String) categories.get(0);
        }
        return null;
    }

    public void jsSet_contributor(Object contributor) {
        ArrayList contributors = new ArrayList();
        contributors.add(String.valueOf(contributor));
        feed.setContributors(contributors);
    }

    public String jsGet_contributor() {
        ArrayList contributors = (ArrayList) feed.getContributors();
        if (contributors.get(0) != null) {
            return (String) contributors.get(0);
        }
        return null;
    }

    public void jsSet_link(Object link) {
        feed.setLink(String.valueOf(link));
    }

    public NativeArray jsGet_link() {
        if (feed != null) {
            NativeArray nativeArray = new NativeArray(0);
            if (feed.getLinks() != null) {
                List list = feed.getLinks();
                int size = list.size();
                for (int i = 0; i < size; i++) {
                    SyndLink element = (SyndLink) list.get(i);
                    nativeArray.put(i, nativeArray, element.getHref());
                }
                return nativeArray;
            } else if (feed.getLink().compareTo("") != 0) {
                nativeArray.put(0, nativeArray, feed.getLink());
                return nativeArray;
            }
        }
        return null;
    }

    public void jsSet_title(Object title) {
        feed.setTitle(String.valueOf(title));
    }

    public String jsGet_title() {
        return feed.getTitle();
    }

    /**
     * Returns the E4X XML contents of this AtomFeed object
     *
     * @return E4X XML
     */
    public Scriptable jsGet_XML() throws CarbonException {
        Context cx = Context.getCurrentContext();
        if (feed != null) {
            SyndFeedOutput output = new SyndFeedOutput();
            try {
                String xmlRep = output.outputString(feed);
                //removing the encoding part, since it makes trouble sometimes
                xmlRep = xmlRep.substring(xmlRep.indexOf("?>") + 2);
                Object[] objects = { xmlRep };
                return cx.newObject(this, "XML", objects);
            } catch (FeedException e) {
                throw new CarbonException(e);
            }
        }
        return null;
    }

    SyndFeed getFeed() {
        return this.feed;
    }

    void setFeed(SyndFeed newFeed) {
        this.feed = newFeed;
    }


    /**
     * <p>Returns the complete set of entries contained in this feed</p>
     * <p/>
     * <pre>
     * eg:
     *    var entries = feed.getEntries();
     * </pre>
     *
     * @param cx        Provides the current context
     * @param thisObj   Gives an instance of the js object from which this method was called
     * @param arguments No arguments required
     * @param funObj
     * @return AtomEntryHostObject[]    An Array of Entry Host Objects contained in this feed
     */
    public static Entry[] jsFunction_getEntries(Context cx, Scriptable thisObj, Object[] arguments,
                                                Function funObj) {
        Feed feedObject = (Feed) thisObj;
        Entry[] retEntries = null;

        //Retrieving the entries from our feed
        List tempEntries = feedObject.feed.getEntries();
        Iterator tempEntryIterator = tempEntries.iterator();

        //Creating a list to store converted Entries
        ArrayList convertedEntries = new ArrayList();

        SyndEntry currentEntry = null;

        //Converting the list of ROME Entries to Entry Host Objects
        while (tempEntryIterator.hasNext()) {
            currentEntry = (SyndEntry) tempEntryIterator.next();
            Entry newEntry = (Entry) cx.newObject(feedObject, "Entry", new Object[0]);
            newEntry.setEntry(currentEntry);

            // Storing the feed type of the entry so that it is self aware
            newEntry.setType(feedObject.getFeed().getFeedType());
            convertedEntries.add(newEntry);
        }

        retEntries = new Entry[convertedEntries.size()];
        convertedEntries.toArray(retEntries);

        return retEntries;
    }


    /**
     * <p>Adds a new Entry to the <i>end</i> of the Feeds collection of entries</p>
     * <p/>
     * <pre>
     * eg:
     *     feed.insertEntry(newEntry);
     * </pre>
     *
     * @param cx        Provides the current context
     * @param thisObj   Gives an instance of the js object from which this method was called
     * @param arguments Argumets can be either an Entry HostObject or None
     * @param funObj
     * @return Scriptable Entry HostObject
     * @throws IOException
     */
    public static Scriptable jsFunction_insertEntry(Context cx, Scriptable thisObj,
                                                    Object[] arguments,
                                                    Function funObj) {

        Feed feedObject = (Feed) thisObj;
        Entry retScriptObject = null;

        if (arguments[0] instanceof Entry) {
            Entry newEntry = (Entry) arguments[0];

            // Storing the Feed type of this entry, so that it is self aware
            newEntry.setType(feedObject.getFeed().getFeedType());

            //getting the existing set of entries from the feed and adding the passed entry
            List currentEntries = feedObject.feed.getEntries();
            currentEntries.add(newEntry.getEntry());
            feedObject.feed.setEntries(currentEntries);

            retScriptObject = newEntry;
        } else if (arguments.length == 0) {
            SyndEntry newEntry = new SyndEntryImpl();

            //getting the existing set of entries from the feed and adding the new blank entry
            List currentEntries = feedObject.feed.getEntries();
            currentEntries.add(newEntry);
            feedObject.feed.setEntries(currentEntries);

            retScriptObject = (Entry) cx.newObject(feedObject, "Entry", new Object[0]);
            retScriptObject.setEntry(newEntry);
        }
        return retScriptObject;
    }


    /**
     * <p>Serializes the Feed to a given local file</p>
     * <p/>
     * <pre>
     * eg:
     *     feed.writeTo("file-name.xml");
     * </pre>
     *
     * @param cx        Provides the current context
     * @param thisObj   Gives an instance of the js object from which this method was called
     * @param arguments The expected argument is a String representing the file path
     * @param funObj
     * @throws CarbonException
     */
    public static Scriptable jsFunction_writeTo(Context cx, Scriptable thisObj, Object[] arguments,
                                                Function funObj) throws CarbonException {

        if (!isJavaScriptFileObjectAvailable()) {
            throw new CarbonException("Cannot find the FileHostObject class. " +
                    "Make sure that its present in the classpath");
        }
        
        Feed feedObject = (Feed) thisObj;
        FileHostObject fileHostHostObject;
        FileWriter fileWriter = null;

        try {
            if (arguments[0] instanceof String) {
                fileHostHostObject = (FileHostObject) cx.newObject(feedObject, "File", arguments);
                fileWriter = new FileWriter(fileHostHostObject.getFile());
                SyndFeedOutput output = new SyndFeedOutput();
                output.output(feedObject.feed, fileWriter);
                fileWriter.close();
            } else if (arguments[0] instanceof FileHostObject) {
                fileHostHostObject = (FileHostObject) arguments[0];
                fileWriter = new FileWriter(fileHostHostObject.getFile());
                SyndFeedOutput output = new SyndFeedOutput();
                output.output(feedObject.feed, fileWriter);
                fileWriter.flush();
            } else {
                throw new CarbonException("Invalid parameter");
            }
        } catch (FeedException e) {
            throw new CarbonException(e);
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
        return feedObject;
    }

    private static boolean isJavaScriptFileObjectAvailable() {
        HostObjectService hostObjectDetails = FeedServiceComponent.getHostObjectService();
        if (hostObjectDetails != null) {
            if (hostObjectDetails.getHostObjectClasses().contains(MashupConstants.FILE_HOST_OBJECT_NAME)) {
                return true;
            }
        }
        return false;
    }
}
