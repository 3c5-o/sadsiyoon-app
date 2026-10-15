package com.sadsiyoon.app;

import android.app.Activity;
import android.content.res.Configuration;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.ViewCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

public final class RemotePlayerActivity extends AppCompatActivity {
    private ExoPlayer player;
    private PlayerView playerView;
    private WebView web;
    private FrameLayout videoStage;
    private TextView status;
    private String title,url,type,id;
    private SharedPreferences prefs;
    private View embeddedFull;
    private WebChromeClient.CustomViewCallback embeddedCallback;
    private int dp(int value){return Ui.dp(this,value);}
    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        WindowCompat.setDecorFitsSystemWindows(getWindow(),false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        prefs=getSharedPreferences("sadsiyoon_local_v1",MODE_PRIVATE);
        title=getIntent().getStringExtra("title");
        url=getIntent().getStringExtra("url");
        type=getIntent().getStringExtra("type");
        id=getIntent().getStringExtra("id");
        if(title==null)title="محاضرة سادسيون";
        if(id==null)id="";
        if(url==null||!url.startsWith("https://")){finish();return;}
        if(type==null)type="";
        build();
        play();
    }
    private void build(){
        LinearLayout root=Ui.col(this);root.setBackgroundColor(Ui.BG);
        ViewCompat.setOnApplyWindowInsetsListener(root,(v,insets)->{
            Insets bars=insets.getInsets(WindowInsetsCompat.Type.systemBars());
            root.setPadding(bars.left,bars.top,bars.right,bars.bottom);
            return insets;
        });
        LinearLayout toolbar=Ui.row(this);Ui.pad(this,toolbar,11,9);
        TextView back=Ui.btn(this,"رجوع",false);
        back.setOnClickListener(v->finish());toolbar.addView(back,Ui.lp(this,66,42));
        TextView name=Ui.txt(this,title,17,Ui.WHITE,true);
        name.setMaxLines(2);name.setPadding(dp(9),0,dp(9),0);
        toolbar.addView(name,new LinearLayout.LayoutParams(0,-2,1));
        TextView fullscreen=Ui.btn(this,"تكبير",true);
        fullscreen.setOnClickListener(v->{
            boolean wide=getResources().getConfiguration().orientation==Configuration.ORIENTATION_LANDSCAPE;
            setRequestedOrientation(wide?ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        });
        toolbar.addView(fullscreen,Ui.lp(this,72,42));root.addView(toolbar);
        videoStage=new FrameLayout(this);videoStage.setBackgroundColor(Color.BLACK);
        int width=getResources().getDisplayMetrics().widthPixels;
        int height=Math.max(dp(155),Math.round(width*9f/16f));
        root.addView(videoStage,Ui.lp(this,-1,Math.round(height/getResources().getDisplayMetrics().density)));
        status=Ui.txt(this,"جاري تحميل المحاضرة...",13,Ui.MUTED,false);
        status.setPadding(dp(15),dp(13),dp(15),dp(13));root.addView(status);
        TextView note=Ui.txt(this,"المحتوى يعمل من المصدر المنشور، وقد تتطلب بعض الروابط إذن مشاهدة من مزودها.",12,Ui.MUTED,false);
        note.setPadding(dp(15),0,dp(15),0);root.addView(note);
        setContentView(root);ViewCompat.requestApplyInsets(root);
    }
    private void play(){
        String lower=url.toLowerCase(java.util.Locale.ROOT);
        boolean direct="mp4".equalsIgnoreCase(type)||"hls".equalsIgnoreCase(type)||
            lower.contains(".mp4")||lower.contains(".m3u8");
        if(direct){
            playerView=new PlayerView(this);playerView.setUseController(true);
            playerView.setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS);
            videoStage.addView(playerView,new FrameLayout.LayoutParams(-1,-1));
            player=new ExoPlayer.Builder(this).build();playerView.setPlayer(player);
            player.addListener(new Player.Listener(){
                @Override public void onPlaybackStateChanged(int state){
                    if(state==Player.STATE_READY)status.setText("المحاضرة جاهزة");
                    else if(state==Player.STATE_BUFFERING)status.setText("جاري تحميل الفيديو...");
                    else if(state==Player.STATE_ENDED)status.setText("اكتملت المحاضرة");
                }
                @Override public void onPlayerError(PlaybackException e){
                    status.setText("تعذر تشغيل الفيديو. افحص المصدر أو الاتصال.");
                    Toast.makeText(RemotePlayerActivity.this,"فشل تشغيل المحاضرة",Toast.LENGTH_LONG).show();
                }
            });
            MediaItem.Builder item=new MediaItem.Builder().setUri(url);
            if("hls".equalsIgnoreCase(type)||lower.contains(".m3u8"))
                item.setMimeType(MimeTypes.APPLICATION_M3U8);
            player.setMediaItem(item.build());player.prepare();
            if(!id.isEmpty())player.seekTo(prefs.getLong("remote_position_"+id,0));
            player.play();return;
        }
        Uri parsed=Uri.parse(url);
        String host=parsed.getHost();
        boolean allowed=host!=null && (host.equals("rumble.com")||host.equals("www.rumble.com")||
            host.equals("www.bitchute.com")||host.equals("bitchute.com")||
            host.equals("iframe.mediadelivery.net")||host.equals("ok.ru")||
            host.equals("vk.com")||host.equals("vkvideo.ru"));
        if(!"embed".equalsIgnoreCase(type)||!allowed){
            status.setText("هذا النوع غير مدعوم. يلزم رابط MP4 أو HLS أو تضمين من مصدر معتمد.");
            return;
        }
        web=new WebView(this);web.setBackgroundColor(Color.BLACK);
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setDomStorageEnabled(true);
        web.getSettings().setSafeBrowsingEnabled(true);
        web.getSettings().setAllowFileAccess(false);
        web.getSettings().setAllowContentAccess(false);
        web.getSettings().setMixedContentMode(android.webkit.WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        web.setWebChromeClient(new WebChromeClient(){
            @Override public void onShowCustomView(View view,CustomViewCallback callback){
                if(embeddedFull!=null){callback.onCustomViewHidden();return;}
                embeddedFull=view;embeddedCallback=callback;
                ((ViewGroup)getWindow().getDecorView()).addView(view,new ViewGroup.LayoutParams(-1,-1));
            }
            @Override public void onHideCustomView(){clearFull();}
        });
        web.setWebViewClient(new WebViewClient(){
            @Override public void onPageFinished(WebView w,String address){
                status.setText("تم تحميل المشغّل؛ التشغيل يعتمد على صلاحيات المصدر.");
            }
            @Override public boolean shouldOverrideUrlLoading(WebView w,android.webkit.WebResourceRequest request){
                return request.isForMainFrame();
            }
        });
        videoStage.addView(web,new FrameLayout.LayoutParams(-1,-1));
        String html="<html><head><meta name='viewport' content='width=device-width,initial-scale=1'>"+
            "<style>html,body,iframe{width:100%;height:100%;margin:0;border:0;background:#000}</style></head>"+
            "<body><iframe src=\""+android.text.TextUtils.htmlEncode(url)+"\" allow='autoplay;fullscreen;picture-in-picture' allowfullscreen></iframe></body></html>";
        web.loadDataWithBaseURL("https://"+host+"/",html,"text/html","UTF-8",null);
    }
    private void clearFull(){
        if(embeddedFull==null)return;
        ((ViewGroup)getWindow().getDecorView()).removeView(embeddedFull);
        embeddedFull=null;
        if(embeddedCallback!=null){embeddedCallback.onCustomViewHidden();embeddedCallback=null;}
    }
    @Override protected void onPause(){
        if(player!=null&&!id.isEmpty())
            prefs.edit().putLong("remote_position_"+id,Math.max(0,player.getCurrentPosition())).apply();
        super.onPause();
    }
    @Override public void onConfigurationChanged(Configuration config){
        super.onConfigurationChanged(config);
        if(videoStage!=null){
            boolean wide=config.orientation==Configuration.ORIENTATION_LANDSCAPE;
            int h=wide?getResources().getDisplayMetrics().heightPixels-dp(95):
                Math.round(getResources().getDisplayMetrics().widthPixels*9f/16f);
            videoStage.getLayoutParams().height=Math.max(dp(155),h);videoStage.requestLayout();
        }
    }
    @Override public void onBackPressed(){
        if(embeddedFull!=null){clearFull();return;}
        if(getResources().getConfiguration().orientation==Configuration.ORIENTATION_LANDSCAPE){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);return;
        }
        super.onBackPressed();
    }
    @Override protected void onDestroy(){
        clearFull();
        if(player!=null){player.release();player=null;}
        if(web!=null){web.stopLoading();web.destroy();web=null;}
        super.onDestroy();
    }
}
