package com.sadsiyoon.app;

import android.app.AlertDialog;
import android.app.PictureInPictureParams;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import android.widget.SeekBar;
import android.text.InputType;
import java.util.List;
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
    private FrameLayout videoBox,mediaStage;
    private LinearLayout root,topbar,tabContent,details,playerShell,settingsPanel,playerChrome;
    private TextView heading,info,status,favorite,finished,prev,next,speedButton,fullscreenButton,pipButton,playPauseButton;
    private TextView tabsLessons,tabsNotes,tabsBooks,clock,seekBackButton,seekForwardButton;
    private SeekBar seekBar;
    private boolean userSeeking=false;
    private final Handler ticker=new Handler(Looper.getMainLooper());
    private final Runnable progressTick=new Runnable(){
        @Override public void run(){updateProgress();ticker.postDelayed(this,1000);}
    };
    private boolean landscape=false;
    private boolean pipActive=false;
    private String tab="lessons";
    private boolean toolsVisible=false;
    private float speed=1f;

    @Override public void onCreate(Bundle saved){
        super.onCreate(saved);
        WindowCompat.setDecorFitsSystemWindows(getWindow(),false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(0);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
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
        ViewCompat.setOnApplyWindowInsetsListener(root,(v,insets)->{
            Insets bars=insets.getInsets(WindowInsetsCompat.Type.systemBars());
            boolean wide=getResources().getConfiguration().orientation==
                Configuration.ORIENTATION_LANDSCAPE;
            root.setPadding(dp(wide?5:12),wide?bars.top:bars.top+dp(5),
                dp(wide?5:12),wide?bars.bottom:bars.bottom+dp(5));
            return insets;
        });
        setContentView(root);
        ViewCompat.requestApplyInsets(root);

        topbar=Ui.row(this);
        TextView back=Ui.btn(this,"رجوع",false);
        back.setTextSize(13);
        topbar.addView(back,Ui.lp(this,70,44));
        back.setOnClickListener(v->finish());
        LinearLayout labels=Ui.col(this);labels.setPadding(dp(12),0,0,0);
        labels.addView(Ui.txt(this,"سادسيون",19,Ui.WHITE,true));
        labels.addView(Ui.txt(this,"مشغل المحاضرات • "+current.teacher.batch,11,Ui.MUTED,false));
        topbar.addView(labels,new LinearLayout.LayoutParams(0,dp(45),1f));
        root.addView(topbar,Ui.lp(this,-1,51));

        playerShell=Ui.col(this);
        playerShell.setBackground(Ui.round(this,0xff111F34,18));
        playerShell.setClipToOutline(true);
        videoBox=new FrameLayout(this);
        videoBox.setBackgroundColor(Color.BLACK);
        mediaStage=new FrameLayout(this);
        videoBox.addView(mediaStage,new FrameLayout.LayoutParams(-1,-1));

        // All application playback actions are inside the video frame.
        playerChrome=Ui.col(this);
        playerChrome.setBackgroundColor(0x990C192C);
        LinearLayout row=Ui.row(this);
        prev=playerButton(row,"السابق",()->step(-1),false);
        playPauseButton=playerButton(row,"تشغيل",this::togglePlay,false);
        next=playerButton(row,"التالي",()->step(1),false);
        playerButton(row,"الأدوات",this::toggleTools,true);
        fullscreenButton=playerButton(row,"تكبير",this::toggleOrientation,false);
        playerChrome.addView(row);
        FrameLayout.LayoutParams chromePos=new FrameLayout.LayoutParams(-1,dp(47),Gravity.TOP);
        videoBox.addView(playerChrome,chromePos);

        // Native streams have a single app-owned progress bar; provider embeds retain
        // their own playback UI because the app cannot control their video timeline.
        LinearLayout timeline=Ui.col(this);
        timeline.setBackgroundColor(0xA60C192C);
        seekBar=new SeekBar(this);
        seekBar.setMax(1000);
        seekBar.setProgressTintList(ColorStateList.valueOf(Ui.MINT));
        seekBar.setThumbTintList(ColorStateList.valueOf(Ui.MINT));
        timeline.addView(seekBar,Ui.lp(this,-1,29));
        clock=Ui.txt(this,"00:00 / 00:00",11,Ui.WHITE,false);
        clock.setGravity(Gravity.CENTER);
        timeline.addView(clock,Ui.lp(this,-1,21));
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override public void onProgressChanged(SeekBar bar,int value,boolean fromUser){}
            @Override public void onStartTrackingTouch(SeekBar bar){userSeeking=true;}
            @Override public void onStopTrackingTouch(SeekBar bar){
                userSeeking=false;
                if(player!=null){
                    long duration=player.getDuration();
                    if(duration>0 && duration!=androidx.media3.common.C.TIME_UNSET)
                        player.seekTo(duration*bar.getProgress()/1000);
                }
            }
        });
        FrameLayout.LayoutParams timelinePos=new FrameLayout.LayoutParams(-1,dp(52),Gravity.BOTTOM);
        videoBox.addView(timeline,timelinePos);
        seekBar.setTag(timeline);

        settingsPanel=Ui.col(this);
        Ui.pad(this,settingsPanel,8,9);
        settingsPanel.setBackground(Ui.round(this,0xf014263f,15));
        TextView close=Ui.txt(this,"إعدادات المشغل   ✕",13,Ui.WHITE,true);
        close.setGravity(Gravity.CENTER);
        settingsPanel.addView(close,Ui.lp(this,-1,28));
        close.setOnClickListener(v->setToolsVisible(false));
        LinearLayout controls=Ui.row(this);
        speedButton=panelButton(controls,"السرعة 1×",this::nextSpeed);
        pipButton=panelButton(controls,"نافذة عائمة",this::enterPip);
        panelButton(controls,"إعادة",this::load);
        settingsPanel.addView(controls,Ui.top(this,-1,40,5));
        LinearLayout controls2=Ui.row(this);
        favorite=panelButton(controls2,"☆ المفضلة",this::toggleFavorite);
        finished=panelButton(controls2,"○ مكتملة",this::toggleCompleted);
        seekBackButton=panelButton(controls2,"إعادة 10ث",()->seekRelative(-10000));
        seekForwardButton=panelButton(controls2,"تقديم 10ث",()->seekRelative(10000));
        settingsPanel.addView(controls2,Ui.top(this,-1,40,5));
        FrameLayout.LayoutParams menuPos=new FrameLayout.LayoutParams(-1,-2,Gravity.BOTTOM);
        menuPos.leftMargin=dp(4);menuPos.rightMargin=dp(4);
        videoBox.addView(settingsPanel,menuPos);
        settingsPanel.setVisibility(View.GONE);

        playerShell.addView(videoBox,new LinearLayout.LayoutParams(-1,videoHeight()));
        root.addView(playerShell,Ui.top(this,-1,-2,8));

        heading=Ui.txt(this,"",18,Ui.WHITE,true);
        heading.setMaxLines(2);heading.setEllipsize(TextUtils.TruncateAt.END);
        root.addView(heading,Ui.top(this,-1,-2,10));
        info=Ui.txt(this,"",12,Ui.MINT,false);
        info.setMaxLines(2);
        root.addView(info,Ui.top(this,-1,-2,3));

        details=Ui.col(this);
        status=Ui.txt(this,"",12,Ui.MUTED,false);
        status.setMaxLines(2);status.setEllipsize(TextUtils.TruncateAt.END);
        details.addView(status,Ui.top(this,-1,-2,7));
        LinearLayout tabs=Ui.row(this);
        tabsLessons=addTab(tabs,"المحاضرات",()->selectTab("lessons"));
        tabsNotes=addTab(tabs,"الملاحظات",()->selectTab("notes"));
        tabsBooks=addTab(tabs,"الملازم",()->selectTab("books"));
        details.addView(tabs,Ui.top(this,-1,42,11));
        ScrollView listScroll=new ScrollView(this);
        listScroll.setFillViewport(true);
        listScroll.setVerticalScrollBarEnabled(false);
        tabContent=Ui.col(this);Ui.pad(this,tabContent,2,6);
        listScroll.addView(tabContent);
        details.addView(listScroll,new LinearLayout.LayoutParams(-1,0,1f));
        root.addView(details,new LinearLayout.LayoutParams(-1,0,1f));
        showTab();updateOrientation();
    }
    private TextView playerButton(LinearLayout container,String title,Runnable action,boolean highlighted){
        TextView button=Ui.txt(this,title,12,highlighted?Ui.MINT:Ui.WHITE,true);
        button.setGravity(Gravity.CENTER);
        button.setSingleLine(true);
        button.setAutoSizeTextTypeUniformWithConfiguration(9,12,1,TypedValue.COMPLEX_UNIT_SP);
        button.setContentDescription(title);
        LinearLayout.LayoutParams layout=new LinearLayout.LayoutParams(0,dp(47),1);
        container.addView(button,layout);
        button.setOnClickListener(v->action.run());
        return button;
    }
    private TextView panelButton(LinearLayout container,String title,Runnable action){
        TextView button=Ui.btn(this,title,false);
        button.setTextSize(12);button.setSingleLine(true);
        button.setAutoSizeTextTypeUniformWithConfiguration(9,12,1,TypedValue.COMPLEX_UNIT_SP);
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(0,dp(40),1f);
        params.setMargins(dp(2),0,dp(2),0);
        container.addView(button,params);
        button.setOnClickListener(v->action.run());
        return button;
    }
    private TextView addTab(LinearLayout container,String title,Runnable action){
        TextView button=Ui.txt(this,title,13,Ui.MUTED,true);
        button.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(0,dp(42),1f);
        params.setMargins(dp(2),0,dp(2),0);
        container.addView(button,params);
        button.setOnClickListener(v->action.run());
        return button;
    }
    private void styleTabs(){
        if(tabsLessons==null)return;
        TextView[] tabs={tabsLessons,tabsNotes,tabsBooks};
        String[] names={"lessons","notes","books"};
        for(int i=0;i<tabs.length;i++){
            boolean selected=tab.equals(names[i]);
            tabs[i].setTextColor(selected?Ui.WHITE:Ui.MUTED);
            tabs[i].setBackground(Ui.round(this,selected?Ui.BLUE:Ui.PANEL2,11));
        }
    }
    private void setToolsVisible(boolean visible){
        toolsVisible=visible;
        if(settingsPanel!=null)settingsPanel.setVisibility(visible&&!pipActive?View.VISIBLE:View.GONE);
    }
    private void toggleTools(){setToolsVisible(!toolsVisible);}
    private void togglePlay(){
        if(player==null){
            Toast.makeText(this,"التحكم بالتشغيل من أدوات مزود الفيديو",Toast.LENGTH_SHORT).show();
            return;
        }
        if(player.isPlaying())player.pause();else player.play();
        updateProgress();
    }
    private void seekRelative(long distance){
        if(player==null)return;
        long duration=player.getDuration();
        long target=Math.max(0L,player.getCurrentPosition()+distance);
        if(duration>0 && duration!=androidx.media3.common.C.TIME_UNSET)
            target=Math.min(target,duration);
        player.seekTo(target);
        updateProgress();
    }
    private static String mmss(long millis){
        long seconds=Math.max(0,millis/1000);
        return String.format(java.util.Locale.US,"%02d:%02d",seconds/60,seconds%60);
    }
    private void updateProgress(){
        boolean nativeAvailable=player!=null;
        if(playPauseButton!=null){
            playPauseButton.setText(nativeAvailable?(player.isPlaying()?"إيقاف":"تشغيل"):"المزود");
            playPauseButton.setEnabled(nativeAvailable);
            playPauseButton.setAlpha(nativeAvailable?1f:0.5f);
        }
        if(seekBar==null||clock==null||!nativeAvailable)return;
        long duration=player.getDuration(),position=Math.max(0,player.getCurrentPosition());
        if(duration>0 && duration!=androidx.media3.common.C.TIME_UNSET){
            if(!userSeeking)seekBar.setProgress((int)Math.min(1000,position*1000/duration));
            clock.setText(mmss(position)+" / "+mmss(duration));
        }else clock.setText(mmss(position)+" / --:--");
    }
    private void updateAvailableControls(){
        boolean nativePlayer=current!=null &&
            ("hls".equals(current.lesson.type)||"mp4".equals(current.lesson.type));
        if(speedButton!=null)speedButton.setVisibility(nativePlayer?View.VISIBLE:View.GONE);
        if(pipButton!=null)pipButton.setVisibility(nativePlayer?View.VISIBLE:View.GONE);
        if(seekBackButton!=null)seekBackButton.setVisibility(nativePlayer?View.VISIBLE:View.GONE);
        if(seekForwardButton!=null)seekForwardButton.setVisibility(nativePlayer?View.VISIBLE:View.GONE);
        if(seekBar!=null && seekBar.getTag() instanceof View)
            ((View)seekBar.getTag()).setVisibility(nativePlayer?View.VISIBLE:View.GONE);
        if(playPauseButton!=null){
            playPauseButton.setVisibility(nativePlayer?View.VISIBLE:View.GONE);
        }
    }
    private void setStatus(String msg){if(status!=null)status.setText(msg);}
    private void load(){
        if(current==null)return;
        heading.setText(current.lesson.title);
        info.setText(current.teacher.name+"  •  "+current.chapter.title+"  •  دفعة "+current.teacher.batch);
        setToolsVisible(false);
        if(seekBar!=null)seekBar.setProgress(0);
        if(clock!=null)clock.setText("00:00 / 00:00");
        refreshActions();destroyPlayback();
        updateAvailableControls();
        if(!current.lesson.available()){setStatus("هذه المحاضرة غير متاحة للتشغيل حالياً");showUnavailable();return;}
        setStatus(current.lesson.access.equals("premium")?
            "قد تتطلب المشاهدة اشتراكاً لدى مزود الفيديو.":
            "جاري تحميل مشغّل المحاضرة...");
        if(current.lesson.type.equals("hls")||current.lesson.type.equals("mp4"))openNative();
        else if(current.lesson.type.equals("embed"))openEmbedded();
        else showUnavailable();
        showTab();
    }
    private void showUnavailable(){
        TextView empty=Ui.txt(this,"لا يمكن تشغيل هذه المحاضرة حالياً",16,Ui.WHITE,true);
        empty.setGravity(Gravity.CENTER);mediaStage.addView(empty,new FrameLayout.LayoutParams(-1,-1));
    }
    private void openNative(){
        playerView=new PlayerView(this);playerView.setUseController(false);
        playerView.setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS);
        playerView.setBackgroundColor(Color.BLACK);
        mediaStage.addView(playerView,new FrameLayout.LayoutParams(-1,-1));
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
                    "تم تحميل صفحة المشغّل، لكن تشغيل الفيديو لم يتأكد وقد يحتاج Premium.":
                    "تم تحميل صفحة المشغّل، لا يمكن تأكيد التشغيل حتى يبدأ الفيديو فعلياً.");
            }
            @Override public void onReceivedError(WebView view,android.webkit.WebResourceRequest request,android.webkit.WebResourceError error){
                if(request.isForMainFrame())setStatus("تعذر تحميل صفحة المشغّل. تحقق من الإنترنت أو أعد المحاولة.");
            }
        });
        mediaStage.addView(web,new FrameLayout.LayoutParams(-1,-1));
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
        mediaStage.removeAllViews();playerView=null;
        updateProgress();
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
        savePosition();current=nextRef;
        prefs.edit().putInt("last_t",current.ti).putInt("last_c",current.ci).putInt("last_l",current.li).apply();
        load();
    }
    private void refreshActions(){
        String key=current.key();boolean fav=prefs.getBoolean("fav_"+key,false),done=prefs.getBoolean("done_"+key,false);
        favorite.setText(fav?"★ محفوظة":"☆ المفضلة");
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
        // Android will call onConfigurationChanged when dimensions actually update.
    }
    private void enterPip(){
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.O||player==null){
            Toast.makeText(this,"النافذة العائمة متاحة لمشغّل الفيديو الأصلي فقط",Toast.LENGTH_SHORT).show();return;
        }
        try{setPictureInPictureParams(new PictureInPictureParams.Builder().setAspectRatio(new Rational(16,9)).build());enterPictureInPictureMode();}
        catch(Exception e){Toast.makeText(this,"الجهاز لا يدعم النافذة العائمة",Toast.LENGTH_SHORT).show();}
    }
    private void selectTab(String t){tab=t;showTab();}
    private void showTab(){
        if(tabContent==null||current==null)return;
        tabContent.removeAllViews();
        styleTabs();
        if(tab.equals("lessons")){
            tabContent.addView(Ui.txt(this,"محاضرات "+current.chapter.title,16,Ui.WHITE,true));
            tabContent.addView(Ui.txt(this,current.chapter.lessons.size()+" محاضرة • الدفعة "+current.teacher.batch,
                12,Ui.MUTED,false),Ui.top(this,-1,-2,4));
            for(int i=0;i<current.chapter.lessons.size();i++){
                Catalog.Ref ref=Catalog.ref(this,current.ti,current.ci,i);if(ref==null)continue;
                final Catalog.Ref chosen=ref;
                boolean selected=i==current.li;
                LinearLayout row=Ui.row(this);
                Ui.pad(this,row,12,11);
                row.setBackground(Ui.round(this,selected?0xff244A76:Ui.PANEL,12));
                TextView number=Ui.txt(this,String.valueOf(i+1),14,selected?Ui.MINT:Ui.MUTED,true);
                number.setGravity(Gravity.CENTER);
                row.addView(number,Ui.lp(this,30,31));
                LinearLayout text=Ui.col(this);
                text.addView(Ui.txt(this,ref.lesson.title,13,selected?Ui.MINT:Ui.WHITE,selected));
                text.addView(Ui.txt(this,selected?"قيد المشاهدة":
                    !ref.lesson.available()?"غير متاحة":"افتح المحاضرة",11,Ui.MUTED,false),
                    Ui.top(this,-1,-2,3));
                row.addView(text,new LinearLayout.LayoutParams(0,-2,1f));
                tabContent.addView(row,Ui.top(this,-1,-2,7));
                row.setOnClickListener(w->{
                    if(!chosen.lesson.available()){
                        Toast.makeText(this,"هذه المحاضرة غير متاحة حالياً",Toast.LENGTH_SHORT).show();return;
                    }
                    savePosition();current=chosen;
                    prefs.edit().putInt("last_t",current.ti).putInt("last_c",current.ci)
                        .putInt("last_l",current.li).apply();
                    load();
                });
            }
        }else if(tab.equals("notes")){
            LinearLayout headingRow=Ui.row(this);
            headingRow.addView(Ui.txt(this,"ملاحظات المحاضرة",17,Ui.WHITE,true),
                new LinearLayout.LayoutParams(0,dp(43),1f));
            TextView add=Ui.btn(this,"+ ملاحظة",true);add.setTextSize(12);
            headingRow.addView(add,Ui.lp(this,108,42));
            add.setOnClickListener(v->editNote(-1));
            tabContent.addView(headingRow);
            List<NoteCards.Note> notes;
            try{notes=NoteCards.list(this,current.key());}
            catch(Exception e){
                tabContent.addView(Ui.txt(this,"تعذر قراءة الملاحظات المخزنة. لم يتم تغييرها.",13,Ui.RED,false));
                return;
            }
            if(notes.isEmpty()){
                LinearLayout empty=Ui.card(this);
                empty.addView(Ui.txt(this,"ابدأ بأول ملاحظة",16,Ui.WHITE,true));
                empty.addView(Ui.txt(this,"اكتب عنواناً مثل «قانون فيزياء»، ثم أضف الملخص أو الشرح.",
                    13,Ui.MUTED,false),Ui.top(this,-1,-2,7));
                tabContent.addView(empty,Ui.top(this,-1,-2,9));
            }
            for(int i=0;i<notes.size();i++){
                int index=i;NoteCards.Note note=notes.get(i);
                LinearLayout card=Ui.card(this);
                LinearLayout titleRow=Ui.row(this);
                titleRow.addView(Ui.txt(this,note.title,16,Ui.MINT,true),
                    new LinearLayout.LayoutParams(0,-2,1f));
                TextView edit=Ui.btn(this,"تعديل",false);edit.setTextSize(11);
                titleRow.addView(edit,Ui.lp(this,62,39));
                TextView remove=Ui.btn(this,"حذف",false);remove.setTextSize(11);
                LinearLayout.LayoutParams removeParams=Ui.lp(this,56,39);
                removeParams.leftMargin=dp(5);titleRow.addView(remove,removeParams);
                card.addView(titleRow);
                TextView summary=Ui.txt(this,note.body,13,Ui.WHITE,false);
                summary.setTextIsSelectable(true);
                card.addView(summary,Ui.top(this,-1,-2,9));
                tabContent.addView(card,Ui.top(this,-1,-2,9));
                edit.setOnClickListener(v->editNote(index));
                remove.setOnClickListener(v->confirmDelete(index));
            }
        }else{
            LinearLayout empty=Ui.card(this);
            empty.addView(Ui.txt(this,"ملازم الأستاذ "+current.teacher.name,17,Ui.WHITE,true));
            empty.addView(Ui.txt(this,"لا توجد ملازم مضافة لهذا المدرس حالياً. ستظهر هنا ملازم PDF المرتبطة بالمدرس عندما تتوفر.",
                13,Ui.MUTED,false),Ui.top(this,-1,-2,9));
            tabContent.addView(empty,Ui.top(this,-1,-2,8));
        }
    }
    private void editNote(int index){
        List<NoteCards.Note> entries;
        try{entries=NoteCards.list(this,current.key());}
        catch(Exception e){
            Toast.makeText(this,"تعذر فتح الملاحظات المخزنة",Toast.LENGTH_LONG).show();return;
        }
        if(index>=entries.size())return;
        String lessonKey=current.key();
        LinearLayout form=Ui.col(this);
        Ui.pad(this,form,20,10);
        TextView titleLabel=Ui.txt(this,"عنوان الملاحظة",14,Ui.WHITE,true);
        form.addView(titleLabel);
        EditText title=new EditText(this);
        title.setSingleLine(true);title.setTextSize(15);title.setTextColor(Ui.WHITE);
        title.setHintTextColor(Ui.MUTED);title.setHint("مثال: قانون فيزياء");
        title.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(90)});
        title.setBackground(Ui.round(this,Ui.PANEL2,12));
        Ui.pad(this,title,12,10);
        form.addView(title,Ui.top(this,-1,48,7));
        form.addView(Ui.txt(this,"الموجز أو الشرح",14,Ui.WHITE,true),Ui.top(this,-1,-2,14));
        EditText body=new EditText(this);
        body.setTextSize(15);body.setTextColor(Ui.WHITE);
        body.setHintTextColor(Ui.MUTED);
        body.setHint("اكتب القانون والشرح أو أي ملاحظة ترغب بحفظها...");
        body.setMinLines(5);body.setMaxLines(9);
        body.setGravity(Gravity.TOP|Gravity.RIGHT);
        body.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE|
            InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        body.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(4000)});
        body.setBackground(Ui.round(this,Ui.PANEL2,12));
        Ui.pad(this,body,12,12);
        form.addView(body,Ui.top(this,-1,-2,7));
        if(index>=0){
            title.setText(entries.get(index).title);
            body.setText(entries.get(index).body);
        }
        AlertDialog dialog=new AlertDialog.Builder(this)
            .setTitle(index<0?"إضافة ملاحظة":"تعديل الملاحظة")
            .setView(form)
            .setNegativeButton("إلغاء",null)
            .setPositiveButton("حفظ",null).create();
        dialog.setOnShowListener(ignored->
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{
                String titleText=title.getText().toString().trim();
                String content=body.getText().toString().trim();
                if(titleText.isEmpty()){title.setError("أدخل عنواناً");return;}
                if(content.isEmpty()){body.setError("أدخل نص الملاحظة");return;}
                if(!NoteCards.save(this,lessonKey,index,titleText,content)){
                    Toast.makeText(this,"تعذر الحفظ أو تم بلوغ الحد المسموح للملاحظات",
                        Toast.LENGTH_LONG).show();return;
                }
                dialog.dismiss();
                if(current!=null&&lessonKey.equals(current.key())&&tab.equals("notes"))showTab();
            }));
        dialog.show();
    }
    private void confirmDelete(int index){
        String lessonKey=current.key();
        new AlertDialog.Builder(this)
            .setTitle("حذف الملاحظة؟")
            .setMessage("سيُحذف عنوان الملاحظة ونصها نهائياً من هذا الجهاز.")
            .setNegativeButton("إلغاء",null)
            .setPositiveButton("حذف",(d,w)->{
                boolean removed=NoteCards.delete(this,lessonKey,index);
                if(!removed)Toast.makeText(this,"تعذر حذف الملاحظة",Toast.LENGTH_SHORT).show();
                if(current!=null&&lessonKey.equals(current.key())&&tab.equals("notes"))showTab();
            }).show();
    }
    private void updateOrientation(){
        if(videoBox==null || playerShell==null)return;
        boolean wide=getResources().getConfiguration().orientation==
            Configuration.ORIENTATION_LANDSCAPE;
        landscape=wide;
        boolean expanded=wide||pipActive;
        if(topbar!=null)topbar.setVisibility(expanded?View.GONE:View.VISIBLE);
        if(heading!=null)heading.setVisibility(expanded?View.GONE:View.VISIBLE);
        if(info!=null)info.setVisibility(expanded?View.GONE:View.VISIBLE);
        if(details!=null)details.setVisibility(expanded?View.GONE:View.VISIBLE);
        if(playerChrome!=null)playerChrome.setVisibility(pipActive?View.GONE:View.VISIBLE);
        if(pipActive)setToolsVisible(false);
        if(seekBar!=null && seekBar.getTag() instanceof View)
            ((View)seekBar.getTag()).setVisibility(expanded && pipActive?View.GONE:
                ("hls".equals(current.lesson.type)||"mp4".equals(current.lesson.type))?View.VISIBLE:View.GONE);
        LinearLayout.LayoutParams shell=new LinearLayout.LayoutParams(-1,expanded?0:-2,expanded?1f:0f);
        shell.topMargin=expanded?0:dp(10);
        playerShell.setLayoutParams(shell);
        LinearLayout.LayoutParams video=new LinearLayout.LayoutParams(-1,expanded?0:videoHeight(),expanded?1f:0f);
        videoBox.setLayoutParams(video);
        if(fullscreenButton!=null)fullscreenButton.setText(wide?"تصغير":"ملء الشاشة");
        ViewCompat.requestApplyInsets(root);
    }
    @Override public void onBackPressed(){
        if(embeddedFullScreen!=null){exitEmbeddedFullScreen();return;}
        if(landscape){setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);return;}
        super.onBackPressed();
    }
    @Override public void onPictureInPictureModeChanged(boolean pip,Configuration config){
        super.onPictureInPictureModeChanged(pip,config);
        pipActive=pip;
        updateOrientation();
    }
    @Override public void onConfigurationChanged(Configuration cfg){super.onConfigurationChanged(cfg);updateOrientation();}
    @Override protected void onResume(){super.onResume();ticker.removeCallbacks(progressTick);ticker.post(progressTick);}
    @Override protected void onPause(){ticker.removeCallbacks(progressTick);savePosition();super.onPause();}
    @Override protected void onDestroy(){ticker.removeCallbacks(progressTick);destroyPlayback();super.onDestroy();}
}
