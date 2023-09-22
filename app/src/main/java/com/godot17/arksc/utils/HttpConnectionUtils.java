package com.godot17.arksc.utils;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * 没用OKhttp，因为没啥复杂需求。想控制安装包尽量在10M内。
 */
public class HttpConnectionUtils {
    private final static String TAG = "HttpConnectionUtils";

    public enum RequestMethod{
        GET,
        POST
    }

    public static InputStream getResponse(URL url) {
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
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static InputStream getResponseStream(URL url, Map<String, String> header) {
        try {
            Log.e(TAG, "connection");
            HttpsURLConnection httpsConn = (HttpsURLConnection) url.openConnection();
            httpsConn.setRequestMethod("GET");
            httpsConn.setConnectTimeout(10000);
            header.forEach(httpsConn::setRequestProperty);
            httpsConn.setDoOutput(false);
            httpsConn.setDoInput(true);
            httpsConn.setInstanceFollowRedirects(true);
            httpsConn.connect();
            if (httpsConn.getResponseCode() == 200) {
                //LEAK
                //httpsConn.disconnect();
                return httpsConn.getInputStream();
            } else {
                //httpsConn.disconnect();
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String postResponse(URL url, String jsonInput, Map<String, String> header) {
        StringBuilder result = new StringBuilder();
        try {
            Log.e(TAG, "connection");
            HttpsURLConnection httpsConn = (HttpsURLConnection) url.openConnection();
            httpsConn.setRequestMethod("POST");
            httpsConn.setConnectTimeout(5000);
            header.forEach(httpsConn::setRequestProperty);
            httpsConn.setDoOutput(true);
            httpsConn.setDoInput(true);
            OutputStream os = httpsConn.getOutputStream();
            byte[] input = jsonInput.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
            os.flush();
            os.close();
            Log.e(TAG, "before connection");
            int respCode = httpsConn.getResponseCode();
            if (respCode == HttpURLConnection.HTTP_OK) {
                Log.e(TAG, "connection == 200");
                InputStreamReader is = new InputStreamReader(httpsConn.getInputStream());
                BufferedReader buffer = new BufferedReader(is);
                String inputLine;
                while ((inputLine = buffer.readLine()) != null) {
                    result.append(inputLine);
                }
                is.close();
            } else if (respCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                httpsConn.disconnect();
                return "UNAUTHORIZED";
            } else if (respCode == HttpURLConnection.HTTP_FORBIDDEN) {
                Log.e(TAG, "connection == 403");
                InputStreamReader is = new InputStreamReader(httpsConn.getErrorStream());
                BufferedReader buffer = new BufferedReader(is);
                String inputLine;
                while ((inputLine = buffer.readLine()) != null) {
                    result.append(inputLine);
                }
                is.close();
            } else {
                Log.e(TAG, httpsConn.getResponseCode() + "");
                httpsConn.disconnect();
                return null;
            }
            httpsConn.disconnect();
        } catch (Exception e) {
            Log.e(TAG, "error with", e);
        }
        return result.toString();
    }


    public static String postResponseNew(URL url, String jsonInput, Map<String, String> header) {
        StringBuilder result = new StringBuilder();
        try {
            Log.e(TAG, "connection");
            HttpsURLConnection httpsConn = (HttpsURLConnection) url.openConnection();
            httpsConn.setRequestMethod("POST");
            httpsConn.setConnectTimeout(5000);
            header.forEach(httpsConn::setRequestProperty);
            httpsConn.setDoInput(true);
            httpsConn.setDoOutput(true);

            OutputStream os = httpsConn.getOutputStream();
            byte[] input = jsonInput.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
            os.flush();
            os.close();

            Log.e(TAG, "before connection");
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
            httpsConn.disconnect();
            is.close();
            return result.toString();
        } catch (Exception e) {
            Log.e(TAG, "error with", e);
        }
        return null;
    }

    public static String httpResponse(URL url, @Nullable String jsonInput, Map<String, String> header, RequestMethod method) {
        StringBuilder result = new StringBuilder();
        try {
            HttpsURLConnection httpsConn = (HttpsURLConnection) url.openConnection();
            httpsConn.setConnectTimeout(5000);
            httpsConn.setRequestMethod(method.toString());
            header.forEach(httpsConn::setRequestProperty);
            httpsConn.setDoInput(true);
            httpsConn.setDoOutput(false);
            if(method==RequestMethod.POST){
                httpsConn.setDoOutput(true);
            }
            if(jsonInput!=null){
                OutputStream os = httpsConn.getOutputStream();
                byte[] input = jsonInput.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
                os.flush();
                os.close();
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
            httpsConn.disconnect();
            is.close();
            return result.toString();
        }catch (Exception e){
            Log.e(TAG, "error with", e);
        }
        return null;
    }

    public static String getResponse(URL url, Map<String, String> header) {
        StringBuilder result = new StringBuilder();
        try {
            HttpsURLConnection httpsConn = (HttpsURLConnection) url.openConnection();
            httpsConn.setConnectTimeout(5000);
            httpsConn.setRequestMethod("GET");
            httpsConn.setDoInput(true);
            httpsConn.setDoOutput(false);
            header.forEach(httpsConn::setRequestProperty);
            httpsConn.connect();
            int respCode = httpsConn.getResponseCode();
            if (respCode == HttpURLConnection.HTTP_OK) {
                InputStreamReader is = new InputStreamReader(httpsConn.getInputStream());
                BufferedReader buffer = new BufferedReader(is);
                String inputLine;
                while ((inputLine = buffer.readLine()) != null) {
                    result.append(inputLine);
                    Log.e(TAG, inputLine);
                }
                is.close();
            } else if (respCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                httpsConn.disconnect();
                return "UNAUTHORIZED";
            } else {
                httpsConn.disconnect();
                return null;
            }
            httpsConn.disconnect();
        } catch (Exception e) {
            Log.e(TAG, "error with", e);
        }
        return result.toString();
    }

    /*TODO:添加对gzip流支持*/
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

    public static void downloadToLocal(String localPath, String link) throws IOException {
        URL url = new URL(link);
        InputStream is = getResponse(url);
        if (is == null) {
            return;
        }
        writeToLocal(localPath, is);
    }

    public static boolean isNetConnected(Context context) {
        if (context != null) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null) {
                return networkInfo.isAvailable();
            }
        }
        return false;
    }
}
