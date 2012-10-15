import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
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
 * This class defines the threads used to upload content from the local
 * shared folder to ccnr.  It also calculates a sha1 digest of the file
 * contents and adds an entry into the local snapshot hashtable, sharedFiles.
 */
public class ReconcileThread implements Runnable {
	private File file;
	private String contentName;
	private Parameters parameters;
	
	public ReconcileThread(File file, String contentName, Parameters parameters) {
		this.file = file;
		this.contentName = contentName;
		this.parameters = parameters;
	}

	public void run() {
		try {
			/** De-bounce */
			Thread.sleep(250);

			/** Convert File Contents to byte[] */
			byte[] byteArray = FileUtils.readFileToByteArray(file);
			System.out.println("ISFILE");

			CCNFileObject networkObject;

			if (parameters.sharedFiles.containsKey(contentName)) {
				FileInformation fileInfo = parameters.sharedFiles.get(contentName);
				
				fileInfo.setExists(true);
				fileInfo.networkObject.save(byteArray);	
				
				System.out.println("SAVED: " + contentName);
			}
			else {
				/** Create NetworkObject */
				networkObject = new CCNFileObject(ContentName.fromNative(parameters.getNamespace() + contentName), parameters.putHandle());
				
				/** Add Listener */
				try {
					networkObject.updateInBackground(true, new FileUpdateListener(parameters, contentName));
				} 
				catch (IOException e) {
					e.printStackTrace();
				}

				/** Save to Repository */
				networkObject.setupSave(SaveType.REPOSITORY);
				networkObject.save(byteArray);

				parameters.sharedFiles.put(contentName, new FileInformation(true, networkObject));
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