package com.sadsiyoon.app;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.util.LruCache;
import java.net.HttpURLConnection;
import java.net.URL;

public final class Ui {
 public static final int BG=Color.rgb(7,22,33), PANEL=Color.rgb(16,37,48), PANEL2=Color.rgb(25,56,65);
 public static final int MINT=Color.rgb(85,230,191), GOLD=Color.rgb(217,182,108);
 public static final int WHITE=Color.rgb(242,249,246), MUTED=Color.rgb(160,185,189), RED=Color.rgb(255,160,155);
 private static final LruCache<String,Bitmap> imageCache=new LruCache<>(18);
 private Ui(){}
 public static int dp(Context c,float x){return Math.round(c.getResources().getDisplayMetrics().density*x);}
 public static GradientDrawable round(Context c,int bg,int r){
    GradientDrawable d=new GradientDrawable();d.setColor(bg);d.setCornerRadius(dp(c,r));return d;
 }
 public static GradientDrawable stroke(Context c,int bg,int r,int line){
    GradientDrawable d=round(c,bg,r);d.setStroke(dp(c,1),line);return d;
 }
 public static GradientDrawable gradient(Context c,int a,int b,int r){
    GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{a,b});d.setCornerRadius(dp(c,r));return d;
 }
 public static LinearLayout col(Context c){LinearLayout l=new LinearLayout(c);l.setOrientation(1);l.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);return l;}
 public static LinearLayout row(Context c){LinearLayout l=new LinearLayout(c);l.setOrientation(0);l.setGravity(Gravity.CENTER_VERTICAL);l.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);return l;}
 public static void pad(Context c,View v,int h,int vpad){v.setPadding(dp(c,h),dp(c,vpad),dp(c,h),dp(c,vpad));}
 public static TextView txt(Context c,String s,int sp,int color,boolean bold){
    TextView v=new TextView(c);v.setText(s);v.setTextSize(sp);v.setTextColor(color);v.setGravity(Gravity.CENTER_VERTICAL);v.setTextDirection(View.TEXT_DIRECTION_FIRST_STRONG);
    if(bold)v.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return v;
 }
 public static TextView btn(Context c,String s,boolean main){
    TextView t=txt(c,s,14,main?BG:WHITE,true);t.setGravity(Gravity.CENTER);
    pad(c,t,12,10);t.setBackground(round(c,main?MINT:PANEL2,12));
    t.setClickable(true);t.setFocusable(true);return t;
 }
 public static LinearLayout card(Context c){
    LinearLayout l=col(c);pad(c,l,15,15);l.setBackground(stroke(c,PANEL,18,Color.rgb(32,77,78)));return l;
 }
 public static View gap(Context c,int h){View v=new View(c);v.setLayoutParams(new LinearLayout.LayoutParams(1,dp(c,h)));return v;}
 public static LinearLayout.LayoutParams lp(Context c,int w,int h){
    return new LinearLayout.LayoutParams(w<0?w:dp(c,w),h<0?h:dp(c,h));
 }
 public static LinearLayout.LayoutParams top(Context c,int w,int h,int margin){
    LinearLayout.LayoutParams p=lp(c,w,h);p.topMargin=dp(c,margin);return p;
 }
 public static ImageView logo(Context c,int dpSize,boolean full){
    ImageView image=new ImageView(c);
    image.setImageResource(full?R.drawable.logo_full:R.drawable.logo_icon);
    image.setScaleType(ImageView.ScaleType.FIT_CENTER);
    image.setLayoutParams(lp(c,dpSize,dpSize));
    return image;
 }
 public static View portrait(Context c,String url,String name,int size){
    android.widget.FrameLayout frame=new android.widget.FrameLayout(c);
    frame.setBackground(round(c,PANEL2,15));frame.setClipToOutline(true);
    TextView initial=txt(c,name.isEmpty()?"س":name.substring(0,1),28,GOLD,true);initial.setGravity(Gravity.CENTER);
    frame.addView(initial,new android.widget.FrameLayout.LayoutParams(-1,-1));
    if(url!=null&&url.startsWith("https://")){
      ImageView image=new ImageView(c);image.setScaleType(ImageView.ScaleType.CENTER_CROP);
      frame.addView(image,new android.widget.FrameLayout.LayoutParams(-1,-1));
      Bitmap cached=imageCache.get(url);
      if(cached!=null)image.setImageBitmap(cached);
      else new Thread(()->{
        HttpURLConnection connection=null;
        try{
          connection=(HttpURLConnection)new URL(url).openConnection();
          connection.setConnectTimeout(4500);connection.setReadTimeout(4500);connection.setInstanceFollowRedirects(true);
          // Render remote portraits as bounded thumbnails to avoid storing full-size
          // 1200px+ source images for every teacher in the in-memory card cache.
          BitmapFactory.Options opts=new BitmapFactory.Options();
          opts.inSampleSize=4;
          Bitmap decoded=BitmapFactory.decodeStream(connection.getInputStream(),null,opts);
          if(decoded!=null){imageCache.put(url,decoded);image.post(()->{if(image.isAttachedToWindow())image.setImageBitmap(decoded);});}
        }catch(Exception ignored){}finally{if(connection!=null)connection.disconnect();}
      }).start();
    }
    return frame;
 }
}
