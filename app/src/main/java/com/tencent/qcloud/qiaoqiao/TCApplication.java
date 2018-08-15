package com.tencent.qcloud.qiaoqiao;

import android.app.Activity;
import android.os.Bundle;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.tencent.bugly.crashreport.CrashReport;
import com.tencent.qcloud.qiaoqiao.common.utils.TCConstants;
import com.tencent.qcloud.qiaoqiao.common.utils.TCHttpEngine;
import com.tencent.qcloud.qiaoqiao.login.TCUserMgr;
import com.tencent.rtmp.TXLiveBase;
import com.tencent.ugc.TXUGCBase;
import com.umeng.socialize.PlatformConfig;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

//import com.squareup.leakcanary.LeakCanary;
//import com.squareup.leakcanary.RefWatcher;
/**
 * 小视频应用类，用于全局的操作，如
 * sdk初始化,全局提示框
 */
public class TCApplication extends MultiDexApplication {

//    private RefWatcher mRefWatcher;

    private static TCApplication instance;
    private String ugcKey = "c8a22e70cdfcc946634e944d8436cbb2";
    private String ugcLicenceUrl = "http://license.vod2.myqcloud.com/license/v1/c0e542d5006f755b89ab6750c6064ae9/TXUgcSDK.licence";

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        initSDK();

        //配置分享第三方平台的appkey
        PlatformConfig.setWeixin(TCConstants.WEIXIN_SHARE_ID, TCConstants.WEIXIN_SHARE_SECRECT);
        PlatformConfig.setSinaWeibo(TCConstants.SINA_WEIBO_SHARE_ID, TCConstants.SINA_WEIBO_SHARE_SECRECT, TCConstants.SINA_WEIBO_SHARE_REDIRECT_URL);
        PlatformConfig.setQQZone(TCConstants.QQZONE_SHARE_ID, TCConstants.QQZONE_SHARE_SECRECT);
//        mRefWatcher = LeakCanary.install(this);
        // 短视频licence设置
        TXUGCBase.getInstance().setLicence(this, ugcLicenceUrl, ugcKey);

        // 上报启动次数
        TCUserMgr.getInstance().uploadLogs(TCConstants.ELK_ACTION_START_UP, TCUserMgr.getInstance().getUserId(), 0, "", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });
        registerActivityLifecycleCallbacks(new MyActivityLifecycleCallbacks(this));
    }

    public static TCApplication getApplication() {
        return instance;
    }

//    public static RefWatcher getRefWatcher(Context context) {
//        TCApplication application = (TCApplication) context.getApplicationContext();
//        return application.mRefWatcher;
//    }

    /**
     * 初始化SDK，包括Bugly，LiteAVSDK等
     */
    public void initSDK() {
        //启动bugly组件，bugly组件为腾讯提供的用于crash上报和分析的开放组件，如果您不需要该组件，可以自行移除
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(getApplicationContext());
        strategy.setAppVersion(TXLiveBase.getSDKVersionStr());
        CrashReport.initCrashReport(getApplicationContext(), TCConstants.BUGLY_APPID, true, strategy);

        //设置rtmpsdk log回调，将log保存到文件
//        TXLiveBase.setListener(new TCLog(getApplicationContext()));

        TCUserMgr.getInstance().initContext(getApplicationContext());
        TCHttpEngine.getInstance().initContext(getApplicationContext());

        Log.w("TCLog", "app init sdk");
    }

    private class MyActivityLifecycleCallbacks implements ActivityLifecycleCallbacks {

        private int foregroundActivities;
        private boolean isChangingConfiguration;
        private long time;

        public MyActivityLifecycleCallbacks(TCApplication tcApplication) {

        }

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

        }

        @Override
        public void onActivityStarted(Activity activity) {
            foregroundActivities++;
            if (foregroundActivities == 1 && !isChangingConfiguration) {
                // 应用进入前台
                time = System.currentTimeMillis();
            }
            isChangingConfiguration = false;
        }

        @Override
        public void onActivityResumed(Activity activity) {

        }

        @Override
        public void onActivityPaused(Activity activity) {

        }

        @Override
        public void onActivityStopped(Activity activity) {
            foregroundActivities--;
            if (foregroundActivities == 0) {
                // 应用切入后台
                long bgTime = System.currentTimeMillis();
                long diff = (bgTime - time) / 1000;
                uploadStayTime(diff);
            }
            isChangingConfiguration = activity.isChangingConfigurations();
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {

        }
    }

    private void uploadStayTime(long diff) {
        TCUserMgr.getInstance().uploadLogs(TCConstants.ELK_ACTION_STAY_TIME, TCUserMgr.getInstance().getUserId(), diff, "", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });
    }
}
