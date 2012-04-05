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

import com.sun.syndication.feed.module.mediarss.MediaEntryModuleImpl;
import com.sun.syndication.feed.module.mediarss.types.MediaContent;
import com.sun.syndication.feed.module.mediarss.types.Metadata;
import com.sun.syndication.feed.module.mediarss.types.Thumbnail;
import com.sun.syndication.feed.module.mediarss.types.UrlReference;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.wso2.carbon.scriptengine.exceptions.ScriptException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 * <p/>
 * This module allows the addition of Media Content to an Entry
 * <p/>
 * ex:
 * <pre>
 *      //Creating a Feed
 *      var mashupFeed = new Feed();
 *      mashupFeed.feedType = "rss_2.0";
 *      mashupFeed.title = "EarthShots.org";
 *      mashupFeed.description = "Photos from EarthShots.org";
 *      mashupFeed.link = "http://www.earthshots.org";
 * <p/>
 *      //Creating an Entry for the Feed
 *      var newEntry = new Entry();
 *      newEntry.title = "Capitol Peak Evening by Tad Bowman";
 *      newEntry.link = "http://www.earthshots.org/2007/10/capitol-peak-evening-by-tad-bowman/";
 *      newEntry.description = "&lt;a href=&quot;http://www.earthshots.org/2007/10/capitol-peak-evening-by-tad-bowman/&quot;&gt;&lt;img src=&quot;http://www.earthshots.org/photos/387.thumb.jpg&quot; alt=&quot;Capitol Peak Evening by Tad Bowman&quot; /&gt;&lt;/a&gt;";
 * <p/>
 *      //Creating a Media module to be added to the Entry
 *      var mediaModule = new MediaModule("http://www.earthshots.org/photos/387.jpg");
 *      mediaModule.copyright = "2007 Tad Bowman";
 *      mediaModule.type = "image/jpeg";
 *      mediaModule.thumbnail = "http://www.earthshots.org/photos/387.thumb.jpg";
 * <p/>
 *      //Adding the Media Module to the Entry
 *      newEntry.addMediaModule(mediaModule);
 * <p/>
 *      //Adding the Entry to the Feed
 *      mashupFeed.insertEntry(newEntry);
 *      </pre>
 * </p>
 */

public class MediaModule extends ScriptableObject {

    MediaEntryModuleImpl module;


    /**
     * @param args A string reprepsentation of the media object URI is mandatory
     */
    public static Scriptable jsConstructor(Context cx, Object[] args, Function ctorObj,
                                           boolean inNewExpr) throws ScriptException {
        MediaModule newMediaModule = null;

        //The first argument is a string representing the URI of the media
        if (args[0] instanceof String) {
            MediaEntryModuleImpl newModule = new MediaEntryModuleImpl();
            MediaContent[] newContents = new MediaContent[1];
            MediaContent newMediaItem = null;
            try {
                newMediaItem = new MediaContent(new UrlReference((String) args[0]));
            } catch (URISyntaxException e) {
                throw new ScriptException(e.getMessage());
            }
            newContents[0] = newMediaItem;
            Metadata newMetaData = new Metadata();
            newMediaItem.setMetadata(newMetaData);
            newModule.setMediaContents(newContents);

            newMediaModule = new MediaModule();
            newMediaModule.module = newModule;
        } else {
            throw new ScriptException(
                    "The first argument for the MediaModule constructor should be a String representation of the Media Content URI.");
        }

        return newMediaModule;
    }

    public String getClassName() {
        return "MediaModule";
    }

    /**
     * <p/>
     * <media:copyright>
     * Copyright information for media object.
     * </p>
     *
     * @param copyRight copyright information as a String
     */
    public void jsSet_copyright(String copyRight) {
        module.getMediaContents()[0].getMetadata().setCopyright(copyRight);
    }

    /**
     * <p/>
     * <media:copyright>
     * Copyright information for media object.
     * </p>
     *
     * @return copyright information as a String
     */
    public Object jsGet_copyright() {
        return module.getMediaContents()[0].getMetadata().getCopyright();
    }

    /**
     * <p>type is the standard MIME type of the object. It is an optional attribute.</p>
     *
     * @param type as String
     */
    public void jsSet_type(String type) {
        module.getMediaContents()[0].setType(type);
    }

    /**
     * <p>type is the standard MIME type of the object. It is an optional attribute.</p>
     *
     * @return type as String
     */
    public Object jsGet_type() {
        return module.getMediaContents()[0].getType();
    }

    /**
     * <media:thumbnail>
     * <p/>
     * <p/>
     * Allows particular images to be used as representative images for the media object. If multiple thumbnails are included,
     * it is assumed that the images are in order of importance.
     * </p>
     *
     * @param uri as String
     * @throws ScriptException if the given URI is malformed
     */
    public void jsSet_thumbnail(String uri) throws ScriptException {
        Thumbnail[] thumbnails = module.getMediaContents()[0].getMetadata().getThumbnail();
        ArrayList tempThumbs = new ArrayList();

        //Adding existing thumbnails to the array list
        if (thumbnails.length > 0) {
            for (int x = 0; x < thumbnails.length; x++) {
                tempThumbs.add(thumbnails[x]);
            }
        }

        //Adding the new thumbnail to the collection
        try {
            tempThumbs.add(new Thumbnail(new URI(uri)));
        } catch (URISyntaxException e) {
            throw new ScriptException(e);
        }

        thumbnails = new Thumbnail[tempThumbs.size()];
        tempThumbs.toArray(thumbnails);

        //Updating the media item with the new thumbnail
        module.getMediaContents()[0].getMetadata().setThumbnail(thumbnails);
    }

    /**
     * <media:thumbnail>
     * <p/>
     * <p/>
     * Allows particular images to be used as representative images for the media object. If multiple thumbnails are included,
     * it is assumed that the images are in order of importance.
     * </p>
     *
     * @return a String array of thumbnail URIs
     */
    public String[] jsGet_thumbnail() {
        Thumbnail[] thumbs = module.getMediaContents()[0].getMetadata().getThumbnail();
        String[] retThumbs = new String[thumbs.length];

        for (int x = 0; x < thumbs.length; x++) {
            retThumbs[x] = thumbs[x].getUrl().toString();
        }

        return retThumbs;
    }

    /**
     * <p>fileSize is the number of bytes of the media object.</p>
     *
     * @param fileSize in Bytes
     */
    public void jsSet_fileSize(Long fileSize) {
        module.getMediaContents()[0].setFileSize(fileSize);
    }

    /**
     * <p>fileSize is the number of bytes of the media object.</p>
     *
     * @return the size of the file in bytes
     */
    public Long jsGet_fileSize() {
        return module.getMediaContents()[0].getFileSize();
    }

    /**
     * @param height is the height of the media object as an Integer.
     */
    public void jsSet_height(Integer height) {
        module.getMediaContents()[0].setHeight(height);
    }

    /**
     * @return the height of the media object as an Integer.
     */
    public Integer jdGet_height() {
        return module.getMediaContents()[0].getHeight();
    }

    /**
     * <p>width is the width of the media object. It is an optional attribute.</p>
     */
    public void jsSet_width(Integer width) {
        module.getMediaContents()[0].setWidth(width);
    }

    /**
     * <p>width is the width of the media object. It is an optional attribute.</p>
     */
    public Integer jsGet_width() {
        return module.getMediaContents()[0].getWidth();
    }

    /**
     * <media:description>
     * Short description describing the media object typically a sentence in length.
     *
     * @param desc as String
     */
    public void jsSet_description(String desc) {
        module.getMediaContents()[0].getMetadata().setDescription(desc);
    }

    /**
     * <media:description>
     * Short description describing the media object typically a sentence in length.
     *
     * @return description as String
     */
    public String jsGet_description() {
        return module.getMediaContents()[0].getMetadata().getDescription();
    }

    /**
     * <p>lang is the primary language encapsulated in the media object. Language codes possible are detailed in RFC 3066.
     * This attribute is used similar to the xml:lang attribute detailed in the XML 1.0 Specification (Third Edition).
     * <p/>
     *
     * @param lang as String
     */
    public void jsSet_language(String lang) {
        module.getMediaContents()[0].setLanguage(lang);
    }

    /**
     * <p>lang is the primary language encapsulated in the media object. Language codes possible are detailed in RFC 3066.
     * This attribute is used similar to the xml:lang attribute detailed in the XML 1.0 Specification (Third Edition).
     * <p/>
     *
     * @return language as String
     */
    public String jsGet_language() {
        return module.getMediaContents()[0].getLanguage();
    }

    /**
     * <p>bitrate is the kilobits per second rate of media.</p>
     *
     * @param rate as Float
     */
    public void jsSet_bitrate(Float rate) {
        module.getMediaContents()[0].setBitrate(rate);
    }

    /**
     * <p>bitrate is the kilobits per second rate of media.</p>
     *
     * @return bitrate as Float
     */
    public Float jsGet_bitrate() {
        return module.getMediaContents()[0].getBitrate();
    }

    /**
     * <p>framerate is the number of frames per second for the media object. </p>
     *
     * @param rate as Float
     */
    public void jsSet_framerate(Float rate) {
        module.getMediaContents()[0].setFramerate(rate);
    }

    /**
     * <p>framerate is the number of frames per second for the media object. </p>
     *
     * @return framerate as Float
     */
    public Float jsGet_framerate() {
        return module.getMediaContents()[0].getFramerate();
    }

    /**
     * <p>samplingrate is the number of samples per second taken to create the media object. It is expressed in thousands of samples per second (kHz).</p>
     *
     * @param rate as Float
     */
    public void jsSet_samplingrate(Float rate) {
        module.getMediaContents()[0].setSamplingrate(rate);
    }

    /**
     * <p>samplingrate is the number of samples per second taken to create the media object. It is expressed in thousands of samples per second (kHz).</p>
     *
     * @return samplingrate as Float
     */
    public Float jsGet_samplingrate() {
        return module.getMediaContents()[0].getSamplingrate();
    }

    /**
     * <p>channels is number of audio channels in the media object.</p>
     *
     * @param channles as Integer
     */
    public void jsSet_channels(Integer channles) {
        module.getMediaContents()[0].setAudioChannels(channles);
    }

    /**
     * <p>channels is number of audio channels in the media object.</p>
     *
     * @return The number of channles as Integer
     */
    public Integer jsGet_channels() {
        return module.getMediaContents()[0].getAudioChannels();
    }

    /**
     * <p>duration is the number of seconds the media object plays. </p>
     *
     * @param duration
     */
    public void jsSet_duration(Long duration) {
        module.getMediaContents()[0].setDuration(duration);
    }

    /**
     * <p>duration is the number of seconds the media object plays. </p>
     *
     * @return duration as Long
     */
    public Long jsGet_duration() {
        return module.getMediaContents()[0].getDuration();
    }
}
