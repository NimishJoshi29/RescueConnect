package com.example.rescueconnect;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NotificationPusher implements Runnable {
    private String topic;
    private Response response;
    NotificationPusher(String t){
        topic=t;
    }
    @Override
    public void run() {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\n  \"validate_only\": false,\n  \"message\": {\n    \"notification\": {\n        \"title\" : \"FCM API Title\",\n        \"body\" : \"FCM API Body\"\n    },\n    \"topic\": \"Tapowan\"\n  }\n}");
        Request request = new Request.Builder()
                .url("https://fcm.googleapis.com/v1/projects/rescueconnect-565fb/messages:send")
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer ya29.a0Ad52N3-UpHuhtzAndF5iF_nf4E70nFwfF9mqWTP7z_FrxUcnPjIyvTDg_khP3gjAQdAWh9gHbMW1zhBnCz4Nsz7slRDfuLZAWhxD0uP36je6ZkX3elYGdqdU_RwkclY0_OAUxCrLFIT92MJGanS20nyYJI97GsafcoIaCgYKAdQSARMSFQHGX2MiCXX0hfqRHZiJFKnLVlMCzQ0170")
                .build();
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    boolean isSuccessful(){
        return response.isSuccessful();
    }
}
