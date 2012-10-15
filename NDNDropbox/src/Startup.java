import java.io.File;

/**
 * NDN Dropbox: Distributed, Dropbox-like File Sharing Service over NDN
 *  
 * @category Distributed File Sharing
 * @author Jared Lindblom
 * @author Huang (John) Ming-Chun
 * @version 1.0
 * 
 * This is a static class used to initially scan the shared folder and populate
 * the hashtable, sharedFiles.
 */
public class Startup {
	public static void Populate(String sharedPath, Parameters parameters) {
		File sharedDirectory = new File(sharedPath);
		File[] fileList = sharedDirectory.listFiles();
		
		for (int i = 0; i < fileList.length; i++) {
			if (fileList[i].isDirectory()) {
				Populate(fileList[i].getPath(), parameters);
			} 
			else if(fileList[i].isFile() && !fileList[i].isHidden() && fileList[i].canRead()) {
				
				/** Determine ContentName */
				String contentName = "";
				
				int index = fileList[i].getAbsolutePath().lastIndexOf(sharedPath);
				
				if (index > -1) {
					contentName = fileList[i].getAbsolutePath().substring(sharedPath.length());
				}
				else {
					contentName = fileList[i].getAbsolutePath();
				}
				
				/** Create File */
				Runnable runnable = new ReconcileThread(fileList[i], contentName, parameters);
				parameters.taskProgress.add(parameters.putFileThreadPool.submit(runnable));
				NDNDropbox.updateNeeded = true;				
			}
			else {
				/** Ignore */
			}
		}
	}
}