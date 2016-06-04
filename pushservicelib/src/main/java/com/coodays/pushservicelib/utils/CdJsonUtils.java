package com.coodays.pushservicelib.utils;

import android.support.v4.util.ArrayMap;
import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by panliuting on 16/3/18.
 */
public class CdJsonUtils {
  public static String hashMapToJson(HashMap map) {
    return toJson(map);
  }

  public static String arrayMapToJson(ArrayMap map) {
    return toJson(map);
  }

  private static String toJson(Map map){
    if (map.isEmpty()) return "{}";
    Gson gson = new Gson();
    String json = gson.toJson(map);
    return json;
  }
}
