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
   	
}