package com.owen.tvrecyclerview.example.bridge;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.owen.tvrecyclerview.example.R;

import java.lang.ref.WeakReference;

public class RecyclerViewBridge extends EffectNoDrawBridge {
    private static final int TAG_KEY_X = R.id.tag_offset_x;
    private static final int TAG_KEY_Y = R.id.tag_offset_y;
    private WeakReference<RecyclerView> mWeakRecyclerView;
	private AnimatorSet mCurrentAnimatorSet;
    private RecyclerViewScrollListener mRecyclerViewScrollListener;
    private int mOffsetX;
    private int mOffsetY;
    private ScaleView mScaleView;
    private int newWidth = 0;
    private int newHeight = 0;
    private int oldWidth = 0;
    private int oldHeight = 0;
    private int newX = 0;
    private int newY = 0;
    private float mScaleX = 1f;
    private float mScaleY = 1f;
    
    private void registerScrollListener(RecyclerView recyclerView) {
        if(null != mWeakRecyclerView && mWeakRecyclerView.get() == recyclerView) {
            return;
        } 
        else if(null == mWeakRecyclerView || null == mWeakRecyclerView.get()) {
            mWeakRecyclerView = new WeakReference<>(recyclerView);
        }
        else if(mWeakRecyclerView.get() != recyclerView) {
            mWeakRecyclerView.get().removeOnScrollListener(mRecyclerViewScrollListener);
            mWeakRecyclerView.clear();
            mWeakRecyclerView = new WeakReference<>(recyclerView);
        }
        
        if(null == mRecyclerViewScrollListener) {
            mRecyclerViewScrollListener = new RecyclerViewScrollListener(this);
        }
        recyclerView.removeOnScrollListener(mRecyclerViewScrollListener);
        recyclerView.addOnScrollListener(mRecyclerViewScrollListener);
        
    }
    
    //查询focusView位置
    private Rect findLocationWithFocusView(View focusView) {
        Rect outRect = findLocationWithView(focusView);
        
        if(focusView.getParent() instanceof RecyclerView) {
            final RecyclerView recyclerView = (RecyclerView) focusView.getParent();
            registerScrollListener(recyclerView);

            final Object tagX = focusView.getTag(TAG_KEY_X);
            mOffsetX = (tagX != null && tagX instanceof Integer) ? (int) tagX : 0;
            final Object tagY = focusView.getTag(TAG_KEY_Y);
            mOffsetY = (tagY != null && tagY instanceof Integer) ? (int) tagY : 0;
            
            if(mOffsetX != 0 || mOffsetY != 0) {
                outRect.offset(-mOffsetX, -mOffsetY);
                mOffsetX = mOffsetY = 0;
            }
        }
        
        return outRect;
    }

    private void runAnim(final View focusView, final View moveView, final float scaleX, final float scaleY, int duration) {
        if(null == focusView)
            return;

        // 取消之前的动画.
        if (mCurrentAnimatorSet != null) {
            mCurrentAnimatorSet.cancel();
            mCurrentAnimatorSet = null;
        }

        Rect paddingRect = getDrawUpRect();

        newWidth = (int) (focusView.getMeasuredWidth() * scaleX);
        newHeight = (int) (focusView.getMeasuredHeight() * scaleY);
        oldWidth = moveView.getMeasuredWidth();
        oldHeight = moveView.getMeasuredHeight();
        Rect fromRect = findLocationWithView(moveView);
        Rect toRect = findLocationWithFocusView(focusView);

        //
        int x = toRect.left - fromRect.left - (paddingRect.left);
        int y = toRect.top - fromRect.top - (paddingRect.top);
        newX = x - Math.abs(focusView.getMeasuredWidth() - newWidth) / 2;
        newY = y - Math.abs(focusView.getMeasuredHeight() - newHeight) / 2;
        //
        newWidth += (paddingRect.right + paddingRect.left);
        newHeight += (paddingRect.bottom + paddingRect.top);

        if(null == mScaleView) {
            mScaleView = new ScaleView(moveView);
        }

        ObjectAnimator transAnimatorX = ObjectAnimator.ofFloat(moveView, "translationX", newX);
        ObjectAnimator transAnimatorY = ObjectAnimator.ofFloat(moveView, "translationY", newY);
        // BUG，因为缩放会造成图片失真(拉伸).
        // hailong.qiu 2016.02.26 修复 :)
        ObjectAnimator scaleXAnimator = ObjectAnimator.ofInt(mScaleView, "width", oldWidth, newWidth);
        ObjectAnimator scaleYAnimator = ObjectAnimator.ofInt(mScaleView, "height", oldHeight, newHeight);
        //
        final AnimatorSet mAnimatorSet = new AnimatorSet();
        mAnimatorSet.playTogether(transAnimatorX, transAnimatorY, scaleXAnimator, scaleYAnimator);
        mAnimatorSet.setInterpolator(new DecelerateInterpolator(1));
        mAnimatorSet.setDuration(duration);
        mAnimatorSet.addListener(new AnimatorListener() {
            private boolean isCancel = false;

            @Override
            public void onAnimationStart(Animator animation) {
                if (isVisibleWidget()) {
                    getMainUpView().setVisibility(View.GONE);
                }
                if (getNewAnimatorListener() != null)
                    getNewAnimatorListener().onAnimationStart(RecyclerViewBridge.this, focusView, animation);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if(!isCancel) {
                    if ((newWidth - moveView.getMeasuredWidth()) > 1 || (newHeight - moveView.getMeasuredHeight()) > 1) {
                        mScaleView.setWidth(newWidth);
                        mScaleView.setHeight(newHeight);
                    }
                }
                getMainUpView().setVisibility(isVisibleWidget() ? View.GONE : View.VISIBLE);
                if (getNewAnimatorListener() != null) {
                    getNewAnimatorListener().onAnimationEnd(RecyclerViewBridge.this, focusView, animation);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                isCancel = true;
            }
        });
        mAnimatorSet.start();
        mCurrentAnimatorSet = mAnimatorSet;
    }
    
	/**
	 * 重写边框移动函数.
	 */
	@Override
	public void flyWhiteBorder(final View focusView, final View moveView, final float scaleX, final float scaleY) {
        mScaleX = scaleX;
        mScaleY = scaleY;
        runAnim(focusView, moveView, scaleX, scaleY, getTranDurAnimTime());
	}

    /**
     * 纠正焦点框
     * @param focusView
     */
	private void rectifyFocusFollow(View focusView) {
        runAnim(focusView, getMainUpView(), mScaleX, mScaleY, getTranDurAnimTime());
    }
	
	
	private static class RecyclerViewScrollListener extends RecyclerView.OnScrollListener {
        private WeakReference<RecyclerViewBridge> mWeakRecyclerViewBridge;
        private int mScrolledX = 0, mScrolledY = 0;

        public RecyclerViewScrollListener(RecyclerViewBridge bridge){
            mWeakRecyclerViewBridge = new WeakReference<>(bridge);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            mScrolledX = Math.abs(dx) == 1 ? 0 : dx;
            mScrolledY = Math.abs(dy) == 1 ? 0 : dy;
            Log.i("@!@!", "onScrolled...dx="+dx+" dy="+dy);
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            if(newState == RecyclerView.SCROLL_STATE_IDLE) {
                Log.i("@!@!", "onScrollStateChanged...IDLE");
                final RecyclerViewBridge bridge = mWeakRecyclerViewBridge.get();
                Log.i("@!@!", "onScrollStateChanged...bridge is null = " + (null == bridge));
                if((mScrolledX != 0 || mScrolledY != 0) && null != bridge) {
                    bridge.rectifyFocusFollow(recyclerView.getFocusedChild());
                }
                mScrolledX = mScrolledY = 0;
            }
        }
    }
    
}
