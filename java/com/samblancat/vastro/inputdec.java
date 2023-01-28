package com.samblancat.vastro;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;

//Pour entrer une valeur décimale.décimale avec signe +/-
public class inputdec extends AppCompatActivity {
    Context mContext;
    public double x1,x2,y1,y2;
    public double wx , wy, cx, cy;
    private inputdrawdec inputview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext=this;

        wx = getResources().getDisplayMetrics().widthPixels;
        wy = getResources().getDisplayMetrics().heightPixels;

        cx = wx / 2;
        cy = wy / 2;

        glob.car = new int[5];
        //Affiche le float inp en degrès/minutes
        if (glob.inp < 0) { glob.sign=-1; glob.inp=-glob.inp; } else glob.sign=1;
        //dizaines
        int nn=(int)(Math.floor(glob.inp/10));
        glob.car[0] = nn;
        glob.inp -= 10*nn;
        //unités
        nn=(int)(Math.floor(glob.inp));
        glob.car[1] = nn;
        glob.inp -= nn;
        //dizièmes
        glob.inp *= 10;
        nn=(int)(Math.floor(glob.inp));
        glob.car[2] = nn;
        //que 3 chiffres
        glob.car[3] = 0;

        inputview = new inputdrawdec(this);
        setContentView(inputview);
    }

    //Detection du swipe sur gauche ou autre .... pour finish ?
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        //Detection du swipe sur gauche ou autre .... pour finish ?
            switch(ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    x1 = ev.getX();
                    y1 = ev.getY();
                    x2=x1;
                    y2=y1;
                    break;
                case MotionEvent.ACTION_MOVE:
                case MotionEvent.ACTION_UP:
                    x2 = ev.getX();
                    y2 = ev.getY();
                    //test si FIN
                    if ( (Math.abs(x1-x2)>180) && (Math.abs(y1-y2)<50) ) finish();

                    //Test appui 'OK' en bas écran
                    if ((y1>wy*0.7)&&(y2>wy*0.7)&&(x1>cx-40)&&(x2<cx+40)) {
                        //valeur de retour en .glob (de DMS à float)
                        glob.inp = glob.sign * (float) ((10*glob.car[0])+(glob.car[1])+(0.1*glob.car[2]));
                        finish();
                    }
                    inputview.updateData(x1, y1, x2, y2);
                    break;
            }

            // Dessine Mot à chercher & test si change ?
            //       Log.d("x1x2y1y2", String.valueOf(x1)+" "+String.valueOf(y1)+" "+String.valueOf(x2)+" "+String.valueOf(y2));
            //       gameview.updateData(x1, y1, x2, y2);
            return false;   //super.dispatchTouchEvent(ev);
    }


    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }
}