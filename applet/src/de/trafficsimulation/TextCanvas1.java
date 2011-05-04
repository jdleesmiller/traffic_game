package de.trafficsimulation;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.TextArea;


// TextArea: No colors or different fonts simultaneously, but much more
// simple than Ansgars soution

public class TextCanvas1 extends TextArea{
    

    
/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
private String title1="Closed System (Ring Road)";
private String text1= "The dynamics depends essentially on the average vehicle density rho, the main control parameter in closed systems. The following ranges depend on the number and distibution of trucks and can vary from simulation to simulation.\n* Density rho < 8 vehicles/km/lane: Free traffic\n* rho = 8..15 veh./km/lane: Queues of vehicles behind trucks\n* rho = 15..25 veh./km/lane: Dense traffic, but no queues or stop-ang-go waves\n* rho = 25..55 veh./km/lane: Stop-and-go waves\n* rho>50 veh./km/lane: Several regions with standing traffic separated by regions where vehicles move slowly.";

private String title2="Open System with On Ramp";
private String text2="This szenario demonstrates, how an on-ramp acts as a stationary bottleneck provoquing a traffic breakdown on the main road UPSTREAM of the on-ramp with a STATIONARY downstream front and FREE traffic downstream.\n* With the initial settings, upstream propagating stop-and-go waves are triggered on the main road\n* At an reduced inflow of 2000 veh./h, free traffic can be sustained for up to 20 min but sooner or later breaks down\n* At 1600 veh./h, no breakdown occurs, but an existing jam will not dissolve\n* Observe a stationary traffic breakdown pinned at the ramp region for a ramp flow of 800 veh./h, and a main flow of 1600 veh./h\n* Play with the politeness factor and switching threshold to change the aggressivity of the ramp vehicles.";

private String title3="Development of congested traffic at bottlenecks";
private String text3="We simulate an open system with a lane closing (e.g., due to road works) at the position of the white squares. Tune the truck percentage at inflow to observe the remarkably positive effect of a homogeneous vehicle population:\n(i) No breakdown for 100 % trucks\n(ii) Breakdown with, e.g., 50% trucks, although the theoretical (static) capacity is higher in the latter case!.\n The dynamics of the resulting traffic jam is similar to the on-ramp scenario 2. Since speed limits have similar homogenizing effects, this means \"unfortunately\" that speed limits can be useful if (i) there is  heavy traffic, and (ii) there is a bottleneck.";

private String title4="Uphill Gradients";
private String text4="Uphill and downhill gradients are examples of \"flow-conserving bottlenecks\". We implemented it by reducing the maximum (\"desired\") speed, especially of the trucks. Notice that inposing a speed limit locally on a homogeneous road may have the same negative effect!";

private String title5="Red Traffic Light";
private String text5="This simulation shows the deceleration of vehicles to a complete stop (due to, e.g., a red traffic light or road closing), and the subsequent acceleration after the traffic light switches to green. The traffic flux of the outflowing traffic is the same as the outflow of stop-ang-go waves";

private String title6="Vehicle Mixing by Forced Lane Changes";
private String text6="This scenario shows lane changes forced by standing obstacles. Although the mathematical simulation model is time-continuous and deterministic (see \"background information\"), the symmetric initial state becomes irregular and the order of vehicles becomes mixed up in the course of time. All vehicles are identical. For illustrative purposes, we painted half of them red and the other half green.";



    public TextCanvas1 (int choice_Szen){
	super("",8,60,SCROLLBARS_VERTICAL_ONLY);
	update(choice_Szen);
	setEditable(false);          // grey if "false"; white otherwise
        setBackground(Color.white);
    }


    public void update(int choice_Szen){
        Font textFont = new Font("SansSerif",Font.PLAIN,12);
        setFont(textFont);           // Only 1 font possible!
        setColumns(50);
        setRows(10);
	//	Color bg = new Color(200,100,0);;
	String actTitle="";
	String underlineTitle="";
	String actText="";
	
	if (choice_Szen==1){
	    actTitle=title1;
	    actText=text1;
	}
	if (choice_Szen==2){
	    actTitle=title2;
	    actText=text2;
	}
	if (choice_Szen==3){
	    actTitle=title3;
	    actText=text3;
	}
	if (choice_Szen ==4){
	    actTitle=title4;
	    actText = text4;
	}
	if (choice_Szen ==5){
	    actTitle=title5;
	    actText = text5;
	}
	if (choice_Szen ==6){
	    actTitle=title6;
	    actText = text6;
	}

        
        FontMetrics fm = (new Frame().getFontMetrics(textFont));
        char underlineChar = '~';
        int widthTitle = fm.stringWidth(actTitle);        // in pixels
        int widthUnderlChar = fm.charWidth(underlineChar);
        for (int i=0; i<= (int)(widthTitle/widthUnderlChar); i++){
           underlineTitle += underlineChar;
	}

        setText("                " + actTitle       +'\n');
        append( "                " + underlineTitle +'\n');
        append(actText);
    }
}

