package de.trafficsimulation;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Scrollbar;



public class AdjustmentMethods implements Constants{


    static final double REL_SBTEXTHEIGHT = 0.015; // textheight/simWindow size
    static final int    MAX_SBTEXTHEIGHT = 24;  //12
    static final int    MIN_SBTEXTHEIGHT = 8;  //12



    //################################################
    // Adjustments by mouse motions
    //################################################

    // increment/decrement variable as reaction to pixel increments
    // incr, e.g., obtained by MouseMotionListeners.
    // The whole var range from varMin to varMax corresponds
    // to rangePix, 
    // if logarithmic=true; changes of the logarithms of variables
    // are proportional to incr

    public static double changeVarByIncr(double incr, int rangePix, 
                                            double var,
                                            double varMin, double varMax,
                                            boolean logarithmic){
      double arg=var;
      if(arg<Math.min(varMin,varMax)){arg=Math.min(varMin,varMax);}
      if(arg>Math.max(varMin,varMax)){arg=Math.max(varMin,varMax);}
      if(logarithmic&&(arg<0.000001)){arg=0.000001;}

      double lnMin=(logarithmic) ? Math.log(varMin) : 0;
      double lnMax=(logarithmic) ? Math.log(varMax) : 1;

      double relpos=(logarithmic) 
	  ? (Math.log(arg)-lnMin)/(lnMax-lnMin)
	  :  (arg-varMin)/(varMax-varMin);

      double relposNew=relpos+incr/(double)rangePix;
      relposNew=Math.max(Math.min(relposNew,1), 0);

      double result=(logarithmic)
	  ? Math.exp(lnMin + relposNew*(lnMax-lnMin))
	  : varMin + relposNew*(varMax-varMin);

      if(false){
        System.out.println("changeVarByIncr: var="+var+" relpos="+relpos
			 +" relposNew="+relposNew
			 +" result="+result);
      }
      return result;
    }

    //################################################
    // functions for real-valued control input in [0,1],
    // e.g. from Joystick,
    // with linear or logarithmic control curve
    // Treiber 31.08.04: Neu
    //################################################
    public static double getVariableFromRel(double relpos, 
                                            double varMin, double varMax,
                                            boolean logarithmic){
      if(logarithmic){
        double lnMin=Math.log(varMin);
        double lnMax=Math.log(varMax);
        //System.out.println("relpos="+relpos+" lnMin="+lnMin);
        return(Math.exp(lnMin + relpos*(lnMax-lnMin)));
      }
      else{
	  return(varMin + relpos*(varMax-varMin));
      }
    }


    //################################################
    // functions for sliders with linear or logarithmic control curve
    // Treiber 31.08.04: umformuliert, selbe Funktionalitaet wie bisher
    //################################################

    // sliderPos pos = integer units from 0 to SLIDER_STEPS
    public static double getVariableFromSliderpos(int sliderPos, 
                                            double varMin, double varMax,
                                            boolean logarithmic){
      final int SLIDER_STEPS=1000;
      double relpos=((double)sliderPos)/SLIDER_STEPS;
      return getVariableFromRel(relpos,varMin, varMax, logarithmic);
    }


    // inverse of above function
    public static int getSliderposFromVariable(double var, 
                                         double varMin, double varMax,
                                         boolean logarithmic){
      final int SLIDER_STEPS=1000;
      double relpos=0;
      double arg=var;
      if(arg<Math.min(varMin,varMax)){arg=Math.min(varMin,varMax);}
      if(arg>Math.max(varMin,varMax)){arg=Math.max(varMin,varMax);}

      if(logarithmic){
        if (arg<0.000001){arg=0.000001;}
        double lnMin=Math.log(varMin);
        double lnMax=Math.log(varMax);
        relpos=(Math.log(arg)-lnMin)/(lnMax-lnMin);
        if(false){
          System.out.println("!!!getSliderposFromVariable: lnMin="+lnMin
			 +" lnMax="+lnMax+" lnarg="+Math.log(arg)
                         +" var="+var+" arg="+arg+" varMin="+varMin
                         +" varMax="+varMax+" relpos="+relpos);
	}
      }

      else{
	  relpos=(arg-varMin)/(varMax-varMin);
      }


      return( (int)(SLIDER_STEPS*relpos));
    }






    public static void addScrollbar(Panel panel, int row, Scrollbar sb, 
			      String str_quantity, Label changeableLabel){

      GridBagConstraints gbconstr = new GridBagConstraints();


      // width of the paddings at the 4 edges (N,W,S,E)
      gbconstr.insets = new Insets(SB_SPACEY,SB_SPACEX,SB_SPACEY,SB_SPACEX);
      //gbconstr.insets = new Insets(0,0,0,0);

      gbconstr.gridx = 0;
      gbconstr.gridy = row-1;
      gbconstr.fill = GridBagConstraints.NONE;
      gbconstr.anchor = GridBagConstraints.EAST;
      gbconstr.weightx = 0;
      gbconstr.weighty = 1;


      panel.add(new Label(str_quantity), gbconstr);

	// 2th column: actual scrollbars

      gbconstr.gridx = 1;
      gbconstr.gridy = row-1;
      gbconstr.weightx = 0.5;
      gbconstr.fill = GridBagConstraints.HORIZONTAL;
      gbconstr.anchor = GridBagConstraints.CENTER;
      panel.add(sb,gbconstr);
    

	// 3th column: Actual values + units

      gbconstr.gridx = 2;
      gbconstr.gridy = row-1;
      gbconstr.weightx = 0.;
      gbconstr.fill = GridBagConstraints.NONE;
      gbconstr.anchor = GridBagConstraints.WEST;
      panel.add(changeableLabel, gbconstr);


    } // end addScrollbar


    public static Scrollbar getSB (double min, double max, double init, 
			     boolean logarithmic){
      final int knobSize = 1;
      final int SLIDER_STEPS=1000;

      int initPos=getSliderposFromVariable(init,min,max,logarithmic);
      Scrollbar sb= new Scrollbar(Scrollbar.HORIZONTAL, 
				  initPos, knobSize, 0, SLIDER_STEPS);

      //sb.setFont(textFont); 
     //DOS hier => Fonts in ../../MicroSim.java gesetzt =>sbFontHeight 

      //sb.setSize(10,3); //!!DOS hier

      if(false){
        System.out.println("getSB: init="+init
			 +" min="+min+" max="+max
                         +" init="+init+" initPos="+initPos);
      }
      return sb;
    }




} 

