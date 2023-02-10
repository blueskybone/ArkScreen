package com.example.arkscreen.database;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ArkdbDao {

    /*
    * 已阅，鉴定为逆天sql
    * 写出这种sql的应该上绞架
    * */

    @Query("SELECT * FROM star_6_op WHERE class LIKE :cls||'%' AND pos LIKE :opePos||'%' " +
            "AND ((tag1 LIKE :opeTag1||'%' AND tag2 LIKE :opeTag2||'%' AND tag3 LIKE :opeTag3||'%')" +
            "OR(tag1 LIKE :opeTag1||'%' AND tag2 LIKE :opeTag3||'%' AND tag3 LIKE :opeTag2||'%')" +
            "OR(tag1 LIKE :opeTag2||'%' AND tag2 LIKE :opeTag1||'%' AND tag3 LIKE :opeTag3||'%')" +
            "OR(tag1 LIKE :opeTag2||'%' AND tag2 LIKE :opeTag3||'%' AND tag3 LIKE :opeTag1||'%')" +
            "OR(tag1 LIKE :opeTag3||'%' AND tag2 LIKE :opeTag2||'%' AND tag3 LIKE :opeTag1||'%')" +
            "OR(tag1 LIKE :opeTag3||'%' AND tag2 LIKE :opeTag1||'%' AND tag3 LIKE :opeTag2||'%'))")
    List<Operator6>getStar6List(String cls,String opePos,
                                String opeTag1, String opeTag2, String opeTag3);

    @Query("SELECT * FROM star_5_op WHERE class LIKE :cls||'%' AND pos LIKE :opePos||'%' " +
            "AND ((tag1 LIKE :opeTag1||'%' AND tag2 LIKE :opeTag2||'%' AND tag3 LIKE :opeTag3||'%')" +
            "OR(tag1 LIKE :opeTag1||'%' AND tag2 LIKE :opeTag3||'%' AND tag3 LIKE :opeTag2||'%')" +
            "OR(tag1 LIKE :opeTag2||'%' AND tag2 LIKE :opeTag1||'%' AND tag3 LIKE :opeTag3||'%')" +
            "OR(tag1 LIKE :opeTag2||'%' AND tag2 LIKE :opeTag3||'%' AND tag3 LIKE :opeTag1||'%')" +
            "OR(tag1 LIKE :opeTag3||'%' AND tag2 LIKE :opeTag2||'%' AND tag3 LIKE :opeTag1||'%')" +
            "OR(tag1 LIKE :opeTag3||'%' AND tag2 LIKE :opeTag1||'%' AND tag3 LIKE :opeTag2||'%'))")
    List<Operator5>getStar5List(String cls,String opePos,
                                String opeTag1, String opeTag2, String opeTag3);

    @Query("SELECT * FROM star_4_op WHERE class LIKE :cls||'%' AND pos LIKE :opePos||'%' " +
            "AND ((tag1 LIKE :opeTag1||'%' AND tag2 LIKE :opeTag2||'%' AND tag3 LIKE :opeTag3||'%')" +
            "OR(tag1 LIKE :opeTag1||'%' AND tag2 LIKE :opeTag3||'%' AND tag3 LIKE :opeTag2||'%')" +
            "OR(tag1 LIKE :opeTag2||'%' AND tag2 LIKE :opeTag1||'%' AND tag3 LIKE :opeTag3||'%')" +
            "OR(tag1 LIKE :opeTag2||'%' AND tag2 LIKE :opeTag3||'%' AND tag3 LIKE :opeTag1||'%')" +
            "OR(tag1 LIKE :opeTag3||'%' AND tag2 LIKE :opeTag2||'%' AND tag3 LIKE :opeTag1||'%')" +
            "OR(tag1 LIKE :opeTag3||'%' AND tag2 LIKE :opeTag1||'%' AND tag3 LIKE :opeTag2||'%'))")
    List<Operator4>getStar4List(String cls,String opePos,
                                String opeTag1, String opeTag2, String opeTag3);
    @Query("SELECT * FROM star_3n2_op WHERE class LIKE :cls||'%' AND pos LIKE :opePos||'%' " +
            "AND ((tag1 LIKE :opeTag1||'%' AND tag2 LIKE :opeTag2||'%' AND tag3 LIKE :opeTag3||'%')" +
            "OR(tag1 LIKE :opeTag1||'%' AND tag2 LIKE :opeTag3||'%' AND tag3 LIKE :opeTag2||'%')" +
            "OR(tag1 LIKE :opeTag2||'%' AND tag2 LIKE :opeTag1||'%' AND tag3 LIKE :opeTag3||'%')" +
            "OR(tag1 LIKE :opeTag2||'%' AND tag2 LIKE :opeTag3||'%' AND tag3 LIKE :opeTag1||'%')" +
            "OR(tag1 LIKE :opeTag3||'%' AND tag2 LIKE :opeTag2||'%' AND tag3 LIKE :opeTag1||'%')" +
            "OR(tag1 LIKE :opeTag3||'%' AND tag2 LIKE :opeTag1||'%' AND tag3 LIKE :opeTag2||'%'))")
    List<Operator3n2>getStar3n2List(String cls,String opePos,
                                    String opeTag1, String opeTag2, String opeTag3);
    @Query("SELECT * FROM star_1_op WHERE class LIKE :cls||'%' AND pos LIKE :opePos||'%' " +
            "AND ((tag1 LIKE :opeTag1||'%' AND tag2 LIKE :opeTag2||'%' AND tag3 LIKE :opeTag3||'%')" +
            "OR(tag1 LIKE :opeTag1||'%' AND tag2 LIKE :opeTag3||'%' AND tag3 LIKE :opeTag2||'%')" +
            "OR(tag1 LIKE :opeTag2||'%' AND tag2 LIKE :opeTag1||'%' AND tag3 LIKE :opeTag3||'%')" +
            "OR(tag1 LIKE :opeTag2||'%' AND tag2 LIKE :opeTag3||'%' AND tag3 LIKE :opeTag1||'%')" +
            "OR(tag1 LIKE :opeTag3||'%' AND tag2 LIKE :opeTag2||'%' AND tag3 LIKE :opeTag1||'%')" +
            "OR(tag1 LIKE :opeTag3||'%' AND tag2 LIKE :opeTag1||'%' AND tag3 LIKE :opeTag2||'%'))")
        List<Operator1>getStar1List(String cls,String opePos,
                                String opeTag1, String opeTag2, String opeTag3);
    @Query("SELECT ch_key FROM en_to_ch WHERE en_key = :enKey")
    String getChKey(String enKey);

}
