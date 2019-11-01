package com.sharry.lib.album;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 图片查看器的管理类
 *
 * @author Sharry <a href="sharrychoochn@gmail.com">Contact me.</a>
 * @version 1.0
 * @since 4/28/2019 4:34 PM
 */
public class WatcherManager {

    public static final String TAG = WatcherManager.class.getSimpleName();
    private static String[] sPermissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    public static WatcherManager with(@NonNull Context context) {
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            return new WatcherManager(activity);
        } else {
            throw new IllegalArgumentException("WatcherManager.with -> Context can not cast to Activity");
        }
    }

    private Activity mBind;
    private WatcherConfig mConfig;
    private View mTransitionView;

    private WatcherManager(Activity activity) {
        this.mBind = activity;
    }

    /**
     * 设置共享元素
     */
    public WatcherManager setSharedElement(@NonNull View transitionView) {
        mTransitionView = Preconditions.checkNotNull(transitionView, "Please ensure View not null!");
        return this;
    }

    /**
     * 设置图片预览的配置
     */
    public WatcherManager setConfig(@NonNull WatcherConfig config) {
        this.mConfig = Preconditions.checkNotNull(config, "Please ensure WatcherConfig not null!");
        return this;
    }

    /**
     * 设置图片加载方案
     */
    public WatcherManager setLoaderEngine(@NonNull ILoaderEngine loader) {
        Loader.setLoaderEngine(loader);
        return this;
    }

    /**
     * 调用图片查看器的方法
     */
    public void start() {
        startForResult(null);
    }

    /**
     * 调用图片查看器, 一般用于相册
     */
    public void startForResult(@Nullable final WatcherCallback callback) {
        Preconditions.checkNotNull(callback, "Please ensure WatcherCall not null!");
        Preconditions.checkNotNull(mConfig, "Please ensure U set WatcherConfig correct.");
        PermissionsHelper.with(mBind)
                .request(sPermissions)
                .execute(new PermissionsCallback() {
                    @Override
                    public void onResult(boolean granted) {
                        if (granted) {
                            startForResultActual(callback);
                        }
                    }
                });
    }

    /**
     * 真正执行 Activity 的启动
     */
    private void startForResultActual(final WatcherCallback callback) {
        CallbackFragment callbackFragment = CallbackFragment.getInstance(mBind);
        if (callbackFragment == null) {
            Log.e(TAG, "launch picture watcher failed.");
            return;
        }
        callbackFragment.setCallback(new CallbackFragment.Callback() {
            @Override
            public void onActivityResult(int requestCode, int resultCode, Intent data) {
                if (resultCode != Activity.RESULT_OK || null == data || null == callback) {
                    return;
                }
                switch (requestCode) {
                    case WatcherActivity.REQUEST_CODE:
                        callback.onWatcherPickedComplete(
                                data.getBooleanExtra(WatcherActivity.RESULT_EXTRA_IS_PICKED_ENSURE, false)
                        );
                        break;
                    default:
                        break;
                }
            }
        });
        WatcherActivity.launchActivityForResult(mBind, callbackFragment, mConfig, mTransitionView);
    }

}
