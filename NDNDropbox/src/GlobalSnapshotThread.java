import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.concurrent.Future;

/**
 * NDN Dropbox: Distributed, Dropbox-like File Sharing Service over NDN
 *  
 * @category Distributed File Sharing
 * @author Jared Lindblom
 * @author Huang (John) Ming-Chun
 * @version 1.0
 * 
 * This class defines the thread used to upload a global snapshot
 * to ccnr.  This thread is delegated by the main class a certain number of 
 * "file created" threads to wait on before it uploads a new snapshot version 
 * reflecting the changes made by those threads.  This process helps lower the 
 * number of snapshots needed to be uploaded to ccnr by reflecting a batch of 
 * changes, rather than one change per snapshot.
 */
public class GlobalSnapshotThread implements Runnable {
	private Hashtable<String, FileInformation> sharedFiles;
	
	@SuppressWarnings("rawtypes")
	private ArrayList<Future> taskProgress;
	
	private CCNFileObject globalSnapshotObject;

	public GlobalSnapshotThread(Parameters parameters, @SuppressWarnings("rawtypes") ArrayList<Future> taskProgress) {
		this.sharedFiles = parameters.sharedFiles;
		this.taskProgress = taskProgress;
		this.globalSnapshotObject = parameters.globalSnapshotObject;
	}

	public void run() {
		/** Create Snapshot */
		try {
			@SuppressWarnings("rawtypes")
			Future future;
			
			while(!taskProgress.isEmpty()) {
				@SuppressWarnings("rawtypes")
				Iterator<Future> itr = taskProgress.iterator();
				
				while (itr.hasNext()) {
					future = itr.next();
					if(future.isDone() || future.isCancelled()) {
						itr.remove();
					}
				}
			}
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(baos));

			Enumeration<String> keys = sharedFiles.keys();

			synchronized(sharedFiles) {
				while(keys.hasMoreElements()) {
					String key = keys.nextElement(), result;
					
					FileInformation info = sharedFiles.get(key);
					
					if (info.getExists()) {
						result = "true," + key;
					}
					else {
						result = "false," + key;
					}
					
					System.out.println(result);
					
					bufferedWriter.write(result);
					bufferedWriter.newLine();
				}
			}
			bufferedWriter.close();

			byte[] snapshotArray = baos.toByteArray();

			globalSnapshotObject.save(snapshotArray);
		} 
		catch (IOException e) {
			e.printStackTrace();
		} 
	}
}