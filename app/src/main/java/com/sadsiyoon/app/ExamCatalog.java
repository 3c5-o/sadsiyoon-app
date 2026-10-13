package com.sadsiyoon.app;

import android.content.Context;
import org.json.JSONObject;
import org.json.JSONArray;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Source-linked authentic 2025 exam pages and optional licensed image galleries. */
public final class ExamCatalog {
    private ExamCatalog(){}
    public static final class Paper {
        public final int year;
        public final String branch,subject,round,title,source;
        public final List<String> urls;
        Paper(int y,String b,String s,String r,String t,String sourcePage,List<String> imgs){
            year=y;branch=b;subject=s;round=r;title=t;source=sourcePage;
            urls=Collections.unmodifiableList(imgs);
        }
    }
    public static List<Paper> select(Context ctx,int year,String branch,String subject,String round){
        List<Paper> selected=new ArrayList<>();
        try(InputStream input=ctx.getAssets().open("exams/2025.json")){
            ByteArrayOutputStream buf=new ByteArrayOutputStream();
            byte[] temp=new byte[8192];int len;
            while((len=input.read(temp))!=-1)buf.write(temp,0,len);
            JSONObject data=new JSONObject(new String(buf.toByteArray(),StandardCharsets.UTF_8));
            JSONArray docs=data.optJSONArray("papers");
            if(docs==null)return selected;
            for(int i=0;i<docs.length();i++){
                JSONObject p=docs.optJSONObject(i);if(p==null)continue;
                if(p.optInt("year",-1)!=year || !branch.equals(p.optString("branch")) ||
                   !subject.equals(p.optString("subject")) || !round.equals(p.optString("round")))continue;
                String source=p.optString("source","").trim();
                JSONArray pages=p.optJSONArray("images");
                List<String> urls=new ArrayList<>();
                if(pages!=null)for(int j=0;j<pages.length();j++){
                    String u=pages.optString(j,"");
                    if(u.startsWith("https://"))urls.add(u);
                }
                // Read Mlazemna's published page, without mirroring its images.
                if(!urls.isEmpty()||(urls.isEmpty()&&source.startsWith("https://mlazemna.com/")))
                    selected.add(new Paper(year,branch,subject,round,p.optString("title"),source,urls));
            }
        }catch(Exception e){android.util.Log.e("Sadsiyoon","Exam catalogue unavailable",e);}
        return selected;
    }
}
