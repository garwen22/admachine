package com.xmlan.machine.module.xixun.controller;

import com.google.gson.Gson;
import com.xmlan.machine.common.base.BaseBean;
import com.xmlan.machine.module.xixun.util.StartActivityData;
import okhttp3.*;

import java.io.IOException;

/**
 * 启动xwalk
 * @program: admachine
 * @description: controller
 * @author: YSS
 * @create: 2018-07-26 17:52
 **/
public class StartActivity {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private OkHttpClient client = new OkHttpClient();

    public String post(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    public static void main(String[] args) {
        String led = args[0];
        Gson gson = new Gson();
        StartActivityData data = new StartActivityData();
        String jsonData = gson.toJson(data);
        StartActivity test = new StartActivity();
        String url = BaseBean.URL+led; //check this
        String result;
        try {
            result = test.post(url, jsonData);
            System.out.print(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
