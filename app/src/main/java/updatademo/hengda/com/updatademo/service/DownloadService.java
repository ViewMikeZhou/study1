package updatademo.hengda.com.updatademo.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;

import updatademo.hengda.com.updatademo.R;
import updatademo.hengda.com.updatademo.utils.NotificationUtils;
import updatademo.hengda.com.updatademo.utils.OkHttpClientManager;

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
        Log.d("DownloadService", "go.here");
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
    private void download(String downloadUrl, final String filePath, final String fileName, boolean aotuInstall) {
        OkHttpClientManager.getInstance().download(downloadUrl, filePath, fileName, new OkHttpClientManager.OnDownloadListener() {
            @Override
            public void onDownloadSuccess() {
             File file = new File(filePath,fileName);
                NotificationUtils.showDownloadSuccessNotification(DownloadService.this,file,iconResId,appName,"下载成功请点击安装",false);
                Log.d("DownloadService", "onDownloadSuccess--0go.here");
            }

            @Override
            public void onDownloading(long progress, long total) {
                NotificationUtils.showDownloadingNotification(DownloadService.this,(int)progress,6000000,iconResId,appName,false);
                Log.d("DownloadService", "onDownloading--go.here"+progress);
            }

            @Override
            public void onDownloadFailed() {
                NotificationUtils.showDownloadFailureNotification(DownloadService.this,mIntent,iconResId,appName,"下载失败,点击重新下载",true);
                Log.d("DownloadService", "onDownloadFailed--go.here");
            }
        });
    }
}
