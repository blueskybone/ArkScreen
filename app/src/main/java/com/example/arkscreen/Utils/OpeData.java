package com.example.arkscreen.Utils;


import com.example.arkscreen.database.Operator;

import java.util.ArrayList;
import java.util.List;

public class OpeData implements Comparable<OpeData>{

     public List<String> tags = new ArrayList<>();

     public List<Operator> operatorList = new ArrayList<>();
     @Override
     public int compareTo(OpeData o) {
          if(this.operatorList.size()> o.operatorList.size()){
               return 1;
          }
          else{
               return -1;
          }
     }
}
