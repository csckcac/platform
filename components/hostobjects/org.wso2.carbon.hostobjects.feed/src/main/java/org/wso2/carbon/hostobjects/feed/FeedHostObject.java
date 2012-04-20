package org.wso2.carbon.hostobjects.feed;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.abdera.Abdera;
import org.apache.abdera.factory.Factory;
import org.apache.abdera.i18n.iri.IRISyntaxException;

import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
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
	private static final String HoST_OBJECT_NAME = "Feeder";
	private static boolean isRssFeed;
	private static Log log = LogFactory.getLog(FeedHostObject.class);

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
			HostObjectUtil.invalidNumberOfArgs(HoST_OBJECT_NAME, HoST_OBJECT_NAME,
					argsCount, true);
		}
		if (argsCount == 0) {
			Abdera abdera = new Abdera();
			Factory factory = abdera.getFactory();
			feed = factory.newFeed();
			feedHostObject = new FeedHostObject();
			return feedHostObject;
		}
		if (argsCount == 1) {
			if (!(args[0] instanceof String)) {
				HostObjectUtil.invalidArgsError(HoST_OBJECT_NAME, HoST_OBJECT_NAME,
						"1", "string", args[0], true);
			}

		
			Abdera abdera = new Abdera();
			Factory factory = abdera.getFactory();
			feed = factory.newFeed();
			feedHostObject = new FeedHostObject();
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

	public String jsGet_author() {
		String author;
		if (isRssFeed) {
			author = rssFeed.getAuthor().toString();
		} else {
			author = feed.getAuthor().toString();
		}
		return author;
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

	public String jsGet_language() {
		String language = feed.getLanguage();
		return language;
	}

	public String jsGet_alternateLink() {
		String alternateLink = feed.getAlternateLink().toString();
		return alternateLink;
	}

	public String jsGet_entriesAsString() {
		String entries = null;
		if (isRssFeed) {
			entries = rssFeed.getEntries().toString();
		} else {

			entries = feed.getEntries().toString();
		}
		return entries;
	}

	public static Entry jsFunction_getEntry(Context cx, Scriptable thisObj,
			Object[] arguments, Function funObj) throws ScriptException {
		Entry entryOut = null;
		if (arguments[0] instanceof String) {

			try {

				List<Entry> entry = feed.getEntries();
				Object[] entryArray = entry.toArray();
				int x = Integer.parseInt((String) arguments[0]);
				entryOut = (Entry) entryArray[x];

			} catch (IRISyntaxException e) {
				throw new ScriptException(e);
			}
		} else {
			throw new ScriptException("Invalid parameter");
		}

		return entryOut;
	}

	public Entry[] jsGet_entries() throws ScriptException {
		Entry[] entryOut = null;
		List<Entry> entry = null;
		Object[] entryArray = null;
		int entryCount = 0;
		try {

			if (isRssFeed) {
				List rssEntry = rssFeed.getEntries();
				entryArray = rssEntry.toArray();
				entryCount = rssEntry.size();
			} else {
				entry = feed.getEntries();
				entryArray = entry.toArray();
				entryCount = entry.size();
			}

			// Object[] entryArray = entry.toArray();
			entryOut = new Entry[entryCount];
			for (int i = 0; i < entryCount; i++) {
				entryOut[i] = (Entry) entryArray[i];
			}
		} catch (IRISyntaxException e) {
			throw new ScriptException(e);
		}

		return entryOut;
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
