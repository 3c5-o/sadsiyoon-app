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
    // User-supplied external teacher portraits for the 2026 and 2027 directories.
    // Two spelling variants were matched to existing catalog entries:
    // "سجاد العگيلي" -> "ساجد العكيلي" and "هشام الغرباوي" -> "هاشم الغرباوي".
    // Confirm these two associations with the user before a public production release.
    private static final String[] IMAGES={
        "https://f.top4top.io/p_3938pnnw51.jpg", // حسين محمد
        "https://j.top4top.io/p_3938gg6om1.jpg", // حيدر عبدالائمة
        "https://d.top4top.io/p_3938suhes1.jpg", // سجاد العبيدي
        "https://h.top4top.io/p_3938lmygb1.jpg", // هاشم الغرباوي
        "https://j.top4top.io/p_3938l5zla1.jpg", // علي السوداني
        "https://j.top4top.io/p_3938dihl31.jpg", // رفل الزبيدي
        "https://l.top4top.io/p_3938p9alv1.jpg", // عباس الدراجي
        "https://d.top4top.io/p_3938josxy1.jpg", // مهند السوداني
        "https://a.top4top.io/p_3938yv4y11.jpg", // نورس الموسوي
        "", // محمد العنزي
        "https://d.top4top.io/p_3938pjd2e1.jpg", // أزل سلوان
        "", // يوسف الربيعي
        "", // أحمد الجاف
        "", // حسن عبدالكاظم
        "https://c.top4top.io/p_3938su4u21.jpg", // محمد قاسم
        "https://i.top4top.io/p_3938bfw3s1.jpg", // هشام المعموري
        "https://h.top4top.io/p_3938f3g7s1.jpg", // جعفر الحسني
        "https://l.top4top.io/p_3938ii0nw1.jpg", // ساجد العكيلي
        "https://edson.my/assets/hjzpw.jpg" // حسين الهاشمي
    };
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
                    try{
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
                    }catch(Exception fileError){
                        android.util.Log.e("Sadsiyoon","Skipping invalid 2026 catalog "+filename,fileError);
                    }
                }
            }
        }catch(Exception e){android.util.Log.e("Sadsiyoon","Cannot list bundled catalog files",e);}
        // Stable ordering: keep 2026 IDs 0..18 and Haider 2027 ID 19.
        // Import subsequent 2027 teachers by lexicographic asset filename.
        try{
            String[] batchFiles=context.getAssets().list("catalog_2027");
            if(batchFiles!=null){
                Arrays.sort(batchFiles);
                for(String name:batchFiles){
                    try{
                    if(!name.endsWith(".json"))continue;
                    InputStream stream=context.getAssets().open("catalog_2027/"+name);
                    ByteArrayOutputStream buffer=new ByteArrayOutputStream();
                    try{
                        byte[] part=new byte[8192];int n;
                        while((n=stream.read(part))!=-1)buffer.write(part,0,n);
                    }finally{stream.close();}
                    JSONObject doc=new JSONObject(new String(buffer.toByteArray(),StandardCharsets.UTF_8));
                    if(!"2027".equals(doc.optString("batch")))continue;
                    String teacherName=doc.optString("teacher","").trim();
                    if(teacherName.isEmpty())continue;
                    String imageUrl="";
                    for(int i=0;i<NAMES.length;i++)if(NAMES[i].equals(teacherName)){imageUrl=IMAGES[i];break;}
                    Teacher teacher=new Teacher(out.size(),teacherName,
                            doc.optString("subject",""),imageUrl,"2027");
                    JSONArray chapters=doc.optJSONArray("chapters");
                    if(chapters==null)continue;
                    for(int c=0;c<chapters.length();c++){
                        JSONObject chapterJson=chapters.optJSONObject(c);if(chapterJson==null)continue;
                        Chapter chapter=new Chapter(chapterJson.optString("title","الفصل "+(c+1)));
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
                    }catch(Exception fileError){
                        android.util.Log.e("Sadsiyoon","Skipping invalid 2027 catalog "+name,fileError);
                    }
                }
            }
        }catch(Exception e){android.util.Log.e("Sadsiyoon","Cannot list 2027 course catalogs",e);}
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
