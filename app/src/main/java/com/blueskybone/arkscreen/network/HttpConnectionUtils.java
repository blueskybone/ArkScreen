package com.blueskybone.arkscreen.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.hjq.toast.Toaster;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class HttpConnectionUtils {

    private final static String TAG = "HttpConnectionUtils";
//    private static HttpsURLConnection httpsConn;

    public enum RequestMethod {
        GET,
        POST
    }

    static class Response {
        int responseCode;
        String responseContent;

        public Response(int respCode, String content) {
            this.responseCode = respCode;
            this.responseContent = content;
        }
    }

    public static HttpsURLConnection httpResponseConnection(URL url, @Nullable Map<String, String> header, RequestMethod method) {
        try {
            Log.e(TAG, "connection");
            HttpsURLConnection httpsConn = (HttpsURLConnection) url.openConnection();
            httpsConn.setRequestMethod(method.toString());
            httpsConn.setConnectTimeout(10000);
            if (header != null) {
                header.forEach(httpsConn::setRequestProperty);
            }
            httpsConn.setDoOutput(false);
            if (method == RequestMethod.POST) {
                httpsConn.setDoOutput(true);
            }
            httpsConn.setDoInput(true);
            httpsConn.setInstanceFollowRedirects(true);
            httpsConn.connect();
            if (httpsConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                return httpsConn;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Response httpResponse(URL url, @Nullable String jsonInput, @Nullable Map<String, String> header, RequestMethod method) throws Exception {
        StringBuilder result = new StringBuilder();
        try {
            HttpsURLConnection httpsConn = (HttpsURLConnection) url.openConnection();
            httpsConn.setConnectTimeout(5000);
            httpsConn.setRequestMethod(method.toString());
            if (header != null) {
                header.forEach(httpsConn::setRequestProperty);
            }
            httpsConn.setDoInput(true);
            httpsConn.setDoOutput(false);
            if (method == RequestMethod.POST) {
                httpsConn.setDoOutput(true);
            }
            if (jsonInput != null) {
                DataOutputStream dataOs = new DataOutputStream(
                        httpsConn.getOutputStream());
                dataOs.writeBytes(jsonInput);
                dataOs.flush();
                dataOs.close();
            }
            httpsConn.connect();
            int respCode = httpsConn.getResponseCode();
            InputStreamReader is;
            if (respCode == HttpURLConnection.HTTP_OK) {
                is = new InputStreamReader(httpsConn.getInputStream());
            } else {
                is = new InputStreamReader(httpsConn.getErrorStream());
            }
            BufferedReader buffer = new BufferedReader(is);
            String inputLine;
            while ((inputLine = buffer.readLine()) != null) {
                result.append(inputLine);
            }
            is.close();
            httpsConn.disconnect();
            return new Response(respCode, result.toString());
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }

    }

    public static void writeToLocal(String destination, InputStream input)
            throws IOException {
        int index;
        byte[] bytes = new byte[1024];
        FileOutputStream downloadFile = new FileOutputStream(destination);
        while ((index = input.read(bytes)) != -1) {
            downloadFile.write(bytes, 0, index);
            downloadFile.flush();
        }
        input.close();
        downloadFile.close();
    }

    private static InputStream getResponseStream(URL url) {
        try {
            HttpsURLConnection httpsConn = (HttpsURLConnection) url.openConnection();
            httpsConn.setConnectTimeout(5000);
            httpsConn.setRequestMethod("GET");
            httpsConn.setDoInput(true);
            httpsConn.setDoOutput(false);
            httpsConn.setInstanceFollowRedirects(true);
            httpsConn.connect();
            int responseCode = httpsConn.getResponseCode();
            if (responseCode == 200) {
                InputStream is = httpsConn.getInputStream();
                httpsConn.disconnect();
                return is;
            } else {
                httpsConn.disconnect();
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void downloadToLocal(String localPath, URL url) throws IOException {
        InputStream is = getResponseStream(url);
        if (is == null) {
            return;
        }
        writeToLocal(localPath, is);
    }
}
