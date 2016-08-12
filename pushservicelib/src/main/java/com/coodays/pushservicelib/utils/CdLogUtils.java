package com.coodays.pushservicelib.utils;

import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

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

	public static void dWriteLog(String tag, String msg, String textName) {
		writeLogToFile(tag, msg, textName);
	}

	private static FileOutputStream fos = null;
	private static String mFileName = null;

	private static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
	private static SimpleDateFormat timelogFormatter = new SimpleDateFormat("HH:mm:ss");

	private static final String PARENT_DIR = "cd51Repair";

	public static void writeLogToFile(final String tag,final String msg, final String textName)
	{
		if(msg == null || msg.equals(""))
		{
			Log.v(tag, "writeLogToFile failed1");
			return;
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					if(fos != null)
					{
						File tmpFile = new File(mFileName);
						if (!tmpFile.exists()) {
							closeLogFile();
							openLogFile(textName);
						}
						String time = timelogFormatter.format(new Date())+"   ";
						StringBuffer sb = new StringBuffer();
						sb.append(time);
						sb.append(tag);
						int len = tag.length();
						int timelen = time.length();
						sb.setLength(timelen+32);
						for(int i=0;i<32-len;i++)
						{
							sb.setCharAt(timelen+len+i, ' ');
						}
						sb.append(msg);
						sb.append("\r\n");
						fos.write(sb.toString().getBytes());

					}
					else
					{
						openLogFile(textName);
						writeLogToFile(tag, msg, textName);
					}
				} catch (Exception e) {
					Log.v(tag, "writeLogToFile err111");
					e.printStackTrace();
				}
			}
		}).start();
	}
	private static void openLogFile(String textName)
	{
		try {
			//long timestamp = System.currentTimeMillis();
			String time = formatter.format(new Date());
			String fileName = textName+".txt";
			File parentFile = null;
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				File file = Environment.getExternalStorageDirectory();
				parentFile = new File(file, PARENT_DIR+"/"+"推送信息");
				if(!parentFile.exists())
				{
					parentFile.mkdirs();
				}
				if(parentFile != null)
				{
					mFileName = parentFile.getAbsolutePath() +"/"+ fileName;
					fos = new FileOutputStream(mFileName,true);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void closeLogFile()
	{
		try {
			if(fos != null)
			{
				fos.flush();
				fos.close();
				fos = null;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}