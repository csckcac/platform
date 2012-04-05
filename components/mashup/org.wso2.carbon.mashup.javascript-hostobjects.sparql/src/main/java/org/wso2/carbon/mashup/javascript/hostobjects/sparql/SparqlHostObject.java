/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.mashup.javascript.hostobjects.sparql;


import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.engine.http.HttpQuery;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.mozilla.javascript.*;
import org.wso2.carbon.CarbonException;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;

import com.hp.hpl.jena.sparql.engine.http.HttpContentType;

public class SparqlHostObject extends ScriptableObject {

    private String rdfDataSource;
    private String sparqlEndPoint;
    private String spaqrlQuery;
    private String resultType;

    private static final String RESULT_TYPE_JSON = "JSON";

    public SparqlHostObject() {

    }

    public static Scriptable jsConstructor(Context cx, Object[] args, Function ctorObj,
                                           boolean inNewExpr) throws CarbonException {

        SparqlHostObject sparqlObject = new SparqlHostObject();
        if (args.length != 0) {
            throw new CarbonException("SparqlObject constructor doesn't accept any arguments");
        }
        return sparqlObject;
    }

    public Object jsGet_sparqlEndPoint() {
        return sparqlEndPoint;
    }

    public void jsSet_sparqlEndPoint(Object sparqlEndPoint) {
        this.sparqlEndPoint = (String)sparqlEndPoint;
    }

    public Object jsGet_rdfDataSource() {
        return rdfDataSource;
    }

    public void jsSet_rdfDataSource(Object rdfDataSource) {
        this.rdfDataSource = (String) rdfDataSource;
    }

    public Object jsGet_spaqrlQuery() {
        return spaqrlQuery;
    }

    public void jsSet_spaqrlQuery(Object spaqrlQuery) {
        this.spaqrlQuery = (String) spaqrlQuery;
    }

    public Object jsGet_resultType() {
        return resultType;
    }

    public void jsSet_resultType(Object resultType) {
        this.resultType = (String) resultType;
    }

    public static Object jsFunction_getDataFromRdfSource(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws Exception {
        String resultString;
        SparqlHostObject sparqlObject = (SparqlHostObject) thisObj;
        if (sparqlObject == null) {
            throw new CarbonException("SparqlObject is null");
        }
        //Open a given rdf datasource from a URL
        try {

            Dataset dataSet = DatasetFactory.create(sparqlObject.rdfDataSource);

            // Create a new query form a given user query
            String queryString = sparqlObject.spaqrlQuery;

            Query query = QueryFactory.create(queryString);

            QueryExecution qe = QueryExecutionFactory.create(query, dataSet);

            ResultSet results = qe.execSelect();

            if ((sparqlObject.resultType) != null && (sparqlObject.resultType).toUpperCase().equals(sparqlObject.RESULT_TYPE_JSON)) {
                // Output query results as JSON
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ResultSetFormatter.outputAsJSON(bos, results);
                resultString = bos.toString();

            } else {
                // Output query results as XML
                resultString = ResultSetFormatter.asXMLString(results);
                OMElement e = AXIOMUtil.stringToOM(resultString);
                return e.toString();
            }

            // Important - free up resources used running the query
            qe.close();
            return resultString;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static Object jsFunction_getDataFromSparqlEndPoint(Context cx, Scriptable thisObj, Object[] args, Function funObj)
            throws Exception {
        String resultString;
        SparqlHostObject sparqlObject = (SparqlHostObject) thisObj;
        if (sparqlObject == null) {
            throw new CarbonException("SparqlObject is null");
        }
        try {
            // Create a new query form a given user query
            String queryString = sparqlObject.spaqrlQuery;

            Query query = QueryFactory.create(queryString);

            QueryExecution qe = QueryExecutionFactory.sparqlService(sparqlObject.sparqlEndPoint, query);

            ResultSet results = qe.execSelect();

            if ((sparqlObject.resultType) != null && (sparqlObject.resultType).toUpperCase().equals(sparqlObject.RESULT_TYPE_JSON)) {
                // Output query results as JSON
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ResultSetFormatter.outputAsJSON(bos, results);
                resultString = bos.toString();

            } else {
                // Output query results as XML
                resultString = ResultSetFormatter.asXMLString(results);
                OMElement e = AXIOMUtil.stringToOM(resultString);
                return e.toString();
            }

            // Important - free up resources used running the query
            qe.close();
            return resultString;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    @Override
    public String getClassName() {
        return "Sparql";
    }


}
