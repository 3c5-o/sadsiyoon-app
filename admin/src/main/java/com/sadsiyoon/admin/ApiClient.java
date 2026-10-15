package com.sadsiyoon.admin;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;

/** HTTPS REST client. Publishable key is public; all write permissions belong to Supabase RLS. */
final class ApiClient {
    private final SharedPreferences prefs;
    ApiClient(Context context) {
        prefs=context.getSharedPreferences("admin_session_v1", Context.MODE_PRIVATE);
    }
    private static String encode(String value) {
        try{return URLEncoder.encode(value,"UTF-8");}catch(Exception ex){return value;}
    }
    private static final class Response {
        final int code; final String body;
        Response(int c,String b){code=c;body=b;}
    }
    private static String read(InputStream input)throws Exception {
        if(input==null)return "";
        try(InputStream in=input; ByteArrayOutputStream output=new ByteArrayOutputStream()){
            byte[] buf=new byte[8192];int n;
            while((n=in.read(buf))>0){
                if(output.size()>4*1024*1024)throw new Exception("استجابة كبيرة جداً");
                output.write(buf,0,n);
            }
            return output.toString(StandardCharsets.UTF_8.name());
        }
    }
    private Response raw(String method,String path,JSONObject json,String bearer)throws Exception {
        okhttp3.OkHttpClient client=new okhttp3.OkHttpClient.Builder()
            .connectTimeout(9,java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(14,java.util.concurrent.TimeUnit.SECONDS)
            .followRedirects(false)
            .build();
        okhttp3.Request.Builder req=new okhttp3.Request.Builder()
            .url(BuildConfig.SUPABASE_URL+path)
            .header("apikey",BuildConfig.SUPABASE_PUBLISHABLE_KEY)
            .header("Accept","application/json")
            .header("Content-Type","application/json");
        if(bearer!=null&&!bearer.isEmpty())req.header("Authorization","Bearer "+bearer);
        if(path.startsWith("/rest/v1/") && !"GET".equals(method))
            req.header("Prefer","return=representation");
        okhttp3.RequestBody payload=json==null?null:
            okhttp3.RequestBody.create(json.toString(),
                okhttp3.MediaType.parse("application/json; charset=utf-8"));
        req.method(method,payload);
        try(okhttp3.Response response=client.newCall(req.build()).execute()){
            String body=response.body()==null?"":response.body().string();
            if(body.length()>4*1024*1024)throw new Exception("استجابة كبيرة جداً");
            return new Response(response.code(),body);
        }
    }
    private static String error(Response r){
        try {
            JSONObject x=new JSONObject(r.body);
            String message=x.optString("msg",x.optString("message",x.optString("error_description","")));
            if(!message.isEmpty())return message;
        }catch(Exception ignored){}
        return "فشل الاتصال (HTTP "+r.code+")";
    }
    private static Response success(Response r)throws Exception{
        if(r.code<200 || r.code>=300)throw new Exception(error(r));
        return r;
    }
    boolean hasSession(){return !prefs.getString("refresh","").isEmpty();}
    void clearSession(){prefs.edit().clear().apply();}
    private void persist(JSONObject json){
        long expiry=System.currentTimeMillis()+Math.max(60,json.optLong("expires_in",3600)-90)*1000L;
        prefs.edit().putString("access",json.optString("access_token",""))
            .putString("refresh",json.optString("refresh_token",""))
            .putLong("expiry",expiry).apply();
    }
    void login(String email,String password)throws Exception {
        JSONObject body=new JSONObject().put("email",email.trim()).put("password",password);
        Response response=success(raw("POST","/auth/v1/token?grant_type=password",body,null));
        JSONObject tokens=new JSONObject(response.body);
        if(tokens.optString("access_token","").isEmpty()||
           tokens.optString("refresh_token","").isEmpty())throw new Exception("الجلسة غير مكتملة");
        persist(tokens);
    }
    private String token()throws Exception {
        String existing=prefs.getString("access","");
        if(!existing.isEmpty() && System.currentTimeMillis()<prefs.getLong("expiry",0L))return existing;
        String refresh=prefs.getString("refresh","");
        if(refresh.isEmpty())throw new Exception("يرجى تسجيل الدخول");
        JSONObject body=new JSONObject().put("refresh_token",refresh);
        try{
            Response response=success(raw("POST","/auth/v1/token?grant_type=refresh_token",body,null));
            JSONObject tokens=new JSONObject(response.body);persist(tokens);
            return tokens.getString("access_token");
        }catch(Exception ex){clearSession();throw new Exception("انتهت جلسة الإدارة. سجل الدخول مجدداً.");}
    }
    private Response auth(String method,String path,JSONObject body)throws Exception {
        Response response=raw(method,path,body,token());
        if(response.code==401){
            prefs.edit().putLong("expiry",0L).apply();
            response=raw(method,path,body,token());
        }
        return success(response);
    }
    boolean verifyOwner()throws Exception{
        // No email-only authorization. The protected SQL function checks auth.uid()
        // against the server-maintained owner allowlist and confirmed Auth identity.
        Response r=auth("POST","/rest/v1/rpc/sadesion_is_admin",new JSONObject());
        return "true".equals(r.body.trim());
    }
    JSONArray listAll()throws Exception{
        Response r=auth("GET","/rest/v1/sadesion_content?select=*&order=kind.asc,sort_index.asc,created_at.desc&limit=1000",null);
        return new JSONArray(r.body);
    }
    JSONArray audit()throws Exception{
        Response r=auth("GET","/rest/v1/sadesion_content_audit?select=action,created_at,content_id&order=created_at.desc&limit=60",null);
        return new JSONArray(r.body);
    }
    void save(JSONObject item,String id)throws Exception{
        if(id==null||id.isEmpty()){
            auth("POST","/rest/v1/sadesion_content",item);
        }else{
            auth("PATCH","/rest/v1/sadesion_content?id=eq."+encode(id),item);
        }
    }
    void delete(String id)throws Exception{
        auth("DELETE","/rest/v1/sadesion_content?id=eq."+encode(id),null);
    }
}
