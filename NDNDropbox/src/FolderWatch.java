import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.io.FileUtils;

import net.contentobjects.jnotify.JNotify;
import net.contentobjects.jnotify.JNotifyException;
import net.contentobjects.jnotify.JNotifyListener;

/**
 * NDN Dropbox: Distributed, Dropbox-like File Sharing Service over NDN
 *  
 * @category Distributed File Sharing
 * @author Jared Lindblom
 * @author Huang (John) Ming-Chun
 * @version 1.0
 * 
 * This class is used as an interface to the Jnotify library.  It defines how
 * a Jnotify event is to be handled by our program, when such an event occurs.
 * When needed, it spawns "file created" threads to upload content to the repository.
 */
public class FolderWatch {
	private int watchID;
	private Parameters parameters;

	public FolderWatch(Parameters parameters) {
		this.watchID = 0;
		this.parameters = parameters;
	}

	public void startJNotify() throws JNotifyException {
		int mask =  JNotify.FILE_ANY;

		boolean watchSubtree = true;  // Recursive

		watchID = JNotify.addWatch(parameters.getSharedPath(), mask, watchSubtree, new JNotifyListener() {
			public void fileRenamed(int wd, String rootPath, String oldName, String newName) {
				/** Add Slash to Names */
				oldName = "/" + oldName;
				newName = "/" + newName;
				
				/** Create File based on Event */
				File file = new File(rootPath + newName);

				/** Does this File meet our criteria? */
				if (file.isFile() && !file.isHidden() && !file.getAbsolutePath().endsWith("~") && file.canRead()) {
					try {
						byte[] byteArray = FileUtils.readFileToByteArray(file);

						MessageDigest md = MessageDigest.getInstance("SHA-1");

						byte[] digest = md.digest(byteArray);

						/** Have we seen it before? */
						if (parameters.sharedFiles.containsKey(newName)) {
							FileInformation fileInfo = parameters.sharedFiles.get(newName);						

							/** Is it untouched? */
							if(!fileInfo.getFlag()) {
								/** Have we seen this digest? */
								if (fileInfo.getLocalDigest() != null) {
									/** Are the Digests Equal? */
									if (!MessageDigest.isEqual(fileInfo.getLocalDigest(), digest)) {

										/** Reconcile */
										Runnable runnable = new ReconcileThread(file, newName, parameters);
										parameters.taskProgress.add(parameters.putFileThreadPool.submit(runnable));
									}
								}
							}
						}
						else {

							/** Reconcile */
							Runnable runnable = new ReconcileThread(file, newName, parameters);
							parameters.taskProgress.add(parameters.putFileThreadPool.submit(runnable));

							/** Snapshot Update Needed */
							NDNDropbox.updateNeeded = true;
						}					
					} 
					catch (NoSuchAlgorithmException e) {
						e.printStackTrace();
					} 
					catch (IOException e) {
						e.printStackTrace();
					}
				}

				/** Delete Content under Old Name */
				if (!oldName.equalsIgnoreCase("/null")) {
					if(parameters.sharedFiles.containsKey(oldName)) {
						FileInformation fileInfo = parameters.sharedFiles.get(oldName);

						/** Does the old file still exist? */
						if (fileInfo.getExists()) {
							fileInfo.setExists(false);

							/** Snapshot Update Needed */
							NDNDropbox.updateNeeded = true;
						}
					}
				}
			}

			public void fileModified(int wd, String rootPath, String name) {

				/** Add slash to Name */
				name = "/" + name;

				/** Create File Based on Event */
				File file = new File(rootPath + name);

				/** Does this File meet our criteria? */
				if (file.isFile() && !file.isHidden() && !file.getAbsolutePath().endsWith("~") && file.canRead()) {
					try {
						byte[] byteArray = FileUtils.readFileToByteArray(file);

						MessageDigest md = MessageDigest.getInstance("SHA-1");

						byte[] digest = md.digest(byteArray);

						/** Have we seen it before? */
						if (parameters.sharedFiles.containsKey(name)) {
							FileInformation fileInfo = parameters.sharedFiles.get(name);						

							/** Is it untouched? */
							if(!fileInfo.getFlag()) {
								/** Have we seen this digest? */
								if (fileInfo.getLocalDigest() != null) {

									/** Are the Digests Equal? */
									if (!MessageDigest.isEqual(fileInfo.getLocalDigest(), digest)) {

										/** Reconcile */
										Runnable runnable = new ReconcileThread(file, name, parameters);
										parameters.taskProgress.add(parameters.putFileThreadPool.submit(runnable));
									}
								}
							}
						}

					}
					catch (NoSuchAlgorithmException e) {
						e.printStackTrace();
					} 
					catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			public void fileDeleted(int wd, String rootPath, String name) {
				/** Add Slash to Name */
				name = "/" + name;

				/** Does this file exist? */
				if(parameters.sharedFiles.containsKey(name)) {
					if (parameters.sharedFiles.containsKey(name) && parameters.sharedFiles.get(name).getExists()) {
						parameters.sharedFiles.get(name).setExists(false);	

						/** Snapshot Update Needed */
						NDNDropbox.updateNeeded = true;
					}
				}
			}

			public void fileCreated(int wd, String rootPath, String name) {
				/** Add Slash to Name */
				name = "/" + name;

				/** Create File Based on Event */
				File file = new File(rootPath + name);
				
				/** Does this File meet our criteria? */
				if (file.isFile() && !file.isHidden() && !file.getAbsolutePath().endsWith("~") && file.canRead()) {
					/** Reconcile */
					Runnable runnable = new ReconcileThread(file, name, parameters);
					parameters.taskProgress.add(parameters.putFileThreadPool.submit(runnable));

					/** Snapshot Update Needed */
					NDNDropbox.updateNeeded = true;
				}
			}
		});
	}

	public void stopJNotify() throws JNotifyException {
		boolean res = false;

		res = JNotify.removeWatch(watchID);

		if (!res) {
			// invalid watch ID specified.
		}
	}

	public int getWatchID() {
		return watchID;
	}
}