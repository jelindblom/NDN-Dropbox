import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.ccnx.ccn.impl.CCNFlowControl.SaveType;
import org.ccnx.ccn.io.ErrorStateException;
import org.ccnx.ccn.io.content.CCNNetworkObject;
import org.ccnx.ccn.io.content.ContentGoneException;
import org.ccnx.ccn.io.content.ContentNotReadyException;
import org.ccnx.ccn.io.content.UpdateListener;
import org.ccnx.ccn.protocol.ContentName;
import org.ccnx.ccn.protocol.MalformedContentNameStringException;

public class GlobalSnapshotUpdateListener implements UpdateListener {
	private Parameters parameters;

	public GlobalSnapshotUpdateListener (Parameters parameters) {
		this.parameters = parameters;
	}

	@Override
	public void newVersionAvailable(CCNNetworkObject<?> arg0, boolean arg1) {
		CCNFileObject newVersion = (CCNFileObject) arg0;

		try {
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(newVersion.contents())));

			String line = bufferedReader.readLine();

			while (line != null) {
				String[] values = line.split(",");

				if (values[0].equals("true")) { // File Exists
					if (!parameters.sharedFiles.containsKey(values[1])) {
						/** Create NetworkObject */
						
						try {
							CCNFileObject networkObject = new CCNFileObject(ContentName.fromNative(parameters.getNamespace() + values[1]), parameters.getHandle());

							/** Setup Save to Repository */
							networkObject.setupSave(SaveType.REPOSITORY);
							
							parameters.sharedFiles.put(values[1], new FileInformation(true, networkObject));

							/** Add Listener */
							try {
								FileUpdateListener listener = new FileUpdateListener(parameters, values[1]);
								networkObject.updateInBackground(true, listener);

								if(networkObject.available()) {
									listener.newVersionAvailable(networkObject, true);
								}
							} 
							catch (IOException e) {
								e.printStackTrace();
							}
						} 
						catch (MalformedContentNameStringException e) {
							e.printStackTrace();
						}
					}
					else {
						/** Add Listener */
						if (!parameters.sharedFiles.get(values[1]).getExists()) {
							parameters.sharedFiles.get(values[1]).setExists(true);
							FileUpdateListener listener = new FileUpdateListener(parameters, values[1]);
						
							if(parameters.sharedFiles.get(values[1]).networkObject.available()) {
								listener.newVersionAvailable(parameters.sharedFiles.get(values[1]).networkObject, true);
							}
						}
					}
				}
				else { // File Does not exist
					if (parameters.sharedFiles.containsKey(values[1])) { // Hashtable Hit
						FileInformation fileInfo = parameters.sharedFiles.get(values[1]);
						
						if (fileInfo.getExists()) { // Hashtable says local file exists
							fileInfo.networkObject.close();
							fileInfo.setExists(false);

							// Delete File from Shared Folder
							File deleteFile = new File(parameters.getSharedPath() + values[1]);
							//String parentDirectory = deleteFile.getParent();
							
							if (deleteFile.exists()) {
								fileInfo.setFlag(true);
								deleteFile.delete();
								fileInfo.setFlag(false);	
							}
						}
						else { // Hashtable says local file does not exist
							// Nothing to Do
						}
					}
					else { //Hashtable Miss (No Entry)
						/** Create NetworkObject */
						try {
							CCNFileObject networkObject = new CCNFileObject(ContentName.fromNative(parameters.getNamespace() + values[1]), parameters.getHandle());

							/** Setup Save to Repository */
							networkObject.setupSave(SaveType.REPOSITORY);

							parameters.sharedFiles.put(values[1], new FileInformation(false, networkObject));
							
							/** Add Listener */
							try {
								FileUpdateListener listener = new FileUpdateListener(parameters, values[1]);
								networkObject.updateInBackground(true, listener);

								if(networkObject.available()) {
									listener.newVersionAvailable(networkObject, true);
								}
							} 
							catch (IOException e) {
								e.printStackTrace();
							}
						} 
						catch (MalformedContentNameStringException e) {
							e.printStackTrace();
						}
					}
				}
				
				line = bufferedReader.readLine();
			}
		} 
		catch (ContentNotReadyException e) {
			e.printStackTrace();
		} 
		catch (ContentGoneException e) {
			e.printStackTrace();
		} 
		catch (ErrorStateException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}