package org.wso2.carbon.mapred.mgt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.FileUtil;

public class HadoopJobRunnerThread extends Thread {

	private String args;
	private String jarName;
	private String className;
	private String myTGTCache;
	private Log log = LogFactory.getLog(HadoopJobRunner.class);
    
	public HadoopJobRunnerThread(String jarName, String className, String args, String myTGTCache) {
		this.jarName = jarName;
		this.args = args;
		this.className = className;
		this.myTGTCache = myTGTCache;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		JarFile jarFile = null;
		String mainClassName = null;
		File file = new File(jarName);
		Properties hadoopConf = new Properties();
		try {
			hadoopConf.load(new FileInputStream(HadoopJobRunner.HADOOP_CONFIG));
			jarFile = new JarFile(HadoopJobRunner.DEFAULT_HADOOP_JAR_PATH+"/"+jarName);
			Manifest manifest = jarFile.getManifest();
			if (manifest != null) {
				mainClassName = manifest.getMainAttributes().getValue("Main-Class");
			}
			jarFile.close();
			if (mainClassName == null) {
				mainClassName = className;
			}
			mainClassName = mainClassName.replaceAll("/", ".");
			File tmpDir = new File(hadoopConf.getProperty("taskcontroller.job.dir"));
			tmpDir.mkdirs();
			if (!tmpDir.isDirectory()) {
				System.err.println("Mkdirs failed to create " + tmpDir);
				return;
			}
			final File workDir = File.createTempFile("hadoop-unjar", "", tmpDir);
			workDir.delete();
			workDir.mkdirs();
			if (!workDir.isDirectory()) {
				log.error("Mkdirs failed to create " + workDir);
				return;
			}

			Runtime.getRuntime().addShutdownHook(new Thread() {
				public void run() {
					try {
						FileUtil.fullyDelete(workDir);
					} catch (IOException e) {
					}
				}
			});
			unJar(file, workDir);

			ArrayList<String> classPath = new ArrayList<String>();
			classPath.add(new File(workDir+"/").getAbsolutePath());
			classPath.add(file.getAbsolutePath());
			classPath.add(new File(workDir, "classes/").getAbsolutePath());
			File[] libs = new File(workDir, "lib").listFiles();
			if (libs != null) {
				for (int i = 0; i < libs.length; i++) {
					classPath.add(libs[i].getAbsolutePath());
				}
			}
			String[] classPathsVars = System.getProperty("java.class.path").split(":");
			for (int i=0; i<classPathsVars.length; i++) {
				classPath.add(new File(classPathsVars[i]).getAbsolutePath());
			}
			
			classPathsVars = hadoopConf.getProperty("taskcontroller.depends.dir").split(":");
			for (int j=0; j<classPathsVars.length; j++) {
				File[] carbonJars = new File(classPathsVars[j]).listFiles();
				if (carbonJars != null) {
					for (int i=0; i<carbonJars.length; i++){
						if (carbonJars[i].isFile()) {
							classPath.add(carbonJars[i].getAbsolutePath());
                                                }
					}
				}
			}
			/*UClassLoader loader =
				new URLClassLoader(classPath.toArray(new URL[0]));

			Thread.currentThread().setContextClassLoader(loader);
			Class<?> mainClass = Class.forName(mainClassName, true, loader);
			Method main = mainClass.getMethod("main", new Class[] {
					Array.newInstance(String.class, 0).getClass()
			});
			String[] newArgs = (className+" "+args).split(" ");
			main.invoke(null, new Object[] { newArgs });*/
			/**
			 * We cannot simply invoke main method. If this is done when running inside Carbon both the client (job submitter) and the 
			 * Job Tracker will share the same JVM which in-turn will cause problems to the static variables declared in the org.apache.hadoop.security.
			 * Hadoop is not designed to use the same JVM by both Job Tracker and Client or by multiple clients. Therefore at this point we have to start
			 * a new JVM for each client request.
			 */
			//String[] newArgs = (className+" "+args).split(" ");
			String[] strClassPath = new String[classPath.size()];
			strClassPath = classPath.toArray(strClassPath);
			String classPathParam = "";
			for (int i=0; i<strClassPath.length; i++) {
				if (strClassPath.length - 1 != i)
					classPathParam += strClassPath[i]+":";
				else
					classPathParam += strClassPath[i];
			}
			Map<String, String> envMap = System.getenv();
			String javaHome = envMap.get("JAVA_HOME");
			String carbonTGTHandle = "-Dcarbon.kerberos.tgt.handle="+this.myTGTCache;
			String javaPath = javaHome+"/bin/java";
			ArrayList<String> paramList = new ArrayList<String>();
			paramList.add(javaPath);
			paramList.add("-classpath");
			paramList.add(classPathParam);
			paramList.add(carbonTGTHandle);
			paramList.add(mainClassName);
			paramList.add(className);
			String[] argsArray = args.split(" ");
			for (int i=0; i<argsArray.length; i++) {
				System.out.println(argsArray[i]);
				paramList.add(argsArray[i]);
			}
			ProcessBuilder jvm = new ProcessBuilder(paramList);
			Map<String, String> jvmEnv = jvm.environment();
			jvmEnv.putAll(envMap);
			log.info("Executing Hadoop job");
			Process jvmProc = jvm.start();
			int extCode = 0;
			if ((extCode=jvmProc.waitFor()) != 0) {
				log.error("JVM for the Hadoop Job: "+extCode);
				InputStream err = jvmProc.getErrorStream();
				InputStream out = jvmProc.getInputStream();
				
				byte buffer[] = new byte[255];
				if (err.available() > 0) {
					while (err.read(buffer) > 0)
						System.out.print(new String(buffer));
				}
				if (out.available() > 0) {
					while (out.read(buffer) > 0)
						System.out.print(new String(buffer));
				}
			}
			else {
				log.info("JVM for the Hadoop Job Executed");
			}
		}
		catch (IOException io) {
			log.error("Error opening job jar: " + jarName);
			io.printStackTrace();
			//throw new IOException("Error opening job jar: " + jarName).initCause(io);
			return;
		}
		/*catch (NoSuchMethodException noSuchMethod) {
			log.error("Cannot find main method in "+className+" in "+jarName);
			noSuchMethod.printStackTrace();
			return;
		}
		catch (ClassNotFoundException noClass) {
			log.error("Cannot find the class"+ className +" in "+jarName);
			noClass.printStackTrace();
			return;
		}
		catch (IllegalAccessException illegalAccess) {
			log.error("Unable to access main method in "+className+" in "+jarName);
			illegalAccess.printStackTrace();
			return;
		}
		catch (InvocationTargetException e) {
			log.error("Unable to Execute Job due to InvocationTargetException: "+e.getMessage());
			e.getTargetException().printStackTrace();
			return;
		} */catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void unJar(File jarFile, File toDir) throws IOException {
		JarFile jar = new JarFile(jarFile);
		try {
			Enumeration<JarEntry> entries = jar.entries();
			while (entries.hasMoreElements()) {
				JarEntry entry = (JarEntry)entries.nextElement();
				if (!entry.isDirectory()) {
					InputStream in = jar.getInputStream(entry);
					try {
						File file = new File(toDir, entry.getName());
						if (!file.getParentFile().mkdirs()) {
							if (!file.getParentFile().isDirectory()) {
								log.error("Mkdirs failed to create "+file.getParentFile().toString());
								throw new IOException("Mkdirs failed to create " +
										file.getParentFile().toString());
							}
						}
						OutputStream out = new FileOutputStream(file);
						try {
							byte[] buffer = new byte[8192];
							int i;
							while ((i = in.read(buffer)) != -1) {
								out.write(buffer, 0, i);
							}
						} finally {
							out.close();
						}
					} finally {
						in.close();
					}
				}
			}
		} finally {
			jar.close();
		}
	}
	
}
