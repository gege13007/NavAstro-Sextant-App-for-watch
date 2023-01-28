package com.samblancat.vastro;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class Main extends AppCompatActivity  {
    Context mContext;
    SharedPreferences sharedPref;

    static double ff = 57.295779513082;

    static int yyyy = 2022;
    static int mois = 10;
    static int day = 30;

    //Julian Day, T
    static double jdm, T;

    //flag si heure courante
    boolean running;
    static Calendar rNow;

    //no astre & vue selectionné
    static int astre, modevue;

    //Coords calculées
    static double ahso, ahvo, ahvg, dec, obliq;
    static double demid, age, dst;
    //Hauteur mesurée Compensée !
    static public double ho;

    //Droite de Hauteur calculée
    static public double Hc, AZ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext=this;
        sharedPref = getBaseContext().getSharedPreferences("SEXTANT", MODE_PRIVATE);
        glob.latit = sharedPref.getFloat("latit", (float) 45.0);
        glob.longit = sharedPref.getFloat("longit", (float) 5.0);
        glob.hmes = sharedPref.getFloat("hmes", (float) 25.0);
        glob.hmetre = sharedPref.getFloat("hmetre", (float) 3.0);
        glob.collim = sharedPref.getFloat("collim", (float) 0.0);

        setContentView(R.layout.liste);

        //init date
        rNow = Calendar.getInstance();
        final Button utbut = findViewById(R.id.utinp);
        utbut.setBackgroundColor(Color.GREEN);
        running=true;

        //------ Fait Spin liste des Astres disponibles ------
        String[] astres = new String[2];
        astres[0] = "Sun";
        astres[1] = "Moon";

        //Remplis la liste Astres
        ArrayAdapter adapter = new ArrayAdapter<String>(mContext, R.layout.spinner_item, astres);
        final Spinner spin = findViewById(R.id.starlist);
        spin.setAdapter(adapter);

        spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                astre = spin.getSelectedItemPosition();
                //refresh tous les calculs
                refresh();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        //------ Fait Spin liste des Modes de Mesures ------
        String[] modes = new String[3];
        modes[0] = "Low limb";
        modes[1] = "Upper limb";
        modes[2] = "Artificial";

        //Remplis la liste Astres
        ArrayAdapter adapter2 = new ArrayAdapter<String>(mContext, R.layout.spinner_item, modes);
        final Spinner spin2 = findViewById(R.id.modelist);
        spin2.setAdapter(adapter2);

        spin2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                modevue = spin2.getSelectedItemPosition();
                //refresh tous les calculs
                refresh();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        //Lance Timer défilement de l'heure
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (running) {
                            rNow = Calendar.getInstance();
                            dst = (rNow.get(Calendar.ZONE_OFFSET)) / 3600000;
                            int ss = (rNow.get(Calendar.SECOND));
                            int mm = (rNow.get(Calendar.MINUTE));
                            double hh = -dst + rNow.get(Calendar.HOUR_OF_DAY);
                            String dat = rNow.get(Calendar.DAY_OF_MONTH) + "/" + (1 + rNow.get(Calendar.MONTH));
                            TextView txt = findViewById(R.id.utinp);
                            txt.setText(new DecimalFormat("#0").format(hh) + ":" + new DecimalFormat("00").format(mm) + ":" + new DecimalFormat("00").format(ss) + "  " + dat);
                            //Recalcule a chaque seconde
                            refresh();
                        }
                    }
                });
            }
        };
        timer.scheduleAtFixedRate(timerTask, 0, 1000);

        utbut.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Arrête / Relance le chrono !
                if (!running) utbut.setBackgroundColor(Color.GREEN); else utbut.setBackgroundColor(Color.RED);
                running = !running;
                refresh();
            }
        });

        final Button latbut = findViewById(R.id.latitinp);
        latbut.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
              glob.legende = "Latitude estime";
              glob.inp = glob.latit;
              glob.nval = 1;
              glob.unit = "'";
              Intent intent = new Intent(Main.this, input.class);
              startActivity(intent);
            }
        });

        final Button longbut = findViewById(R.id.longitinp);
        longbut.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
              glob.legende = "Longit estime";
              glob.inp = glob.longit;
              glob.nval = 2;
              glob.unit = "'";
              Intent intent = new Intent(Main.this, input.class);
              startActivity(intent);
            }
        });

        final Button hmesbut = findViewById(R.id.hsextantinp);
        hmesbut.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
             glob.legende = "Hauteur mes";
             glob.inp = glob.hmes;
             glob.nval = 3;
             glob.unit = "'";
             Intent intent = new Intent(Main.this, input.class);
             startActivity(intent);
            }
        });

        final Button hautmesbut = findViewById(R.id.hauteurminp);
        hautmesbut.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                glob.legende = "Alt metre";
                glob.inp = glob.hmetre;
                glob.nval = 4;
                glob.unit = "m";
                Intent intent = new Intent(Main.this, inputdec.class);
                startActivity(intent);
            }
        });

        final Button collimbut = findViewById(R.id.colliminp);
        collimbut.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                glob.legende = "Collimation";
                glob.inp = 60 * glob.collim;
                glob.nval = 5;
                glob.unit = "'";
                Intent intent = new Intent(Main.this, inputdec.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        Log.d("Proc", "onUSERLeave");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("Proc", "onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        //refresh tous les calculs
        refresh();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sharedPref = getBaseContext().getSharedPreferences("SEXTANT", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putFloat("latit", (float)glob.latit);
        editor.putFloat("longit", (float)glob.longit);
        editor.putFloat("hmes", (float)glob.hmes);
        editor.putFloat("hmetre", (float)glob.hmetre);
        editor.putFloat("collim", (float)glob.collim);
        editor.commit();
        editor.apply();

        //ASSURE FIN DU PROCESS !!!
        Log.d("Proc", "killPROCESS");
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    //---------- Fait et affiche les Calculs ---------
    public void refresh() {

      julian(rNow);

      f_aHso();

      TextView txt = findViewById(R.id.sunset);
      txt.setText(sunset());

      if (astre == 0) {
          f_aHvo();
          txt = findViewById(R.id.suntxt);
          txt.setText("GHA= " + set_dmm(ahvo) + "  dec=" + set_dmm(dec));
      } else {
          f_aHao();
          txt = findViewById(R.id.suntxt);
          txt.setText("aHao= " + set_dmm(ahvo) + "  dec=" + set_dmm(dec));
      }

      //aiguillage des valeurs retour
      if (glob.nval==1) glob.latit = glob.inp;
      if (glob.nval==2) glob.longit = glob.inp;
      if (glob.nval==3) glob.hmes = glob.inp;
      if (glob.nval==4) glob.hmetre = glob.inp;
      if (glob.nval==5) glob.collim = glob.inp/60.0;    // en minutes direct

        //reaffiche les valeurs
        Button but = findViewById(R.id.latitinp);
        but.setText("Latit "+set_dmm(glob.latit));
        but = findViewById(R.id.longitinp);
        but.setText("Long "+set_dmm(glob.longit));
        but = findViewById(R.id.hsextantinp);
        but.setText("He "+set_dmm(glob.hmes));
        but = findViewById(R.id.hauteurminp);
        but.setText("Alt "+new DecimalFormat("#0.#").format(glob.hmetre)+"m");
        but = findViewById(R.id.colliminp);
        but.setText("Collim "+set_min(60*glob.collim));    // collimation en minutes

        //Corrige hauteur sextant (Lower Limb)
        if (modevue==0)
          ho = glob.hmes + glob.collim + demid;
        //Upper Limb
        if (modevue==1)
          ho = glob.hmes + glob.collim  - demid;

        if (modevue == 2)
            ho = glob.hmes / 2;
        else {
            //Enlève parralaxe DIP correction
            ho = ho - (1.76 * Math.sqrt(glob.hmetre)) / 60;   // hauteur 2 mètres

            //Calc refraction
            double r = ho + (7.31 / (4.4 + ho));
            r = 1 / (Math.tan(r / ff));
            ho = ho - (r / 60);
        }

        //Aff hauteur corrigée & demid
        txt = findViewById(R.id.he_z);
        txt.setText("hv="+set_dmm(ho)+" demid="+new DecimalFormat("#0.#").format(60*demid)+"'");

        //Fait calcul de hauteur
        ahvg= ahvo + glob.longit;
        Log.d("aHvg=", String.valueOf(ahvg));
        double sinhe = (mySin(dec) * mySin(glob.latit)) + (myCos(dec) * myCos(glob.latit) * myCos(ahvg));
        Hc = ff * Math.asin(sinhe);

        //affiche intercept
        double p=60*(ho - Hc);
        txt = findViewById(R.id.intertxt);
        txt.setText("Intercept = "+new DecimalFormat("#0.#").format(p)+"m");

        AZ = mySin(dec) - (mySin(glob.latit) * sinhe);
        AZ /= (myCos(glob.latit) * myCos(Hc));
        AZ = ff * Math.acos(AZ);
        if (ahvg < 180.0) AZ = 360-AZ;

        //Affiche azimut
        txt = findViewById(R.id.azimtxt);
        txt.setText("Azimut = "+set_dmm(AZ));
    }

    public double myCos(double a) {
        return Math.cos(a / ff);
    }

    public double mySin(double a) {
        return  Math.sin(a / ff);
    }

    public String set_min(double x) {
        String st="";
        if (x<0) {
            st="-";
            x=-x;
        }
        int n = (int)(Math.floor(x));
        st += new DecimalFormat("##0").format(n)+"'";
        return(st);
    }

    public String set_dmm(double x) {
        String st="";
        if (x<0) {
            st="-";
            x=-x;
        }
        int n = (int)(Math.floor(x));
        st += new DecimalFormat("#0").format(n)+"°";
        x -= n;
        x = x*60;
        st += new DecimalFormat("00").format(x)+"'";
        return(st);
    }

    public String set_hms(double x) {
        String st="";
        if (x<0) {
            st="-";
            x=-x;
        }
        int n = (int)(Math.floor(x));
        st += new DecimalFormat("#0").format(n)+"h";
        x = (x-n)*60;
        n = (int)(Math.floor(x));
        st += new DecimalFormat("00").format(n)+"m";
        x = (x-n)*60;
        n = (int)(Math.floor(x));
        st += new DecimalFormat("00").format(n)+"s";
        return(st);
    }

    static public void julian(Calendar rightNow) {
        //Calcule Jult à partir de l'heure date locale jour/mois/an/h (non modifiés)
        //ATTENTION HEURES HIVER/ETE ? testé en hiver
        dst = (rightNow.get(Calendar.ZONE_OFFSET))/3600000;
        double h = -dst + rightNow.get(Calendar.HOUR_OF_DAY);
        h +=(rightNow.get(Calendar.MINUTE))/60.0;
        h += (rightNow.get(Calendar.SECOND))/3600.0;

        yyyy = rightNow.get(Calendar.YEAR);
        day = rightNow.get(Calendar.DAY_OF_MONTH);
        mois = 1 + rightNow.get(Calendar.MONTH);

        //Ajoute heure
        double jj = day + (h/24.0);

        //calcul date julienne à 0H00
        if (mois <= 2) {
            yyyy = yyyy - 1;
            mois = mois + 12;
        }

        double b=0;
        if (yyyy >= 1582) {
            double a100 = (yyyy / 100.0);
            double a4 = (a100 / 4.0);
            b = 2 - Math.floor(a100) + Math.floor(a4);
        }
        jdm = b + Math.floor(365.25 * (yyyy + 4716.0)) + Math.floor(30.6001 * (mois + 1)) + jj -1524.5;
    }

    public String sunset() {
        //rang jour année voir
        // http://www.softrun.fr/index.php/bases-scientifiques/heure-de-lever-et-de-coucher-du-soleil
        Calendar rightNow = Calendar.getInstance();
        double jj = rightNow.get(Calendar.DAY_OF_YEAR);

        //Solar mean anomaly (radians)
        double M = tronc360(357.5291 + (0.98560028*jj) ) / ff;

        //equation of center (in minutes ???)
        double C = 1.9148*Math.sin(M) + 0.020*Math.sin(2*M) + 0.0003*Math.sin(3*M);

        double L = tronc360(280 + C + (0.9856*jj));
        double R = -2.466 * Math.sin(2*L/ff) + 0.053*Math.sin(4*L/ff);
        double DECL = Math.asin(0.3978*Math.sin(L/ff));

        //équation-temps
        double EQUAT = 4*(C + R);

        //Calc H0 heures coucher
        double H0 = (Math.sin(-0.83/ff)-Math.sin(DECL)*Math.sin(glob.latit/ff)) / (Math.cos(DECL)*Math.cos(glob.latit/ff) );

        double sunset = 0;

        if (H0 < -1) {
           return "No sunset";
        } else {
            if (H0 > 1) {
                return "No sunrise";
            } else {
                H0 = ff * Math.acos(H0);
                double delt = (12 + H0 / 15) + (EQUAT / 60) - glob.longit / 15;
                sunset = dst + Math.floor(delt) + Math.ceil((delt - Math.floor(delt)) * 60) / 60;
            }
        }
        return "Sunset : "+set_hms(sunset);
    }


    public double tronc360(double in) {
        while (in < 0) in+=360.0;
        while (in >= 360.0) in-=360.0;
        return(in);
    }

    public double tronc180(double in) {
        while (in < -90.0) in+=180.0;
        while (in >= 90.0) in-=180.0;
        return(in);
    }

    public void f_aHso() {
        //Calcule Point vernal en degrés a partir du temps sidéral T
        T=(jdm-2451545.0)/36525.0;
        double pv = 280.46061837 + (360.98564736629*(jdm-2451545.0)) + (0.000387933*T*T) - ((T*T*T)/38710000.0);
        ahso = tronc360(pv);
    }

    public void f_aHvo() {
        //Calcule AHao a partir de mois,an,h,jour

        //geometric mean longitude
        double l = 280.46646 + T * (36000.76983 + .0003032 * T);
        l = tronc360(l);

        //sun's mean anomaly
        double m = 357.52911 + T * (35999.05029 - .0001537 * T);
        m = tronc360(m) / ff;

        //sun equation of center
        double granc = (1.914602 - T * (.004817 + (.000014 * T))) * Math.sin(m);
        granc = granc + Math.sin(2 * m) * (.019993 - .000101 * T);
        granc = granc + .000289 * Math.sin(3 * m);

        //ascending node longitude
        double ohm = 125.04 - 1934.136 * T;
        ohm = tronc360(ohm) / ff;

        //calcule l'obliquite eccliptique epsilon en radians
        obliq = 23.439291111 - T * (.013004166 + T * (.0000001638 - T * .0000005036));
        obliq /= ff;
        double epsil = obliq + (.00256 * Math.cos(ohm)) / ff;

        //sun apparent longitude corrigee
        double delta = l + granc - .00569 - .00478 * Math.sin(ohm);
        //correction J2000
        //delta = delta - .01397 * (an + mois / 12 - 2000);
        delta = tronc360(delta) / ff;

        //tout en radians
        double xx = Math.cos(delta);
        double yy = Math.cos(epsil) * Math.sin(delta);
        double zz = Math.sin(epsil) * Math.sin(delta);

        double alpha = Math.atan(yy / xx) * ff;

        if (xx < 0) alpha = 180 + alpha;

        dec = Math.asin(zz) * ff;
        dec = tronc180(dec);

        //eccentricity of earth
        double e = .016708634 - T * (.000042037 + .0000001267 * T);
        double v = m + granc / ff;
        double r = .9997218 / (1 + e * Math.cos(v));
        demid = .26656388888 / r;

        ahvo = ahso + 360 - alpha;
        ahvo = tronc360(ahvo);
    }


   public void f_aHao() {
       // moon mean anomaly
       double mp = 134.9633964 + T * (477198.8675055 + T * (.0087414 + T / 69699 - T * T / 14712000));
       mp = tronc360(mp);

       //sun mean anomaly
       double m = 357.5291092 + T * (35999.0502909 - T * (.0001536 + T / 24490000));
       m = tronc360(m);

       // distance of moon
       double f = 93.272095 + T * (483202.0175233 + T * (-.0036539 - T / 3526000 + T * T / 863310000));
       f = tronc360(f);

       // moon mean elongation
       double d = 297.8501921 + T * (445267.1114034 - (.0018819 * T) + T * T * (1 / 545868 + T / 113065000));
       d = tronc360(d);

       // moon mean longitude
       double lp = 218.3164477 + T * (481267.88123421 - (.0015786 * T) + T * T * (1 / 538841 - T / 65194000));
       lp = tronc360(lp);

       // coeff for corrections
       double e = 1 - T * (.002516 + T * .0000074);

       // additive arguments
       double a1 = tronc360(119.75 + 131.849 * T); a1 = a1 / ff;
       double a2 = tronc360(53.09 + 479264.29 * T); a2 = a2 / ff;
       double a3 = tronc360(313.45 + 481266.484 * T); a3 = a3 / ff;

       d = d / ff; f = f / ff; m = m / ff; mp = mp / ff;

       double lg = 6288774 * Math.sin(mp);
       lg = lg + 1274027 * Math.sin(2 * d - mp);
       lg = lg + 658314 * Math.sin(2 * d);
       lg = lg + 213618 * Math.sin(2 * mp);
       lg = lg - 185116 * e * Math.sin(m);
       lg = lg - 114332 * Math.sin(2 * f);
       lg = lg + 58793 * Math.sin(2 * (d - mp));
       lg = lg + 57066 * e * Math.sin(2 * d - mp - m);
       lg = lg + 53322 * Math.sin(2 * d + mp);
       lg = lg + 45758 * e * Math.sin(2 * d - m);
       lg = lg - 40923 * e * Math.sin(m - mp);
       lg = lg - 34720 * Math.sin(d);
       lg = lg - 30383 * e * Math.sin(m + mp);
       lg = lg + 15327 * Math.sin(2 * (d - f));
       lg = lg - 12528 * Math.sin(mp + 2 * f);
       lg = lg + 10980 * Math.sin(-2 * f + mp);
       lg = lg + 10675 * Math.sin(4 * d - mp);
       lg = lg + 10034 * Math.sin(3 * mp);
       lg = lg + 8548 * Math.sin(4 * d - 2 * mp);
       lg = lg - 7888 * e * Math.sin(m - mp + 2 * d);
       lg = lg - 6766 * e * Math.sin(m + 2 * d);
       lg = lg - 5163 * Math.sin(-mp + d);
       lg = lg + 4987 * e * Math.sin(m + d);
       lg = lg + 4036 * e * Math.sin(mp - m + 2 * d);
       lg = lg + 3994 * Math.sin(2 * d + 2 * mp);
       lg = lg + 3861 * Math.sin(4 * d);
       lg = lg + 3665 * Math.sin(2 * d - 3 * mp);
       lg = lg + -2689 * e * Math.sin(-2 * mp + m);
       lg = lg - 2602 * Math.sin(-mp + 2 * f + 2 * d);
       lg = lg + 2390 * e * Math.sin(2 * d - m - 2 * mp);
       lg = lg - 2348 * Math.sin(d + mp);
       lg = lg + 2236 * e * e * Math.sin(2 * d - 2 * m);
       lg = lg - 2120 * e * Math.sin(m + 2 * mp);
       lg = lg - 2069 * e * e * Math.sin(2 * m);
       lg = lg + 2048 * e * e * Math.sin(-mp - 2 * m + 2 * d);
       lg = lg - 1773 * Math.sin(mp + 2 * d - 2 * f);
       lg = lg - 1595 * Math.sin(2 * f + 2 * d);
       lg = lg + 1215 * e * Math.sin(4 * d - m - mp);
       lg = lg - 1110 * Math.sin(2 * f + 2 * mp);
       lg = lg - 892 * Math.sin(-mp + 3 * d);
       lg = lg - 810 * e * Math.sin(m + mp + 2 * d);
       lg = lg + 759 * e * Math.sin(4 * d - m - 2 * mp);
       lg = lg - 713 * e * e * Math.sin(-mp + 2 * m);
       lg = lg - 700 * e * e * Math.sin(2 * d + 2 * m - mp);
       lg = lg + 691 * e * Math.sin(m - 2 * mp + 2 * d);

       lg = lg + 3958 * Math.sin(a1);
       lg = lg + 1962 * Math.sin(lp / ff - f);
       lg = lg + 318 * Math.sin(a2);
       lg = tronc360(lp + .000001 * lg);

       double r = -20905355 * Math.cos(mp);
       r = r - 3699111 * Math.cos(2 * d - mp);
       r = r - 2955968 * Math.cos(2 * d);
       r = r - 569925 * Math.cos(2 * mp);
       r = r + 48888 * e * Math.cos(m);
       r = r - 3149 * Math.cos(2 * f);
       r = r + 246158 * Math.cos(2 * d - 2 * mp);
       r = r - 152138 * e * Math.cos(2 * d - mp - m);
       r = r - 170733 * Math.cos(2 * d + mp);
       r = r - 204586 * e * Math.cos(2 * d - m);
       r = r - 129620 * e * Math.cos(m - mp);
       r = r + 108743 * Math.cos(d);
       r = r + 104755 * e * Math.cos(m + mp);
       r = r + 10321 * Math.cos(2 * d - 2 * f);
       r = r + 79661 * Math.cos(-2 * f + mp);
       r = r - 34782 * Math.cos(4 * d - mp);
       r = r - 23210 * Math.cos(3 * mp);
       r = r - 21636 * Math.cos(4 * d - 2 * mp);
       r = r + 24208 * e * Math.cos(m - mp + 2 * d);
       r = r + 30824 * e * Math.cos(m + 2 * d);
       r = r - 8379 * Math.cos(-mp + d);
       r = r - 16675 * e * Math.cos(m + d);
       r = r - 12831 * e * Math.cos(mp - m + 2 * d);
       r = r - 10445 * Math.cos(2 * d + 2 * mp);
       r = r - 11650 * Math.cos(4 * d);
       r = r + 14403 * Math.cos(2 * d - 3 * mp);
       r = r - 7003 * e * Math.cos(-2 * mp + m);
       r = r + 10056 * e * Math.cos(2 * d - m - 2 * mp);
       r = r + 6322 * Math.cos(d + mp);
       r = r - 9884 * e * e * Math.cos(2 * d - 2 * m);
       r = r + 5751 * e * Math.cos(m + 2 * mp);
       r = r - 4950 * e * e * Math.cos(-mp - 2 * m + 2 * d);
       r = r + 4130 * Math.cos(mp + 2 * d - 2 * f);
       r = r - 3958 * e * Math.cos(4 * d - m - mp);
       r = r + 3258 * Math.cos(-mp + 3 * d);
       r = r + 2616 * e * Math.cos(m + mp + 2 * d);
       r = r - 1897 * e * Math.cos(4 * d - m - 2 * mp);
       r = r - 2117 * e * e * Math.cos(-mp + 2 * m);
       r = r + 2354 * e * e * Math.cos(2 * d + 2 * m - mp);
       r = r - 1423 * Math.cos(4 * d + mp);
       r = r - 1117 * Math.cos(4 * mp);
       r = r - 1571 * e * Math.cos(4 * d - m);
       r = r - 1739 * Math.cos(d - 2 * mp);
       r = r - 4421 * Math.cos(2 * mp - 2 * f);
       r = r + 1165 * e * e * Math.cos(2 * m + mp);
       r = r + 8752 * Math.cos(2 * d - mp - 2 * f);

       double delta = 385000.56 + r / 1000;

       double la = 5128122 * Math.sin(f);
       la = la + 280602 * Math.sin(mp + f);
       la = la + 277693 * Math.sin(mp - f);
       la = la + 173237 * Math.sin(2 * d - f);
       la = la + 55413 * Math.sin(2 * d + f - mp);
       la = la + 46271 * Math.sin(2 * d - f - mp);
       la = la + 32573 * Math.sin(2 * d + f);
       la = la + 17198 * Math.sin(2 * mp + f);
       la = la + 9266 * Math.sin(2 * d + mp - f);
       la = la + 8822 * Math.sin(2 * mp - f);
       la = la + 8216 * e * Math.sin(2 * d - m - f);
       la = la + 4324 * Math.sin(2 * d - f - 2 * mp);
       la = la + 4200 * Math.sin(2 * d + f + mp);
       la = la + 3359 * e * Math.sin(f - m - 2 * d);
       la = la + 2463 * e * Math.sin(2 * d + f - m - mp);
       la = la + 2211 * e * Math.sin(2 * d + f - m);
       la = la + 2065 * e * Math.sin(2 * d - f - m - mp);
       la = la + 1870 * e * Math.sin(f - m + mp);
       la = la + 1828 * Math.sin(4 * d - mp - f);
       la = la - 1794 * e * Math.sin(m + f);
       la = la - 1749 * Math.sin(3 * f);
       la = la - 1565 * e * Math.sin(-mp + m + f);
       la = la - 1491 * Math.sin(d + f);
       la = la - 1475 * e * Math.sin(f + m + mp);
       la = la - 1410 * e * Math.sin(-f + m + mp);
       la = la - 1344 * e * Math.sin(-f + m);
       la = la - 1335 * Math.sin(d - f);
       la = la + 1107 * Math.sin(f + 3 * mp);
       la = la + 1021 * Math.sin(4 * d - f);
       la = la + 833 * Math.sin(4 * d + f - mp);
       la = la + 777 * Math.sin(mp - 3 * f);
       la = la + 671 * Math.sin(f + 4 * d - 2 * mp);
       la = la + 607 * Math.sin(2 * d - 3 * f);
       la = la + 596 * Math.sin(2 * d + 2 * mp - f);
       la = la + 491 * e * Math.sin(2 * d + mp - m - f);
       la = la - 451 * Math.sin(2 * d - 2 * mp + f);
       la = la + 439 * Math.sin(3 * mp - f);
       la = la + 422 * Math.sin(2 * d + 2 * mp + f);
       la = la + 421 * Math.sin(2 * d - 3 * mp - f);
       la = la - 366 * e * Math.sin(m + 2 * d - mp + f);
       la = la - 351 * e * Math.sin(2 * d + m + f);
       la = la + 331 * Math.sin(4 * d + f);
       la = la + 315 * e * Math.sin(2 * d - m + mp + f);
       la = la + 302 * e * e * Math.sin(2 * d - 2 * m - f);
       la = la - 283 * Math.sin(mp + 3 * f);
       la = la - 229 * e * Math.sin(2 * d + m + mp - f);
       la = la + 223 * e * Math.sin(d + m - f);
       la = la + 223 * e * Math.sin(d + m + f);
       la = la - 220 * e * Math.sin(m - 2 * mp - f);
       la = la - 220 * e * Math.sin(2 * d + m - mp - f);
       la = la - 185 * Math.sin(d + mp + f);
       la = la + 181 * e * Math.sin(2 * d - m - 2 * mp - f);
       la = la - 177 * e * Math.sin(m + 2 * mp + f);
       la = la + 176 * Math.sin(4 * d - 2 * mp - f);
       la = la + 166 * e * Math.sin(4 * d - m - mp - f);
       la = la - 164 * Math.sin(d + mp - f);
       la = la + 132 * Math.sin(4 * d + mp - f);
       la = la - 119 * Math.sin(d - mp - f);
       la = la - 2235 * Math.sin(lp / ff);
       la = la + 382 * Math.sin(a3);
       la = la + 175 * Math.sin(a1 - f);
       la = la + 175 * Math.sin(a1 + f);
       la = la + 127 * Math.sin(-mp + lp / ff);
       la = la - 115 * Math.sin(mp + lp / ff);
       la = tronc360(.000001 * la);

       double paral = ff * Math.asin(6378.14 / delta);
       demid = .272481 * paral;

       //longitude moon ascending node
       double ohm = 125.04452 + T * (-1934.136261 + .0020708 * T);
       ohm = tronc360(ohm) / ff;
       //sun mean longitude
       double l = 280.4665 + T * 36000.76983;
       l = tronc360(l) / ff;

       //effet de la nutation
       double dpsi = -17.2 * Math.sin(ohm) - 1.32 * Math.sin(2 * l) - .23 * Math.sin(2 * lp / ff) + .21 * Math.sin(2 * ohm);
       double deps = 9.2 * Math.cos(ohm) + .57 * Math.cos(2 * l) + .1 * Math.cos(2 * lp / ff) - .09 * Math.cos(2 * ohm);

       //correction nutation
       //calcule l'obliquite eccliptique epsilon en radians
       obliq = 23.439291111 - T * (.013004166 + T * (.0000001638 - T * .0000005036));
       obliq /= ff;
       double epsil = obliq + deps / (3600 * ff);
       lg = lg + dpsi / 3600;

       lg = lg / ff; la = la / ff;
       dec = Math.cos(la) * Math.sin(lg) * Math.sin(epsil) + Math.sin(la) * Math.cos(epsil);
       dec = Math.asin(dec);

       double ar = (Math.cos(la) * Math.cos(lg)) / Math.cos(dec);
       ar = ff * Math.acos(ar);
       dec = ff * dec;

       double k = -Math.sin(epsil) * Math.sin(la) + Math.cos(la) * Math.sin(lg) * Math.cos(epsil);
       if (k <= 0.0) ar = 360 - ar;

       ahvo = tronc360(ahso - ar + 360);

       age = 285 + 445267.14 * T;
       age = age + 6.3 * Math.sin((296.1 + 477198.7 * T) / ff);
       age = tronc360(age) * .082;
   }

 /*   public float earth() {
        //elementes des planetes
        double el0a[] = new double[0], el0f[], el0p[];
        double el1a[], el1f[], el1p[];
        double el2a[], el2f[], el2p[];
        double el3a[], el3f[], el3p[];
        double el4a[], el4f[], el4p[];
        double el5a[], el5f[], el5p[];
        double eb0a[], eb0f[], eb0p[];
        double er0a[], er0f[], er0p[];
        double eb1a[], eb1f[], eb1p[];
        double er1a[], er1f[], er1p[];
        double er2a[], er2f[], er2p[];
        double er3a[], er3f[], er3p[];
        double er4a[], er4f[], er4p[];

        el0a[1] = 175347046; el0f[1] = 0; el0p[1] = 0;
        el0a[2] = 3341656; el0f[2] = 4.6692568; el0p[2] = 6283.07585;
        el0a[3] = 34894; el0f[3] = 4.6261; el0p[3] = 12566.1517;
        el0a[4] = 3497; el0f[4] = 2.7441; el0p[4] = 5753.3849;
        el0a[5] = 3418; el0f[5] = 2.8289; el0p[5] = 3.5231;
        el0a[6] = 3136; el0f[6] = 3.6277; el0p[6] = 77713.7715;
        el0a[7] = 2676; el0f[7] = 4.4181; el0p[7] = 7860.4194;
        el0a[8] = 2343; el0f[8] = 6.1352; el0p[8] = 3930.2097;
        el0a[9] = 1324; el0f[9] = .7425; el0p[9] = 11506.7698;
        el0a[10] = 1273; el0f[10] = 2.0371; el0p[10] = 529.691;
        el0a[11] = 1199; el0f[11] = 1.1096; el0p[11] = 1577.3435;
        el0a[12] = 990; el0f[12] = 5.233; el0p[12] = 5884.927;
        el0a[13] = 902; el0f[13] = 2.045; el0p[13] = 26.298;
        el0a[14] = 857; el0f[14] = 3.508; el0p[14] = 398.149;
        el0a[15] = 780; el0f[15] = 1.179; el0p[15] = 5223.694;
        el0a[16] = 753; el0f[16] = 2.533; el0p[16] = 5507.553;
        el0a[17] = 505; el0f[17] = 4.583; el0p[17] = 18849.228;
        el0a[18] = 492; el0f[18] = 4.205; el0p[18] = 775.523;
        el0a[19] = 357; el0f[19] = 2.92; el0p[19] = .067;
        el0a[20] = 317; el0f[20] = 5.849; el0p[20] = 11790.629;
        el0a[21] = 284; el0f[21] = 1.899; el0p[21] = 796.298;
        el0a[22] = 271; el0f[22] = .315; el0p[22] = 10997.079;
        el0a[23] = 243; el0f[23] = .345; el0p[23] = 5486.778;
        el0a[24] = 206; el0f[24] = 4.806; el0p[24] = 2544.314;
        el0a[25] = 205; el0f[25] = 1.869; el0p[25] = 5573.143;
        el0a[26] = 202; el0f[26] = 2.458; el0p[26] = 6069.777;
        el0a[27] = 156; el0f[27] = .833; el0p[27] = 213.299;
        el0a[28] = 132; el0f[28] = 3.411; el0p[28] = 2942.463;
        el0a[29] = 126; el0f[29] = 1.083; el0p[29] = 20.775;
        el0a[30] = 115; el0f[30] = .645; el0p[30] = .98;
        el0a[31] = 103; el0f[31] = .636; el0p[31] = 4694.003;
        el0a[32] = 102; el0f[32] = .976; el0p[32] = 15720.839;
        el0a[33] = 102; el0f[33] = 4.267; el0p[33] = 7.114;
        el0a[34] = 99; el0f[34] = 6.21; el0p[34] = 2146.17;
        el0a[35] = 98; el0f[35] = .68; el0p[35] = 155.42;
        el0a[36] = 86; el0f[36] = 5.98; el0p[36] = 161000.69;
        el0a[37] = 0; el0f[37] = 0; el0p[37] = 0;

        el1a[1] = 628331966747.0; el1f[1] = 0; el1p[1] = 0;
        el1a[2] = 206059; el1f[2] = 2.678235; el1p[2] = 6283.07585;
        el1a[3] = 4303; el1f[3] = 2.6351; el1p[3] = 12566.1517;
        el1a[4] = 425; el1f[4] = 1.59; el1p[4] = 3.523;
        el1a[5] = 119; el1f[5] = 5.796; el1p[5] = 26.298;
        el1a[6] = 109; el1f[6] = 2.966; el1p[6] = 1577.344;
        el1a[7] = 93; el1f[7] = 2.59; el1p[7] = 18849.23;
        el1a[8] = 72; el1f[8] = 1.14; el1p[8] = 529.69;
        el1a[9] = 68; el1f[9] = 1.87; el1p[9] = 398.15;
        el1a[10] = 67; el1f[10] = 4.41; el1p[10] = 5507.55;
        el1a[11] = 59; el1f[11] = 2.89; el1p[11] = 5223.69;
        el1a[12] = 0; el1f[12] = 0; el1p[12] = 0;

        el2a[1] = 52919; el2f[1] = 0; el2p[1] = 0;
        el2a[2] = 8720; el2f[2] = 1.0721; el2p[2] = 6283.0758;
        el2a[3] = 309; el2f[3] = .867; el2p[3] = 12566.152;
        el2a[4] = 27; el2f[4] = .05; el2p[4] = 3.52;
        el2a[5] = 0; el2f[5] = 0; el2p[5] = 0;

        el3a[1] = 289; el3f[1] = 5.844; el3p[1] = 6283.076;
        el3a[2] = 35; el3f[2] = 0; el3p[2] = 0;
        el3a[3] = 17; el3f[3] = 5.49; el3p[3] = 12566.15;
        el3a[4] = 3; el3f[4] = 5.2; el3p[4] = 155.42;
        el3a[5] = 1; el3f[5] = 4.72; el3p[5] = 3.52;
        el3a[6] = 1; el3f[6] = 5.3; el3p[6] = 18849.23;
        el3a[7] = 1; el3f[7] = 5.97; el3p[7] = 242.73;

        el4a[1] = 114; el4f[1] = 3.142; el4p[1] = 0;
        el4a[2] = 8; el4f[2] = 4.13; el4p[2] = 6283.08;
        el4a[3] = 1; el4f[3] = 3.84; el4p[3] = 12566.15;

        el5a[1] = 1; el5f[1] = 0; el5p[1] = 0;

        eb0a[1] = 280; eb0f[1] = 3.199; eb0p[1] = 0;
        eb0a[2] = 102; eb0f[2] = 5.422; eb0p[2] = 0;
        eb0a[3] = 80; eb0f[3] = 3.88; eb0p[3] = 0;
        eb0a[4] = 44; eb0f[4] = 3.7; eb0p[4] = 0;
        eb0a[5] = 32; eb0f[5] = 4; eb0p[5] = 0;

        eb1a[1] = 9; eb1f[1] = 3.9; eb1p[1] = 5507.55;
        eb1a[2] = 6; eb1f[2] = 1.73; eb1p[2] = 5223.69;

        er0a[1] = 100013989; er0f[1] = 0; er0p[1] = 0;
        er0a[2] = 1670700; er0f[2] = 3.0984635; er0p[2] = 6283.07585;
        er0a[3] = 13956; er0f[3] = 3.05525; er0p[3] = 12566.1517;
        er0a[4] = 3084; er0f[4] = 5.1985; er0p[4] = 77713.7715;
        er0a[5] = 1628; er0f[5] = 1.1739; er0p[5] = 5753.3849;
        er0a[6] = 1576; er0f[6] = 2.8469; er0p[6] = 7860.4194;
        er0a[7] = 925; er0f[7] = 5.453; er0p[7] = 11506.77;
        er0a[8] = 542; er0f[8] = 4.564; er0p[8] = 3930.21;
        er0a[9] = 472; er0f[9] = 3.661; er0p[9] = 5884.927;
        er0a[10] = 346; er0f[10] = .964; er0p[10] = 5507.553;
        er0a[11] = 329; er0f[11] = 5.9; er0p[11] = 5223.694;
        er0a[12] = 307; er0f[12] = .299; er0p[12] = 5573.143;
        er0a[13] = 243; er0f[13] = 4.273; er0p[13] = 11790.629;
        er0a[14] = 212; er0f[14] = 5.847; er0p[14] = 1577.344;
        er0a[15] = 186; er0f[15] = 5.022; er0p[15] = 10977.079;
        er0a[16] = 175; er0f[16] = 3.012; er0p[16] = 18849.228;
        er0a[17] = 110; er0f[17] = 5.055; er0p[17] = 5486.778;
        er0a[18] = 98; er0f[18] = .89; er0p[18] = 6069.78;
        er0a[19] = 86; er0f[19] = 5.69; er0p[19] = 15720.84;
        er0a[20] = 0; er0f[20] = 0; er0p[20] = 0;

        er1a[1] = 103019; er1f[1] = 1.10749; er1p[1] = 6283.07585;
        er1a[2] = 1721; er1f[2] = 1.0644; er1p[2] = 12566.1517;
        er1a[3] = 702; er1f[3] = 3.142; er1p[3] = 0;
        er1a[4] = 32; er1f[4] = 1.02; er1p[4] = 18849.23;
        er1a[5] = 31; er1f[5] = 2.84; er1p[5] = 5507.55;
        er1a[6] = 25; er1f[6] = 1.32; er1p[6] = 5223.69;
        er1a[7] = 0; er1f[7] = 0; er1p[7] = 0;

        er2a[1] = 4359; er2f[1] = 5.7846; er2p[1] = 6283.0758;
        er2a[2] = 124; er2f[2] = 5.579; er2p[2] = 12566.152;
        er2a[3] = 12; er2f[3] = 3.14; er2p[3] = 0;
        er2a[4] = 9; er2f[4] = 3.63; er2p[4] = 77713.77;
        er2a[5] = 6; er2f[5] = 1.87; er2p[5] = 5573.14;
        er2a[6] = 3; er2f[6] = 5.47; er2p[6] = 18849.23;

        er3a[1] = 145; er3f[1] = 4.273; er3p[1] = 6283.076;
        er3a[2] = 7; er3f[2] = 3.92; er3p[2] = 12566.15;

        er4a[1] = 4; er4f[1] = 2.56; er4p[1] = 6283.08;
        return (0);
    }  */

}