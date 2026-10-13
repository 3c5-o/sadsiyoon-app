package com.sadsiyoon.app;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class Catalog {
    private Catalog(){}
    private static final String[] NAMES={
        "حسين محمد","حيدر عبدالائمة","سجاد العبيدي","هاشم الغرباوي",
        "علي السوداني","رفل الزبيدي","عباس الدراجي","مهند السوداني","نورس الموسوي",
        "محمد العنزي","أزل سلوان","يوسف الربيعي","أحمد الجاف","حسن عبدالكاظم",
        "محمد قاسم","هشام المعموري","جعفر الحسني","ساجد العكيلي","حسين الهاشمي"};
    private static final String[] SUBJECTS={
        "الفيزياء","الرياضيات","اللغة الإنكليزية","الكيمياء","الفيزياء","اللغة العربية",
        "الرياضيات","الكيمياء","الأحياء","الأحياء","اللغة الإنكليزية","الكيمياء",
        "الأحياء","الفيزياء","الرياضيات","اللغة العربية","الأحياء","التربية الإسلامية","الكيمياء"};
    private static final String[] IMAGES={
        "https://i.ibb.co/9mcCChnr/upload-1769168321.jpg",
        "https://edson.my/assets/photo_5397877939879218921_y.jpg",
        "https://i.ibb.co/PGntDJPc/upload-1769166009.jpg",
        "https://i.ibb.co/35xHB9bs/upload-1769165990.jpg","",
        "https://i.ibb.co/7NrJ92Xv/upload-1769166615.jpg",
        "https://yt3.googleusercontent.com/l0QnUF1_R9JLkyK-Zeao8qA5yfYKZndunAVkGua2EmuPIQyAXTXVdM_MiqxZx0m02B1Ucjly=s900-c-k-c0x00ffffff-no-rj",
        "",
        "https://yt3.googleusercontent.com/B_86-v0AFl5PN_mJXykCJ-pyzQetoKg8DL5wISXMt5IDs8yyE8dBbzZCtyXdo57GjBM54QTDlA=s900-c-k-c0x00ffffff-no-rj",
        "",
        "https://yt3.googleusercontent.com/8HQeUmCfq8xI4YzMJiVtMX_rflqNOchtXvrICB1Erbfk5avv3K4HaqtfVEy3pEw5txM4_xJl=s900-c-k-c0x00ffffff-no-rj",
        "","","","","","","https://d157djim0keagl.cloudfront.net/76c26858-4c62-47a5-ba8e-2ac359420814.webp",
        "https://edson.my/assets/hjzpw.jpg"};
    private static List<Teacher> teachers;
    public static final class Teacher {
        public final int index; public final String name,subject,image,batch;
        public final List<Chapter> chapters=new ArrayList<>();
        private Teacher(int i){this(i,NAMES[i],SUBJECTS[i],IMAGES[i],"2026");}
        private Teacher(int i,String n,String s,String imageUrl,String batchId){
            index=i;name=n;subject=s;image=imageUrl;batch=batchId;
        }
        public int count(){int n=0;for(Chapter c:chapters)n+=c.lessons.size();return n;}
        public int ready(){int n=0;for(Chapter c:chapters)for(Lesson l:c.lessons)if(l.available())n++;return n;}
    }
    public static final class Chapter {
        public final String title; public final List<Lesson> lessons=new ArrayList<>();
        private Chapter(String t){title=t;}
    }
    public static final class Lesson {
        public final String title,url,type,access;
        private Lesson(String t,String u){this(t,u,"standard");}
        private Lesson(String t,String u,String a){title=t;url=u;access=a;type=kind(u);}
        public boolean available(){return !url.isEmpty() && !type.equals("none");}
        private static String kind(String url){
            if(url.isEmpty())return "none";
            String lower=url.toLowerCase(java.util.Locale.ROOT);
            if(lower.contains("/embed/")||lower.contains("videoembed")||lower.contains("video_ext.php"))return "embed";
            if(lower.contains(".m3u8"))return "hls";
            if(lower.contains(".mp4"))return "mp4";
            return "none";
        }
    }
    public static final class Ref {
        public final Teacher teacher;public final Chapter chapter;public final Lesson lesson;
        public final int ti,ci,li;
        private Ref(Teacher t,Chapter c,Lesson l,int tid,int cid,int lid){
            teacher=t;chapter=c;lesson=l;ti=tid;ci=cid;li=lid;
        }
        public String key(){return "t"+ti+"_c"+ci+"_l"+li;}
    }
    private static String cleanUrl(String value){
        if(value==null || !value.startsWith("https://"))return "";
        String v=value.trim();
        if(v.contains("otp=")||v.contains("playbackInfo=")||v.contains("lecture_id="))return "";
        if(v.contains("iframe.mediadelivery.net")&&v.contains(".m3u8"))return "";
        if(v.matches(".*\\.m3u(?:\\?.*)?$"))return "";
        return v;
    }
    public static synchronized void initialize(Context context){
        if(teachers!=null)return;
        ArrayList<Teacher> out=new ArrayList<>();
        for(int i=0;i<NAMES.length;i++)out.add(new Teacher(i));
        try{
            String[] filenames=context.getAssets().list("catalog");
            if(filenames!=null){
                Arrays.sort(filenames);
                for(String filename:filenames){
                    if(!filename.endsWith(".json")||filename.length()<2)continue;
                    int number;
                    try{number=Integer.parseInt(filename.substring(0,2));}catch(Exception ignored){continue;}
                    if(number<1||number>out.size())continue;
                    Teacher teacher=out.get(number-1);
                    InputStream in=context.getAssets().open("catalog/"+filename);
                    ByteArrayOutputStream buffer=new ByteArrayOutputStream();
                    byte[] chunk=new byte[8192];int read;
                    while((read=in.read(chunk))!=-1)buffer.write(chunk,0,read);
                    in.close();
                    JSONObject document=new JSONObject(new String(buffer.toByteArray(),StandardCharsets.UTF_8));
                    JSONArray sections=document.optJSONArray("chapters");
                    if(sections==null)continue;
                    for(int c=0;c<sections.length();c++){
                        JSONObject raw=sections.optJSONObject(c);if(raw==null)continue;
                        String title=raw.optString("title",raw.optString("chapter_title",raw.optString("name","فصل"))).trim();
                        if(title.isEmpty())title="فصل";
                        Chapter chapter=null;
                        for(Chapter old:teacher.chapters)if(old.title.equals(title)){chapter=old;break;}
                        if(chapter==null){chapter=new Chapter(title);teacher.chapters.add(chapter);}
                        JSONArray lectures=raw.optJSONArray("lessons");
                        if(lectures==null)lectures=raw.optJSONArray("lectures");
                        if(lectures==null)continue;
                        for(int j=0;j<lectures.length();j++){
                            JSONObject rawLesson=lectures.optJSONObject(j);if(rawLesson==null)continue;
                            String label=rawLesson.optString("title",rawLesson.optString("lecture_title","محاضرة")).trim();
                            String url=cleanUrl(rawLesson.optString("url",""));
                            if(label.isEmpty())continue;
                            boolean duplicated=false;
                            for(int k=0;k<chapter.lessons.size();k++){
                                Lesson old=chapter.lessons.get(k);
                                if((!url.isEmpty()&&url.equals(old.url)) || (label.equals(old.title)&&(url.isEmpty()||url.equals(old.url)))){
                                    duplicated=true;break;
                                }
                                if(label.equals(old.title)&&old.url.isEmpty()&&!url.isEmpty()){
                                    chapter.lessons.set(k,new Lesson(label,url));duplicated=true;break;
                                }
                            }
                            if(!duplicated)chapter.lessons.add(new Lesson(label,url));
                        }
                    }
                }
            }
        }catch(Exception e){android.util.Log.e("Sadsiyoon","Cannot load bundled catalog",e);}
        // 2027 is a separate catalog and retains all existing 2026 teacher IDs and user progress.
        try{
            java.io.InputStream stream=context.getAssets().open("catalog_2027/01-haider-abdul-imma.json");
            java.io.ByteArrayOutputStream buffer=new java.io.ByteArrayOutputStream();
            byte[] part=new byte[8192];int len;
            try{while((len=stream.read(part))!=-1)buffer.write(part,0,len);}
            finally{stream.close();}
            JSONObject doc=new JSONObject(new String(buffer.toByteArray(),StandardCharsets.UTF_8));
            if("2027".equals(doc.optString("batch"))){
                Teacher teacher=new Teacher(out.size(),doc.optString("teacher","حيدر عبدالائمة"),
                    doc.optString("subject","الرياضيات"),IMAGES[1],"2027");
                JSONArray chapters=doc.optJSONArray("chapters");
                if(chapters!=null)for(int c=0;c<chapters.length();c++){
                    JSONObject chapterJson=chapters.optJSONObject(c);if(chapterJson==null)continue;
                    Chapter chapter=new Chapter(chapterJson.optString("title","الفصل الأول"));
                    JSONArray episodes=chapterJson.optJSONArray("lessons");
                    if(episodes==null)continue;
                    for(int j=0;j<episodes.length();j++){
                        JSONObject item=episodes.optJSONObject(j);if(item==null)continue;
                        String url=cleanUrl(item.optString("url",""));
                        String label=item.optString("title","المحاضرة "+(j+1)).trim();
                        String access=item.optString("access","unverified");
                        if(!label.isEmpty())chapter.lessons.add(new Lesson(label,url,access));
                    }
                    teacher.chapters.add(chapter);
                }
                out.add(teacher);
            }
        }catch(Exception e){android.util.Log.e("Sadsiyoon","Cannot load 2027 course catalog",e);}
        teachers=Collections.unmodifiableList(out);
    }
    public static List<Teacher> teachers(Context c){initialize(c);return teachers;}
    public static List<Teacher> byBatch(Context c,String batch){
        initialize(c);ArrayList<Teacher> selected=new ArrayList<>();
        for(Teacher teacher:teachers)if(teacher.batch.equals(batch))selected.add(teacher);
        return Collections.unmodifiableList(selected);
    }
    public static Teacher teacher(Context c,int index){initialize(c);return index>=0&&index<teachers.size()?teachers.get(index):null;}
    public static Ref ref(Context ctx,int ti,int ci,int li){
        Teacher t=teacher(ctx,ti);
        if(t==null||ci<0||ci>=t.chapters.size())return null;
        Chapter c=t.chapters.get(ci);
        if(li<0||li>=c.lessons.size())return null;
        return new Ref(t,c,c.lessons.get(li),ti,ci,li);
    }
    public static List<Ref> all(Context c){
        initialize(c);ArrayList<Ref> refs=new ArrayList<>();
        for(Teacher t:teachers)for(int ci=0;ci<t.chapters.size();ci++)
            for(int li=0;li<t.chapters.get(ci).lessons.size();li++)
                refs.add(ref(c,t.index,ci,li));
        return refs;
    }
}
