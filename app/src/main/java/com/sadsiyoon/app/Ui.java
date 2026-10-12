package com.sadsiyoon.app;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public final class Ui {
    public static final int BG=Color.rgb(8,19,28), PANEL=Color.rgb(17,35,48);
    public static final int PANEL2=Color.rgb(27,53,64), MINT=Color.rgb(85,230,191);
    public static final int WHITE=Color.rgb(242,252,249), MUTED=Color.rgb(160,183,187);
    private Ui(){}
    public static int dp(Context c,float n){return Math.round(c.getResources().getDisplayMetrics().density*n);}
    public static GradientDrawable background(Context c,int color,int radius){
        GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(c,radius));return d;
    }
    public static GradientDrawable outlined(Context c,int color,int radius,int border){
        GradientDrawable d=background(c,color,radius);d.setStroke(dp(c,1),border);return d;
    }
    public static LinearLayout column(Context c){
        LinearLayout l=new LinearLayout(c);l.setOrientation(LinearLayout.VERTICAL);l.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);return l;
    }
    public static LinearLayout row(Context c){
        LinearLayout l=new LinearLayout(c);l.setOrientation(LinearLayout.HORIZONTAL);l.setGravity(Gravity.CENTER_VERTICAL);l.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);return l;
    }
    public static TextView text(Context c,String s,int sp,int color,boolean bold){
        TextView t=new TextView(c);t.setText(s);t.setTextSize(sp);t.setTextColor(color);t.setGravity(Gravity.CENTER_VERTICAL);
        t.setTextDirection(View.TEXT_DIRECTION_FIRST_STRONG);
        if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        return t;
    }
    public static TextView action(Context c,String s,boolean primary){
        TextView v=text(c,s,13,primary?BG:WHITE,true);v.setGravity(Gravity.CENTER);
        v.setPadding(dp(c,13),dp(c,11),dp(c,13),dp(c,11));
        v.setBackground(background(c,primary?MINT:PANEL2,12));v.setClickable(true);v.setFocusable(true);return v;
    }
    public static LinearLayout.LayoutParams boxParams(Context c,int h,int top){
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,h<0?h:dp(c,h));p.topMargin=dp(c,top);return p;
    }
    public static void pad(Context c,View v,int horizontal,int vertical){
        v.setPadding(dp(c,horizontal),dp(c,vertical),dp(c,horizontal),dp(c,vertical));
    }
    public static TextView spacer(Context c,int height){
        TextView t=text(c,"",1,WHITE,false);t.setHeight(dp(c,height));return t;
    }
}
