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
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static final int BACKUP_EXPORT_REQUEST=8181;
    private static final int BACKUP_IMPORT_REQUEST=8182;
    private SharedPreferences prefs;
    private String page="home",subjectFilter="",libraryFilter="fav",selectedBatch="2026";
    private int selectedTeacher=1,selectedChapter=0;
    private String examBranch="scientific",examSubject="الرياضيات",examRound="الدور الأول";
    private LinearLayout content;
    private ScrollView pageScroller;
    private final ArrayDeque<String> navigationHistory=new ArrayDeque<>();
    private final Map<String,Integer> scrollPositions=new HashMap<>();
    private String scrollKey(){return page+":"+selectedBatch+":"+selectedTeacher+":"+selectedChapter+":"+examBranch+":"+examSubject+":"+examRound;}
    private void rememberScroll(){if(pageScroller!=null)scrollPositions.put(scrollKey(),pageScroller.getScrollY());}
    private int questionIndex=0,quizScore=0;
    private String practiceBranch="scientific";
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
        prefs=getSharedPreferences("sadsiyoon_local_v1",MODE_PRIVATE);
        if(saved!=null){
            page=saved.getString("page","home");
            selectedBatch=saved.getString("batch","2026");
            selectedTeacher=saved.getInt("teacher",1);
            selectedChapter=saved.getInt("chapter",0);
            subjectFilter=saved.getString("subjectFilter","");
            libraryFilter=saved.getString("libraryFilter","fav");
            examBranch=saved.getString("examBranch","scientific");
            examSubject=saved.getString("examSubject","الرياضيات");
            examRound=saved.getString("examRound","الدور الأول");
            practiceBranch=saved.getString("practiceBranch","scientific");
            questionIndex=saved.getInt("questionIndex",0);
            quizScore=saved.getInt("quizScore",0);
            ArrayList<String> history=saved.getStringArrayList("navHistory");
            if(history!=null)for(String oldPage:history)navigationHistory.addLast(oldPage);
        }
        Catalog.initialize(this);render();
    }
    private int d(int n){return Ui.dp(this,n);}
    private void space(LinearLayout a,int height){a.addView(Ui.gap(this,height));}
    private void show(String next){
        if(next==null)return;
        if(next.equals(page)){
            // Rebuild the same screen when a filter, quiz answer, or retry changes.
            scrollPositions.remove(scrollKey());
            render();
            return;
        }
        rememberScroll();
        navigationHistory.addLast(page);
        if(navigationHistory.size()>30)navigationHistory.removeFirst();
        page=next;render();
    }
    @Override protected void onSaveInstanceState(Bundle state){
        rememberScroll();
        state.putString("page",page);state.putString("batch",selectedBatch);
        state.putInt("teacher",selectedTeacher);state.putInt("chapter",selectedChapter);
        state.putString("subjectFilter",subjectFilter);state.putString("libraryFilter",libraryFilter);
        state.putString("examBranch",examBranch);state.putString("examSubject",examSubject);
        state.putString("examRound",examRound);
        state.putString("practiceBranch",practiceBranch);
        state.putInt("questionIndex",questionIndex);
        state.putInt("quizScore",quizScore);
        state.putStringArrayList("navHistory",new ArrayList<>(navigationHistory));
        super.onSaveInstanceState(state);
    }
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
        boolean rootPage=page.equals("home")||page.equals("teachers")||page.equals("exams")||page.equals("library")||page.equals("more");
        root.addView(toolbar(rootPage),Ui.lp(this,-1,65));
        ScrollView scroll=new ScrollView(this);
        pageScroller=scroll;
        scroll.setFillViewport(true);scroll.setVerticalScrollBarEnabled(false);
        content=Ui.col(this);Ui.pad(this,content,16,14);scroll.addView(content);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1f));
        if(rootPage)root.addView(bottomNavigation(),Ui.lp(this,-1,70));
        setContentView(root);
        ViewCompat.requestApplyInsets(root);
        if(page.equals("home"))home(); else if(page.equals("teachers"))teachers();
        else if(page.equals("teacher"))teacherPage();else if(page.equals("chapter"))chapterPage();
        else if(page.equals("library"))library();else if(page.equals("tests"))tests();
        else if(page.equals("more"))more();else if(page.equals("search"))search();
        else if(page.equals("notice"))notice();
        else if(page.equals("exams"))exams();
        final int oldY=scrollPositions.getOrDefault(scrollKey(),0);
        if(oldY>0)scroll.post(()->{if(scroll.getParent()!=null)scroll.scrollTo(0,oldY);});
    }
    private View toolbar(boolean topLevel){
        LinearLayout bar=Ui.row(this);Ui.pad(this,bar,16,8);
        bar.setBackground(Ui.gradient(this,0xff152C51,Ui.BG,0));
        if(!topLevel){
            TextView back=Ui.btn(this,"رجوع",false);
            bar.addView(back,Ui.lp(this,67,42));
            back.setOnClickListener(v->back());
        }else{
            bar.addView(Ui.logo(this,46,false),Ui.lp(this,46,46));
        }
        LinearLayout text=Ui.col(this);text.setPadding(d(11),0,d(11),0);
        String title=page.equals("home")?"سادسيون":page.equals("teachers")?"المدرسون":
            page.equals("exams")?"الوزاريات":page.equals("library")?"مكتبتي":
            page.equals("more")?"حسابي":page.equals("tests")?"الاختبارات":
            page.equals("teacher")?"المدرس":page.equals("chapter")?"المحاضرات":
            page.equals("search")?"البحث":"التنبيهات";
        text.addView(Ui.txt(this,title,21,Ui.WHITE,true));
        text.addView(Ui.txt(this,topLevel?"تعلم بوضوح، وتقدم بثقة":"دفعة "+selectedBatch,11,Ui.MUTED,false));
        bar.addView(text,new LinearLayout.LayoutParams(0,-2,1));
        if(topLevel){
            TextView find=Ui.btn(this,"بحث",false);
            find.setTextSize(12);
            bar.addView(find,Ui.lp(this,57,42));
            find.setContentDescription("البحث عن مدرس أو محاضرة");
            find.setOnClickListener(v->show("search"));
        }
        return bar;
    }
    private View bottomNavigation(){
        LinearLayout nav=Ui.row(this);
        Ui.pad(this,nav,8,5);nav.setGravity(Gravity.CENTER);
        nav.setBackground(Ui.stroke(this,0xff14243B,16,0xff2C4062));
        final String[] pages={"home","teachers","exams","library","more"};
        final String[] labels={"الرئيسية","المدرسون","الوزاريات","مكتبتي","حسابي"};
        final String[] marks={"⌂","▦","▤","☆","☰"};
        for(int i=0;i<pages.length;i++){
            final String destination=pages[i];boolean active=page.equals(destination);
            LinearLayout box=Ui.col(this);box.setGravity(Gravity.CENTER);
            TextView icon=Ui.txt(this,marks[i],21,active?Ui.MINT:Ui.MUTED,true);
            icon.setGravity(Gravity.CENTER);
            box.addView(icon,Ui.lp(this,-1,27));
            TextView title=Ui.txt(this,labels[i],11,active?Ui.WHITE:Ui.MUTED,active);
            title.setGravity(Gravity.CENTER);
            box.addView(title,Ui.lp(this,-1,21));
            if(active)box.setBackground(Ui.round(this,0xff264477,13));
            LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,d(56),1f);
            p.setMargins(d(2),0,d(2),0);nav.addView(box,p);
            box.setContentDescription(labels[i]);
            box.setOnClickListener(v->{subjectFilter="";show(destination);});
        }
        return nav;
    }
    private void home(){
        String nickname=prefs.getString("nickname","طالب سادسيون");
        List<Catalog.Ref> refs=Catalog.all(this);
        int fav=0,done=0;
        for(Catalog.Ref r:refs){
            if(prefs.getBoolean("fav_"+r.key(),false))fav++;
            if(prefs.getBoolean("done_"+r.key(),false))done++;
        }
        int total=refs.size();
        LinearLayout hero=Ui.card(this);
        hero.setBackground(Ui.gradient(this,0xff254B86,0xff162B4C,22));
        hero.addView(Ui.txt(this,"مرحباً، "+nickname,23,Ui.WHITE,true));
        hero.addView(Ui.txt(this,"خطوة جديدة نحو هدفك الدراسي",13,0xffD6E6FF,false),Ui.top(this,-1,-2,6));
        space(hero,17);
        LinearLayout progressHead=Ui.row(this);
        progressHead.addView(Ui.txt(this,"إنجازك في المحاضرات",13,Ui.WHITE,true),
            new LinearLayout.LayoutParams(0,d(27),1f));
        progressHead.addView(Ui.txt(this,total==0?"0%":((done*100)/total)+"%",14,Ui.MINT,true));
        hero.addView(progressHead);
        ProgressBar progress=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);
        progress.setMax(Math.max(1,total));progress.setProgress(done);
        progress.setProgressTintList(android.content.res.ColorStateList.valueOf(Ui.MINT));
        progress.setProgressBackgroundTintList(android.content.res.ColorStateList.valueOf(0xff385576));
        hero.addView(progress,Ui.top(this,-1,6,5));
        space(hero,11);
        LinearLayout stats=Ui.row(this);
        stat(stats,"محاضرة مكتملة",String.valueOf(done));
        stat(stats,"محاضرة محفوظة",String.valueOf(fav));
        stat(stats,"المحاضرات",String.valueOf(total));
        hero.addView(stats);
        content.addView(hero,margins(-1,-2,4));

        if(prefs.contains("last_t")){
            Catalog.Ref last=Catalog.ref(this,prefs.getInt("last_t",0),
                prefs.getInt("last_c",0),prefs.getInt("last_l",0));
            if(last!=null){
                sectionHeader("أكمل من حيث توقفت","مكتبتي",()->show("library"));
                LinearLayout resume=Ui.card(this);
                resume.setBackground(Ui.gradient(this,0xff1E385C,Ui.PANEL,17));
                resume.addView(Ui.txt(this,"متابعة المحاضرة",12,Ui.MINT,true));
                resume.addView(Ui.txt(this,last.lesson.title,17,Ui.WHITE,true),Ui.top(this,-1,-2,6));
                resume.addView(Ui.txt(this,last.teacher.name+" • "+last.chapter.title,12,Ui.MUTED,false),Ui.top(this,-1,-2,5));
                TextView continueButton=Ui.btn(this,"متابعة المشاهدة  ←",true);
                resume.addView(continueButton,Ui.top(this,-1,44,13));
                continueButton.setOnClickListener(v->openLesson(last));
                content.addView(resume,margins(-1,-2,6));
            }
        }

        sectionHeader("استكشف سادسيون","بحث",()->show("search"));
        LinearLayout firstRow=Ui.row(this);
        firstRow.addView(quickTile("▦","المدرسون","الدروس والفصول",()->show("teachers")),
            quickParams());
        firstRow.addView(quickTile("▤","الوزاريات","الأسئلة والحلول",()->show("exams")),
            quickParams());
        content.addView(firstRow);
        LinearLayout secondRow=Ui.row(this);
        secondRow.addView(quickTile("✓","الاختبارات","درّب نفسك",()->show("tests")),
            quickParams());
        secondRow.addView(quickTile("☆","مكتبتي","المفضلة والتقدم",()->show("library")),
            quickParams());
        content.addView(secondRow,Ui.top(this,-1,-2,7));

        sectionHeader("اختر دفعتك","المدرسون",()->show("teachers"));
        LinearLayout cohortRow=Ui.row(this);
        String[] batches={"2026","2027"};
        for(String batch:batches){
            List<Catalog.Teacher> teachers=Catalog.byBatch(this,batch);
            int lectures=0;for(Catalog.Teacher t:teachers)lectures+=t.count();
            LinearLayout card=Ui.card(this);
            card.setBackground(Ui.gradient(this,"2027".equals(batch)?0xff164C61:0xff224277,Ui.PANEL,18));
            card.addView(Ui.txt(this,"دفعة "+batch,21,Ui.WHITE,true));
            card.addView(Ui.txt(this,teachers.size()+" مدرس • "+lectures+" محاضرة مفهرسة",
                12,Ui.MUTED,false),Ui.top(this,-1,-2,8));
            TextView explore=Ui.txt(this,"عرض المدرسين  ←",13,Ui.MINT,true);
            card.addView(explore,Ui.top(this,-1,-2,15));
            cohortRow.addView(card,quickParams());
            card.setOnClickListener(v->{selectedBatch=batch;subjectFilter="";show("teachers");});
        }
        content.addView(cohortRow);

        sectionHeader("أساتذتنا","عرض الجميع",()->show("teachers"));
        HorizontalScrollView slider=new HorizontalScrollView(this);
        slider.setHorizontalScrollBarEnabled(false);
        LinearLayout teachersLine=Ui.row(this);
        List<Catalog.Teacher> teachers=Catalog.byBatch(this,selectedBatch);
        for(int i=0;i<Math.min(teachers.size(),8);i++){
            Catalog.Teacher teacher=teachers.get(i);
            LinearLayout card=Ui.card(this);card.setGravity(Gravity.CENTER);
            card.addView(Ui.portrait(this,teacher.image,teacher.name,78),Ui.lp(this,-1,83));
            TextView name=Ui.txt(this,teacher.name,14,Ui.WHITE,true);
            name.setGravity(Gravity.CENTER);
            card.addView(name,Ui.top(this,-1,28,8));
            TextView subject=Ui.txt(this,teacher.subject,12,Ui.MINT,false);
            subject.setGravity(Gravity.CENTER);
            card.addView(subject,Ui.lp(this,-1,25));
            LinearLayout.LayoutParams p=Ui.lp(this,140,163);
            p.leftMargin=d(9);
            teachersLine.addView(card,p);
            card.setOnClickListener(v->openTeacher(teacher.index));
        }
        slider.addView(teachersLine);
        content.addView(slider,margins(-1,171,6));

        sectionHeader("المواد الدراسية","الكل",()->show("teachers"));
        String[] subjects={"الرياضيات","الفيزياء","الكيمياء","الأحياء",
            "اللغة العربية","اللغة الإنكليزية","التربية الإسلامية"};
        String[] symbols={"∑","Φ","⚗","DNA","ع","En","✧"};
        for(int i=0;i<subjects.length;i+=2){
            LinearLayout row=Ui.row(this);
            for(int j=i;j<Math.min(i+2,subjects.length);j++){
                final String subject=subjects[j];
                LinearLayout card=Ui.card(this);
                TextView symbol=Ui.txt(this,symbols[j],23,Ui.MINT,true);
                card.addView(symbol,Ui.lp(this,-1,32));
                card.addView(Ui.txt(this,subject,14,Ui.WHITE,true),Ui.top(this,-1,-2,4));
                int count=0;for(Catalog.Teacher t:teachers)if(t.subject.equals(subject))count++;
                card.addView(Ui.txt(this,count+" مدرس",12,Ui.MUTED,false),Ui.top(this,-1,-2,4));
                LinearLayout.LayoutParams p=quickParams();row.addView(card,p);
                card.setOnClickListener(v->{subjectFilter=subject;show("teachers");});
            }
            content.addView(row,Ui.top(this,-1,-2,6));
        }
        TextView sourceNote=Ui.txt(this,"قد تتطلب بعض محاضرات المزودين اشتراكاً، وتظهر حالة توفرها داخل كل محاضرة.",
            11,Ui.MUTED,false);
        content.addView(sourceNote,Ui.top(this,-1,-2,19));
    }
    private LinearLayout.LayoutParams quickParams(){
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-2,1f);
        p.setMargins(d(4),0,d(4),0);
        return p;
    }
    private LinearLayout quickTile(String symbol,String title,String description,Runnable action){
        LinearLayout tile=Ui.card(this);
        TextView marker=Ui.txt(this,symbol,26,Ui.MINT,true);
        tile.addView(marker,Ui.lp(this,-1,39));
        tile.addView(Ui.txt(this,title,17,Ui.WHITE,true));
        tile.addView(Ui.txt(this,description,12,Ui.MUTED,false),Ui.top(this,-1,-2,6));
        tile.setMinimumHeight(d(128));
        tile.setContentDescription(title);
        tile.setOnClickListener(v->action.run());
        return tile;
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
        if("2026".equals(selectedBatch)){
            int missing=0;
            for(Catalog.Teacher teacher:batchTeachers)missing+=teacher.count()-teacher.ready();
            if(missing>0)addMuted(content,"تنبيه: "+missing+" سجل محاضرة بلا رابط مفهرس في دفعة 2026.");
        }
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
        LinearLayout hero=Ui.card(this);hero.setBackground(Ui.gradient(this,0xff244776,Ui.PANEL,20));
        LinearLayout top=Ui.row(this);top.addView(Ui.portrait(this,t.image,t.name,98),Ui.lp(this,100,105));
        LinearLayout right=Ui.col(this);right.setPadding(d(15),0,d(8),0);
        right.addView(Ui.txt(this,"الأستاذ "+t.name,20,Ui.WHITE,true));
        right.addView(Ui.txt(this,t.subject,14,Ui.MINT,true));
        right.addView(Ui.txt(this,"دفعة "+t.batch,12,Ui.MUTED,false));
        top.addView(right,new LinearLayout.LayoutParams(0,-2,1));hero.addView(top);
        space(hero,12);
        hero.addView(Ui.txt(this,t.chapters.size()+" أقسام     •     "+t.count()+" محاضرة     •     "+t.ready()+" لها روابط (التشغيل غير مضمون)",12,Ui.GOLD,true));
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
        LinearLayout panel=Ui.card(this);panel.setBackground(Ui.gradient(this,0xff203E6B,Ui.PANEL,18));
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
            ref.lesson.access.equals("unverified")?"مشغّل مضمن — التشغيل غير مؤكّد":"رابط موجود — التشغيل غير مفحوص";
        text.addView(Ui.txt(this,playbackLabel,11,done?Ui.MINT:Ui.MUTED,false),Ui.top(this,-1,-2,4));
        line.addView(text,new LinearLayout.LayoutParams(0,-2,1));
        TextView favorite=Ui.txt(this,fav?"★":"☆",24,fav?Ui.GOLD:Ui.MUTED,true);favorite.setGravity(Gravity.CENTER);
        line.addView(favorite,Ui.lp(this,30,44));
        favorite.setContentDescription(fav?"إزالة من المفضلة":"إضافة إلى المفضلة");
        favorite.setOnClickListener(v->{
            boolean value=!prefs.getBoolean("fav_"+id,false);
            prefs.edit().putBoolean("fav_"+id,value).apply();
            favorite.setText(value?"★":"☆");
            favorite.setTextColor(value?Ui.GOLD:Ui.MUTED);
            favorite.setContentDescription(value?"إزالة من المفضلة":"إضافة إلى المفضلة");
            if(page.equals("library")){rememberScroll();render();}
        });
        TextView finished=Ui.txt(this,done?"✓":"○",21,done?Ui.MINT:Ui.MUTED,true);finished.setGravity(Gravity.CENTER);
        line.addView(finished,Ui.lp(this,31,44));
        finished.setContentDescription(done?"إلغاء اكتمال المحاضرة":"تحديد المحاضرة كمكتملة");
        finished.setOnClickListener(v->{
            boolean value=!prefs.getBoolean("done_"+id,false);
            prefs.edit().putBoolean("done_"+id,value).apply();
            finished.setText(value?"✓":"○");
            finished.setTextColor(value?Ui.MINT:Ui.MUTED);
            finished.setContentDescription(value?"إلغاء اكتمال المحاضرة":"تحديد المحاضرة كمكتملة");
            if(page.equals("library")){rememberScroll();render();}
        });
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
        // Render the first page only. Subsequent pages are appended in place so
        // a large collection stays usable without losing the student's scroll.
        LinearLayout items=Ui.col(this);
        content.addView(items);
        final int[] shown={Math.min(100,selected.size())};
        for(int i=0;i<shown[0];i++)lessonRow(items,selected.get(i));
        if(shown[0]<selected.size()){
            TextView loadMore=Ui.btn(this,"عرض المزيد ("+(selected.size()-shown[0])+" متبقية)",false);
            content.addView(loadMore,Ui.top(this,-1,48,12));
            loadMore.setOnClickListener(v->{
                int nextLimit=Math.min(shown[0]+100,selected.size());
                for(int i=shown[0];i<nextLimit;i++)lessonRow(items,selected.get(i));
                shown[0]=nextLimit;
                if(shown[0]>=selected.size())loadMore.setVisibility(View.GONE);
                else loadMore.setText("عرض المزيد ("+(selected.size()-shown[0])+" متبقية)");
            });
        }
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

    // 2025 is a source-verified image gallery prototype, not a fabricated exam dataset.
    // Aselatna currently shows sixth-preparatory exams only through 2021.
    private void exams(){
        addTitle(content,"وزاريات سادسيون",24);
        addMuted(content,"أوراق وزارية 2025 — نماذج علمي وأدبي من ملازمنا • تجربة");

        LinearLayout years=Ui.card(this);years.setBackground(Ui.gradient(this,0xff203D65,Ui.PANEL,17));
        years.addView(Ui.txt(this,"2025",27,Ui.GOLD,true));
        addMuted(years,"الاعتماد على السنة المكتوبة في الورقة، مو تاريخ نشر المنشور.");
        content.addView(years,margins(-1,-2,12));

        LinearLayout branches=Ui.row(this);
        String[] branchIds={"scientific","literary"};
        String[] branchLabels={"السادس العلمي","السادس الأدبي"};
        for(int i=0;i<branchIds.length;i++){
            final String next=branchIds[i];
            TextView b=Ui.btn(this,branchLabels[i],examBranch.equals(next));
            LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,d(46),1f);
            p.setMargins(d(3),0,d(3),0);branches.addView(b,p);
            b.setOnClickListener(v->{examBranch=next;examSubject="الرياضيات";show("exams");});
        }
        content.addView(branches,margins(-1,-2,11));
        addTitle(content,"اختر المادة",18);

        String[] subjects=examBranch.equals("scientific")?
            new String[]{"الرياضيات","الفيزياء","الكيمياء","الأحياء","اللغة العربية","اللغة الإنكليزية","التربية الإسلامية"}:
            new String[]{"الرياضيات","التاريخ","الجغرافية","الاقتصاد","اللغة العربية","اللغة الإنكليزية","التربية الإسلامية"};
        for(int i=0;i<subjects.length;i+=2){
            LinearLayout row=Ui.row(this);
            for(int j=i;j<Math.min(i+2,subjects.length);j++){
                final String name=subjects[j];
                TextView b=Ui.btn(this,name,examSubject.equals(name));
                LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,d(44),1f);
                p.setMargins(d(3),d(3),d(3),d(3));row.addView(b,p);
                b.setOnClickListener(v->{examSubject=name;show("exams");});
            }
            content.addView(row);
        }

        addTitle(content,"اختر الدور",18);
        HorizontalScrollView roundsScroll=new HorizontalScrollView(this);
        roundsScroll.setHorizontalScrollBarEnabled(false);
        LinearLayout rounds=Ui.row(this);
        for(String role:new String[]{"الدور الأول","الدور الثاني","الدور الثالث","التمهيدي"}){
            TextView b=Ui.btn(this,role,examRound.equals(role));
            LinearLayout.LayoutParams p=Ui.lp(this,115,43);p.setMargins(d(3),0,d(3),0);
            rounds.addView(b,p);
            b.setOnClickListener(v->{examRound=role;show("exams");});
        }
        roundsScroll.addView(rounds);
        content.addView(roundsScroll,margins(-1,-2,8));

        List<ExamCatalog.Paper> papers=ExamCatalog.select(this,2025,examBranch,examSubject,examRound);
        addTitle(content,"صور الأوراق",19);
        if(papers.isEmpty()){
            LinearLayout empty=Ui.card(this);
            empty.addView(Ui.txt(this,"لا توجد ورقة 2025 مؤكدة بهذا التصنيف",17,Ui.GOLD,true));
            addMuted(empty,"المتوفر حالياً من ملازمنا: رياضيات وعربي 2025 الدور الأول لكل فرع. بقية المواد والأدوار تنتظر فحص المصدر.");
            empty.addView(Ui.txt(this,"هذا نموذج شكل عرض الصورة عند إضافتها:",13,Ui.MINT,true),
                Ui.top(this,-1,-2,13));
            content.addView(empty,margins(-1,-2,9));
            examPlaceholder();
        }else{
            for(ExamCatalog.Paper paper:papers){
                LinearLayout card=Ui.card(this);
                card.addView(Ui.txt(this,paper.title,18,Ui.WHITE,true));
                addMuted(card,paper.urls.isEmpty()?"المصدر: ملازمنا • الورقة الأصلية داخل التطبيق":paper.urls.size()+" صفحات • "+paper.round+" • "+paper.subject);
                TextView open=Ui.btn(this,paper.urls.isEmpty()?"عرض ورقة ملازمنا":"عرض صور الورقة",true);
                card.addView(open,Ui.top(this,-1,46,10));
                open.setOnClickListener(v->displayExamPaper(paper));
                content.addView(card,margins(-1,-2,11));
            }
        }
        LinearLayout src=Ui.card(this);
        src.addView(Ui.txt(this,"المصادر",16,Ui.WHITE,true));
        addMuted(src,"أوراق التجربة من ملازمنا مع الإبقاء على مصدرها وعلاماتها. ويمكنك زيارة فهرس أسئلتنا للمقارنة.");
        TextView link=Ui.btn(this,"زيارة قسم "+(examBranch.equals("scientific")?"العلمي":"الأدبي"),false);
        src.addView(link,Ui.top(this,-1,42,10));
        link.setOnClickListener(v->{
            String url=examBranch.equals("scientific")?
                "https://www.aselatna.com/search/label/%D8%A7%D9%84%D8%B3%D8%A7%D8%AF%D8%B3%20%D8%A7%D9%84%D8%B9%D9%84%D9%85%D9%8A?m=1":
                "https://www.aselatna.com/search/label/%D8%A7%D9%84%D8%B3%D8%A7%D8%AF%D8%B3%20%D8%A7%D9%84%D8%A7%D8%AF%D8%A8%D9%8A?m=1";
            startActivity(new Intent(Intent.ACTION_VIEW,android.net.Uri.parse(url)));
        });
        content.addView(src,margins(-1,-2,15));
    }

    private void examPlaceholder(){
        android.widget.FrameLayout sheet=new android.widget.FrameLayout(this);
        sheet.setBackground(Ui.round(this,0xfff4f5ef,16));
        LinearLayout copy=Ui.col(this);Ui.pad(this,copy,14,15);
        copy.addView(Ui.txt(this,"سادسيون | نموذج معاينة المعرض",17,0xff15383b,true));
        TextView disclaimer=Ui.txt(this,"مكان صورة ورقة الامتحان الأصلية",16,0xff253b40,true);
        disclaimer.setGravity(Gravity.CENTER);copy.addView(disclaimer,new LinearLayout.LayoutParams(-1,0,1f));
        TextView footer=Ui.txt(this,"لا تحتوي هذه المعاينة على أسئلة وزارية فعلية",12,0xff637276,false);
        footer.setGravity(Gravity.CENTER);
        copy.addView(footer);
        sheet.addView(copy,new android.widget.FrameLayout.LayoutParams(-1,-1));
        TextView wm=Ui.txt(this,"سادسيون",45,0xff14745c,true);
        wm.setGravity(Gravity.CENTER);wm.setAlpha(0.13f);
        sheet.addView(wm,new android.widget.FrameLayout.LayoutParams(-1,-1));
        content.addView(sheet,margins(-1,285,9));
    }

    private void displayExamPaper(ExamCatalog.Paper paper){
        if(paper.urls.isEmpty()){
            if(paper.source.startsWith("https://mlazemna.com/")){
                Intent intent=new Intent(this,SourcePaperActivity.class);
                intent.putExtra("source_url",paper.source);
                intent.putExtra("source_title",paper.title);
                startActivity(intent);
            }else Toast.makeText(this,"رابط المصدر غير متاح",Toast.LENGTH_SHORT).show();
            return;
        }
        android.widget.FrameLayout paperPage=new android.widget.FrameLayout(this);
        paperPage.setBackgroundColor(0xffffffff);
        android.widget.ImageView image=new android.widget.ImageView(this);
        image.setScaleType(android.widget.ImageView.ScaleType.FIT_CENTER);
        paperPage.addView(image,new android.widget.FrameLayout.LayoutParams(-1,-1));
        TextView watermark=Ui.txt(this,"سادسيون",42,0xff11795e,true);
        watermark.setGravity(Gravity.CENTER);watermark.setAlpha(0.14f);
        paperPage.addView(watermark,new android.widget.FrameLayout.LayoutParams(-1,-1));
        // Photos are displayed unaltered with a reversible watermark overlay, not
        // rewritten or re-rendered: equations and printed question text are preserved.
        final int[] position={0};
        final TextView info=Ui.txt(this,"",14,Ui.GOLD,true);
        final Runnable load=()->{
            String url=paper.urls.get(position[0]);
            info.setText("صفحة "+(position[0]+1)+" من "+paper.urls.size());
            image.setImageDrawable(null);
            new Thread(()->{
                java.net.HttpURLConnection conn=null;
                try{
                    conn=(java.net.HttpURLConnection)new java.net.URL(url).openConnection();
                    conn.setConnectTimeout(7000);conn.setReadTimeout(9000);
                    android.graphics.Bitmap bmp=android.graphics.BitmapFactory.decodeStream(conn.getInputStream());
                    if(bmp!=null)image.post(()->image.setImageBitmap(bmp));
                }catch(Exception e){image.post(()->android.widget.Toast.makeText(this,"تعذّر تحميل صورة الورقة",Toast.LENGTH_SHORT).show());}
                finally{if(conn!=null)conn.disconnect();}
            }).start();
        };
        LinearLayout viewer=Ui.col(this);
        Ui.pad(this,viewer,10,10);
        viewer.addView(info);
        viewer.addView(paperPage,Ui.top(this,-1,460,10));
        LinearLayout buttons=Ui.row(this);
        TextView prev=Ui.btn(this,"السابقة",false),next=Ui.btn(this,"التالية",true);
        buttons.addView(prev,new LinearLayout.LayoutParams(0,d(46),1f));
        buttons.addView(next,new LinearLayout.LayoutParams(0,d(46),1f));
        prev.setOnClickListener(v->{if(position[0]>0){position[0]--;load.run();}});
        next.setOnClickListener(v->{if(position[0]<paper.urls.size()-1){position[0]++;load.run();}});
        viewer.addView(buttons,Ui.top(this,-1,-2,8));
        new AlertDialog.Builder(this).setTitle(paper.title).setView(viewer)
            .setPositiveButton("إغلاق",null).show();
        load.run();
    }

    private void tests(){
        addTitle(content,"اختبارات سادسيون",24);
        TextView archive=Ui.btn(this,"افتح الأسئلة الوزارية المصورة",true);
        content.addView(archive,margins(-1,46,8));
        archive.setOnClickListener(v->show("exams"));
        addMuted(content,"اختبارات تدريبية أصلية وليست أسئلة وزارية رسمية. تعمل بدون إنترنت.");

        LinearLayout branches=Ui.row(this);
        String[] ids={"scientific","literary"};
        String[] titles={"السادس العلمي","السادس الأدبي"};
        for(int i=0;i<ids.length;i++){
            final String choice=ids[i];
            TextView b=Ui.btn(this,titles[i],choice.equals(practiceBranch));
            LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,d(46),1f);
            p.setMargins(d(4),d(4),d(4),d(4));
            branches.addView(b,p);
            b.setOnClickListener(v->{if(!choice.equals(practiceBranch)){
                practiceBranch=choice;questionIndex=0;quizScore=0;show("tests");
            }});
        }
        content.addView(branches,margins(-1,-2,9));

        List<PracticeBank.Question> bank=PracticeBank.forBranch(practiceBranch);
        if(bank.isEmpty()){
            addMuted(content,"لا توجد أسئلة تدريبية متاحة");return;
        }
        if(questionIndex>=bank.size()){
            int score=quizScore;
            String key="practice_best_"+practiceBranch;
            int best=Math.max(score,prefs.getInt(key,0));
            prefs.edit().putInt(key,best).apply();
            LinearLayout result=Ui.card(this);
            result.setBackground(Ui.gradient(this,0xff254B79,Ui.PANEL,18));
            result.addView(Ui.txt(this,"النتيجة: "+score+" من "+bank.size(),25,Ui.GOLD,true));
            addMuted(result,"أفضل نتيجة محفوظة على جهازك: "+best+" من "+bank.size());
            TextView retry=Ui.btn(this,"ابدأ اختباراً جديداً",true);
            result.addView(retry,Ui.top(this,-1,48,18));
            retry.setOnClickListener(v->{questionIndex=0;quizScore=0;show("tests");});
            content.addView(result,margins(-1,-2,15));
            return;
        }
        PracticeBank.Question q=bank.get(questionIndex);
        LinearLayout card=Ui.card(this);
        card.setBackground(Ui.gradient(this,0xff213C63,Ui.PANEL,18));
        card.addView(Ui.txt(this,"السؤال "+(questionIndex+1)+" من "+bank.size(),14,Ui.GOLD,true));
        card.addView(Ui.txt(this,q.text,20,Ui.WHITE,true),Ui.top(this,-1,-2,11));
        content.addView(card,margins(-1,-2,13));
        for(int i=0;i<q.options.length;i++){
            final int selected=i;
            TextView option=Ui.btn(this,q.options[i],false);
            option.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);
            content.addView(option,margins(-1,52,9));
            option.setOnClickListener(v->{
                boolean right=selected==q.answer;
                if(right)quizScore++;
                String message=(right?"إجابة صحيحة.":"الإجابة الصحيحة: "+q.options[q.answer])
                    +"\n\nالشرح: "+q.explanation;
                new AlertDialog.Builder(this)
                    .setTitle(right?"صحيح":"راجع الحل")
                    .setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton("السؤال التالي",(dialog,w)->{questionIndex++;show("tests");})
                    .show();
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
        LinearLayout backups=Ui.card(this);
        backups.addView(Ui.txt(this,"النسخ الاحتياطي المحلي",18,Ui.WHITE,true));
        addMuted(backups,"صدّر مفضلاتك وبطاقات ملاحظاتك وتقدمك إلى ملف JSON تختاره بنفسك، أو استعدها على جهاز آخر. احتفظ بالملف بمكان خاص لأن الملاحظات قد تكون شخصية.");
        TextView export=Ui.btn(this,"تصدير نسخة احتياطية",true);
        backups.addView(export,Ui.top(this,-1,45,12));
        export.setOnClickListener(v->{
            Intent action=new Intent(Intent.ACTION_CREATE_DOCUMENT);
            action.addCategory(Intent.CATEGORY_OPENABLE);
            action.setType("application/json");
            action.putExtra(Intent.EXTRA_TITLE,"sadsiyoon-backup.json");
            startActivityForResult(action,BACKUP_EXPORT_REQUEST);
        });
        TextView restore=Ui.btn(this,"استيراد نسخة احتياطية",false);
        backups.addView(restore,Ui.top(this,-1,45,8));
        restore.setOnClickListener(v->new AlertDialog.Builder(this)
            .setTitle("استيراد تقدم الدراسة")
            .setMessage("سيتم دمج الملاحظات والتقدم الموجود في الملف مع بيانات الجهاز. المفاتيح المتطابقة ستتحدث. اختر ملف سادسيون موثوقاً فقط.")
            .setNegativeButton("إلغاء",null)
            .setPositiveButton("اختيار ملف",(dialog,which)->{
                Intent picker=new Intent(Intent.ACTION_OPEN_DOCUMENT);
                picker.addCategory(Intent.CATEGORY_OPENABLE);
                picker.setType("application/json");
                startActivityForResult(picker,BACKUP_IMPORT_REQUEST);
            }).show());
        content.addView(backups,margins(-1,-2,12));
        TextView reset=Ui.btn(this,"مسح تقدم المشاهدة والمفضلة",false);
        content.addView(reset,margins(-1,48,15));
        reset.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("تأكيد مسح التقدم")
            .setMessage("سيتم مسح المفضلة وعلامات الإكمال ومواضع المشاهدة المحفوظة على هذا الجهاز.")
            .setNegativeButton("إلغاء",null).setPositiveButton("مسح",(dialog,w)->{
                SharedPreferences.Editor edit=prefs.edit();
                for(String key:new HashSet<>(prefs.getAll().keySet()))
                    if(key.startsWith("done_")||key.startsWith("fav_")||key.startsWith("position_")||key.startsWith("note_")||
                       key.equals("last_t")||key.equals("last_c")||key.equals("last_l"))
                        edit.remove(key);
                edit.apply();show("more");
            }).show());
        addMuted(content,"سادسيون V1.6.0 Beta — نسخة واجهات تجريبية مستقلة.");
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
    @Override protected void onActivityResult(int request,int result,Intent data){
        super.onActivityResult(request,result,data);
        if(result!=RESULT_OK||data==null||data.getData()==null)return;
        try{
            if(request==BACKUP_EXPORT_REQUEST){
                try(java.io.OutputStream stream=getContentResolver().openOutputStream(data.getData())){
                    if(stream==null)throw new java.io.IOException("Storage location is unavailable");
                    int exported=BackupUtils.exportTo(this,stream);
                    Toast.makeText(this,"تم تصدير "+exported+" عنصر من بياناتك",Toast.LENGTH_LONG).show();
                }
            }else if(request==BACKUP_IMPORT_REQUEST){
                int imported;
                try(java.io.InputStream stream=getContentResolver().openInputStream(data.getData())){
                    if(stream==null)throw new java.io.IOException("Cannot read selected file");
                    imported=BackupUtils.importFrom(this,stream);
                }
                Toast.makeText(this,"تم استيراد "+imported+" عنصر بنجاح",Toast.LENGTH_LONG).show();
                render();
            }
        }catch(Exception error){
            android.util.Log.e("Sadsiyoon","Backup operation failed",error);
            new AlertDialog.Builder(this).setTitle("تعذر تنفيذ النسخ الاحتياطي")
                .setMessage("تأكد من اختيار ملف JSON صالح ومساحة تخزين متاحة، ثم حاول مرة أخرى.")
                .setPositiveButton("حسناً",null).show();
        }
    }
    private void back(){
        rememberScroll();
        if(!navigationHistory.isEmpty())page=navigationHistory.removeLast();
        else if(page.equals("chapter"))page="teacher";
        else if(page.equals("teacher"))page="teachers";
        else page="home";
        render();
    }
    @Override public void onBackPressed(){
        if(page.equals("home"))new AlertDialog.Builder(this).setTitle("الخروج من سادسيون")
            .setMessage("هل تريد إغلاق التطبيق؟").setNegativeButton("إلغاء",null)
            .setPositiveButton("خروج",(d,w)->finish()).show();
        else back();
    }
    @Override protected void onResume(){
        super.onResume();
        if(content!=null&&(page.equals("home")||page.equals("library")||page.equals("chapter"))){
            rememberScroll();render();
        }
    }
}
