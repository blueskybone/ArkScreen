package com.example.arkscreen.Utils;

import android.content.Context;

import com.example.arkscreen.database.Operator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class Utils {

    public static String getAssetsCacheFile(Context context, String fileName)   {
        String filePath = context.getExternalCacheDir() +"/"+ fileName;
        try{
            File file = new File(filePath);
            if(file.exists()) {
                return filePath;
            }
            else{
                File cacheFile = new File(context.getExternalCacheDir(), fileName);
                try {
                    try (InputStream inputStream = context.getAssets().open(fileName)) {
                        try (FileOutputStream outputStream = new FileOutputStream(cacheFile)) {
                            byte[] buf = new byte[4096];
                            int len;
                            while ((len = inputStream.read(buf)) > 0) {
                                outputStream.write(buf, 0, len);
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return cacheFile.getAbsolutePath();
            }
        }
        catch (Exception e) {
            return filePath;
        }
    }

    public static String getMarkDownText(List<OpeData> opeData){
        StringBuilder text = new StringBuilder();
        for(OpeData operator:opeData){
            text.append("` ");
            for(String tag:operator.tags){
                text.append(tag).append(" ");
            }
            text.append("`  \n");
            for(Operator ope:operator.operatorList){
                text.append(ope.opeName).append("\t\t")
                        .append(ope.opeStar).append("★").append("\t\t")
                        .append(ope.opeClass).append("  \n");
            }
            text.append("\n\n");
        }
        return text.toString();
    }
}
