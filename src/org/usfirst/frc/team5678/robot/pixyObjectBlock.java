package org.usfirst.frc.team5678.robot;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class pixyObjectBlock{  
	int xPosition;
	int yPosition;
	int width;
	int height;	
	int signatureNumber;
	int checksum;
	boolean verifiedChecksum;
	
	pixyObjectBlock(){
		xPosition = 0;
		yPosition = 0;
		width = 0;
		height = 0;	
		signatureNumber = -1;
		checksum = 0;	
		verifiedChecksum = false;
	}
	
	void outputToSmartDashboard()
	{
        SmartDashboard.putNumber("signature", signatureNumber);
        SmartDashboard.putNumber("xPosition", xPosition);
        SmartDashboard.putNumber("yPosition", yPosition);
        SmartDashboard.putNumber("width", width);
        SmartDashboard.putNumber("height", height);
	}
}
