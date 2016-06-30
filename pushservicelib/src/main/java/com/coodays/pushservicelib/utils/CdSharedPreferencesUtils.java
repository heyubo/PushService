package com.coodays.pushservicelib.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.coodays.pushservicelib.bean.CdTokenBean;
import com.coodays.pushservicelib.bean.CdTokenListBean;
import com.google.gson.Gson;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;

public class CdSharedPreferencesUtils {
  public static final String KEY_APP_USER_ID = "cd51Repair_appUserId";//app用户ID
  //public static final String KEY_TOKEN_UPLOAD ="cd51Repair_tokenUpload";//s上次上传的token
  public static final String KEY_READ_MESSAGE_URL = "cd51Repair_readMessage";//消息收到 回传URL
  private static final String KEY_TOKEN = "token";//缓存token值
  /**
   * 保存在手机里面的文件名
   */
  public static final String FILE_NAME = "coodays_share_data";

  /**
   * 保存数据的方法，我们需要拿到保存数据的具体类型，然后根据类型调用不同的保存方法
   */
  public static String put(Context context, String key, Object object) {
    if (object == null) {
      return key;
    }

    SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = sp.edit();

    if (object instanceof String) {
      editor.putString(key, (String) object);
    } else if (object instanceof Integer) {
      editor.putInt(key, (Integer) object);
    } else if (object instanceof Boolean) {
      editor.putBoolean(key, (Boolean) object);
    } else if (object instanceof Float) {
      editor.putFloat(key, (Float) object);
    } else if (object instanceof Long) {
      editor.putLong(key, (Long) object);
    } else {
      editor.putString(key, object.toString());
    }

    //SharedPreferencesCompat.apply(editor);
    editor.commit();
    return key;
  }

  /**
   * 得到保存数据的方法，我们根据默认值得到保存的数据的具体类型，然后调用相对于的方法获取值
   */
  public static Object get(Context context, String key, Object defaultObject) {
    SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);

    if (defaultObject instanceof String) {
      return sp.getString(key, (String) defaultObject);
    } else if (defaultObject instanceof Integer) {
      return sp.getInt(key, (Integer) defaultObject);
    } else if (defaultObject instanceof Boolean) {
      return sp.getBoolean(key, (Boolean) defaultObject);
    } else if (defaultObject instanceof Float) {
      return sp.getFloat(key, (Float) defaultObject);
    } else if (defaultObject instanceof Long) {
      return sp.getLong(key, (Long) defaultObject);
    }

    return null;
  }

  /**
   * 移除某个key值已经对应的值
   */
  public static void remove(Context context, String key) {
    SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = sp.edit();
    editor.remove(key);
    SharedPreferencesCompat.apply(editor);
  }

  /**
   * 清除所有数据
   */
  public static void clear(Context context) {
    SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = sp.edit();
    editor.clear();
    SharedPreferencesCompat.apply(editor);
  }

  /**
   * 查询某个key是否已经存在
   */
  public static boolean contains(Context context, String key) {
    SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    return sp.contains(key);
  }

  /**
   * 返回所有的键值对
   */
  public static Map<String, ?> getAll(Context context) {
    SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    return sp.getAll();
  }

  /**
   * 创建一个解决SharedPreferencesCompat.apply方法的一个兼容类
   *
   * @author zhy
   */
  private static class SharedPreferencesCompat {
    private static final Method sApplyMethod = findApplyMethod();

    /**
     * 反射查找apply的方法
     */
    @SuppressWarnings({ "unchecked", "rawtypes" }) private static Method findApplyMethod() {
      try {
        Class clz = SharedPreferences.Editor.class;
        return clz.getMethod("apply");
      } catch (NoSuchMethodException e) {
      }

      return null;
    }

    /**
     * 如果找到则使用apply执行，否则使用commit
     */
    public static void apply(SharedPreferences.Editor editor) {
      try {
        if (sApplyMethod != null) {
          sApplyMethod.invoke(editor);
          return;
        }
      } catch (IllegalArgumentException | IllegalAccessException e) {
      } catch (InvocationTargetException e) {
      }
      editor.commit();
    }
  }

  /**
   * 添加或更新 对应phone_board的token值
   */
  public static void savaToken(Context context, String phone_board, String token) {
    String str = (String) get(context, KEY_TOKEN, "");
    CdTokenListBean list;
    Gson gson = new Gson();
    if (str.equals("")) { //首次新增
      list = new CdTokenListBean();
      CdTokenBean bean = new CdTokenBean();
      bean.setToken(token);
      bean.setPhone_brand(phone_board);
      list.setList(new ArrayList<CdTokenBean>());
      list.getList().add(bean);
      str = gson.toJson(list);
    } else {
      list = gson.fromJson(str, CdTokenListBean.class);
      for (CdTokenBean bean : list.getList()) {
        if (bean.getPhone_brand().equals(phone_board)) {//更新
          bean.setToken(token);
          str = gson.toJson(list);
          put(context, KEY_TOKEN, str);
          return;
        }
      }
      //新增
      CdTokenBean bean = new CdTokenBean();
      bean.setToken(token);
      bean.setPhone_brand(phone_board);
      list.getList().add(bean);
      str = gson.toJson(list);
    }
    put(context, KEY_TOKEN, str);
  }

  /**
   * 清除token
   */
  public static void cleanToken(Context context) {
    put(context, KEY_TOKEN, "");
  }

  /**
   * 获得所有的token 和对应的phone_board
   */
  public static String getToken(Context context) {
    String str = (String) get(context, KEY_TOKEN, "");
    if (str.equals("")) return str;
    CdTokenListBean list;
    Gson gson = new Gson();
    try {
      list = gson.fromJson(str, CdTokenListBean.class);
      return gson.toJson(list.getList());
    } catch (Exception e) {
      e.printStackTrace();
      put(context, KEY_TOKEN, "");
    }
    return "";
  }

  /**
   * 获得对应手机类型的 token
   */
  public static String getTokenSingle(Context context, String phone_board) {
    String str = (String) get(context, KEY_TOKEN, "");
    if (str.equals("")) return str;
    CdTokenListBean list;
    Gson gson = new Gson();
    try {
      list = gson.fromJson(str, CdTokenListBean.class);
      for (CdTokenBean bean : list.getList()) {
        if (bean.getPhone_brand().equals(phone_board)) {//更新
          return bean.getToken();
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      put(context, KEY_TOKEN, "");
    }
    return "";
  }
}
