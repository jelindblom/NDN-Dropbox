import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Hashtable;

import org.apache.commons.io.FileUtils;
import org.bouncycastle.util.encoders.Hex;

/**
 * NDN Dropbox: Distributed, Dropbox-like File Sharing Service over NDN
 *  
 * @category Distributed File Sharing
 * @author Jared Lindblom
 * @author Huang (John) Ming-Chun
 * @version 1.0
 * 
 * This is a static class used to initially scan the shared folder and populate
 * the hashtable, sharedFiles.
 */
public class InitialScan {
	public static void ScanFiles(String sharedPath, Hashtable<String, FileInformation> sharedFiles) {
		File sharedDirectory = new File(sharedPath);
		File[] listOfFiles = sharedDirectory.listFiles();
		
		for (int j = 0; j < listOfFiles.length; j++) {
			if (listOfFiles[j].isDirectory()) {
				ScanFiles(listOfFiles[j].getPath(), sharedFiles);
			} else if(listOfFiles[j].isFile()) {
				/** Create File */
				File file = new File(listOfFiles[j].getPath());
				
				/** Convert File Contents to byte[] */
				try {
					byte[] byteArray = FileUtils.readFileToByteArray(file);

					/** Calculate SHA1 */
					MessageDigest md = MessageDigest.getInstance("SHA-1");

					String sha1 = new String(Hex.encode(md.digest(byteArray)));
					
					sharedFiles.put("/" + listOfFiles[j].getName(), new FileInformation(1, true, sha1));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else {
				// Ignore
			}
		}
	}
}
