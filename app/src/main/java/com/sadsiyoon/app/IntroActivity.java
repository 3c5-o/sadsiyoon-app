package com.sadsiyoon.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class IntroActivity extends AppCompatActivity {
    private boolean navigating=false;
    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        getWindow().setStatusBarColor(Ui.BG);getWindow().setNavigationBarColor(Ui.BG);
        SharedPreferences prefs=getSharedPreferences("sadsiyoon_local_v1",MODE_PRIVATE);
        FrameLayout screen=new FrameLayout(this);
        screen.setBackground(Ui.gradient(this,0xff0e2633,Ui.BG,0));
        LinearLayout body=Ui.col(this);body.setGravity(Gravity.CENTER);Ui.pad(this,body,28,20);
        ImageView image=Ui.logo(this,220,true);body.addView(image);
        TextView slogan=Ui.txt(this,"منصة طلاب السادس الإعدادي",20,Ui.WHITE,true);
        slogan.setGravity(Gravity.CENTER);body.addView(slogan,Ui.top(this,-1,44,8));
        TextView desc=Ui.txt(this,"طريقك نحو المستقبل يبدأ من هنا",14,Ui.MUTED,false);
        desc.setGravity(Gravity.CENTER);body.addView(desc,Ui.top(this,-1,30,6));
        TextView line=Ui.txt(this,"──────  ◆  ──────",17,Ui.GOLD,true);
        line.setGravity(Gravity.CENTER);body.addView(line,Ui.top(this,-1,35,24));
        TextView action=Ui.btn(this,prefs.getBoolean("welcome_done",false)?"متابعة":"ابدأ رحلتك الآن",true);
        body.addView(action,Ui.top(this,-1,54,32));
        action.setOnClickListener(v->{prefs.edit().putBoolean("welcome_done",true).apply();enter();});
        TextView footer=Ui.txt(this,"سادسيون  •  تجربة تعليمية بهوية مستقلة",11,Ui.MUTED,false);
        footer.setGravity(Gravity.CENTER);body.addView(footer,Ui.top(this,-1,32,30));
        FrameLayout.LayoutParams b=new FrameLayout.LayoutParams(-1,-2,Gravity.CENTER);
        screen.addView(body,b);setContentView(screen);
        if(prefs.getBoolean("welcome_done",false))
            new Handler(Looper.getMainLooper()).postDelayed(this::enter,1300);
    }
    private void enter(){
        if(navigating||isFinishing()||isDestroyed())return;navigating=true;
        startActivity(new Intent(this,MainActivity.class));finish();
    }
}
