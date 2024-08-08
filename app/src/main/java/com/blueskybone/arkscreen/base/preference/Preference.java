package com.blueskybone.arkscreen.base.preference;

/*
 * The code for data persistence refers to the following project
 * https://github.com/easybangumiorg/EasyBangumi
 * */

public interface Preference <T> {
    String key();
    T get();
    void set(T value);

    T defaultValue();
    Boolean isSet();

    void delete();
}
