package com.sadsiyoon.app;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONObject;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;

/** User-controlled backup for local progress, notes and favourites only. */
public final class BackupUtils {
    private BackupUtils(){}
    private static boolean permitted(String key){
        return key!=null && key.matches("(fav|done|position|note|note_cards)_t[0-9]+_c[0-9]+_l[0-9]+");
    }
    private static SharedPreferences prefs(Context context){
        return context.getSharedPreferences("sadsiyoon_local_v1",Context.MODE_PRIVATE);
    }
    public static int exportTo(Context context,OutputStream out)throws Exception{
        JSONObject json=new JSONObject();
        json.put("format","sadsiyoon-backup-v1");
        json.put("version",1);
        json.put("created_at",System.currentTimeMillis());
        JSONObject data=new JSONObject();
        int total=0;
        for(Map.Entry<String,?> pair:prefs(context).getAll().entrySet()){
            if(!permitted(pair.getKey()))continue;
            Object value=pair.getValue();
            if(!(value instanceof Boolean || value instanceof Number || value instanceof String))continue;
            data.put(pair.getKey(),value);
            total++;
        }
        json.put("data",data);
        byte[] raw=json.toString(2).getBytes(StandardCharsets.UTF_8);
        if(raw.length>2*1024*1024)throw new IOException("Backup exceeds 2 MiB");
        out.write(raw);out.flush();return total;
    }
    public static int importFrom(Context context,InputStream in)throws Exception{
        ByteArrayOutputStream data=new ByteArrayOutputStream();
        byte[] block=new byte[8192];int count;
        while((count=in.read(block))!=-1){
            if(data.size()+count>2*1024*1024)throw new IOException("Oversized backup");
            data.write(block,0,count);
        }
        JSONObject json=new JSONObject(new String(data.toByteArray(),StandardCharsets.UTF_8));
        if(!"sadsiyoon-backup-v1".equals(json.optString("format"))||
             json.optInt("version",-1)!=1)throw new IOException("Unknown backup format");
        JSONObject items=json.optJSONObject("data");
        if(items==null||items.length()>20000)throw new IOException("Invalid backup entries");
        // Validate every value before touching SharedPreferences.
        Iterator<String> iterator=items.keys();
        while(iterator.hasNext()){
            String key=iterator.next();
            Object value=items.opt(key);
            if(!permitted(key))throw new IOException("Unknown entry type");
            if(key.startsWith("note_cards_")){
                if(!(value instanceof String)||!NoteCards.isValidSerialized((String)value))
                    throw new IOException("Invalid structured notes");
            }else if(key.startsWith("note_")){
                if(!(value instanceof String)||((String)value).length()>40000)
                    throw new IOException("Invalid note");
            }else if(key.startsWith("position_")){
                if(!(value instanceof Number)||((Number)value).longValue()<0)
                    throw new IOException("Invalid position");
            }else if(!(value instanceof Boolean))throw new IOException("Invalid checkbox");
        }
        SharedPreferences.Editor editor=prefs(context).edit();iterator=items.keys();int total=0;
        while(iterator.hasNext()){
            String key=iterator.next(),name=key;
            Object value=items.get(key);
            if(name.startsWith("note_"))editor.putString(name,(String)value);
            else if(name.startsWith("position_"))editor.putLong(name,((Number)value).longValue());
            else editor.putBoolean(name,(Boolean)value);
            total++;
        }
        if(!editor.commit())throw new IOException("Failed to save restored data");
        return total;
    }
}
