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
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Link;
import org.apache.abdera.parser.stax.util.FOMHelper;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.NativeArray;
import org.wso2.javascript.xmlimpl.XML;
import org.wso2.carbon.mashup.javascript.hostobjects.feed.IEntry;
import org.wso2.carbon.CarbonException;

import java.util.Date;
import java.util.List;

/**
 * <p> This is a Mozilla Rhino Host Object implementation which tries to provide a javascript representation
 * of the AtomEntry defined in the <a href="link">ATOM specification</a>.
 * <p/>
 * The "atom:entry" element represents an individual entry, acting as a
 * container for metadata and data associated with the entry. This element can
 * appear as a child of the atom:feed element, or it can appear as the document
 * (i.e., top-level) element of a stand-alone Atom Entry Document.
 * </p>
 * <p/>
 * <pre>
 *  atomEntry =
 *  element atom:entry {atomCommonAttributes,
 *  (atomAuthor*
 *  &amp; atomCategory*
 *  &amp; atomContent?
 *  &amp; atomContributor*
 *  &amp; atomId
 *  &amp; atomLink*
 *  &amp; atomPublished?
 *  &amp; atomRights?
 *  &amp; atomSource?
 *  &amp; atomSummary?
 *  &amp; atomTitle
 *  &amp; atomUpdated
 *  &amp; extensionElement*)
 *  }
 * </pre>
 * <p/>
 * <p/>
 * This specification assigns no significance to the order of appearance of the
 * child elements of atom:entry.
 * <p/>
 * The following child elements are defined by this specification (note that it
 * requires the presence of some of these elements):
 * </p>
 * <p/>
 * <pre>
 * <ul>
 *  <li>atom:entry elements MUST contain one or more atom:author elements,
 *  unless the atom:entry contains an atom:source element that
 *  contains an atom:author element or, in an Atom Feed Document, the
 *  atom:feed element contains an atom:author element itself.</li>
 *  <li>atom:entry elements MAY contain any number of atom:category
 *  elements.</li>
 *  <li>atom:entry elements MUST NOT contain more than one atom:content
 *  element.</li>
 *  <li>atom:entry elements MAY contain any number of atom:contributor
 *  elements.</li>
 *  <li>atom:entry elements MUST contain exactly one atom:id element.</li>
 *  <li>atom:entry elements that contain no child atom:content element
 *  MUST contain at least one atom:link element with a rel attribute
 *  value of &quot;alternate&quot;.</li>
 *  <li>atom:entry elements MUST NOT contain more than one atom:link
 *  element with a rel attribute value of &quot;alternate&quot; that has the
 *  same combination of type and hreflang attribute values.</li>
 *  <li>atom:entry elements MAY contain additional atom:link elements
 *  beyond those described above.</li>
 *  <li>atom:entry elements MUST NOT contain more than one atom:published
 *  element.</li>
 *  <li>atom:entry elements MUST NOT contain more than one atom:rights
 *  element.</li>
 *  <li>atom:entry elements MUST NOT contain more than one atom:source
 *  element.</li>
 *  <li>atom:entry elements MUST contain an atom:summary element in either
 *  of the following cases:
 *  the atom:entry contains an atom:content that has a &quot;src&quot;
 *  attribute (and is thus empty).
 *  the atom:entry contains content that is encoded in Base64;
 *  i.e., the &quot;type&quot; attribute of atom:content is a MIME media type
 *  [MIMEREG], but is not an XML media type [RFC3023], does not
 *  begin with &quot;text/&quot;, and does not end with &quot;/xml&quot; or &quot;+xml&quot;.</li>
 *  <li>atom:entry elements MUST NOT contain more than one atom:summary
 *  element.</li>
 *  <li>atom:entry elements MUST contain exactly one atom:title element.</li>
 *  <li>atom:entry elements MUST contain exactly one atom:updated element.</li>
 *  </ul>
 * <p/>
 * </pre>
 */
public class AtomEntryHostObject extends ScriptableObject implements IEntry {

    private static final long serialVersionUID = -2799736487293031053L;
    private Abdera abdera;
    private Entry entry;

    public AtomEntryHostObject() {
        super();
    }

    public AtomEntryHostObject(Scriptable arg0, Scriptable arg1) {
        super(arg0, arg1);
    }

    /**
     * Constructor the user will be using inside javaScript
     */
    public void jsConstructor() {
        abdera = new Abdera();
        Factory factory = abdera.getFactory();
        entry = factory.newEntry();
    }

    public String getClassName() {
        return "AtomEntry";
    }

    public void jsSet_author(Object author) {
        entry.addAuthor(String.valueOf(author));
    }

    public Object jsGet_author() {
        if (entry != null) {
            return entry.getAuthor().getName();
        }
        return null;
    }

    public void jsSet_category(Object category) {
        entry.addCategory(String.valueOf(category));
    }

    public String jsGet_category() {
        if (entry != null)
            return entry.getCategories().get(0).toString();
        return null;
    }

    //process content
    public void jsSet_content(Object content) {
        if (content instanceof XML) {
            entry.setContentAsXhtml(content.toString());
        } else {
            entry.setContent(String.valueOf(content));
        }
    }

    public String jsGet_content() {
        if (entry != null)
            return entry.getContent();
        return null;
    }

    //process contributor       
    public void jsSet_contributor(Object contributor) {
        entry.addContributor(String.valueOf(contributor));
    }

    public String jsGet_contributor() {
        if (entry != null)
            return entry.getContributors().get(0).toString();
        return null;
    }

    //process id
    public void jsSet_id(Object id) {
        if (id instanceof String) {
            entry.setId((String) (id));
        } else {
            entry.setId(FOMHelper.generateUuid());
        }
    }

    public String jsGet_id() {
        if (entry != null)
            return entry.getId().toASCIIString();
        return null;
    }

    // process link                   
    public void jsSet_link(Object link) {
        entry.addLink(String.valueOf(link));
    }

    public NativeArray jsGet_link() {
        if (entry != null) {
            List list = entry.getLinks();
            int size = list.size();
            NativeArray nativeArray = new NativeArray(0);
            for (int i = 0; i < size; i++) {
                Link element = (Link) list.get(i);
                nativeArray.put(i, nativeArray, element.getHref().toString());
            }
            return nativeArray;
        }
        return null;
    }

    //process published
    public void jsSet_published(Object published) throws CarbonException {
        Date date = null;

        if (published instanceof Date) {
            date = (Date) published;
        } else {
            date = (Date) Context.jsToJava(published, Date.class);
        }

        if (date != null) {
            entry.setPublished(date);
        } else {
            throw new CarbonException("Invalid parameter");
        }
    }

    public Date jsGet_published() {
        if (entry != null)
            return entry.getPublished();
        return null;
    }

    //process rights
    public void jsSet_rights(Object rights) {
        if (rights instanceof XML) {
            entry.setRightsAsXhtml(rights.toString());
        } else {
            entry.setRights(String.valueOf(rights));
        }
    }

    public String jsGet_rights() {
        if (entry != null)
            return entry.getRights();
        return null;
    }

    //process summary
    public void jsSet_summary(Object summary) {
        if (summary instanceof XML) {
            entry.setSummaryAsXhtml(summary.toString());
        } else {
            entry.setSummary(String.valueOf(summary));
        }
    }

    public String jsGet_summary() {
        if (entry != null)
            return entry.getSummary();
        return null;
    }

    //process title
    public void jsSet_title(Object title) {
        if (title instanceof XML) {
            entry.setTitleAsXhtml(title.toString());
        } else {
            entry.setTitle(String.valueOf(title));
        }
    }

    public String jsGet_title() {
        if (entry != null)
            return entry.getTitle();
        return null;
    }

    //process updated
    public void jsSet_updated(Object updated) throws CarbonException {
        Date date = null;

        if (updated instanceof Date) {
            date = (Date) updated;
        } else {
            date = (Date) Context.jsToJava(updated, Date.class);
        }

        if (date != null) {
            entry.setUpdated(date);
        } else {
            throw new CarbonException("Invalid parameter");
        }
    }

    public String jsGet_updated() {
        if (entry != null)
            return entry.getUpdated().toString();
        return null;
    }

    /**
     * @return the E4X XML of the contents in this AtomEntry object
     */
    public Scriptable jsGet_XML() {
        Context cx = Context.getCurrentContext();
        if (entry != null) {
            Object[] objects = { entry };
            Scriptable xmlHostObject = cx.newObject(this, "XML", objects);
            return xmlHostObject;
        }
        return null;
    }

    Entry getEntry() {
        return entry;
    }

    void setEntry(Entry entry) {
        this.entry = entry;
    }

    public String jsFunction_toString() {
        return entry.toString();
    }
}
