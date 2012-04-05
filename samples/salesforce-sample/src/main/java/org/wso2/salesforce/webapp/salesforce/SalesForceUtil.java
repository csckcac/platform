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
package org.wso2.salesforce.webapp.salesforce;

import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.QueryResult;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.bind.XmlObject;
import org.wso2.salesforce.webapp.exception.SalesForceWebAppException;
import org.wso2.salesforce.webapp.salesforce.entity.Case;
import org.wso2.salesforce.webapp.salesforce.entity.Contact;
import org.wso2.salesforce.webapp.salesforce.entity.Lead;
import org.wso2.salesforce.webapp.salesforce.entity.Opportunity;

import java.util.Iterator;
import java.util.Vector;

/**
 * Contains util methods associated with data extracting from Salesforce.
 */
public class SalesForceUtil {

    /**
     * Retrieves the contacts associated with a particular salesforce account.
     *
     * @param conn Partner Connection
     * @return a vector containing contact details
     * @throws SalesForceWebAppException webapp exception
     */
    public static Vector<Contact> getContactsForAccount(PartnerConnection conn) throws
                                                                                SalesForceWebAppException {
        Vector<Contact> contacts = new Vector<Contact>();
        String query = "SELECT Account.Name, (SELECT Contact.Id, Contact.FirstName, " +
                       "Contact.LastName, Contact.Title, Contact.Phone, Contact.Email" +
                       " FROM Account.Contacts)" +
                       " FROM Account" +
                       " WHERE Account.Name='United Oil & Gas Corp.'";
        try {
            //Queries the database
            QueryResult result = conn.query(query);
            boolean done = false;
            //Checks whether the result is empty
            if (result.getSize() <= 0) {
                return new Vector<Contact>();
                //throw new SalesForceWebAppException(SalesForceWebAppConstants.NO_RESULTS_FOUND);
            }
            while (!done) {
                //Array containing the tuples (Rows) of the details
                SObject[] records = result.getRecords();
                for (SObject record : records) {
                    //Get all the contacts under the Account and iterates through them to create
                    //Contact objects
                    Iterator<XmlObject> iterator =
                            record.getChild("Contacts").getChildren("records");
                    while (iterator.hasNext()) {
                        XmlObject xmlObject = iterator.next();
                        //Creates a Contact object and assign values for it
                        String id = (String) xmlObject.getChild("Id").getValue();
                        String fName = (String) xmlObject.getChild("FirstName").getValue();
                        String lName = (String) xmlObject.getChild("LastName").getValue();
                        String title = (String) xmlObject.getChild("Title").getValue();
                        String phone = (String) xmlObject.getChild("Phone").getValue();
                        String email = (String) xmlObject.getChild("Email").getValue();
                        Contact contact = new Contact();
                        //Checks for null values
                        if (id != null) {
                            contact.setId(id);
                        }
                        if (fName != null) {
                            contact.setFirstName(fName);
                        }
                        if (lName != null) {
                            contact.setLastName(lName);
                        }
                        if (title != null) {
                            contact.setTitle(title);
                        }
                        if (phone != null) {
                            contact.setPhone(phone);
                        }
                        if (email != null) {
                            contact.setEmail(email);
                        }
                        contacts.add(contact);     //Adds to the Contacts Vector
                    }

                    if (result.isDone()) {
                        done = true;
                    } else {
                        result = conn.queryMore(result.getQueryLocator());
                    }
                }
            }
        } catch (ConnectionException e) {
            throw new SalesForceWebAppException("Unable to get the partner connection", e);
        }
        return contacts;
    }

    /**
     * Retrieves the opportunities associated with a particular salesforce account.
     *
     * @param conn Partner Connection
     * @return a vector containing opportunity details
     * @throws SalesForceWebAppException webapp exception
     */
    public static Vector<Opportunity> getOpportunitiesForAccount(PartnerConnection conn)
            throws SalesForceWebAppException {
        Vector<Opportunity> opportunities = new Vector<Opportunity>();
        String query = "SELECT Account.Name, (SELECT Opportunity.Id, Opportunity.Name, " +
                       "Opportunity.StageName, Opportunity.Amount, Opportunity.CloseDate" +
                       " FROM Account.Opportunities)" +
                       " FROM Account" +
                       " WHERE Account.Name='United Oil & Gas Corp.'";
        try {
            //Queries the database
            QueryResult result = conn.query(query);
            boolean done = false;
            //Checks whether the result is empty
            if (result.getSize() <= 0) {
                return new Vector<Opportunity>();
                //throw new SalesForceWebAppException(SalesForceWebAppConstants.NO_RESULTS_FOUND);
            }
            while (!done) {
                //Array containing the tuples (Rows) of the details
                SObject[] records = result.getRecords();
                for (SObject record : records) {
                    //Get all the Opportunities under the Account and iterates through them to create
                    //Opportunity objects
                    Iterator<XmlObject> iterator =
                            record.getChild("Opportunities").getChildren("records");
                    while (iterator.hasNext()) {
                        XmlObject xmlObject = iterator.next();
                        //Creates a Opportunity object and assign values for it
                        String id = (String) xmlObject.getChild("Id").getValue();
                        String name = (String) xmlObject.getChild("Name").getValue();
                        String stageName = (String) xmlObject.getChild("StageName").getValue();
                        String amount = (String) xmlObject.getChild("Amount").getValue();
                        String closeDate = (String) xmlObject.getChild("CloseDate").getValue();
                        Opportunity opportunity = new Opportunity();
                        //Checking for null values
                        if (id != null) {
                            opportunity.setId(id);
                        }
                        if (name != null) {
                            opportunity.setName(name);
                        }
                        if (stageName != null) {
                            opportunity.setStage(stageName);
                        }
                        if (amount != null) {
                            opportunity.setAmount(amount);
                        }
                        if (closeDate != null) {
                            opportunity.setCloseDate(closeDate);
                        }
                        opportunities.add(opportunity);
                    }

                    if (result.isDone()) {
                        done = true;
                    } else {
                        result = conn.queryMore(result.getQueryLocator());
                    }
                }
            }
        } catch (ConnectionException e) {
            throw new SalesForceWebAppException("Unable to get the partner connection", e);
        }
        return opportunities;
    }

    /**
     * Retrieves the cases associated with a particular salesforce account.
     *
     * @param conn Partner Connection
     * @return a vector containing case details
     * @throws SalesForceWebAppException webapp exception
     */
    public static Vector<Case> getCasesForAccount(PartnerConnection conn) throws
                                                                          SalesForceWebAppException {
        Vector<Case> cases = new Vector<Case>();
        String query = "SELECT Account.Name, (SELECT Case.CaseNumber, Case.Subject, " +
                       "Case.Priority, Case.Status, Case.Contact.FirstName, Case.Contact.LastName" +
                       " FROM Account.Cases)" +
                       " FROM Account" +
                       " WHERE Account.Name='United Oil & Gas Corp.'";
        try {
            //Queries the database
            QueryResult result = conn.query(query);
            boolean done = false;
            //Checks whether the result is empty
            if (result.getSize() <= 0) {
                return new Vector<Case>();
                //throw new SalesForceWebAppException(SalesForceWebAppConstants.NO_RESULTS_FOUND);
            }
            while (!done) {
                //Array containing the tuples (Rows) of the details
                SObject[] records = result.getRecords();
                for (SObject record : records) {
                    //Get all the cases under the Account and iterates through them to create
                    //Case objects
                    Iterator<XmlObject> iterator = record.getChild("Cases").getChildren("records");
                    while (iterator.hasNext()) {
                        XmlObject xmlObject = iterator.next();
                        //Creates a Case object and assign values for it
                        String number = (String) xmlObject.getChild("CaseNumber").getValue();
                        String subject = (String) xmlObject.getChild("Subject").getValue();
                        String priority = (String) xmlObject.getChild("Priority").getValue();
                        String status = (String) xmlObject.getChild("Status").getValue();
                        String contactFName = (String) xmlObject.getChild("Contact").
                                getChild("FirstName").getValue() ;
                        String contactLName = (String) xmlObject.getChild("Contact").
                                getChild("LastName").getValue() ;
                        Case aCase = new Case();
                        //Checking for null values
                        if (number != null) {
                            aCase.setCaseNo(number);
                        }
                        if (subject != null) {
                            aCase.setSubject(subject);
                        }
                        if (priority != null) {
                            aCase.setPriority(priority);
                        }
                        if (status != null) {
                            aCase.setStatus(status);
                        }
                        if (contactFName != null && contactLName != null) {
                            aCase.setContactName(contactFName + " " + contactLName);
                        }
                        if (contactFName != null && contactLName == null) {
                            aCase.setContactName(contactFName);
                        }
                        if (contactFName == null && contactLName != null) {
                            aCase.setContactName(contactLName);
                        }
                        cases.add(aCase);
                    }
                }
                if (result.isDone()) {
                    done = true;
                } else {
                    result = conn.queryMore(result.getQueryLocator());
                }
            }
        } catch (ConnectionException e) {
            throw new SalesForceWebAppException("Unable to get the partner connection", e);
        }
        return cases;
    }

    /**
     * Retrieves leads associated with a particular salesforce account.
     *
     * @param conn partner connection obtained via salesforce api.
     * @return a vector of leads associated with a given salesforce account.
     * @throws SalesForceWebAppException webapp exception.
     */
    public static Vector<Lead> getLeadsForAccount(PartnerConnection conn)
            throws SalesForceWebAppException {

        Vector<Lead> leads = new Vector<Lead>();

        //First: retrieves the Account ID of the given Account
        String accountId = null;
        String accountIdQuery = "SELECT Account.Id FROM Account" +
                                " WHERE Account.Name='United Oil & Gas Corp.'";
        try {
            //Queries the database
            QueryResult result = conn.query(accountIdQuery);
            boolean done = false;
            //Checks whether the result is empty
            if (result.getSize() <= 0) {
                return new Vector<Lead>();
                //throw new SalesForceWebAppException(SalesForceWebAppConstants.NO_RESULTS_FOUND);
            }
            while (!done) {
                //Array containing the tuples (Rows) of the details
                SObject[] records = result.getRecords();
                for (SObject record : records) {
                    accountId = (String) record.getChild("Id").getValue();
                    if (result.isDone()) {
                        done = true;
                    } else {
                        result = conn.queryMore(result.getQueryLocator());
                    }
                }
            }

            //Second: Queries the  Leads for the retrieved Account ID i.e. Therefore the result will
            //be Leads who were converted to the given account
            if (accountId != null) {
                String leadQuery = "SELECT Id, FirstName, LastName, Title, Email, Phone" +
                                   " FROM Lead" +
                                   " WHERE Lead.ConvertedAccountId='" + accountId + "'";
                //Queries the database
                result = conn.query(leadQuery);
                done = false;
                //Checks whether the result is empty
                if (result.getSize() <= 0) {
                    return new Vector<Lead>();
                    //throw new SalesForceWebAppException(SalesForceWebAppConstants.NO_RESULTS_FOUND);
                }
                while (!done) {
                    //Array containing the tuples (Rows) of the details
                    SObject[] records = result.getRecords();
                    for (SObject record : records) {
                        //Creates a Lead object and assigns value to it
                        String id = (String) record.getChild("Id").getValue();
                        String fName = (String) record.getChild("FirstName").getValue();
                        String lName = (String) record.getChild("LastName").getValue();
                        String title = (String) record.getChild("Title").getValue();
                        String phone = (String) record.getChild("Phone").getValue();
                        String email = (String) record.getChild("Email").getValue();
                        Lead lead = new Lead();
                        //Checks for null values
                        if (id != null) {
                            lead.setId(id);
                        }
                        if (fName != null) {
                            lead.setFirstName(fName);
                        }
                        if (lName != null) {
                            lead.setLastName(lName);
                        }
                        if (title != null) {
                            lead.setTitle(title);
                        }
                        if (phone != null) {
                            lead.setPhone(phone);
                        }
                        if (email != null) {
                            lead.setEmail(email);
                        }
                        leads.add(lead);
                        if (result.isDone()) {
                            done = true;
                        } else {
                            result = conn.queryMore(result.getQueryLocator());
                        }
                    }
                }

            } else {
                throw new SalesForceWebAppException("Unable to query the Account ID of the given Account");
            }
        } catch (ConnectionException e) {
            throw new SalesForceWebAppException("Unable to get the partner connection", e);
        }
        return leads;
    }

}
