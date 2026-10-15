package com.sadsiyoon.admin;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Native portrait owner dashboard. The server's RLS is authoritative, not UI visibility. */
public final class AdminActivity extends AppCompatActivity {
    private static final int BG=0xff0D1627, PANEL=0xff17263D, LINE=0xff2F4868;
    private static final int MINT=0xff20C7B4, WHITE=0xffF5F8FF, MUTED=0xffA4B6D2;
    private static final String[] KINDS={"cohort","subject","teacher","chapter","lesson","resource","exam","quiz","announcement"};
    private static final String[] KIND_LABELS={"الدفعات","المواد","المدرسون","الفصول","المحاضرات","الملازم","الوزاريات","الاختبارات","التنبيهات"};
    private static final String[] STATUS_IDS={"draft","published","hidden"};
    private static final String[] STATUS_LABELS={"مسودة","منشور","مخفي"};
    private final ExecutorService worker=Executors.newSingleThreadExecutor();
    private ApiClient api;
    private JSONArray records=new JSONArray();
    private LinearLayout screen,body;
    private String view="dashboard",kind="teacher",editingId="";
    private EditText title,subtitle,batch,subject,mediaUrl,mediaType,imageUrl,sourceUrl,sortOrder,extra,search;
    private Spinner statusPicker,parentPicker;
    private final List<String> parentIds=new ArrayList<>();
    private boolean authorized=false;
    private int lastCount=0;

    interface Work { void run()throws Exception; }
    private void work(Work action) {
        worker.execute(()->{
            try{action.run();}catch(Exception ex){
                runOnUiThread(()->{
                    String msg=ex.getMessage();
                    message(msg==null?"حدث خطأ في الاتصال":msg);
                });
            }
        });
    }
    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        WindowCompat.setDecorFitsSystemWindows(getWindow(),false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(BG);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        api=new ApiClient(this);
        if(api.hasSession()){
            frame("التحقق من جلسة المدير",false);
            label(body,"جارٍ التحقق من صلاحية المالك...",15,MUTED);
            work(()->{
                boolean ok=api.verifyOwner();
                runOnUiThread(()->{
                    if(!ok){api.clearSession();loginScreen();message("الحساب لا يملك صلاحية الإدارة");}
                    else {authorized=true;reload("dashboard");}
                });
            });
        }else loginScreen();
    }
    private int dp(int v){return Math.round(getResources().getDisplayMetrics().density*v);}
    private LinearLayout column(){LinearLayout l=new LinearLayout(this);l.setOrientation(1);l.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);return l;}
    private LinearLayout row(){LinearLayout l=new LinearLayout(this);l.setOrientation(0);l.setGravity(Gravity.CENTER_VERTICAL);l.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);return l;}
    private android.graphics.drawable.GradientDrawable shape(int c,int radius){
        android.graphics.drawable.GradientDrawable d=new android.graphics.drawable.GradientDrawable();
        d.setColor(c);d.setCornerRadius(dp(radius));d.setStroke(dp(1),LINE);return d;
    }
    private TextView text(String s,int size,int color,boolean bold){
        TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);
        t.setTextDirection(View.TEXT_DIRECTION_FIRST_STRONG);
        t.setGravity(Gravity.CENTER_VERTICAL);
        if(bold)t.setTypeface(null,1);return t;
    }
    private void label(LinearLayout parent,String s,int size,int color){
        TextView t=text(s,size,color,false);t.setPadding(0,dp(5),0,dp(7));parent.addView(t);
    }
    private TextView button(String name,boolean primary,Runnable click){
        TextView b=text(name,14,WHITE,true);b.setGravity(Gravity.CENTER);
        b.setPadding(dp(12),dp(11),dp(12),dp(11));
        b.setBackground(shape(primary?0xff245F79:0xff253E5D,12));
        b.setClickable(true);b.setFocusable(true);
        b.setOnClickListener(v->click.run());return b;
    }
    private LinearLayout card(){
        LinearLayout panel=column();
        panel.setPadding(dp(14),dp(13),dp(14),dp(13));
        panel.setBackground(shape(PANEL,16));
        return panel;
    }
    private void addCard(LinearLayout panel){
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);
        p.bottomMargin=dp(10);body.addView(panel,p);
    }
    private void frame(String heading,boolean tabs){
        screen=column();screen.setBackgroundColor(BG);
        ViewCompat.setOnApplyWindowInsetsListener(screen,(v,insets)->{
            Insets bars=insets.getInsets(WindowInsetsCompat.Type.systemBars());
            screen.setPadding(bars.left,bars.top,bars.right,bars.bottom);
            return insets;
        });
        LinearLayout toolbar=row();toolbar.setPadding(dp(16),dp(10),dp(16),dp(10));
        TextView brand=text("سادسيون | الإدارة",18,WHITE,true);
        toolbar.addView(brand,new LinearLayout.LayoutParams(0,dp(54),1));
        if(tabs)toolbar.addView(button("الرئيسية",false,()->show("dashboard")),new LinearLayout.LayoutParams(dp(91),dp(46)));
        screen.addView(toolbar);
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);
        body=column();body.setPadding(dp(15),dp(12),dp(15),dp(12));
        scroll.addView(body);screen.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        TextView headingView=text(heading,22,WHITE,true);
        headingView.setPadding(0,0,0,dp(13));body.addView(headingView);
        if(tabs) {
            LinearLayout nav=row();nav.setPadding(dp(9),dp(6),dp(9),dp(7));
            String[] names={"الرئيسية","المحتوى","السجل","الإعدادات"};
            String[] ids={"dashboard","collection","audit","settings"};
            for(int i=0;i<names.length;i++){
                final String target=ids[i];
                TextView btn=button(names[i],ids[i].equals(view),()->{
                    if("collection".equals(target))showKinds();
                    else show(target);
                });
                btn.setTextSize(11);
                LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(47),1);
                p.setMargins(dp(2),0,dp(2),0);nav.addView(btn,p);
            }
            screen.addView(nav);
        }
        setContentView(screen);ViewCompat.requestApplyInsets(screen);
    }
    private void message(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
    private EditText field(LinearLayout parent,String label,String value,boolean multiline){
        TextView text=text(label,13,MUTED,true);
        parent.addView(text);
        EditText entry=new EditText(this);
        entry.setText(value==null?"":value);entry.setTextColor(WHITE);entry.setHintTextColor(MUTED);
        entry.setTextSize(14);
        if(multiline){entry.setMinLines(2);entry.setMaxLines(5);entry.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE);}
        else entry.setSingleLine(true);
        entry.setBackground(shape(0xff243B57,10));
        entry.setPadding(dp(12),dp(10),dp(12),dp(10));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);
        p.topMargin=dp(5);p.bottomMargin=dp(14);parent.addView(entry,p);
        return entry;
    }
    private ArrayAdapter<String> pickerAdapter(List<String> names){
        return new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,names){
            @Override public View getView(int position,View convert,android.view.ViewGroup parent){
                TextView v=text(getItem(position),14,WHITE,false);v.setPadding(dp(12),dp(10),dp(12),dp(10));return v;
            }
            @Override public View getDropDownView(int pos,View convert,android.view.ViewGroup parent){
                TextView v=text(getItem(pos),14,WHITE,false);v.setBackgroundColor(PANEL);v.setPadding(dp(12),dp(13),dp(12),dp(13));return v;
            }
        };
    }
    private String value(EditText input){return input.getText().toString().trim();}
    private void loginScreen(){
        authorized=false;view="login";frame("تسجيل دخول المالك",false);
        LinearLayout c=card();
        c.addView(text("SADESION ADMIN",19,MINT,true));
        label(c,"الدخول مخصص لحساب المالك المعتمد فقط. لا يوجد إنشاء حسابات من هذه النسخة.",13,MUTED);
        EditText email=field(c,"البريد الإلكتروني","",false);
        email.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        EditText password=field(c,"كلمة المرور","",false);
        password.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
        TextView enter=button("تسجيل الدخول",true,()->{
            final String em=value(email),pw=password.getText().toString();
            if(em.isEmpty()||pw.isEmpty()){message("أدخل البريد وكلمة المرور");return;}
            password.setText("");message("جارٍ التحقق...");
            work(()->{
                api.login(em,pw);
                boolean ok=api.verifyOwner();
                if(!ok){
                    api.clearSession();throw new Exception("الحساب غير مخول بالإدارة. فعّل صلاحية المالك في قاعدة البيانات.");
                }
                runOnUiThread(()->{authorized=true;reload("dashboard");});
            });
        });
        c.addView(enter);
        addCard(c);
    }
    private void reload(String next){
        if(!authorized){loginScreen();return;}
        view=next;frame("جارٍ تحميل البيانات...",true);
        work(()->{
            JSONArray loaded=api.listAll();
            runOnUiThread(()->{
                records=loaded;lastCount=records.length();show(next);
            });
        });
    }
    private void show(String next){
        if(!authorized){loginScreen();return;}
        view=next;
        if("dashboard".equals(next))dashboard();
        else if("collection".equals(next))collection();
        else if("editor".equals(next))editor();
        else if("audit".equals(next))audit();
        else if("settings".equals(next))settings();
        else showKinds();
    }
    private static String displayKind(String id){
        for(int i=0;i<KINDS.length;i++)if(KINDS[i].equals(id))return KIND_LABELS[i];
        return id;
    }
    private int count(String type,String state){
        int count=0;
        for(int i=0;i<records.length();i++){
            JSONObject x=records.optJSONObject(i);
            if(x!=null && (type==null||type.equals(x.optString("kind"))) &&
               (state==null||state.equals(x.optString("status"))))count++;
        }
        return count;
    }
    private void dashboard(){
        frame("لوحة التحكم",true);
        label(body,"متابعة المحتوى وحالة النشر — مشروع سادسيون",14,MUTED);
        LinearLayout c=card();c.addView(text("سجلات المحتوى: "+lastCount,21,MINT,true));
        label(c,"المنشور: "+count(null,"published")+"    •    المسودات: "+count(null,"draft")+
              "    •    المخفي: "+count(null,"hidden"),13,WHITE);
        addCard(c);
        for(int i=0;i<KINDS.length;i++){
            final String id=KINDS[i];final String name=KIND_LABELS[i];
            LinearLayout tile=card();
            tile.addView(text(name+"    ("+count(id,null)+")",17,WHITE,true));
            label(tile,"منشور: "+count(id,"published")+"    •    غير منشور: "+
                (count(id,null)-count(id,"published")),12,MUTED);
            addCard(tile);tile.setOnClickListener(v->{kind=id;show("collection");});
        }
        body.addView(button("تحديث البيانات",true,()->reload("dashboard")));
    }
    private void showKinds(){
        frame("أقسام المحتوى",true);
        for(int i=0;i<KINDS.length;i++){
            final String k=KINDS[i];
            body.addView(button(KIND_LABELS[i]+"  ←",false,()->{kind=k;show("collection");}),
                space(0,54,8));
        }
    }
    private static LinearLayout.LayoutParams space(int width,int height,int bottom){
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(width==0?-1:width,height);
        p.bottomMargin=bottom;return p;
    }
    private void collection(){
        frame(displayKind(kind),true);
        LinearLayout options=row();
        TextView add=button("+ إضافة",true,()->{editingId="";show("editor");});
        options.addView(add,new LinearLayout.LayoutParams(0,dp(50),1));
        TextView refresh=button("تحديث",false,()->reload("collection"));
        LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(0,dp(50),1);rp.leftMargin=dp(8);
        options.addView(refresh,rp);body.addView(options,space(0,50,11));
        search=field(body,"بحث داخل "+displayKind(kind),"",false);
        LinearLayout results=column();body.addView(results);
        search.addTextChangedListener(new android.text.TextWatcher(){
            public void beforeTextChanged(CharSequence s,int st,int c,int af){}
            public void onTextChanged(CharSequence s,int st,int before,int count){renderList(results,s.toString());}
            public void afterTextChanged(android.text.Editable e){}
        });
        renderList(results,"");
    }
    private void renderList(LinearLayout parent,String q){
        parent.removeAllViews();int found=0;
        String needle=q.trim().toLowerCase(Locale.ROOT);
        for(int i=0;i<records.length();i++){
            JSONObject x=records.optJSONObject(i);
            if(x==null||!kind.equals(x.optString("kind")))continue;
            String title=x.optString("title");
            if(!needle.isEmpty()&&!title.toLowerCase(Locale.ROOT).contains(needle) &&
               !x.optString("subject").toLowerCase(Locale.ROOT).contains(needle))continue;
            final String id=x.optString("id");
            LinearLayout c=card();
            c.addView(text(title,17,WHITE,true));
            label(c,x.optString("batch")+"  •  "+x.optString("subject")+"  •  "+
                  labelStatus(x.optString("status")),12,MUTED);
            TextView edit=button("تعديل ←",false,()->{editingId=id;show("editor");});
            c.addView(edit);
            parent.addView(c,space(0,-2,10));
            found++;
        }
        if(found==0)label(parent,"لا توجد عناصر. أضف أول "+displayKind(kind)+" من الزر بالأعلى.",14,MUTED);
    }
    private static String labelStatus(String status){
        for(int i=0;i<STATUS_IDS.length;i++)if(STATUS_IDS[i].equals(status))return STATUS_LABELS[i];
        return status;
    }
    private JSONObject editing(){
        for(int i=0;i<records.length();i++){
            JSONObject item=records.optJSONObject(i);
            if(item!=null&&editingId.equals(item.optString("id")))return item;
        }
        return new JSONObject();
    }
    private void editor(){
        JSONObject old=editing();
        boolean modifying=!editingId.isEmpty();
        frame((modifying?"تعديل ":"إضافة ")+displayKind(kind),true);
        label(body,"احفظ كمسودة أولاً، وافحص الروابط قبل النشر. لا تُنشر المحاضرات المدفوعة بدون إذن.",13,MUTED);
        title=field(body,"الاسم / العنوان *",old.optString("title"),false);
        subtitle=field(body,"التفاصيل",old.optString("subtitle"),true);
        batch=field(body,"الدفعة (مثل 2027)",old.optString("batch"),false);
        subject=field(body,"المادة (مثل الرياضيات)",old.optString("subject"),false);
        mediaUrl=field(body,"رابط المحاضرة أو الملف HTTPS",old.optString("media_url"),false);
        mediaType=field(body,"نوع التشغيل (mp4 / hls / embed / pdf)",old.optString("media_type"),false);
        imageUrl=field(body,"رابط الصورة HTTPS",old.optString("image_url"),false);
        sourceUrl=field(body,"رابط المرجع الأصلي HTTPS",old.optString("source_url"),false);
        sortOrder=field(body,"ترتيب الظهور",String.valueOf(old.optInt("sort_index",0)),false);
        sortOrder.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_SIGNED);
        extra=field(body,"بيانات إضافية JSON (مثل خيارات الأسئلة)",old.optJSONObject("extra")==null?"{}":old.optJSONObject("extra").toString(),true);
        label(body,"التصنيف",13,MUTED);
        parentPicker=new Spinner(this);
        parentIds.clear();parentIds.add("");
        List<String> parents=new ArrayList<>();parents.add("بدون ربط");
        for(int i=0;i<records.length();i++){
            JSONObject item=records.optJSONObject(i);
            if(item==null||item.optString("id").equals(editingId))continue;
            if(!isParentType(kind,item.optString("kind")))continue;
            parentIds.add(item.optString("id"));
            parents.add(displayKind(item.optString("kind"))+" • "+item.optString("title"));
        }
        parentPicker.setAdapter(pickerAdapter(parents));body.addView(parentPicker,space(0,56,16));
        int selectedParent=parentIds.indexOf(old.optString("parent_id"));
        if(selectedParent>=0)parentPicker.setSelection(selectedParent);
        label(body,"حالة النشر",13,MUTED);
        statusPicker=new Spinner(this);
        List<String> statuses=new ArrayList<>();for(String s:STATUS_LABELS)statuses.add(s);
        statusPicker.setAdapter(pickerAdapter(statuses));body.addView(statusPicker,space(0,56,16));
        for(int i=0;i<STATUS_IDS.length;i++)if(STATUS_IDS[i].equals(old.optString("status")))statusPicker.setSelection(i);
        body.addView(button(modifying?"حفظ التعديلات":"إنشاء المحتوى",true,()->saveItem()),space(0,53,12));
        body.addView(button("معاينة رابط المحتوى",false,()->preview()),space(0,50,10));
        if(modifying)body.addView(button("حذف العنصر",false,()->confirmDelete()),space(0,50,10));
    }
    private static boolean isParentType(String child,String parent){
        if("subject".equals(child))return "cohort".equals(parent);
        if("teacher".equals(child))return "cohort".equals(parent)||"subject".equals(parent);
        if("chapter".equals(child))return "teacher".equals(parent);
        if("lesson".equals(child))return "chapter".equals(parent);
        if("resource".equals(child))return "teacher".equals(parent)||"chapter".equals(parent);
        if("exam".equals(child))return "subject".equals(parent);
        if("quiz".equals(child))return "exam".equals(parent)||"subject".equals(parent);
        return false;
    }
    private void saveItem(){
        String name=value(title);
        if(name.isEmpty()){message("عنوان العنصر مطلوب");return;}
        String url=value(mediaUrl),img=value(imageUrl),src=value(sourceUrl);
        for(String candidate:new String[]{url,img,src}){
            if(!candidate.isEmpty() && !candidate.startsWith("https://")) {
                message("يُسمح بروابط HTTPS فقط");return;
            }
        }
        JSONObject payload;
        int index;
        try{
            payload=new JSONObject(value(extra));
            index=Integer.parseInt(value(sortOrder));
        }catch(Exception ex){message("تحقق من تنسيق JSON والترتيب");return;}
        String p=parentIds.get(parentPicker.getSelectedItemPosition());
        JSONObject doc=new JSONObject();
        try{
            doc.put("kind",kind).put("title",name).put("subtitle",value(subtitle))
               .put("batch",value(batch)).put("subject",value(subject))
               .put("media_url",url).put("media_type",value(mediaType))
               .put("image_url",img).put("source_url",src)
               .put("extra",payload).put("sort_index",index)
               .put("status",STATUS_IDS[statusPicker.getSelectedItemPosition()]);
            doc.put("parent_id",p.isEmpty()?JSONObject.NULL:p);
        }catch(Exception ex){message("خطأ في تكوين البيانات");return;}
        String id=editingId;
        message("جارٍ حفظ المحتوى...");
        work(()->{
            api.save(doc,id);
            runOnUiThread(()->{message("تم حفظ المحتوى");reload("collection");});
        });
    }
    private void preview(){
        String candidate=value(mediaUrl);
        if(candidate.isEmpty())candidate=value(sourceUrl);
        if(!candidate.startsWith("https://")){message("لا يوجد رابط HTTPS للمعاينة");return;}
        try{startActivity(new Intent(Intent.ACTION_VIEW,android.net.Uri.parse(candidate)));}
        catch(Exception ex){message("لا يوجد تطبيق يدعم فتح هذا الرابط");}
    }
    private void confirmDelete(){
        String id=editingId;
        new AlertDialog.Builder(this)
          .setTitle("تأكيد الحذف")
          .setMessage("سيُحذف هذا السجل ويُسجل التغيير في سجل الإدارة. هل تريد المتابعة؟")
          .setNegativeButton("إلغاء",null)
          .setPositiveButton("حذف",(d,w)->work(()->{
              api.delete(id);
              runOnUiThread(()->{message("تم الحذف");editingId="";reload("collection");});
          })).show();
    }
    private void audit(){
        frame("سجل التعديلات",true);
        label(body,"آخر عمليات الإضافة والتعديل والحذف من قاعدة البيانات.",13,MUTED);
        LinearLayout items=column();body.addView(items);
        work(()->{
            JSONArray log=api.audit();
            runOnUiThread(()->{
                items.removeAllViews();
                if(log.length()==0)label(items,"لا توجد عمليات بعد.",14,MUTED);
                for(int i=0;i<log.length();i++){
                    JSONObject entry=log.optJSONObject(i);if(entry==null)continue;
                    LinearLayout c=card();
                    c.addView(text(entry.optString("action")+" • "+entry.optString("created_at"),14,WHITE,true));
                    label(c,"السجل: "+entry.optString("content_id"),11,MUTED);
                    items.addView(c,space(0,-2,8));
                }
            });
        });
    }
    private void settings(){
        frame("إعدادات الإدارة",true);
        LinearLayout info=card();
        info.addView(text("ربط سادسيون",18,MINT,true));
        label(info,"قاعدة البيانات: SADESION • الدخول عبر Supabase Auth • الكتابة محمية بصلاحية المالك (RLS).",13,MUTED);
        label(info,"نسخة تجريبية بتوقيع Debug، وليست إصدار إنتاج.",13,MUTED);
        addCard(info);
        body.addView(button("تحديث المحتوى",true,()->reload("dashboard")),space(0,52,11));
        body.addView(button("تسجيل الخروج",false,()->{
            api.clearSession();records=new JSONArray();authorized=false;loginScreen();
        }),space(0,52,11));
    }
    @Override public void onBackPressed(){
        if(!authorized){super.onBackPressed();return;}
        if("editor".equals(view)){show("collection");return;}
        if(!"dashboard".equals(view)){show("dashboard");return;}
        super.onBackPressed();
    }
    @Override protected void onDestroy(){worker.shutdownNow();super.onDestroy();}
}
