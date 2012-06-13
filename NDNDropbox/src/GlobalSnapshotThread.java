import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.concurrent.Future;

import org.ccnx.ccn.CCNHandle;
import org.ccnx.ccn.impl.CCNFlowControl.SaveType;
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
 * This class defines the thread used to upload a global snapshot
 * to ccnr.  This thread is delegated by the main class a certain number of 
 * "file created" threads to wait on before it uploads a new snapshot version 
 * reflecting the changes made by those threads.  This process helps lower the 
 * number of snapshots needed to be uploaded to ccnr by reflecting a batch of 
 * changes, rather than one change per snapshot.
 */
public class GlobalSnapshotThread implements Runnable {
	private CCNHandle handle;
	private String snapshot;
	private Hashtable<String, FileInformation> sharedFiles;
	private ArrayList<Future> taskProgress;

	public GlobalSnapshotThread(String snapshot, Hashtable<String, FileInformation> sharedFiles, CCNHandle handle, ArrayList<Future> taskProgress) {
		this.snapshot = snapshot;
		this.handle = handle;
		this.sharedFiles = sharedFiles;
		this.taskProgress = taskProgress;
	}

	public void run() {
		/** Create Snapshot */
		try {
			Future future;
			while(!taskProgress.isEmpty()) {
				Iterator<Future> itr = taskProgress.iterator();
				while (itr.hasNext()) {
					future = itr.next();
					if(future.isDone() || future.isCancelled()) {
						itr.remove();
					}
				}
			}

			CCNFileObject networkObject;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(baos));


			Enumeration<String> keys = sharedFiles.keys();

			synchronized(sharedFiles) {
				while(keys.hasMoreElements()) {
					String key = keys.nextElement(), result;
					FileInformation info = sharedFiles.get(key);
					if (info.getExists()) {
						result = "true," + key + "," + info.getVersion() + "," + info.getSha1();
					}
					else {
						result = "false," + key + "," + info.getVersion() + "," + info.getSha1();
					}
					bufferedWriter.write(result);
					bufferedWriter.newLine();
				}
			}
			bufferedWriter.close();

			byte[] snapshotArray = baos.toByteArray();

			networkObject = new CCNFileObject(byte[].class, true, ContentName.fromNative(snapshot + "/snapshot" + "_" + (++NDNDropbox.snapshotVersion)), handle, snapshotArray);

			networkObject.setupSave(SaveType.REPOSITORY);
			networkObject.save();
			networkObject.close();

			System.out.println("Uploaded Snapshot: " + NDNDropbox.snapshotVersion);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedContentNameStringException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}