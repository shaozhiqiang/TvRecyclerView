package com.owen.tvrecyclerview.example;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by owen on 16/8/30.
 */
public class IRecyclerView extends RecyclerView {
    private OnItemListener mOnItemListener;
    private ItemListener mItemListener;

    private boolean mHasFocus = false;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 110:
                    mHasFocus = true;
                    onFocusChanged(mHasFocus, View.FOCUS_DOWN, null);
                    break;

                case 111:
                    if(getFocusedChild() == null) {
                        mHasFocus = false;
                        onFocusChanged(mHasFocus, View.FOCUS_DOWN, null);
                    }
                    break;
            }
        }
    };
    
    public IRecyclerView(Context context) {
        super(context);
        init(context);
    }

    public IRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public IRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context){

        mItemListener = new ItemListener() {
            /**
             * 子控件的点击事件
             * @param itemView
             */
            @Override
            public void onClick(View itemView) {
                if(null != mOnItemListener) {
                    mOnItemListener.onItemClick(IRecyclerView.this, itemView, getChildLayoutPosition(itemView));
                }
            }

            /**
             * 子控件的焦点变动事件
             * @param itemView
             * @param hasFocus
             */
            @Override
            public void onFocusChange(View itemView, boolean hasFocus) {
                if(hasFocus && !mHasFocus){
                    mHandler.sendEmptyMessage(110);
                }
                else if(!hasFocus && mHasFocus) {
                    mHandler.removeMessages(111);
                    mHandler.sendEmptyMessageDelayed(111, 100);
                }

                if(null != mOnItemListener) {
                    if(null != itemView) {
                        itemView.setSelected(hasFocus);
                        if (hasFocus) {
                            mOnItemListener.onItemSelected(IRecyclerView.this, itemView, getChildLayoutPosition(itemView));
                        } else {
                            mOnItemListener.onItemPreSelected(IRecyclerView.this, itemView, getChildLayoutPosition(itemView));
                        }
                    }
                }
            }
        };
    }

    @Override
    public void onChildAttachedToWindow(View child) {
        if(!ViewCompat.hasOnClickListeners(child)) {
            child.setOnClickListener(mItemListener);
        }
        if(null == child.getOnFocusChangeListener()) {
            child.setOnFocusChangeListener(mItemListener);
        }
    }

    @Override
    public boolean hasFocus() {
        Log.i("IRecyclerView", "hasFocus...");
        return super.hasFocus();
    }

    @Override
    public boolean isInEditMode() {
        return true;
    }

    public void setOnItemListener(OnItemListener onItemListener) {
        mOnItemListener = onItemListener;
    }

    public interface OnItemListener {
        void onItemPreSelected(IRecyclerView parent, View itemView, int position);

        void onItemSelected(IRecyclerView parent, View itemView, int position);

        void onReviseFocusFollow(IRecyclerView parent, View itemView, int position);

        void onItemClick(IRecyclerView parent, View itemView, int position);
    }

    private interface ItemListener extends View.OnClickListener, View.OnFocusChangeListener {

    }
}
