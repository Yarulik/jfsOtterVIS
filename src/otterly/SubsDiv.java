package otterly;
/*
 * Licences
 *
 *  All my software in source or binary form are under the FreeBSD-license.
 *  
 *	For the software used libraries refer to
 *
 *	log4j https://logging.apache.org/log4j/1.2/license.html
 *  rxtx http://rxtx.qbang.org/wiki/index.php/Main_Page
 *  plotapplet https://ptolemy.eecs.berkeley.edu/
 *	miglayout http://www.miglayout.com/mavensite/license.html
 */
public class SubsDiv
 {
	
   public static int toUnsignedInt(byte value)
   {
     return (value & 0x7F) + (value < 0 ? 128 : 0);
   }
   	
   public static String bytesToHex(byte[] bytes) {
	    final char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
	    char[] hexChars = new char[bytes.length * 2];
	    int v;
	    for ( int j = 0; j < bytes.length; j++ ) {
	        v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
}