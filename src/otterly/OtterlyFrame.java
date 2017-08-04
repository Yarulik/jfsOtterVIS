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
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;




import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class OtterlyFrame extends JFrame {
	/*
	 * Logging etc
	 */
	public static Logger log = Logger.getLogger(OtterlyFrame.class.getName());	
	static {
		PropertyConfigurator.configure("jfslog4j.conf");
	}
	public OtterlyFrame() {
		initcomponents();
	}
	private void initcomponents() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JTabbedPane jtab = new JTabbedPane(JTabbedPane.TOP);
		getContentPane().add(jtab);

        JPanel otterly = new jfsOtterly();
        jtab.add("Otterly",otterly);
        setSize(1200, 800);
		setVisible(true);

	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (Exception ignore){
					log.fatal("main could not be started !");
				}
				new OtterlyFrame().setVisible(true);
			}
		});

	}

}
