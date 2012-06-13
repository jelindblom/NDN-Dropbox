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
	private int version;
	private boolean exists;
	private String sha1;
	
	public FileInformation(int version, boolean exists, String sha1) {
		this.version = version;
		this.exists = exists; 
		this.sha1 = sha1;
	}

	public int getVersion() {
		return version;
	}
	
	public boolean getExists() {
		return exists;
	}
	
	public String getSha1() {
		return sha1;
	}
	
	public void setVersion(int version) {
		this.version = version; 
	}
	
	public void setExists(boolean exists) {
		this.exists = exists;
	}
	
	public void setSha1(String sha1) {
		this.sha1 = sha1;
	}
}
