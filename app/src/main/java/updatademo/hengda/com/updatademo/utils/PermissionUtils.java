package updatademo.hengda.com.updatademo.utils;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v4.app.Fragment;

import com.yanzhenjie.alertdialog.AlertDialog;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RationaleListener;

/**
 * Created by Administrator on 2017/3/17.
 */

public class PermissionUtils {
    private static final int PERMISSION_REQUEST_CODE = 100;

    /**
     *
     * @param  context  可传入activity或fragment
     * @param permissionName 提示用户语言
     * @param permisson 所需权限可以是多个
     */
    public static void setPermisson(final Object context , final String permissionName, String...permisson){
        final Activity activity = getContext(context);
        AndPermission.with(activity).requestCode(PERMISSION_REQUEST_CODE)
                .permission(permisson)
                .rationale(new RationaleListener() {
                    @Override
                    public void showRequestPermissionRationale(int requestCode, final Rationale rationale) {
                        AlertDialog.build(activity)
                                .setTitle("友好提醒")
                                .setMessage("您已拒绝过"+permissionName+"权限,将无法使用该功能,请允许打开"+permissionName+"权限")
                                .setPositiveButton("允许", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        rationale.resume();
                                    }
                                })
                                .setNegativeButton("禁止", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        rationale.cancel();
                                    }
                                }).show();
                    }
                })
                .send();

    }
    static Activity getContext(Object o) {
        if (o instanceof Activity)
            return (Activity) o;
        else if (o instanceof Fragment)
            return ((Fragment) o).getActivity();
        else if (o instanceof android.app.Fragment)
            return ((android.app.Fragment) o).getActivity();
        throw new IllegalArgumentException("The " + o.getClass().getName() + " is not support.");
    }
}
