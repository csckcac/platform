/*
 * Copyright 2012 The Apache Software Foundation.
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
package org.wso2.carbon.jaggery.tools;

import java.io.*;

import org.mozilla.javascript.ScriptableObject;
import org.wso2.carbon.jaggery.core.manager.JaggeryContext;
import org.wso2.carbon.scriptengine.engine.RhinoEngine;
import org.wso2.carbon.jaggery.core.manager.CommandLineManager;
import org.wso2.carbon.jaggery.core.ScriptReader;

/**
*
* Jaggery CommnadLineExecutor - this will parse file URLs or expressions by 
* runtime Jaggery engine
*/
public final class CommandLineExecutor {

	private static PrintStream out = System.out;
    
	private CommandLineExecutor() {
	    //disable external instantiation
	}
	/**
     * Parse Jaggery scripts resides in the file path
     *
     * @param fileURL url of the file
     */
	public static void parseJaggeryScript(String fileURL) {

        try{

            //Initialize the Rhino context
            RhinoEngine.enterContext();
			FileInputStream fstream = new FileInputStream(fileURL);
            
        	RhinoEngine engine = CommandLineManager.getCommandLineEngine();
        	ScriptableObject scope = engine.getRuntimeScope();
        	
        	//initialize JaggeryContext
        	JaggeryContext jaggeryContext = new JaggeryContext();
        	jaggeryContext.setEnvironment(CommandLineManager.ENV_COMMAND_LINE);
        	jaggeryContext.setTenantId("0");
        	jaggeryContext.setOutputStream(System.out);
        	jaggeryContext.setEngine(engine);
        	jaggeryContext.setScope(scope);
        	
        	RhinoEngine.putContextProperty("jaggeryContext", jaggeryContext);

            //Parsing the script
            Reader source = new ScriptReader(new BufferedInputStream(fstream));
            out.println("\n\n************ Executing Jaggery script ***********\n");
            ShellUtilityService.initializeUtilityServices();
            engine.exec(source, scope, null);
            ShellUtilityService.destroyUtilityServices();
            out.flush();
            out.println("\n\n********************* Done! *********************\n\n");
		}catch (Exception e){
			out.println("\n");
			out.println("Error: " + e.getMessage());
			out.println("\n");
		}

	}

    /**
     * Parse Jaggery expressions
     *
     * @param expression Jaggery expression string
     */
	public static void parseJaggeryExpression(String expression) {

        try{

        	//Initialize the Rhino context
            RhinoEngine.enterContext();

        	RhinoEngine engine = CommandLineManager.getCommandLineEngine();
        	ScriptableObject scope = engine.getRuntimeScope();
        	
        	//initialize JaggeryContext
        	JaggeryContext jaggeryContext = new JaggeryContext();
        	jaggeryContext.setEnvironment(CommandLineManager.ENV_COMMAND_LINE);
        	jaggeryContext.setTenantId("0");
        	jaggeryContext.setOutputStream(out);
        	jaggeryContext.setEngine(engine);
        	jaggeryContext.setScope(scope);
        	
        	RhinoEngine.putContextProperty("jaggeryContext", jaggeryContext);

        	//Parsing the script
        	ShellUtilityService.initializeUtilityServices();
            engine.exec(new StringReader(expression), scope, null);
            ShellUtilityService.destroyUtilityServices();
            out.flush();
		}catch (Exception e){
			out.println("Error: " + e.getMessage());
		}

	}

}
