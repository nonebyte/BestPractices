# BestPractices
Android最佳实践

###BaseConfig
Android程序基础信息配置，例如
public static final boolean DEBUG = true;  //debug开关，可用来控制log输出等
   

###ContextManager
Android程序Context管理，提供context常用api的封装，例如
activityManager()方法为(ActivityManager) sAppContext.getSystemService(Context.ACTIVITY_SERVICE)的封装

###Assert
Android原生assert默认是disable不可用的（详见 http://stackoverflow.com/questions/2364910/can-i-use-assert-on-android-devices）,
这里封装自己的Assert

###Dlog
解决打log时出现字符串导致OOM问题，自定义log并提供release包打log功能
