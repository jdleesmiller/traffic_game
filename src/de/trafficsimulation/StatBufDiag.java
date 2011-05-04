package de.trafficsimulation;

// only for diagrams!

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.util.Vector;

public class StatBufDiag extends Component{

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
    private int bufferlength;

    private int xorg;
    private int yorg;
    private double xscale;
    private double yscale;
    
    private Vector xVals=new Vector();
    private Vector yVals=new Vector();

    private Image image;
    private Graphics g = null;
    private Color backgroundColor;
    private Color pixelColor;


    private int pwidth=2;
    private String xlabel,ylabel,label,title,xticLabel,yticLabel;
    private double xtic,ytic;
    private Font text=new Font("SansSerif",Font.PLAIN,12);
    private Font tics=new Font("SansSerif",Font.PLAIN,10);
    private FontMetrics metrictxt=getFontMetrics(text);
    private FontMetrics metrictics=getFontMetrics(tics);

    public StatBufDiag(double xmin, double xmax, double ymin, double ymax,
		       int nptsx, int nptsy, int bufferlength, Color bg, Color fg,
		       Image image, String xlabel, String ylabel,
		       String label, String title, double xtic, String xticLabel,
		       double ytic, String yticLabel){
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
	this.bufferlength=bufferlength;
	this.backgroundColor=bg;
	this.pixelColor=fg;
	this.image=image;
	this.xlabel=xlabel;
	this.ylabel=ylabel;
	this.label=label;
	this.title=title;
	this.xticLabel=xticLabel;
	this.yticLabel=yticLabel;
	this.xtic=xtic;
	this.ytic=ytic;

	xscale=(0.7*nptsx)/(xmax-xmin);
	yscale=(0.7*nptsy)/(ymax-ymin);
	
	xorg=(int)(0.15*nptsx)-(int)(xmin*xscale);
	yorg=(int)(0.85*nptsy)+(int)(ymin*yscale);

	this.g=image.getGraphics(); 
	
	(this.g).setColor(backgroundColor);
	(this.g).fillRect(0,0,nptsx,nptsy);

	paintAxes();

    }
    
    public void addPoint(double xval, double yval){

	int x=xPoint(xval);
	int y=yPoint(yval);
	Integer temp = new Integer(x);
	xVals.insertElementAt(temp, 0);
	temp = new Integer(y);
	yVals.insertElementAt(temp, 0);

	g.setColor(pixelColor);
	g.fillRect(x,y,pwidth,pwidth);

	int size = xVals.size();
	if (size>bufferlength){
	    int xdel=((Integer)(xVals.elementAt(size-1))).intValue();
	    int ydel=((Integer)(yVals.elementAt(size-1))).intValue();
	    g.setColor(backgroundColor);
	    g.fillRect(xdel,ydel,pwidth,pwidth);
	    xVals.removeElementAt(size-1);
	    yVals.removeElementAt(size-1);
	    paintAxes();
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

	g.setFont(tics);
	int tix=xPoint(xtic);
	g.drawLine(tix,yorg,tix,yorg-5);
	int xoff=(int)(0.5*metrictics.stringWidth(xticLabel));
	g.drawString(xticLabel,tix-xoff,yorg-5);

	int tiy=yPoint(ytic);
	g.drawLine(xorg,tiy,xorg-5,tiy);
	g.drawString(yticLabel,xorg,tiy+4);
	
	xoff= metrictxt.stringWidth(title);
	g.setFont(text);
	int tpos=(int)(0.5*(nptsx-xoff));
	g.drawString(title,tpos,nymax-5);
	xoff= metrictxt.stringWidth(xlabel);
	g.drawString(xlabel,nxmax-xoff,yorg+10);
	xoff= metrictxt.stringWidth(label);
	g.drawString(label,nxmax-xoff,nymax+10);
	xoff= metrictxt.stringWidth(ylabel);      
	g.drawString(ylabel,xorg-xoff,nymax+10);
	
    }
}
