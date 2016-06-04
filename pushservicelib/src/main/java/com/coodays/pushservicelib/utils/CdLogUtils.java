package com.coodays.pushservicelib.utils;

import android.util.Log;

/**
 * Log统一管理类
 * @author zxh
 */
public class CdLogUtils
{

	private CdLogUtils()
	{
		/* cannot be instantiated */
		throw new UnsupportedOperationException("cannot be instantiated");
	}

	public static boolean isDebug = true;// 是否需要打印bug，可以在application的onCreate函数里面初始化
	private static final String TAG = "com.coodays.cd51Repair";
	private static final String PREFIX = "coodays_push_";

	//// 下面四个是默认tag的函数
	//public static void i(String msg)
	//{
	//	if (isDebug)
	//		Log.i(TAG, msg);
	//}
  //
	//public static void d(String msg)
	//{
	//	if (isDebug)
	//		Log.d(TAG, msg);
	//}
  //
	//public static void e(String msg)
	//{
	//	if (isDebug)
	//		Log.e(TAG, msg);
	//}
  //
	//public static void v(String msg)
	//{
	//	if (isDebug)
	//		Log.v(TAG, msg);
	//}

	// 下面是传入自定义tag的函数
	public static void i(String tag, String msg)
	{
		if (isDebug)
			Log.i(PREFIX+tag, msg);
	}

	public static void d(String tag, String msg)
	{
		if (isDebug)
			Log.d(PREFIX+tag, msg);
	}

	public static void e(String tag, String msg)
	{
		if (isDebug)
			Log.e(PREFIX+tag, msg);
	}

	public static void v(String tag, String msg)
	{
		if (isDebug)
			Log.v(PREFIX+tag, msg);
	}

	public static void w(String tag, String msg)
	{
		if (isDebug)
			Log.w(PREFIX+tag, msg);
	}

	//*******************************************************************
	//					下面是传入是传入this类打印
	//*******************************************************************
	//public static void i(Object thisObj, String msg)
	//{
	//	if (isDebug)
	//		Log.i(thisObj.getClass().getSimpleName(), msg);
	//}
  //
	//public static void d(Object thisObj, String msg)
	//{
	//	if (isDebug)
	//		Log.d(thisObj.getClass().getSimpleName(), msg);
	//}
  //
	//public static void e(Object thisObj, String msg)
	//{
	//	if (isDebug)
	//		Log.e(thisObj.getClass().getSimpleName(), msg);
	//}
  //
	//public static void v(Object thisObj, String msg)
	//{
	//	if (isDebug)
	//		Log.v(thisObj.getClass().getSimpleName(), msg);
	//}
  //
	//public static void w(Object thisObj, String msg) {
	//	if (isDebug)
	//		Log.w(thisObj.getClass().getSimpleName(), msg);
	//}
}