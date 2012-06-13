import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Hashtable;

import org.apache.commons.io.FileUtils;
import org.bouncycastle.util.encoders.Hex;
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
 * This class defines the threads used to upload content from the local
 * shared folder to ccnr.  It also calculates a sha1 digest of the file
 * contents and adds an entry into the local snapshot hashtable, sharedFiles.
 */
public class FileCreatedThread implements Runnable {
	private int wd;
	private CCNHandle handle;
	private String rootPath, name, namespace;
	private Hashtable<String, FileInformation> sharedFiles;

	public FileCreatedThread(int wd, String rootPath, String name, String namespace, Hashtable<String, FileInformation> sharedFiles, CCNHandle handle) {
		this.wd = wd;
		this.rootPath = rootPath;
		this.name = name;
		this.namespace = namespace;
		this.handle = handle;
		this.sharedFiles = sharedFiles;
	}

	public void run() {
		try {
			Thread.sleep(250);

			CCNFileObject networkObject;

			/** Create File */
			File file = new File(rootPath + name);

			if (file.isFile()) {
				/** Convert File Contents to byte[] */
				byte[] byteArray = FileUtils.readFileToByteArray(file);

				/** Calculate SHA1 */
				MessageDigest md = MessageDigest.getInstance("SHA-1");

				String sha1 = new String(Hex.encode(md.digest(byteArray)));

				if (sharedFiles.containsKey(name)) {
					if (!sharedFiles.get(name).getSha1().equals(sha1)) {
						sharedFiles.get(name).setVersion(sharedFiles.get(name).getVersion() + 1);
						sharedFiles.get(name).setExists(true);
					}
					else {
						return;
					}
				}
				else {
					sharedFiles.put(name, new FileInformation(1, true, sha1));
				}
				
				/** Create NetworkObject */
				networkObject = new CCNFileObject(byte[].class, true, ContentName.fromNative(namespace + name + "_" + sharedFiles.get(name).getVersion()), handle, byteArray);

				/** Save to Repository */
				networkObject.setupSave(SaveType.REPOSITORY);
				networkObject.save();
				networkObject.close();
			}			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedContentNameStringException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
