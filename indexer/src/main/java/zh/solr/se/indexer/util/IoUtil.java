package zh.solr.se.indexer.util;

import java.io.File;

public class IoUtil {
	
    public static boolean deleteDir(File dir) {
    	if (dir == null || !dir.exists())
    		return false;
    	
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (String child : children) {
                boolean success = deleteDir(new File(dir, child));
                if (!success) {
                    return false;
                }
            }
        }
    
        // This is a file or an empty directory, so delete it
        return dir.delete();
    } 
    
	/**
	 * Concatenate file paths
	 * 
	 * @param parentDir path of the parent directory
	 * @param childPath path of a child directory or file
	 * @return the concatenated path
	 */
	public static String concatenatePaths(String parentDir, String childPath) {
		if ( parentDir == null)
			return childPath;
		else if (childPath == null)
			return parentDir;
		
		String path = parentDir.trim();
		if (path.length() > 0 && !path.endsWith("/"))
			path += "/";
		
		if (childPath != null) {
			childPath = childPath.trim();
			
			// remove the leading slash
			if (childPath.startsWith("/"))
				childPath = childPath.substring(1);
			path += childPath;
		}
		
		return path;
	}
	
	/**
	 * Create multi-level directory if it does not exist. The directory is in the form of "dir1/dir2/dir3"
	 * @param dir multi-level directory
	 */
	public static void createDirectoryIfNotExist(File dir) {
		if (dir == null)
			throw new NullPointerException("createDirsIfNotExist: directory must not be null");
		
		boolean dirExists = dir.exists();
		if (dirExists && !dir.isDirectory()) {
			dir.delete();
			dirExists = false;
		}
		
		// create the directory
		if (!dirExists)
			dir.mkdirs();
	}	

	/**
	 * separate the file path into parent directory and file name
	 * e.g., for "/var/data/data_file.txt", parent directory = "/var/data", file name = "data_file.txt"
	 * @param filePath
	 * @return the first element of the returned string array is parent directory, and the second is file name
	 */
	public static String[] getDirAndName(String filePath) {
		if (filePath == null)
			return null;
		
		File file = new File(filePath);
		
		String[] dirAndName = new String[2];
		dirAndName[0] = file.getParent();
		dirAndName[1] = file.getName();
		
		return dirAndName;
	}
	
	public static String getParentDir(String filePath) {
		if (filePath == null)
			return null;
		
		File file = new File(filePath.trim());
		
		return file.getParent();
	}
	
	public static String getFileName(String filePath) {
		if (filePath == null)
			return null;
		
		File file = new File(filePath.trim());
		
		return file.getName();		
	}
}
