import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.ccnx.ccn.protocol.ContentName;
import org.ccnx.ccn.protocol.MalformedContentNameStringException;

/**
 * NDN Dropbox: Distributed, Dropbox-like File Sharing Service over NDN
 *  
 * @category Distributed File Sharing
 * @author Jared Lindblom
 * @author Huang (John) Ming-Chun
 * @version 1.0
 */
public class NDNDriveGUI implements WindowListener{
	static JFrame myWindow;
	static JLabel repoLabel;
	static JLabel sharedLabel;
	static JLabel topoLabel;
	static JLabel nsLabel;
	static JTextField repoPath;
	static JTextField sharedPath;
	static JTextField topoName;
	static JTextField nsName;
	static JButton repoButton;
	static JButton sharedButton;
	static JButton topoButton;
	static JButton nsButton;
	static JButton confirmButton;
	static String oldRepoPath;
	static String oldSharedPath;
	
	public static void main(String[] args){
		myWindow = new JFrame("NDNDriveGUI");
		GroupLayout gl_contentPanel = new GroupLayout(myWindow.getContentPane());
        myWindow.setLayout(gl_contentPanel);
        repoLabel = new JLabel(" Repository Path");
      	sharedLabel = new JLabel(" Shared Folder Path");
      	topoLabel = new JLabel(" NDN Topology");
      	nsLabel = new JLabel(" NDN Namespace");
      	repoPath = new JTextField(32);
      	sharedPath = new JTextField(32);
      	topoName = new JTextField(32);
      	nsName = new JTextField(32);
      	repoButton = new JButton("Search"); 
      	repoButton.addActionListener(new ActionListener() {      		 
            public void actionPerformed(ActionEvent e)
            {
            	JFileChooser chooser = new JFileChooser(repoPath.getText());
   	 		 	chooser.setDialogTitle("Select a Repository Directory");
   	 		 	chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
   	 		 	chooser.setMultiSelectionEnabled(false);
   	 		 	int returnVal = chooser.showOpenDialog(myWindow);
   	 		
   	 		 	if(returnVal == JFileChooser.APPROVE_OPTION) {
   	 		 		repoPath.setText(chooser.getSelectedFile().getAbsolutePath());
   	 		 	}
            }});      
      	sharedButton = new JButton("Search");
      	sharedButton.addActionListener(new ActionListener() {      		 
      		public void actionPerformed(ActionEvent e)
            {
            	JFileChooser chooser = new JFileChooser(sharedPath.getText());
   	 		 	chooser.setDialogTitle("Select a Shared Folder");
   	 		 	chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
   	 		 	chooser.setMultiSelectionEnabled(false);
   	 		 	int returnVal = chooser.showOpenDialog(myWindow);
   	 		
   	 		 	if(returnVal == JFileChooser.APPROVE_OPTION) {
   	 		 		sharedPath.setText(chooser.getSelectedFile().getAbsolutePath());
   	 		 	}
            }});  
      	topoButton = new JButton("Validate");
      	topoButton.addActionListener(new ActionListener() {      		 
            public void actionPerformed(ActionEvent e)
            {
            	try {
					ContentName.fromNative(topoName.getText());
				} 
				catch (MalformedContentNameStringException e1) {
					topoName.setText("");
				}
            }});
      	nsButton = new JButton("Validate");
      	nsButton.addActionListener(new ActionListener() {      		 
            public void actionPerformed(ActionEvent e)
            {
            	try {
					ContentName.fromNative(nsName.getText());
				} 
				catch (MalformedContentNameStringException e1) {
					nsName.setText("");
				}
            }});
      	confirmButton = new JButton("Confirm");
      	confirmButton.addActionListener(new ActionListener() {
      		String repoDirectoryPath = "", sharedDirectoryPath = "", topologyString = "", namespaceString = "";
      		ContentName topology = null, namespace = null, snapshot = null;
      		
            public void actionPerformed(ActionEvent e)
            {
            	boolean success = true;
            	repoDirectoryPath = repoPath.getText();
            	File RepoDir = new File(repoDirectoryPath);
				if(!RepoDir.exists()){
					System.out.println("No Repo Directory");
					repoPath.setText(oldRepoPath);
					success = false;				
				} 
				sharedDirectoryPath = sharedPath.getText();
				File SharedDir = new File(sharedDirectoryPath);
				if(!SharedDir.exists()){
					System.out.println("No Shared Folder");	
					sharedPath.setText(oldSharedPath);
					success = false;				
				}
            	try {
            		topologyString = topoName.getText();
					topology = ContentName.fromNative(topologyString);
				} 
				catch (MalformedContentNameStringException e1) {
					topoName.setText("");
					success = false;
				}
            	
            	try {
            		namespaceString = nsName.getText();
					namespace = ContentName.fromNative(namespaceString);
					snapshot = ContentName.fromNative(namespaceString + "/snapshot");	
				} 
				catch (MalformedContentNameStringException e1) {
					nsName.setText("");
					success = false;
				}
            	
            	if(success){
            		myWindow.dispose();
            		File removeSetup = new File(NDNDropbox.NDNSETUP);
            		if(removeSetup.exists()) {
            			removeSetup.delete();
            		}
            		BufferedWriter writer;
					try {
						writer = new BufferedWriter(new FileWriter(NDNDropbox.NDNSETUP));
						writer.write(repoDirectoryPath+'\n');
						writer.write(sharedDirectoryPath+'\n');
						writer.write(topologyString+'\n');
						writer.write(namespaceString);
						writer.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
            		MainProgram MainProgram = new MainProgram(
            				    				repoDirectoryPath,
            				    				sharedDirectoryPath, 
            				    				topologyString, 
            				    				namespaceString,
            				    				topology, 
            				    				namespace, 
            				    				snapshot);
            		MainProgram.RunProgram();
            	}
            }});

  		gl_contentPanel.setHorizontalGroup(
  		        gl_contentPanel.createSequentialGroup()
  		                       .addGroup(gl_contentPanel.createParallelGroup(GroupLayout.Alignment.LEADING)
  		                       .addComponent(repoLabel)
  		                       .addComponent(sharedLabel)
  		                       .addComponent(topoLabel)
  		                       .addComponent(nsLabel))
  		                       .addGroup(gl_contentPanel.createParallelGroup(GroupLayout.Alignment.LEADING)
  		                       .addComponent(repoPath)
  		                       .addComponent(sharedPath)
  		                       .addComponent(topoName)
  		                       .addComponent(nsName))
  							   .addGroup(gl_contentPanel.createParallelGroup(GroupLayout.Alignment.LEADING)
  							   .addComponent(repoButton)
  							   .addComponent(sharedButton)
  							   //.addComponent(topoButton)
  							   //.addComponent(nsButton)
  							   .addComponent(confirmButton)));
  		gl_contentPanel.setVerticalGroup(
  				gl_contentPanel.createSequentialGroup()
  							   .addGroup(gl_contentPanel.createParallelGroup(GroupLayout.Alignment.BASELINE)
  		                       .addComponent(repoLabel)
  		                       .addComponent(repoPath)
  		                       .addComponent(repoButton))
  		                       .addGroup(gl_contentPanel.createParallelGroup(GroupLayout.Alignment.BASELINE)
  		                       .addComponent(sharedLabel)
  		                       .addComponent(sharedPath)
  		                       .addComponent(sharedButton))
  		                       .addGroup(gl_contentPanel.createParallelGroup(GroupLayout.Alignment.BASELINE)
  		                       .addComponent(topoLabel)
  		                       .addComponent(topoName))
  		                       //.addComponent(topoButton))
  		                       .addGroup(gl_contentPanel.createParallelGroup(GroupLayout.Alignment.BASELINE)
  		                       .addComponent(nsLabel)
  		                       .addComponent(nsName))
  		                       //.addComponent(nsButton))
  		                       .addGroup(gl_contentPanel.createParallelGroup(GroupLayout.Alignment.BASELINE)  		                       
  							   .addComponent(confirmButton)));
		myWindow.pack();
		
		//String NDNSETUP = System.getProperty("user.home") + "/.ccnx/NDNSetup.txt";
		File mysetup = new File(NDNDropbox.NDNSETUP);
		if(mysetup.exists()) {
			try {
					BufferedReader reader = new BufferedReader(new FileReader(NDNDropbox.NDNSETUP));
					String line = null;
					List<String> fileContents = new ArrayList<String>();
					while((line=reader.readLine())!=null){
						fileContents.add(line);
					}
					reader.close();
					if(fileContents.size()!=NDNDropbox.LIMIT_NUM_OF_CONTENT) {
						mysetup.delete();
					} else {
						oldRepoPath = fileContents.get(0);
						repoPath.setText(fileContents.get(0));
						oldSharedPath = fileContents.get(1);
						sharedPath.setText(fileContents.get(1));
						topoName.setText(fileContents.get(2));
						nsName.setText(fileContents.get(3));
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		myWindow.setVisible(true);
  }
		
	@Override
	public void windowActivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosed(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosing(WindowEvent arg0) {
		// TODO Auto-generated method stub
		myWindow.dispose();
        System.exit(0);
	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeiconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowIconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowOpened(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}
