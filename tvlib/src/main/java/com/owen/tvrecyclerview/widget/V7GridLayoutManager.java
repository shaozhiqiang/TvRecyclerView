package com.owen.tvrecyclerview.widget;

import android.content.Context;
import android.graphics.Rect;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by owen on 2017/7/4.
 */

public class V7GridLayoutManager extends GridLayoutManager {
    private int mExtraLayoutSpace = 500;

    public V7GridLayoutManager(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, 0);
    }

    public V7GridLayoutManager(Context context, int spanCount) {
        super(context, spanCount);
    }

    public V7GridLayoutManager(Context context, int spanCount, int orientation, boolean reverseLayout) {
        super(context, spanCount, orientation, reverseLayout);
    }

    @Override
    public boolean requestChildRectangleOnScreen(RecyclerView parent, View child, Rect rect, boolean immediate, boolean focusedChildVisible) {
        if(parent instanceof TvRecyclerView) {
            return parent.requestChildRectangleOnScreen(child, rect, immediate);
        }
        return super.requestChildRectangleOnScreen(parent, child, rect, immediate, focusedChildVisible);
    }

    @Override
    protected int getExtraLayoutSpace(RecyclerView.State state) {
        return mExtraLayoutSpace;
    }

    public void setExtraLayoutSpace(int extraLayoutSpace) {
        mExtraLayoutSpace = extraLayoutSpace;
    }
}
