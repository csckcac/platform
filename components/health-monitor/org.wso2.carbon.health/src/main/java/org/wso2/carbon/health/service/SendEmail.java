/*
 *  Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.health.service;

import java.util.*;
import java.io.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

public class SendEmail {
    /**
     * Sends an email to the receiver
     * @param to Receiver's email address
     * @param from Sender's email address or name
     * @param hostMailServer mail server
     * @param pathToAttachment path to the zip attachment
     */
    public SendEmail(String to, String from, String hostMailServer, String pathToAttachment){
        // Create properties for the Session
        Properties props = new Properties();

        // If using static Transport.send(),
        // need to specify the mail server here
        props.put("mail.smtp.host", hostMailServer);
        // To see what is going on behind the scene
        props.put("mail.debug", "true");

        // Get a session
        Session session = Session.getInstance(props);

        try {
            // Get a Transport object to send e-mail
            Transport bus = session.getTransport("smtp");

            // Connect only once here
            // Transport.send() disconnects after each send
            // Usually, no username and password is required for SMTP
            bus.connect();
            //bus.connect("smtpserver.yourisp.net", "username", "password");

            // Instantiate a message
            Message msg = new MimeMessage(session);

            // Set message attributes
            msg.setFrom(new InternetAddress(from));
            InternetAddress[] address = {new InternetAddress(to)};
            msg.setRecipients(Message.RecipientType.TO, address);
            // Parse a comma-separated list of email addresses. Be strict.
            msg.setRecipients(Message.RecipientType.CC,
                                InternetAddress.parse(to, true));
            // Parse comma/space-separated list. Cut some slack.
            msg.setRecipients(Message.RecipientType.BCC,
                                InternetAddress.parse(to, false));

            msg.setSubject("[Carbon-Jira] Debug level error found");
            msg.setSentDate(new Date());

            setFileAsAttachment(msg, pathToAttachment);
            msg.saveChanges();
            bus.sendMessage(msg, address);

            bus.close();
         }
        catch (MessagingException mex) {
            // Prints all nested (chained) exceptions as well
            mex.printStackTrace();
            // How to access nested exceptions
            while (mex.getNextException() != null) {
                // Get next exception in chain
                Exception ex = mex.getNextException();
                ex.printStackTrace();
                if (!(ex instanceof MessagingException)) break;
                else mex = (MessagingException)ex;
            }
        }
    }

    /**
     * A simple, single-part text/plain e-mail.
     * @param msg message to be sent
     * @throws MessagingException
     */
    public static void setTextContent(Message msg) throws MessagingException {
            // Set message content
            String mytxt = "This is a ZipDemo of sending a " +
                            "plain text e-mail through Java.\n" +
                            "Here is line 2.";
            msg.setText(mytxt);

            // Alternate form
            msg.setContent(mytxt, "text/plain");

    }

    /**
     * A simple multipart/mixed e-mail. Both body parts are text/plain.
     * @param msg message to be sent
     * @throws MessagingException
     */
    public static void setMultipartContent(Message msg) throws MessagingException {
        // Create and fill first part
        MimeBodyPart p1 = new MimeBodyPart();
        p1.setText("This is part one of a ZipDemo multipart e-mail.");

        // Create and fill second part
        MimeBodyPart p2 = new MimeBodyPart();
        // Here is how to set a charset on textual content
        p2.setText("This is the second part", "us-ascii");

        // Create the Multipart.  Add BodyParts to it.
        Multipart mp = new MimeMultipart();
        mp.addBodyPart(p1);
        mp.addBodyPart(p2);

        // Set Multipart as the message's content
        msg.setContent(mp);
    }

    /**
     * Set a file as an attachment.  Uses JAF FileDataSource.
     * @param msg message to be sent
     * @param filename file to be attached to the email
     * @throws MessagingException
     */
    public static void setFileAsAttachment(Message msg, String filename)
             throws MessagingException {

        // Create and fill first part
        MimeBodyPart p1 = new MimeBodyPart();
        p1.setText("This jira was generated by Carbon-Health component");

        // Create second part
        MimeBodyPart p2 = new MimeBodyPart();

        // Put a file in the second part
        FileDataSource fds = new FileDataSource(filename);
        p2.setDataHandler(new DataHandler(fds));
        p2.setFileName(fds.getName());

        // Create the Multipart.  Add BodyParts to it.
        Multipart mp = new MimeMultipart();
        mp.addBodyPart(p1);
        mp.addBodyPart(p2);

        // Set Multipart as the message's content
        msg.setContent(mp);
    }

    /**
     * Inner class to act as a JAF datasource to send HTML e-mail content
     */
    static class HTMLDataSource implements DataSource {
        private String html;

        public HTMLDataSource(String htmlString) {
            html = htmlString;
        }

        /**
         * A new stream must be returned each time.
         * @return Return html string in an InputStream
         * @throws IOException
         */
        public InputStream getInputStream() throws IOException {
            if (html == null) throw new IOException("Null HTML");
            return new ByteArrayInputStream(html.getBytes());
        }

        public OutputStream getOutputStream() throws IOException {
            throw new IOException("This DataHandler cannot write HTML");
        }

        public String getContentType() {
            return "text/html";
        }

        public String getName() {
            return "JAF text/html dataSource to send e-mail only";
        }
    }

}