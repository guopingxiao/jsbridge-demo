package com.xiaoguoping.jsbrige_demo;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;


import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private MainActivity self = this;
    private WebView webview;
    private Button refreshBtn;
    private Button showBtn;
    private Button showBtn2;
    private TextView inputText;
    private NativeSDK nativeSDK = new NativeSDK(this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webview= findViewById(R.id.webView);
        refreshBtn = findViewById(R.id.refreshBtn);
        showBtn = findViewById(R.id.showBtn);
        showBtn2 = findViewById(R.id.showBtn2);
        inputText = findViewById(R.id.inputText);


        webview.loadUrl("http://192.168.100.153:8080/?timestamp" + new Date().getTime());

        // 打开webview执行js代码的权限
        webview.getSettings().setJavaScriptEnabled(true);

        // 1. 拦截url方式
        /* webview.setWebChromeClient(new WebChromeClient(){
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                if(!message.startsWith("jsbridge://")){
                    return super.onJsAlert(view, url, message, result);
                }
                String text = message.substring(message.indexOf("=") + 1);
                self.showNativeDialog(text);

                result.confirm();
                return true;
            }
        });*/

        // 2. 注入jsApi的方式
        webview.setWebChromeClient(new WebChromeClient());
        webview.addJavascriptInterface(new NativeBridge(this), "NativeBridge");

        // 刷新页面
        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webview.loadUrl("http://192.168.100.153:8080/?timestamp" + new Date().getTime());
            }
        });

        // 显示web弹窗
        showBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputValue = inputText.getText().toString().trim();
                self.showWebDialog(inputValue);

            }
        });

        // 获取web输入框的值
        showBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nativeSDK.getWebEditTextValue(new CallBack() {
                    @Override
                    public void invoke(String value) {
                        new AlertDialog.Builder(self)
                                .setMessage("Web 输入值:" + value)
                                .create()
                                .show();
                    }
                });

            }
        });

    }
    private void showWebDialog (String text){
        String jsCode = String.format("window.showWebDialog('%s')", text);
        webview.evaluateJavascript(jsCode, null);
    }

    /**
     * native展示弹窗
     * @param text
     */
    private void showNativeDialog(String text){
        new AlertDialog.Builder(this)
                .setMessage(text)
                .create()
                .show();
    }

    // Native暴露给JS的桥
    class NativeBridge {
        private Context ctx;
        NativeBridge (Context ctx){
            this.ctx = ctx;
        }

        @JavascriptInterface
        public void showNativeDialog(String text) {
            new AlertDialog.Builder(ctx)
                    .setMessage(text)
                    .create()
                    .show();
        }

        @JavascriptInterface
        public void getNativeEditTextValue(int callbackId){
            final MainActivity mainActivity = (MainActivity) ctx;
            String value = mainActivity.inputText.getText().toString();
            final String jsCode = String.format("window.JSSDK.receiveMessage(%s, '%s')", callbackId, value);
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mainActivity.webview.evaluateJavascript(jsCode, null);
                }
            });

        }

        @JavascriptInterface
        public void recieveMessage(int callbackId, String value){
            ((MainActivity) ctx).nativeSDK.recieveMessage(callbackId, value);
        }
    }


    // callback回调的接口
    interface CallBack{
        void invoke(String value);
    }

    // NativeSDK
    class NativeSDK {
        private Context ctx;
        private int callbackId = 0;
        private Map<Integer, CallBack> callBackMap = new HashMap();
        NativeSDK(Context ctx){
            this.ctx = ctx;
        }

        /**
         * 调用js 代码获取web数据
         * @param callback
         */
        void getWebEditTextValue(CallBack callback){
            callBackMap.put(callbackId, callback);
            final String jsCode = String.format("window.JSSDK.getWebEditValue(%s)", callbackId);
            ((MainActivity)ctx).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((MainActivity) ctx).webview.evaluateJavascript(jsCode, null);
                }
            });
            callbackId++;
        }

        /**
         * 接收js回调的处理
         * @param callbackId
         * @param value
         */
        void recieveMessage(int callbackId, String value){
            if(callBackMap.containsKey(callbackId)){
                callBackMap.get(callbackId).invoke(value);
            }
        }
    }

}
