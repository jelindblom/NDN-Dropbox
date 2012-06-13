import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.ccnx.ccn.CCNHandle;
import org.ccnx.ccn.config.ConfigurationException;
import org.ccnx.ccn.io.content.ConfigSlice;
import org.ccnx.ccn.protocol.ContentName;
import org.ccnx.ccn.protocol.MalformedContentNameStringException;

/**
 * NDN Dropbox: Distributed, Dropbox-like File Sharing Service over NDN
 *  
 * @category Distributed File Sharing
 * @author Jared Lindblom
 * @author Huang (John) Ming-Chun
 * @version 1.0
 * 
 * This is the main class.  It initializes hashtables and threadpools, establishes
 * connections with ccnd, creates a collection in ccnr, registers callbacks on snapshots
 * with ccnr, starts Jnotify, and constantly listens for updates to shared folder (local/global)
 * to create snapshots.
 */
public class NDNDropbox {
	static volatile int snapshotVersion = 0;
	static volatile boolean updateNeeded = false;
	
	public static void main (String[] args) {
		/** Global Knowledge of Local Shared Folder
		 *  Using HashTable for fast, synchronized lookup */
		Hashtable<String, FileInformation> sharedFiles = new Hashtable<String, FileInformation>();
		
		/** Defining Location Specifics */	
		String sharedPath = "/home/jared/Desktop/Shared";
		String repositoryPath = "/home/jared/Desktop/Repository";
		String topology = "/Topo";
		String namespace = "/root/beer";
		String snapshot = namespace + "/snapshot";
		
		/** Defining Communication Handles */
		CCNHandle putHandle, getHandle;
		
		/** Defining ThreadPools */ 
		final int numThreads = 20;
		ExecutorService putFileThreadPool = Executors.newFixedThreadPool(numThreads);
		ExecutorService getFileThreadPool = Executors.newFixedThreadPool(numThreads);
		ArrayList<Future> taskProgress = new ArrayList<Future>();		
		
		/** Build Local Shared Folder HashTable */
		InitialScan.ScanFiles(sharedPath, sharedFiles);
		
		/** Register for Namespace updates */
		try {
			/** Get Handle */
			getHandle = CCNHandle.open();
			
			/** Create Prefix (Namespace) to Monitor */
			ContentName prefix = ContentName.fromNative(snapshot);
			
			/** Register for Name Enumerations */
			FileNameEnumerator fileNameEnumerator = new FileNameEnumerator(sharedPath, sharedFiles, namespace, snapshot, getHandle, getFileThreadPool);
			fileNameEnumerator.registerPrefix(prefix);
		} catch (MalformedContentNameStringException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
		/** End Register for Namespace updates */
		
		/** Create Slice, Start Jnotfiy */
		try {
			/** Get Handle */
			putHandle = CCNHandle.open();
			
			/** Create Slice */
			ConfigSlice.checkAndCreate(ContentName.fromNative(topology), ContentName.fromNative(namespace), null, putHandle);
			
			/** Create Instance of JNotify */
			FolderWatch folderWatch = new FolderWatch(sharedPath, sharedFiles, namespace, putHandle, putFileThreadPool, taskProgress);
			
			/** Start JNotify */
			folderWatch.startJNotify();
			
			/** Loop Forever */
			while(true) {
				/** Snapshot update needed? */
				if (updateNeeded) {
					System.out.println("Updating!");
					synchronized(taskProgress) {
						updateNeeded = false;
						Runnable runnable = new GlobalSnapshotThread(snapshot, sharedFiles, putHandle, new ArrayList<Future>(taskProgress));
						putFileThreadPool.submit(runnable);
						taskProgress.removeAll(taskProgress);
					}
				}
				Thread.sleep(3000);
			}
		} catch (ConfigurationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (MalformedContentNameStringException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}	
}