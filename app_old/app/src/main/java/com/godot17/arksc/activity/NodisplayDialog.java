package com.godot17.arksc.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;

import com.godot17.arksc.App;
import com.godot17.arksc.service.ScreenTaskService;

public class NodisplayDialog extends Dialog {
    Context context;

    public NodisplayDialog(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    public void startScreenTask() {
        //Android14开始，不能通过保存ScreenshotPermission的方法反复截屏，
        //只能每次截屏获取一次动态权限。
        if (Build.VERSION.SDK_INT < 34 && App.getScreenshotPermission() != null) {
//            Intent intent = new Intent(context, ScreenTaskService.class);
//            context.startForegroundService(intent);
            App.startScreenshotRecruit(App.getInstance());
        } else {
            Intent acquireIntent = new Intent(context, AcquireScreenshotPermission.class);
            acquireIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(acquireIntent);
        }
    }

    //just for float tile service
    public void prepareScreenTask() {
        if (App.getScreenshotPermission() == null) {
            Intent acquireIntent = new Intent(context, AcquireScreenshotPermission.class);
            acquireIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(acquireIntent);
        }
    }
}
