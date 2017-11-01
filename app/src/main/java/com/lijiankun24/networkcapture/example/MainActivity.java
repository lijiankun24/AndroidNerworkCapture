package com.lijiankun24.networkcapture.example;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.lijiankun24.networkcapture.library.filter.BrowserMobHttpFilterChain;
import com.lijiankun24.networkcapture.library.proxy.BrowserMobProxyServer;

import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;

import java.io.IOException;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
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
        initProxy();
        mHttpClient = new OkHttpClient();
        findViewById(R.id.tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                okhttp();
            }
        });
    }

    private void okhttp() {
        findViewById(R.id.tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Request request = new Request.Builder()
                        .url("http://www.baidu.com")
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
        });
    }

    private void initProxy() {
        System.setProperty("http.proxyHost", "127.0.0.1");
        System.setProperty("http.proxyPort", String.valueOf(6666));
        System.setProperty("https.proxyHost", "127.0.0.1");
        System.setProperty("https.proxyPort", String.valueOf(6666));

        DefaultHttpProxyServer.bootstrap()
                .withPort(6666)
                .withFiltersSource(new HttpFiltersSourceAdapter() {
                    @Override
                    public HttpFilters filterRequest(HttpRequest originalRequest) {
                        Log.i("lijk", "filterRequest 1 ");
                        return super.filterRequest(originalRequest);
                    }

                    @Override
                    public HttpFilters filterRequest(HttpRequest originalRequest, ChannelHandlerContext ctx) {
                        Log.i("lijk", "filterRequest 2 ");
                        return new BrowserMobHttpFilterChain(originalRequest, ctx, new BrowserMobProxyServer());
                    }

                    @Override
                    public int getMaximumRequestBufferSizeInBytes() {
                        Log.i("lijk", "getMaximumRequestBufferSizeInBytes 1 ");
                        return super.getMaximumRequestBufferSizeInBytes();
                    }

                    @Override
                    public int getMaximumResponseBufferSizeInBytes() {
                        Log.i("lijk", "getMaximumRequestBufferSizeInBytes 2 ");
                        return super.getMaximumResponseBufferSizeInBytes();
                    }
                })
                .start();
    }
}
