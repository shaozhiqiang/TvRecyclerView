package com.owen.tvrecyclerview.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;

/**
 * 分辨率转换类
 * 
 * @author zhousuqiang
 * */
public class DensityUtil {

	public static int getScreenHeight(Context context) {
		return getDisplayMetrics(context).heightPixels;
	}

	public static int getScreenWidth(Context context) {
		return getDisplayMetrics(context).widthPixels;
	}

	public static DisplayMetrics getDisplayMetrics(Context context){
		DisplayMetrics dm = null;
		if(null != context){
			dm = context.getResources().getDisplayMetrics();
		}
		return dm;
	}

	/**
	 * 根据分辨率从 dp 转为 px
	 */
	public static int dip2px(Context context, float dpValue) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, getDisplayMetrics(context));
//		final float scale = getDisplayMetrics(context).density;
//		return (int) (dpValue * scale + 0.5f);
	}

	/**
	 * 根据分辨率从 px 转为 dp
	 */
	public static int px2dip(Context context, float pxValue) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, pxValue, getDisplayMetrics(context));
//		final float scale = getDisplayMetrics(context).density;
//		return (int) (pxValue / scale + 0.5f);
	}

	public static int sp2px(Context context, float spValue){
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue, getDisplayMetrics(context));
	}

	/**
	 * 根据分辨率从 px 转为 px
	 * @param context
	 * @param pxValue 720P下的width像素值
     * @return
     */
	public static float px2pxByWidth(Context context, int pxValue) {
		float cellWidth = getScreenWidth(context) * 1.0f / 1280;
		return cellWidth * pxValue;
	}

	/**
	 * 根据分辨率从 px 转为 px
	 * @param context
	 * @param pxValue 720P下的height像素值
	 * @return
	 */
	public static float px2pxByHeight(Context context, int pxValue) {
		float cellHeight = getScreenHeight(context) * 1.0f / 720;
		return cellHeight * pxValue;
	}
}
