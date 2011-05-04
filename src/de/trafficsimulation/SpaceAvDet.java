package de.trafficsimulation;
import java.util.Vector;

public class SpaceAvDet{

    private Vector avVel = new Vector();
    private Vector avDist = new Vector();
    private double DeltaX;
    private double pos;
    private int lane;

    public SpaceAvDet(double pos, double DeltaX, int lane){
	this.pos=pos;
	this.DeltaX=DeltaX;
	this.lane=lane;
    }

    public void update(Vector act_pos, Vector distances, Vector vel, Vector lanes){

	int count=(distances.size()<vel.size())
	    ?distances.size():vel.size();
	count=count-1;
	avVel = new Vector();
	avDist = new Vector();
	for (int i=0;i<=count;i++){
	    if ((((Integer)(lanes.elementAt(i))).intValue())==lane){
		double posc=((Double)(act_pos.elementAt(i))).doubleValue();
		if ((posc>(pos-DeltaX))&&(posc<(pos+DeltaX))){
		    avVel.insertElementAt((vel.elementAt(i)),0);
		    avDist.insertElementAt((distances.elementAt(i)),0);
		}
	    }
	}
    }
    
    public double density(){
	double dist=0.0;
	double density=0.0;
	int count=avDist.size();
	if (count>0){
	    for (int i=0;i<count;i++){
		dist=dist+((Double)(avDist.elementAt(i))).doubleValue();
	    }
	    density=((double)(count))/dist;
	}
	return density;
    }

    public double avVel(){
	double vel=0.0;
	int count=avVel.size();
	if (count>0){
	    for (int i=0;i<count;i++){
		vel=vel+((Double)(avVel.elementAt(i))).doubleValue();
	    }
	    vel=vel/count;
	}
	return vel;
    }
}
