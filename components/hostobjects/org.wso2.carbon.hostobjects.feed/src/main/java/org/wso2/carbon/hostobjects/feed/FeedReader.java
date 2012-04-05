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
package org.wso2.carbon.hostobjects.feed;

import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.wso2.carbon.scriptengine.exceptions.ScriptException;
import org.wso2.carbon.scriptengine.util.HostObjectUtil;

import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * The FeedReader is a generic reader of Atom and RSS feeds.
 */
public class FeedReader extends ScriptableObject {

    private SyndFeed feed;

    public void jsConstructor() {
        feed = new SyndFeedImpl();
    }

    public String getClassName() {
        return "FeedReader";
    }

    /**
     * Reads a RSS/Atom feed from a given url
     * <p/>
     * <pre>
     * eg:
     *    var reader = new FeedReader();
     *    var feed = new Feed();
     *    feed = reader.get("http://newsrss.bbc.co.uk/rss/newsonline_world_edition/front_page/rss.xml");
     *         or
     *    feed = reader.get("http://newsrss.bbc.co.uk/rss/newsonline_world_edition/front_page/rss.xml", "username", "password");
     * </pre>
     *
     * @param cx        Provides the current context
     * @param thisObj   Gives an instance of the js object from which this method was called
     * @param arguments The expected argument is a String representing the URL of the feed. Optionally a username and a password can be provided for basic authneticaiton.
     * @param funObj
     * @return A Feed HostObject containing the retrieved feed
     * @throws java.io.IOException
     */
    public static Feed jsFunction_get(Context cx, Scriptable thisObj, Object[] arguments,
                                      Function funObj) throws ScriptException {
        Feed retFeed;
        FeedReader feedReaderObject = (FeedReader) thisObj;
        if (arguments[0] instanceof String) {

        } else {
            throw new ScriptException("The parameter must be a string representing the Feeds URL");
        }
        String feedUrl = null;
        String username = null;
        String password = null;

        if ((arguments.length > 1) && (arguments.length < 4)) {
            feedUrl = (String) arguments[0];

            // We have a username password combo as well
            if ((arguments[1] == null) || (!(arguments[1] instanceof String))) {
                throw new ScriptException(
                        "The second argument for get function should be a string containing the username ");
            } else {
                username = (String) arguments[1];
            }
            if ((arguments[2] == null) || (!(arguments[2] instanceof String))) {
                throw new ScriptException(
                        "The third argument for get function should be a string containing the password ");
            } else {
                password = (String) arguments[2];
            }
        } else if (arguments.length == 1) {
            // We have only a URL
            feedUrl = (String) arguments[0];
        } else {
            throw new ScriptException(
                    "The get function should be called with either a single parameter which is the url to fetch the Feed from or three parameters, which are the url to fetch Feed from, the User Name and a Password for basic authentication.");
        }

        GetMethod method = new GetMethod(feedUrl);
        try {
            int statusCode = HostObjectUtil.getURL(feedUrl, username, password);
            if (statusCode != HttpStatus.SC_OK) {
                throw new ScriptException(
                        "An error occured while getting the feed at " + feedUrl +
                                ". Reason :" +
                                method.getStatusLine());
            }
            URL feedUri = new URL(feedUrl);
            InputStream response = feedUri.openStream();
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(response));
            retFeed = (Feed) cx.newObject(feedReaderObject, "Feed", new Object[0]);
            retFeed.setFeed(feed);
        } catch (SSLHandshakeException e) {
            if (e.getMessage().indexOf(
                    "sun.security.validator.ValidatorException: PKIX path building failed") > -1) {
                throw new ScriptException("An error occured while getting the feed since a " +
                        "trusted certificate was not found for the destination site \"" +
                        feedUrl + "\". You can manage your trusted certificates through " +
                        "the management UI", e);
            }
            throw new ScriptException("An error occured while getting the feed.", e);
        } catch (FeedException e) {
            throw new ScriptException("An error occured while getting the feed.", e);
        } catch (HttpException e) {
            throw new ScriptException("Fatal protocol violation: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new ScriptException("Fatal transport error: " + e.getMessage(), e);
        } finally {
            // Release the connection.
            method.releaseConnection();
        }

        return retFeed;
    }
}
