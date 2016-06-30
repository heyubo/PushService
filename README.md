# PushService
封装小米、华为、云信等的消息推送服务

v1.03 同时启用3套推送、添加推送重复、用户过滤

混淆
##---------------Begin: proguard configauration for  xiaomi ---------------
-keep class com.coodays.cd51Repair.bean.MiuiPushMessageReceiver { *; }
##-------------------------------------------------------------------------

##---------------Begin:proguard configuration for netease --------------
-dontwarn com.netease.**
-dontwarn io.netty.**
-keep class com.netease.** {*;}
##---------------End: proguard confiuration for netease ----------------

##---------------Begin: proguard configauration for  huawei ---------------
-keep class com.huawei.android.pushagent.**{*;}
-keep class com.huawei.android.pushselfshow.**{*;}
-keep class com.huawei.android.microkernel.**{*;}
-dontwarn com.huawei.android.pushagent.**
-dontwarn com.huawei.android.pushselfshow.**
-dontwarn com.huawei.android.microkernel.**
##-------------------------------------------------------------------------

-keepnames class com.coodays.pushservicelib.bean.** { *; }