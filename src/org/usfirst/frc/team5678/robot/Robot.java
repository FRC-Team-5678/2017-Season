package org.usfirst.frc.team5678.robot;

import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Joystick.ButtonType;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.RobotDrive.MotorType;												  
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.buttons.JoystickButton;
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
	Joystick leftStick; //, rightStick;
	Spark leftMotor, rightMotor;						 
	Encoder encL, encR;
	DigitalInput gearLimitSwitch;
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
	CameraServer cameraServer;					   
	
	double gyroAngleGoalStartingLeft = 60;
	double gyroAngleGoalStartingRight = -60;
	double turnPower = .4;

	double distance, distanceL, distanceR;
	double angle;
	double turningValue;
	double previousMotorCommand;
    double angleSetpoint = 0.0;
    final double pGain = .006; //propotional turning constant	
    double maxMotorCommandChange = 0.05;
	

	
	
	boolean squareJoystickYAxis = false;
	boolean useTankDrive = false;

	double throttleScalingConstant = 1;
	double turnSensitivityConstant = 1;
	double distanceBeforeApproach = 0;
	
	boolean  gearEngaged = false;
	boolean forwardDirection = false;
	double gradualStopPower;
	boolean switchDirectionInProgress = false;
	double switchDirectionLoopCounter;
	final int waitAftergearEngaged = 120;
	double gearEngagedLoopCounter;
	JoystickButton reverseDirectionButton;

	
	SendableChooser<String> chooser = new SendableChooser<>();
	SendableChooser<String> boilerPositionChooser = new SendableChooser<>();
	SendableChooser<String> robotPositionChooser = new SendableChooser<>();

    ADXRS450_Gyro gyro;
	
    /**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
    public void robotInit() {
		//chooser.addDefault("Default Auto", defaultAuto);
		//chooser.addObject("My Auto", customAuto);
		//SmartDashboard.putData("Auto choices", chooser);
		
		boilerPositionChooser.addDefault("Boiler to the Left", boilerToTheLeft);
		boilerPositionChooser.addObject("Boiler to the right", boilerToTheRight);
		SmartDashboard.putData("Boiler Position", boilerPositionChooser);
		
		robotPositionChooser.addDefault("Robot on the Left", robotStartingOnLeft);
		robotPositionChooser.addObject("Robot at Center", robotStartingOnCenter);
		robotPositionChooser.addObject("Robot on the Right", robotStartingOnRight);
		SmartDashboard.putData("Robot Position", robotPositionChooser);
		
		leftMotor = new Spark(0);
        rightMotor = new Spark(1);
        leftMotor.enableDeadbandElimination(true);
        rightMotor.enableDeadbandElimination(true);
        squareJoystickYAxis = true;
        gearLimitSwitch = new DigitalInput(4);
		myRobot = new RobotDrive(leftMotor,rightMotor);     //myRobot = new RobotDrive(0,1);
		myRobot.setInvertedMotor(RobotDrive.MotorType.kRearLeft, true);  //so that positive motor commands will correspond to the forward direction
    	myRobot.setInvertedMotor(RobotDrive.MotorType.kRearRight, true);	//so that positive motor commands will correspond to the forward direction													 
		leftStick = new Joystick(0);
    	//rightStick = new Joystick(0);
    	reverseDirectionButton = new JoystickButton(leftStick,2);
    	encL = new Encoder(0, 1, false, Encoder.EncodingType.k2X);
    	encR = new Encoder(2, 3, false, Encoder.EncodingType.k2X);
        gyro = new ADXRS450_Gyro(); 
        gyro.calibrate();
        //CameraServer.getInstance().startAutomaticCapture("cam0",0);
        //CameraServer.getInstance().startAutomaticCapture("cam1",1);
        
        
        
    }
    
    /**
     * This function is run once each time the robot enters autonomous mode
     */
    public void autonomousInit() {
    	//System.out.println("Autonomous Init: ==============================");
    	gyro.reset();
    	autoLoopCounter = 0;
    	autoState = 0;
    	distance = 0.0;
    	angle = 0.0;
		turningValue = 0.0;
    	gearEngagedLoopCounter = -1;

    	encL.setMaxPeriod(.1);
		encL.setMinRate(10);
		encL.setDistancePerPulse(.088);
		encL.setReverseDirection(true);
		encL.setSamplesToAverage (7);
    	encL.reset();

		encR.setMaxPeriod(.1);
		encR.setMinRate(10);
		encR.setDistancePerPulse(.088);
		encR.setReverseDirection(true);
		encR.setSamplesToAverage (7);
    	encR.reset();

		autoSelected = chooser.getSelected();
		robotPosition = robotPositionChooser.getSelected();
		//boilerPosition = boilerPositionChooser.getSelected();
		// autoSelected = SmartDashboard.getString("Auto Selector",
		// defaultAuto);
		//System.out.println("Auto selected: " + autoSelected);
		robotPositionChooser.addDefault("Robot on the Left", robotStartingOnLeft);
    	
    	
    }

    /**
     * This function is called periodically during autonomous
     */
    public void autonomousPeriodic() {
    	switch (robotPosition) {
			case robotStartingOnLeft:
				//System.out.printf("  Robot Starting on Left ");
				break;
			case robotStartingOnCenter:
				//System.out.printf("  Robot Starting at Center ");
				break;
			case robotStartingOnRight:
				//System.out.printf("  Robot Starting on Right ");
				break;
    	}
    	switch (autoState) {      // where is autoState set?
	    	case 0:
	    		// drive forward till mark
	    		distance = encL.getDistance(); 
		    	if(distance < 60) //Check if we've completed 100 loops (approximately 2 seconds)
				{
					//turningValue =  0;
		            turningValue =  (angleSetpoint - gyro.getAngle())*pGain;
	                myRobot.drive(-0.5, turningValue);
				    //System.out.printf("  Driving forward. Distance: %2f%n", distance);
					autoLoopCounter++;
				} else {
					myRobot.drive(0.0, 0.0); 	// stop robot
					autoState = 1;
				} 
	    		
				//distanceL = encL.getDistance();
	    		//distanceR = encR.getDistance();
	    		SmartDashboard.putNumber("left encoder distance", distanceL);
	    		//SmartDashboard.putNumber("right encoder distance", distanceR);
				//System.out.printf("  Driving forward. DistanceL: %2f%n", distanceL);
				//System.out.printf("  Driving forward. DistanceR: %2f%n", distanceR);
					autoLoopCounter++;
					//myRobot.tankDrive( (distanceL < 60)? -0.5:0, (distanceL < 60)? -0.5:0 ); 	// stop robot
//					myRobot.tankDrive( -0.5, (distanceR < 60)? -0.5:0 ); 	// stop robot	
				//autoState = 1;
				
		    	break;
	    	case 1:
	    		if (robotPosition == robotStartingOnLeft){
	    			angle = gyro.getAngle();
			    	if(angle < 30) //??assume that CW rotation is positive 
					{
						//System.out.println("  Turning Right ");
			    		myRobot.tankDrive(-turnPower, turnPower);  //and assume that this is the direction for CW rotation
						angle = gyro.getAngle();
						SmartDashboard.putNumber("gyro angle", angle); 
						//System.out.printf("Angle:  %.2f%n", angle);
						SmartDashboard.putNumber("turnPower", turnPower);
						SmartDashboard.putString("state", "auto state 1");
					} else {
						myRobot.tankDrive(0.0, 0.0); 	// stop robot
						distanceBeforeApproach = encL.getDistance();
						autoState = 2;
					}
			    	break;
	    		}
	    		else if (robotPosition == robotStartingOnRight){
	    			if(angle > -30) //this is the case for robot starting on the right, we need to add other cases
					{
						//System.out.println("  Turning Right ");
			    		myRobot.tankDrive(turnPower, -turnPower);
						angle = gyro.getAngle();
						//System.out.printf("Angle:  %.2f%n", angle);
					} else {
						myRobot.tankDrive(0.0, 0.0); 	// stop robot
						distanceBeforeApproach = encL.getDistance();
						autoState = 2;
					}
			    	break;
	    		}
	    		else {
	    			autoState = 2;
	    			distanceBeforeApproach = encL.getDistance();
	    			break;
	    		}
	    		
	    	case 2:
	    		// drive forward till mark
	    		SmartDashboard.putString("state", "auto state 2");
	    		distance = encL.getDistance(); 
		    	if(distance < (36 + distanceBeforeApproach)) //Check if we've completed 100 loops (approximately 2 seconds)
				{
		    		myRobot.tankDrive(-0.4, -0.4);
				}
		    	else{
		    		myRobot.tankDrive(0, 0);
		    		SmartDashboard.putString("state", "post auto state 2 stopped");
		    	}
	    		
	        /*	if ((gearLimitSwitch.get() == true) & (gearEngagedLoopCounter < 0) & (distance > 400))  
	        	{
	        		gearEngaged = true;
	        		SmartDashboard.putBoolean("gear engaged", true);
	        		gearEngagedLoopCounter = 0;
	        		SmartDashboard.putString("gear engaged timer", Double.toString(gearEngagedLoopCounter));
	        		myRobot.arcadeDrive(0,0);
	        	}
	        	else if ((gearEngaged == true) & (gearEngagedLoopCounter < 120))  //wait                                                                                                                       xxxxxxxxxxxxxxxxxxxx
	        	{
	        		gearEngagedLoopCounter++;
	        		SmartDashboard.putString("gear engaged timer", Double.toString(gearEngagedLoopCounter));
	        		myRobot.arcadeDrive(0,0);
	        	}
	        	else if ((gearEngaged == true) & (gearEngagedLoopCounter > 120) & (gearEngagedLoopCounter < 200)){
	        		myRobot.arcadeDrive(-0.5, 0);  //backup
	        		gearEngaged = false;
	        	}*/
	        	
	    }
    }
    
    /**
     * This function is called once each time the robot enters tele-operated mode
     */
    public void teleopInit(){
    	switchDirectionLoopCounter = -1;
    	forwardDirection = true;
    	//CameraServer.getInstance().startAutomaticCapture("cam0",0);
    	//Server.getInstance().startAutomaticCapture("cam1",1);
    	squareJoystickYAxis = true;
    	useTankDrive = false;

    	throttleScalingConstant = 1;
    	turnSensitivityConstant = 1;
    	
    	gearEngaged = false;
    	forwardDirection = true;
    	switchDirectionInProgress = false;

    }

    /**
     * This function is called periodically during operator control
     */
    
    public double calculateEasedMotorCommand(double previousMotorCommand, double currentMotorGoal){
    	double tempMotorCommandChange;
    	double tempMotorCommand;
    	tempMotorCommandChange = currentMotorGoal - previousMotorCommand;

    	tempMotorCommandChange = java.lang.Math.abs(tempMotorCommandChange) < maxMotorCommandChange ? tempMotorCommandChange : (tempMotorCommandChange > 0 ? maxMotorCommandChange : -maxMotorCommandChange);
    	tempMotorCommand = previousMotorCommand + tempMotorCommandChange;
    	return tempMotorCommand;
    }
    
    @SuppressWarnings("deprecation")
	public void teleopPeriodic() {
    	double currentMotorCommand = 0;
    	double moveValue = 0;
    	double rotateValue;
    	double moveRValue = 0;
    	
    	
    		SmartDashboard.putString("trigger", leftStick.getTrigger() ? "true" : "false");
    		SmartDashboard.putString("reverse button", reverseDirectionButton.get() ? "true" : "false");
    		if ((reverseDirectionButton.get() & switchDirectionInProgress == false)){		
    			switchDirectionInProgress = true;
    			
    			switchDirectionLoopCounter = 0;
    			currentMotorCommand = calculateEasedMotorCommand(forwardDirection ? leftStick.getY() : -leftStick.getY(), 0);
    		
    		}
    		else if (switchDirectionInProgress & (switchDirectionLoopCounter < 25)){  //slowing down gradually
    			currentMotorCommand = calculateEasedMotorCommand(previousMotorCommand, forwardDirection ? leftStick.getY() : -leftStick.getY());
    			switchDirectionLoopCounter++;
    		}
    		else if (switchDirectionInProgress & (switchDirectionLoopCounter == 25)){
    			switchDirectionLoopCounter++;
    			myRobot.tankDrive(0, 0);
    			switchDirectionInProgress = false;
    			forwardDirection = forwardDirection ? false: true;
    			//CameraServer.getInstance().
    			//CameraServer.getInstance().removeCamera("forward");
    			//CameraServer.getInstance().removeCamera("backward");
    			//Server.getInstance().startAutomaticCapture("forward",forwardDirection ? 0 : 1);
    			//CameraServer.getInstance().startAutomaticCapture("backward",forwardDirection ? 1 : 0);
    			
    		}
    		else{   		
            moveValue = leftStick.getY();
            moveValue = forwardDirection ? moveValue : -moveValue;
            currentMotorCommand = calculateEasedMotorCommand(previousMotorCommand, moveValue);
            }
    		SmartDashboard.putNumber("switch direction loop counter", switchDirectionLoopCounter);
    		SmartDashboard.putString("switch direction in progress", switchDirectionInProgress ? "true" : "false");
            SmartDashboard.putString("Calculated Motor Command", Double.toString(currentMotorCommand));   
            SmartDashboard.putString("forward direction", forwardDirection ? "forward" : "reverse");
            rotateValue = 0.7 * leftStick.getX(); 
            SmartDashboard.putString("Rotate Value", Double.toString(rotateValue));
            SmartDashboard.putString("useTankDrive", useTankDrive ? "true" : "false");
            if (useTankDrive){
            	//moveRValue = rightStick.getY();
            	SmartDashboard.putString("useTankDrive", useTankDrive ? "true" : "false");
            	SmartDashboard.putNumber("moveRValue", moveRValue);
            	moveRValue = forwardDirection ? moveRValue : -moveRValue;
            	myRobot.tankDrive(moveValue, moveRValue);
            	SmartDashboard.putString("state", "teleop tank drive");
            }
            else{
            	SmartDashboard.putString("state", "teleop arcade drive");
            	myRobot.arcadeDrive(currentMotorCommand, rotateValue, squareJoystickYAxis);
            }
            
            previousMotorCommand = currentMotorCommand;
            
    
    }
    
    /**
     * This function is called periodically during test mode
     */
    public void testPeriodic() {
//	    	LiveWindow.run();
    } 
    
}
