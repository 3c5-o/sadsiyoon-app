package com.sadsiyoon.app;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceError;
import android.webkit.WebResourceResponse;
import android.widget.Toast;
import androidx.core.graphics.Insets;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.ViewCompat;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public final class SourcePaperActivity extends Activity {
    private WebView browser;
    private String url;
    private boolean allowed(Uri u){
        if(u==null || !"https".equalsIgnoreCase(u.getScheme()))return false;
        String host=u.getHost();
        return host!=null&&("mlazemna.com".equalsIgnoreCase(host)||host.toLowerCase(java.util.Locale.ROOT).endsWith(".mlazemna.com"));
    }
    private void outside(Uri u){
        if(u==null || !"https".equalsIgnoreCase(u.getScheme()))return;
        try{startActivity(new Intent(Intent.ACTION_VIEW,u));}catch(Exception ignored){}
    }
    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        url=getIntent().getStringExtra("source_url");
        if(url==null||!allowed(Uri.parse(url))){finish();return;}
        String title=getIntent().getStringExtra("source_title");
        LinearLayout root=Ui.col(this);
        root.setBackgroundColor(Ui.BG);
        WindowCompat.setDecorFitsSystemWindows(getWindow(),false);
        ViewCompat.setOnApplyWindowInsetsListener(root,(view,insets)->{
            Insets bars=insets.getInsets(WindowInsetsCompat.Type.systemBars());
            root.setPadding(bars.left+Ui.dp(this,10),bars.top+Ui.dp(this,8),
                bars.right+Ui.dp(this,10),bars.bottom+Ui.dp(this,10));
            return insets;
        });
        LinearLayout toolbar=Ui.row(this);
        TextView back=Ui.btn(this,"رجوع",false);
        toolbar.addView(back,Ui.lp(this,68,44));
        back.setOnClickListener(v->finish());
        TextView name=Ui.txt(this,title==null?"ورقة وزارية":title,15,Ui.WHITE,true);
        name.setMaxLines(2);
        name.setGravity(Gravity.CENTER_VERTICAL);
        toolbar.addView(name,new LinearLayout.LayoutParams(0,Ui.dp(this,48),1f));
        root.addView(toolbar);
        TextView origin=Ui.txt(this,"المصدر الأصلي: ملازمنا • جميع العلامات والصور كما نشرها المصدر",12,Ui.GOLD,false);
        root.addView(origin,Ui.top(this,-1,-2,6));
        LinearLayout actions=Ui.row(this);
        TextView reload=Ui.btn(this,"تحديث",false);
        TextView open=Ui.btn(this,"فتح بالمتصفح",true);
        actions.addView(reload,new LinearLayout.LayoutParams(0,Ui.dp(this,42),1f));
        actions.addView(open,new LinearLayout.LayoutParams(0,Ui.dp(this,42),1f));
        root.addView(actions,Ui.top(this,-1,-2,8));
        open.setOnClickListener(v->outside(Uri.parse(url)));
        ProgressBar progress=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);
        progress.setIndeterminate(true);
        root.addView(progress,Ui.top(this,-1,4,5));
        browser=new WebView(this);
        browser.setBackgroundColor(Color.WHITE);
        WebSettings settings=browser.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        browser.setWebViewClient(new WebViewClient(){
            @Override public void onReceivedError(WebView view,WebResourceRequest req,WebResourceError err){
                if(req.isForMainFrame()){
                    progress.setVisibility(View.GONE);
                    Toast.makeText(SourcePaperActivity.this,"تعذّر فتح المصدر؛ جرّب فتحه بالمتصفح",Toast.LENGTH_LONG).show();
                }
            }
            @Override public void onReceivedHttpError(WebView view,WebResourceRequest req,WebResourceResponse response){
                if(req.isForMainFrame()&&response.getStatusCode()>=400){
                    progress.setVisibility(View.GONE);
                    Toast.makeText(SourcePaperActivity.this,"الموقع يعرض خطأ "+response.getStatusCode()+"؛ استخدم المتصفح",Toast.LENGTH_LONG).show();
                }
            }
            @Override public boolean shouldOverrideUrlLoading(WebView view,WebResourceRequest request){
                if(request.isForMainFrame()&&!allowed(request.getUrl())){
                    outside(request.getUrl());return true;
                }
                return false;
            }
            @Override public void onPageFinished(WebView view,String url){
                progress.setVisibility(View.GONE);
            }
        });
        reload.setOnClickListener(v->{progress.setVisibility(View.VISIBLE);browser.reload();});
        root.addView(browser,new LinearLayout.LayoutParams(-1,0,1f));
        setContentView(root);
        ViewCompat.requestApplyInsets(root);
        browser.loadUrl(url);
    }
    @Override public void onBackPressed(){
        if(browser!=null&&browser.canGoBack())browser.goBack();
        else super.onBackPressed();
    }
    @Override protected void onDestroy(){
        if(browser!=null){browser.stopLoading();browser.destroy();browser=null;}
        super.onDestroy();
    }
}
