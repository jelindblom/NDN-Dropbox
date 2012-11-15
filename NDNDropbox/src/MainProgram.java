import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Future;
import org.ccnx.ccn.protocol.ContentName;

/**
 * NDN Dropbox: Distributed, Dropbox-like File Sharing Service over NDN
 *  
 * @category Distributed File Sharing
 * @author Jared Lindblom
 * @author Huang (John) Ming-Chun
 * @version 1.0
 */
public class MainProgram {
	String repoDirectoryPath = "", sharedDirectoryPath = "", topologyString = "", namespaceString = "";
	ContentName topology = null, namespace = null, snapshot = null;
	
	public MainProgram(String getRepoDirectoryPath, 
			           String getSharedDirectoryPath, 
			           String getTopologyString,
			           String getNameSpaceString,
			           ContentName getTopology,
			           ContentName getNameSpace,
			           ContentName getSnapShot) {
		repoDirectoryPath = getRepoDirectoryPath;
		sharedDirectoryPath = getSharedDirectoryPath;
		topologyString = getTopologyString;
		namespaceString = getNameSpaceString;
		topology = getTopology;
		namespace = getNameSpace;
		snapshot = getSnapShot;
	}
	
	public void RunProgram() {
		/** Uncomment the following code for automatic execution of ccnd and ccnr */
		
		/*try {
			Runtime.getRuntime().exec("ccndstop");
			Runtime.getRuntime().exec("ccndstart");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			File tmpRepo = new File(repoDirectoryPath);
			Runtime.getRuntime().exec("ccnr",null, tmpRepo);
		} catch (IOException e2) {
			e2.printStackTrace();
		}*/
		
		System.out.println("Bootstrap End");
				
		/** Initialize Parameters */		
		Parameters parameters = new Parameters(sharedDirectoryPath, topology, namespace, snapshot);			
		
		/** Build Local Shared Folder Hashtable */
		Startup startup = new Startup(parameters);
		startup.Populate(parameters.getSharedDirectoryPath(), parameters);
		
		/**  Start Jnotify */
		try {	
			/** Create Instance of JNotify */
			FolderWatch folderWatch = new FolderWatch(parameters);
			
			/** Start JNotify */
			folderWatch.startJNotify();
			
			/** Loop Forever */
			while(true) {
				/** Snapshot update needed? */
				if (!parameters.taskProgress.isEmpty()) {
					synchronized(parameters.taskProgress) {
						@SuppressWarnings("rawtypes")
						Runnable runnable = new GlobalSnapshotThread(parameters, new ArrayList<Future>(parameters.taskProgress));
						
						parameters.threadPool.submit(runnable);
						parameters.taskProgress.removeAll(parameters.taskProgress);
					}
				}
				Thread.sleep(2000);
			}
		} catch (IOException e3) {
			System.out.println("JNotify could not start.");
			return;
		} catch (InterruptedException e4) {
			e4.printStackTrace();
		}
	}
}
	
