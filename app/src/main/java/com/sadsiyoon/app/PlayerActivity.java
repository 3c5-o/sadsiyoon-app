package com.sadsiyoon.app;

import android.app.PictureInPictureParams;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Rational;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
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
    private ExoPlayer player;
    private PlayerView playerView;
    private LinearLayout root, header, footer;
    private TextView title, status, favoriteButton, doneButton, prevButton, nextButton, speedButton, rotateButton;
    private int index;
    private float speed=1f;
    private boolean expanded=false;
    private boolean switching=false;

    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        getWindow().setStatusBarColor(Ui.BG);getWindow().setNavigationBarColor(Ui.BG);
        prefs=getSharedPreferences("sadsiyoon_local_v1",MODE_PRIVATE);
        index=getIntent().getIntExtra("lesson_index",0);
        index=Math.max(0,Math.min(index,LessonCatalog.size()-1));
        buildScreen();
        buildPlayer();
        loadLesson();
    }

    private TextView addAction(LinearLayout group,String text,boolean primary,View.OnClickListener action){
        TextView a=Ui.action(this,text,primary);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,Ui.dp(this,43),1);
        p.setMargins(Ui.dp(this,3),0,Ui.dp(this,3),0);
        group.addView(a,p);a.setOnClickListener(action);return a;
    }

    private void buildScreen(){
        root=Ui.column(this);root.setBackgroundColor(Ui.BG);Ui.pad(this,root,12,12);
        setContentView(root);
        header=Ui.column(this);
        LinearLayout top=Ui.row(this);
        TextView back=Ui.action(this,"‹ رجوع",false);
        back.setOnClickListener(v->finish());
        top.addView(back);
        TextView brand=Ui.text(this,"سادسيون  |  الفصل الأول",16,Ui.MINT,true);
        brand.setGravity(Gravity.CENTER);top.addView(brand,new LinearLayout.LayoutParams(0,Ui.dp(this,42),1));
        header.addView(top);
        title=Ui.text(this,"",19,Ui.WHITE,true);
        title.setPadding(Ui.dp(this,4),Ui.dp(this,13),Ui.dp(this,4),Ui.dp(this,9));
        header.addView(title);root.addView(header);
        playerView=new PlayerView(this);
        playerView.setBackgroundColor(android.graphics.Color.BLACK);
        playerView.setUseController(true);
        playerView.setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS);
        root.addView(playerView,Ui.boxParams(this,240,10));
        status=Ui.text(this,"",12,Ui.MUTED,false);
        status.setPadding(Ui.dp(this,8),Ui.dp(this,12),Ui.dp(this,8),Ui.dp(this,12));
        root.addView(status);

        footer=Ui.column(this);
        LinearLayout row1=Ui.row(this);
        prevButton=addAction(row1,"السابق",false,v->change(-1));
        nextButton=addAction(row1,"التالي",true,v->change(1));
        footer.addView(row1);
        LinearLayout row2=Ui.row(this);
        speedButton=addAction(row2,"السرعة 1×",false,v->cycleSpeed());
        rotateButton=addAction(row2,"ملء الشاشة",false,v->toggleLandscape());
        addAction(row2,"نافذة عائمة",false,v->enterPip());
        footer.addView(row2,Ui.boxParams(this,43,10));
        LinearLayout row3=Ui.row(this);
        favoriteButton=addAction(row3,"☆ مفضلة",false,v->toggleFavorite());
        doneButton=addAction(row3,"☐ مكتملة",false,v->toggleDone());
        footer.addView(row3,Ui.boxParams(this,43,10));
        LinearLayout row4=Ui.row(this);
        addAction(row4,"نسخ رابط البث",false,v->copyLink());
        addAction(row4,"مشغل السيرفر",false,v->openServerPlayer());
        footer.addView(row4,Ui.boxParams(this,43,10));
        TextView note=Ui.text(this,"المشغّل الأصلي يشغّل ملف HLS مباشرة. خيار مشغل السيرفر يفتح صفحة العرض في المتصفح. لا يتم تنزيل الفيديو أو إعادة استضافته.",11,Ui.MUTED,false);
        note.setPadding(Ui.dp(this,6),Ui.dp(this,14),Ui.dp(this,6),0);footer.addView(note);
        root.addView(footer);
        updateOrientation();
    }

    private void buildPlayer(){
        player=new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);
        player.addListener(new Player.Listener(){
            @Override public void onPlaybackStateChanged(int state){
                if(state==Player.STATE_BUFFERING)status.setText("جاري تحميل المحاضرة...");
                if(state==Player.STATE_READY)status.setText("جاهز للمشاهدة • تشغيل HLS مباشر");
                if(state==Player.STATE_ENDED){
                    prefs.edit().putBoolean("done_"+index,true).putLong("position_"+index,0).apply();
                    refreshButtons();
                    if(index<LessonCatalog.size()-1)change(1);
                    else status.setText("أكملت الفصل الأول. أحسنت!");
                }
            }
            @Override public void onPlayerError(PlaybackException error){
                status.setText("تعذر تشغيل المصدر الحالي. تحقق من الإنترنت أو جرّب مشغل السيرفر. رمز الخطأ: "+error.errorCode);
            }
        });
    }

    private void loadLesson(){
        LessonCatalog.Lesson lesson=LessonCatalog.at(index);
        title.setText(lesson.title);
        status.setText("جاري الاتصال بالمصدر...");
        prevButton.setEnabled(index>0);prevButton.setAlpha(index>0?1f:.4f);
        nextButton.setEnabled(index<LessonCatalog.size()-1);nextButton.setAlpha(index<LessonCatalog.size()-1?1f:.4f);
        refreshButtons();
        MediaItem item=new MediaItem.Builder().setUri(Uri.parse(lesson.streamUrl())).setMimeType(MimeTypes.APPLICATION_M3U8).setMediaId(String.valueOf(index)).build();
        player.setMediaItem(item);
        player.prepare();
        long saved=prefs.getLong("position_"+index,0L);
        if(saved>0)player.seekTo(saved);
        player.setPlaybackParameters(new PlaybackParameters(speed));
        player.play();
    }

    private void saveProgress(){
        if(player==null)return;
        long pos=player.getCurrentPosition();
        if(pos>=0&&player.getPlaybackState()!=Player.STATE_ENDED)
            prefs.edit().putLong("position_"+index,pos).apply();
    }
    private void change(int offset){
        int next=index+offset;
        if(next<0||next>=LessonCatalog.size())return;
        saveProgress();index=next;loadLesson();
    }
    private void refreshButtons(){
        boolean fav=prefs.getBoolean("fav_"+index,false);
        boolean done=prefs.getBoolean("done_"+index,false);
        favoriteButton.setText(fav?"★ في المفضلة":"☆ مفضلة");
        doneButton.setText(done?"☑ مكتملة":"☐ مكتملة");
    }
    private void toggleFavorite(){
        prefs.edit().putBoolean("fav_"+index,!prefs.getBoolean("fav_"+index,false)).apply();
        refreshButtons();
    }
    private void toggleDone(){
        prefs.edit().putBoolean("done_"+index,!prefs.getBoolean("done_"+index,false)).apply();
        refreshButtons();
    }
    private void cycleSpeed(){
        float[] options={1f,1.25f,1.5f,2f,.75f};
        int current=0;for(int i=0;i<options.length;i++)if(Math.abs(speed-options[i])<0.001)current=i;
        speed=options[(current+1)%options.length];
        if(player!=null)player.setPlaybackParameters(new PlaybackParameters(speed));
        speedButton.setText("السرعة "+speed+"×");
    }
    private void copyLink(){
        String url=LessonCatalog.at(index).streamUrl();
        android.content.ClipboardManager mgr=(android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
        if(mgr!=null)mgr.setPrimaryClip(android.content.ClipData.newPlainText("HLS",url));
        Toast.makeText(this,"تم نسخ الرابط",Toast.LENGTH_SHORT).show();
    }
    private void openServerPlayer(){
        // ?player=1 is a browser player page, not guaranteed to be a valid HLS manifest.
        try{
            Intent i=new Intent(Intent.ACTION_VIEW,Uri.parse(LessonCatalog.at(index).serverPlayerUrl()));
            i.addCategory(Intent.CATEGORY_BROWSABLE);startActivity(i);
        }catch(ActivityNotFoundException e){
            Toast.makeText(this,"لا يوجد تطبيق لفتح رابط المشغّل",Toast.LENGTH_LONG).show();
        }
    }
    private void toggleLandscape(){
        expanded=!expanded;
        setRequestedOrientation(expanded?ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE:ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        updateOrientation();
    }
    private void updateOrientation(){
        if(root==null||playerView==null)return;
        boolean landscape=getResources().getConfiguration().orientation==Configuration.ORIENTATION_LANDSCAPE;
        header.setVisibility(landscape?View.GONE:View.VISIBLE);
        footer.setVisibility(landscape?View.GONE:View.VISIBLE);
        status.setVisibility(landscape?View.GONE:View.VISIBLE);
        rotateButton.setText(landscape?"تصغير الشاشة":"ملء الشاشة");
        LinearLayout.LayoutParams params=(LinearLayout.LayoutParams)playerView.getLayoutParams();
        params.height=landscape?0:Ui.dp(this,240);
        params.weight=landscape?1:0;
        playerView.setLayoutParams(params);
    }
    private void enterPip(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            try{
                setPictureInPictureParams(new PictureInPictureParams.Builder()
                        .setAspectRatio(new Rational(16,9)).build());
                enterPictureInPictureMode();
            }catch(Exception e){
                Toast.makeText(this,"وضع النافذة العائمة غير مدعوم على هذا الجهاز",Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override public void onPictureInPictureModeChanged(boolean inPip, Configuration cfg){
        super.onPictureInPictureModeChanged(inPip,cfg);
        header.setVisibility(inPip?View.GONE:View.VISIBLE);
        footer.setVisibility(inPip?View.GONE:View.VISIBLE);
        status.setVisibility(inPip?View.GONE:View.VISIBLE);
    }
    @Override public void onConfigurationChanged(Configuration config){super.onConfigurationChanged(config);updateOrientation();}
    @Override protected void onPause(){saveProgress();super.onPause();}
    @Override protected void onDestroy(){
        if(player!=null){saveProgress();playerView.setPlayer(null);player.release();player=null;}
        super.onDestroy();
    }
}
