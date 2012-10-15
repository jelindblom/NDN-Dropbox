import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.ccnx.ccn.CCNHandle;
import org.ccnx.ccn.config.ConfigurationException;
import org.ccnx.ccn.impl.CCNFlowControl.SaveType;
import org.ccnx.ccn.protocol.ContentName;
import org.ccnx.ccn.protocol.MalformedContentNameStringException;

public class Parameters {
	/** Define Number of Threads */
	final int numThreads = 20;

	/** Global Knowledge of Local Shared Folder
	 *  Using HashTable for fast, synchronized lookup */
	Hashtable<String, FileInformation> sharedFiles;
	
	/** Global Snapshot Network Object */
	CCNFileObject globalSnapshotObject;
	
	/** Defining Location Specifics */	
	private String sharedPath, repositoryPath, topology, namespace, snapshot;
	
	/** Defining Communication Handles */
	private CCNHandle putHandle, getHandle;
	
	/** Defining ThreadPools */ 
	ExecutorService putFileThreadPool = Executors.newFixedThreadPool(numThreads);
	ExecutorService getFileThreadPool = Executors.newFixedThreadPool(numThreads);
	
	/** Monitor Threads */
	@SuppressWarnings("rawtypes")
	ArrayList<Future> taskProgress = new ArrayList<Future>();	
	
	public Parameters (String sharedPath, String repositoryPath, String topology, String namespace) {
		sharedFiles = new Hashtable<String, FileInformation>();
		
		this.sharedPath = sharedPath;
		this.repositoryPath = repositoryPath;
		this.topology = topology;
		this.namespace = namespace;
		snapshot = namespace + "/snapshot";
		
		try {
			/** Get, Put Handle */
			getHandle = CCNHandle.open();
			putHandle = CCNHandle.open();
			
			globalSnapshotObject = new CCNFileObject(ContentName.fromNative(snapshot), putHandle);
			globalSnapshotObject.setupSave(SaveType.REPOSITORY);
		} 
		catch (ConfigurationException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		} 
		catch (MalformedContentNameStringException e) {
			e.printStackTrace();
		}
	}
	
	public String getSharedPath() {
		return sharedPath;
	}
	
	public String getRepositoryPath() {
		return repositoryPath;
	}
	
	public String getTopology() {
		return topology;
	}
	
	public String getNamespace() {
		return namespace;
	}
	
	public String getSnapshot() {
		return snapshot;
	}
	
	public CCNHandle getHandle() {
		return getHandle;
	}
	
	public CCNHandle putHandle() {
		return putHandle;
	}
}