package org.usfirst.frc.team5678.robot;

import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.ADXRS450_Gyro;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.DigitalInput;

//import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;


/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class Robot extends IterativeRobot {
	RobotDrive myRobot;
	Joystick stick;
	Encoder encL, encR;
	int autoLoopCounter;
	int autoState;
	final String defaultAuto = "Default";
	final String customAuto = "My Auto";
	final String boilerToTheLeft = "BoilerLeft";
	final String boilerToTheRight = "BoilerRight";
	final String robotStartingOnLeft = "RobotLeft";
	final String robotStartingOnRight = "RobotRight";
	final String robotStartingOnCenter = "RobotCenter";
	String boilerPosition;
	String robotPosition;
	String autoSelected;

	double distance;
	double angle;
	
	SendableChooser<String> chooser = new SendableChooser<>();
	SendableChooser<String> boilerPositionChooser = new SendableChooser<>();
	SendableChooser<String> robotPositionChooser = new SendableChooser<>();

    ADXRS450_Gyro gyro;
	
    /**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
    public void robotInit() {
		chooser.addDefault("Default Auto", defaultAuto);
		chooser.addObject("My Auto", customAuto);
		SmartDashboard.putData("Auto choices", chooser);
		
		boilerPositionChooser.addDefault("Boiler to the Left", boilerToTheLeft);
		boilerPositionChooser.addObject("Boiler to the right", boilerToTheRight);
		SmartDashboard.putData("Boiler Position", boilerPositionChooser);
		
		robotPositionChooser.addDefault("Robot on the Left", robotStartingOnLeft);
		robotPositionChooser.addObject("Robot at Center", robotStartingOnCenter);
		robotPositionChooser.addObject("Robot on the Right", robotStartingOnRight);
		SmartDashboard.putData("Robot Position", robotPositionChooser);
		
    	myRobot = new RobotDrive(0,1);
    	stick = new Joystick(0);
    	encL = new Encoder(0, 1, false, Encoder.EncodingType.k2X);
    	encR = new Encoder(2, 3, false, Encoder.EncodingType.k2X);
        gyro = new ADXRS450_Gyro(); 
        gyro.calibrate();
        CameraServer.getInstance().startAutomaticCapture("cam0",0);
    }
    
    /**
     * This function is run once each time the robot enters autonomous mode
     */
    public void autonomousInit() {
    	System.out.println("Autonomous Init: ==============================");
    	gyro.reset();
    	autoLoopCounter = 0;
    	autoState = 0;
    	distance = 0.0;
    	angle = 0.0;

    	encL.setMaxPeriod(.1);
		encL.setMinRate(10);
		encL.setDistancePerPulse(5);
		encL.setReverseDirection(true);
		encL.setSamplesToAverage (7);
    	encL.reset();

		encR.setMaxPeriod(.1);
		encR.setMinRate(10);
		encR.setDistancePerPulse(5);
		encR.setReverseDirection(true);
		encR.setSamplesToAverage (7);
    	encR.reset();

		autoSelected = chooser.getSelected();
		robotPosition = robotPositionChooser.getSelected();
		boilerPosition = boilerPositionChooser.getSelected();
		// autoSelected = SmartDashboard.getString("Auto Selector",
		// defaultAuto);
		System.out.println("Auto selected: " + autoSelected);
		robotPositionChooser.addDefault("Robot on the Left", robotStartingOnLeft);
    	
    	
    }

    /**
     * This function is called periodically during autonomous
     */
    public void autonomousPeriodic() {
    	switch (robotPosition) {
			case robotStartingOnLeft:
				System.out.printf("  Robot Starting on Left ");
				break;
			case robotStartingOnCenter:
				System.out.printf("  Robot Starting at Center ");
				break;
			case robotStartingOnRight:
				System.out.printf("  Robot Starting on Right ");
				break;
    	}
    	switch (autoState) {      // where is autoState set?
	    	case 0:
	    		// drive forward till mark
	    		distance = encL.getDistance();
		    	if(distance + autoLoopCounter < 200) //Check if we've completed 100 loops (approximately 2 seconds)
				{
					myRobot.drive(-0.5, 0.0); 	// drive forwards half speed
					System.out.printf("  Driving forward. Distance: %2f%n", distance);
					autoLoopCounter++;
				} else {
					myRobot.drive(0.0, 0.0); 	// stop robot
					autoState = 1;
				}
		    	break;
	    	case 1:
	    		angle = gyro.getAngle();
		    	if(angle < 30) //If angle is less than 30 degrees, turn right
				{
					System.out.println("  Turning Right ");
		    		myRobot.tankDrive(0.5, -0.5);
					angle = gyro.getAngle();
					System.out.printf("Angle:  %.2f%n", angle);
				} else {
					myRobot.tankDrive(0.0, 0.0); 	// stop robot
					autoState = 2;
				}
		    	break;
	    	case 2:
	    		// drive forward till mark
	    		distance = encL.getDistance();
		    	if(distance + autoLoopCounter < 200) //Check if we've completed 100 loops (approximately 2 seconds)
				{
					myRobot.drive(-0.5, 0.0); 	// drive forwards half speed
					System.out.println("  Driving forward ");
					autoLoopCounter++;
				} else {
					myRobot.drive(0.0, 0.0); 	// stop robot
					autoState = 3;
				}
		    	break;
		    default: 
				myRobot.drive(0.0, 0.0); 	// stop robot
		    	break;	
	    }
    }
    
    /**
     * This function is called once each time the robot enters tele-operated mode
     */
    public void teleopInit(){
    }

    /**
     * This function is called periodically during operator control
     */
    public void teleopPeriodic() {
        myRobot.arcadeDrive(stick, true);    //true applies a square to the input values
    }
    
    /**
     * This function is called periodically during test mode
     */
    public void testPeriodic() {
//	    	LiveWindow.run();
    }
    
}
