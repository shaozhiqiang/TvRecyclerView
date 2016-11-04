/*
 * Copyright (C) 2014 Lucas Rocha
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.owen.tvrecyclerview.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.View;

import com.owen.tvrecyclerview.BaseLayoutManager;
import com.owen.tvrecyclerview.R;
import com.owen.tvrecyclerview.TwoWayLayoutManager;

import java.lang.reflect.Constructor;

public class TvRecyclerView extends RecyclerView {
    private static final String LOGTAG = TvRecyclerView.class.getSimpleName() + ":::";
    private static final int DEFAULT_SELECTED_ITEM_OFFSET = 40;
    private static final int DEFAULT_LOAD_MORE_BEFOREHAND_COUNT = 4;

    private int mVerticalSpacingWithMargins = 0;
    private int mHorizontalSpacingWithMargins = 0;
    
    private int mSelectedItemOffsetStart;
    private int mSelectedItemOffsetEnd;
    
    private boolean mSelectedItemCentered;
    private boolean mIsBaseLayoutManager;
    private boolean mIsInterceptKeyEvent;
    private boolean mIsSelectFirstVisiblePosition;
    private boolean mIsMenu;
    private boolean mHasFocus = false;
    private int mLoadMoreBeforehandCount;
    
    private int mOldSelectedPosition = 0;
    private int mSelectedPosition = 0;
    private int mOverscrollValue;
    private int mOffset = -1;

    private OnItemListener mOnItemListener;
    private OnInBorderKeyEventListener mOnInBorderKeyEventListener;
    private OnLoadMoreListener mOnLoadMoreListener;
    private boolean mHasMore = true;
    private boolean mLoadingMore = false;
    
    private ItemListener mItemListener;

    private static final Class<?>[] sConstructorSignature = new Class[] {
            Context.class, AttributeSet.class};

    private final Object[] sConstructorArgs = new Object[2];
    
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
    

    public TvRecyclerView(Context context) {
        this(context, null);
    }

    public TvRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TvRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        init(context);
        
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TvRecyclerView, defStyle, 0);

        final String name = a.getString(R.styleable.TvRecyclerView_tv_layoutManager);
        if (!TextUtils.isEmpty(name)) {
            loadLayoutManagerFromName(context, attrs, name);
        }
        mSelectedItemCentered = a.getBoolean(R.styleable.TvRecyclerView_tv_selectedItemIsCentered, false);
        mIsInterceptKeyEvent = a.getBoolean(R.styleable.TvRecyclerView_tv_isInterceptKeyEvent, false);
        mIsMenu = a.getBoolean(R.styleable.TvRecyclerView_tv_isMenu, false);
        mIsSelectFirstVisiblePosition = a.getBoolean(R.styleable.TvRecyclerView_tv_isSelectFirstVisiblePosition, false);
        mLoadMoreBeforehandCount = a.getInt(R.styleable.TvRecyclerView_tv_loadMoreBeforehandCount, DEFAULT_LOAD_MORE_BEFOREHAND_COUNT);
        mSelectedItemOffsetStart = a.getDimensionPixelOffset(R.styleable.TvRecyclerView_tv_selectedItemOffsetStart, DEFAULT_SELECTED_ITEM_OFFSET);
        mSelectedItemOffsetEnd = a.getDimensionPixelOffset(R.styleable.TvRecyclerView_tv_selectedItemOffsetEnd, DEFAULT_SELECTED_ITEM_OFFSET);
        
        a.recycle();
    }

    private void init(Context context){
        setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
        setChildrenDrawingOrderEnabled(true);
        setWillNotDraw(true); // 自身不作onDraw处理
        setHasFixedSize(true);
        setOverScrollMode(View.OVER_SCROLL_NEVER);

        setClipChildren(false);
        setClipToPadding(false);

        setClickable(false);
        setFocusable(true);
        setFocusableInTouchMode(true);
        
        mItemListener = new ItemListener() {
            /**
             * 子控件的点击事件
             * @param itemView
             */
            @Override
            public void onClick(View itemView) {
                if(null != mOnItemListener) {
                    mOnItemListener.onItemClick(TvRecyclerView.this, itemView, getChildLayoutPosition(itemView));
                }
            }

            /**
             * 子控件的焦点变动事件
             * @param itemView
             * @param hasFocus
             */
            @Override
            public void onFocusChange(View itemView, boolean hasFocus) {
                mHandler.removeMessages(110);
                mHandler.removeMessages(111);
                if(hasFocus && !mHasFocus){
                    mHandler.sendEmptyMessage(110);
                } 
                else if(!hasFocus && mHasFocus) {
                    mHandler.sendEmptyMessageDelayed(111, 50);
                }
                
                if(null != itemView) {
                    final int position = getChildLayoutPosition(itemView);
                    itemView.setSelected(hasFocus);
                    if (hasFocus) {
                        mSelectedPosition = position;
                        if(null != mOnItemListener)
                            mOnItemListener.onItemSelected(TvRecyclerView.this, itemView, position);
                    } else {
                        mOldSelectedPosition = position;
                        if(null != mOnItemListener)
                            mOnItemListener.onItemPreSelected(TvRecyclerView.this, itemView, position);
                    }
                }
            }
        };
    }
    
    private void loadLayoutManagerFromName(Context context, AttributeSet attrs, String name) {
        try {
            final int dotIndex = name.indexOf('.');
            if (dotIndex == -1) {
                name = "com.owen.tvrecyclerview.widget." + name;
            } else if (dotIndex == 0) {
                final String packageName = context.getPackageName();
                name = packageName + "." + name;
            }

            Class<? extends TwoWayLayoutManager> clazz =
                    context.getClassLoader().loadClass(name).asSubclass(TwoWayLayoutManager.class);

            Constructor<? extends TwoWayLayoutManager> constructor =
                    clazz.getConstructor(sConstructorSignature);

            sConstructorArgs[0] = context;
            sConstructorArgs[1] = attrs;

            setLayoutManager(constructor.newInstance(sConstructorArgs));
        } catch (Exception e) {
            throw new IllegalStateException("Could not load TwoWayLayoutManager from " +
                                             "class: " + name, e);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        Log.i(LOGTAG, "onLayout: ");
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);
        Log.i(LOGTAG, "onMeasure: ");
    }

    @Override
    public void setLayoutManager(LayoutManager layout) {
        mIsBaseLayoutManager = layout instanceof BaseLayoutManager;
        super.setLayoutManager(layout);
    }

    @Override
    public void setAdapter(final Adapter adapter) {
        super.setAdapter(adapter);
        
        if(null == adapter) return;
        adapter.registerAdapterDataObserver(new AdapterDataObserver() {
            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                if(mHasFocus) {
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            requestFocus();
                        }
                    }, 300);
                }
            }
        });
    }
    
    public void requestDefaultFocus() {
        if(mIsMenu || !mIsSelectFirstVisiblePosition) {
            setSelection(mOldSelectedPosition);
        } else {
            setSelection(getFirstVisiblePosition());
        }
    }
    
    public void setSelection(int position) {
        ViewHolder holder = findViewHolderForLayoutPosition(position);
        if(null == holder) {
            holder = findViewHolderForAdapterPosition(position - getFirstVisiblePosition());
        }
        if(null == holder && getChildCount() > 0) {
            holder = getChildViewHolder(getChildAt(0));
        }
        if(null != holder) {
            holder.itemView.requestFocusFromTouch();
            holder.itemView.requestFocus();
        }
    }

    public int getSelectedPosition() {
        return mSelectedPosition;
    }
    
    public int getOldSelectedPosition() {
        return mOldSelectedPosition;
    }

    public void setSelectFirstVisiblePosition(boolean selectFirstVisiblePosition) {
        mIsSelectFirstVisiblePosition = selectFirstVisiblePosition;
    }

    public boolean isSelectFirstVisiblePosition() {
        return mIsSelectFirstVisiblePosition;
    }

    public void setMenu(boolean menu) {
        mIsMenu = menu;
    }

    public boolean isMenu() {
        return mIsMenu;
    }

    public void setLoadMoreBeforehandCount(int loadMoreBeforehandCount) {
        mLoadMoreBeforehandCount = loadMoreBeforehandCount;
    }

    public int getLoadMoreBeforehandCount() {
        return mLoadMoreBeforehandCount;
    }

    /**
     * 设置选中的Item距离开始或结束的偏移量；
     * 与滚动方向有关；
     * 与setSelectedItemAtCentered()方法二选一
     * @param offsetStart
     * @param offsetEnd
     */
    public void setSelectedItemOffset(int offsetStart, int offsetEnd) {
        this.mSelectedItemOffsetStart = offsetStart;
        this.mSelectedItemOffsetEnd = offsetEnd;
    }

    /**
     * 设置选中的Item居中；
     * 与setSelectedItemOffset()方法二选一
     * @param isCentered
     */
    public void setSelectedItemAtCentered(boolean isCentered) {
        this.mSelectedItemCentered = isCentered;
    }

    public boolean isSelectedItemCentered() {
        return mSelectedItemCentered;
    }

    public void setLoadingMore(boolean loadingMore) {
        mLoadingMore = loadingMore;
    }

    public boolean isLoadingMore() {
        return mLoadingMore;
    }

    /**
     * 设置是否拦截OnKey事件
     * @param interceptKeyEvent
     */
    public void setInterceptKeyEvent(boolean interceptKeyEvent) {
        mIsInterceptKeyEvent = interceptKeyEvent;
    }

    public boolean isInterceptKeyEvent() {
        return mIsInterceptKeyEvent;
    }

    public boolean isVertical() {
        if(mIsBaseLayoutManager) {
            BaseLayoutManager layout = (BaseLayoutManager) getLayoutManager();
            return layout.isVertical();
        } else if (getLayoutManager() instanceof LinearLayoutManager) {
            LinearLayoutManager layout = (LinearLayoutManager) getLayoutManager();
            return layout.getOrientation() == LinearLayoutManager.VERTICAL;
        }
        return true;
    }
    
    private int getFreeSize() {
        if(!isVertical()) {
            return getFreeHeight();
        } else {
            return getFreeWidth();
        }
    }
    
    private int getFreeHeight() {
        return getHeight() - getPaddingTop() - getPaddingBottom();
    }
    
    private int getFreeWidth() {
        return getWidth() - getPaddingLeft() - getPaddingRight();
    }

    @Override
    public void requestChildFocus(View child, View focused) {
//        Log.i(LOGTAG, "requestChildFocus: "+child);
        
        if(null != child) {
            if (mSelectedItemCentered) {
                mSelectedItemOffsetStart = !isVertical() ? (getFreeWidth() - child.getWidth()) : (getFreeHeight() - child.getHeight());
                mSelectedItemOffsetStart /= 2;
                mSelectedItemOffsetEnd = mSelectedItemOffsetStart;
            }
        }
        super.requestChildFocus(child, focused);
    }

    @Override
    public void onScrolled(int dx, int dy) {
        if(isVertical()) {
            mOverscrollValue = dy;
        } else {
            mOverscrollValue = dx;
        }
        super.onScrolled(dx, dy);
    }

    @Override
    public void onScrollStateChanged(int state) {
        if(state == SCROLL_STATE_IDLE) {
            mOffset = -1;
            if (Math.abs(mOverscrollValue) != 1) {
                mOverscrollValue = 1;
                final View focuse = getFocusedChild();
                if (null != mOnItemListener && null != focuse) {
                    mOnItemListener.onReviseFocusFollow(this, focuse, getChildLayoutPosition(focuse));
                }
            }

            // 加载更多回调
            if(!mLoadingMore && mHasMore && null != mOnLoadMoreListener) {
                if(getLastVisiblePosition() >= getAdapter().getItemCount() - (1 + mLoadMoreBeforehandCount)) {
                    mHasMore = mOnLoadMoreListener.onLoadMore();
                }
            }
        }
        super.onScrollStateChanged(state);
    }

    @Override
    public boolean requestChildRectangleOnScreen(View child, Rect rect, boolean immediate) {
        final int parentLeft = getPaddingLeft();
        final int parentTop = getPaddingTop();
        final int parentRight = getWidth() - getPaddingRight();
        final int parentBottom = getHeight() - getPaddingBottom();
        final int childLeft = child.getLeft() + rect.left;
        final int childTop = child.getTop() + rect.top;
        final int childRight = childLeft + rect.width();
        final int childBottom = childTop + rect.height();

        final int offScreenLeft = Math.min(0, childLeft - parentLeft - mSelectedItemOffsetStart);
        final int offScreenTop = Math.min(0, childTop - parentTop - mSelectedItemOffsetStart);
        final int offScreenRight = Math.max(0, childRight - parentRight + mSelectedItemOffsetEnd);
        final int offScreenBottom = Math.max(0, childBottom - parentBottom + mSelectedItemOffsetEnd);

        final boolean canScrollHorizontal = getLayoutManager().canScrollHorizontally();
        final boolean canScrollVertical = getLayoutManager().canScrollVertically();

        // Favor the "start" layout direction over the end when bringing one side or the other
        // of a large rect into view. If we decide to bring in end because start is already
        // visible, limit the scroll such that start won't go out of bounds.
        final int dx;
        if(canScrollHorizontal) {
            if (ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL) {
                dx = offScreenRight != 0 ? offScreenRight
                        : Math.max(offScreenLeft, childRight - parentRight);
            } else {
                dx = offScreenLeft != 0 ? offScreenLeft
                        : Math.min(childLeft - parentLeft, offScreenRight);
            }
        } else {
            dx = 0;
        }

        // Favor bringing the top into view over the bottom. If top is already visible and
        // we should scroll to make bottom visible, make sure top does not go out of bounds.
        final int dy;
        if(canScrollVertical) {
            dy = offScreenTop != 0 ? offScreenTop : Math.min(childTop - parentTop, offScreenBottom);
        } else {
            dy = 0;
        }
        
        if(cannotScrollForwardOrBackward(isVertical() ? dy : dx)) {
            mOffset = -1;
        } else {
            mOffset = isVertical() ? dy : dx;
            
            if (dx != 0 || dy != 0) {
                if (immediate) {
                    scrollBy(dx, dy);
                } else {
                    smoothScrollBy(dx, dy);
                }
                return true;
            }

        }

        // 重绘是为了选中item置顶，具体请参考getChildDrawingOrder方法
        postInvalidate();
        
        return false;
    }

    /**
     * 获取选中ITEM的滚动偏移量
     * @return
     */
    public int getSelectedItemScrollOffset() {
        return mOffset;
    }

    /**
     * 判断当前是否还可以向前或后滚动
     * @param value
     * @return
     */
    private boolean cannotScrollForwardOrBackward(int value) {
        if(mIsBaseLayoutManager) {
            final BaseLayoutManager layoutManager = (BaseLayoutManager) getLayoutManager();
            return (layoutManager.cannotScrollBackward(value)
                    || layoutManager.cannotScrollForward(value));
               
        }
        return false;
    }
    
    /**
     * 通过Margins来设置布局的横纵间距；
     * (与addItemDecoration()方法可二选一)
     * @param verticalSpacing
     * @param horizontalSpacing
     */
    public void setSpacingWithMargins(int verticalSpacing, int horizontalSpacing) {
        this.mVerticalSpacingWithMargins = verticalSpacing;
        this.mHorizontalSpacingWithMargins = horizontalSpacing;
        if(mIsBaseLayoutManager) {
            BaseLayoutManager layout = (BaseLayoutManager) getLayoutManager();
            layout.setSpacingWithMargins(verticalSpacing, horizontalSpacing);
        }
        adjustPadding();
    }

    /**
     * 根据Margins调整Padding值
     */
    private void adjustPadding() {
        if((mVerticalSpacingWithMargins > 0 || mHorizontalSpacingWithMargins > 0)) {
            final int verticalSpacingHalf = mVerticalSpacingWithMargins / 2;
            final int horizontalSpacingHalf = mHorizontalSpacingWithMargins / 2;
            final int l = getPaddingLeft() - verticalSpacingHalf;
            final int t = getPaddingTop() - horizontalSpacingHalf;
            final int r = getPaddingRight() - verticalSpacingHalf;
            final int b = getPaddingBottom() - horizontalSpacingHalf;
            setPadding(l, t, r, b);
        }
    }
    
    public TwoWayLayoutManager.Orientation getOrientation() {
        if(mIsBaseLayoutManager) {
            final BaseLayoutManager layout = (BaseLayoutManager) getLayoutManager();
            return layout.getOrientation();
        } 
        else if(getLayoutManager() instanceof LinearLayoutManager) {
            final LinearLayoutManager layout = (LinearLayoutManager) getLayoutManager();
            return layout.getOrientation() == LinearLayoutManager.HORIZONTAL 
                    ? BaseLayoutManager.Orientation.HORIZONTAL 
                    : BaseLayoutManager.Orientation.VERTICAL;
        }
        else {
            return BaseLayoutManager.Orientation.VERTICAL;
        }
    }

    public void setOrientation(TwoWayLayoutManager.Orientation orientation) {
        if(mIsBaseLayoutManager) {
            final BaseLayoutManager layout = (BaseLayoutManager) getLayoutManager();
            layout.setOrientation(orientation);
        } 
        else if(getLayoutManager() instanceof LinearLayoutManager) {
            final LinearLayoutManager layout = (LinearLayoutManager) getLayoutManager();
            layout.setOrientation(orientation == BaseLayoutManager.Orientation.HORIZONTAL 
                    ? LinearLayoutManager.HORIZONTAL : LinearLayoutManager.VERTICAL);
        }
    }

    public int getFirstVisiblePosition() {
        if(getChildCount() == 0)
            return 0;
        else
            return getChildLayoutPosition(getChildAt(0));
    }

    public int getLastVisiblePosition() {
        final int childCount = getChildCount();
        if(childCount == 0)
            return 0;
        else
            return getChildLayoutPosition(getChildAt(childCount - 1));
    }
    
    public void scrollToPositionWithOffset(int position) {
        if(mIsBaseLayoutManager) {
            BaseLayoutManager layout = (BaseLayoutManager) getLayoutManager();
            layout.scrollToPositionWithOffset(position, mSelectedItemOffsetStart);
            return;
        }
        scrollToPosition(position);
    }

    int mTempPosition = 0;
    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        View view = getFocusedChild();
        if(null != view) {
            mTempPosition = getChildLayoutPosition(view) - getFirstVisiblePosition();
            if (mTempPosition < 0) {
                return i;
            } else {
                if (i == childCount - 1) {//这是最后一个需要刷新的item
                    if (mTempPosition > i) {
                        mTempPosition = i;
                    }
                    return mTempPosition;
                }
                if (i == mTempPosition) {//这是原本要在最后一个刷新的item
                    return childCount - 1;
                }
            }
        }
        return i;
    }

    public boolean isScrolling() {
        return getScrollState() != SCROLL_STATE_IDLE;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean result = super.dispatchKeyEvent(event);
        if(!result) {
            switch (event.getAction()) {
                case KeyEvent.ACTION_DOWN:
                    result = onKeyDown(event.getKeyCode(), event);
                    break;
                case KeyEvent.ACTION_UP:
                    result = onKeyUp(event.getKeyCode(), event);
                    break;
            }
        }
        return result;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean reuslt = super.onKeyDown(keyCode, event);
        // 拦截key事件
        if(mIsInterceptKeyEvent && !reuslt) {
            reuslt = handleOnKey(keyCode, event);
        }
        return reuslt;
    }

    /**
     * 处理onKeyDown等事件
     * @param keyCode
     * @param event
     * @return
     */
    private boolean handleOnKey(int keyCode, KeyEvent event) {
        int direction = keyCode2Direction(keyCode);

        if(direction == -1) {
            return false;
        } 
        else if(hasInBorder(direction)) {
            return null != mOnInBorderKeyEventListener && mOnInBorderKeyEventListener.onInBorderKeyEvent(direction, keyCode, event);
        }
        else {
            View newFocusedView = findNextFocus(direction);
            if (null != newFocusedView) {
                newFocusedView.requestFocus();
            }
        }
        return true;
    }

    /**
     * 查找下个可获取焦点的view
     * @param direction
     * @return
     */
    private View findNextFocus(int direction) {
        return FocusFinder.getInstance().findNextFocus(this, getFocusedChild(), direction);
    }

    /**
     * keycode值转成Direction值
     * @param keyCode
     * @return
     */
    private int keyCode2Direction(int keyCode) {
        switch (keyCode){
            case KeyEvent.KEYCODE_DPAD_DOWN:
                return FOCUS_DOWN;
            
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                return FOCUS_RIGHT;
            
            case KeyEvent.KEYCODE_DPAD_LEFT:
                return FOCUS_LEFT;
            
            case KeyEvent.KEYCODE_DPAD_UP:
                return FOCUS_UP;
            
            default:
                return -1;
        }
    }

    @Override
    public View focusSearch(View focused, int direction) {
        if(hasInBorder(direction)) {
            return super.focusSearch(focused, direction);
        } else {
            return findNextFocus(direction);
        }
    }

    /**
     * 判断选中的item是否到达边界
     * @param direction
     * @return
     */
    private boolean hasInBorder(int direction) {
        boolean result = false;
        final View view = getFocusedChild();
        if(null != view) {
            Rect outRect = new Rect();
            getLayoutManager().calculateItemDecorationsForChild(view, outRect);
            LayoutParams lp = (LayoutParams) view.getLayoutParams();
            switch (direction) {
                case FOCUS_DOWN:
                    result = getHeight() - view.getBottom() <= getPaddingBottom() + lp.bottomMargin + outRect.bottom;
                    if(isVertical()) {
                        result = result && getLastVisiblePosition() == (getAdapter().getItemCount() - 1);
                    }
                    break;
                case FOCUS_UP:
                    result = view.getTop() <= getPaddingTop() + lp.topMargin + outRect.top;
                    if(isVertical()) {
                        result = result && getFirstVisiblePosition() == 0;
                    }
                    break;
                case FOCUS_LEFT:
                    result = view.getLeft() <= getPaddingLeft() + lp.leftMargin + outRect.left;
                    if(!isVertical()) {
                        result = result && getFirstVisiblePosition() == 0;
                    }
                    break;
                case FOCUS_RIGHT:
                    result = getWidth() - view.getRight() <= getPaddingRight() + lp.rightMargin + outRect.right;
                    if(!isVertical()) {
                        result = result && getLastVisiblePosition() == (getAdapter().getItemCount() - 1);
                    }
                    break;
            }
        }
        return result;
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
    public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
        setDescendantFocusability(FOCUS_BEFORE_DESCENDANTS);
        requestDefaultFocus();
        
        int descendantFocusability = getDescendantFocusability();
        switch (descendantFocusability) {
            case FOCUS_BLOCK_DESCENDANTS:
                return true;
            case FOCUS_BEFORE_DESCENDANTS: {
                final boolean took = true;
                return took ? took : onRequestFocusInDescendants(direction, previouslyFocusedRect);
            }
            case FOCUS_AFTER_DESCENDANTS: {
                final boolean took = onRequestFocusInDescendants(direction, previouslyFocusedRect);
                return took ? took : super.requestFocus(direction, previouslyFocusedRect);
            }
            default:
                throw new IllegalStateException("descendant focusability must be "
                        + "one of FOCUS_BEFORE_DESCENDANTS, FOCUS_AFTER_DESCENDANTS, FOCUS_BLOCK_DESCENDANTS "
                        + "but is " + descendantFocusability);
        }
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
//        Log.i(LOGTAG, "onFocusChanged..." + gainFocus + " ,direction="+direction + " ,mOldSelectedPosition="+mOldSelectedPosition);
        mHasFocus = gainFocus;
        if(mIsMenu) {
            // 解决选中后无状态表达的问题，selector中使用activated代表选中后焦点移走
            ViewHolder holder = findViewHolderForLayoutPosition(mOldSelectedPosition);
            if(null != holder) {
                holder.itemView.setActivated(!gainFocus);
            }
        }
        
        if(gainFocus) {
            setDescendantFocusability(FOCUS_BEFORE_DESCENDANTS);
        } else {
            setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
        }
        
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
    }
    
    public void setItemActivated(int position) {
        if(mIsMenu) {
            ViewHolder holder = findViewHolderForLayoutPosition(position);
            if(null != holder) {
                holder.itemView.setActivated(true);
                mOldSelectedPosition = position;
            }
        }
    }
    
    @Override
    public boolean hasFocus() {
//        Log.i(LOGTAG, "hasFocus...");
        return super.hasFocus();
    }

    @Override
    public boolean isInTouchMode() {
//        Log.i(LOGTAG, "isInTouchMode...");
        boolean result = super.isInTouchMode();
        // 解决4.4版本抢焦点的问题
        if (Build.VERSION.SDK_INT == 19) {
            return !(hasFocus() && !result);
        } else {
            return result;
        }
    }

    @Override
    public boolean isInEditMode() {
        return true;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        SavedState savedState = new SavedState(super.onSaveInstanceState());
        savedState.mSelectedPosition = mSelectedPosition;
        savedState.mVerticalSpacingWithMargins = mVerticalSpacingWithMargins;
        savedState.mHorizontalSpacingWithMargins = mHorizontalSpacingWithMargins;
        savedState.mSelectedItemOffsetStart = mSelectedItemOffsetStart;
        savedState.mSelectedItemOffsetEnd = mSelectedItemOffsetEnd;
        savedState.mSelectedItemCentered = mSelectedItemCentered;
        savedState.mIsBaseLayoutManager = mIsBaseLayoutManager;
        savedState.mIsInterceptKeyEvent = mIsInterceptKeyEvent;
        savedState.mIsMenu = mIsMenu;
        savedState.mHasMore = mHasMore;
        savedState.mIsSelectFirstVisiblePosition = mIsSelectFirstVisiblePosition;
        return savedState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {

        if(null != state) {
            if(state instanceof SavedState) {
                SavedState savedState = (SavedState) state;
                mOldSelectedPosition = savedState.mSelectedPosition;
                mSelectedPosition = savedState.mSelectedPosition;
                mVerticalSpacingWithMargins = savedState.mVerticalSpacingWithMargins;
                mHorizontalSpacingWithMargins = savedState.mHorizontalSpacingWithMargins;
                mSelectedItemOffsetStart = savedState.mSelectedItemOffsetStart;
                mSelectedItemOffsetEnd = savedState.mSelectedItemOffsetEnd;
                mSelectedItemCentered = savedState.mSelectedItemCentered;
                mIsBaseLayoutManager = savedState.mIsBaseLayoutManager;
                mIsInterceptKeyEvent = savedState.mIsInterceptKeyEvent;
                mIsMenu = savedState.mIsMenu;
                mHasMore = savedState.mHasMore;
                mIsSelectFirstVisiblePosition = savedState.mIsSelectFirstVisiblePosition;
                
                super.onRestoreInstanceState(savedState.getSuperState());
            } else {
                super.onRestoreInstanceState(state);
            }
        }
    }

    protected static class SavedState implements Parcelable {
        protected static final SavedState EMPTY_STATE = new SavedState();

        private final Parcelable superState;
        private int mSelectedPosition;
        private int mVerticalSpacingWithMargins;
        private int mHorizontalSpacingWithMargins;
        private int mSelectedItemOffsetStart;
        private int mSelectedItemOffsetEnd;
        private boolean mSelectedItemCentered;
        private boolean mIsBaseLayoutManager;
        private boolean mIsInterceptKeyEvent;
        private boolean mIsMenu;
        private boolean mHasMore;
        private boolean mIsSelectFirstVisiblePosition;

        private SavedState() {
            superState = null;
        }

        protected SavedState(Parcelable superState) {
            if (superState == null) {
                throw new IllegalArgumentException("superState must not be null");
            }

            this.superState = (superState != EMPTY_STATE ? superState : null);
        }

        protected SavedState(Parcel in) {
            this.superState = EMPTY_STATE;
            mSelectedPosition = in.readInt();
            mVerticalSpacingWithMargins = in.readInt();
            mHorizontalSpacingWithMargins = in.readInt();
            mSelectedItemOffsetStart = in.readInt();
            mSelectedItemOffsetEnd = in.readInt();
            boolean[] booleens = new boolean[6];
            in.readBooleanArray(booleens);
            mSelectedItemCentered = booleens[0];
            mIsBaseLayoutManager = booleens[1];
            mIsInterceptKeyEvent = booleens[2];
            mIsMenu = booleens[3];
            mHasMore = booleens[4];
            mIsSelectFirstVisiblePosition = booleens[5];
        }

        public Parcelable getSuperState() {
            return superState;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            out.writeInt(mSelectedPosition);
            out.writeInt(mVerticalSpacingWithMargins);
            out.writeInt(mHorizontalSpacingWithMargins);
            out.writeInt(mSelectedItemOffsetStart);
            out.writeInt(mSelectedItemOffsetEnd);
            boolean[] booleens = {mSelectedItemCentered, mIsBaseLayoutManager,
                    mIsInterceptKeyEvent, mIsMenu, mHasMore, mIsSelectFirstVisiblePosition};
            out.writeBooleanArray(booleens);
        }

        public static final Creator<SavedState> CREATOR
                = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    public void setOnItemListener(OnItemListener onItemListener) {
        mOnItemListener = onItemListener;
    }

    public void setOnInBorderKeyEventListener(OnInBorderKeyEventListener onInBorderKeyEventListener) {
        mOnInBorderKeyEventListener = onInBorderKeyEventListener;
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        mOnLoadMoreListener = onLoadMoreListener;
    }

    public interface OnLoadMoreListener {
        boolean onLoadMore();
    }

    public interface OnInBorderKeyEventListener {
        boolean onInBorderKeyEvent(int direction, int keyCode, KeyEvent event);
    }

    public interface OnItemListener {
        void onItemPreSelected(TvRecyclerView parent, View itemView, int position);

        void onItemSelected(TvRecyclerView parent, View itemView, int position);

        void onReviseFocusFollow(TvRecyclerView parent, View itemView, int position);

        void onItemClick(TvRecyclerView parent, View itemView, int position);
    }

    private interface ItemListener extends View.OnClickListener, View.OnFocusChangeListener {
        
    }
}
