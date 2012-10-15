import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Hashtable;

import org.apache.commons.io.FileUtils;
import org.ccnx.ccn.io.ErrorStateException;
import org.ccnx.ccn.io.content.CCNNetworkObject;
import org.ccnx.ccn.io.content.ContentGoneException;
import org.ccnx.ccn.io.content.ContentNotReadyException;
import org.ccnx.ccn.io.content.UpdateListener;

public class FileUpdateListener implements UpdateListener {
	private String sharedPath, name;
	Hashtable<String, FileInformation> sharedFiles;

	public FileUpdateListener(Parameters parameters, String name) {
		this.sharedPath = parameters.getSharedPath();
		this.name = name;
		this.sharedFiles = parameters.sharedFiles;
	}

	@Override
	public void newVersionAvailable(CCNNetworkObject<?> arg0, boolean arg1) {
		try {
			/** Extract contents from network object */
			CCNFileObject newVersion = (CCNFileObject) arg0;
			byte[] newVersionContents = newVersion.contents();

			/** Does this content exist in our local hashtable? */
			if(sharedFiles.containsKey(name)) {
				FileInformation fileInfo = sharedFiles.get(name);

				/** Should the content exist in our local shared folder? */
				if (fileInfo.getExists()) {
					MessageDigest md = MessageDigest.getInstance("SHA-1");

					byte[] newVersionDigest = md.digest(newVersionContents);

					File file = new File(sharedPath + name);

					/** Does the content exist in our local shared folder? */
					if (file.exists() && file.canRead() && file.canWrite()) {
						/** Has the content been saved to the shared folder before? */
						if (fileInfo.getLocalDigest() != null) {

							/** Are the digests equal? */
							if (MessageDigest.isEqual(fileInfo.getLocalDigest(), newVersionDigest)) {
								return;
							}
						}
					}
					
					/** Write content to file */
					fileInfo.setFlag(true);
					FileUtils.writeByteArrayToFile(file, newVersionContents);
					fileInfo.setFlag(false);

					/** Update New Digest */
					fileInfo.setLocalDigest(newVersionDigest);
				}
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
		catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}	
}
