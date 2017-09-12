# jfsOtterVIS

Java Software for Esben Rossels OtterVIS LGL Spectrometer https://hackaday.io/project/10738-ottervis-lgl-spectrophotometer
The aim of the software is to help to connect the UART Version of the spectrometer to different platforms.
I develop the software with Version: Kepler Service Release 2. and bundled it with the fatjar

## !! The Software is under Construction - be patient !!

## Getting started
* Build a ottervis-lgl-spectrophotometer https://hackaday.io/project/10738-ottervis-lgl-spectrophotometer
* Install the UART Version https://tcd1304.wordpress.com/downloads/
* Download a java runtime environment
* Install the RXTX Software http://fizzed.com/oss/rxtx-for-java
* Download the project-zip file and extract it.
* run: java -jar jfsOtterVIS_fat.jar 
* try first the [Help me] Button

## Linux
* The software is successfully tested on a ubuntu 16.04 32bit Version
* The RXTX Software encounters problems with a 64 bit linux Version ..Problematic frame: C [librxtxSerial.so+0x6d9d] read_byte_array+0x3d..
* * The problem is now solved on a ubuntu 16.04 64bit Version 
* * * sudo apt-get install default-jre
* * * sudo apt-get install librxtx-java
* * * java -jar jfsOtterVIS_fat.jar now works.
* * * Thanks to https://eclipsesource.com/de/blogs/2012/10/17/serial-communication-in-java-with-raspberry-pi-and-rxtx/
* The program assumes the device on /dev/ttyACM0 (please contact my blog  http://science.jefro.de if you need another address)
* Instead of running as root try chmod 777 /dev/ttyACM0

## Dependencies
* RXTX http://fizzed.com/oss/rxtx-for-java
* Ptolemy PLot https://ptolemy.eecs.berkeley.edu
* log4j https://logging.apache.org/log4j
* miglayout http://www.miglayout.com
* HelpGUI http://helpgui.sourceforge.net/
* fatjar http://kurucz-grafika.de/fatjar
* 3D plotting https://github.com/yannrichet/jmathplot

## Licence
All my software in source or binary form are under the FreeBSD-license.
