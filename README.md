# ArkScreen
基于标签图像识别的明日方舟公开招募计算器工具 for Android

### 使用

开启悬浮窗权限。

将arkscreen加入通知栏快捷开关。

在公招界面使用，识别标签后弹悬浮窗出组合结果。

### 注意

较新的Android版本可能不稳定，包括但不限于悬浮窗一秒消失。目前看来是Android12的新特性导致的。以后再修。

没钱租服务器，公招池更新需要重新下载新版本。

自行更新数据库的方法：用sqlite把db文件改一改，放进cache文件夹里。

### 1.1和1.0的区别

1.0的前台服务没有设置关闭，用完了也会一直存在通知，除非后台杀死服务。不想看设置一下通知管理。

1.1的前台服务设置了3s关闭，为了防止一直开着前台服务。最好间隔3秒的cd时间再使用。

虽然崩了也问题不大就是了。

1.1在新Android版本似乎闪退更频繁，建议用1.0
