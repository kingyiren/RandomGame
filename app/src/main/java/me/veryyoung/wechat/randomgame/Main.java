package me.veryyoung.wechat.randomgame;

import android.app.Activity;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;


public class Main implements IXposedHookLoadPackage {

    private static final String WECHAT_PACKAGE_NAME = "com.tencent.mm";

    private static String wechatVersion = "";

    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        if (lpparam.packageName.equals(WECHAT_PACKAGE_NAME)) {
            if (TextUtils.isEmpty(wechatVersion)) {
                Context context = (Context) callMethod(callStaticMethod(findClass("android.app.ActivityThread", null), "currentActivityThread", new Object[0]), "getSystemContext", new Object[0]);
                String versionName = context.getPackageManager().getPackageInfo(lpparam.packageName, 0).versionName;
                log("Found wechat version:" + versionName);
                this.wechatVersion = versionName;
                VersionParam.init(versionName);

                if (!support(versionName)) {
                    log("Maybe RandomGame don't support wechat at version:" + versionName);
                }
            }
            findAndHookMethod("com.tencent.mm.ui.LauncherUI", lpparam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Activity activity = (Activity) param.thisObject;
                    if (activity != null) {
                        Intent intent = activity.getIntent();
                        if (intent != null) {
                            String className = intent.getComponent().getClassName();
                            if (!TextUtils.isEmpty(className) && className.equals("com.tencent.mm.ui.LauncherUI") && intent.hasExtra("donate")) {
                                Intent donateIntent = new Intent();
                                donateIntent.setClassName(activity, "com.tencent.mm.plugin.remittance.ui.RemittanceUI");
                                donateIntent.putExtra("scene", 1);
                                donateIntent.putExtra("pay_scene", 32);
                                donateIntent.putExtra("scan_remittance_id", "011259012001125901201468688368254");
                                donateIntent.putExtra("fee", 10.0d);
                                donateIntent.putExtra("pay_channel", 12);
                                donateIntent.putExtra("receiver_name", "yang_xiongwei");
                                donateIntent.removeExtra("donate");
                                activity.startActivity(donateIntent);
                                activity.finish();
                            }
                        }
                    }
                }
            });

            findAndHookMethod(VersionParam.randomGameClass, lpparam.classLoader, VersionParam.gameType, int.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    int gameType = ((Integer) param.args[0]).intValue();
                    switch (gameType) {
                        case 2:
                            if (PreferencesUtils.moraCheat()) {
                                param.setResult(PreferencesUtils.moraNum());
                            }
                            break;
                        case 5:
                            if (PreferencesUtils.diceCheat()) {
                                param.setResult(PreferencesUtils.diceNum());
                            }
                        default:
                    }
                }
            });


            hideModule(lpparam);

        }
    }


    private void hideModule(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        findAndHookMethod("android.app.ApplicationPackageManager", loadPackageParam.classLoader, "getInstalledApplications", int.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                List<ApplicationInfo> applicationList = (List) param.getResult();
                List<ApplicationInfo> resultapplicationList = new ArrayList<>();
                for (ApplicationInfo applicationInfo : applicationList) {
                    String packageName = applicationInfo.packageName;
                    if (isTarget(packageName)) {
                        log("Hid package: " + packageName);
                    } else {
                        resultapplicationList.add(applicationInfo);
                    }
                }
                param.setResult(resultapplicationList);
            }
        });
        findAndHookMethod("android.app.ApplicationPackageManager", loadPackageParam.classLoader, "getInstalledPackages", int.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                List<PackageInfo> packageInfoList = (List) param.getResult();
                List<PackageInfo> resultpackageInfoList = new ArrayList<>();

                for (PackageInfo packageInfo : packageInfoList) {
                    String packageName = packageInfo.packageName;
                    if (isTarget(packageName)) {
                        log("Hid package: " + packageName);
                    } else {
                        resultpackageInfoList.add(packageInfo);
                    }
                }
                param.setResult(resultpackageInfoList);
            }
        });
        findAndHookMethod("android.app.ApplicationPackageManager", loadPackageParam.classLoader, "getPackageInfo", String.class, int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                String packageName = (String) param.args[0];
                if (isTarget(packageName)) {
                    param.args[0] = WECHAT_PACKAGE_NAME;
                    log("Fake package: " + packageName + " as " + WECHAT_PACKAGE_NAME);
                }
            }
        });
        findAndHookMethod("android.app.ApplicationPackageManager", loadPackageParam.classLoader, "getApplicationInfo", String.class, int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                String packageName = (String) param.args[0];
                if (isTarget(packageName)) {
                    param.args[0] = WECHAT_PACKAGE_NAME;
                    log("Fake package: " + packageName + " as " + WECHAT_PACKAGE_NAME);
                }
            }
        });
        findAndHookMethod("android.app.ActivityManager", loadPackageParam.classLoader, "getRunningServices", int.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                List<RunningServiceInfo> serviceInfoList = (List) param.getResult();
                List<RunningServiceInfo> resultList = new ArrayList<>();

                for (RunningServiceInfo runningServiceInfo : serviceInfoList) {
                    String serviceName = runningServiceInfo.process;
                    if (isTarget(serviceName)) {
                        log("Hid service: " + serviceName);
                    } else {
                        resultList.add(runningServiceInfo);
                    }
                }
                param.setResult(resultList);
            }
        });
        findAndHookMethod("android.app.ActivityManager", loadPackageParam.classLoader, "getRunningTasks", int.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                List<RunningTaskInfo> serviceInfoList = (List) param.getResult();
                List<RunningTaskInfo> resultList = new ArrayList<>();

                for (RunningTaskInfo runningTaskInfo : serviceInfoList) {
                    String taskName = runningTaskInfo.baseActivity.flattenToString();
                    if (isTarget(taskName)) {
                        log("Hid task: " + taskName);
                    } else {
                        resultList.add(runningTaskInfo);
                    }
                }
                param.setResult(resultList);
            }
        });
        findAndHookMethod("android.app.ActivityManager", loadPackageParam.classLoader, "getRunningAppProcesses", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                List<RunningAppProcessInfo> runningAppProcessInfos = (List) param.getResult();
                List<RunningAppProcessInfo> resultList = new ArrayList<>();

                for (RunningAppProcessInfo runningAppProcessInfo : runningAppProcessInfos) {
                    String processName = runningAppProcessInfo.processName;
                    if (isTarget(processName)) {
                        log("Hid process: " + processName);
                    } else {
                        resultList.add(runningAppProcessInfo);
                    }
                }
                param.setResult(resultList);
            }
        });
    }


    private boolean isTarget(String name) {
        return name.contains("veryyoung") || name.contains("xposed");
    }


    private boolean support(String wechatVersion) {
        List<String> supportVersion = Arrays.asList("6.3.25");
        return supportVersion.contains(wechatVersion);
    }

}
