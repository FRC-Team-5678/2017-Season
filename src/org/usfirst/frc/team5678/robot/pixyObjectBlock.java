package org.usfirst.frc.team5678.robot;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;

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
	
	void outputToFile(BufferedWriter bw)
	{
		try {
			bw.write("signature= " + signatureNumber);
			bw.newLine();
			bw.write("xPosition= " + xPosition);
			bw.newLine();
			bw.write("yPosition= " + yPosition);
			bw.newLine();
			bw.write("width= " + width);
			bw.newLine();
			bw.write("height= " + height);
			bw.newLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	void outputToFile(PrintWriter printWriter)
	{
	
			printWriter.println("signature= ," + Integer.toString(signatureNumber) + ",");
			printWriter.println("xPosition= ," + Integer.toString(xPosition) + ",");
			printWriter.println("yPosition= ," + Integer.toString(yPosition) + ",");
			printWriter.println("width= ," + Integer.toString(width) + ",");
			printWriter.println("height= ," + Integer.toString(height) + ",");	
			printWriter.format("\n\n");
	}
	
}
