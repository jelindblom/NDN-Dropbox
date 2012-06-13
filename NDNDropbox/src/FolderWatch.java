import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.ccnx.ccn.CCNHandle;

import net.contentobjects.jnotify.JNotify;
import net.contentobjects.jnotify.JNotifyException;
import net.contentobjects.jnotify.JNotifyListener;

/**
 * NDN Dropbox: Distributed, Dropbox-like File Sharing Service over NDN
 *  
 * @category Distributed File Sharing
 * @author Jared Lindblom
 * @author Huang (John) Ming-Chun
 * @version 1.0
 * 
 * This class is used as an interface to the Jnotify library.  It defines how
 * a Jnotify event is to be handled by our program, when such an event occurs.
 * When needed, it spawns "file created" threads to upload content to the repository.
 */
public class FolderWatch {
	private int watchID;
	private String sharedPath;
	private CCNHandle putHandle;
	private String namespace;
	ExecutorService putFileThreadPool;
	private Hashtable<String, FileInformation> sharedFiles;
	ArrayList<Future> taskProgress;

	public FolderWatch(String sharedPath, Hashtable<String, FileInformation> sharedFiles, String namespace, CCNHandle putHandle, ExecutorService putFileThreadPool, ArrayList<Future> taskProgress) {
		this.watchID = 0;
		this.sharedPath = sharedPath;
		this.sharedFiles = sharedFiles;
		this.putHandle = putHandle;
		this.namespace = namespace;
		this.putFileThreadPool = putFileThreadPool;
		this.taskProgress = taskProgress;
	}

	public void startJNotify() throws JNotifyException {
		int mask =  JNotify.FILE_CREATED | 
				JNotify.FILE_DELETED | 
				JNotify.FILE_MODIFIED| 
				JNotify.FILE_RENAMED;

		boolean watchSubtree = true;  // Recursive

		watchID = JNotify.addWatch(sharedPath, mask, watchSubtree, new JNotifyListener() {
			public void fileRenamed(int wd, String rootPath, String oldName, String newName) {
				//System.out.println("JNotifyTest.fileRenamed() : wd #" + wd + " root = " + rootPath + ", " + oldName + " -> " + newName);

				// TODO Handle fileRenamed case
			}

			public void fileModified(int wd, String rootPath, String name) {
				//System.out.println("JNotifyTest.fileModified() : wd #" + wd + " root = " + rootPath + ", " + name);

				// TODO Handle fileModified case
			}

			public void fileDeleted(int wd, String rootPath, String name) {
				//System.out.println("JNotifyTest.fileDeleted() : wd #" + wd + " root = " + rootPath + ", " + name);

				// TODO Handle fileDeleted case
				name = "/" + name; // Add slash
				
				if(name.endsWith("/")) {
					name = name.substring(0, name.length()-1);
				}
				
				if (sharedFiles.containsKey(name) && sharedFiles.get(name).getExists() == true) {
					NDNDropbox.updateNeeded = true;
					sharedFiles.get(name).setExists(false);	
				}
				else {
					System.out.println("File Deleted was not known.");
				}
			}

			public void fileCreated(int wd, String rootPath, String name) {
				//System.out.println("JNotifyTest.fileCreated() : wd #" + wd + " root = " + rootPath + ", " + name);
				
				/** Add File to Local Shared Files */
				name = "/" + name; // Add slash
				
				if (sharedFiles.containsKey(name) && sharedFiles.get(name).getExists() == false) {
					sharedFiles.get(name).setExists(true);
					Runnable runnable = new FileCreatedThread(wd, rootPath, name, namespace, sharedFiles, putHandle);
					taskProgress.add(putFileThreadPool.submit(runnable));
					NDNDropbox.updateNeeded = true;
				}
				else if (!sharedFiles.containsKey(name)) {
					Runnable runnable = new FileCreatedThread(wd, rootPath, name, namespace, sharedFiles, putHandle);
					taskProgress.add(putFileThreadPool.submit(runnable));
					NDNDropbox.updateNeeded = true;
				}
				else {
					// Do Nothing
				}			
			}
		});
	}

	public void stopJNotify() throws JNotifyException {
		boolean res = false;

		res = JNotify.removeWatch(watchID);

		if (!res) {
			// invalid watch ID specified.
		}
	}

	public int getWatchID() {
		return watchID;
	}
}
