package org.usfirst.frc.team5678.robot;

public class Segment {
	double timeLimit;
	double feedForward;
	double rightEncoderLimit;
	double leftEncoderLimit;
	boolean PIDControllerEnabled;
	double Kd;
	double Kp;
	double Ki;
	double Kf;
	
	Segment()
	{
		timeLimit = 0;
		PIDControllerEnabled = false;
		Kd = .1;
		Kp = .1;
		Ki = .1;
		Kf = .5;
	}
}
