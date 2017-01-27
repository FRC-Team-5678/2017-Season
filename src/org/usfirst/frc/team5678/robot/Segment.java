package org.usfirst.frc.team5678.robot;

public class Segment {
	double timeLimit;
	int feedForward;
	int rightEncoderLimit;
	int leftEncoderLimit;
	boolean PIDControllerEnabled;
	double Kd;
	double Kp;
	double Ki;
	double Kf;
	
	Segment()
	{
		PIDControllerEnabled = false;
		Kd = .1;
		Kp = .1;
		Ki = .1;
		Kf = .5;
	}
}
