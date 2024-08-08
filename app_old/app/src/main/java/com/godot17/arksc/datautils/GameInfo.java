package com.godot17.arksc.datautils;

/**
 * jackson
 * 理智：AP: (currentTs - lastApAddTime) /360 + current / max , recover - currentTs/60/60
 * 无人机：value/maxValue
 * 训练室：
 * 公招：
 * 公招刷新：
 * 剿灭：
 * 日常：
 * 周常：
 * 保全：
 * ---
 * 订单
 * 制造进度：
 * 休息进度：
 * 线索收集：
 * 无人机：
 * 干员疲劳：
 */
public class GameInfo {
    public static Info info = new Info();
    public static Ap ap = new Ap();
    public static Train train = new Train();
    public static Recruit recruit = new Recruit();
    public static Hire hire = new Hire();
    public static Campaign campaign = new Campaign();
    public static RoutineDay routineDay = new RoutineDay();
    public static RoutineWeek routineWeek = new RoutineWeek();
    public static Tower tower = new Tower();
    public static Trading trading = new Trading();
    public static Dormitories dormitories = new Dormitories();
    public static Manufactures manufactures = new Manufactures();
    public static Meeting meeting = new Meeting();
    public static Tired tired = new Tired();
    public static Labor labor = new Labor();

    public static class Info{
        public String nickName;
        public int level;
        public String progress;
    }

    public static class Ap {
        public int current;
        public int max;
        public int recoverTime;      //s
    }

    public static class Train {
        public boolean isNull;
        public String trainee;
        public boolean traineeIsNull;
        public int status;
        public int time;            //s
    }

    public static class Recruit {
        public boolean isNull;
        public int max;
        public int value;
        public int time;             //s
    }

    public static class Hire {
        public boolean isNull;
        public int value;
        public int time;             //s
    }

    public static class Campaign {
        public boolean isNull;
        public int current;
        public int total;
    }

    public static class RoutineDay {
        public boolean isNull;
        public int current;
        public int total;
    }

    public static class RoutineWeek {
        public boolean isNull;
        public int current;
        public int total;
    }

    public static class Tower {
        public boolean isNull;
        public int highCurrent;
        public int highTotal;
        public int lowCurrent;
        public int lowTotal;
    }

    //database
    public static class Trading {
        public boolean isNull;
        public int value;
        public int maxValue;
    }

    public static class Labor {
        public boolean isNull;
        public int value;
        public int maxValue;
        public int recoverTime;
    }

    public static class Dormitories {
        public boolean isNull;
        public int value;
        public int maxValue;
    }

    public static class Meeting {
        public boolean isNull;
        public int value;
        public int maxValue = 7;
        public String status;
    }

    public static class Manufactures {
        public boolean isNull;
        public int value;
        public int maxValue;
    }

    public static class Tired {
        public int value;
    }
}