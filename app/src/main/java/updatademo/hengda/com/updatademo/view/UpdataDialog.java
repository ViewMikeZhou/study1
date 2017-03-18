package updatademo.hengda.com.updatademo.view;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import updatademo.hengda.com.updatademo.R;
import updatademo.hengda.com.updatademo.service.DownloadService;
import updatademo.hengda.com.updatademo.utils.NetWorkUtils;
import updatademo.hengda.com.updatademo.utils.PermissionUtils;

/**
 * Created by Administrator on 2017/3/7.
 */

public class UpdataDialog extends Dialog {
    public static final String TAG = "UpdataDialog";
    private final Activity context;

    //  @Bind(R.id.updateTitle)
    TextView mUpdateTitle;
    // @Bind(R.id.updateTime)
    TextView mUpdateTime;
    // @Bind(R.id.updateVersion)
    TextView mUpdateVersion;
    //@Bind(R.id.updateSize)
    TextView mUpdateSize;
    //@Bind(R.id.updateDesc)
    TextView mUpdateDesc;
    //@Bind(R.id.updateDescLayout)
    LinearLayout mUpdateDescLayout;
    //@Bind(R.id.updateNetworkState)
    TextView mUpdateNetworkState;
    //@Bind(R.id.noUpdate)
    Button mNoUpdate;
    //@Bind(R.id.update)
    Button mUpdate;
    //@Bind(R.id.forceUpdateProgress)
    NumberProgressBar mForceUpdateProgress;
    private View mView;
    private String mTitle;
    private String mAppTime;
    private String mVersionName;
    private String mAppDesc;
    private String mFileName;
    private String mAppName;
    private int mAppSize;
    private String mDownloadUrl;
    private String mFilePath;
    private long timeRange;
    //通知栏图标
    private int mIconResId;
    private boolean isShowProgress = false;


    public UpdataDialog(@NonNull Activity context) {
        super(context);
        setDialogTheme();
        setCanceledOnTouchOutside(false);
        this.context = context;
    }

    /**
     * 设置对话框主题
     */
    private void setDialogTheme() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);// android:windowNoTitle
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);// android:backgroundDimEnabled默认是true的
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));// android:windowBackground
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mView = LayoutInflater.from(context).inflate(R.layout.update_dialog_layout, null);
        setContentView(mView);
        initView(mView);
        initData(); //初始化数据
        initEvent();// 初始化点击时间

    }

    private void initView(View view) {
        mUpdateTitle = (TextView) view.findViewById(R.id.updateTitle);
        mUpdateTime = (TextView) view.findViewById(R.id.updateTime);
        mUpdateVersion = (TextView) view.findViewById(R.id.updateVersion);
        mUpdateSize = (TextView) view.findViewById(R.id.updateSize);
        mUpdateDesc = (TextView) view.findViewById(R.id.updateDesc);
        mUpdateDescLayout = (LinearLayout) view.findViewById(R.id.updateDescLayout);
        mUpdateNetworkState = (TextView) view.findViewById(R.id.updateNetworkState);
        mNoUpdate = (Button) view.findViewById(R.id.noUpdate);
        mUpdate = (Button) view.findViewById(R.id.update);
        mForceUpdateProgress = (NumberProgressBar) view.findViewById(R.id.forceUpdateProgress);
    }

    private void initEvent() {
        mNoUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /* ((Activity) context).finish();
                System.exit(0);*/

                dismiss();
            }
        });
        mUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO 需要6.0 读写内存卡权限
                PermissionUtils.setPermisson(context, "读写内存卡", Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE);
                download();
            }

        });
    }
    /**
     * 后台下载
     */
    public void download() {
        //防抖动,两次点击间隔小于1s都return;
        if (System.currentTimeMillis() - timeRange < 1000) {
            return;
        }
        timeRange = System.currentTimeMillis();
        setNetWorkState();
        if (!NetWorkUtils.hasNetConnection(context)) {
            Toast.makeText(context, "当前无网络连接", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra("downloadUrl", mDownloadUrl);
        intent.putExtra("filePath", mFilePath);
        intent.putExtra("fileName", mFileName);
        intent.putExtra("iconResId", mIconResId);
        intent.putExtra("isShowProgress", isShowProgress);
        intent.putExtra("appName", mAppName);
        context.startService(intent);
        dismiss();
        Toast.makeText(context, "正在后台为您下载...", Toast.LENGTH_SHORT).show();
    }
    /**
     * 前台下载
     */
    private void forcedown() {
        if (mDownloadUrl == null || mDownloadUrl.length() == 0) {
            //没给地址
            dismiss();
            return;
        }
        //防抖动,两次点击间隔小于500ms都return;
        if (System.currentTimeMillis() - timeRange < 500) {
            return;
        }
        timeRange = System.currentTimeMillis();
        setNetWorkState();
        if ("点击安装".equals(mUpdate.getText().toString().trim())) {
           installAPK(mDownloadUrl);
            return;
        }
        // 开始下载
        Log.e(TAG, "----------------" + mDownloadUrl);
        /*OkHttpClientManager.getInstance().download(mDownloadUrl, mFilePath, mFileName, new OkHttpClientManager.OnDownloadListener() {
            @Override
            public void onDownloadSuccess() {
                mForceUpdateProgress.setVisibility(View.GONE);
                mUpdate.setEnabled(true);
                mUpdate.setText("点击安装");
                CustomToast.showToast("下载完成请安装");
            }

            @Override
            public void onDownloading(long progress, long total) {
                Log.d("UpdataDialog", "progress:"+progress +"total"+total);
                mUpdate.setEnabled(false);
                mUpdate.setText("正在下载");
                mForceUpdateProgress.setVisibility(View.VISIBLE);
                mForceUpdateProgress.setProgress((int) progress);
                mForceUpdateProgress.setMax(5815424);
            }

            @Override
            public void onDownloadFailed() {
                mUpdate.setEnabled(true);
                mUpdate.setText("重新下载");
                CustomToast.showToast("下载失败");
                // dismiss();
            }
        });*/

       /* OkHttpClientManager.getDownloadDelegate().downloadAsyn(mDownloadUrl, mFilePath, new OkHttpClientManager.ResultCallback<String>() {
            @Override
            public void onError(Request request, Exception e) {
                Log.d(TAG, "onError: e == " + e.toString());
                mUpdate.setEnabled(true);
                mUpdate.setText("重新下载");
                CustomToast.showToast("下载失败");
                if (!isShowing()){
                    show();
                }
                // dismiss();
            }

            @Override
            public void onResponse(String response) {
                mUpdate.setText("点击安装");
                Log.e(TAG,response);
                installAPK(response);
            }
        }, new UIProgressListener() {
            @Override
            public void onUIProgress(long currentBytes, long contentLength, boolean done) {
                mUpdate.setEnabled(false);
                mUpdate.setText("正在下载");
                mForceUpdateProgress.setVisibility(View.VISIBLE);
                mForceUpdateProgress.setProgress((int) currentBytes);
                mForceUpdateProgress.setMax(6000000);
            }
        },TAG);*/
    }

    /**
     * 根据文件路径安装apk
     * @param response apk路径
     */
    private void installAPK(String response) {
        File file = new File(response);
        if (file.exists()) {
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(Intent.ACTION_VIEW);
            Uri uri = null;
            if (Build.VERSION.SDK_INT >= 24) {
                //7.0安装
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                uri = FileProvider.getUriForFile(context, "com.headerits.operating.fileprovider", file);
            } else {
                uri = Uri.fromFile(file);
            }
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
            context.startActivity(intent);
            dismiss();
        }
    }

    //标题 , 发布时间 , 版本名 ,跟新日志
    private void initData() {
        //标题
        if (TextUtils.isEmpty(mTitle)) {
            mUpdateTitle.setVisibility(View.GONE);
        } else {
            mUpdateTitle.setText(mTitle);
        }
        //发布时间
        if (TextUtils.isEmpty(mAppTime)) {
            mUpdateTime.setVisibility(View.GONE);
        } else {
            mUpdateTime.setText("更新时间:" + mAppTime);
        }
        //新版版本名,eg:2.2.1
        if (TextUtils.isEmpty(mVersionName)) {
            mUpdateVersion.setVisibility(View.GONE);
        } else {
            mUpdateVersion.setText("版本:" + mVersionName);
        }
        //新版本app大小
        if (mAppSize == 0) {
            mUpdateSize.setVisibility(View.GONE);
        } else {
            mUpdateSize.setText("大小:" + mAppSize + "M");
        }
        //更新日志
        if (TextUtils.isEmpty(mAppDesc)) {
            mUpdateDescLayout.setVisibility(View.GONE);
        } else {
            mUpdateDesc.setText(mAppDesc);
        }
        setNetWorkState();
    }

    /**
     * 设置网络状态
     */
    private void setNetWorkState() {
        if (NetWorkUtils.isNetworkConnected(context)) {
            mUpdateNetworkState.setText("当前为WiFi网络环境,可放心下载.");
            mUpdateNetworkState.setTextColor(Color.parseColor("#629755"));
        } else if (NetWorkUtils.isMobileConnected(context)) {
            mUpdateNetworkState.setText("当前为移动网络环境,下载将会消耗流量!");
            mUpdateNetworkState.setTextColor(Color.parseColor("#BAA029"));
        } else if (!NetWorkUtils.hasNetConnection(context)) {
            mUpdateNetworkState.setText("当前无网络连接,请打开网络后重试!");
            mUpdateNetworkState.setTextColor(Color.RED);
        } else {
            mUpdateNetworkState.setVisibility(View.GONE);
        }
    }

    /**
     * 设置标题
     *
     * @param title
     * @return
     */
    public UpdataDialog setTitle(String title) {
        this.mTitle = title;
        return this;
    }

    /**
     * 设置发布时间
     *
     * @param releaseTime
     * @return
     */
    public UpdataDialog setReleaseTime(String releaseTime) {
        this.mAppTime = releaseTime;
        return this;
    }

    /**
     * 设置 版本名
     *
     * @param versionName
     * @return
     */
    public UpdataDialog setVersionName(String versionName) {
        this.mVersionName = versionName;
        return this;
    }

    /**
     * 跟新详情
     *
     * @param updateDesc
     * @return
     */
    public UpdataDialog setUpdateDesc(String updateDesc) {
        this.mAppDesc = updateDesc;
        return this;
    }

    /**
     * 设置下载文件名
     *
     * @param fileName
     * @return
     */
    public UpdataDialog setFileName(String fileName) {
        this.mFileName = fileName;
        return this;
    }

    /**
     * 设置app名
     *
     * @param appName
     * @return
     */
    public UpdataDialog setAppName(String appName) {
        this.mAppName = appName;
        return this;
    }

    /**
     * 设置download下载地址
     *
     * @param downloadUrl
     * @return
     */
    public UpdataDialog setDownloadUrl(String downloadUrl) {
        this.mDownloadUrl = downloadUrl;
        return this;
    }

    /**
     * 设置文件存储的位置
     *
     * @param filePath
     * @return
     */
    public UpdataDialog setFilePath(String filePath) {
        this.mFilePath = filePath;
        return this;
    }

    /**
     * 设置图标资源id,该图标用来显示在通知栏中
     */
    public UpdataDialog setIconResId(int iconResId) {
        this.mIconResId = iconResId;
        return this;
    }
    /**
     * 是否在通知栏显示下载进度(true:显示,false:不显示,默认不显示)
     */
    public UpdataDialog setShowProgress(boolean isShowProgress) {
        this.isShowProgress = isShowProgress;
        return this;
    }


}
