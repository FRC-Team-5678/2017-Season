package org.usfirst.frc.team5678.robot;

public class Trajectory {
	Segment[] segments;
	String name;
	String selectionString;
	
	Trajectory(String _name)
	{
		Segment[] segments = new Segment[3];
		for (int i=0; i < 3; i++)
		{		
			segments[i] = new Segment();
			name = _name;
		}
	}
}
