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
package org.wso2.carbon.dataservices.sql.driver.parser;

import org.wso2.carbon.dataservices.sql.driver.parser.analyzer.SyntaxAnalyser;

public class ParserTester {

    public static void main(String[] args) {

       // String inputString="INSERT INTO tbl_name (a,b,c) VALUES(1,2,3)";


        //String inputString="SELECT a,COUNT(b) FROM employee WHERE id=1";

        //String inputString="SELECT CONCAT(id,',',name) AS full_name FROM employee";

        //done
        //String inputString="SELECT id,name,address FROM employee";
        //done
        //String inputString="SELECT t1.name,t2.address FROM employee";
        //done but need some improvements to generalize the absence of WHERE result
        //String inputString="SELECT id,name,address,id1,name1,address1 FROM employee,employee1";
        //done
        //String inputString="SELECT id,name,address FROM employee WHERE id=1";
        //done
        //String inputString="SELECT * FROM employee WHERE id=1 OR name='banuka'";
        //done
        //String inputString="SELECT id,id1 FROM employee,employee1 WHERE (address = 'colombo' OR address1 = 'kottawa') AND name = 'banuka'";
        //done
        //String inputString="SELECT id,id1 FROM employee,employee1 WHERE address1 = 'kottawa' OR name1 = 'banuka'";
        //done
        //String inputString="SELECT id,id1 FROM employee,employee1";
        //done
        //String inputString="SELECT id,name FROM employee WHERE id=1";

        //String inputString="SELECT Model,Model1 FROM Product,Product1 WHERE ManufacturerID IN (SELECT ManufacturerID FROM Manufacturer WHERE ManufacturerName = 'Dell')";
        //String inputString="SELECT Model,Model1 FROM Product,Product1,Product2 WHERE ManufacturerID IN (SELECT ManufacturerID FROM Manufacturer WHERE Manufacturer = 'Dell')";
        //String inputString = "UPDATE Persons SET Address='Nissestien 67', City='Sandnes' WHERE LastName='Tjessem' AND FirstName='Jakob'";

        //done
        String inputString = "SELECT test AS t FROM employee";
        //done
        //String inputString = "SELECT (test,test1) FROM employee";
        //done
        //String inputString="SELECT SUM(P.Name), T.ID, test FROM employee";
        //done
        //String inputString="SELECT  CONCAT(abc,efg) AS test FROM employee";
        //done
        //String inputString="SELECT CONCAT(abc) AS pqr, Name FROM employee";

        SyntaxAnalyser sa = new SyntaxAnalyser(SQLParserUtil.getTokens(inputString));

        try {
           sa.getSQLQuery();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
