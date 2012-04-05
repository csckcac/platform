/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.multiple.instance.endpoint.mgt.autoscale;

import java.lang.*;
import java.io.*;

/* This handles the standard input and error of starting java processes using java.exec */
public class StreamHandler implements Runnable {
    String name;
    InputStream is;
    Thread thread;

    public StreamHandler (String name, InputStream is) {
        this.name = name;
        this.is = is;
    }

    public void start () {
        thread = new Thread (this);
        thread.start ();
    }

    public void run () {
        try {
            InputStreamReader isr = new InputStreamReader (is);
            BufferedReader br = new BufferedReader (isr);

            while (true) {
                String s = br.readLine ();
                if (s == null) break;
                System.out.println ("[" + name + "] " + s);
            }

            is.close ();

        } catch (Exception ex) {
            System.out.println ("Problem reading stream " + name + "... :" + ex);
            ex.printStackTrace ();
        }
    }
}
