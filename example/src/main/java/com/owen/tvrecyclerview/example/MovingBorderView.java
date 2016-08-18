package com.owen.tvrecyclerview.example;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by owen on 16/8/18.
 */
public class MovingBorderView extends View {
    public MovingBorderView(Context context) {
        super(context);
    }

    public MovingBorderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MovingBorderView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MovingBorderView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
    
    
}
