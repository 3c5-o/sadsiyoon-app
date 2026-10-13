package com.sadsiyoon.app;

import android.app.PictureInPictureParams;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import android.content.res.ColorStateList;
import android.text.TextUtils;
import android.util.TypedValue;
import androidx.core.graphics.Insets;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.ViewCompat;
import android.util.Rational;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.PlaybackParameters;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

public class PlayerActivity extends AppCompatActivity {
    private SharedPreferences prefs;
    private Catalog.Ref current;
    private ExoPlayer player;
    private PlayerView playerView;
    private WebView web;
    private View embeddedFullScreen;
    private WebChromeClient.CustomViewCallback embeddedFullScreenCallback;
    private FrameLayout videoBox;
    private LinearLayout root,topbar,actions,tabContent,details,playerShell,extrasRow,secondRow;
    private TextView heading,info,status,favorite,finished,prev,next,speedButton,fullscreenButton,pipButton;
    private EditText noteInput;
    private boolean landscape=false;
    private String tab="lessons";
    private float speed=1f;

    @Override public void onCreate(Bundle saved){
        super.onCreate(saved);
        WindowCompat.setDecorFitsSystemWindows(getWindow(),false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(0);
        prefs=getSharedPreferences("sadsiyoon_local_v1",MODE_PRIVATE);
        int ti=getIntent().getIntExtra("ti",1),ci=getIntent().getIntExtra("ci",0),li=getIntent().getIntExtra("li",0);
        current=Catalog.ref(this,ti,ci,li);
        if(current==null){finish();return;}
        buildScreen();load();
    }

    private int dp(float v){return Ui.dp(this,v);}
    private int videoHeight(){
        int width=getResources().getDisplayMetrics().widthPixels-dp(24);
        return Math.max(dp(155),Math.round(width*9f/16f));
    }
    private void buildScreen(){
        root=Ui.col(this);
        root.setBackgroundColor(Ui.BG);
        // Android 15 enforces edge-to-edge. Keep the content beneath status icons
        // and above the gesture navigation area, even with large display fonts.
        ViewCompat.setOnApplyWindowInsetsListener(root,(v,insets)->{
            Insets bars=insets.getInsets(WindowInsetsCompat.Type.systemBars());
            boolean wide=getResources().getConfiguration().orientation==
                Configuration.ORIENTATION_LANDSCAPE;
            root.setPadding(dp(wide?5:12),wide?bars.top:bars.top+dp(6),
                dp(wide?5:12),wide?bars.bottom:bars.bottom+dp(7));
            return insets;
        });
        setContentView(root);
        ViewCompat.requestApplyInsets(root);

        topbar=Ui.row(this);
        TextView back=Ui.btn(this,"رجوع ‹",false);
        back.setTextSize(13);back.setSingleLine(true);
        back.setGravity(Gravity.CENTER);
        topbar.addView(back,Ui.lp(this,78,42));
        back.setOnClickListener(v->finish());
        LinearLayout labels=Ui.col(this);
        labels.setGravity(Gravity.CENTER_VERTICAL);
        labels.setPadding(dp(12),0,dp(7),0);
        TextView brand=Ui.txt(this,"سادسيون",18,Ui.GOLD,true);
        brand.setSingleLine(true);brand.setEllipsize(TextUtils.TruncateAt.END);
        labels.addView(brand);
        TextView subtitle=Ui.txt(this,"محاضرات دفعة "+current.teacher.batch,11,Ui.MUTED,false);
        subtitle.setSingleLine(true);subtitle.setEllipsize(TextUtils.TruncateAt.END);
        labels.addView(subtitle);
        topbar.addView(labels,new LinearLayout.LayoutParams(0,dp(44),1f));
        root.addView(topbar,Ui.lp(this,-1,48));

        heading=Ui.txt(this,"",20,Ui.WHITE,true);
        heading.setSingleLine(true);heading.setEllipsize(TextUtils.TruncateAt.END);
        heading.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);
        root.addView(heading,Ui.top(this,-1,34,8));
        info=Ui.txt(this,"",12,Ui.MINT,false);
        info.setSingleLine(true);info.setEllipsize(TextUtils.TruncateAt.END);
        root.addView(info,Ui.top(this,-1,28,3));

        playerShell=Ui.col(this);
        playerShell.setBackground(Ui.round(this,Ui.PANEL,17));
        playerShell.setClipToOutline(true);
        videoBox=new FrameLayout(this);
        videoBox.setBackgroundColor(Color.BLACK);
        playerShell.addView(videoBox,new LinearLayout.LayoutParams(-1,videoHeight()));
        actions=Ui.col(this);
        Ui.pad(this,actions,5,7);

        LinearLayout navigation=Ui.row(this);
        prev=addAction(navigation,"السابق",false,()->step(-1));
        next=addAction(navigation,"التالي",true,()->step(1));
        favorite=addAction(navigation,"☆ حفظ",false,this::toggleFavorite);
        finished=addAction(navigation,"○ إكمال",false,this::toggleCompleted);
        actions.addView(navigation);

        secondRow=Ui.row(this);
        fullscreenButton=addAction(secondRow,"تكبير",false,this::toggleOrientation);
        speedButton=addAction(secondRow,"السرعة 1×",false,this::nextSpeed);
        pipButton=addAction(secondRow,"نافذة عائمة",false,this::enterPip);
        addAction(secondRow,"إعادة المحاولة",false,this::load);
        actions.addView(secondRow,Ui.top(this,-1,39,5));
        playerShell.addView(actions);
        root.addView(playerShell,Ui.top(this,-1,-2,10));

        details=Ui.col(this);
        status=Ui.txt(this,"",12,Ui.MUTED,false);
        status.setMaxLines(2);status.setEllipsize(TextUtils.TruncateAt.END);
        details.addView(status,Ui.top(this,-1,-2,9));
        LinearLayout tabs=Ui.row(this);
        addAction(tabs,"المحاضرات",true,()->selectTab("lessons"));
        addAction(tabs,"الملاحظات",false,()->selectTab("notes"));
        addAction(tabs,"الملفات",false,()->selectTab("files"));
        details.addView(tabs,Ui.top(this,-1,40,13));
        ScrollView listScroll=new ScrollView(this);
        listScroll.setFillViewport(true);
        listScroll.setVerticalScrollBarEnabled(false);
        tabContent=Ui.col(this);Ui.pad(this,tabContent,2,6);
        listScroll.addView(tabContent);
        details.addView(listScroll,new LinearLayout.LayoutParams(-1,0,1f));
        root.addView(details,new LinearLayout.LayoutParams(-1,0,1f));
        showTab();updateOrientation();
    }
    private TextView addAction(LinearLayout container,String label,boolean primary,Runnable runnable){
        TextView v=Ui.btn(this,label,primary);
        v.setTextSize(12);
        v.setSingleLine(true);v.setEllipsize(TextUtils.TruncateAt.END);
        v.setAutoSizeTextTypeUniformWithConfiguration(10,12,1,TypedValue.COMPLEX_UNIT_SP);
        v.setContentDescription(label);
        Ui.pad(this,v,3,7);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(39),1f);
        p.setMargins(dp(2),0,dp(2),0);
        container.addView(v,p);
        v.setOnClickListener(w->runnable.run());
        return v;
    }
    private void updateAvailableControls(){
        boolean nativePlayer=current!=null &&
            ("hls".equals(current.lesson.type)||"mp4".equals(current.lesson.type));
        if(speedButton!=null)speedButton.setVisibility(nativePlayer?View.VISIBLE:View.GONE);
        if(pipButton!=null)pipButton.setVisibility(nativePlayer?View.VISIBLE:View.GONE);
    }
    private void setStatus(String msg){if(status!=null)status.setText(msg);}
    private void load(){
        if(current==null)return;
        heading.setText(current.lesson.title);
        info.setText(current.teacher.name+"  •  "+current.chapter.title+"  •  دفعة "+current.teacher.batch);
        saveNote();
        refreshActions();destroyPlayback();
        updateAvailableControls();
        if(!current.lesson.available()){setStatus("هذه المحاضرة غير متاحة للتشغيل حالياً");showUnavailable();return;}
        setStatus(current.lesson.access.equals("premium")?
            "قد تتطلب المشاهدة اشتراكاً لدى مزود الفيديو.":
            "جاري تحميل مشغّل المحاضرة...");
        if(current.lesson.type.equals("hls")||current.lesson.type.equals("mp4"))openNative();
        else if(current.lesson.type.equals("embed"))openEmbedded();
        else showUnavailable();
        if(tab.equals("notes")||tab.equals("lessons"))showTab();
    }
    private void showUnavailable(){
        TextView empty=Ui.txt(this,"لا يمكن تشغيل هذه المحاضرة حالياً",16,Ui.WHITE,true);
        empty.setGravity(Gravity.CENTER);videoBox.addView(empty,new FrameLayout.LayoutParams(-1,-1));
    }
    private void openNative(){
        playerView=new PlayerView(this);playerView.setUseController(true);
        playerView.setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS);
        playerView.setBackgroundColor(Color.BLACK);
        videoBox.addView(playerView,new FrameLayout.LayoutParams(-1,-1));
        player=new ExoPlayer.Builder(this).build();playerView.setPlayer(player);
        player.addListener(new Player.Listener(){
            @Override public void onPlaybackStateChanged(int state){
                if(state==Player.STATE_BUFFERING)setStatus("جاري تحميل المحاضرة...");
                if(state==Player.STATE_READY)setStatus("المحاضرة جاهزة للمشاهدة");
                if(state==Player.STATE_ENDED){
                    prefs.edit().putBoolean("done_"+current.key(),true).putLong("position_"+current.key(),0L).apply();
                    refreshActions();if(prefs.getBoolean("autoplay",true))step(1);
                    else setStatus("اكتملت المحاضرة");
                }
            }
            @Override public void onPlayerError(PlaybackException err){
                setStatus("تعذر تشغيل هذه المحاضرة. تحقق من اتصال الإنترنت أو حاول مرة أخرى.");
                Toast.makeText(PlayerActivity.this,"حدث خطأ أثناء التشغيل",Toast.LENGTH_LONG).show();
            }
        });
        MediaItem.Builder builder=new MediaItem.Builder().setUri(Uri.parse(current.lesson.url));
        if(current.lesson.type.equals("hls"))builder.setMimeType(MimeTypes.APPLICATION_M3U8);
        player.setMediaItem(builder.build());player.prepare();
        long saved=prefs.getLong("position_"+current.key(),0L);
        if(saved>0)player.seekTo(saved);
        player.setPlaybackParameters(new PlaybackParameters(speed));player.play();
    }
    private void openEmbedded(){
        String link=current.lesson.url;
        Uri uri=Uri.parse(link);String host=uri.getHost();
        if(host==null||!(host.equals("iframe.mediadelivery.net")||host.equals("ok.ru")||
            host.equals("vk.com")||host.equals("vkvideo.ru")||
            host.equals("rumble.com")||host.equals("www.rumble.com")||
            host.equals("bitchute.com")||host.equals("www.bitchute.com"))){
            setStatus("نوع المشغّل غير مدعوم في النسخة الحالية");showUnavailable();return;
        }
        web=new WebView(this);web.setBackgroundColor(Color.BLACK);
        web.getSettings().setJavaScriptEnabled(true);web.getSettings().setDomStorageEnabled(true);
        web.getSettings().setAllowFileAccess(false);web.getSettings().setAllowContentAccess(false);
        web.getSettings().setMediaPlaybackRequiresUserGesture(false);
        web.getSettings().setMixedContentMode(android.webkit.WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        web.setWebChromeClient(new WebChromeClient(){
            @Override public void onShowCustomView(View view,CustomViewCallback callback){
                if(embeddedFullScreen!=null){callback.onCustomViewHidden();return;}
                embeddedFullScreen=view;embeddedFullScreenCallback=callback;
                ViewGroup decor=(ViewGroup)getWindow().getDecorView();
                decor.addView(view,new ViewGroup.LayoutParams(-1,-1));
                getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            }
            @Override public void onHideCustomView(){exitEmbeddedFullScreen();}
        });
        web.setWebViewClient(new WebViewClient(){
            @Override public void onPageFinished(WebView view,String loaded){
                // A loaded iframe is NOT proof that a playable video exists.
                setStatus(current.lesson.access.equals("premium")?
                    "المشغّل مفتوح. قد يطلب مزود الفيديو اشتراكاً؛ تحكّم بالتشغيل من داخل الفيديو.":
                    "المشغّل مفتوح؛ استخدم أدوات التشغيل الداخلية للفيديو.");
            }
            @Override public void onReceivedError(WebView view,android.webkit.WebResourceRequest request,android.webkit.WebResourceError error){
                if(request.isForMainFrame())setStatus("تعذر عرض الفيديو. تحقق من اتصال الإنترنت.");
            }
        });
        videoBox.addView(web,new FrameLayout.LayoutParams(-1,-1));
        String safe=android.text.TextUtils.htmlEncode(link);
        String html="<!doctype html><html><head><meta name='viewport' content='width=device-width,initial-scale=1'>"+
            "<style>html,body{margin:0;background:#000;width:100%;height:100%;overflow:hidden}iframe{width:100%;height:100%;border:0}</style></head>"+
            "<body><iframe src=\""+safe+"\" allow=\"autoplay;fullscreen;picture-in-picture;encrypted-media\" allowfullscreen></iframe></body></html>";
        web.loadDataWithBaseURL("https://"+host+"/",html,"text/html","UTF-8",null);
        setStatus(current.lesson.access.equals("premium")?
            "قد تتطلب المحاضرة اشتراكاً لدى مزود الفيديو.":"جاري تحميل المشغّل...");
    }
    private void exitEmbeddedFullScreen(){
        if(embeddedFullScreen!=null){
            ViewGroup decor=(ViewGroup)getWindow().getDecorView();
            decor.removeView(embeddedFullScreen);
            embeddedFullScreen=null;
            getWindow().getDecorView().setSystemUiVisibility(0);
            if(embeddedFullScreenCallback!=null){
                embeddedFullScreenCallback.onCustomViewHidden();
                embeddedFullScreenCallback=null;
            }
        }
    }
    private void destroyPlayback(){
        exitEmbeddedFullScreen();
        if(player!=null){if(playerView!=null)playerView.setPlayer(null);player.release();player=null;}
        if(web!=null){web.loadUrl("about:blank");web.stopLoading();web.destroy();web=null;}
        videoBox.removeAllViews();playerView=null;
    }
    private void savePosition(){
        if(player!=null&&current!=null&&player.getPlaybackState()!=Player.STATE_ENDED){
            long pos=player.getCurrentPosition();
            if(pos>=0)prefs.edit().putLong("position_"+current.key(),pos).apply();
        }
    }
    private void step(int change){
        int i=current.li+change;Catalog.Ref nextRef=null;
        while(i>=0&&i<current.chapter.lessons.size()){
            Catalog.Ref r=Catalog.ref(this,current.ti,current.ci,i);
            if(r!=null&&r.lesson.available()){nextRef=r;break;}i+=change;
        }
        if(nextRef==null){Toast.makeText(this,"لا توجد محاضرة أخرى متاحة",Toast.LENGTH_SHORT).show();return;}
        savePosition();saveNote();current=nextRef;
        prefs.edit().putInt("last_t",current.ti).putInt("last_c",current.ci).putInt("last_l",current.li).apply();
        load();
    }
    private void refreshActions(){
        String key=current.key();boolean fav=prefs.getBoolean("fav_"+key,false),done=prefs.getBoolean("done_"+key,false);
        favorite.setText(fav?"★ في المفضلة":"☆ المفضلة");
        finished.setText(done?"✓ مكتملة":"○ مكتملة");
        prev.setEnabled(current.li>0);prev.setAlpha(current.li>0?1f:0.45f);
        next.setEnabled(current.li<current.chapter.lessons.size()-1);
        next.setAlpha(current.li<current.chapter.lessons.size()-1?1f:0.45f);
    }
    private void toggleFavorite(){
        String key="fav_"+current.key();prefs.edit().putBoolean(key,!prefs.getBoolean(key,false)).apply();refreshActions();
    }
    private void toggleCompleted(){
        String key="done_"+current.key();prefs.edit().putBoolean(key,!prefs.getBoolean(key,false)).apply();refreshActions();
    }
    private void nextSpeed(){
        if(player==null){Toast.makeText(this,"السرعة تُضبط من أدوات المشغّل الحالي",Toast.LENGTH_SHORT).show();return;}
        float[] values={1f,1.25f,1.5f,2f,.75f};
        int index=0;for(int i=0;i<values.length;i++)if(Math.abs(speed-values[i])<.01f)index=i;
        speed=values[(index+1)%values.length];
        player.setPlaybackParameters(new PlaybackParameters(speed));
        speedButton.setText("السرعة "+speed+"×");
    }
    private void toggleOrientation(){
        landscape=!landscape;
        setRequestedOrientation(landscape?ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE:ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        updateOrientation();
    }
    private void enterPip(){
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.O||player==null){
            Toast.makeText(this,"النافذة العائمة متاحة لمشغّل الفيديو الأصلي فقط",Toast.LENGTH_SHORT).show();return;
        }
        try{setPictureInPictureParams(new PictureInPictureParams.Builder().setAspectRatio(new Rational(16,9)).build());enterPictureInPictureMode();}
        catch(Exception e){Toast.makeText(this,"الجهاز لا يدعم النافذة العائمة",Toast.LENGTH_SHORT).show();}
    }
    private void selectTab(String t){saveNote();tab=t;showTab();}
    private void showTab(){
        if(tabContent==null||current==null)return;
        tabContent.removeAllViews();noteInput=null;
        if(tab.equals("lessons")){
            tabContent.addView(Ui.txt(this,"قائمة محاضرات "+current.chapter.title,17,Ui.WHITE,true));
            for(int i=0;i<current.chapter.lessons.size();i++){
                Catalog.Ref ref=Catalog.ref(this,current.ti,current.ci,i);if(ref==null)continue;
                final Catalog.Ref chosen=ref;
                TextView v=Ui.txt(this,(i+1)+"  •  "+ref.lesson.title,13,
                    i==current.li?Ui.MINT:(ref.lesson.available()?Ui.WHITE:Ui.MUTED),i==current.li);
                Ui.pad(this,v,12,13);v.setBackground(Ui.round(this,i==current.li?0xff15483c:Ui.PANEL,11));
                tabContent.addView(v,Ui.top(this,-1,-2,6));
                v.setOnClickListener(w->{
                    if(!chosen.lesson.available()){Toast.makeText(this,"هذه المحاضرة غير متاحة حالياً",Toast.LENGTH_SHORT).show();return;}
                    savePosition();saveNote();current=chosen;load();
                });
            }
        }else if(tab.equals("notes")){
            tabContent.addView(Ui.txt(this,"ملاحظاتي على هذه المحاضرة",17,Ui.WHITE,true));
            noteInput=new EditText(this);noteInput.setTextColor(Ui.WHITE);
            noteInput.setTextSize(15);noteInput.setHintTextColor(Ui.MUTED);
            noteInput.setHint("اكتب ملاحظاتك هنا...");noteInput.setMinLines(4);
            noteInput.setGravity(Gravity.TOP|Gravity.RIGHT);
            Ui.pad(this,noteInput,12,13);noteInput.setBackground(Ui.round(this,Ui.PANEL,13));
            noteInput.setText(prefs.getString("note_"+current.key(),""));
            tabContent.addView(noteInput,Ui.top(this,-1,-2,12));
            TextView save=Ui.btn(this,"حفظ الملاحظات",true);tabContent.addView(save,Ui.top(this,-1,44,12));
            save.setOnClickListener(v->{saveNote();Toast.makeText(this,"تم حفظ الملاحظات",Toast.LENGTH_SHORT).show();});
        }else{
            LinearLayout empty=Ui.card(this);
            empty.addView(Ui.txt(this,"لا توجد ملفات مرفقة بهذه المحاضرة",16,Ui.WHITE,true));
            TextView hint=Ui.txt(this,"يمكنك استخدام تبويب الملاحظات لحفظ ملخصك الخاص.",12,Ui.MUTED,false);
            empty.addView(hint,Ui.top(this,-1,-2,9));
            tabContent.addView(empty,Ui.top(this,-1,-2,8));
        }
    }
    private void saveNote(){if(noteInput!=null&&current!=null)
        prefs.edit().putString("note_"+current.key(),noteInput.getText().toString()).apply();}
    private void updateOrientation(){
        if(videoBox==null || playerShell==null)return;
        boolean wide=getResources().getConfiguration().orientation==
            Configuration.ORIENTATION_LANDSCAPE;
        landscape=wide;
        if(topbar!=null)topbar.setVisibility(wide?View.GONE:View.VISIBLE);
        if(heading!=null)heading.setVisibility(wide?View.GONE:View.VISIBLE);
        if(info!=null)info.setVisibility(wide?View.GONE:View.VISIBLE);
        if(details!=null)details.setVisibility(wide?View.GONE:View.VISIBLE);
        if(actions!=null)actions.setVisibility(wide?View.GONE:View.VISIBLE);
        LinearLayout.LayoutParams shell=new LinearLayout.LayoutParams(-1,wide?0:-2,wide?1f:0f);
        shell.topMargin=wide?0:dp(10);
        playerShell.setLayoutParams(shell);
        LinearLayout.LayoutParams video=new LinearLayout.LayoutParams(-1,wide?0:videoHeight(),wide?1f:0f);
        videoBox.setLayoutParams(video);
        if(fullscreenButton!=null)fullscreenButton.setText(wide?"تصغير":"تكبير");
        ViewCompat.requestApplyInsets(root);
    }
    @Override public void onBackPressed(){
        if(embeddedFullScreen!=null){exitEmbeddedFullScreen();return;}
        if(landscape){setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);return;}
        super.onBackPressed();
    }
    @Override public void onPictureInPictureModeChanged(boolean pip,Configuration config){
        super.onPictureInPictureModeChanged(pip,config);
        if(topbar!=null)topbar.setVisibility(pip?View.GONE:View.VISIBLE);
        if(details!=null)details.setVisibility(pip?View.GONE:View.VISIBLE);
        if(heading!=null)heading.setVisibility(pip?View.GONE:View.VISIBLE);
        if(info!=null)info.setVisibility(pip?View.GONE:View.VISIBLE);
    }
    @Override public void onConfigurationChanged(Configuration cfg){super.onConfigurationChanged(cfg);updateOrientation();}
    @Override protected void onPause(){savePosition();saveNote();super.onPause();}
    @Override protected void onDestroy(){saveNote();destroyPlayback();super.onDestroy();}
}
