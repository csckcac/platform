package org.wso2.carbon.hostobjects.feed;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.abdera.Abdera;
import org.apache.abdera.factory.Factory;
import org.apache.abdera.i18n.iri.IRISyntaxException;

import org.apache.abdera.model.Category;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Link;
import org.apache.abdera.parser.stax.util.FOMHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.NativeArray;
import org.wso2.carbon.scriptengine.exceptions.ScriptException;
import org.wso2.carbon.scriptengine.util.HostObjectUtil;
import org.wso2.javascript.xmlimpl.XML;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

//import com.sun.xml.internal.bind.v2.schemagen.xmlschema.List;

public class FeedHostObject extends ScriptableObject {

	private static Feed feed;
	private static SyndFeed rssFeed;
	private static FeedHostObject feedHostObject;
	private static Abdera abdera;
	private static Entry entry;
	private static final String HOST_OBJECT_NAME = "Feed";
	private static boolean isRssFeed;
	private static Log log = LogFactory.getLog(FeedHostObject.class);
	public static Context ctx;

	@Override
	public String getClassName() {
		return "Feed";
	}

	/**
	 * Constructor the user will be using inside javaScript
	 */
	public static Scriptable jsConstructor(Context cx, Object[] args,
			Function ctorObj, boolean inNewExpr) throws ScriptException {
		int argsCount = args.length;

		if (argsCount > 2) {
			HostObjectUtil.invalidNumberOfArgs(HOST_OBJECT_NAME,
					HOST_OBJECT_NAME, argsCount, true);
		}
		Abdera abdera = new Abdera();
		Factory factory = abdera.getFactory();
		feed = factory.newFeed();
		feedHostObject = new FeedHostObject();	
		ctx = cx;
		if (argsCount == 0) {					
			return feedHostObject;
		}
		if (argsCount == 1) {
			if (!(args[0] instanceof String)) {
				HostObjectUtil.invalidArgsError(HOST_OBJECT_NAME,
						HOST_OBJECT_NAME, "1", "string", args[0], true);
			}		
			
			FeedHostObject.jsFunction_getFeed(cx, null, args, null);
		}
		return feedHostObject;
	}

	public Scriptable feedConstructor() {
		Abdera abdera = new Abdera();
		Factory factory = abdera.getFactory();
		feed = factory.newFeed();
		return this;
	}

	synchronized public static void jsFunction_getFeed(Context cx,
			Scriptable thisObj, Object[] arguments, Function funObj)
			throws ScriptException {
		if (arguments.length != 1) {
			throw new ScriptException("Invalid parameter");
		}

		if (arguments[0] instanceof String) {

			feed = null;
			URL url = null;
			try {

				url = new URL((String) arguments[0]);
				feed = (Feed) Abdera.getNewParser().parse(url.openStream())
						.getRoot();
				isRssFeed = false;
			} catch (ClassCastException e) {

				XmlReader reader = null;

				try {
					reader = new XmlReader(url);
					rssFeed = new SyndFeedInput().build(reader);
					isRssFeed = true;

					for (Iterator i = rssFeed.getEntries().iterator(); i
							.hasNext();) {
						SyndEntry entry = (SyndEntry) i.next();

					}
				} catch (IOException e1) {
					throw new ScriptException(e1);
				} catch (Exception e1) {
					throw new ScriptException(e1);
				} finally {
					if (reader != null)
						try {
							reader.close();
						} catch (IOException e1) {
							throw new ScriptException(e1);
						}
				}
			} catch (IRISyntaxException e) {
				throw new ScriptException(e);
			} catch (MalformedURLException e) {
				throw new ScriptException(e);
			} catch (IOException e) {
				throw new ScriptException(e);
			}

		} else {
			throw new ScriptException(
					"Invalid parameter, It is must to be a String");
		}
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

	public String jsGet_author() {
		String author;
		if (isRssFeed) {
			author = rssFeed.getAuthor().toString();
		} else {
			author = feed.getAuthor().toString();
		}
		return author;
	}

	public void jsSet_author(Object author) {
		feed.addAuthor(String.valueOf(author));
	}

	public void jsSet_updated(Object updated) throws ScriptException {
		Date date;

		if (updated instanceof Date) {
			date = (Date) updated;
		} else {
			date = (Date) Context.jsToJava(updated, Date.class);
		}

		if (date != null) {
			feed.setUpdated(date);
		} else {
			throw new ScriptException("Invalid parameter");
		}

	}

	public String jsGet_updated() {
		if (feed != null)
			return feed.getUpdated().toString();
		return null;
	}

	public String jsGet_title() {
		String title;
		if (isRssFeed) {
			title = rssFeed.getTitle().toString();
		} else {
			title = feed.getTitle();
		}
		return title;
	}

	public void jsSet_title(Object title) {
		if (title instanceof XML) {
			feed.setTitleAsXhtml(title.toString());
		} else {
			feed.setTitle(String.valueOf(title));
		}
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

	public void jsSet_category(Object category) {
		feed.addCategory(String.valueOf(category));
	}

	public NativeArray jsGet_category() {
		if (feed != null) {
			NativeArray nativeArray = new NativeArray(0);
			List<Category> list = feed.getCategories();
			int size = list.size();
			for (int i = 0; i < size; i++) {
				Category element = (Category) list.get(i);
				nativeArray.put(i, nativeArray, element.toString());
			}
			return nativeArray;
		}
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
			List<Link> list = feed.getLinks();
			int size = list.size();
			for (int i = 0; i < size; i++) {
				Link element = (Link) list.get(i);
				nativeArray.put(i, nativeArray, element.getHref().toString());
			}
			return nativeArray;
		}
		return null;
	}

	public String jsGet_language() {
		String language = feed.getLanguage();
		return language;
	}

	public void jsSet_language(Object language) {
		if (language instanceof String) {
			feed.setLanguage(language.toString());
		}
	}

	public String jsGet_alternateLink() {
		String alternateLink = feed.getAlternateLink().toString();
		return alternateLink;
	}

	public String jsGet_toString() {
		String entries = null;
		if (isRssFeed) {
			entries = rssFeed.getEntries().toString();
		} else {

			entries = feed.getEntries().toString();
		}
		return entries;
	}

	public static EntryHostObject jsFunction_getEntry(Context cx,
			Scriptable thisObj, Object[] arguments, Function funObj)
			throws ScriptException {
		int argsCount = arguments.length;
		if (argsCount != 1) {
			HostObjectUtil.invalidNumberOfArgs(HOST_OBJECT_NAME,
					HOST_OBJECT_NAME, argsCount, true);
		}
		if (!(arguments[0] instanceof String)) {
			HostObjectUtil.invalidArgsError(HOST_OBJECT_NAME, HOST_OBJECT_NAME,
					"1", "string", arguments[0], true);
		}

		Entry currentEntry = feed.getEntry(arguments[0].toString());

		EntryHostObject newAtomEntry = (EntryHostObject) cx.newObject(
				feedHostObject, "Entry", new Object[0]);
		newAtomEntry.setEntry(currentEntry);

		return newAtomEntry;
	}

	public EntryHostObject[] jsGet_entries() throws ScriptException {

		EntryHostObject[] retEntries = null;
		List tempEntries = null;
		// Retrieving the entries from the feed
		if (isRssFeed) {
			tempEntries = rssFeed.getEntries();
		} else {
			tempEntries = feed.getEntries();
		}

		Iterator tempEntryIterator = tempEntries.iterator();

		// Creating a list to store converted Entries
		ArrayList convertedEntries = new ArrayList();

		Entry currentEntry;

		// Converting the list of Abdera Entries to AtomEntryHostObjects
		while (tempEntryIterator.hasNext()) {
			currentEntry = (Entry) tempEntryIterator.next();
			// ctx = new Context();
			EntryHostObject newAtomEntry = (EntryHostObject) ctx.newObject(
					feedHostObject, "Entry", new Object[0]);
			newAtomEntry.setEntry(currentEntry);
			convertedEntries.add(newAtomEntry);
		}

		retEntries = new EntryHostObject[convertedEntries.size()];
		convertedEntries.toArray(retEntries);
		return retEntries;
	}

	public void jsSet_entries(Object entryList) throws ScriptException {

		if (entryList instanceof NativeArray) {
			NativeArray fields = (NativeArray) entryList;
			for (Object o : fields.getIds()) {

				int index = (Integer) o;
				Object nativeObject = fields.get(index, null);
				addEntry(nativeObject);
			}

		} else {

			throw new ScriptException("Invalid parameter");
		}
	}

	public void addEntry(Object entryObject) throws ScriptException {

		abdera = new Abdera();
		Factory factory = abdera.getFactory();
		entry = factory.newEntry();
		if (entryObject instanceof EntryHostObject) {
			EntryHostObject entryHostObject = (EntryHostObject) entryObject;
			entry = entryHostObject.getEntry();
			feed.addEntry(entry);
		} else if (entryObject instanceof NativeObject) {

			try {
				NativeObject nativeObject = (NativeObject) entryObject;

				ScriptableObject scriptableObject = (ScriptableObject) nativeObject;

				// author and authors processing
				Object authorProperty = ScriptableObject.getProperty(
						nativeObject, "author");
				if (authorProperty instanceof String) {

					entry.addAuthor((String) (authorProperty));
				}
				Object authorsProperty = ScriptableObject.getProperty(
						nativeObject, "authors");
				if (authorsProperty instanceof NativeArray) {
					NativeArray authorsPropertyArray = (NativeArray) authorsProperty;
					for (Object o1 : authorsPropertyArray.getIds()) {

						int indexx = (Integer) o1;
						String name = authorsPropertyArray.get(indexx, null)
								.toString();

						entry.addAuthor(name);
					}

				}

				// processing category
				Object categoryProperty = ScriptableObject.getProperty(
						nativeObject, "category");
				if (categoryProperty instanceof String) {

					entry.addCategory((String) (categoryProperty));
				}
				Object categoriesProperty = ScriptableObject.getProperty(
						nativeObject, "categories");
				if (categoriesProperty instanceof NativeArray) {
					NativeArray categoriesPropertyArray = (NativeArray) categoriesProperty;
					for (Object o1 : categoriesPropertyArray.getIds()) {

						int indexC = (Integer) o1;
						String name = categoriesPropertyArray.get(indexC, null)
								.toString();

						entry.addCategory(name);
					}

				}

				// process content
				Object content = ScriptableObject.getProperty(nativeObject,
						"content");
				if (content instanceof XML) {
					entry.setContentAsXhtml(content.toString());
				} else if (content instanceof String) {
					entry.setContent(content.toString());
				} else {
					throw new ScriptException("Unsupported Content");
				}

				// process contributor
				Object contributorProperty = ScriptableObject.getProperty(
						nativeObject, "contributor");
				if (contributorProperty instanceof String) {
					entry.addContributor(contributorProperty.toString());
				}
				Object contributorsProperty = ScriptableObject.getProperty(
						nativeObject, "contributors");
				if (contributorsProperty instanceof NativeArray) {
					NativeArray contributorsPropertyArray = (NativeArray) contributorsProperty;
					for (Object o1 : contributorsPropertyArray.getIds()) {

						int index = (Integer) o1;
						String name = contributorsPropertyArray
								.get(index, null).toString();

						entry.addContributor(name);
					}
				}

				// process id
				Object idProperty = ScriptableObject.getProperty(nativeObject,
						"id");
				if (idProperty instanceof String) {
					entry.setId((String) (idProperty));
				} else {
					entry.setId(FOMHelper.generateUuid());
				}

				// process link
				// TODO link object
				Object linkProperty = ScriptableObject.getProperty(
						nativeObject, "link");
				if (linkProperty instanceof String) {
					entry.addLink((String) (linkProperty));
				}
				Object linksProperty = ScriptableObject.getProperty(
						nativeObject, "links");
				if (linksProperty instanceof NativeArray) {
					NativeArray linksPropertyArray = (NativeArray) contributorsProperty;
					for (Object o1 : linksPropertyArray.getIds()) {

						int index = (Integer) o1;
						String name = linksPropertyArray.get(index, null)
								.toString();

						entry.addLink(name);
					}
				}

				// process published
				// TODO handle javascript date
				Object publishedProperty = ScriptableObject.getProperty(
						nativeObject, "published");
				if (publishedProperty instanceof String) {
					if (publishedProperty.equals("now")) {
						entry.setPublished(new Date(System.currentTimeMillis()));
					} else {
						entry.setPublished(publishedProperty.toString());
					}
				}

				// process rights
				Object rights = ScriptableObject.getProperty(nativeObject,
						"rights");
				if (rights instanceof XML) {
					entry.setRightsAsXhtml(rights.toString());
				} else if (rights instanceof String) {
					entry.setRights(rights.toString());
				}

				// process summary
				Object summary = ScriptableObject.getProperty(nativeObject,
						"summary");
				if (summary instanceof XML) {
					entry.setSummaryAsXhtml(summary.toString());
				} else if (summary instanceof String) {
					entry.setSummary(summary.toString());
				}

				// process title
				Object title = ScriptableObject.getProperty(nativeObject,
						"title");
				if (title instanceof XML) {
					entry.setTitleAsXhtml(title.toString());
				} else if (title instanceof String) {
					entry.setTitle(title.toString());
				} else {
					throw new ScriptException("An Entry MUST have a title.");
				}

				// process updated
				Object updated = ScriptableObject.getProperty(nativeObject,
						"updated");
				if (updated instanceof String) {
					if (updated.equals("now")) {
						entry.setUpdated(new Date(System.currentTimeMillis()));
					} else {
						entry.setUpdated((String) updated);
					}
				}

			} catch (IRISyntaxException e) {
				throw new ScriptException(e);
			}
		} else if (!(entryObject instanceof EntryHostObject)) {
			throw new ScriptException("Invalid parameter");
		}
		// for testing will be removed
		log.info("New Added Entry" + entry);
	}
	

    }