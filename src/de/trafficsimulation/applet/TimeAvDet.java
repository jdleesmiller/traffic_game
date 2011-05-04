package de.trafficsimulation.applet;
import java.util.Vector;

public class TimeAvDet{

    private Vector<Double> buffer = new Vector<Double>();
    private Vector<Double> times = new Vector<Double>();
    private double DeltaT;
    private double pos;
    private int lane;

    public TimeAvDet(double pos, double DeltaT, int lane){
	this.pos=pos;
	this.DeltaT=DeltaT;
	this.lane=lane;
    }

    public void update(Vector<Double> act_pos, Vector<Double> old_pos, Vector<Double> vel, 
		       Vector<Integer> lanes, double time){
	int count=act_pos.size();
	for (int i=0;i<count;i++){
	    if(((Integer)(lanes.elementAt(i))).intValue()==lane){
		double pos1=((Double)(old_pos.elementAt(i))).doubleValue();
		double pos2=((Double)(act_pos.elementAt(i))).doubleValue();
		if ((pos1<pos)&&(pos2>=pos)){
		    buffer.insertElementAt((vel.elementAt(i)),0);
		    times.insertElementAt((new Double(time)),0);
		}
	    }
	}
	count = buffer.size();
	if (count>0){
	    count = count -1;
	    double tmin=((Double)(times.elementAt(count))).doubleValue();
	    while((time-tmin>DeltaT)&&(count>=0)){
		buffer.removeElementAt(count);
		count = buffer.size();
		count = count -1;
		if (count>=0){
		tmin=((Double)(times.elementAt(count))).doubleValue();
		}
	    }
	}
    }
    
    public double flow(){
	int count=buffer.size();
	double val=0.0;
	for(int i=0; i <count; i++){
	    val=val+1.0;
	}
	return (val/DeltaT);
    }

    public double harmVel(){
	int count=buffer.size();
	double val=0.0;
	double nr=0.0;
		double min=1000.0;
	for(int i=0; i <count; i++){
	    double temp=((Double)(buffer.elementAt(i))).doubleValue();
	    if (temp<min){min=temp;}
	    val=val+(1/temp);
	    nr=nr+1.0;
	}
	if (nr>0.0){
	    return (nr/val);}
	else{return 0.0;}
    }
        public double arrVel(){
	int count=buffer.size();
	double val=0.0;
	double nr=0.0;
	double min=1000.0;
	for(int i=0; i <count; i++){
	    double temp=((Double)(buffer.elementAt(i))).doubleValue();
	    if (temp<min){min=temp;}
	    val=val+temp;
	    nr=nr+1.0;
	}
	if (nr>0.0){
	    return (val/nr);
	}
	else{return 0.0;}
    }
}
