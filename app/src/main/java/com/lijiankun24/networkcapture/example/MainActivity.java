package com.lijiankun24.networkcapture.example;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.lijiankun24.networkcapture.library.NetworkProxyManager;
import com.lijiankun24.networkcapture.library.har.HarEntry;
import com.lijiankun24.networkcapture.library.har.HarLog;
import com.lijiankun24.networkcapture.library.har.HarTimings;
import com.lijiankun24.networkcapture.library.util.L;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private OkHttpClient mHttpClient = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mHttpClient = new OkHttpClient();
        initProxy();
        initView();

    }

    private void initProxy() {
        NetworkProxyManager.getInstance().startProxy("127.0.0.1", 6666);
    }

    private void initView() {
        findViewById(R.id.tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                okhttp();
            }
        });
        findViewById(R.id.tv_print_log).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                printLog();
            }
        });
    }

    private void printLog() {
        HarLog harLog = NetworkProxyManager.getInstance().getHarLog();
        for (HarEntry entry : harLog.getEntries()) {
            HarTimings timings = entry.getTimings();
            L.i("getDns time is     " + timings.getDns());
            L.i("getConnect time is " + timings.getConnect());
            L.i("getSend time is    " + timings.getSend());
            L.i("getReceive time is " + timings.getReceive());
        }
    }

    private void okhttp() {
        Request request = new Request.Builder()
//                .url("https://www.baidu.com")
                .url("http://music.qq.com/musicbox/shop/v3/data/hit/hit_all.js")
                .build();
        mHttpClient.newCall(request)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.i("lijk", "onFailure ");
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        Log.i("lijk", "onResponse ");
                    }
                });
    }
}
