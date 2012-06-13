import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.ccnx.ccn.CCNHandle;
import org.ccnx.ccn.io.content.CCNNetworkObject;
import org.ccnx.ccn.io.content.ContentDecodingException;
import org.ccnx.ccn.io.content.ContentEncodingException;
import org.ccnx.ccn.protocol.ContentName;

/**
 * NDN Dropbox: Distributed, Dropbox-like File Sharing Service over NDN
 *  
 * @category Distributed File Sharing
 * @author Jared Lindblom
 * @author Huang (John) Ming-Chun
 * @version 1.0
 * 
 * This class defines our version of the CCNNetworkObject, called 
 * CCNFileObject.  It defines how our object is to be read from inputstreams
 * and written to outputstreams.
 */
public class CCNFileObject extends CCNNetworkObject<byte[]> {
	
	public CCNFileObject(Class<byte[]> type, boolean contentIsMutable, ContentName name, CCNHandle handle, byte[] data) throws ContentDecodingException, IOException {
		super(type, contentIsMutable, name, handle);
		setData(data);
	}

	@Override
	protected byte[] readObjectImpl(InputStream arg0) throws ContentDecodingException, IOException {
		/** InputStream to byte[] */	
		byte[] temp = new byte[arg0.available()];
		arg0.read(temp);
		return temp;
	}

	@Override
	protected void writeObjectImpl(OutputStream arg0) throws ContentEncodingException, IOException {
		/** byte[] to OutputStream */
		arg0.write(data());		
	}
}
