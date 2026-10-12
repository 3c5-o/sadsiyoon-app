package com.sadsiyoon.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private SharedPreferences prefs;
    private LinearLayout listHost;
    private TextView summary, filterButton;
    private String query="";
    private boolean favoritesOnly=false;

    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        getWindow().setStatusBarColor(Ui.BG);
        getWindow().setNavigationBarColor(Ui.BG);
        prefs=getSharedPreferences("sadsiyoon_local_v1",MODE_PRIVATE);
        buildScreen();
    }

    private void buildScreen(){
        LinearLayout root=Ui.column(this);root.setBackgroundColor(Ui.BG);
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setVerticalScrollBarEnabled(false);
        LinearLayout body=Ui.column(this);Ui.pad(this,body,18,18);
        scroll.addView(body);root.addView(scroll,new LinearLayout.LayoutParams(-1,-1));setContentView(root);

        LinearLayout header=Ui.row(this);
        TextView logo=Ui.text(this,"س",30,Ui.BG,true);logo.setGravity(Gravity.CENTER);
        logo.setBackground(Ui.background(this,Ui.MINT,14));
        header.addView(logo,new LinearLayout.LayoutParams(Ui.dp(this,54),Ui.dp(this,54)));
        LinearLayout brand=Ui.column(this);brand.setPadding(Ui.dp(this,12),0,Ui.dp(this,12),0);
        brand.addView(Ui.text(this,"سادسيون",25,Ui.WHITE,true));
        brand.addView(Ui.text(this,"منصتك التعليمية | النسخة التجريبية",12,Ui.MUTED,false));
        header.addView(brand,new LinearLayout.LayoutParams(0,-2,1));
        body.addView(header);body.addView(Ui.spacer(this,22));

        LinearLayout hero=Ui.column(this);Ui.pad(this,hero,18,20);
        hero.setBackground(Ui.outlined(this,Ui.PANEL,22,Ui.PANEL2));
        TextView chip=Ui.text(this,"دفعة 2026   •   الرياضيات",12,Ui.MINT,true);
        hero.addView(chip);
        hero.addView(Ui.spacer(this,10));
        hero.addView(Ui.text(this,LessonCatalog.CHAPTER,23,Ui.WHITE,true));
        hero.addView(Ui.spacer(this,6));
        hero.addView(Ui.text(this,"الأستاذ "+LessonCatalog.TEACHER,14,Ui.MUTED,false));
        hero.addView(Ui.spacer(this,14));
        summary=Ui.text(this,"",13,Ui.MINT,true);hero.addView(summary);
        TextView start=Ui.action(this,"ابدأ المحاضرة الأولى  ←",true);
        start.setOnClickListener(v->open(1));
        hero.addView(start,Ui.boxParams(this,44,16));
        body.addView(hero);body.addView(Ui.spacer(this,22));

        body.addView(Ui.text(this,"المحاضرات",21,Ui.WHITE,true));
        body.addView(Ui.spacer(this,5));
        body.addView(Ui.text(this,"بحث، حفظ مفضلة، وعلامة إكمال — جميعها تعمل بدون حساب.",12,Ui.MUTED,false));
        body.addView(Ui.spacer(this,14));
        EditText search=new EditText(this);
        search.setSingleLine(true);search.setHint("ابحث باسم المحاضرة أو الموضوع...");
        search.setTextSize(14);search.setHintTextColor(Ui.MUTED);search.setTextColor(Ui.WHITE);
        search.setPadding(Ui.dp(this,15),0,Ui.dp(this,15),0);
        search.setBackground(Ui.background(this,Ui.PANEL,12));
        body.addView(search,Ui.boxParams(this,52,0));
        search.addTextChangedListener(new TextWatcher(){
            @Override public void beforeTextChanged(CharSequence s,int st,int c,int a){}
            @Override public void onTextChanged(CharSequence s,int st,int before,int count){
                query=s.toString().trim().toLowerCase(Locale.ROOT);render();
            }
            @Override public void afterTextChanged(Editable e){}
        });
        filterButton=Ui.action(this,"☆ عرض المفضلة فقط",false);
        filterButton.setOnClickListener(v->{favoritesOnly=!favoritesOnly;filterButton.setText(favoritesOnly?"★ عرض جميع المحاضرات":"☆ عرض المفضلة فقط");render();});
        body.addView(filterButton,Ui.boxParams(this,42,12));
        listHost=Ui.column(this);body.addView(listHost,Ui.boxParams(this,-2,10));
        TextView footer=Ui.text(this,"مشغل تجريبي — قد تتغير روابط المصادر الخارجية. يُرجى احترام حقوق أصحاب المحتوى.",12,Ui.MUTED,false);
        footer.setGravity(Gravity.CENTER);footer.setPadding(0,Ui.dp(this,25),0,Ui.dp(this,25));body.addView(footer);
        render();
    }

    private void render(){
        if(listHost==null)return;
        listHost.removeAllViews();
        int completed=0,favorites=0,visible=0;
        for(LessonCatalog.Lesson lesson:LessonCatalog.all()){
            if(prefs.getBoolean("done_"+lesson.index,false))completed++;
            if(prefs.getBoolean("fav_"+lesson.index,false))favorites++;
        }
        summary.setText(LessonCatalog.size()+" محاضرة    •    "+completed+" مكتملة    •    "+favorites+" بالمفضلة");
        for(LessonCatalog.Lesson lesson:LessonCatalog.all()){
            boolean fav=prefs.getBoolean("fav_"+lesson.index,false);
            if(favoritesOnly&&!fav)continue;
            if(!query.isEmpty()&&!lesson.title.toLowerCase(Locale.ROOT).contains(query)
                    &&!String.valueOf(lesson.index).contains(query))continue;
            visible++;
            LinearLayout card=Ui.row(this);Ui.pad(this,card,11,12);
            card.setBackground(Ui.background(this,Ui.PANEL,15));
            LinearLayout.LayoutParams cp=Ui.boxParams(this,-2,8);listHost.addView(card,cp);
            TextView num=Ui.text(this,String.format(Locale.US,"%02d",lesson.index+1),13,Ui.MINT,true);
            num.setGravity(Gravity.CENTER);num.setBackground(Ui.background(this,Ui.PANEL2,9));
            card.addView(num,new LinearLayout.LayoutParams(Ui.dp(this,39),Ui.dp(this,41)));
            LinearLayout texts=Ui.column(this);texts.setPadding(Ui.dp(this,9),0,Ui.dp(this,9),0);
            TextView title=Ui.text(this,lesson.title,14,Ui.WHITE,true);title.setMaxLines(3);texts.addView(title);
            texts.addView(Ui.text(this,prefs.getBoolean("done_"+lesson.index,false)?"تم إكمال المحاضرة":"اضغط للتشغيل",11,prefs.getBoolean("done_"+lesson.index,false)?Ui.MINT:Ui.MUTED,false));
            card.addView(texts,new LinearLayout.LayoutParams(0,-2,1));
            LinearLayout actions=Ui.column(this);
            TextView favBtn=Ui.text(this,fav?"★":"☆",25,fav?Ui.MINT:Ui.MUTED,true);
            favBtn.setGravity(Gravity.CENTER);actions.addView(favBtn,new LinearLayout.LayoutParams(Ui.dp(this,38),Ui.dp(this,38)));
            favBtn.setOnClickListener(v->{prefs.edit().putBoolean("fav_"+lesson.index,!prefs.getBoolean("fav_"+lesson.index,false)).apply();render();});
            TextView doneBtn=Ui.text(this,prefs.getBoolean("done_"+lesson.index,false)?"☑":"☐",22,prefs.getBoolean("done_"+lesson.index,false)?Ui.MINT:Ui.MUTED,false);
            doneBtn.setGravity(Gravity.CENTER);actions.addView(doneBtn,new LinearLayout.LayoutParams(Ui.dp(this,38),Ui.dp(this,34)));
            doneBtn.setOnClickListener(v->{prefs.edit().putBoolean("done_"+lesson.index,!prefs.getBoolean("done_"+lesson.index,false)).apply();render();});
            card.addView(actions,new LinearLayout.LayoutParams(Ui.dp(this,40),-2));
            card.setOnClickListener(v->open(lesson.index));
        }
        if(visible==0){
            TextView empty=Ui.text(this,"ماكو محاضرات تطابق البحث.",15,Ui.MUTED,false);
            empty.setGravity(Gravity.CENTER);listHost.addView(empty,Ui.boxParams(this,100,10));
        }
    }
    private void open(int index){startActivity(new Intent(this,PlayerActivity.class).putExtra("lesson_index",index));}
    @Override protected void onResume(){super.onResume();if(listHost!=null)render();}
}
