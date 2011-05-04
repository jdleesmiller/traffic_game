package de.trafficsimulation;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.util.Vector;

// only for diagrams!

public class DynBufDiag extends Component{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private double xmin;
    private double xmax;
    private double ymin;
    private double ymax;
    private int nptsx;
    private int nptsy;
    private double maxdiff;

    private int xorg;
    private int yorg;
    private double xscale;
    private double yscale;
    
    private Vector xVals=new Vector();
    private Vector yVals=new Vector();
    private Vector NR=new Vector();

    private Image image;
    private Graphics g = null;
    private Color backgroundColor;
    private Color pixelColor;

    private int pwidth=2;

    private String xlabel,label,title,xticLabel;
    private String[] ylabels;
    private String[] yticLabels;
    private double[] ytics;
    private double xtic;
    private Color[] colors;
    private Font text=new Font("SansSerif",Font.PLAIN,12);
    private FontMetrics metrictxt=getFontMetrics(text);
    private Font tics=new Font("SansSerif",Font.PLAIN,10);
    private FontMetrics metrictics=getFontMetrics(tics);


    public DynBufDiag(double xmin, double xmax, double ymin, double ymax,
		       int nptsx, int nptsy, double maxdiff, Color bg, Color fg,
		      Image image, String xlabel, String[] ylabels,
		      Color[] colors, String label, String title,
		      double xtic, String xticLabel, 
		      double[] ytics, String yticLabels[]){
	if (xmin>0.0){xmin=0.0;}
	if (ymin>0.0){ymin=0.0;}
	if (xmax<0.0){xmax=0.0;}
	if (ymax<0.0){ymax=0.0;}
	this.xmin=xmin;
	this.xmax=xmax;
	this.ymin=ymin;
	this.ymax=ymax;
	this.nptsx=nptsx;
	this.nptsy=nptsy;
	this.maxdiff=maxdiff;
	this.backgroundColor=bg;
	this.pixelColor=fg;
	this.image=image;
	this.xlabel=xlabel;
	this.ylabels=ylabels;
	this.colors=colors;
	this.label=label;
	this.title=title;
	this.yticLabels=yticLabels;
	this.xtic=xtic;
	this.xticLabel=xticLabel;
	this.ytics=ytics;
	
	xscale=(0.7*nptsx)/(xmax-xmin);
	yscale=(0.7*nptsy)/(ymax-ymin);
	
	xorg=(int)(0.15*nptsx)-(int)(xmin*xscale);
	yorg=(int)(0.85*nptsy)+(int)(ymin*yscale);

	this.g = image.getGraphics();
	
	(this.g).setColor(backgroundColor);
	(this.g).fillRect(0,0,nptsx,nptsy);

	paintAxes();

    }
    
    public void addPoint(double xval, double yval, int nr){

	Double temp = new Double(yval);
	Integer tint= new Integer(nr);
	yVals.insertElementAt(temp, 0);
	NR.insertElementAt(tint, 0);
	Double xmax_val = new Double(xval);
	xVals.insertElementAt(xmax_val, 0);
	int size = xVals.size();
	size=size-1;
	while ((xval-((Double)(xVals.elementAt(size))).doubleValue())>maxdiff){
	    
	    xVals.removeElementAt(size);
	    yVals.removeElementAt(size);
	    size = xVals.size();
	    size = size - 1;
	}
	

	temp=(Double)xVals.elementAt(size);
	double min_x_val=temp.doubleValue();
	
	g.setColor(backgroundColor);
	g.fillRect(0,0,nptsx,nptsy);
	paintAxes();
	for (int i=0;i<=size;i++){
	    Double xco=(Double) xVals.elementAt(i);
	    Double yco=(Double) yVals.elementAt(i);
	    int x = xPoint(xco.doubleValue()-min_x_val);
	    int y = yPoint(yco.doubleValue());
	    g.setColor(colors[(((Integer)(NR.elementAt(i))).intValue())]);
	    g.fillRect(x,y,pwidth,pwidth);
	}
    }

    private int xPoint(double xval){
	int x=xorg;
	x=x+(int)(xval*xscale-(double)(pwidth)*0.5);
	return x; 
    }

    private int yPoint(double yval){
	int y=yorg;
	y=y-(int)(yval*yscale+(double)(pwidth)*0.5);
	return y;
    }

    public Image picture(){
	return image;
    }

    private void paintAxes(){

	int nxmax=xPoint(xmax);
	int nxmin=xPoint(xmin);
	int nymin=yPoint(ymin);
	int nymax=yPoint(ymax);
	
    	g.setColor(pixelColor);

	int arrow=(int) (nptsx*0.02);
	g.drawLine(xorg,yorg,xorg,nymax);
	g.drawLine(xorg,nymax,xorg-arrow,nymax+arrow);
	g.drawLine(xorg,nymax,xorg+arrow,nymax+arrow);

	g.drawLine(xorg,yorg,xorg,nymin);
	g.drawLine(xorg,yorg,nxmin,yorg);
	g.drawLine(xorg,yorg,nxmax,yorg);
	g.drawLine(nxmax,yorg,nxmax-arrow,yorg-arrow);
	g.drawLine(nxmax,yorg,nxmax-arrow,yorg+arrow);
	
	int xoff= metrictxt.stringWidth(title);
	g.setFont(text);
	int tpos=(int)(0.5*(nptsx-xoff));
	g.drawString(title,tpos,nymax-5);
	xoff= metrictxt.stringWidth(xlabel);
	g.drawString(xlabel,nxmax-xoff,yorg+10);
	xoff= metrictxt.stringWidth(label);
	g.drawString(label,nxmax-xoff,nymax+10);
	int ypos=0;
	
	g.setFont(tics);
	int tix=xPoint(xtic);
	g.drawLine(tix,yorg,tix,yorg-5);
	xoff=(int)(0.5*metrictics.stringWidth(xticLabel));
	g.drawString(xticLabel,tix-xoff,yorg-5);

	int tiy;
	for(int i=0;i<ylabels.length;i++){
	    	xoff= metrictxt.stringWidth(ylabels[i]);
		g.setFont(text);
		g.setColor(colors[i]);
		g.drawString(ylabels[i],xorg-xoff,nymax+10+ypos);
		ypos=ypos+12;
		
		g.setFont(tics);
		tiy=yPoint(ytics[i]);
		g.drawLine(xorg,tiy,xorg-5,tiy);
		g.drawString(yticLabels[i],xorg,tiy+4);
	
	}
    }
}
