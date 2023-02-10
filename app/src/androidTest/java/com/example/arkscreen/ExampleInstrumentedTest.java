package com.example.arkscreen;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleInstrumentedTest {
//    @Test
//    public void useAppContext() {
//        // Context of the app under test.
//        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
//        assertEquals("com.example.arkscreen", appContext.getPackageName());
//    }

    @Test
    public void Test1() {
        List<Integer> intList = Arrays.asList(1, 2, 3, 4, 5);
    }



    private void getAllCom(int size, int range, int[] data) {
        for(int i = range; i > 0; i--){
            getCom(size, i,data);
        }
    }
    private void getCom(int size, int range, int[] data) {
        for (int Str = (1 << size) - 1; Str >= 0; Str--) {
            int cnt = 0;
            int[] array = new int[10];
            List<Integer> intList = new ArrayList<Integer>();
            for (int i = 0; i < size; i++){
                if ((Str & (1 << i))!=0){
                    array[cnt++] = i;
                    if(cnt == range){
                        for(int k = range-1;k>=0;k--){
                            intList.add(data[size - 1 - array[i]]);
                        }
                    }
                }
            }
            if(cnt == range) {
                System.out.println(intList);
                //comList.add(strList);
                break;
            }
        }
    }
    private List<List<String>> getAllCombination(int size, int range, String[] data) {
        List<List<String>> comListAll = new ArrayList<List<String>>();
        for(int i = range; i > 0; i--){
            comListAll.addAll(getCombination(size, i ,data));
        }
        return comListAll;
    }

    private List<List<String>> getCombination(int size, int range, String[] data) {
        List<List<String>> comList = new ArrayList<List<String>>();
        for (int Str = (1 << size) - 1; Str >= 0; Str--) {

            int cnt = 0;
            int[] array = new int[10];
            List<String> strList = new ArrayList<>();
            for (int i = 0; i < size; i++){
                if ((Str & (1 << i))!=0){
                    array[cnt++] = i;
                    if(cnt == range){
                        for(int k = range-1;k>=0;k--){
                            strList.add(data[size - 1 - array[i]]);
                        }
                    }
                }
            }
            if(cnt == range) {
                comList.add(strList);
                break;
            }
        }
        return comList;
    }
}