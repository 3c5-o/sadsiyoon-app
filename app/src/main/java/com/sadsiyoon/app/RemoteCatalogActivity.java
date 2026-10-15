package com.sadsiyoon.app;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.ViewCompat;
import org.json.JSONArray;
import org.json.JSONObject;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

/** Published Supabase content, with safe offline cache. Bundled catalog remains unchanged. */
public final class RemoteCatalogActivity extends AppCompatActivity {
    private final ExecutorService workers=Executors.newSingleThreadExecutor();
    private final ArrayDeque<String> history=new ArrayDeque<>();
    private JSONArray records=new JSONArray();
    private LinearLayout root,content;
    private String currentParent="";
    private String notice="جارٍ التحقق من المحتوى المنشور...";
    private int dp(int p){return Ui.dp(this,p);}
    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        WindowCompat.setDecorFitsSystemWindows(getWindow(),false);
        records=readCache();
        render();refresh();
    }
    private JSONArray readCache(){
        File file=new File(getFilesDir(),"published_catalog_v1.json");
        if(!file.exists())return new JSONArray();
        try(FileInputStream in=new FileInputStream(file);ByteArrayOutputStream out=new ByteArrayOutputStream()){
            byte[] buf=new byte[8192];int n;
            while((n=in.read(buf))!=-1){if(out.size()>2*1024*1024)return new JSONArray();out.write(buf,0,n);}
            return new JSONArray(out.toString(StandardCharsets.UTF_8.name()));
        }catch(Exception ignored){return new JSONArray();}
    }
    private void writeCache(JSONArray rows){
        try(FileOutputStream out=openFileOutput("published_catalog_v1.json",MODE_PRIVATE)){
            out.write(rows.toString().getBytes(StandardCharsets.UTF_8));
        }catch(Exception ignored){}
    }
    private void refresh(){
        notice="جارٍ تحديث قائمة المحتوى...";
        workers.execute(()->{
            HttpURLConnection c=null;
            try {
                URL url=new URL(BuildConfig.SUPABASE_URL+
                    "/rest/v1/sadesion_content?select=*&status=eq.published&order=sort_index.asc,created_at.asc&limit=1000");
                c=(HttpURLConnection)url.openConnection();
                c.setConnectTimeout(10000);c.setReadTimeout(13000);
                c.setRequestProperty("apikey",BuildConfig.SUPABASE_PUBLISHABLE_KEY);
                c.setRequestProperty("Accept","application/json");
                if(c.getResponseCode()!=200)throw new IOException("HTTP "+c.getResponseCode());
                ByteArrayOutputStream out=new ByteArrayOutputStream();
                try(InputStream in=c.getInputStream()){
                    byte[] bytes=new byte[8192];int n;
                    while((n=in.read(bytes))!=-1){
                        if(out.size()+n>2*1024*1024)throw new IOException("large response");
                        out.write(bytes,0,n);
                    }
                }
                JSONArray updated=new JSONArray(out.toString(StandardCharsets.UTF_8.name()));
                writeCache(updated);
                runOnUiThread(()->{
                    records=updated;
                    notice="المحتوى المنشور • تحديث مباشر";
                    render();
                });
            }catch(Exception ex){
                runOnUiThread(()->{
                    notice=records.length()>0?
                        "تعذر تحديث المحتوى، يتم عرض آخر نسخة محفوظة.":
                        "لا يوجد محتوى منشور متاح حالياً. المحاضرات المحلية باقية في قسم المدرسين.";
                    render();
                });
            }finally{if(c!=null)c.disconnect();}
        });
    }
    private void render(){
        root=Ui.col(this);root.setBackgroundColor(Ui.BG);
        ViewCompat.setOnApplyWindowInsetsListener(root,(v,insets)->{
            Insets bars=insets.getInsets(WindowInsetsCompat.Type.systemBars());
            root.setPadding(bars.left,bars.top,bars.right,bars.bottom);
            return insets;
        });
        LinearLayout bar=Ui.row(this);Ui.pad(this,bar,13,9);
        TextView back=Ui.btn(this,"رجوع",false);
        bar.addView(back,Ui.lp(this,65,42));back.setOnClickListener(v->back());
        TextView name=Ui.txt(this,"محتوى سادسيون",20,Ui.WHITE,true);
        name.setPadding(dp(11),0,dp(11),0);
        bar.addView(name,new LinearLayout.LayoutParams(0,-2,1));
        TextView reload=Ui.btn(this,"تحديث",true);
        bar.addView(reload,Ui.lp(this,74,42));reload.setOnClickListener(v->refresh());
        root.addView(bar);
        ScrollView scroll=new ScrollView(this);
        content=Ui.col(this);Ui.pad(this,content,14,12);
        content.addView(Ui.txt(this,notice,13,Ui.MUTED,false),Ui.top(this,-1,-2,6));
        scroll.addView(content);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        setContentView(root);ViewCompat.requestApplyInsets(root);
        int shown=0;
        HashSet<String> existing=new HashSet<>();
        for(int i=0;i<records.length();i++){
            JSONObject item=records.optJSONObject(i);
            if(item!=null)existing.add(item.optString("id"));
        }
        for(int i=0;i<records.length();i++){
            JSONObject item=records.optJSONObject(i);
            if(item==null)continue;
            String parent=item.optString("parent_id","");
            if("null".equals(parent))parent="";
            if(currentParent.isEmpty()){
                if(!parent.isEmpty()&&existing.contains(parent))continue;
            }else if(!currentParent.equals(parent))continue;
            addItem(item);shown++;
        }
        if(shown==0){
            LinearLayout empty=Ui.card(this);
            empty.addView(Ui.txt(this,"لا توجد عناصر في هذا القسم",18,Ui.WHITE,true));
            empty.addView(Ui.txt(this,"تظهر هنا المواد والمحاضرات المنشورة من لوحة الإدارة.",
                13,Ui.MUTED,false),Ui.top(this,-1,-2,8));
            content.addView(empty,Ui.top(this,-1,-2,15));
        }
    }
    private static String category(String kind){
        switch(kind){
            case "cohort":return "دفعة";
            case "subject":return "مادة";
            case "teacher":return "أستاذ";
            case "chapter":return "فصل";
            case "lesson":return "محاضرة";
            case "resource":return "ملزمة";
            case "exam":return "وزاري";
            case "quiz":return "اختبار";
            case "announcement":return "إعلان";
            default:return "محتوى";
        }
    }
    private void addItem(JSONObject x){
        LinearLayout card=Ui.card(this);
        card.addView(Ui.txt(this,x.optString("title"),18,Ui.WHITE,true));
        String kind=x.optString("kind");
        card.addView(Ui.txt(this,category(kind)+" • "+x.optString("batch")+
            " • "+x.optString("subject"),12,Ui.MINT,false),Ui.top(this,-1,-2,5));
        if(!x.optString("subtitle").isEmpty())
            card.addView(Ui.txt(this,x.optString("subtitle"),13,Ui.MUTED,false),Ui.top(this,-1,-2,8));
        content.addView(card,Ui.top(this,-1,-2,10));
        card.setOnClickListener(v->open(x));
    }
    private boolean hasChildren(String id){
        for(int i=0;i<records.length();i++){
            JSONObject x=records.optJSONObject(i);
            if(x!=null&&id.equals(x.optString("parent_id")))return true;
        }
        return false;
    }
    private void open(JSONObject x){
        String id=x.optString("id"),kind=x.optString("kind");
        if("lesson".equals(kind)&&!x.optString("media_url").isEmpty()){
            Intent intent=new Intent(this,RemotePlayerActivity.class);
            intent.putExtra("id",id);
            intent.putExtra("title",x.optString("title"));
            intent.putExtra("url",x.optString("media_url"));
            intent.putExtra("type",x.optString("media_type"));
            startActivity(intent);return;
        }
        if("quiz".equals(kind)){showQuiz(x);return;}
        if(hasChildren(id)){
            history.addLast(currentParent);currentParent=id;render();return;
        }
        String url=x.optString("media_url");
        if(url.isEmpty())url=x.optString("source_url");
        if(url.startsWith("https://")){
            try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url)));}
            catch(Exception ignored){Toast.makeText(this,"تعذر فتح الرابط",Toast.LENGTH_SHORT).show();}
        }else Toast.makeText(this,"لا يوجد ملف منشور لهذا العنصر",Toast.LENGTH_SHORT).show();
    }
    private void showQuiz(JSONObject item){
        JSONObject extra=item.optJSONObject("extra");
        JSONArray options=extra==null?null:extra.optJSONArray("options");
        if(options==null||options.length()<2){
            Toast.makeText(this,"لم تُنشر خيارات الاختبار بعد",Toast.LENGTH_SHORT).show();return;
        }
        String[] labels=new String[options.length()];
        for(int i=0;i<labels.length;i++)labels[i]=options.optString(i);
        int answer=extra.optInt("answer",-1);
        new android.app.AlertDialog.Builder(this).setTitle(item.optString("title"))
            .setItems(labels,(dialog,which)->{
                boolean correct=which==answer;
                new android.app.AlertDialog.Builder(this)
                    .setTitle(correct?"إجابة صحيحة":"راجع الحل")
                    .setMessage((correct?"أحسنت.":answer>=0&&answer<labels.length?
                        "الإجابة الصحيحة: "+labels[answer]:"تحقق من الإجابة لدى المدرس")+
                        "\n"+extra.optString("explanation",""))
                    .setPositiveButton("إغلاق",null).show();
            }).setNegativeButton("إلغاء",null).show();
    }
    private void back(){
        if(!history.isEmpty()){currentParent=history.removeLast();render();}
        else finish();
    }
    @Override public void onBackPressed(){back();}
    @Override protected void onDestroy(){workers.shutdownNow();super.onDestroy();}
}
