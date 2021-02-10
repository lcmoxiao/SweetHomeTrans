package main.tools;

import okhttp3.*;

import java.io.IOException;

public class HttpRequestTools {

    static OkHttpClient client = new OkHttpClient();


    public static String get(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    public static String post(String url, String json) throws IOException {

        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");

        RequestBody stringBody = RequestBody.create(json, mediaType);

        Request request = new Request
                .Builder()
                .url(url)
                .post(stringBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        } catch (Exception ignored) {
            return null;
        }
    }

    public static String delete(String url, String json) throws IOException {

        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");

        RequestBody stringBody = RequestBody.create(json, mediaType);

        Request request = new Request
                .Builder()
                .url(url)
                .delete(stringBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        } catch (Exception ignored) {
            return null;
        }
    }

}
