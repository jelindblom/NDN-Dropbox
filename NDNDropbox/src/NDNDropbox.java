import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.Future;

import org.apache.commons.io.FileUtils;
import org.ccnx.ccn.protocol.ContentName;
import org.ccnx.ccn.protocol.MalformedContentNameStringException;

import java.util.List;

/**
 * NDN Dropbox: Distributed, Dropbox-like File Sharing Service over NDN
 *  
 * @category Distributed File Sharing
 * @author Jared Lindblom
 * @author Huang (John) Ming-Chun
 * @version 1.0
 */
public class NDNDropbox {	
	static volatile boolean FinishSetup = false;
	static String NDNSETUP=System.getProperty("user.home") + "/.ccnx/NDNSetup.txt"; 
	static final int LIMIT_NUM_OF_CONTENT = 4;
	// 1. Repo Path, 2. Shared Folder Path, 3. Topo, 4. Namespace
	
	public static void main (String[] args) {		
		/** Get Parameter Information from User */
		//String NDNSETUP = System.getProperty("user.home") + "/.ccnx/NDNSetup.txt";
			
		String repoDirectoryPath = "", sharedDirectoryPath = "", topologyString = "", namespaceString = "";
		ContentName topology = null, namespace = null, snapshot = null;
		
		System.out.println("Game Start");
		File mysetup = new File(NDNSETUP);
		
		while (FinishSetup != true) {
			List<String> fileContents = new ArrayList<String>();
			if(!mysetup.exists()){
				try {
					mysetup.createNewFile();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				Scanner input = new Scanner(System.in);
				boolean successful = false;
				File repoDirectory;				
				do {
					System.out.print("Repository Directory Path: ");
					repoDirectoryPath = input.nextLine();
					
					// Open Repository Directory
					repoDirectory = new File(repoDirectoryPath);
				
					if (repoDirectory.exists() && repoDirectory.isDirectory()) {
						successful = true;
					}
					else {
						repoDirectory.mkdirs();
						System.out.println("Repository Directory is created");
						successful = true;
					}
				}
				while (!successful);
				
				successful = false;
				File sharedDirectory;				
				do {
					System.out.print("Shared Directory Path: ");
					sharedDirectoryPath = input.nextLine();
					
					// Open Shared Directory
					sharedDirectory = new File(sharedDirectoryPath);
				
					if (sharedDirectory.exists() && sharedDirectory.isDirectory()) {
						successful = true;
					}
					else {
						System.out.println("Shared Directory is created");
						sharedDirectory.mkdirs();
						successful = true;
					}
				}
				while (!successful);
				
				// Topology 
				successful = false;				
				do {
					System.out.print("Topology: ");
					topologyString = input.nextLine();
					
					try {
						topology = ContentName.fromNative(topologyString);
						successful = true;
					} 
					catch (MalformedContentNameStringException e) {
						System.out.println("Topology is Malformed... Please try again.");
						successful = false;
					}
				}
				while (!successful);
				
				// Namespace
				successful = false;
				do {
					System.out.print("Namespace: ");
					namespaceString = input.nextLine();
					
					try {
						namespace = ContentName.fromNative(namespaceString);
						snapshot = ContentName.fromNative(namespaceString + "/snapshot");				
						successful = true;				
					} 
					catch (MalformedContentNameStringException e) {
						System.out.println("Namespace is Malformed... Please try again.");
					}
				}
				while (!successful);
			
				// Close Scanner 
				input.close();
				
				try {
					BufferedWriter writer = new BufferedWriter(new FileWriter(NDNSETUP));
					writer.write(repoDirectoryPath+'\n');
					writer.write(sharedDirectoryPath+'\n');
					writer.write(topologyString+'\n');
					writer.write(namespaceString);
					writer.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
				FinishSetup = true;
				
			} else {
				System.out.println("NDNSetup.txt exists");
				try {
					BufferedReader reader = new BufferedReader(new FileReader(NDNSETUP));
					String line = null;
					while((line=reader.readLine())!=null){
						fileContents.add(line);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				//Read File Contents
				//Case 1. Three paths are included and valid
				//Case 2. Three paths are included and but some of them are invalid
				//Case 3. Some paths are missing
				System.out.println("Total Number of Contents: "+fileContents.size());			
				if(fileContents.size() != LIMIT_NUM_OF_CONTENT) {
					//Bad file contents, we should delete it and restart
					mysetup.delete();
				} else {
					FinishSetup = true;
					// Check if everything is valid
					File RepoDir = new File(fileContents.get(0));
					if(!RepoDir.exists()){
						System.out.println("No Repo Directory");						
						FinishSetup = false;				
					}
					File SharedDir = new File(fileContents.get(1));
					if(!SharedDir.exists()){						
						System.out.println("No Shared Directory");
						FinishSetup = false;
					}					
					if(fileContents.get(2).isEmpty()){
						System.out.println("No Topo");
						FinishSetup = false;
					}
					if(fileContents.get(3).isEmpty()){
						System.out.println("No Namespace");
						FinishSetup = false;
					}
					if(FinishSetup == false) {
						mysetup.delete();
					} else {
						repoDirectoryPath = fileContents.get(0);
					    sharedDirectoryPath = fileContents.get(1);
					    topologyString = fileContents.get(2);
					    namespaceString = fileContents.get(3);
						try {
							topology = ContentName.fromNative(topologyString);
						} catch (MalformedContentNameStringException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						};
						try {
							namespace = ContentName.fromNative(namespaceString);
						} catch (MalformedContentNameStringException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						try {
							snapshot = ContentName.fromNative(namespaceString + "/snapshot");
						} catch (MalformedContentNameStringException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}		
				}
			}
		}
		System.out.println("Game End");
		try {
			Runtime.getRuntime().exec("ccndstop");
			Runtime.getRuntime().exec("ccndstart");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			File tmpRepo = new File(repoDirectoryPath);
			
			FileUtils.cleanDirectory(tmpRepo);
			
			Runtime.getRuntime().exec("ccnr",null, tmpRepo);
		} 
		catch (IOException e1) {
			e1.printStackTrace();
		}
		System.out.println("Bootstrap End");
				
		/** Initialize Parameters */		
		Parameters parameters = new Parameters(sharedDirectoryPath, topology, namespace, snapshot);			
		
		/** Build Local Shared Folder Hashtable */
		//Startup.Populate(parameters.getSharedDirectoryPath(), parameters);
		
		/**  Start Jnotify */
		try {	
			/** Create Instance of JNotify */
			FolderWatch folderWatch = new FolderWatch(parameters);
			
			/** Start JNotify */
			folderWatch.startJNotify();
			
			/** Loop Forever */
			while(true) {
				/** Snapshot update needed? */
				if (!parameters.taskProgress.isEmpty()) {
					synchronized(parameters.taskProgress) {
						@SuppressWarnings("rawtypes")
						Runnable runnable = new GlobalSnapshotThread(parameters, new ArrayList<Future>(parameters.taskProgress));
						
						parameters.threadPool.submit(runnable);
						parameters.taskProgress.removeAll(parameters.taskProgress);
					}
				}
				Thread.sleep(2000);
			}
		} 
		catch (IOException e) {
			System.out.println("JNotify could not start.");
			return;
		} 
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
