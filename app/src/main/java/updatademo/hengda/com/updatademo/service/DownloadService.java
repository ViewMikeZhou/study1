package updatademo.hengda.com.updatademo.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import updatademo.hengda.com.updatademo.R;

/**
 * Created by Administrator on 2017/3/18.
 */

public class DownloadService extends Service{
    private int iconResId;
    private String appName;
    private Intent mIntent;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (null == intent) {
            return START_NOT_STICKY;
        }
        mIntent = intent;
        appName = intent.getStringExtra("appName");
        iconResId = intent.getIntExtra("iconResId", -1);
        if (iconResId == -1) {
            iconResId = R.drawable.ic_home_black_24dp;
        }
        download(intent.getStringExtra("downloadUrl"), intent.getStringExtra("filePath"), intent.getStringExtra("fileName"), true);
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     *
     * @param downloadUrl  下载地址
     * @param filePath    安装路径
     * @param fileName    安装名
     * @param aotuInstall  是否自动安装
     */
    private void download(String downloadUrl, String filePath, String fileName, boolean aotuInstall) {

    }
}
