/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.salesforce.webapp.gspread;

import com.google.gdata.client.GoogleAuthTokenFactory.UserToken;
import com.google.gdata.client.docs.DocsService;
import com.google.gdata.client.spreadsheet.SpreadsheetQuery;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.docs.DocumentListEntry;
import com.google.gdata.data.spreadsheet.*;
import com.google.gdata.util.ServiceException;
import org.wso2.salesforce.webapp.constants.SalesForceWebAppConstants;
import org.wso2.salesforce.webapp.salesforce.entity.Case;
import org.wso2.salesforce.webapp.salesforce.entity.Contact;
import org.wso2.salesforce.webapp.salesforce.entity.Lead;
import org.wso2.salesforce.webapp.salesforce.entity.Opportunity;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Handles google spreadsheet related data manipulative functionalities.
 */
public class GspreadManager {
    private static SpreadsheetService spreadsheetService;
    private WorksheetFeed worksheetFeed;
    private List<String> existingWorkSheets = new ArrayList<String>();
    private boolean isDefaultWSAvailable = false;

    /**
     * Initializes the Spreadsheet which is used to store data
     *
     * @param username google username
     * @param password google password
     * @param title    title
     * @param category data category
     * @throws IOException      IOException
     * @throws ServiceException ServiceException
     */
    public void initializeSpreadSheet(String username, String password, String title,
                                      String category) throws IOException, ServiceException {
        //Creates a SpreadSheetService with Application Name
        spreadsheetService = new SpreadsheetService(SalesForceWebAppConstants.APP_NAME);
        spreadsheetService.setCookieManager(null);
        spreadsheetService.setUserCredentials(username, password);
        spreadsheetService.setUserToken(((UserToken) spreadsheetService.getAuthTokenFactory().
                getAuthToken()).getValue());

        //Initializes the desired spreadsheet
        URL feedURL = new URL(SalesForceWebAppConstants.SPREADSHEET_FEED);
        SpreadsheetQuery spreadsheetQuery = new SpreadsheetQuery(feedURL);
        //Getting the Desired Spreadsheet
        spreadsheetQuery.setTitleQuery(title);
        spreadsheetQuery.setTitleExact(true);  //Exact match upon the aforementioned name
        SpreadsheetFeed spreadsheetFeed = spreadsheetService.getFeed(spreadsheetQuery,
                SpreadsheetFeed.class);
        List<SpreadsheetEntry> spreadsheetEntryList = spreadsheetFeed.getEntries();

        //Check the Spreadsheet is already created earlier
        if (spreadsheetEntryList.size() > 0) {
            SpreadsheetEntry spreadsheetEntry = spreadsheetEntryList.get(0);
            //Updates the existingWorkSheets list
            updateExistingWorkSheetsList(spreadsheetEntry);
            //Adds the worksheet if not existing
            this.createNewWorkSheets(spreadsheetEntry, category);
            //Complete WorksheetFeed for the Spreadsheet
            worksheetFeed = spreadsheetService.getFeed(spreadsheetEntry.getWorksheetFeedUrl(),
                    WorksheetFeed.class);
            this.addHeadersToWorkSheets(category);
        }
        //If the Spreadsheet dose not exist create a new Spreadsheet
        else {
            //Creates a new Spreadsheet with the given name
            this.createNewSpreadSheet(title, username, password);
            //Adding work sheets to the newly created Spreadsheet
            spreadsheetFeed = spreadsheetService.getFeed(spreadsheetQuery,
                    SpreadsheetFeed.class);
            spreadsheetEntryList = spreadsheetFeed.getEntries();
            SpreadsheetEntry spreadsheetEntry = spreadsheetEntryList.get(0);
            this.isDefaultWSAvailable = true;
            //Deleting default worksheet added at the initialization of a particular spreadsheet.
            this.createNewWorkSheets(spreadsheetEntry, category);
            worksheetFeed = spreadsheetService.getFeed(spreadsheetEntry.getWorksheetFeedUrl(),
                    WorksheetFeed.class);
            this.addHeadersToWorkSheets(category);
        }
    }

    /**
     * Updates the existingWorkSheets list by the values obtained from the Spreadsheet
     *
     * @param spreadsheetEntry The Spreadsheet
     * @throws IOException      IOException
     * @throws ServiceException ServiceException
     */
    private void updateExistingWorkSheetsList(SpreadsheetEntry spreadsheetEntry)
            throws IOException, ServiceException {
        worksheetFeed = spreadsheetService.getFeed(spreadsheetEntry.getWorksheetFeedUrl(),
                WorksheetFeed.class);
        for (WorksheetEntry worksheetEntry : worksheetFeed.getEntries()) {
            String currTitle = worksheetEntry.getTitle().getPlainText();
            existingWorkSheets.add(currTitle);
        }
    }

    /**
     * Adds headers to the worksheets
     *
     * @param category Data category
     * @throws IOException      IOException
     * @throws ServiceException ServiceException
     */
    private void addHeadersToWorkSheets(String category) throws IOException, ServiceException {
        //Iterates through each worksheet
        for (WorksheetEntry worksheetEntry : worksheetFeed.getEntries()) {
            String currTitle = worksheetEntry.getTitle().getPlainText();
            //If the work sheet name matches "Contacts"
            if (currTitle.equals(SalesForceWebAppConstants.CONTACTS)) {
                //Getting a CellFeed which is used to add header row
                URL cellFeedUrl = worksheetEntry.getCellFeedUrl();
                CellFeed cellFeed = spreadsheetService.getFeed(cellFeedUrl,
                        CellFeed.class);
                //Column Headers
                String[] columnHeaders = {"ID", "FirstName", "LastName", "Title", "Phone", "Email"};
                //Creates a CellEntry with the Column Header and adds it to the first Row
                CellEntry cellEntry;
                for (int i = 0; i < columnHeaders.length; i++) {
                    cellEntry = new CellEntry(1, i + 1, columnHeaders[i]);
                    cellFeed.insert(cellEntry);
                }
            }
            //If the work sheet name matches "Opportunities"
            else if (currTitle.equals(SalesForceWebAppConstants.OPPORTUNITIES)) {
                //Getting a CellFeed which is used to add header row
                URL cellFeedUrl = worksheetEntry.getCellFeedUrl();
                CellFeed cellFeed = spreadsheetService.getFeed(cellFeedUrl,
                        CellFeed.class);
                //Column Headers
                String[] columnHeaders = {"ID", "Name", "Stage", "Amount", "CloseDate"};
                //Creates a CellEntry with the Column Header and adds it to the first Row
                CellEntry cellEntry;
                for (int i = 0; i < columnHeaders.length; i++) {
                    cellEntry = new CellEntry(1, i + 1, columnHeaders[i]);
                    cellFeed.insert(cellEntry);
                }
            }
            //If the work sheet name matches "Cases"
            else if (currTitle.equals(SalesForceWebAppConstants.CASES)) {
                //Column Headers
                URL cellFeedUrl = worksheetEntry.getCellFeedUrl();
                CellFeed cellFeed = spreadsheetService.getFeed(cellFeedUrl,
                        CellFeed.class);
                String[] columnHeaders = {"CaseNumber", "ContactName", "Subject", "Priority",
                        "Status"};
                //Creates a CellEntry with the Column Header and adds it to the first Row
                CellEntry cellEntry;
                for (int i = 0; i < columnHeaders.length; i++) {
                    cellEntry = new CellEntry(1, i + 1, columnHeaders[i]);
                    cellFeed.insert(cellEntry);
                }
            } else if (currTitle.equals(SalesForceWebAppConstants.LEADS)) {
                //Column Headers
                URL cellFeedUrl = worksheetEntry.getCellFeedUrl();
                CellFeed cellFeed = spreadsheetService.getFeed(cellFeedUrl,
                        CellFeed.class);
                String[] columnHeaders = {"ID", "FirstName", "LastName", "Title", "Phone", "Email"};
                //Creates a CellEntry with the Column Header and adds it to the first Row
                CellEntry cellEntry;
                for (int i = 0; i < columnHeaders.length; i++) {
                    cellEntry = new CellEntry(1, i + 1, columnHeaders[i]);
                    cellFeed.insert(cellEntry);
                }
            }
        }
    }


    /**
     * Creates New Work Sheets for the given Spreadsheet
     *
     * @param spreadsheetEntry A Spread Sheet Entry
     * @param category         data category
     * @throws IOException      IOException
     * @throws ServiceException ServiceException
     */
    private void createNewWorkSheets(SpreadsheetEntry spreadsheetEntry, String category)
            throws IOException, ServiceException {
        if (isDefaultWSAvailable) {
            this.updateDefaultWorksheet(spreadsheetEntry, category);
            return;
        }

        if (!this.existingWorkSheets.contains(category)) {
            URL worksheetFeedUrl = spreadsheetEntry.getWorksheetFeedUrl();
            if ("Contacts".equals(category)) {
                //Create Contacts WorkSheet
                this.insertNewWorkSheetToSpreadsheet(worksheetFeedUrl,
                        SalesForceWebAppConstants.CONTACTS);
            }
            if ("Opportunities".equals(category)) {
                //Create Opportunities WorkSheet
                this.insertNewWorkSheetToSpreadsheet(worksheetFeedUrl,
                        SalesForceWebAppConstants.OPPORTUNITIES);
            }
            if ("Cases".equals(category)) {
                //Create Cases WorkSheet
                this.insertNewWorkSheetToSpreadsheet(worksheetFeedUrl,
                        SalesForceWebAppConstants.CASES);
            }
            if ("Leads".equals(category)) {
                //Create Cases WorkSheet
                this.insertNewWorkSheetToSpreadsheet(worksheetFeedUrl,
                        SalesForceWebAppConstants.LEADS);
            }
            if ("All".equals(category)) {
                if (!existingWorkSheets.contains(SalesForceWebAppConstants.LEADS)) {
                    this.insertNewWorkSheetToSpreadsheet(worksheetFeedUrl,
                            SalesForceWebAppConstants.LEADS);
                }
                if (!existingWorkSheets.contains(SalesForceWebAppConstants.CONTACTS)) {
                    this.insertNewWorkSheetToSpreadsheet(worksheetFeedUrl,
                            SalesForceWebAppConstants.CONTACTS);
                }
                if (!existingWorkSheets.contains(SalesForceWebAppConstants.OPPORTUNITIES)) {
                    this.insertNewWorkSheetToSpreadsheet(worksheetFeedUrl,
                            SalesForceWebAppConstants.OPPORTUNITIES);
                }
                if (!existingWorkSheets.contains(SalesForceWebAppConstants.CASES)) {
                    this.insertNewWorkSheetToSpreadsheet(worksheetFeedUrl,
                            SalesForceWebAppConstants.CASES);
                }
            }
            this.existingWorkSheets.add(category);
        }
    }

    private void updateDefaultWorksheet(SpreadsheetEntry ssEntry, String category) throws IOException,
            ServiceException {
        WorksheetEntry wcEntry = ssEntry.getDefaultWorksheet();
        wcEntry.setTitle(new PlainTextConstruct(category));
        wcEntry.setRowCount(SalesForceWebAppConstants.ROWS_TO_ADD_TO_NEW_SPREADSHEET);
        wcEntry.setColCount(SalesForceWebAppConstants.COLUMNS_TO_ADD_TO_NEW_SPREADSHEET);
        wcEntry.update();
        this.isDefaultWSAvailable = false;
    }

    /**
     * Inserts WorkSheets to the Spreadsheet
     *
     * @param worksheetFeedUrl The worksheetFeedUrl of the Spreadsheet
     * @param title            Title of the Worksheet
     * @throws IOException      IOException
     * @throws ServiceException ServiceException
     */

    private void insertNewWorkSheetToSpreadsheet(URL worksheetFeedUrl, String title)
            throws IOException, ServiceException {
        WorksheetEntry entry = new WorksheetEntry();
        entry.setTitle(new PlainTextConstruct(title));
        entry.setRowCount(SalesForceWebAppConstants.ROWS_TO_ADD_TO_NEW_SPREADSHEET);
        entry.setColCount(SalesForceWebAppConstants.COLUMNS_TO_ADD_TO_NEW_SPREADSHEET);
        spreadsheetService.insert(worksheetFeedUrl, entry);
    }

    /**
     * Creates a new Spreadsheet using Google Documents List Data API
     *
     * @param title    Title of the Spreadsheet
     * @param username Username
     * @param password Password
     * @throws IOException      IOException
     * @throws ServiceException ServiceException
     */
    public void createNewSpreadSheet(String title, String username, String password)
            throws IOException, ServiceException {
        //Creating a DocsService with the App name
        DocsService client = new DocsService(SalesForceWebAppConstants.APP_NAME);
        //Setting Credentials
        client.setUserCredentials(username, password);
        //Creates a new Spreadsheet entry
        DocumentListEntry newEntry = new com.google.gdata.data.docs.SpreadsheetEntry();
        //Setting Title of the Spreadsheet
        newEntry.setTitle(new PlainTextConstruct(title));
        //Inserts to the docs
        client.insert(new URL(SalesForceWebAppConstants.DOCS_FEED), newEntry);
    }

    /**
     * Updates the Contacts Worksheet
     *
     * @param contacts A Vector containing Contact Objects
     * @throws IOException      IOException
     * @throws ServiceException ServiceException
     */
    public void updateContactsWorkSheet(Vector<Contact> contacts)
            throws IOException, ServiceException {
        //Loop through all the Worksheets
        for (WorksheetEntry worksheetEntry : worksheetFeed.getEntries()) {
            String currTitle = worksheetEntry.getTitle().getPlainText();
            //If the Current Worksheet is "Contacts"
            if (currTitle.equals(SalesForceWebAppConstants.CONTACTS)) {
                //Getting the ListFeed of the worksheet
                URL listFeedUrl = worksheetEntry.getListFeedUrl();
                ListFeed feed = spreadsheetService.getFeed(listFeedUrl, ListFeed.class);
                //Compares with the Values which have been inserted to the Spreadsheet earlier
                for (ListEntry entry : feed.getEntries()) {
                    for (int i = 0; i < contacts.size(); i++) {
                        //Checks the value of "ID" field for duplicate values
                        if (entry.getCustomElements().getValue("ID").equals(
                                contacts.get(i).getId())) {
                            //If the values are already in the spreadsheet remove them from the
                            //vector which is used to write data to spreadsheet
                            contacts.remove(i);
                        }
                    }
                }
                //Writes the remaining contacts from the earlier filtering into the Spreadsheet
                for (Contact contact : contacts) {
                    //Creates a new ListEntry and populates it with values
                    ListEntry newEntry = new ListEntry();
                    //Values are added with Column Header and Value
                    newEntry.getCustomElements().setValueLocal("ID", contact.getId());
                    newEntry.getCustomElements().setValueLocal("FirstName", contact.getFirstName());
                    newEntry.getCustomElements().setValueLocal("LastName", contact.getLastName());
                    newEntry.getCustomElements().setValueLocal("Title", contact.getTitle());
                    newEntry.getCustomElements().setValueLocal("Phone", contact.getPhone());
                    newEntry.getCustomElements().setValueLocal("Email", contact.getEmail());
                    //Inserts to the Spreadsheet
                    spreadsheetService.insert(listFeedUrl, newEntry);
                }
            }
        }

    }

    /**
     * Updates the Opportunities worksheet
     *
     * @param opportunities A Vector containing Opportunity Objects
     * @throws IOException      IOException
     * @throws ServiceException ServiceException
     */
    public void updateOpportunitiesWorkSheet(Vector<Opportunity> opportunities)
            throws IOException, ServiceException {
        //Loop through all the Worksheets
        for (WorksheetEntry worksheetEntry : worksheetFeed.getEntries()) {
            String currTitle = worksheetEntry.getTitle().getPlainText();
            //If the Current Worksheet is "Opportunities"
            if (currTitle.equals(SalesForceWebAppConstants.OPPORTUNITIES)) {
                //Getting the ListFeed of the worksheet
                URL listFeedUrl = worksheetEntry.getListFeedUrl();
                ListFeed feed = spreadsheetService.getFeed(listFeedUrl, ListFeed.class);
                //Compares with the Values which have been inserted to the Spreadsheet earlier
                for (ListEntry entry : feed.getEntries()) {
                    for (int i = 0; i < opportunities.size(); i++) {
                        //Checks the value of "ID" field for duplicate values
                        if (entry.getCustomElements().getValue("ID").equals(
                                opportunities.get(i).getId())) {
                            //If the values are already in the spreadsheet remove them from the
                            //vector which is used to write data to spreadsheet
                            opportunities.remove(i);
                        }
                    }
                }
                //Writes the remaining contacts from the earlier filtering into the Spreadsheet
                for (Opportunity opportunity : opportunities) {
                    //Creates a new ListEntry and populates it with values
                    ListEntry newEntry = new ListEntry();
                    //Values are added with Column Header and Value
                    newEntry.getCustomElements().setValueLocal("ID", opportunity.getId());
                    newEntry.getCustomElements().setValueLocal("Name", opportunity.getName());
                    newEntry.getCustomElements().setValueLocal("Stage", opportunity.getStage());
                    newEntry.getCustomElements().setValueLocal("Amount", opportunity.getAmount());
                    newEntry.getCustomElements().setValueLocal("CloseDate",
                            opportunity.getCloseDate());
                    //Inserts to the Spreadsheet
                    spreadsheetService.insert(listFeedUrl, newEntry);
                }
            }
        }
    }

    public void updateCasesWorkSheet(Vector<Case> cases)
            throws IOException, ServiceException {
        //Loop through all the Worksheets
        for (WorksheetEntry worksheetEntry : worksheetFeed.getEntries()) {
            String currTitle = worksheetEntry.getTitle().getPlainText();
            //If the Current Worksheet is "Cases"
            if (currTitle.equals(SalesForceWebAppConstants.CASES)) {
                //Getting the ListFeed of the worksheet
                URL listFeedUrl = worksheetEntry.getListFeedUrl();
                ListFeed feed = spreadsheetService.getFeed(listFeedUrl, ListFeed.class);
                //Compares with the Values which have been inserted to the Spreadsheet earlier
                for (ListEntry entry : feed.getEntries()) {
                    for (int i = 0; i < cases.size(); i++) {
                        String noInSheet = entry.getCustomElements().getValue("CaseNumber");
                        //Values for CaseNumber in the SalesForce contains 8 digits while in
                        //Spreadsheet zeroes in the front are removed. Therefore for comparison
                        //necessary number of zeroes are added to the value from the Spreadsheet
                        if (noInSheet.length() == 4) {
                            noInSheet = "0000" + noInSheet;
                        } else if (noInSheet.length() == 3) {
                            noInSheet = "000" + noInSheet;
                        } else if (noInSheet.length() == 2) {
                            noInSheet = "00" + noInSheet;
                        } else if (noInSheet.length() == 1) {
                            noInSheet = "0" + noInSheet;
                        }
                        //Checks the value of "ID" field for duplicate values
                        if (noInSheet.equals(cases.get(i).getCaseNo())) {
                            //If the values are already in the spreadsheet remove them from the
                            //vector which is used to write data to spreadsheet
                            cases.remove(i);
                        }
                    }
                }
                //Writes the remaining contacts from the earlier filtering into the Spreadsheet
                for (Case c : cases) {
                    //Creates a new ListEntry and populates it with values
                    ListEntry newEntry = new ListEntry();
                    //Values are added with Column Header and Value
                    newEntry.getCustomElements().setValueLocal("CaseNumber", c.getCaseNo());
                    newEntry.getCustomElements().setValueLocal("ContactName", c.getContactName());
                    newEntry.getCustomElements().setValueLocal("Subject", c.getSubject());
                    newEntry.getCustomElements().setValueLocal("Priority", c.getPriority());
                    newEntry.getCustomElements().setValueLocal("Status", c.getStatus());
                    //Inserts to the Spreadsheet
                    spreadsheetService.insert(listFeedUrl, newEntry);
                }
            }
        }
    }

    public void updateLeadWorkSheet(Vector<Lead> leads) throws IOException, ServiceException {
        //Loop through all the Worksheets
        for (WorksheetEntry worksheetEntry : worksheetFeed.getEntries()) {
            String currTitle = worksheetEntry.getTitle().getPlainText();
            //If the Current Worksheet is "Leads"
            if (currTitle.equals(SalesForceWebAppConstants.LEADS)) {
                //Getting the ListFeed of the worksheet
                URL listFeedUrl = worksheetEntry.getListFeedUrl();
                ListFeed feed = spreadsheetService.getFeed(listFeedUrl, ListFeed.class);
                //Compares with the Values which have been inserted to the Spreadsheet earlier
                for (ListEntry entry : feed.getEntries()) {
                    for (int i = 0; i < leads.size(); i++) {
                        //Checks the value of "ID" field for duplicate values
                        if (entry.getCustomElements().getValue("ID").equals(
                                leads.get(i).getId())) {
                            //If the values are already in the spreadsheet remove them from the
                            //vector which is used to write data to spreadsheet
                            leads.remove(i);
                        }
                    }
                }
                //Writes the remaining contacts from the earlier filtering into the Spreadsheet
                for (Lead lead : leads) {
                    //Creates a new ListEntry and populates it with values
                    ListEntry newEntry = new ListEntry();
                    //Values are added with Column Header and Value
                    newEntry.getCustomElements().setValueLocal("ID", lead.getId());
                    newEntry.getCustomElements().setValueLocal("FirstName", lead.getFirstName());
                    newEntry.getCustomElements().setValueLocal("LastName", lead.getLastName());
                    newEntry.getCustomElements().setValueLocal("Title", lead.getTitle());
                    newEntry.getCustomElements().setValueLocal("Phone", lead.getPhone());
                    newEntry.getCustomElements().setValueLocal("Email", lead.getEmail());
                    //Inserts to the Spreadsheet
                    spreadsheetService.insert(listFeedUrl, newEntry);
                }
            }
        }
    }

}
