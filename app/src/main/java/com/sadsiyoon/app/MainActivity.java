package com.sadsiyoon.app;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.ViewCompat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private SharedPreferences prefs;
    private String page="home",subjectFilter="",libraryFilter="fav",selectedBatch="2026";
    private int selectedTeacher=1,selectedChapter=0;
    private LinearLayout content;
    private int questionIndex=0,quizScore=0;
    private final String[] questions={
        "ما ناتج ٢ + ٣ × ٤؟","أيّ مما يأتي اسم إشارة؟",
        "ما وحدة قياس شدة التيار الكهربائي؟","ما الفرع العلمي الذي يدرس التفاعلات بين المواد؟",
        "أيّ مما يأتي يمثل عدداً مركباً؟"};
    private final String[][] options={
        {"٢٠","١٤","٢٤","١٨"},{"يلعب","هذه","إلى","جميل"},
        {"الفولت","الأمبير","الأوم","الواط"},{"الفيزياء","الرياضيات","الكيمياء","التاريخ"},
        {"٧","١٣","٣ + ٢i","صفر"}};
    private final int[] correct={1,1,1,2,2};

    @Override public void onCreate(Bundle saved){
        super.onCreate(saved);
        WindowCompat.setDecorFitsSystemWindows(getWindow(),false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        prefs=getSharedPreferences("sadsiyoon_local_v1",MODE_PRIVATE);Catalog.initialize(this);render();
    }
    private int d(int n){return Ui.dp(this,n);}
    private void space(LinearLayout a,int height){a.addView(Ui.gap(this,height));}
    private void show(String s){page=s;render();}
    private void addTitle(LinearLayout dest,String s,int size){dest.addView(Ui.txt(this,s,size,Ui.WHITE,true),Ui.top(this,-1,-2,10));}
    private void addMuted(LinearLayout dest,String s){dest.addView(Ui.txt(this,s,13,Ui.MUTED,false),Ui.top(this,-1,-2,5));}

    private LinearLayout.LayoutParams margins(int h,int top){return margins(-1,h,top);}
    private LinearLayout.LayoutParams margins(int w,int h,int top){
        LinearLayout.LayoutParams p=Ui.lp(this,w,h);p.topMargin=d(top);return p;
    }
    private void render(){
        LinearLayout root=Ui.col(this);root.setBackgroundColor(Ui.BG);
        ViewCompat.setOnApplyWindowInsetsListener(root,(v,insets)->{
            Insets bars=insets.getInsets(WindowInsetsCompat.Type.systemBars());
            root.setPadding(bars.left,bars.top,bars.right,bars.bottom);
            return insets;
        });
        boolean rootPage=page.equals("home")||page.equals("teachers")||page.equals("library")||page.equals("tests")||page.equals("more");
        root.addView(toolbar(rootPage),Ui.lp(this,-1,65));
        ScrollView scroll=new ScrollView(this);
        scroll.setFillViewport(true);scroll.setVerticalScrollBarEnabled(false);
        content=Ui.col(this);Ui.pad(this,content,17,12);scroll.addView(content);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1f));
        if(rootPage)root.addView(bottomNavigation(),Ui.lp(this,-1,70));
        setContentView(root);
        ViewCompat.requestApplyInsets(root);
        if(page.equals("home"))home(); else if(page.equals("teachers"))teachers();
        else if(page.equals("teacher"))teacherPage();else if(page.equals("chapter"))chapterPage();
        else if(page.equals("library"))library();else if(page.equals("tests"))tests();
        else if(page.equals("more"))more();else if(page.equals("search"))search();
        else if(page.equals("notice"))notice();
    }
    private View toolbar(boolean topLevel){
        LinearLayout bar=Ui.row(this);Ui.pad(this,bar,16,8);
        bar.setBackground(Ui.gradient(this,0xff0d2933,Ui.BG,0));
        if(!topLevel){
            TextView back=Ui.btn(this,"‹",false);bar.addView(back,Ui.lp(this,44,43));
            back.setOnClickListener(v->back());
        }else{
            bar.addView(Ui.logo(this,46,false),Ui.lp(this,46,46));
        }
        LinearLayout text=Ui.col(this);text.setPadding(d(11),0,d(11),0);
        String title=topLevel?"سادسيون":page.equals("teacher")?"المدرس":page.equals("chapter")?"المحاضرات":page.equals("search")?"البحث":"الإشعارات";
        text.addView(Ui.txt(this,title,21,Ui.GOLD,true));
        text.addView(Ui.txt(this,topLevel?"منصة طلاب السادس الإعدادي":"دفعة "+selectedBatch,11,Ui.MUTED,false));
        bar.addView(text,new LinearLayout.LayoutParams(0,-2,1));
        if(topLevel){
            TextView find=Ui.txt(this,"⌕",29,Ui.MINT,true);find.setGravity(Gravity.CENTER);
            bar.addView(find,Ui.lp(this,38,43));find.setOnClickListener(v->show("search"));
            TextView bell=Ui.txt(this,"♧",24,Ui.GOLD,true);bell.setGravity(Gravity.CENTER);
            bar.addView(bell,Ui.lp(this,34,43));bell.setOnClickListener(v->show("notice"));
        }
        return bar;
    }
    private View bottomNavigation(){
        LinearLayout nav=Ui.row(this);nav.setGravity(Gravity.CENTER);
        nav.setBackground(Ui.stroke(this,0xff0b2029,16,0xff20413d));
        final String[] pages={"home","teachers","library","tests","more"};
        final String[] labels={"الرئيسية","المدرسون","مكتبتي","الاختبارات","المزيد"};
        final String[] marks={"⌂","▦","▣","✓","≡"};
        for(int i=0;i<5;i++){
            final String destination=pages[i];boolean active=page.equals(destination);
            LinearLayout box=Ui.col(this);box.setGravity(Gravity.CENTER);
            TextView icon=Ui.txt(this,marks[i],24,active?Ui.GOLD:Ui.MUTED,true);
            icon.setGravity(Gravity.CENTER);
            box.addView(icon,Ui.lp(this,-1,30));
            TextView title=Ui.txt(this,labels[i],10,active?Ui.MINT:Ui.MUTED,active);
            title.setGravity(Gravity.CENTER);box.addView(title,Ui.lp(this,-1,21));
            if(active)box.setBackground(Ui.round(this,0xff12392f,12));
            LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,d(57),1f);p.setMargins(d(2),0,d(2),0);nav.addView(box,p);
            box.setOnClickListener(v->{subjectFilter="";show(destination);});
        }
        return nav;
    }
    private void home(){
        String nickname=prefs.getString("nickname","طالب سادسيون");
        List<Catalog.Ref> refs=Catalog.all(this);
        int fav=0,done=0;for(Catalog.Ref r:refs){if(prefs.getBoolean("fav_"+r.key(),false))fav++;if(prefs.getBoolean("done_"+r.key(),false))done++;}
        final int completed=done, total=refs.size();
        LinearLayout hero=Ui.card(this);hero.setBackground(Ui.gradient(this,0xff184d3d,0xff102634,20));
        hero.addView(Ui.txt(this,"مرحباً "+nickname,23,Ui.WHITE,true));
        addMuted(hero,"معاً نحو حلمك في السادس الإعدادي");
        space(hero,14);
        ProgressBar progress=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);
        progress.setMax(Math.max(1,total));progress.setProgress(completed);
        progress.setProgressTintList(android.content.res.ColorStateList.valueOf(Ui.MINT));
        progress.setProgressBackgroundTintList(android.content.res.ColorStateList.valueOf(0xff255454));
        hero.addView(progress,Ui.lp(this,-1,6));
        space(hero,12);
        LinearLayout stats=Ui.row(this);
        stat(stats,"محاضرة مكتملة",String.valueOf(done));
        stat(stats,"بالمفضلة",String.valueOf(fav));
        stat(stats,"نسبة الإكمال",total==0?"0%":((done*100)/total)+"%");
        hero.addView(stats);content.addView(hero,margins(-2,3));
        LinearLayout batch=Ui.card(this);batch.setBackground(Ui.gradient(this,0xff463c2c,0xff123b38,20));
        batch.addView(Ui.txt(this,"دفعة 2026",27,Ui.GOLD,true));addMuted(batch,"دروسك ومدرسوك بمكان واحد");
        TextView open=Ui.btn(this,"استعرض المدرسين   ‹",true);batch.addView(open,Ui.top(this,-1,45,16));
        open.setOnClickListener(v->{subjectFilter="";selectedBatch="2026";show("teachers");});
        content.addView(batch,margins(-2,15));
        // Dedicated 2027 cohort: no change to the existing 2026 directory.
        Catalog.Teacher teacher2027=null;
        for(Catalog.Teacher item:Catalog.byBatch(this,"2027")){teacher2027=item;break;}
        if(teacher2027!=null){
            final int teacherId=teacher2027.index;
            LinearLayout newBatch=Ui.card(this);
            newBatch.setBackground(Ui.gradient(this,0xff124b46,0xff113245,20));
            newBatch.addView(Ui.txt(this,"دفعة 2027",27,Ui.MINT,true));
            newBatch.addView(Ui.txt(this,"الرياضيات • حيدر عبدالائمة",16,Ui.WHITE,true),
                Ui.top(this,-1,-2,7));
            newBatch.addView(Ui.txt(this,teacher2027.chapters.size()+" فصل • "+teacher2027.count()+" محاضرة",13,Ui.GOLD,false),
                Ui.top(this,-1,-2,4));
            newBatch.addView(Ui.txt(this,"بعض المشاهدات تتطلب اشتراكاً لدى مزود الفيديو؛ لا يضمن هذا التطبيق تشغيلها.",12,Ui.MUTED,false),
                Ui.top(this,-1,-2,9));
            TextView view2027=Ui.btn(this,"استعرض محاضرات دفعة 2027   ‹",true);
            newBatch.addView(view2027,Ui.top(this,-1,45,13));
            view2027.setOnClickListener(v->{subjectFilter="";selectedBatch="2027";openTeacher(teacherId);});
            content.addView(newBatch,margins(-2,11));
        }
        sectionHeader("أساتذتنا المميزون","عرض الجميع",()->show("teachers"));
        HorizontalScrollView slider=new HorizontalScrollView(this);slider.setHorizontalScrollBarEnabled(false);
        LinearLayout line=Ui.row(this);
        List<Catalog.Teacher> ts=Catalog.byBatch(this,"2026");
        for(int i=0;i<Math.min(ts.size(),9);i++){
            Catalog.Teacher t=ts.get(i);final int selected=i;
            LinearLayout card=Ui.card(this);card.setGravity(Gravity.CENTER);
            card.setBackground(Ui.gradient(this,i%2==0?0xff15473d:0xff223448,Ui.PANEL,18));
            android.view.View picture=Ui.portrait(this,t.image,t.name,96);card.addView(picture,Ui.lp(this,-1,98));space(card,8);
            TextView name=Ui.txt(this,t.name,15,Ui.WHITE,true);name.setGravity(Gravity.CENTER);card.addView(name,Ui.lp(this,-1,26));
            TextView material=Ui.txt(this,t.subject,12,Ui.MINT,false);material.setGravity(Gravity.CENTER);card.addView(material,Ui.lp(this,-1,20));
            LinearLayout.LayoutParams p=Ui.lp(this,144,-2);p.leftMargin=d(10);line.addView(card,p);
            card.setOnClickListener(v->openTeacher(selected));
        }
        slider.addView(line);content.addView(slider,margins(-1,190,10));
        sectionHeader("المواد الدراسية","جميع المواد",()->show("teachers"));
        String[] subjects={"الرياضيات","الفيزياء","الكيمياء","الأحياء","اللغة العربية","اللغة الإنكليزية","التربية الإسلامية"};
        String[] symbols={"√x","∑","∫","DNA","ع","En","◈"};
        for(int i=0;i<subjects.length;i+=2){
            LinearLayout row=Ui.row(this);
            for(int j=i;j<Math.min(i+2,subjects.length);j++){
                final String subject=subjects[j];
                LinearLayout card=Ui.card(this);card.setBackground(Ui.gradient(this,j%2==0?0xff125244:0xff19385a,Ui.PANEL,16));
                card.addView(Ui.txt(this,symbols[j],25,Ui.GOLD,true));
                card.addView(Ui.txt(this,subject,14,Ui.WHITE,true),Ui.top(this,-1,24,4));
                int count=0;for(Catalog.Teacher t:ts)if(t.subject.equals(subject))count++;
                card.addView(Ui.txt(this,count+" مدرس",12,Ui.MUTED,false));
                LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,d(116),1f);p.setMargins(d(3),d(5),d(3),d(5));row.addView(card,p);
                card.setOnClickListener(v->{subjectFilter=subject;selectedBatch="2026";show("teachers");});
            }
            content.addView(row);
        }
        if(prefs.contains("last_t")){
            sectionHeader("متابعة المشاهدة","مكتبتي",()->show("library"));
            Catalog.Ref last=Catalog.ref(this,prefs.getInt("last_t",0),prefs.getInt("last_c",0),prefs.getInt("last_l",0));
            if(last!=null)lessonRow(content,last);
        }
    }
    private void stat(LinearLayout row,String label,String value){
        LinearLayout col=Ui.col(this);col.setGravity(Gravity.CENTER);
        TextView v=Ui.txt(this,value,19,Ui.MINT,true);v.setGravity(Gravity.CENTER);col.addView(v);
        TextView t=Ui.txt(this,label,11,Ui.MUTED,false);t.setGravity(Gravity.CENTER);col.addView(t);
        row.addView(col,new LinearLayout.LayoutParams(0,d(61),1f));
    }
    private void sectionHeader(String title,String action,Runnable runnable){
        LinearLayout row=Ui.row(this);
        row.addView(Ui.txt(this,title,20,Ui.WHITE,true),new LinearLayout.LayoutParams(0,d(47),1f));
        TextView button=Ui.txt(this,action,12,Ui.MINT,true);row.addView(button);
        button.setOnClickListener(v->runnable.run());content.addView(row,margins(-1,47,14));
    }
    private void teachers(){
        addTitle(content,"مدرسو دفعة "+selectedBatch,24);
        LinearLayout batchTabs=Ui.row(this);
        TextView older=Ui.btn(this,"دفعة 2026",selectedBatch.equals("2026"));
        TextView newer=Ui.btn(this,"دفعة 2027",selectedBatch.equals("2027"));
        LinearLayout.LayoutParams first=new LinearLayout.LayoutParams(0,d(45),1f);
        first.setMargins(0,0,d(5),0);batchTabs.addView(older,first);
        batchTabs.addView(newer,new LinearLayout.LayoutParams(0,d(45),1f));
        older.setOnClickListener(v->{selectedBatch="2026";subjectFilter="";show("teachers");});
        newer.setOnClickListener(v->{selectedBatch="2027";subjectFilter="";show("teachers");});
        content.addView(batchTabs,margins(-1,45,12));
        List<Catalog.Teacher> batchTeachers=Catalog.byBatch(this,selectedBatch);
        addMuted(content,batchTeachers.size()+" مدرس ضمن دفعة "+selectedBatch);
        if(!subjectFilter.isEmpty()){
            TextView reset=Ui.btn(this,subjectFilter+"    ×",false);
            content.addView(reset,Ui.top(this,-1,40,12));reset.setOnClickListener(v->{subjectFilter="";show("teachers");});
        }
        for(Catalog.Teacher t:batchTeachers){
            if(!subjectFilter.isEmpty()&&!subjectFilter.equals(t.subject))continue;
            LinearLayout card=Ui.card(this);card.setOrientation(LinearLayout.HORIZONTAL);card.setGravity(Gravity.CENTER_VERTICAL);
            card.addView(Ui.portrait(this,t.image,t.name,80),Ui.lp(this,75,80));
            LinearLayout detail=Ui.col(this);detail.setPadding(d(12),0,d(12),0);
            detail.addView(Ui.txt(this,t.name,18,Ui.WHITE,true));
            detail.addView(Ui.txt(this,t.subject+"   •   "+t.chapters.size()+" أقسام",13,Ui.MINT,false),Ui.top(this,-1,23,2));
            detail.addView(Ui.txt(this,t.count()+" محاضرة مفهرسة",12,Ui.MUTED,false),Ui.top(this,-1,20,2));
            card.addView(detail,new LinearLayout.LayoutParams(0,-2,1));
            card.addView(Ui.txt(this,"‹",22,Ui.GOLD,true));
            content.addView(card,margins(-1,-2,11));
            card.setOnClickListener(v->openTeacher(t.index));
        }
    }
    private void openTeacher(int index){
        Catalog.Teacher teacher=Catalog.teacher(this,index);
        if(teacher==null)return;
        selectedBatch=teacher.batch;selectedTeacher=index;selectedChapter=0;show("teacher");
    }
    private void teacherPage(){
        Catalog.Teacher t=Catalog.teacher(this,selectedTeacher);if(t==null){show("teachers");return;}
        LinearLayout hero=Ui.card(this);hero.setBackground(Ui.gradient(this,0xff194c42,Ui.PANEL,20));
        LinearLayout top=Ui.row(this);top.addView(Ui.portrait(this,t.image,t.name,98),Ui.lp(this,100,105));
        LinearLayout right=Ui.col(this);right.setPadding(d(15),0,d(8),0);
        right.addView(Ui.txt(this,"الأستاذ "+t.name,20,Ui.WHITE,true));
        right.addView(Ui.txt(this,t.subject,14,Ui.MINT,true));
        right.addView(Ui.txt(this,"دفعة "+t.batch,12,Ui.MUTED,false));
        top.addView(right,new LinearLayout.LayoutParams(0,-2,1));hero.addView(top);
        space(hero,12);
        hero.addView(Ui.txt(this,t.chapters.size()+" أقسام     •     "+t.count()+" محاضرة     •     "+t.ready()+" قابلة للمحاولة",12,Ui.GOLD,true));
        content.addView(hero,margins(-1,-2,3));
        addTitle(content,"الفصول والمحاضرات",21);
        for(int c=0;c<t.chapters.size();c++){
            final int chapterIndex=c;Catalog.Chapter chapter=t.chapters.get(c);
            LinearLayout line=Ui.card(this);line.setOrientation(LinearLayout.HORIZONTAL);line.setGravity(Gravity.CENTER_VERTICAL);
            TextView num=Ui.txt(this,String.valueOf(c+1),16,Ui.GOLD,true);num.setGravity(Gravity.CENTER);
            num.setBackground(Ui.round(this,Ui.PANEL2,10));line.addView(num,Ui.lp(this,42,42));
            LinearLayout text=Ui.col(this);text.setPadding(d(12),0,d(8),0);
            text.addView(Ui.txt(this,chapter.title,16,Ui.WHITE,true));
            text.addView(Ui.txt(this,chapter.lessons.size()+" محاضرة",12,Ui.MUTED,false));
            line.addView(text,new LinearLayout.LayoutParams(0,-2,1));
            line.addView(Ui.txt(this,"‹",25,Ui.MINT,true));
            content.addView(line,margins(-1,-2,9));line.setOnClickListener(v->{selectedChapter=chapterIndex;show("chapter");});
        }
    }
    private void chapterPage(){
        Catalog.Teacher t=Catalog.teacher(this,selectedTeacher);
        if(t==null||selectedChapter>=t.chapters.size()){show("teachers");return;}
        Catalog.Chapter chapter=t.chapters.get(selectedChapter);
        LinearLayout panel=Ui.card(this);panel.setBackground(Ui.gradient(this,0xff12493d,Ui.PANEL,18));
        panel.addView(Ui.txt(this,t.name+"  •  دفعة "+t.batch,15,Ui.GOLD,true));
        panel.addView(Ui.txt(this,chapter.title,22,Ui.WHITE,true),Ui.top(this,-1,-2,8));
        panel.addView(Ui.txt(this,chapter.lessons.size()+" محاضرة",13,Ui.MUTED,false),Ui.top(this,-1,-2,8));
        content.addView(panel,margins(-1,-2,5));
        for(int i=0;i<chapter.lessons.size();i++){
            Catalog.Ref ref=Catalog.ref(this,selectedTeacher,selectedChapter,i);
            if(ref!=null)lessonRow(content,ref);
        }
    }
    private void lessonRow(LinearLayout host,Catalog.Ref ref){
        LinearLayout line=Ui.card(this);line.setOrientation(LinearLayout.HORIZONTAL);line.setGravity(Gravity.CENTER_VERTICAL);
        String id=ref.key();boolean done=prefs.getBoolean("done_"+id,false),fav=prefs.getBoolean("fav_"+id,false);
        TextView play=Ui.txt(this,ref.lesson.available()?"▶":"—",18,ref.lesson.available()?Ui.MINT:Ui.MUTED,true);
        play.setGravity(Gravity.CENTER);play.setBackground(Ui.round(this,Ui.PANEL2,11));
        line.addView(play,Ui.lp(this,43,43));
        LinearLayout text=Ui.col(this);text.setPadding(d(10),0,d(10),0);
        text.addView(Ui.txt(this,ref.lesson.title,14,Ui.WHITE,true));
        String playbackLabel=done?"مكتملة":
            !ref.lesson.available()?"غير متاحة للتشغيل حالياً":
            ref.lesson.access.equals("premium")?"يتطلب اشتراك مشاهدة لدى المزود":
            ref.lesson.access.equals("unverified")?"مشغّل مضمن — التشغيل غير مؤكّد":"شاهد المحاضرة";
        text.addView(Ui.txt(this,playbackLabel,11,done?Ui.MINT:Ui.MUTED,false),Ui.top(this,-1,-2,4));
        line.addView(text,new LinearLayout.LayoutParams(0,-2,1));
        TextView favorite=Ui.txt(this,fav?"★":"☆",24,fav?Ui.GOLD:Ui.MUTED,true);favorite.setGravity(Gravity.CENTER);
        line.addView(favorite,Ui.lp(this,30,44));
        favorite.setOnClickListener(v->{prefs.edit().putBoolean("fav_"+id,!prefs.getBoolean("fav_"+id,false)).apply();favorite.setText(prefs.getBoolean("fav_"+id,false)?"★":"☆");favorite.setTextColor(prefs.getBoolean("fav_"+id,false)?Ui.GOLD:Ui.MUTED);});
        TextView finished=Ui.txt(this,done?"✓":"○",21,done?Ui.MINT:Ui.MUTED,true);finished.setGravity(Gravity.CENTER);
        line.addView(finished,Ui.lp(this,31,44));
        finished.setOnClickListener(v->{prefs.edit().putBoolean("done_"+id,!prefs.getBoolean("done_"+id,false)).apply();boolean b=prefs.getBoolean("done_"+id,false);finished.setText(b?"✓":"○");finished.setTextColor(b?Ui.MINT:Ui.MUTED);});
        host.addView(line,margins(-1,-2,8));
        line.setOnClickListener(v->openLesson(ref));
    }
    private void openLesson(Catalog.Ref r){
        if(!r.lesson.available()){
            Toast.makeText(this,"هذه المحاضرة غير متاحة للمشاهدة حالياً",Toast.LENGTH_SHORT).show();return;
        }
        prefs.edit().putInt("last_t",r.ti).putInt("last_c",r.ci).putInt("last_l",r.li).apply();
        Intent i=new Intent(this,PlayerActivity.class);
        i.putExtra("ti",r.ti);i.putExtra("ci",r.ci);i.putExtra("li",r.li);startActivity(i);
    }
    private void library(){
        addTitle(content,"مكتبتي",24);addMuted(content,"مفضلاتك والمحاضرات المكتملة ومتابعة المشاهدة");
        LinearLayout filters=Ui.row(this);
        TextView fav=Ui.btn(this,"المفضلة",libraryFilter.equals("fav"));
        TextView done=Ui.btn(this,"المكتملة",libraryFilter.equals("done"));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,d(42),1f);p.leftMargin=d(6);filters.addView(fav,p);
        filters.addView(done,new LinearLayout.LayoutParams(0,d(42),1f));content.addView(filters,margins(-1,42,13));
        fav.setOnClickListener(v->{libraryFilter="fav";show("library");});
        done.setOnClickListener(v->{libraryFilter="done";show("library");});
        List<Catalog.Ref> selected=new ArrayList<>();
        String key=libraryFilter.equals("fav")?"fav_":"done_";
        for(Catalog.Ref ref:Catalog.all(this))if(prefs.getBoolean(key+ref.key(),false))selected.add(ref);
        addMuted(content,selected.size()+" محاضرة");
        if(selected.isEmpty()){
            LinearLayout empty=Ui.card(this);
            empty.addView(Ui.txt(this,"مكتبتك بانتظار أول محاضرة",18,Ui.WHITE,true));
            addMuted(empty,"احفظ محاضراتك بالمفضلة أو علّمها كمكتملة حتى تظهر هنا.");
            content.addView(empty,margins(-1,-2,18));
        }
        for(int i=0;i<Math.min(100,selected.size());i++)lessonRow(content,selected.get(i));
        if(selected.size()>100)addMuted(content,"تم عرض أول 100 محاضرة. يمكنك الوصول للبقية عبر صفحات المدرسين.");
    }
    private void search(){
        EditText input=new EditText(this);input.setSingleLine(true);
        input.setHint("ابحث عن مدرس أو فصل أو محاضرة");input.setTextColor(Ui.WHITE);input.setHintTextColor(Ui.MUTED);
        input.setTextSize(15);Ui.pad(this,input,15,8);input.setBackground(Ui.round(this,Ui.PANEL2,13));
        content.addView(input,margins(-1,53,8));
        LinearLayout results=Ui.col(this);content.addView(results,Ui.top(this,-1,-2,12));
        input.addTextChangedListener(new TextWatcher(){
            public void beforeTextChanged(CharSequence s,int a,int c,int f){}
            public void onTextChanged(CharSequence s,int a,int b,int count){populateSearch(results,s.toString());}
            public void afterTextChanged(Editable e){}
        });
        populateSearch(results,"");
    }
    private void populateSearch(LinearLayout dest,String text){
        dest.removeAllViews();String q=text.trim().toLowerCase(Locale.ROOT);
        if(q.length()<2){dest.addView(Ui.txt(this,"اكتب حرفين أو أكثر للبحث",14,Ui.MUTED,false));return;}
        int found=0;
        for(Catalog.Teacher t:Catalog.teachers(this)){
            if(t.name.toLowerCase(Locale.ROOT).contains(q) || t.subject.toLowerCase(Locale.ROOT).contains(q)){
                final int index=t.index;TextView b=Ui.btn(this,t.name+" — "+t.subject+" — "+t.batch,false);
                dest.addView(b,margins(-1,-2,7));b.setOnClickListener(v->openTeacher(index));found++;
            }
        }
        for(Catalog.Ref ref:Catalog.all(this)){
            if(ref.lesson.title.toLowerCase(Locale.ROOT).contains(q)||ref.chapter.title.toLowerCase(Locale.ROOT).contains(q)){
                lessonRow(dest,ref);if(++found>=65)break;
            }
        }
        if(found==0)dest.addView(Ui.txt(this,"لا توجد نتائج مطابقة",14,Ui.MUTED,false));
    }
    private void tests(){
        addTitle(content,"الاختبارات",24);addMuted(content,"اختبار تدريبي محلي قصير، لا يحتاج اتصالاً بالإنترنت.");
        if(questionIndex>=questions.length){
            LinearLayout box=Ui.card(this);box.addView(Ui.txt(this,"نتيجتك: "+quizScore+" من "+questions.length,24,Ui.GOLD,true));
            addMuted(box,"يمكنك المحاولة مرة ثانية لتحسين نتيجتك.");
            TextView retry=Ui.btn(this,"إعادة الاختبار",true);box.addView(retry,Ui.top(this,-1,48,18));
            retry.setOnClickListener(v->{questionIndex=0;quizScore=0;show("tests");});content.addView(box,margins(-1,-2,16));return;
        }
        LinearLayout box=Ui.card(this);box.setBackground(Ui.gradient(this,0xff103b34,Ui.PANEL,20));
        box.addView(Ui.txt(this,"السؤال "+(questionIndex+1)+" من "+questions.length,14,Ui.GOLD,true));
        box.addView(Ui.txt(this,questions[questionIndex],21,Ui.WHITE,true),Ui.top(this,-1,-2,12));
        content.addView(box,margins(-1,-2,17));
        final int at=questionIndex;
        for(int i=0;i<4;i++){
            final int choice=i;TextView option=Ui.btn(this,options[at][i],false);option.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);
            content.addView(option,margins(-1,52,10));
            option.setOnClickListener(v->{
                boolean correctAnswer=choice==correct[at];
                if(correctAnswer)quizScore++;
                new AlertDialog.Builder(this).setTitle(correctAnswer?"إجابة صحيحة":"راجع الإجابة")
                    .setMessage(correctAnswer?"أحسنت.":"الإجابة الصحيحة: "+options[at][correct[at]])
                    .setPositiveButton("التالي",(dialog,w)->{questionIndex++;show("tests");}).setCancelable(false).show();
            });
        }
    }
    private void more(){
        addTitle(content,"حسابي والإعدادات",24);
        LinearLayout profile=Ui.card(this);
        profile.addView(Ui.txt(this,prefs.getString("nickname","طالب سادسيون"),20,Ui.WHITE,true));
        addMuted(profile,"تقدمك محفوظ على هذا الجهاز فقط.");
        TextView rename=Ui.btn(this,"تعديل الاسم",true);
        profile.addView(rename,Ui.top(this,-1,43,16));rename.setOnClickListener(v->editName());
        content.addView(profile,margins(-1,-2,12));
        LinearLayout panel=Ui.card(this);
        panel.addView(Ui.txt(this,"التشغيل التلقائي للمحاضرة التالية",15,Ui.WHITE,true));
        android.widget.Switch autoplay=new android.widget.Switch(this);
        autoplay.setText(prefs.getBoolean("autoplay",true)?"مفعّل":"متوقف");
        autoplay.setTextColor(Ui.MINT);autoplay.setChecked(prefs.getBoolean("autoplay",true));
        autoplay.setOnCheckedChangeListener((b,value)->{prefs.edit().putBoolean("autoplay",value).apply();autoplay.setText(value?"مفعّل":"متوقف");});
        panel.addView(autoplay,Ui.top(this,-1,45,7));
        addMuted(panel,"المشغّل يدعم ملء الشاشة والنافذة العائمة حسب إمكانات الجهاز.");
        content.addView(panel,margins(-1,-2,12));
        TextView reset=Ui.btn(this,"مسح تقدم المشاهدة والمفضلة",false);
        content.addView(reset,margins(-1,48,15));
        reset.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("تأكيد مسح التقدم")
            .setMessage("سيتم مسح المفضلة وعلامات الإكمال ومواضع المشاهدة المحفوظة على هذا الجهاز.")
            .setNegativeButton("إلغاء",null).setPositiveButton("مسح",(dialog,w)->{
                SharedPreferences.Editor edit=prefs.edit();
                for(String key:new HashSet<>(prefs.getAll().keySet()))
                    if(key.startsWith("done_")||key.startsWith("fav_")||key.startsWith("position_")||key.startsWith("note_"))
                        edit.remove(key);
                edit.apply();show("more");
            }).show());
        addMuted(content,"سادسيون V1.2.0 — دفعتا 2026 و2027 — نسخة اختبارية.");
    }
    private void notice(){
        LinearLayout empty=Ui.card(this);empty.addView(Ui.txt(this,"لا توجد إشعارات جديدة",19,Ui.WHITE,true));
        addMuted(empty,"سيتم عرض التنبيهات المهمة هنا عند توفرها.");
        content.addView(empty,margins(-1,-2,10));
    }
    private void editName(){
        EditText input=new EditText(this);input.setSingleLine(true);
        input.setText(prefs.getString("nickname","طالب سادسيون"));
        new AlertDialog.Builder(this).setTitle("اسم الطالب").setView(input)
            .setNegativeButton("إلغاء",null).setPositiveButton("حفظ",(d,w)->{
                String name=input.getText().toString().trim();
                if(!name.isEmpty())prefs.edit().putString("nickname",name.substring(0,Math.min(30,name.length()))).apply();
                show("more");
            }).show();
    }
    private void back(){
        if(page.equals("chapter"))show("teacher");
        else if(page.equals("teacher"))show("teachers");
        else show("home");
    }
    @Override public void onBackPressed(){
        if(page.equals("home"))new AlertDialog.Builder(this).setTitle("الخروج من سادسيون")
            .setMessage("هل تريد إغلاق التطبيق؟").setNegativeButton("إلغاء",null)
            .setPositiveButton("خروج",(d,w)->finish()).show();
        else back();
    }
    @Override protected void onResume(){super.onResume();if(content!=null&&(page.equals("home")||page.equals("library")||page.equals("chapter")))render();}
}
