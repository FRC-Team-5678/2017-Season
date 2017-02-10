package org.usfirst.frc.team5678.robot;

import edu.wpi.first.wpilibj.I2C;
import edu.wpi.first.wpilibj.I2C.Port;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.interfaces.Gyro;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.internal.HardwareTimer;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.ADXRS450_Gyro;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.hal.I2CJNI;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */



public class Robot extends IterativeRobot {
	RobotDrive myRobotDrive = new RobotDrive(0, 1);   //left motor channel, right motor channel
	
	Timer timer = new Timer();
	final String optiondefaultAuto = "Default";
	final String optionDriveStraight = "Drive Straight";
	final String optionTurn = "Turn";
	final String optionApproachTarget = "Approach Target";
	final String optionAutoLeftSide = "autonomous  mode starting from left side";
	final String optionAutoRightSide = "autonomous mode starting from right side";
	
	String tst;
	String autoSelected;
	SendableChooser<String> chooser = new SendableChooser<>();
	Encoder rightCimCoder;
	Encoder leftCimCoder;
	PIDController pidControllerLeft;
	PIDController pidControllerRight;
	Talon leftMotor;
	Talon rightMotor;
	String autoSelectedFromDD;
	static int loopCounter;
	Gyro gyro;
	double Kp;
	static final String FILEPATH = "/home/lvuser/pixyData.txt";
	static File f;
	static BufferedWriter bw;
	static FileWriter fw;
	static PrintWriter printWriter;
	static final String pixyTestFormat = "";
	static String newLine;
	public static I2C pixy;
	public static I2CJNI pixyJNI;  //Since there is only one, do we still need to open and close it on each iteration?
	final static byte pixyAddress = 0x54;
	static I2C.Port pixyPort = I2C.Port.kOnboard;
	static byte pixyPortByte;
	
	
/*	Robot()
	{
        //gyro = new ADXRS450_Gyro();   //use Cs0
        myRobotDrive = new RobotDrive(1,2);  // need to know motor controller type ????
        myRobotDrive.setExpiration(0.2);
	}*/

	Trajectory driveStraight = new Trajectory("DriveStraight");
	Trajectory turnTowardsGearPeg =  new Trajectory("turnTowardsGearPeg");
	Trajectory approachGearPeg = new Trajectory("approachGearPeg");
	

	

	
	
	/**
	 * This function is run when the robot is first started up and should be
	 * used for any initialization code.
	 */
	
	
	static void openTestFile(){
		try {
			f = new File(FILEPATH);
			if(!f.exists()){                
				f.createNewFile();
				//SmartDashboard.putString("file status", "opened");
				System.out.println("file opened");
			}	
			if (printWriter == null){
				printWriter = new PrintWriter (f);
				
			}
			SmartDashboard.putString("file status", "open");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void robotInit() {
		//chooser.addDefault("Default Auto", defaultAuto);
		//chooser.addObject("Drive Straight", optionDriveStraight);
		//chooser.addObject("Turn", optionDriveStraight);
		//chooser.addObject("Approach Gear Peg", optionApproachTarget);
		
		//SmartDashboard.putData("Auto choices", chooser);
		
		
		//SmartDashboard.putString("DB/String 5", " <==== enter mode " );
		//SmartDashboard.putString("DB/String 1", tst);
		//System.out.println(tst);
		SmartDashboard.putString("status", "robotInit");
		myRobotDrive.setExpiration(0.2);
		//gyro.reset();
		Kp = 1;
		
		pixy = new I2C(Port.kOnboard, 0x54);     
		I2CJNI pixyJNI = new I2CJNI();
		//pixyJNI.i2CInitialize(Port.kOnboard);
		byte pixyPortByte = (byte)I2C.Port.kOnboard.value;
		I2CJNI.i2CInitialize(pixyPortByte);
		CameraServer.getInstance().startAutomaticCapture("cam0",0);
		
	}
		

		
		
		
		
	

		//pidControllerRight = new PIDController(.1, .1, .1, .1, rightCimCoder, rightMotor);
		//pidControllerLeft = new PIDController(.1, .1, .1, .1, leftCimCoder, leftMotor);
		
		
		/*driveStraight.segments[0].timeLimit = 1;
		driveStraight.segments[1].timeLimit = 2;
		driveStraight.segments[2].timeLimit = 3;
		driveStraight.segments[0].feedForward = .5;
		driveStraight.segments[1].feedForward = 1;
		driveStraight.segments[2].feedForward = .5;
		driveStraight.segments[0].PIDControllerEnabled = false;
		driveStraight.segments[0].Kd = 0;
		driveStraight.segments[0].Kp = .5;
		driveStraight.segments[0].Ki = .1;
		driveStraight.segments[0].Kf = .5;
		driveStraight.segments[1].Kd = 0;
		driveStraight.segments[1].Kp = .5;
		driveStraight.segments[1].Ki = .1;
		driveStraight.segments[1].Kf = .5;
		driveStraight.segments[2].Kd = 0;
		driveStraight.segments[2].Kp = .5;
		driveStraight.segments[2].Ki = .1;
		driveStraight.segments[2].Kf = .5;*/
		

		
		//LiveWindow.addActuator("Drive train", "right motor", rightMotor);
		//LiveWindow.addActuator("Drive train", "left motor", leftMotor);

	
 	
	/**
	 * This autonomous (along with the chooser code above) shows how to select
	 * between different autonomous modes using the dashboard. The sendable
	 * chooser code works with the Java SmartDashboard. If you prefer the
	 * LabVIEW Dashboard, remove all of the chooser code and uncomment the
	 * getString line to get the auto name from the text box below the Gyro
	 *
	 * You can add additional auto modes by adding additional comparisons to the
	 * switch structure below with additional strings. If using the
	 * SendableChooser make sure to add them to the chooser code above as well.
	 */
	@Override
	public void autonomousInit() {
		SmartDashboard.putString("status", "autonomousInit");
		timer.reset();
		timer.start();
		loopCounter = 0;	
		openTestFile();
		
}

	
	
	void DriveStraightFeedback(){  
    int rightEncoderValue = rightCimCoder.get();
   // int leftEncoderValue = leftCimCoder.get();
		if ((rightEncoderValue < driveStraight.segments[0].rightEncoderLimit) && (timer.get() < driveStraight.segments[0].timeLimit)) {
			pidControllerRight.setPID(driveStraight.segments[0].Kp, driveStraight.segments[0].Ki, driveStraight.segments[0].Kd);
			pidControllerLeft.setPID(driveStraight.segments[0].Kp, driveStraight.segments[0].Ki, driveStraight.segments[0].Kd);
		} else if (timer.get() < driveStraight.segments[1].timeLimit){
			pidControllerRight.setPID(driveStraight.segments[1].Kp, driveStraight.segments[1].Ki, driveStraight.segments[1].Kd);
			pidControllerLeft.setPID(driveStraight.segments[1].Kp, driveStraight.segments[1].Ki, driveStraight.segments[1].Kd);
		} else { 
			pidControllerRight.setPID(driveStraight.segments[2].Kp, driveStraight.segments[2].Ki, driveStraight.segments[2].Kd);
			pidControllerLeft.setPID(driveStraight.segments[2].Kp, driveStraight.segments[2].Ki, driveStraight.segments[2].Kd);
			
			myRobotDrive.drive(0.0, 0.0); // stop robot
		}
	}
	
	
	
	
	void DriveStraight(){  
		if (timer.get() < driveStraight.segments[0].timeLimit) {
			SmartDashboard.putString("drive power", "0.5 ");
			SmartDashboard.putString("curve", "0 ");
			myRobotDrive.drive(-0.5, 0.0); // drive forwards half speed
		} else if (timer.get() < driveStraight.segments[1].timeLimit){
			SmartDashboard.putString("drive power", "0.2 ");
			SmartDashboard.putString("curve", "0 ");
			myRobotDrive.drive(-0.2, 0.0); // slow down for .25 second
		} else { 
			SmartDashboard.putString("drive power", "0 ");
			SmartDashboard.putString("curve", "0 "); 
			myRobotDrive.drive(0.0, 0.0); // stop robot
		}
	}
	
	void DriveStraightGyroFeedback()
	{
		double angle = gyro.getAngle(); // get current heading
		SmartDashboard.putNumber("gyroAngle", angle);
        myRobotDrive.drive(0.2, -angle*Kp); // drive towards heading 0
	}
	
	void DriveTest()
	{
		double angle = gyro.getAngle(); // get current heading
		double rightEncoderValue = rightCimCoder.get();
		SmartDashboard.putNumber("rightEncoder", rightEncoderValue);
		double leftEncoderValue = leftCimCoder.get();
		SmartDashboard.putNumber("leftEncoder", leftEncoderValue);
		SmartDashboard.putNumber("gyroAngle", angle);
		myRobotDrive.drive(0.2, 0.0); // slow down for .25 second
		myRobotDrive.drive(0.2, -angle*Kp); // drive towards heading 0
	}
	
	void Turn()
	{
		if (timer.get() < .5) {
			myRobotDrive.drive(-0.4, 0.5); // drive forwards half speed
			SmartDashboard.putString("drive power", ".4 ");
			SmartDashboard.putString("curve", ".5 ");
		} else {
			SmartDashboard.putString("drive power", "0 ");
			SmartDashboard.putString("curve", "0 ");
			myRobotDrive.drive(0.0, 0.0); // stop robot
		}
	}
	
	void ApproachTarget() throws IOException
	{
		myRobotDrive.drive(0.0, 0.0); 
		testPixyi2c();
		
		if (timer.get() < .5) {
			myRobotDrive.drive(-0.4, 0.0); // drive forwards half speed
		} else if (timer.get() < 1.0){
			myRobotDrive.drive(-0.2, 0.0); // slow down for .25 second
		} else {
			myRobotDrive.drive(0.0, 0.0); // stop robot
		}
	}

	void segment123(){
		myRobotDrive.drive(0.0, 0.0); 
	}
	
	public void disabledInit(){
	
		/*bw.flush();
		bw.close();
		fw.close();*/
		SmartDashboard.putString("status", "disabledInit");
		if ( printWriter != null ) 
		{
			printWriter.flush();
		    printWriter.close();
		    SmartDashboard.putString("file status", "closed file in disabledInit");
		    System.out.println("file closed in disabled init");
		    //f.close();
		}
		
	}

	/**
	 * This function is called periodically during autonomous
	 */
	//@SuppressWarnings("deprecation")
	@Override
	public void autonomousPeriodic() {
		//autoSelected = chooser.getSelected();
		autoSelectedFromDD = SmartDashboard.getString("DB/String 0", "Default");
		SmartDashboard.putString("status", "autonomousPeriodic");
		loopCounter++;
		openTestFile();
		
		
		//SmartDashboard.putString("auto Selected from DD", autoSelectedFromDD);
		//System.out.println("Auto selected: " + autoSelected);
		//SmartDashboard.putString("mode", autoSelected);
		//SmartDashboard.putDouble("time", timer.get()); 
		//SmartDashboard.putNumber("timeNumber", timer.get());

		switch (autoSelectedFromDD) {
		case "DriveStraight":
			SmartDashboard.putString("DB/String 1", "valid mode entered " );
			DriveStraight();
			break;
		case "DriveTest":
			SmartDashboard.putString("DB/String 1", "valid mode entered " );
			DriveTest();
			break;
		case "Turn":
			SmartDashboard.putString("DB/String 1", "valid mode entered " );
			Turn();
			break;
		case "ApproachTarget":
			SmartDashboard.putString("DB/String 1", "valid mode entered " );
			try {
				ApproachTarget();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case "Default":
			//DriveTest();
			try {
				ApproachTarget();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		default:
			// Put default auto code here
			try {
				ApproachTarget();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("invalid auto mode");
			SmartDashboard.putString("DB/String 1", "*** invalid mode entered !!!" );
			myRobotDrive.drive(0.0, 0.0);
		}
		if (printWriter != null){
			printWriter.flush();
		}
		
	}

	static double time;
	public void findTime()
	{
		//time = getFPGATimestamp();
		time = timer.get();
	}
	

	
	
	/**
	 * This function is called periodically during operator control
	 */
	@Override
	public void teleopPeriodic() {
		myRobotDrive.drive(0.0, 0.0);
	}

	/**
	 * This function is called periodically during test mode
	 */
	@Override
	public void testPeriodic() {
	
		myRobotDrive.drive(0.0, 0.0);

		LiveWindow.run();
	}
	
	
			
	
	
	
	
	
	
	//@SuppressWarnings("deprecation")
	public static void testPixyi2c() throws IOException{
		
		// set the number of bytes to get from the pixycam each read cycle.  The pixycam outputs 14 byte blocks
		// of data with an extra 2 bytes between frames per Object Block Format Figure
		int maxBytes=64;
		int bytesToRead=64;
		int targetIndex = 0;
		int calculatedChecksum = 0;
		final byte PIXY_START_WORD_LSB=(byte) 0x55;
		final byte PIXY_START_WORD_MSB=(byte) 0xaa;
		final byte PIXY_START_WORDX_LSB=(byte) 0xaa;
		final byte PIXY_START_WORDX_MSB=0x55;
		final String HEX_FORMAT="%02X";
		String convertByte = "";

		// declare the object data variables
		pixyObjectBlock[] pixyObjects = new pixyObjectBlock[3];
		
		for (int i=0; i < 3; i++){
			pixyObjects[i] = new pixyObjectBlock();
		}
		
		// declare a byte array to store the data from the camera
		byte[] pixyData = new byte[bytesToRead];
		//ByteBuffer pixyDataBuffer;
		

		boolean dataAllZeros = false;
		//
		//i2CRead(byte port, byte address, ByteBuffer dataRecieved, byte receiveSize) 
		pixy.readOnly(pixyData, bytesToRead);
		//pixyJNI.i2CRead(pixyPortByte, pixyAddress, pixyData, (byte)bytesToRead);
		 //is there buffering at the pixy (or roboRIO), or will we recieve fresh (recent completed) frame data?
		//assumptions:  neither pixy nor roboRIO buffer I2C data.  When the readOnly method is executing, the roboRIO
		// requests data from the pixy and waits until 64 bytes are delivered.  The pixy responds with current data, and if
		// there are insufficient targets to fill out 64 bytes, it sends zeros. 
		// The pixy processes data at 50 frames per second, and the roboRIO iterative methods are called at that same
		// frequency so the roboRIO should be able to recieve fresh data on each iteration. 		
		// check for a null array and dont try to parse bad data 
		if (pixyData != null) {
			int i = 0;
			SmartDashboard.putNumber("loop Counter", loopCounter);
			//bw.write("loop Counter = " + Integer.toString(loopCounter));
			if (printWriter == null) {SmartDashboard.putString("file status", "printwriter null in testPixyi2c");}
			printWriter.print("\r\n" + Integer.toString(loopCounter) + ", ");
			for (int idx=0; idx < bytesToRead; idx++) {  //for testing - write to SmartDashboard and to file on roboRIO
				convertByte = String.format(HEX_FORMAT, pixyData[i]);
				printWriter.print(convertByte + ", ");
				if (idx < 15) {SmartDashboard.putString("pixyData[" + idx + "]", convertByte );}

			}
			//SmartDashboard.putNumber("i start frame", i);
			printWriter.flush();

		// i is incremented until the first two bytes (i and i+1) match the sync bytes (0x55 and 0xaa)
		// Note:  In Java, the byte primitive is signed, and prior to bitwise operations, the JVM
	    // will expand to an int filled with leading 1s, so the & 0xff is required to 
        // treat the number as unsigned. 
			while ((((pixyData[i] & 0xff) != PIXY_START_WORD_LSB) || ((pixyData[i + 1] & 0xff) != PIXY_START_WORD_MSB)) && (i < bytesToRead-13)) { i++; }
			i = i+2;
			//i++;
		 
		// parse away the second set of sync bytes
		//SmartDashboard.putNumber("loopCounter", loopCounter);
		SmartDashboard.putNumber("i start block", i);
        while ((targetIndex < 2) && (!dataAllZeros)){
             	 
             //while (!((pixyData[i] & 0xff) == PIXY_START_WORD_LSB) && ((pixyData[i + 1] & 0xff) == PIXY_START_WORD_MSB) && i < 50) { i++; }
             //while (!(((pixyData[i] & 0xff) == PIXY_START_WORD_LSB) && ((pixyData[i + 1] & 0xff) == PIXY_START_WORD_MSB) && (i < 50))) { i++; }
         	while ((((pixyData[i] & 0xff) != PIXY_START_WORD_LSB) || ((pixyData[i + 1] & 0xff) != PIXY_START_WORD_MSB)) && (i < bytesToRead-14)) { i++; }
         	if (((pixyData[i] & 0xff) != PIXY_START_WORD_LSB) || ((pixyData[i+1] & 0xff) != PIXY_START_WORD_MSB))
         	{
         		break; //no object block sync
         	}

             SmartDashboard.putString("i", String.format(HEX_FORMAT, i));
             printWriter.print("i=" + "," + Integer.toString(i) + ", ");
             printWriter.println("targetIndex= " + "," + Integer.toString(targetIndex) + ",");
            
        
             //SmartDashboard.putNumber("i inside target loop", i);
        	 pixyObjects[targetIndex].checksum = (char) (((pixyData[i + 3] & 0xff) << 8) | (pixyData[i + 2] & 0xff));
        	 SmartDashboard.putNumber("checksum", pixyObjects[targetIndex].checksum);
             printWriter.println("checksum=," +  Integer.toString(pixyObjects[targetIndex].checksum) + ",");
             printWriter.flush();
             if (pixyObjects[targetIndex].checksum > 0)
             {
	             SmartDashboard.putNumber("checksum > 0 for targetIndex=", targetIndex);
				 pixyObjects[targetIndex].signatureNumber = (char) (pixyData[i + 4] & 0xff);
				 //pixyObjects[targetIndex].signatureNumber = (char) (((pixyData[i + 5] & 0xff) << 8) | (pixyData[i + 4] & 0xff));
				 pixyObjects[targetIndex].xPosition = (char) (((pixyData[i + 7] & 0xff) << 8)  | (pixyData[i + 6] & 0xff));
		         pixyObjects[targetIndex].yPosition = (char) (((pixyData[i + 9] & 0xff) << 8) | (pixyData[i + 8] & 0xff));
		         pixyObjects[targetIndex].width = (char) (((pixyData[i + 11] & 0xff) << 8) | (pixyData[i + 10] & 0xff));
		         pixyObjects[targetIndex].height = (char) (((pixyData[i + 13] & 0xff) << 8) | (pixyData[i + 12] & 0xff));
		         calculatedChecksum = pixyObjects[targetIndex].signatureNumber 
		        		 + pixyObjects[targetIndex].xPosition
		        		 + pixyObjects[targetIndex].yPosition
		        		 + pixyObjects[targetIndex].width
		        		 + pixyObjects[targetIndex].height;
		         SmartDashboard.putNumber("calculated checksum", calculatedChecksum);
	        	 //bw.write("calculated checksum=" +  calculatedChecksum);
	        	 printWriter.println("calculated checksum=," +  Integer.toString(calculatedChecksum) + ",");
		         if (calculatedChecksum == pixyObjects[targetIndex].checksum)
		         {
		        	 SmartDashboard.putBoolean("checksumVerified", true);
		        	 
		         }
		         else
		         {
		        	//SmartDashboard.putNumber("targetIndex", targetIndex);
		        	 SmartDashboard.putBoolean("checksumVerified", false);        
		        	 
		         }
		         pixyObjects[targetIndex].outputToSmartDashboard();
		         pixyObjects[targetIndex].outputToFile(printWriter);
		         ++targetIndex;  
		         
		 
	             }
             else 
            	 {
            	 	dataAllZeros = true; 
            	 
            	 }
		     }   
		}
	}
		
  }


