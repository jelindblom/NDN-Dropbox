import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.ExecutorService;

import org.apache.commons.io.FileUtils;
import org.ccnx.ccn.CCNHandle;
import org.ccnx.ccn.io.CCNFileInputStream;
import org.ccnx.ccn.io.CCNInputStream;
import org.ccnx.ccn.profiles.nameenum.BasicNameEnumeratorListener;
import org.ccnx.ccn.profiles.nameenum.CCNNameEnumerator;
import org.ccnx.ccn.protocol.ContentName;
import org.ccnx.ccn.protocol.ContentObject;
import org.ccnx.ccn.protocol.MalformedContentNameStringException;

/**
 * NDN Dropbox: Distributed, Dropbox-like File Sharing Service over NDN
 *  
 * @category Distributed File Sharing
 * @author Jared Lindblom
 * @author Huang (John) Ming-Chun
 * @version 1.0
 * 
 * This class handles callbacks from ccnr.  It parses the most recent global
 * snapshot and updates our local snapshot (hashtable: sharedFiles).  When needed,
 * it spawns "request content" threads to download new content from the CCNx network.
 */
public class FileNameEnumerator implements BasicNameEnumeratorListener {
	private String sharedPath;
	private String namespace;
	private String snapshot;
	private CCNHandle getHandle;
	private ExecutorService getFileThreadPool;
	private CCNNameEnumerator getNameEnumerator;
	private Hashtable<String, FileInformation> sharedFiles;

	public FileNameEnumerator(String sharedPath, Hashtable<String, FileInformation> sharedFiles, String namespace, String snapshot, CCNHandle getHandle, ExecutorService getFileThreadPool) {
		this.sharedPath = sharedPath;
		this.namespace = namespace;
		this.snapshot = snapshot;
		this.getHandle = getHandle;
		this.sharedFiles = sharedFiles;
		this.getNameEnumerator = new CCNNameEnumerator(getHandle, this);
		this.getFileThreadPool = getFileThreadPool;
	}

	public void registerNameSpace(ContentName namespace) {
		try {
			getNameEnumerator.registerNameSpace(namespace);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void registerPrefix(ContentName prefix) {
		try {
			getNameEnumerator.registerPrefix(prefix);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public int handleNameEnumerator(ContentName p, ArrayList<ContentName> names) {
		/** Compare Local Shared Files with Repository */
		System.out.println("Got a Callback! Comparing Local Files with Repository...");

		int temp = 0, tempVersion = 0;
		for (int i = 0; i < names.size(); i++) {
			temp = Integer.parseInt(names.get(i).toString().substring(names.get(i).toString().indexOf("_") + 1));
			if (temp > tempVersion) {
				tempVersion = temp;
			}
		}

		byte[] snapshotByteArray = null;
		synchronized(this) {
			if(tempVersion > 0 && tempVersion >= NDNDropbox.snapshotVersion) {				
				NDNDropbox.snapshotVersion = tempVersion;

				CCNInputStream input;
				try {
					input = new CCNFileInputStream(ContentName.fromNative(snapshot + "/snapshot" + "_" + NDNDropbox.snapshotVersion));

					ByteArrayOutputStream output = new ByteArrayOutputStream();

					byte[] buffer = new byte[1024];

					int readCount = 0;

					while ((readCount = input.read(buffer)) != -1) {
						output.write(buffer, 0, readCount);
						output.flush();
					}
					output.close();

					snapshotByteArray = output.toByteArray();

					System.out.print("Requesting Snapshot: Version " + NDNDropbox.snapshotVersion + "... ");

					if (snapshot != null) {
						System.out.println("Successful.");
						//System.out.println("\tParsing Snapshot: Version " + NDNDropbox.snapshotVersion);

						BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(snapshotByteArray)));				

						String line = bufferedReader.readLine();

						while (line != null) {
							String[] values = line.split(",");

							if (values.length < 3 || values.length > 4) {
								//System.out.println("Parse Error");
							}
							else {
								//System.out.println("\t\tFile Exists?: " + values[0]);
								//System.out.println("\t\tFile Name: " + values[1]);
								//System.out.println("\t\tFile Version: " + values[2]);
								//System.out.println("\t\tSHA1: " + values[3]);

								if (values[0].equals("true")) { // File Exists
									if (sharedFiles.containsKey(values[1])) { // Hashtable Hit
										//System.out.println("\t<--HashTable Hit-->");

										if (sharedFiles.get(values[1]).getExists() == true) { // Hashtable says local file exists
											if (sharedFiles.get(values[1]).getVersion() < Integer.parseInt(values[2])) { // File update Needed?
												Runnable runnable = new RequestContentThread(sharedPath, namespace, values);
												getFileThreadPool.submit(runnable);
											}
										}
										else { // Hashtable says local file does not exist
											sharedFiles.get(values[1]).setExists(true);

											Runnable runnable = new RequestContentThread(sharedPath, namespace, values);
											getFileThreadPool.submit(runnable);
										}
									}
									else { // Hashtable Miss (No Entry)
										//System.out.println("<--Hash Table Miss-->");

										sharedFiles.put(values[1], new FileInformation(Integer.parseInt(values[2]), true, values[3]));

										Runnable runnable = new RequestContentThread(sharedPath, namespace, values);
										getFileThreadPool.submit(runnable);
									}
								}
								else { // File Does not exist
									//System.out.println("<--File Does not Exist-->");
									if (sharedFiles.containsKey(values[1])) { // Hashtable Hit
										if (sharedFiles.get(values[1]).getExists() == true) { // Hashtable says local file exists
											sharedFiles.get(values[1]).setExists(false);
											
											// Delete File from Shared Folder
											File deleteFile = new File(sharedPath + values[1]);
											if (deleteFile.exists()) {
												deleteFile.delete();
											}
										}
										else { // Hashtable says local file does not exist
											// Nothing to Do
										}
									}
									else { //Hashtable Miss (No Entry)
										sharedFiles.put(values[1], new FileInformation(Integer.parseInt(values[2]), false, values[3]));
									}
								}
							}

							line = bufferedReader.readLine();
						}
					}
					else {
						System.out.println("Unsuccessful.");
					}
				} catch (MalformedContentNameStringException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return 0;
	}
}