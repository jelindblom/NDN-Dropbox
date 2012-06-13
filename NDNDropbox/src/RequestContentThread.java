import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.ccnx.ccn.io.CCNFileInputStream;
import org.ccnx.ccn.io.CCNInputStream;
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
 * This class defines the threads used to request content from the CCNx network.
 * It uses CCNInputStream to grab content from the network and writes it to file
 * in the user's shared directory.
 */
public class RequestContentThread implements Runnable {
	private String sharedPath, namespace;
	private String[] values;
	
	public RequestContentThread (String sharedPath, String namespace, String[] values) {
		this.sharedPath = sharedPath;
		this.namespace = namespace;
		this.values = values;
	}

	public void run() {
		//System.out.println("Requesting: " + namespace + values[1] + "_" + Integer.parseInt(values[2]));
		try {
			CCNInputStream input = new CCNFileInputStream(ContentName.fromNative(namespace + values[1] + "_" + Integer.parseInt(values[2])));
			ByteArrayOutputStream output = new ByteArrayOutputStream();

			byte[] buffer = new byte[1024];

			int readCount = 0, readTotal = 0;

			while ((readCount = input.read(buffer)) != -1) {
				readTotal += readCount;
				output.write(buffer, 0, readCount);
				output.flush();
			}
			output.close();

			FileUtils.writeByteArrayToFile(new File(sharedPath + values[1]), output.toByteArray());

			//System.out.println("Read: " + readTotal + " bytes.");
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedContentNameStringException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}
	}
}
