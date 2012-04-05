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

import com.sun.syndication.feed.module.mediarss.MediaEntryModuleImpl;
import com.sun.syndication.feed.module.mediarss.types.MediaContent;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.feed.synd.SyndLink;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedOutput;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.NativeArray;
import org.wso2.carbon.CarbonException;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * <p/>
 * The Entry host object is a generic host object capable of intelligently working with both Atom and RSS feeds. The underlying
 * protocol implementation is kept transparent. However, some data might be lost due to this being an abstraction of both
 * Atom and RSS Entries.
 * </p>
 */
public class Entry extends ScriptableObject implements IEntry {

    private SyndEntry entry;

    private String type; // Stores the type of Feed this entry belongs to

    /**
     * Constructor the user will be using inside javaScript
     */
    public void jsConstructor() {
        entry = new SyndEntryImpl();
    }

    public String getClassName() {
        return "Entry";
    }

    public void jsSet_author(Object author) {
        entry.setAuthor(String.valueOf(author));
    }

    public Object jsGet_author() {
        return entry.getAuthor();
    }

    public void jsSet_description(Object newdescription) {
        SyndContent description = new SyndContentImpl();
        description.setType("text/html");
        description.setValue(String.valueOf(newdescription));
        entry.setDescription(description);
    }

    public String jsGet_description() {
        if (entry.getDescription() != null) {
            SyndContent description = entry.getDescription();
            return description.getValue();
        } else {
            return null;
        }

    }

    public void jsSet_category(Object category) {
        ArrayList categories = new ArrayList();
        categories.add(String.valueOf(category));
        entry.setCategories(categories);
    }

    public String jsGet_category() {
        ArrayList categories = (ArrayList) entry.getCategories();
        if (categories.get(0) != null) {
            return (String) categories.get(0);
        }
        return null;
    }

    public void jsSet_content(Object content) {
        SyndContent contents = new SyndContentImpl();
        contents.setType("text/html");
        contents.setValue(String.valueOf(content));

        ArrayList cont = new ArrayList();
        cont.add(contents);

        entry.setContents(cont);
    }

    public String jsGet_content() {
        String retContent = "";

        if (entry.getContents() != null) {
            ArrayList contents = (ArrayList) entry.getContents();
            Iterator iterator = contents.iterator();

            SyndContent currentContent;
            StringBuilder stringBuilder = new StringBuilder(retContent);
            while (iterator.hasNext()) {
                currentContent = (SyndContent) iterator.next();
                stringBuilder.append(" " + currentContent.getValue());
            }
            retContent = stringBuilder.toString();
        }

        if (retContent.compareTo("") == 0) {
            // Let's check whether entry.description holds a payload
            retContent = jsGet_description();
        }

        return retContent;
    }

    public void jsSet_contributor(Object contributor) {
        ArrayList contributors = new ArrayList();
        contributors.add(String.valueOf(contributor));
        entry.setContributors(contributors);
    }

    public String jsGet_contributor() {
        if (entry.getContributors() != null) {
            ArrayList contributors = (ArrayList) entry.getContributors();
            return (String) contributors.get(0);
        } else {
            return null;
        }
    }

    public void jsSet_link(Object link) {
        entry.setLink(String.valueOf(link));
    }

    public NativeArray jsGet_link() {
        NativeArray nativeArray = new NativeArray(0);
        if (entry.getLinks().size() > 0) {
            List list = entry.getLinks();
            int size = list.size();
            for (int i = 0; i < size; i++) {
                SyndLink element = (SyndLink) list.get(i);
                nativeArray.put(i, nativeArray, element.getHref());
            }
            return nativeArray;
        } else if (entry.getLink() != null) {
            nativeArray.put(0, nativeArray, entry.getLink());
            return nativeArray;
        }

        return null;
    }

    public void jsSet_published(Object published) throws CarbonException {
        Date date;

        if (published instanceof Date) {
            date = (Date) published;
        } else {
            date = (Date) Context.jsToJava(published, Date.class);
        }

        if (date != null) {
            entry.setPublishedDate(date);
        } else {
            throw new CarbonException("Invalid parameter");
        }

    }

    public Date jsGet_published() {
        return entry.getPublishedDate();
    }

    public void jsSet_summary(Object summary) {
        SyndContent summaryContent = new SyndContentImpl();
        summaryContent.setType("text/html");
        summaryContent.setValue(String.valueOf(summary));

        entry.setDescription(summaryContent);
    }

    public String jsGet_summary() {
        SyndContent summaryContent = entry.getDescription();
        return summaryContent.getValue();
    }

    public void jsSet_title(Object title) {
        entry.setTitle(String.valueOf(title));
    }

    public String jsGet_title() {
        return entry.getTitle();
    }

    public void jsSet_updated(Object updated) throws CarbonException {
        Date date;

        if (updated instanceof Date) {
            date = (Date) updated;
        } else {
            date = (Date) Context.jsToJava(updated, Date.class);
        }

        if (date != null) {
            entry.setUpdatedDate(date);
        } else {
            throw new CarbonException("Invalid parameter");
        }

    }

    public String jsGet_updated() {
        if (entry.getUpdatedDate() != null)
            return entry.getUpdatedDate().toString();
        return null;
    }

    /**
     * @param cx        Provides the current context
     * @param thisObj   Gives an instance of the js object from which this method was called
     * @param arguments The argument for this is a Module object
     * @param funObj
     * @throws CarbonException
     */
    public static void jsFunction_addMediaModule(Context cx, Scriptable thisObj,
                                                 Object[] arguments,
                                                 Function funObj) throws CarbonException {
        Entry entryObj = (Entry) thisObj;
        if (arguments[0] instanceof MediaModule) {
            MediaModule mediaModule = (MediaModule) arguments[0];
            entryObj.entry.getModules().add(mediaModule.module);

        } else {
            throw new CarbonException("The argument should be a Module object instance.");
        }
    }

    /**
     * @param cx        Provides the current context
     * @param thisObj   Gives an instance of the js object from which this method was called
     * @param arguments None
     * @param funObj
     * @throws CarbonException
     */
    public static MediaModule[] jsFunction_getMediaModules(Context cx, Scriptable thisObj,
                                                           Object[] arguments,
                                                           Function funObj) {
        MediaModule[] retMediaModules = null;

        Entry entryObj = (Entry) thisObj;
        List modules = entryObj.entry.getModules();
        ArrayList tempMediaModules = new ArrayList();

        if (modules.size() > 0) {
            int listSize = modules.size();
            Object currModule;

            for (int x = 0; x < listSize; x++) {
                currModule = modules.get(x);
                MediaEntryModuleImpl currMediaModule;

                if (currModule instanceof MediaEntryModuleImpl) {
                    //This is a Media module supported by us at the moment
                    currMediaModule = (MediaEntryModuleImpl) currModule;

                    //Since in our implementation we have a 1 to 1 mapping of module to content,
                    //we need to create a module each for every content element.
                    MediaContent[] content = currMediaModule.getMediaContents();

                    if (content.length > 0) {

                        for (int y = 0; y < content.length; y++) {
                            MediaModule mediaModule = new MediaModule();
                            mediaModule.module = currMediaModule;
                            tempMediaModules.add(mediaModule);
                        }
                    }
                }
            }

            retMediaModules = new MediaModule[tempMediaModules.size()];
            tempMediaModules.toArray(retMediaModules);
        }

        return retMediaModules;
    }

    /**
     * @return the E4X XML of the contents in this AtomEntry object
     */
    public Scriptable jsGet_XML() throws CarbonException {

        Context cx = Context.getCurrentContext();

        //Creating a bogus feed 
        SyndFeed feed = new SyndFeedImpl();
        feed.setFeedType(this.getType());
        feed.setTitle("placeHolderTitle");
        feed.setDescription("placeHoldeDescription");
        feed.setLink("http://place.holder.link");
        List entries = feed.getEntries();
        entries.add(entry);
        feed.setEntries(entries);

        SyndFeedOutput output = new SyndFeedOutput();
        try {
            String xmlRep = output.outputString(feed);


            if (this.getType().contains("rss")) {
                int start = xmlRep.indexOf("<item>") + "</item>".length();
                int end = xmlRep.indexOf("</item>");
                String entry = xmlRep.substring(start, end);

                // Injecting the required namespace declarations
                entry =
                        "<item xmlns:content=\"http://purl.org/rss/1.0/modules/content/\" xmlns:taxo=\"http://purl.org/rss/1.0/modules/taxonomy/\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:media=\"http://search.yahoo.com/mrss/\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\">" +
                                entry + "</item>";

                Object[] objects = { entry };
                return cx.newObject(this, "XML", objects);
            } else if (this.getType().contains("atom")) {
                // Extracting just the entry xml required
                int start = xmlRep.indexOf("<entry>") + "</entry>".length();
                int end = xmlRep.indexOf("</entry>");
                String entry = xmlRep.substring(start, end);

                // Injecting the required namespace declarations
                entry =
                        "<entry xmlns=\"http://www.w3.org/2005/Atom\" xmlns:taxo=\"http://purl.org/rss/1.0/modules/taxonomy/\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:sy=\"http://purl.org/rss/1.0/modules/syndication/\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\">" +
                                entry + "</entry>";

                Object[] objects = { entry };
                return cx.newObject(this, "XML", objects);
            }
        } catch (FeedException e) {
            throw new CarbonException(e);
        }

        return null;
    }

    public String jsFunction_toString() {
        return entry.toString();
    }

    void setEntry(SyndEntry entry) {
        this.entry = entry;
    }

    SyndEntry getEntry() {
        return entry;
    }

    public String getType() {
        return type;
    }

    public void setType(Object type) {
        this.type = String.valueOf(type);
    }
}
