package com.example.arkscreen.Utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.example.arkscreen.R;
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

//    public static View getFinalView(List<OpeData> opeData,Context context){
//        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        ConstraintLayout layout = (ConstraintLayout) inflater.inflate(R.layout.list_reference,null);
//       // View view = layout.findViewById(R.layout.list_reference);
//        for(OpeData opeList: opeData){
//            ConstraintLayout itemView = (ConstraintLayout) inflater.inflate(R.layout.list_each,null);
//            LinearLayout listTags = itemView.findViewById(R.id.linear_tags);
//            LinearLayout listOpe= itemView.findViewById(R.id.linear_ope);
//            for(int i = 0;i<opeList.tags.size();i++){
//                String tag = opeList.tags.get(i);
//                TextView tagText = new TextView(context);
//                tagText.setPadding(10,10,10,10);
//                tagText.setBackgroundColor(ContextCompat.getColor(context,R.color.blue));
//                tagText.setTextColor(ContextCompat.getColor(context,R.color.white));
//                tagText.setText(tag);
//                listTags.addView(tagText);
//            }
//
//            for(int i = 0;i<opeList.operatorList.size();i++){
//                Operator operator = opeList.operatorList.get(i);
//                View opeView = inflater.inflate(R.layout.operator,null);
//                TextView opeName = opeView.findViewById(R.id.text_operator_name);
//                TextView opeClass = opeView.findViewById(R.id.text_operator_class);
//                TextView opeStar = opeView.findViewById(R.id.text_operator_star);
//                switch(operator.opeStar){
//                    case "6":opeName.setTextColor(ContextCompat.getColor(context,R.color.star_6));break;
//                    case "5":opeName.setTextColor(ContextCompat.getColor(context,R.color.star_5));break;
//                    case "4":opeName.setTextColor(ContextCompat.getColor(context,R.color.star_4));break;
//                    case "1":opeName.setTextColor(ContextCompat.getColor(context,R.color.star_1));break;
//                }
//                opeName.setText(operator.opeName);
//                opeClass.setText(operator.opeClass);
//                opeStar.setText(operator.opeStar+"★");
//                opeView.getMeasuredWidth();
//                listOpe.addView(opeView);
//            }
//            itemView.addView(listTags);
//            itemView.addView(listOpe);
//            layout.addView(itemView);
//        }
//        return layout;
//    }

}
