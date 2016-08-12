# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/panliuting/dev_tool/adt-bundle-mac-x86_64-20131030/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
##---------------Begin: proguard configauration for  xiaomi ---------------
-keep class com.coodays.cd51Repair.bean.MiuiPushMessageReceiver { *; }
##-------------------------------------------------------------------------

##--------------------------------------------------------------------------
-keep class com.baidu.** {*;}
-keep class vi.com.** {*;}
-dontwarn com.baidu.**
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

##---------------Begin:proguard configuration for pushlib --------------
-keepnames class com.coodays.pushservicelib.bean.** { *; }
##---------------End: proguard confiuration for pushlib ----------------