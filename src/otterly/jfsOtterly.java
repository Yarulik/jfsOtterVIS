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

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.TooManyListenersException;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import ptolemy.plot.Plot;





public class jfsOtterly extends JPanel {
	/*
	 * Logging etc
	 */
	public static Logger log = Logger.getLogger(jfsOtterly.class.getName());	
	static {
		PropertyConfigurator.configure("jfslog4j.conf");
	}
	
	private	Color onColor = Color.green;
	private Color offColor = Color.black;
	private Color errColor = Color.red;

	Enumeration 		ports;
	CommPortIdentifier	portId;
	JComboBox jlistports;
	private Vector<String> getPorts(){
		Vector<String> p = new Vector<String>();
	ports = CommPortIdentifier.getPortIdentifiers();		
	if (ports == null)
	{
		log.error("No comm ports found!");
		return p;
	}
	
	while (ports.hasMoreElements())
	{
		portId = (CommPortIdentifier)ports.nextElement();
		if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL){
			String s =portId.getName();
			p.add(s);
		}
	}
		return p;
	}
	
	CubbyHole ch = new CubbyHole();

	CommPortIdentifier serialPortId;
	Enumeration enumComm;
	SerialPort serialPort;
	OutputStream outputStream;
	InputStream inputStream;
	Boolean serialPortGeoeffnet = false;
	
	DecimalFormat df = new DecimalFormat("####0"); 
	DecimalFormat df1 = new DecimalFormat("####0.#"); 
	
	int baudrate = 115200;
	int dataBits = SerialPort.DATABITS_8;
	int stopBits = SerialPort.STOPBITS_1;
	int parity = SerialPort.PARITY_NONE;
	String portName = "";
	rs232io io;
	JLabel status = new JLabel("                    "); 	// 20 zeichen
	JButton open;
	JButton close;
	Timer time = new Timer();

	


	private Plot plot ;

	private JToggleButton jtbrecord;
	

	public long thrsleep = 20;
	private JTextField jbts;
	
	// otterly
	// Aufnahme
	public static final int  IDLE = 0;
	public static final int  RECORD = 1;
	public static final int  BASELINE = 2;
	public static final int  MULTI = 3;
	// Ausgabe
	public static final int RAW = 0;
	public static final int CALCULATE = 3;
	public static final int TRANSMISSION = 2;
	public static final int EXTENTION = 4;
	
	private int job = IDLE;
	private int out = RAW;
	
	private boolean record = false;	// aufzeichnen

	private boolean gotbase = false; // Baseline vorhanden
	
	static int max_buffer = 7388;
	byte[] datenbuffer = new byte[max_buffer];
	float[] daten = new float[max_buffer/2];
	float[] base = new float[max_buffer/2];

	private Display display = new Display();
	
	int dbpointer = 0;
	boolean dbnew = false;
	int sh_period = 14;
	int icg_period = 14776;
	private JTextField shts;
	byte[] sendbuffer = new byte[] { (byte)0x45, (byte)0x52, 
		(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0E,
		(byte)0x00, (byte)0x00, (byte)0xDA, (byte)0xC0,
		(byte)0x01, (byte)0x00};
	private JLabel bfout;
	private JTextField icgts;
	private JToggleButton jt0;
	private JLabel lblLed;
	private JButton jbclear;
	private JToggleButton jt1;
	private JTextField hights;
	private float high_level=1465;
	private float low_level=3910;
	private JTextField lowts;
	
	
	private void update_send_buffer(){		
		sh_period = Integer.parseInt(shts.getText());
		icg_period = Integer.parseInt(icgts.getText());		
        byte[] bytes = ByteBuffer.allocate(4).putInt(sh_period).array();
        for (int i = 0; i < bytes.length; i++) {
        	 sendbuffer[i+2]=bytes[i];
		}
        bytes = ByteBuffer.allocate(4).putInt(icg_period).array();
        for (int i = 0; i < bytes.length; i++) {
        	 sendbuffer[i+6]=bytes[i];
		}   
        update_parameter();
	}
	
	private JPanel init_rs(){
	    JPanel rs = new JPanel();
	    rs.setLayout(new MigLayout());
	    rs.setBorder(BorderFactory.createTitledBorder("RS 232"));	 
		jlistports = new JComboBox(getPorts());
		portName = (String) jlistports.getItemAt(0);
		rs.add(jlistports,"wrap");
		jlistports.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				 	JComboBox cb = (JComboBox)e.getSource();
			        portName = (String)cb.getSelectedItem();
			
			}
		});

		open = new JButton("open Connection");
		rs.add(open);
		open.addActionListener(new ActionListener() {			
			public void actionPerformed(ActionEvent arg0) {
				open.setEnabled(false);
				close.setEnabled(true);
				jlistports.setEnabled(false);
				io = new rs232io(ch);
				io.start();				
			}
		});
		close = new JButton ("close Connection");
		close.setEnabled(false);
		rs.add(close,"wrap");
		close.addActionListener(new ActionListener() {			
			@SuppressWarnings("deprecation")
			@Override
			public void actionPerformed(ActionEvent arg0) {
				io.schliesseSerialPort();
				io.stop();
				open.setEnabled(true);
				close.setEnabled(false);
				jlistports.setEnabled(true);
			}
		});
		status.setForeground(Color.BLUE);
		rs.add(status);
		return rs;			
	}

	private JPanel functions(){
	    JPanel fs = new JPanel();
	    fs.setLayout(new MigLayout());
	    fs.setBorder(BorderFactory.createTitledBorder("Otterly"));
	    JPanel pfs = new JPanel();
	    pfs.setLayout(new MigLayout());
	    pfs.setBorder(BorderFactory.createTitledBorder("Parameter"));
	    fs.add(pfs,"wrap");
	    bfout = new JLabel("");
	    pfs.add(bfout,"span 2, wrap");
	    pfs.add(new JLabel("SH period"));
	    shts = new JTextField(Integer.toString(sh_period),8);
	    pfs.add(shts,"wrap");
	    shts.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void keyReleased(KeyEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void keyPressed(KeyEvent arg0) {
				if (arg0.getKeyCode()==KeyEvent.VK_ENTER){
		            //sh_period = Integer.parseInt(shts.getText());
		            update_send_buffer();
		        }		
				
			}
		});
	    pfs.add(new JLabel("ICG period"));
	    icgts = new JTextField(Integer.toString(icg_period),8);
	    pfs.add(icgts,"wrap");
	    icgts.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void keyReleased(KeyEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void keyPressed(KeyEvent arg0) {
				if (arg0.getKeyCode()==KeyEvent.VK_ENTER){
		            //icg_period = Integer.parseInt(icgts.getText());
		            update_send_buffer();
		        }		
				
			}
		});
	    pfs.add(new JLabel("High level"));
	    hights = new JTextField(Float.toString(high_level),8);
	    pfs.add(hights,"wrap");
	    hights.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void keyReleased(KeyEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void keyPressed(KeyEvent arg0) {
				if (arg0.getKeyCode()==KeyEvent.VK_ENTER){
		            high_level = Float.parseFloat(hights.getText());
		            update_parameter();
		        }		
				
			}
		});
	    pfs.add(new JLabel("Low level"));
	    lowts = new JTextField(Float.toString(low_level),8);
	    pfs.add(lowts,"wrap");
	    lowts.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void keyReleased(KeyEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void keyPressed(KeyEvent arg0) {
				if (arg0.getKeyCode()==KeyEvent.VK_ENTER){
		            low_level = Float.parseFloat(lowts.getText());
		            update_parameter();
		        }		
				
			}
		});
	    //////////////////////
	    jt0 = new JToggleButton(">>",record);
	    jt0.setForeground(offColor);
	    jt0.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (job == IDLE){
					record=true;	
					job=RECORD;
					jt0.setForeground(onColor);
				} 	
			}
		});
	    pfs.add(jt0);
	    lblLed = new JLabel("�");
	    lblLed.setForeground(Color.GREEN);
	    pfs.add(lblLed,"wrap");
	    // Record Baseline
	    jt1 = new JToggleButton("baseline",record);
	    jt1.setForeground(offColor);
	    jt1.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (job == IDLE){
					record=true;
					job=BASELINE;
					gotbase=true;
					jt1.setForeground(onColor);
				} 	
			}
		});
	    pfs.add(jt1,"wrap");
	    jbclear = new JButton("clear screen");
	    pfs.add(jbclear,"wrap");
	    jbclear.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				plot.clear(false);
				plot.repaint();
				
			}
		});
	    // Ausgabe Optionen
	    JPanel outfs = new JPanel();
	    outfs.setLayout(new MigLayout());
	    outfs.setBorder(BorderFactory.createTitledBorder("Ausgabe"));
	    fs.add(outfs,"wrap");
	    ButtonGroup group = new ButtonGroup();
	    final JRadioButton jrbraw = new JRadioButton("Raw");
	    jrbraw.setSelected(true);
	    group.add(jrbraw);
	    outfs.add(jrbraw,"wrap");
	    jrbraw.addItemListener(new ItemListener() {			
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				int state = arg0.getStateChange();
		        if (state == ItemEvent.SELECTED) {	
		        	out = RAW;
		        	log.debug("Raw +");		
		        	update_parameter();
		        } else if (state == ItemEvent.DESELECTED) {	 
		           // log.debug("Raw -"); 
		        }
			}
		});
	    JRadioButton jrbcalc = new JRadioButton("Calculate");
	    jrbcalc.setSelected(true);
	    group.add(jrbcalc);
	    outfs.add(jrbcalc,"wrap");
	    jrbcalc.addItemListener(new ItemListener() {			
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				int state = arg0.getStateChange();
		        if (state == ItemEvent.SELECTED) {	
		        	out = CALCULATE;
		        	log.debug("Calc +");	
		        	update_parameter();
		        } else if (state == ItemEvent.DESELECTED) {	 
		           // log.debug("Raw -"); 
		        }
			}
		});	    
	    JRadioButton jrbtrans = new JRadioButton("Transmission");
	    group.add(jrbtrans);
	    outfs.add(jrbtrans,"wrap");
	    jrbtrans.addItemListener(new ItemListener() {			
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				int state = arg0.getStateChange();
		        if (state == ItemEvent.SELECTED) {	
		        	if (gotbase==false){
		        		JOptionPane.showMessageDialog(null, "Sorry got no Baseline");
		        		jrbraw.setSelected(true);
		        	} else {
		        		out = TRANSMISSION;
		        		log.debug("Trans +");	
		        		update_parameter();
		        	}
		        } else if (state == ItemEvent.DESELECTED) {	 
		           // log.debug("Trans -"); 
		        }
			}
		});	   
	    JRadioButton jrbext = new JRadioButton("Extention");
	    group.add(jrbext);
	    outfs.add(jrbext,"wrap");
	    jrbext.addItemListener(new ItemListener() {			
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				int state = arg0.getStateChange();
		        if (state == ItemEvent.SELECTED) {	
		        	if (gotbase==false){
		        		JOptionPane.showMessageDialog(null, "Sorry got no Baseline");
		        		jrbraw.setSelected(true);
		        	} else {
		        	out = EXTENTION;
		        	log.debug("Ext +");	
		        	update_parameter();
		        	}
		        } else if (state == ItemEvent.DESELECTED) {	 
		           // log.debug("Ext -"); 
		        }
			}
		});	    
	    // Kontolle
	    JPanel xfs = new JPanel();
	    xfs.setLayout(new MigLayout());
	    xfs.setBorder(BorderFactory.createTitledBorder("Aufzeichnung alle [ms]"));
	    fs.add(xfs,"wrap");
	    jtbrecord = new JToggleButton("On", record);
	    jtbrecord.setForeground(offColor);
	    jtbrecord.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (job == IDLE) {
					 thrsleep = Long.parseLong(jbts.getText());
					jtbrecord.setForeground(onColor);
					record = true;
					job = MULTI;
					time.start();
				} else {
					jtbrecord.setForeground(offColor);
					job = IDLE;
					record = false;
					time.end();
				}
			}
		});
	    xfs.add(jtbrecord);
	    jbts = new JTextField(Long.toString(thrsleep),8);
	    xfs.add(jbts);
	    jbts.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void keyReleased(KeyEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode()==KeyEvent.VK_ENTER){
		            thrsleep = Long.parseLong(jbts.getText());
		            jbts.setText(""+df.format(thrsleep));
		        }				
			}
		});
	    return fs;	    
	    
	    
	}
	
	protected void update_parameter() {
		bfout.setText("Up "+df1.format(high_level)+" Low "+df1.format(low_level));	
		showit();
	}

	

	public jfsOtterly(){
		setLayout(new MigLayout());
	   	plot = new Plot();
	    plot.setSize(1000, 600);
	    plot.setButtons(true);
	    plot.setYRange(1000, 4000);
	    plot.setXRange(0, 3700);	 
	    plot.setMarksStyle("pixels");
	    //plot.setConnected(true);
	    plot.setAutomaticRescale(false);
	    add(plot,"span 2 2");
	    add(init_rs(),"wrap");
	    add(functions());
	    update_send_buffer();
	    return;
	}
	
	

	public void showit(){
		float y = 0;
		float y1=0;
		plot.clear(false);
			switch (out){
			case CALCULATE:
				for (int j = 0; j < daten.length; j++) {
				y= low_level - daten[j];
				display.y[j]= (y / (low_level-high_level));
				display.x[j]=j;
				}
			break;
			case TRANSMISSION:
				for (int j = 0; j < daten.length; j++) {
				y= low_level - daten[j];
				display.y[j]= (y / (low_level-high_level));
				
				y1= low_level - base[j];
				if (y1<=0){
					display.y[j]=1;
				} else {
				display.y[j]= display.y[j]/(y1 / (low_level-high_level));
				}
				display.x[j]=j;
				log.debug("j "+j+" y "+y+ " y1 "+ y1+ " disp.y "+display.y[j]);
				}

			break;
			case EXTENTION:
				
			break;	
			default:
				for (int j = 0; j < daten.length; j++) {
					display.y[j]= daten[j];
					display.x[j]=j;
					}				
				break;			
		}   
			display.show();
	}
	
	
		


	public void update(){
		   int count = 0;
		   for (int i = 0; i < (dbpointer/2); i++) {
			//short a = (short) ((datenbuffer[count+1]<<8)| datenbuffer[count]);
			int a  = SubsDiv.toUnsignedInt(datenbuffer[count+1])* 256 + SubsDiv.toUnsignedInt(datenbuffer[count]);
			count += 2;
			if (job == BASELINE){ 
				base[i]=a; 
			}
			else daten[i]=a; 			
		   }
		   log.debug("job "+job+" out "+out);
		   switch (job) {
		   		case RECORD:
		   			job = IDLE;
		   			record=false;	
		   			jt0.setForeground(offColor);	
		   		break;
		   		case BASELINE:
		   			job = IDLE;
		   			record=false;
		   			jt1.setForeground(offColor);
		   		break;	
		   		default:
		   			break;
		   }
		  
		   showit();
	}	
	
	

	class rs232io implements Runnable {
		/**
		 * @uml.property  name="ch"
		 * @uml.associationEnd  
		 */
		private CubbyHole ch;
		private boolean busy = false;
		
		Thread thread = null;
		private Integer i;
		public synchronized void start(){
			if (thread==null){
				thread = new Thread(this);
				thread.start();
			}
		}
		
		public synchronized void stop(){
			thread.stop();
		}
		public rs232io(CubbyHole ch) {
			this.ch = ch;
		}
		private synchronized void doit(int job){
			update_send_buffer();
			sendeSerialPort(job);
		}
		
		@Override
		public void run() {

	        if (oeffneSerialPort(portName) != true)
	        	return;
		while (true){
			if ((record) && (dbnew==false)) doit(job);
			try {
				Thread.sleep(thrsleep );  //vorher 20
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
			}
		
		}
		
		boolean oeffneSerialPort(String portName)
		{
			Boolean foundPort = false;
			if (serialPortGeoeffnet != false) {
				status.setText("Serialport bereits ge�ffnet");
				return false;
			}
			status.setText("�ffne Serialport");
			enumComm = CommPortIdentifier.getPortIdentifiers();
			while(enumComm.hasMoreElements()) {
				serialPortId = (CommPortIdentifier) enumComm.nextElement();
				if (portName.contentEquals(serialPortId.getName())) {
					foundPort = true;
					break;
				}
			}
			if (foundPort != true) {
				status.setText("Serialport nicht gefunden: " + portName);
				return false;
			}
			try {
				serialPort = (SerialPort) serialPortId.open("�ffnen und Senden", 500);
			} catch (PortInUseException e) {
				status.setText("Port belegt");
			}
			try {
				outputStream = serialPort.getOutputStream();
			} catch (IOException e) {
				status.setText("Keinen Zugriff auf OutputStream");
			}

			try {
				inputStream = serialPort.getInputStream();
			} catch (IOException e) {
				status.setText("Keinen Zugriff auf InputStream");
			}
			try {
				serialPort.addEventListener(new serialPortEventListener());
			} catch (TooManyListenersException e) {
				status.setText("TooManyListenersException f�r Serialport");
			}
			serialPort.notifyOnDataAvailable(true);

			try {
				serialPort.setSerialPortParams(baudrate, dataBits, stopBits, parity);
			} catch(UnsupportedCommOperationException e) {
				status.setText("Konnte Schnittstellen-Paramter nicht setzen");
			}
			
			serialPortGeoeffnet = true;
			return true;
		}

		void schliesseSerialPort()
		{
			if ( serialPortGeoeffnet == true && (busy == false)) {
				status.setText("Schlie�e Serialport");
				serialPort.close();
				serialPortGeoeffnet = false;
			} else {
				status.setText("Serialport bereits geschlossen");
			}
		}
		
		void sendeSerialPort(int job)
		{
			
			if (serialPortGeoeffnet != true)
				return;
			try {				
				outputStream.write(sendbuffer);
				dbnew = true;
				dbpointer = 0;
				//plot.setConnected(true);
				lblLed.setForeground(Color.RED);
			} catch (IOException e) {
				status.setText("Fehler beim Senden");
			}
		}
		void serialPortDatenVerfuegbar() {
			try {
				int num;
				while(inputStream.available() > 0) {
					num = inputStream.read(datenbuffer,dbpointer, datenbuffer.length-dbpointer);				
					dbpointer = dbpointer +num;
					if (dbpointer==max_buffer){
						 status.setText(" read "+dbpointer);
						 lblLed.setForeground(Color.GREEN);
						 dbnew = false;
					    update();
					}
					
				}
			} catch (IOException e) {
				status.setText("Fehler beim Lesen");
			}
			busy = false;
		}
		


		class serialPortEventListener implements SerialPortEventListener {
			public void serialEvent(SerialPortEvent event) {
				busy = true;
				status.setText("serialPortEventlistener");
				switch (event.getEventType()) {
				case SerialPortEvent.DATA_AVAILABLE:
					serialPortDatenVerfuegbar();
					break;
				case SerialPortEvent.BI:
				case SerialPortEvent.CD:
				case SerialPortEvent.CTS:
				case SerialPortEvent.DSR:
				case SerialPortEvent.FE:
				case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
				case SerialPortEvent.PE:
				case SerialPortEvent.RI:
				default:
				}
			}
		}		
	}
	
	
	/*
	 * synchronisierten Zugriff auf einen kritischen Abschnitt
	 * Monitorkonept: ein Objekt kontrolliert den ZUgriff auf sich selbst, indem es den zugreifenden Programmen
	 * f�r die Dauer eines Zugriffs einen Monitor erteilt
	 */
	private class CubbyHole {
		private String contents;
		private boolean available = false;
		private void show(){
			//System.out.println(contents+ " "+available);
		}
		public synchronized String get() {
			while (available== false){
				show();
				try {
					wait();
				} catch (InterruptedException e) {}
			}
			available = false;
			notifyAll();
			return contents;
		}
		
		public synchronized void put(String s) {
			while (available == true){
				show();
				try {
					wait();
				} catch (InterruptedException e) {}
			}
			contents = s;
			available = true;
			notifyAll();
		}
		
	}

	public class Timer {

		  private long startTime = 0;
		  private long endTime   = 0;

		  public void start(){
		    this.startTime = System.currentTimeMillis();
		  }

		  public void end() {
		    this.endTime   = System.currentTimeMillis();  
		  }

		  public long getStartTime() {
		    return this.startTime;
		  }

		  public long getEndTime() {
		    return this.endTime;
		  }

		  public long getTotalTime() {
		    return this.endTime - this.startTime;
		  }
		  
		  public long elapsedTime(){
			  return (System.currentTimeMillis() - this.startTime) ;
		  }
		}

	public class Display {
		float[] x = new float[max_buffer/2];
		float[] y = new float[max_buffer/2];	
		
		public void show(){
			for (int i = 0; i < x.length; i++) {
				plot.addPoint(0, x[i], y[i],true);					
			}
		}	
	}
}