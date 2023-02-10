package com.example.arkscreen.Utils;

import android.content.Context;
import android.util.Log;

import com.example.arkscreen.database.ArkdbDatabase;
import com.example.arkscreen.database.Operator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/*
* 谁写的nt代码
* */

public class DataSearchUtils {
    private static final int range = 3;
    private static List<OpeData> opeDataList = new ArrayList<>();
    private static List<OpeData> opeFinalListStar6 = new ArrayList<>();
    private static List<OpeData> opeFinalListStar5 = new ArrayList<>();
    private static List<OpeData> opeFinalListStar4 = new ArrayList<>();
    private static List<OpeData> opeFinalListStar1 = new ArrayList<>();
    public static List<OpeData> getFinalList(String[] allTag, Context mContext){

        opeDataList.clear();
        opeFinalListStar6.clear();
        opeFinalListStar5.clear();
        opeFinalListStar4.clear();
        opeFinalListStar1.clear();

        Arrays.sort(allTag, Collections.reverseOrder());
        List<List<String>> dataListAll = getAllCombination(allTag.length,range,allTag);;
        for(List<String> dataCom : dataListAll ){
            String flagStar = "";
            String flagPos = "";
            String flagCls = "";
            boolean valid =true;
            String[] tags = {"","",""};
            int cnt = 0;
            for( String tag: dataCom){
                String[] head = tag.split("_");
                if(head[0].equals("75")){
                    valid = false;
                    break;
                }
                if(head[0].equals("90")||head[0].equals("85")||head[0].equals("80")) {
                    if (!flagStar.equals("")) {
                        valid = false;
                        break;
                    }
                    flagStar = head[1];
                   continue;
                }
                if(head[0].equals("60")) {
                    if(!flagPos.equals("")){
                        valid = false;
                        break;
                    }
                    flagPos = head[1];
                    continue;
                }
                if(head[0].equals("50")){
                    if(!flagCls.equals("")){
                        valid = false;
                        break;
                    }
                    flagCls = head[1];
                    continue;
                }
                tags[cnt++] = head[1];
            }
            if(valid){
                OpeData opeData = new OpeData();
                opeData.tags.addAll(splitTag(dataCom));

                switch (flagStar) {
                    case "高级资深干员":
                        opeData.operatorList.addAll(QueryForStar6(flagCls, flagPos, tags[0], tags[1], tags[2], mContext));
                        if(opeData.operatorList.size()>0) {
                            opeFinalListStar6.add(opeData);
                        }
                        break;
                    case "资深干员":
                        opeData.operatorList.addAll(QueryForStar5(flagCls, flagPos, tags[0], tags[1], tags[2], mContext));
                        if(opeData.operatorList.size()>0){
                            opeFinalListStar5.add(opeData);
                        }
                        break;
                    case "支援机械":
                        opeData.operatorList.addAll(QueryForStar1(flagCls, flagPos, tags[0], tags[1], tags[2], mContext));
                        if(opeData.operatorList.size()>0){
                            opeFinalListStar1.add(opeData);
                        }
                        break;
                    case "": {
                        List<Operator> opeDataList3n2 = new ArrayList<>(QueryForStar3n2(flagCls, flagPos, tags[0], tags[1], tags[2], mContext));
                        if (opeDataList3n2.size() > 0) {
                            break;
                        }
                        List<Operator> opeDataList5 = new ArrayList<>(QueryForStar5(flagCls, flagPos, tags[0], tags[1], tags[2], mContext));
                        List<Operator> opeDataList4 = new ArrayList<>(QueryForStar4(flagCls, flagPos, tags[0], tags[1], tags[2], mContext));
                        List<Operator> opeDataList1 = new ArrayList<>(QueryForStar1(flagCls, flagPos, tags[0], tags[1], tags[2], mContext));
//                        Log.e("result_4", opeDataList4.size() + "");
//                        Log.e("result_5", opeDataList5.size() + "");
//                        Log.e("result_1", opeDataList1.size() + "");
                        if (opeDataList4.size() > 0) {
                            opeData.operatorList.addAll(opeDataList5);
                            opeData.operatorList.addAll(opeDataList4);
                            opeData.operatorList.addAll(opeDataList1);
                            opeFinalListStar4.add(opeData);
                        } else if(opeDataList5.size()>0){
                            opeData.operatorList.addAll(opeDataList5);
                            opeData.operatorList.addAll(opeDataList1);
                            opeFinalListStar5.add(opeData);
                        }
                        else if(opeDataList1.size()>0){
                            opeData.operatorList.addAll(opeDataList1);
                            opeFinalListStar1.add(opeData);
                        }
                        break;
                    }
                    default:break;
                }
            }

        }
        if(opeFinalListStar6.size()>0){
            Collections.sort(opeFinalListStar6);
            opeDataList.addAll(opeFinalListStar6);
        }
        if(opeFinalListStar5.size()>0){
            Collections.sort(opeFinalListStar5);
            opeDataList.addAll(opeFinalListStar5);
        }
        if(opeFinalListStar1.size()>0){
            Collections.sort(opeFinalListStar1);
            opeDataList.addAll(opeFinalListStar1);
        }
        if(opeFinalListStar4.size()>0){
            Collections.sort(opeFinalListStar4);
            opeDataList.addAll(opeFinalListStar4);
        }
//        Log.e("opeFinalStar6",opeFinalListStar6.size()+"");
//        Log.e("opeFinalStar5",opeFinalListStar5.size()+"");
//        Log.e("opeFinalStar1",opeFinalListStar1.size()+"");
//        Log.e("opeFinalStar4",opeFinalListStar4.size()+"");
//        Log.e("result_all",opeDataList.size()+ "");
        return opeDataList;
    }

    private static List<List<String>> getAllCombination(int size, int range, String[] data) {
        List<List<String>> comListAll = new ArrayList<List<String>>();
        for(int i = range; i > 0; i--){
            comListAll.addAll(getCombination(size, i ,data));
        }
        return comListAll;
    }

    private static List<List<String>> getCombination(int size, int range, String[] data) {
        List<List<String>> comList = new ArrayList<List<String>>();
        for (int Str = (1 << size) - 1; Str >= 0; Str--) {
            int cnt = 0;
            int[] array = new int[10];
            List<String> strList = new ArrayList<>();
            for (int i = 0; i < size; i++){
                if ((Str & (1 << i))!=0){
                    array[cnt++] = i;
                }
            }
            if(cnt == range) {
                for(int i = range - 1; i >= 0; i--) {
                    strList.add(data[size- 1 - array[i]]);
                }
                comList.add(strList);
            }
        }
        return comList;
    }

    private static List<Operator> QueryForStar6(String cls, String pos, String tag1, String tag2, String tag3, Context context){
        ArkdbDatabase arkdbDatabase = ArkdbDatabase.getDatabase(context);
        return new ArrayList<>(arkdbDatabase.ArkdbDao().getStar6List(cls, pos, tag1, tag2, tag3));
    }
    private static List<Operator> QueryForStar5(String cls, String pos, String tag1, String tag2, String tag3, Context context){
        ArkdbDatabase arkdbDatabase = ArkdbDatabase.getDatabase(context);
        return new ArrayList<>(arkdbDatabase.ArkdbDao().getStar5List(cls, pos, tag1, tag2, tag3));
    }
    private static List<Operator> QueryForStar4(String cls, String pos, String tag1, String tag2, String tag3, Context context){
        ArkdbDatabase arkdbDatabase = ArkdbDatabase.getDatabase(context);
        return new ArrayList<>(arkdbDatabase.ArkdbDao().getStar4List(cls, pos, tag1, tag2, tag3));
    }
    private static List<Operator> QueryForStar3n2(String cls, String pos, String tag1, String tag2, String tag3, Context context){
        ArkdbDatabase arkdbDatabase = ArkdbDatabase.getDatabase(context);
        return new ArrayList<>(arkdbDatabase.ArkdbDao().getStar3n2List(cls, pos, tag1, tag2, tag3));
    }
    private static List<Operator> QueryForStar1(String cls, String pos, String tag1, String tag2, String tag3, Context context){
        ArkdbDatabase arkdbDatabase = ArkdbDatabase.getDatabase(context);
        return new ArrayList<>(arkdbDatabase.ArkdbDao().getStar1List(cls, pos, tag1, tag2, tag3));
    }

    public static String[] tagsEnToCh(String[] enKeys, Context context){
        String[] outTags = new String[enKeys.length];
        for (int i = 0;i<enKeys.length;i++) {
            outTags[i] = QueryForTagEnToCh(enKeys[i], context);
        }
        return outTags;
    }
    public static String[] splitTag(String[] chKeys){
        String[] outTags = new String[chKeys.length];
        for (int i = 0;i<chKeys.length;i++) {
            String[] head = chKeys[i].split("_");
            outTags[i] = head[1];
        }
        return outTags;
    }
    public static List<String> splitTag(List<String> chKeys){
        List<String> outTags = new ArrayList<>();
        for(String chKey:chKeys){
            String[] head = chKey.split("_");
            outTags.add(head[1]);
        }
        return outTags;
    }
    private static String QueryForTagEnToCh(String enKey, Context context){
        ArkdbDatabase arkdbDatabase = ArkdbDatabase.getDatabase(context);
        return arkdbDatabase.ArkdbDao().getChKey(enKey);
    }
}