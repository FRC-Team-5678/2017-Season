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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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
	int loopCounter;
	Gyro gyro;
	double Kp;
	
	Robot()
	{
        gyro = new ADXRS450_Gyro();   //use Cs0
        myRobotDrive = new RobotDrive(1,2);  // need to know motor controller type ????
        myRobotDrive.setExpiration(0.2);
	}

	Trajectory driveStraight = new Trajectory("DriveStraight");
	Trajectory turnTowardsGearPeg =  new Trajectory("turnTowardsGearPeg");
	Trajectory approachGearPeg = new Trajectory("approachGearPeg");
	

	

	//File f;
	//BufferedWriter bw;
	//FileWriter fw;
	
	/**
	 * This function is run when the robot is first started up and should be
	 * used for any initialization code.
	 */
	@Override
	public void robotInit() {
		//chooser.addDefault("Default Auto", defaultAuto);
		//chooser.addObject("Drive Straight", optionDriveStraight);
		//chooser.addObject("Turn", optionDriveStraight);
		//chooser.addObject("Approach Gear Peg", optionApproachTarget);
		
		//SmartDashboard.putData("Auto choices", chooser);
		
		
		SmartDashboard.getString("DB String 5", " <==== enter mode " );
		//SmartDashboard.putString("DB String 1", tst);
		System.out.println(tst);
		myRobotDrive.setExpiration(0.2);
		gyro.reset();
		Kp = 1;

		//pidControllerRight = new PIDController(.1, .1, .1, .1, rightCimCoder, rightMotor);
		//pidControllerLeft = new PIDController(.1, .1, .1, .1, leftCimCoder, leftMotor);
		
		
		driveStraight.segments[0].timeLimit = 1;
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
		driveStraight.segments[2].Kf = .5;
		

		
		//LiveWindow.addActuator("Drive train", "right motor", rightMotor);
		//LiveWindow.addActuator("Drive train", "left motor", leftMotor);

/*    	try {
    		f = new File("~/Output.txt");
    		if(!f.exists()){                
    			f.createNewFile();
    		}
			fw = new FileWriter(f);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	//bw = new BufferedWriter(fw);*/
				
	}

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
		timer.reset();
		timer.start();
		loopCounter = 0;
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
	
	void ApproachTarget()
	{
		loopCounter++;
		testPixyi2c();
		SmartDashboard.putNumber("loop Counter", loopCounter);
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
		
	}

	/**
	 * This function is called periodically during autonomous
	 */
	//@SuppressWarnings("deprecation")
	@Override
	public void autonomousPeriodic() {
		//autoSelected = chooser.getSelected();
		autoSelectedFromDD = SmartDashboard.getString("DB/String 0", "Default");
		
		SmartDashboard.putString("auto Selected from DD", autoSelectedFromDD);
		System.out.println("Auto selected: " + autoSelected);
		//SmartDashboard.putString("mode", autoSelected);
		//SmartDashboard.putDouble("time", timer.get()); 
		SmartDashboard.putNumber("timeNumber", timer.get());

		switch (autoSelectedFromDD) {
		case "DriveStraight":
			SmartDashboard.getString("DB/String 6", "valid mode entered " );
			DriveStraight();
			break;
		case "DriveTest":
			SmartDashboard.getString("DB/String 6", "valid mode entered " );
			DriveTest();
			break;
		case "Turn":
			SmartDashboard.getString("DB/String 6", "valid mode entered " );
			Turn();
			break;
		case "ApproachTarget":
			SmartDashboard.getString("DB/String 6", "valid mode entered " );
			ApproachTarget();
			break;
		case "Default":
			DriveTest();
		default:
			// Put default auto code here
			System.out.println("invalid auto mode");
			SmartDashboard.getString("DB/String 6", "*** invalid mode entered !!!" );
			myRobotDrive.drive(0.0, 0.0);
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
		testPixyi2c();
		myRobotDrive.drive(0.0, 0.0);
	}

	/**
	 * This function is called periodically during test mode
	 */
	@Override
	public void testPeriodic() {
		/*try {
			//bw.close();
			//fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		//testPixyi2c();
		myRobotDrive.drive(0.0, 0.0);
		//SmartDashboard.putString("test", "hello world");
		LiveWindow.run();
	}
	
	public static I2C pixy;
	
			
	
	//@SuppressWarnings("deprecation")
	public static void testPixyi2c(){
		
		// set the number of bytes to get from the pixycam each read cycle.  The pixycam outputs 14 byte blocks
		// of data with an extra 2 bytes between frames per Object Block Format Figure
		int maxBytes=64;
		int targetIndex = 0;
		int calculatedChecksum = 0;
		final byte PIXY_START_WORD_LSB=0x55;
		final byte PIXY_START_WORD_MSB=(byte) 0xaa;
		final byte PIXY_START_WORDX_LSB=(byte) 0xaa;
		final byte PIXY_START_WORDX_MSB=0x55;


		// declare the object data variables
		pixyObjectBlock[] pixyObjects = new pixyObjectBlock[6];
		
		for (int i=0; i < 6; i++){
			pixyObjects[i] = new pixyObjectBlock();
		}
		
		// declare a byte array to store the data from the camera
		byte[] pixyData = new byte[maxBytes];
		pixy = new I2C(Port.kOnboard, 0x54);

		boolean dataAllZeros = false;

		pixy.readOnly(pixyData, 64);   
		
		/* is there buffering at the pixy (or roboRIO), or will we recieve fresh (recent completed) frame data?
		//assumptions:  neither pixy nor roboRIO buffer I2C data.  When the readOnly method is executing, the roboRIO
		// requests data from the pixy and waits until 64 bytes are delivered.  The pixy responds with current data, and if
		// there are insufficient targets to fill out 64 bytes, it sends zeros. 
		// The pixy processes data at 50 frames per second, and the roboRIO iterative methods are called at that same
		// frequency so the roboRIO should be able to recieve fresh data on each iteration. 		
		// check for a null array and dont try to parse bad data */
		if (pixyData != null) {
			int i = 0;
		// parse the data to move the index pointer (i) to the start of a frame
		// i is incremented until the first two bytes (i and i+1) match the sync bytes (0x55 and 0xaa)
		// Note:  In Java, the and operation with 0xff is key to matching the 0xaa because the byte array is
//		           automatically filled by Java with leading 1s that make the number -86
			while (!((pixyData[i] & 0xff) == PIXY_START_WORD_LSB) && ((pixyData[i + 1] & 0xff) == PIXY_START_WORD_MSB) && i < 50) { i++; }
			i++;
		/* check if the index is getting so high that you cant align and see an entire frame.  Ensure it isnt */
			if (i > 50) i = 49;
		// parse away the second set of sync bytes
		//SmartDashboard.putNumber("loopCounter", loopCounter);
		SmartDashboard.putNumber("i before target loop", i);
        while ((targetIndex < 2) && (!dataAllZeros)){
             	 
             while (!((pixyData[i] & 0xff) == PIXY_START_WORD_LSB) && ((pixyData[i + 1] & 0xff) == PIXY_START_WORD_MSB) && i < 50) { i++; }
             SmartDashboard.putNumber("i inside target loop", i);
             SmartDashboard.putNumber("pixyData[i]", pixyData[i]);
        	 SmartDashboard.putNumber("pixyData[i+1]", pixyData[i+1]);
        	 SmartDashboard.putNumber("pixyData[i+2]", pixyData[i+2]);
        	 SmartDashboard.putNumber("pixyData[i+3]", pixyData[i+3]);
        	 SmartDashboard.putNumber("pixyData[i+4]", pixyData[i+4]);
        	 SmartDashboard.putNumber("pixyData[i+5]", pixyData[i+5]);
        	 SmartDashboard.putNumber("pixyData[i+6]", pixyData[i+6]);
        	 SmartDashboard.putNumber("pixyData[i+7]", pixyData[i+7]);
        	 SmartDashboard.putNumber("pixyData[i+8]", pixyData[i+8]);
        	 SmartDashboard.putNumber("pixyData[i+9]", pixyData[i+9]);
        	 SmartDashboard.putNumber("pixyData[i+10]", pixyData[i+10]);
        	 SmartDashboard.putNumber("pixyData[i+11]", pixyData[i+11]);
        	 SmartDashboard.putNumber("pixyData[i+12]", pixyData[i+12]);
        	 SmartDashboard.putNumber("pixyData[i+13]", pixyData[i+13]);
        	 SmartDashboard.putNumber("pixyData[i+14]", pixyData[i+14]);
        
             //SmartDashboard.putNumber("i inside target loop", i);
        	 pixyObjects[targetIndex].checksum = (char) (((pixyData[i + 3] & 0xff) << 8) | (pixyData[i + 2] & 0xff));
        	 SmartDashboard.putNumber("checksum", pixyObjects[targetIndex].checksum);
             if (pixyObjects[targetIndex].checksum > 0)
             {
	             SmartDashboard.putNumber("checksum > 0 for targetIndex=", targetIndex);
				 pixyObjects[targetIndex].signatureNumber = (char) (pixyData[i + 4] & 0xff);
				 pixyObjects[targetIndex].xPosition = (char) (((pixyData[i + 7] & 0xff) << 8) | (pixyData[i + 6] & 0xff));
		         pixyObjects[targetIndex].yPosition = (char) (((pixyData[i + 9] & 0xff) << 8) | (pixyData[i + 8] & 0xff));
		         pixyObjects[targetIndex].width = (char) (((pixyData[i + 11] & 0xff) << 8) | (pixyData[i + 10] & 0xff));
		         pixyObjects[targetIndex].height = (char) (((pixyData[i + 13] & 0xff) << 8) | (pixyData[i + 12] & 0xff));
		         calculatedChecksum = pixyObjects[targetIndex].signatureNumber 
		        		 + pixyObjects[targetIndex].xPosition
		        		 + pixyObjects[targetIndex].yPosition
		        		 + pixyObjects[targetIndex].width
		        		 + pixyObjects[targetIndex].height;
		         SmartDashboard.putNumber("calculated checksum", calculatedChecksum);
		         if (calculatedChecksum == pixyObjects[targetIndex].checksum)
		         {
		        	 SmartDashboard.putBoolean("checksumVerified", true);
		        	 pixyObjects[targetIndex].outputToSmartDashboard();
		         }
		         else
		         {
		        	//SmartDashboard.putNumber("targetIndex", targetIndex);
		        	 SmartDashboard.putBoolean("checksumVerified", false);        
			         
		         }
		         ++targetIndex;  
		         
		 
	             }
             else 
            	 {
            	 	dataAllZeros = true; 
            	 
            	 }
		}
         /*  */
	}
}
}

