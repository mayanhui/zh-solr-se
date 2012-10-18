package zh.solr.se.indexer.util.process;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zh.solr.se.indexer.util.IoUtil;
import zh.solr.se.indexer.util.StringUtil;


public class ProcessRunner {
  private static final Log logger = LogFactory.getLog(ProcessRunner.class);

	public int execFromClassPath(String[] command) throws IOException {
		assert (command != null && command.length > 0);
		
		// the first element of the command array must be the file name of the executable
		String fileName = command[0];
		
		// copy the executable from class path to file system, assuming it is at the root of class path
		String classPathDir = "/";		
		String fullPath = copyFileFromClassPathToWorkingDirectory(fileName, classPathDir);
		logger.info("Full path to script: " + fullPath);
		command[0] = fullPath;
		
		// run the executable
		int exitValue = execComamnd(command, null, null);
		
		// remove the executable from the file system
		(new File(fullPath)).delete();
		
		return exitValue;
	}
	
    public int execComamnd(String[] command, String[] env, String commandDir) {
    	File dir = (commandDir == null) ? null : new File(commandDir);
    	
    	int exitValue = -1;
        try {
        	// execute the command
            Runtime rt = Runtime.getRuntime();
						logger.info("Running " + constructCommandString(command, env, commandDir));
            Process proc = rt.exec(command, env, dir);

            // check for error message and output
            StreamConsumer errorConsumer= new StreamConsumer(proc.getErrorStream(), "ERROR");            
            StreamConsumer outputConsumer = new StreamConsumer(proc.getInputStream(), "OUTPUT");
            errorConsumer.start();
            outputConsumer.start();
                                    
            // wait until the process finishes
			exitValue = proc.waitFor();
		} catch (Throwable t) {
			t.printStackTrace();
			exitValue = 4;
		}
		
		return exitValue;
	}
    
    private String copyFileFromClassPathToWorkingDirectory(String fileName, String classPathDir) throws IOException {
    	assert fileName != null;
    	
    	String workingDir = System.getProperty("user.dir");
    	String inPath = IoUtil.concatenatePaths(classPathDir, fileName);
    	String outPath = IoUtil.concatenatePaths(workingDir, fileName);
			logger.info("Working dir:  " + workingDir);
			logger.info("Path to script:  " + outPath);
    	File outFile = new File(outPath);
    	outFile.createNewFile();
    	outFile.setExecutable(true);
    	
    	// copy the file from class path to working directory
		InputStream inStream = ProcessRunner.class.getResourceAsStream(inPath);
		FileOutputStream outStream = new FileOutputStream(outFile);
    	
	    int bytesRead;
	    byte[] buf = new byte[1024];
	    while((bytesRead=inStream.read(buf))!=-1){
	    	String str = new String(buf);
	    	str = str.replaceAll ( "\r",  "" );     
	    	buf = str.getBytes();		    	  
	    	outStream.write(buf,0,buf.length);
	    	buf = new byte[1024];
	    }
	    outStream.flush();
	    outStream.close();
	    inStream.close();
	      
    	return outPath;
    }
   
    private String constructCommandString(String[] command, String[] env, String commandDir) {
    	StringBuilder builder = new StringBuilder(IoUtil.concatenatePaths(commandDir, command[0]));
    	for (int i = 1; i < command.length; i++) {
    		builder.append(" ");
    		builder.append(command[i]);
    	}
    	
    	if (env != null && env.length > 0) {
    		builder.append(StringUtil.NEW_LINE);
    		builder.append("env:");
    		for (String envStr : env) {
    			builder.append(", ").append(envStr);
    		}
    	}
    	
    	return builder.toString();
    }
}
