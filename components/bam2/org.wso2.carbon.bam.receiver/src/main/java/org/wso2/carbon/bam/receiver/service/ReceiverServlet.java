/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.bam.receiver.service;

import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.server.TServlet;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class ReceiverServlet extends TServlet {

    public ReceiverServlet(TProcessor processor, TProtocolFactory inProtocolFactory,
                           TProtocolFactory outProtocolFactory) {
        super(processor, inProtocolFactory, outProtocolFactory);
    }

    public ReceiverServlet(TProcessor processor, TProtocolFactory protocolFactory) {
        super(processor, protocolFactory);
    }

/*    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("Got request");

        ServletInputStream in = request.getInputStream();
        byte[] buf = new byte[1024];

        StringBuffer sb = new StringBuffer();
        while (in.read(buf) > 0) {
            sb.append(new String(buf));
        }

        System.out.println("Request:" + sb.toString());

        in.close();
        
        PrintWriter writer = response.getWriter();

        writer.write(sb.toString());

        writer.flush();

    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws
                                                                                    ServletException,
                                                                                    IOException {
        System.out.println("Got post request..");

        ServletInputStream in = request.getInputStream();
        byte[] buf = new byte[1024];

        StringBuffer sb = new StringBuffer();
        while (in.read(buf) > 0) {
            sb.append(new String(buf));
        }

        System.out.println("Request:" + sb.toString());

        in.close();

        PrintWriter writer = response.getWriter();

        writer.write(sb.toString());

        writer.flush();
        
    }*/
}
