package com.coodays.pushservicelib.utils;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 项目中用到的辅助方法
 * Created by Administrator on 2015/9/29.
 */
public class Tools {
  /**
   * 手机号码是否有效
   */
  public static boolean isValidPhoneNumber(String phoneNumber) {
    if (phoneNumber == null || "".equals(phoneNumber)) {
      return false;
    } else {
      String str = "^(145|147|170|176|177|178|[1][3,5,8][0-9])[0-9]{8}$";
      Pattern pattern = Pattern.compile(str);
      Matcher mc = pattern.matcher(phoneNumber);
      if (!mc.matches()) {
        return false;
      }
      return true;
    }
  }

  /**
   * 判断密码是否正确（必须是数字和字母的组合，长度6-16）
   */
  public static boolean checkPwd(String pwd) {
    if (pwd == null || "".equals(pwd)) {
      return false;
    } else {

      // String str = "(?!^\\d+$)(?!^[a-zA-Z]+$)[0-9a-zA-Z]{6,16}";
      String str = "^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,16}$";
      Pattern pattern = Pattern.compile(str);
      Matcher mc = pattern.matcher(pwd);
      if (!mc.matches()) {
        return false;
      }
      return true;
    }
  }

  /**
   * 判断守护号是英文或者数字，长度是6-12
   */
  public static boolean checkCareName(String name) {

    if (name == null || "".equals(name)) {
      return false;
    } else {
      String str = "[0-9a-zA-Z]{6,12}";
      Pattern pattern = Pattern.compile(str);
      Matcher mc = pattern.matcher(name);

      if (mc.matches()) {
        return true;
      }
      return false;
    }
  }

  /**
   * 短信验证码是否有效
   */
  public static boolean checkSmsCodeValid(String smsCode) {
    if (smsCode == null || "".equals(smsCode)) {
      return false;
    } else {
      if (smsCode.length() != 4) {
        return false;
      }
      return checkIsNumValid(smsCode);
    }
  }

  /**
   * 是否是数字
   */
  public static boolean checkIsNumValid(String num) {
    Pattern pattern = Pattern.compile("[0-9]+");
    boolean isValid = pattern.matcher(num).matches();
    if (!isValid) {
      return false;
    }
    return true;
  }

  /**
   * 判断用户姓名
   */
  public static boolean checkName(String str) {
    if (TextUtils.isEmpty(str)) {
      return false;
    }
    String regEx = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
    Pattern p = Pattern.compile(regEx);
    Matcher m = p.matcher(str);
    if (m.find()) {
      return false;
    }
    return true;
  }

  /**
   * 判断是否为2-5个中文汉字
   */
  public static boolean checkRealName(String str) {
    if (TextUtils.isEmpty(str)) {
      return false;
    }
    String regEx = "^[\\u4e00-\\u9fa5]{2,5}$";
    Pattern p = Pattern.compile(regEx);
    Matcher m = p.matcher(str);
    if (m.matches()) {
      return true;
    }
    return false;
  }

  /**
   * 验证邮箱
   */
  public static boolean checkEmail(String email) {
    boolean flag = false;
    if (TextUtils.isEmpty(email)) {
      return false;
    }
    try {
      String check = "^([a-z0-9A-Z]+[-|_|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
      Pattern regex = Pattern.compile(check);
      Matcher matcher = regex.matcher(email);
      flag = matcher.matches();
    } catch (Exception e) {
      flag = false;
    }
    return flag;
  }

  /**
   * 判断字符串是否由数字组成
   */
  public static boolean isNumeric(String str) {
    if (str == null) {
      return false;
    }
    Pattern pattern = Pattern.compile("[0-9]+");
    return pattern.matcher(str).matches();
  }

  /**
   * 判断字符串是否有效
   */
  public static boolean isString(String str) {
    if (str == null || str.length() <= 0) {
      return false;
    }
    return true;
  }

  /**
   * 获取APP版本号
   *
   * @return 当前应用的版本号
   */
  public static int getVersionCode(Context context) {
    try {
      //获取packagemanager的实例
      PackageManager packageManager = context.getPackageManager();
      //getPackageName()是你当前类的包名，0代表是获取版本信息
      PackageInfo packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
      return packInfo.versionCode;
    } catch (Exception e) {
      e.printStackTrace();
      return 1;
    }
  }
  /**
   * 获取APP版本名
   *
   * @return 当前应用的版本名
   */
  public static String getVersionName(Context context) {
    try {
      //获取packagemanager的实例
      PackageManager packageManager = context.getPackageManager();
      //getPackageName()是你当前类的包名，0代表是获取版本信息
      PackageInfo packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
      return packInfo.versionName;
    } catch (Exception e) {
      e.printStackTrace();
      return "";
    }
  }



  /**
   * 获取包名
   */
  public static String getPackageName(Context context) {
    try {
      //获取packagemanager的实例
      PackageManager packageManager = context.getPackageManager();
      //getPackageName()是你当前类的包名，0代表是获取版本信息
      PackageInfo packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
      return packInfo.packageName;
    } catch (Exception e) {
      e.printStackTrace();
      return "1.0";
    }
  }

  /**
   * 获得指定   年  和月份  ，的天数
   */
  public static int getMaxDayByMonth(int year, int month) {
    int maxDay = 31;
    switch (month) {
      case 1:
      case 3:
      case 5:
      case 7:
      case 8:
      case 10:
      case 12:
        maxDay = 31;
        break;

      case 4:
      case 6:
      case 9:
      case 11:
        maxDay = 30;
        break;

      case 2:
        if (year % 4 == 0 && year % 100 != 0) {
          maxDay = 29;
        } else {
          maxDay = 28;
        }
        break;
    }
    return maxDay;
  }

  /**
   * 将现在时间按照时间格式转换为字符串
   */
  public static String dateToStr(String format) {
    Date date = new Date();
    SimpleDateFormat formatter = new SimpleDateFormat(format);
    String str = formatter.format(date);
    return str;
  }

  /**
   * 根据手机的分辨率从 DP 的单位 转成为 PX(像素)。
   *
   * @param context 上下文环境
   * @param dpValue DP值
   * @return PX值
   */
  public static int dip2px(Context context, float dpValue) {
    final float scale = context.getResources().getDisplayMetrics().density;
    return (int) (dpValue * scale + 0.5f);
  }

  /**
   * 根据手机的分辨率从 PX(像素) 的单位 转成为 DP。
   *
   * @param context 上下文环境
   * @param pxValue PX值
   * @return DP值
   */
  public static int px2dip(Context context, float pxValue) {
    final float scale = context.getResources().getDisplayMetrics().density;
    return (int) (pxValue / scale + 0.5f);
  }



  /**
   * 计算当前维度的角度
   */
  public static double rad(double d) {
    return d * Math.PI / 180.0;
  }

  /**
   * 状态栏的高度
   */
  public static int getStatusBarHeight(Context context) {
    int result = 0;
    int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
    if (resourceId > 0) {
      result = context.getResources().getDimensionPixelSize(resourceId);
    }
    return result;
  }

  /**
   * 判断网络是否连接
   */
  public static boolean isNetworkAvailable(Context context) {
    ConnectivityManager connectivity =
        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    if (connectivity == null) {
      LogUtils.i("NetWorkState", "Unavailabel");
      return false;
    } else {
      NetworkInfo[] info = connectivity.getAllNetworkInfo();
      if (info != null) {
        for (int i = 0; i < info.length; i++) {
          if (info[i].getState() == NetworkInfo.State.CONNECTED) {
            LogUtils.i("NetWorkState", "Availabel");
            return true;
          }
        }
      }
    }
    return false;
  }

  /**
   * 输入框 光标移动到最后一个文字处
   */
  public static void initSelecton(EditText et) {
    if (et != null) {
      String str = et.getText().toString();
      if (str.length() > 0) {
        et.setSelection(str.length());
      }
    }
  }

  //判断文件是否存在
  public static boolean fileIsExists(String strFile) {
    try {
      File f = new File(strFile);
      if (!f.exists()) {
        return false;
      }
    } catch (Exception e) {
      return false;
    }

    return true;
  }

  /**
   * 将View转换为Bitmap
   */
  public static Bitmap getBitmapFromView(View view) {
    view.destroyDrawingCache();
    view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
    view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
    view.setDrawingCacheEnabled(true);
    Bitmap bitmap = view.getDrawingCache(true);
    return bitmap;
  }

  /**
   * 获取手机型号
   */
  public static String getMoibleModel() {
    return android.os.Build.MODEL;
  }

  /**
   * 获取手机操作系统版本
   */
  public static String getMoibleVersion() {
    return android.os.Build.VERSION.RELEASE;
  }

  /**
   * 获取手机imei
   */
  public static String getMoibleImei(Context context) {
    return ((TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE)).getDeviceId();
  }

  /**
   * 判断是否只包含字母和数字
   *
   * @param s String 类型
   */
  public static boolean isOnlyNumberAndLetter(String s) {
    Pattern p = Pattern.compile("^[A-Za-z0-9]+$");
    Matcher m = p.matcher(s);
    boolean b = m.matches();
    return b;
  }

  /**
   * 获得当前进程名称
   */
  public static String getCurrentProcessName(Context context) {
    int pid = android.os.Process.myPid();
    ActivityManager mActivityManager =
        (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager.getRunningAppProcesses()) {
      if (appProcess.pid == pid) {
        return appProcess.processName;
      }
    }
    return null;
  }

  /**
   * 判断是否是主进程， 这里在androidManifest里设置进程名都带:xxxx
   * application 获得进程是  packageName
   */
  public static boolean isMainProcess(Context mContext) {
    String packageName = mContext.getPackageName();
    // String className = mContext.getClass().getSimpleName();
    String corrProcess = getCurrentProcessName(mContext);
    if (corrProcess == null) {//iuni上会出现这个问题。
      return false;
    }
    if (corrProcess.equals(packageName)) {
      return true;
    }
    return false;
  }

  /**
   * 判断微信是否安装
   */
  public static boolean isWeixinAvilible(Context context) {
    final PackageManager packageManager = context.getPackageManager();// 获取packagemanager
    List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);// 获取所有已安装程序的包信息
    if (pinfo != null) {
      for (int i = 0; i < pinfo.size(); i++) {
        String pn = pinfo.get(i).packageName;
        if (pn.equals("com.tencent.mm")) {
          return true;
        }
      }
    }

    return false;
  }

  /**
   * 需要权限:android.permission.GET_TASKS
   * 判断本程序是否运行在前台
   *
   * @return true
   */
  public static boolean isApplicationInFornt(Context context) {
    ActivityManager am =
        (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
    if (tasks != null && !tasks.isEmpty()) {
      ComponentName topActivity = tasks.get(0).topActivity;
      //LogUtils.v("tools", " the app is "+topActivity.getPackageName() );
      if (topActivity.getPackageName().equals(context.getPackageName())) {
        return true;
      }
    }
    return false;
  }

  /**
   * 判断页面是否正在显示
   */
  public static boolean isActivityTop(Context context, Class cls) {
    ActivityManager am =
        (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(1);
    if (list != null && list.size() > 0) {
      ComponentName cpn = list.get(0).topActivity;
      if (cls.getName().equals(cpn.getClassName())) {
        return true;
      }
    }
    return false;
  }

  /**
   * @param delayTime 延迟时间
   */
  //开启单次闹钟服务
  public static void startOnceAlarmService(Context context, int delayTime, Class<?> cls, String action) {
    //获取AlarmManager系统服务
    AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

    //包装需要执行Service的Intent
    Intent intent = new Intent(context, cls);
    intent.setAction(action);
    PendingIntent pendingIntent =
        PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    long startTime = System.currentTimeMillis();
    //使用AlarmManger的setRepeating方法设置定期执行的时间间隔（seconds秒）和需要执行的Service
    manager.set(AlarmManager.RTC, startTime + delayTime * 1000, pendingIntent);
  }

  //停止闹钟服务
  public static void stopAlarmService(Context context, Class<?> cls, String action) {
    AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    Intent intent = new Intent(context, cls);
    intent.setAction(action);
    PendingIntent pendingIntent =
        PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    //取消正在执行的服务
    manager.cancel(pendingIntent);
    context.stopService(intent);
  }

}
