import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Future;
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
	static volatile boolean updateNeeded = false;
	
	public static void main (String[] args) {
		/** Check Parameters */
		if (args.length != 4) {
			System.out.println("Invalid number of Parameters");
			System.out.println("Usage: NDNDropbox <shared dir> <repository dir> <topology> <namespace>");
			System.out.println("Also, make sure JNotify is in your java.library.path!");
			return;
		}
		
		/** Initialize Parameters */
		Parameters parameters = new Parameters(args[0], args[1], args[2], args[3]);			
			
		/** Add Listener to Global Snapshot */
		try {
			GlobalSnapshotUpdateListener gsul = new GlobalSnapshotUpdateListener(parameters);
			parameters.globalSnapshotObject.updateInBackground(true, gsul);
			
			if(parameters.globalSnapshotObject.available()) {
				gsul.newVersionAvailable(parameters.globalSnapshotObject, false);
			}
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		
		/** Build Local Shared Folder HashTable */
		//Startup.Populate(parameters.getSharedPath(), parameters);
		
		/** Create Slice, Start Jnotify */
		try {	
			/** Create Slice */
			ConfigSlice.checkAndCreate(ContentName.fromNative(parameters.getTopology()), ContentName.fromNative(parameters.getNamespace()), null, parameters.putHandle());
			
			/** Create Instance of JNotify */
			FolderWatch folderWatch = new FolderWatch(parameters);
			
			/** Start JNotify */
			folderWatch.startJNotify();
			
			/** Loop Forever */
			while(true) {
				/** Snapshot update needed? */
				if (updateNeeded) {
					synchronized(parameters.taskProgress) {
						updateNeeded = false;
						
						@SuppressWarnings("rawtypes")
						Runnable runnable = new GlobalSnapshotThread(parameters, new ArrayList<Future>(parameters.taskProgress));
						
						parameters.putFileThreadPool.submit(runnable);
						parameters.taskProgress.removeAll(parameters.taskProgress);
					}
				}
				Thread.sleep(2000);
			}
		} 
		catch (IOException e) {
			e.printStackTrace();
		} 
		catch (MalformedContentNameStringException e) {
			e.printStackTrace();
		} 
		catch (InterruptedException e) {
			e.printStackTrace();
		} 
	}	
}