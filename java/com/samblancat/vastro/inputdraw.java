package com.samblancat.vastro;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;

public class inputdraw extends View {
    public Paint paint;
    public CountDownTimer cTimer = null;
    public Double ddx, ddy;
    public int incred=0;

    public inputdraw(Context context) {
        super(context);

        ddx=0.0;
        ddy=0.0;

        setKeepScreenOn(true);
    }

    //Dessin de l'entrée en degrés minutes
    @Override
    protected void onDraw(Canvas canvas) {
        this.buildDrawingCache();

        int wx = getResources().getDisplayMetrics().widthPixels;
        int wy = getResources().getDisplayMetrics().heightPixels;

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(3);

        paint.setColor(Color.LTGRAY);
        paint.setTextSize(32);
        canvas.drawText(glob.legende, (int)(wx*0.24), (int)(wy*0.2), paint);

        paint.setTextSize(72);
        if (glob.sign > 0)
            canvas.drawText("+",  28 , wy/2+25, paint);
        else
            canvas.drawText("-",  28 , wy/2+25, paint);

        int x0 = 65;
        int incx = 52;

        for (int n=0; n<4; n++) {
            paint.setColor(Color.LTGRAY);
//            canvas.drawRect(x0, wy/2 - 60, 50 + x0, wy/2 + 40, paint);
//            paint.setColor(Color.BLACK);
            paint.setTextSize(74);
            canvas.drawText(Integer.toString(glob.car[n]),  x0 , wy/2+20, paint);
            x0 += incx;
            //Affiche la virgule
            if (n==1) {
                paint.setColor(Color.WHITE);
                x0-=10;
                canvas.drawText("°",  x0, (int)(wy/2), paint);
                x0+=20;
            }
        }
        paint.setColor(Color.WHITE);
        canvas.drawText(glob.unit,  x0, (int)(wy/2) , paint);

        //Bouton vert en bas
        Rect rectangle = new Rect((int)(wx*0.22), (int)(wy*0.7), (int)(wx*0.78), (int)(wy*0.86));
        // create the Paint and set its color
        paint = new Paint();
        paint.setColor(Color.LTGRAY);
        canvas.drawRect(rectangle, paint);
        paint.setColor(Color.BLACK);
        paint.setTextSize(38);
        canvas.drawText("Entrer", (int)(wx*0.38), (int)(wy*0.82), paint);
    }

    public void updateData(double x1, double y1, double x2, double y2) {
        ddx=x2-x1;
        ddy=y1-y2;
        int dcx = (int) ((x1 + x2) / 2);

        if (incred == 0) {

            int x0 = 65;
            int incx = 52;

            if ((dcx > 1) && (dcx < x0) && (Math.abs((y1 - y2)) > 75)) {
                // CHAR + 1
                if ( (ddy > 45)||(ddy < -45) ) {
                    glob.sign = -glob.sign;
                    incred = 1;
                    cTimer = new CountDownTimer(100, 50) {
                        @Override
                        public void onTick(long millisUntilFinished) { }
                        public void onFinish() {
                            incred = 0;
                        }
                    }.start();
                }
            }

            for (int n=0; n<4; n++) {
                int lgau = x0;
                int ldroit = x0 + incx;

                if ((dcx > lgau) && (dcx < ldroit) && (Math.abs((y1 - y2)) > 75)) {
                    // CHAR + 1
                    if (ddy > 45) {
                        glob.car[n]++;
                        if (glob.car[n] > 9) glob.car[n] = 0;
                        incred = 1;
                        cTimer = new CountDownTimer(100, 50) {
                            @Override
                            public void onTick(long millisUntilFinished) { }
                            public void onFinish() {
                                incred = 0;
                            }
                        }.start();
                    }
                    // CHAR - 1
                    if (ddy < -45) {
                        glob.car[n]--;
                        if (glob.car[n] < 0) glob.car[n] = 9;
                        incred = 1;
                        cTimer = new CountDownTimer(100, 50) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                            }
                            public void onFinish() {
                                incred = 0;
                            }
                        }.start();
                    }
                }
                x0 +=  incx;
            }
            invalidate();
        }
    }
}
