package com.owen.tvrecyclerview.example;

import android.content.Context;
import android.graphics.Rect;
import android.support.design.widget.TabLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by owen on 16/8/30.
 */
public class TvTabLayout extends TabLayout{
    private static final String LOGTAG = TvTabLayout.class.getSimpleName();
    
    private ViewGroup mTabStrip;
    private int mActivatedPosition;

    public TvTabLayout(Context context) {
        this(context, null);
    }

    public TvTabLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TvTabLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
        setWillNotDraw(true);
        setClickable(false);
        setFocusable(true);
        setFocusableInTouchMode(true);

        mTabStrip = (ViewGroup) getChildAt(0);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean result = super.dispatchKeyEvent(event);
        if(!result) {
            switch (event.getAction()) {
                case KeyEvent.ACTION_DOWN:
                    result = onKeyDown(event.getKeyCode(), event);
                    break;
            }
        }
        return result;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.i(LOGTAG, "onKeyDown...keyCode="+keyCode);
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
                return setTabActivated(mActivatedPosition - 1);
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                return setTabActivated(mActivatedPosition + 1);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        Log.i(LOGTAG, "onFocusChanged...gainFocus="+gainFocus);
        
        setTabActivated(gainFocus ? getPosition() : mActivatedPosition, gainFocus);
        
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
    }
    
    private boolean setTabActivated(int index) {
        if(index >= 0 && index < getTabCount()) {
            setTabActivated(mActivatedPosition, false);
            setTabActivated(index, true);
            return true;
            
        }
        return false;
    }
    
    private boolean setTabActivated(int position, boolean activated){
        if(position >= 0 && position < getTabCount()) {
            View tabView = mTabStrip.getChildAt(position);
            if(null != tabView) {
                tabView.setActivated(activated);
                tabView.performClick();
                if (activated) {
                    mActivatedPosition = position;
                }
                return true;
            }
        }
        return false;
    }
    
    private int getPosition() {
        return getSelectedTabPosition() != -1 ? getSelectedTabPosition() : 0;
    }
}
