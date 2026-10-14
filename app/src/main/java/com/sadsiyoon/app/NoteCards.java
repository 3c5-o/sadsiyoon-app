package com.sadsiyoon.app;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

/** Multiple titled local notes per lecture. Keeps the legacy single-note value intact. */
public final class NoteCards {
    private static final int MAX_NOTES = 60;
    private static final int MAX_TITLE = 90;
    private static final int MAX_BODY = 4000;
    private static final int MAX_JSON = 40000;
    private NoteCards(){}

    public static final class Note {
        public final String title, body;
        public final long createdAt, positionMs;
        Note(String title,String body,long createdAt,long positionMs){
            this.title=title;this.body=body;this.createdAt=createdAt;this.positionMs=positionMs;
        }
    }
    private static SharedPreferences preferences(Context c){
        return c.getSharedPreferences("sadsiyoon_local_v1",Context.MODE_PRIVATE);
    }
    public static String key(String lessonKey){return "note_cards_"+lessonKey;}
    private static JSONArray readArray(String json)throws Exception{
        if(json==null || json.length()>MAX_JSON)throw new Exception("Invalid note size");
        JSONArray data=new JSONArray(json);
        if(data.length()>MAX_NOTES)throw new Exception("Too many notes");
        for(int i=0;i<data.length();i++){
            JSONObject note=data.getJSONObject(i);
            if(!(note.opt("title") instanceof String)||
                !(note.opt("body") instanceof String)||
                !(note.opt("createdAt") instanceof Number))throw new Exception("Invalid note shape");
            String title=note.getString("title"),body=note.getString("body");
            Object timestamp=note.opt("positionMs");
            if(timestamp!=null && !(timestamp instanceof Number))
                throw new Exception("Invalid note time");
            if(title.trim().isEmpty()||title.length()>MAX_TITLE||body.length()>MAX_BODY||
                note.getLong("createdAt")<0||
                (timestamp instanceof Number && ((Number)timestamp).longValue() < -1))
                throw new Exception("Invalid note fields");
        }
        return data;
    }
    public static boolean isValidSerialized(String text){
        try{readArray(text);return true;}catch(Exception err){return false;}
    }
    private static void migrateLegacy(SharedPreferences prefs,String lessonKey){
        String target=key(lessonKey);
        if(prefs.contains(target))return;
        String old=prefs.getString("note_"+lessonKey,"");
        JSONArray array=new JSONArray();
        if(old!=null && !old.trim().isEmpty()){
            try{
                JSONObject initial=new JSONObject();
                initial.put("title","ملاحظة سابقة");
                initial.put("body",old.substring(0,Math.min(old.length(),MAX_BODY)));
                initial.put("createdAt",System.currentTimeMillis());
                array.put(initial);
            }catch(Exception ignored){}
        }
        prefs.edit().putString(target,array.toString()).apply();
    }
    public static List<Note> list(Context context,String lessonKey){
        SharedPreferences prefs=preferences(context);
        migrateLegacy(prefs,lessonKey);
        List<Note> items=new ArrayList<>();
        try{
            JSONArray array=readArray(prefs.getString(key(lessonKey),"[]"));
            for(int i=0;i<array.length();i++){
                JSONObject entry=array.getJSONObject(i);
                items.add(new Note(entry.getString("title"),entry.getString("body"),
                    entry.getLong("createdAt"),entry.optLong("positionMs",-1L)));
            }
        }catch(Exception err){
            // Never replace malformed imported data automatically.
            throw new IllegalStateException("تعذر قراءة الملاحظات المخزنة",err);
        }
        return items;
    }
    public static boolean save(Context context,String lessonKey,int editIndex,String title,String body){
        return save(context,lessonKey,editIndex,title,body,-1L);
    }
    public static boolean save(Context context,String lessonKey,int editIndex,String title,String body,long positionMs){
        if(title==null||body==null||positionMs < -1L)return false;
        title=title.trim();body=body.trim();
        if(title.isEmpty()||title.length()>MAX_TITLE||body.isEmpty()||body.length()>MAX_BODY)return false;
        SharedPreferences prefs=preferences(context);
        migrateLegacy(prefs,lessonKey);
        try{
            JSONArray data=readArray(prefs.getString(key(lessonKey),"[]"));
            if(editIndex<0 && data.length()>=MAX_NOTES)return false;
            if(editIndex>=data.length())return false;
            JSONObject item=new JSONObject();
            item.put("title",title);item.put("body",body);
            item.put("createdAt",editIndex>=0?data.getJSONObject(editIndex).getLong("createdAt"):
                System.currentTimeMillis());
            item.put("positionMs",positionMs);
            if(editIndex>=0)data.put(editIndex,item);else data.put(item);
            String encoded=data.toString();
            if(encoded.length()>MAX_JSON)return false;
            return prefs.edit().putString(key(lessonKey),encoded).commit();
        }catch(Exception err){return false;}
    }
    public static boolean delete(Context context,String lessonKey,int index){
        SharedPreferences prefs=preferences(context);
        migrateLegacy(prefs,lessonKey);
        try{
            JSONArray data=readArray(prefs.getString(key(lessonKey),"[]"));
            if(index<0||index>=data.length())return false;
            data.remove(index);
            return prefs.edit().putString(key(lessonKey),data.toString()).commit();
        }catch(Exception err){return false;}
    }
}
