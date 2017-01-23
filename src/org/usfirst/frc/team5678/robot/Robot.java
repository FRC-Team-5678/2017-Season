package org.usfirst.frc.team5678.robot;

import edu.wpi.first.wpilibj.I2C;
import edu.wpi.first.wpilibj.I2C.Port;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.internal.HardwareTimer;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.Talon;

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
	RobotDrive myRobot = new RobotDrive(0, 1);   //left motor channel, right motor channel
	
	Timer timer = new Timer();
	final String defaultAuto = "Default";
	final String segment1DriveStraight = "Segment1 - Drive Straight";
	final String segment2TurnTowardsGearPeg = "Segment2 -Turn Towards Gear Peg";
	final String segment3ApproachGearPeg = "Segment3 -Approach Gear Peg";
	final String allSegmentsFromLeftSide = "all segments starting from left side";
	final String allSegmentsFromRightSide = "all segments starting from right side";
	String autoSelected;
	SendableChooser<String> chooser = new SendableChooser<>();
	Encoder rightCimCoder;
	Encoder leftCimCoder;
	PIDController pidControllerLeft;
	PIDController pidControllerRight;
	Talon leftMotor;
	Talon rightMotor;

	
	public class PathToPoint{
		double timeLimit;
		double encoderLimit;
		boolean PIDControllerEnabled;
		double feedForward;
		double Kp;
		double Ki;
		double Kd;
		double Kf;
	}
	
	PathToPoint[] driveStraight = new PathToPoint[3];
	PathToPoint[] turnTowardsGearPeg = new PathToPoint[3];
	PathToPoint[] approachGearPeg = new PathToPoint[3];
	
// Each trajectory is comprised of path segments
	

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
		chooser.addObject("Segment1 - Drive Straight", segment1DriveStraight);
		chooser.addObject("Segment2 - Turn Towards Gear Peg", segment2TurnTowardsGearPeg);
		chooser.addObject("Segment3 - Approach Gear Peg", segment3ApproachGearPeg);
		
		SmartDashboard.putData("Auto choices", chooser);
		myRobot.setExpiration(0.2);
		
		//pidControllerRight = new PIDController(.1, .1, .1, .1, rightCimCoder, rightMotor);
		//pidControllerLeft = new PIDController(.1, .1, .1, .1, leftCimCoder, leftMotor);
		
		
		driveStraight[0].timeLimit = 1;
		driveStraight[1].timeLimit = 2;
		driveStraight[2].timeLimit = 3;
		driveStraight[0].feedForward = .5;
		driveStraight[1].feedForward = 1;
		driveStraight[2].feedForward = .5;
		driveStraight[0].PIDControllerEnabled = false;
		driveStraight[0].Kd = 0;
		driveStraight[0].Kp = .5;
		driveStraight[0].Ki = .1;
		driveStraight[0].Kf = .5;
		driveStraight[1].Kd = 0;
		driveStraight[1].Kp = .5;
		driveStraight[1].Ki = .1;
		driveStraight[1].Kf = .5;
		driveStraight[2].Kd = 0;
		driveStraight[2].Kp = .5;
		driveStraight[2].Ki = .1;
		driveStraight[2].Kf = .5;
		

		
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
	}
	
	
	void segment1DriveStraight(){  
		if (timer.get() < driveStraight[0].timeLimit) {
			SmartDashboard.putString("drive power", "0.5 ");
			SmartDashboard.putString("curve", "0 ");
			myRobot.drive(-0.5, 0.0); // drive forwards half speed
		} else if (timer.get() < driveStraight[1].timeLimit){
			SmartDashboard.putString("drive power", "0.2 ");
			SmartDashboard.putString("curve", "0 ");
			myRobot.drive(-0.2, 0.0); // slow down for .25 second
		} else { 
			SmartDashboard.putString("drive power", "0 ");
			SmartDashboard.putString("curve", "0 "); 
			myRobot.drive(0.0, 0.0); // stop robot
		}
	}
	
	void segment2TurnTowardsGearPeg()
	{
		if (timer.get() < .5) {
			myRobot.drive(-0.4, 0.5); // drive forwards half speed
			SmartDashboard.putString("drive power", ".4 ");
			SmartDashboard.putString("curve", ".5 ");
		} else {
			SmartDashboard.putString("drive power", "0 ");
			SmartDashboard.putString("curve", "0 ");
			myRobot.drive(0.0, 0.0); // stop robot
		}
	}
	
	void segment3ApproachGearPeg()
	{
		testPixyi2c();
		if (timer.get() < .5) {
			myRobot.drive(-0.4, 0.0); // drive forwards half speed
		} else if (timer.get() < 1.0){
			myRobot.drive(-0.2, 0.0); // slow down for .25 second
		} else {
			myRobot.drive(0.0, 0.0); // stop robot
		}
	}
	
	void segment123(){
		myRobot.drive(0.0, 0.0); 
	}
	
	public void disabledInit(){
		
	}

	/**
	 * This function is called periodically during autonomous
	 */
	@SuppressWarnings("deprecation")
	@Override
	public void autonomousPeriodic() {
		autoSelected = chooser.getSelected();
		System.out.println("Auto selected: " + autoSelected);
		SmartDashboard.putString("mode", autoSelected);
		SmartDashboard.putDouble("time", timer.get()); 


		switch (autoSelected) {
		case segment1DriveStraight:
			segment1DriveStraight();
			break;
		case segment2TurnTowardsGearPeg:
			segment2TurnTowardsGearPeg();
			break;
		case segment3ApproachGearPeg:
			segment3ApproachGearPeg();
			break;
		case defaultAuto:
		default:
			// Put default auto code here
			myRobot.drive(0.0, 0.0); 
			System.out.println("no auto mode selected!");
			break;
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
		myRobot.drive(0.0, 0.0);
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
		myRobot.drive(0.0, 0.0);
		//SmartDashboard.putString("test", "hello world");
		LiveWindow.run();
	}
	
	public static I2C pixy;
	
	public class pixyObjectBlock{  
		int xPosition;
		int yPosition;
		int width;
		int height;	
		int signatureNumber;
		int checksum;
		
		pixyObjectBlock(){
			xPosition = 0;
			yPosition = 0;
			width = 0;
			height = 0;	
			signatureNumber = -1;
			checksum = 0;	
		}
		
		void outputToSmartDashboard()
		{
			
		}
		
		
	}
	
	@SuppressWarnings("deprecation")
	public static void testPixyi2c(){
		
		// set the number of bytes to get from the pixycam each read cycle.  The pixycam outputs 14 byte blocks
		// of data with an extra 2 bytes between frames per Object Block Format Figure
		int maxBytes=64;


		// declare the object data variables
		pixyObjectBlock[] pixyObjects = new pixyObjectBlock[6];
		int targetIndex = 0;
		String hex;
	
		


		// declare a byte array to store the data from the camera
		byte[] pixyData = new byte[maxBytes];
		pixy = new I2C(Port.kOnboard, 0x54);

		boolean dataAllZeros = false;

		pixy.readOnly(pixyData, 64);   
/*		SmartDashboard.putNumber("pixyData0", pixyData[0]);
		SmartDashboard.putNumber("pixyData1", pixyData[1]);
		SmartDashboard.putNumber("pixyData2", pixyData[2]);
		SmartDashboard.putNumber("pixyData3", pixyData[3]);*/
		SmartDashboard.putNumber("length", pixyData.length);
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
			while (!((pixyData[i] & 0xff) == 0x55) && ((pixyData[i + 1] & 0xff) == 0xaa) && i < 50) { i++; }
			i++;
		/* check if the index is getting so high that you cant align and see an entire frame.  Ensure it isnt */
			if (i > 50) i = 49;
		// parse away the second set of sync bytes
			
			SmartDashboard.putNumber("i before target loop", i);
         while ((targetIndex < 2) && (!dataAllZeros)){
             	 
             while (!((pixyData[i] & 0xff) == 0x55) && ((pixyData[i + 1] & 0xff) == 0xaa) && i < 50) { i++; }
             SmartDashboard.putNumber("i inside target loop", i);
             SmartDashboard.putNumber("pixyData[i]", pixyData[i]);
        	 SmartDashboard.putNumber("pixyData[i+1]", pixyData[i+1]);
        	 SmartDashboard.putNumber("pixyData[i+2]", pixyData[i+2]);
        	 SmartDashboard.putNumber("pixyData[i+3]", pixyData[i+3]);
             pixyObjects[targetIndex].checksum = (char) (((pixyData[i + 3] & 0xff) << 8) | (pixyData[i + 2] & 0xff));
             if (pixyObjects[targetIndex].checksum > 0)
             {
			 pixyObjects[targetIndex].signatureNumber = (char) (((pixyData[i + 5] & 0xff) << 8) | (pixyData[i + 4] & 0xff));
			 pixyObjects[targetIndex].xPosition = (char) (((pixyData[i + 7] & 0xff) << 8) | (pixyData[i + 6] & 0xff));
	         pixyObjects[targetIndex].yPosition = (char) (((pixyData[i + 9] & 0xff) << 8) | (pixyData[i + 8] & 0xff));
	         pixyObjects[targetIndex].width = (char) (((pixyData[i + 11] & 0xff) << 8) | (pixyData[i + 10] & 0xff));
	         pixyObjects[targetIndex].height = (char) (((pixyData[i + 13] & 0xff) << 8) | (pixyData[i + 12] & 0xff));
	         
	         SmartDashboard.putNumber("Target", targetIndex);
	         SmartDashboard.putNumber("signature", pixyObjects[targetIndex].signatureNumber);
	         SmartDashboard.putNumber("xPosition", pixyObjects[targetIndex].xPosition);
	         SmartDashboard.putNumber("yPosition", pixyObjects[targetIndex].yPosition);
	         SmartDashboard.putNumber("width", pixyObjects[targetIndex].width);
	         SmartDashboard.putNumber("height", pixyObjects[targetIndex].height);
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

