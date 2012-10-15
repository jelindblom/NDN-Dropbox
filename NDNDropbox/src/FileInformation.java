/**
 * NDN Dropbox: Distributed, Dropbox-like File Sharing Service over NDN
 *  
 * @category Distributed File Sharing
 * @author Jared Lindblom
 * @author Huang (John) Ming-Chun
 * @version 1.0
 * 
 * This class holds file information like version, if it exists or not, and 
 * the sha1 of the file's content.  It is used by the hashtable, sharedFiles, 
 * to maintain a local snapshot of the shared folder directory.
 */
public class FileInformation {
	private boolean exists, beingModified;
	private byte[] localDigest;
	CCNFileObject networkObject;
	
	public FileInformation(boolean exists, CCNFileObject networkObject) {
		this.exists = exists;
		this.beingModified = false;
		this.localDigest = null;
		this.networkObject = networkObject;
	}
	
	public boolean getExists() {
		return exists;
	}
	
	public boolean getFlag() {
		return beingModified;
	}
	
	public byte[] getLocalDigest() {
		return localDigest;
	}
	
	public void setExists(boolean exists) {
		this.exists = exists;
	}
	
	public void setFlag(boolean beingModified) {
		this.beingModified = beingModified;
	}
	
	public void setLocalDigest(byte[] localDigest) {
		this.localDigest = localDigest;
	}
}