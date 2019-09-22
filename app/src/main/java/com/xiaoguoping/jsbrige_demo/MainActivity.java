package com.xiaoguoping.jsbrige_demo;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

import wendu.dsbridge.CompletionHandler;
import wendu.dsbridge.DWebView;

public class MainActivity extends AppCompatActivity {

    private MainActivity self = this;
    private DWebView webview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webview= findViewById(R.id.webView);

        webview.loadUrl("http://192.168.100.153:8080/?timestamp" + new Date().getTime());

        // 打开webview执行js代码的权限
        webview.getSettings().setJavaScriptEnabled(true);
        // DSBridge 注入jsApi
        webview.setWebChromeClient(new WebChromeClient());
        webview.addJavascriptObject(new JSApi(this), null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Native 的color格式是ARGB  web的是RGBA
        changeTheme(0xFFFF6633);
        return true;
    }

    // 换肤功能
    private void changeTheme(int color){
        // 状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(color);

        // 标题栏
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(color));

        // 导航栏
        getWindow().setNavigationBarColor(color);

        // web网页背景
        webview.callHandler("changeTheme", new Object[]{color});
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
        public void nativeHttp(Object params, CompletionHandler<String> handler){
            try {
                String url = ((JSONObject)params).getString("url");
                String data = request(url);
                handler.complete(data);
            } catch (JSONException e) {
                handler.complete(e.getMessage());
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private String request(String urlSpec) throws Exception{
            HttpURLConnection connection = (HttpURLConnection )new URL(urlSpec).openConnection();
            connection.setRequestMethod("GET");
            InputStream inputStream = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuffer buffer = new StringBuffer();
            String line;
            while ((line = reader.readLine()) != null){
                buffer.append(line);
            }

            return buffer.toString();
        }
    }

}
