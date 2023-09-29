package com.godot17.arksc.utils;

import android.app.Dialog;
import android.content.Context;

import androidx.annotation.NonNull;

//加载画面
//想加个动画，不会画，摆了（
public class LoadingDialog extends Dialog {

    public LoadingDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }
}
