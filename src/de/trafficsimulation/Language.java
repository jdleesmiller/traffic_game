package de.trafficsimulation;

/* 

     Portable Java code: \u00C0 etc 

      300   192   C0     À     LATIN CAPITAL LETTER A WITH GRAVE
       301   193   C1     Á     LATIN CAPITAL LETTER A WITH ACUTE
       302   194   C2     Â     LATIN CAPITAL LETTER A WITH CIRCUMFLEX
       303   195   C3     Ã     LATIN CAPITAL LETTER A WITH TILDE
       304   196   C4     Ä     LATIN CAPITAL LETTER A WITH DIAERESIS
       305   197   C5     Å     LATIN CAPITAL LETTER A WITH RING ABOVE
       306   198   C6     Æ     LATIN CAPITAL LETTER AE
       307   199   C7     Ç     LATIN CAPITAL LETTER C WITH CEDILLA
       310   200   C8     È     LATIN CAPITAL LETTER E WITH GRAVE
       311   201   C9     É     LATIN CAPITAL LETTER E WITH ACUTE
        312   202   CA     Ê     LATIN CAPITAL LETTER E WITH CIRCUMFLEX
       313   203   CB     Ë     LATIN CAPITAL LETTER E WITH DIAERESIS
       314   204   CC     Ì     LATIN CAPITAL LETTER I WITH GRAVE
       315   205   CD     Í     LATIN CAPITAL LETTER I WITH ACUTE
       316   206   CE     Î     LATIN CAPITAL LETTER I WITH CIRCUMFLEX
       317   207   CF     Ï     LATIN CAPITAL LETTER I WITH DIAERESIS
       320   208   D0     Ð     LATIN CAPITAL LETTER ETH
       321   209   D1     Ñ     LATIN CAPITAL LETTER N WITH TILDE
       322   210   D2     Ò     LATIN CAPITAL LETTER O WITH GRAVE
       323   211   D3     Ó     LATIN CAPITAL LETTER O WITH ACUTE
       324   212   D4     Ô     LATIN CAPITAL LETTER O WITH CIRCUMFLEX
       325   213   D5     Õ     LATIN CAPITAL LETTER O WITH TILDE
       326   214   D6     Ö     LATIN CAPITAL LETTER O WITH DIAERESIS
       327   215   D7     ×     MULTIPLICATION SIGN
       330   216   D8     Ø     LATIN CAPITAL LETTER O WITH STROKE
       331   217   D9     Ù     LATIN CAPITAL LETTER U WITH GRAVE
       332   218   DA     Ú     LATIN CAPITAL LETTER U WITH ACUTE
       333   219   DB     Û     LATIN CAPITAL LETTER U WITH CIRCUMFLEX
       334   220   DC     Ü     LATIN CAPITAL LETTER U WITH DIAERESIS
       335   221   DD     Ý     LATIN CAPITAL LETTER Y WITH ACUTE
       336   222   DE     Þ     LATIN CAPITAL LETTER THORN
       337   223   DF     ß     LATIN SMALL LETTER SHARP S
       340   224   E0     à     LATIN SMALL LETTER A WITH GRAVE
       341   225   E1     á     LATIN SMALL LETTER A WITH ACUTE
       342   226   E2     â     LATIN SMALL LETTER A WITH CIRCUMFLEX
       343   227   E3     ã     LATIN SMALL LETTER A WITH TILDE
       344   228   E4     ä     LATIN SMALL LETTER A WITH DIAERESIS
       345   229   E5     å     LATIN SMALL LETTER A WITH RING ABOVE
       346   230   E6     æ     LATIN SMALL LETTER AE
       347   231   E7     ç     LATIN SMALL LETTER C WITH CEDILLA
       350   232   E8     è     LATIN SMALL LETTER E WITH GRAVE
       351   233   E9     é     LATIN SMALL LETTER E WITH ACUTE
       352   234   EA     ê     LATIN SMALL LETTER E WITH CIRCUMFLEX
       353   235   EB     ë     LATIN SMALL LETTER E WITH DIAERESIS
       354   236   EC     ì     LATIN SMALL LETTER I WITH GRAVE
       355   237   ED     í     LATIN SMALL LETTER I WITH ACUTE
       356   238   EE     î     LATIN SMALL LETTER I WITH CIRCUMFLEX
       357   239   EF     ï     LATIN SMALL LETTER I WITH DIAERESIS
       360   240   F0     ð     LATIN SMALL LETTER ETH
       361   241   F1     ñ     LATIN SMALL LETTER N WITH TILDE
       362   242   F2     ò     LATIN SMALL LETTER O WITH GRAVE
       363   243   F3     ó     LATIN SMALL LETTER O WITH ACUTE

       364   244   F4     ô     LATIN SMALL LETTER O WITH CIRCUMFLEX
       365   245   F5     õ     LATIN SMALL LETTER O WITH TILDE
       366   246   F6     ö     LATIN SMALL LETTER O WITH DIAERESIS
       367   247   F7     ÷     DIVISION SIGN
       370   248   F8     ø     LATIN SMALL LETTER O WITH STROKE
       371   249   F9     ù     LATIN SMALL LETTER U WITH GRAVE
       372   250   FA     ú     LATIN SMALL LETTER U WITH ACUTE
       373   251   FB     û     LATIN SMALL LETTER U WITH CIRCUMFLEX
       374   252   FC     ü     LATIN SMALL LETTER U WITH DIAERESIS
       375   253   FD     ý     LATIN SMALL LETTER Y WITH ACUTE
       376   254   FE     þ     LATIN SMALL LETTER THORN
       377   255   FF     ÿ     LATIN SMALL LETTER Y WITH DIAERESIS
*/

public class Language {
	
	private static Language instance;
	
	// support for following languages:
	// German:   index=0
	// Englisch: index=1
	// Brasil: index=2
	// French: index=3
	
	private Language(){
		// empty 
	}
	
	public static Language getInstance(){
		if(instance == null){
			instance = new Language();
		}
		
		return instance;
	}
	
	
	private int langIndex = Constants.DEFAULT_LANG_INDEX;

	public void setIndex(int i){
		if(i < 0 || i > CAR_STRING.length){
			// error ...
			System.err.println("parameter: language index "+ i +" currently not supported ...");
		}
		else{
			langIndex=i;
		}
	}
	
	public int index(){ return langIndex; }
	
	
	// in SimCanvas:
	
	private String[] CAR_STRING = {"PKW", "Car", "Carro", "Voiture"};
	public String getCarString(){ return CAR_STRING[langIndex]; }
	
	private String[] TRUCK_STRING = {"LKW", "Truck", "Caminh\u00E3o", "Camions"};
	public String getTruckString(){ return TRUCK_STRING[langIndex]; }
	
	private String[] ID_STRING = {"Gleicher PKW-Typ", "Same Car", "Mesmo Carro", "M\u00EAme voiture"};
	public String getIdString(){ return ID_STRING[langIndex]; }
	
	private String[] PROBE_VEH_STRING = {"Testfahrzeug", "Probe Car", "Carro de Teste", "Voiture test"};
	public String getProbeVehString(){ return PROBE_VEH_STRING[langIndex]; }
	
	private String[] SIM_TIME = {"Simulierte Zeit  ", "Time  ", "Tempo  ", "Temps "};
	public String getSimTime(){ return SIM_TIME[langIndex]; }
	
	private String[] UPHILL_MARKER = {"Steigung", "uphill", "ladeira", "inclinaison"};
	public String getUmhillMarker(){ return UPHILL_MARKER[langIndex]; }
	
	private String[] UPHILL_MARKER_BEGIN = {"Beginn", "Begin", "Começo", "D\u00E9but"};
	public String getUphillMarkerBegin(){ return UPHILL_MARKER_BEGIN[langIndex]; }
	
	
	// in MicroSim:
	
	private String[] DESIRED_VEL =  {"Wunschgeschwindigkeit", "Desired Velocity", "Velocidade Desejada","Vitesse souhait\u00E9e"};
	public String getDesiredVelIDM(){ return DESIRED_VEL[langIndex]; }
	
    private String[] DESIRED_HEADWAY = {"Zeitlücke", "Time gap", "Intervalo de tempo", "Intervalle de temps"};
    public String getDesiredHeadwayIDM(){ return DESIRED_HEADWAY[langIndex]; }
    
    private String[] DESIRED_ACC = {"Beschleunigung", "Acceleration", "Aceleração", "Acc\u00E9l\u00E9ration"};
    public String getDesiredAccelIDM(){ return DESIRED_ACC[langIndex]; }
    
    private String[] DESIRED_DECEL = {"Bremsverzögerung", "Deceleration", "Desaceleração", "D\u00E9c\u00E9l\u00E9ration"};
    public String getDesiredDecelIDM(){ return DESIRED_DECEL[langIndex]; }
    
    private String[] MIN_GAP = {"Abstand im Stau", "Minimum gap", "Distância mínima", "\u00C9cart minimal"};
    public String getMinGapIDM(){ return MIN_GAP[langIndex]; }
    
    private String[] IDM_S1  = {"Abstand s1", "Distance s1", "Distância s1", "Distance s1"};
    public String getS1IDM(){ return IDM_S1[langIndex]; }
    
    private String[] AVG_DENSITY = {"Verkehrsdichte", "Average Density", "Densidade Média", "Densit\u00E9 moyenne"};
    public String getAvgDensity(){ return AVG_DENSITY[langIndex]; }
    
    private String[] INFLOW      = {"Haupt-Zufluss", "Main Inflow", "Fluxo de entrada Principal", "Flux entrant principal"};
    public String getInflow(){ return INFLOW[langIndex]; }
    
    private String[] RAMP_INFLOW = {"Zufluss der Zufahrt", "Ramp Inflow", "Fluxo de entrada Rampa", "Demande de la bretelle d'acc\u00E8s"};
    public String getRampInflow(){ return RAMP_INFLOW[langIndex]; }
    
    private String[] MOBIL_POLITE  = {"Höflichkeitsfaktor", "Politeness Factor", "Fator de Polidez", "Degr\u00E9 de politesse"};
    public String getMobilPoliteness(){ return MOBIL_POLITE[langIndex]; }
    
    private String[] MOBIL_RAMP_POLIT  = {"p-Faktor Zufahrt",
   "p-factor ramp","Fator-p Rampa", "Coefficient p de la bretelle d'ac\u00E8s"};
    public String getMobilPolitenessRamp(){ return MOBIL_RAMP_POLIT[langIndex]; }
    
    private String[] TRUCK_PERC  = {"LKW-Anteil", "Truck Percentage", "Porcentagem de Caminhões","Proportion de camions"};
    public String getTruckPerc(){ return TRUCK_STRING[langIndex]; }
    
    private String[] MOBIL_THRES = {"Wechselschwelle", "Changing Threshold", "Limite de Alteração","Limitation de changement de voie"};
    public String getMobilThreshold(){ return MOBIL_THRES[langIndex]; }
    
    private String[] MOBIL_RAMP_BIAS = {"a_bias,Zufahrt", "a_bias,onramp", "a_bias,rampa","a_bias, acc\u00E8s"};
    public String getMobilRampBias(){ return MOBIL_RAMP_BIAS[langIndex]; }
    
    private String[] TIME_WARP  = {"Zeitlicher Warp-Faktor", "Time Warp Factor", "Fator de Time Warp", "Dilatation temporelle"};
    public String getTimeWarp(){ return TIME_WARP[langIndex]; }
    
    private String[] FRAMERATE   = {" - fach", " times", " vezes", " fois"};
    public String getFramerate(){ return FRAMERATE[langIndex]; }
    
    private String[] SPEEDLIMIT  = {"Tempolimit", "Imposed Speed Limit", "Limite de Velocidade", "Limitation de vitesse"};
    public String getSpeedlimit(){ return SPEEDLIMIT[langIndex]; }
    
    private String[] VEH_PER_HOUR  = {" Kfz/h", " Vehicles/h", " Veículos/h", "V\u00E9hicules/h"};
    public String getVehPerHour(){ return VEH_PER_HOUR[langIndex]; }
    
    private String[] VEH_PER_KM  = {" Kfz/km/Spur", " Veh./km/lane", " Veíc./km/pista", "V\u00E9h./km/voie"};
    public String getVehPerKm(){ return VEH_PER_KM[langIndex]; }
    
    private String[] START  = {"Start", "Start", "Iniciar", "D\u00E9marrer"};
    public String getStartName(){ return START[langIndex]; }
    private String[] STOP  = {"Stop", "Stop", "Pausa","Arr\u00EAter"};
    public String getStopName(){ return STOP[langIndex]; }

    private String[] SCEN_RING  = {"Ringstraße", "Ring Road", "Estrada Circular", "Circuit"};
    public String getScenRingName(){ return SCEN_RING[langIndex]; }
    
    private String[] SCEN_ONRAMP  = {"Zufahrt", "On-Ramp", "Com Rampa", "Bretelle d'acc\u00E8s"};
    public String getScenOnrampName(){ return SCEN_ONRAMP[langIndex]; }
    
    private String[] SCEN_LANE_CLOSING  = {"Spursperrung","Laneclosing", "Pista fechada","Voie ferm\u00E9"};
    public String getScenLaneclosingName(){ return SCEN_LANE_CLOSING[langIndex]; }
    
    private String[] SCEN_UPHILL  = {"Steigung", "Uphill Grade", "Com Ladeira", "Route en pente"};
    public String getScenUphillName(){ return SCEN_UPHILL[langIndex]; }
    
    private String[] SCEN_TRAFFIC_LIGHTS  = {"Stadtverkehr", "Traffic Lights", "Semáforo", "Feux tricolores"};
    public String getScenTrafficlightsName(){ return SCEN_TRAFFIC_LIGHTS[langIndex]; }
    
    
    private String[] BUTTON_PERTURBATION  = {"Verurache Störung!", "Apply Perturbation!", "Causar Perturbação!", "Appliquer une perturbation"};
    public String getPerturbationButton(){ return BUTTON_PERTURBATION[langIndex]; }
    

    
	
}
