package com.xiaoguoping.jsbrige_demo;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.widget.Button;
import android.widget.TextView;

import java.util.Date;

import wendu.dsbridge.CompletionHandler;
import wendu.dsbridge.DWebView;
import wendu.dsbridge.OnReturnValue;

public class MainActivity extends AppCompatActivity {

    private MainActivity self = this;
    private DWebView webview;
    private Button refreshBtn;
    private Button showBtn;
    private TextView inputText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webview= findViewById(R.id.webView);
        refreshBtn = findViewById(R.id.refreshBtn);
        showBtn = findViewById(R.id.showBtn);
        inputText = findViewById(R.id.inputText);


        webview.loadUrl("http://192.168.100.153:8080/?timestamp" + new Date().getTime());

        // 打开webview执行js代码的权限
        webview.getSettings().setJavaScriptEnabled(true);


        // DSBridge 注入jsApi
        webview.setWebChromeClient(new WebChromeClient());
        webview.addJavascriptObject(new JSApi(this), null);

        // 刷新页面
        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webview.loadUrl("http://192.168.100.153:8080/?timestamp" + new Date().getTime());
            }
        });

        showBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webview.callHandler("getWebEditValue", null, new OnReturnValue<String>() {

                    @Override
                    public void onValue(String retValue) {
                         new AlertDialog.Builder(self)
                                 .setMessage("web输入值：" + retValue)
                                 .create()
                                 .show();
                    }
                });
            }
        });
    }

    /**
     * 统一的JSApi管理
     */
    class JSApi {
        private Context ctx;
        public JSApi(Context ctx){
            this.ctx = ctx;
        }

        /**
         * 暴露给js调用的api,
         * @param params 参数
         * @param handler 回调方法
         */
        @JavascriptInterface
        public void getNativeEditValue(Object params, CompletionHandler<String> handler){
            String value = ((MainActivity)ctx).inputText.getText().toString();
            handler.complete(value);
        }
    }

}
