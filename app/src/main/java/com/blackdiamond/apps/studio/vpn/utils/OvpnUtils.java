package com.blackdiamond.apps.studio.vpn.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import androidx.annotation.NonNull;

import com.blackdiamond.apps.studio.vpn.model.Server;

import java.io.File;
import java.io.FileOutputStream;

public class OvpnUtils {

    private static final String FILE_EXTENSION = ".ovpn";
    private static final String OPENVPN_PKG_NAME = "net.openvpn.openvpn";
    private static final String OPENVPN_MIME_TYPE = "application/x-openvpn-profile";

    public static String humanReadableCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        char pre = "KMGTPE".charAt(exp-1);
        return String.format("%.2f %s" + (si ? "bps" : "B"),
                bytes / Math.pow(unit, exp), pre);
    }


    private static void saveConfigData(@NonNull Context context, @NonNull Server server) {
        File file;
        FileOutputStream outputStream;

        try {
            file = getFile(context, server);
            outputStream = new FileOutputStream(file);
            outputStream.write(server.ovpnConfigData.getBytes("UTF-8"));
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static File getFile(@NonNull Context context, @NonNull Server server) {
        File filePath;
        if (!Environment.isExternalStorageRemovable() || isExternalStorageWritable()) {
            filePath = context.getExternalCacheDir();
        } else {
            filePath = context.getCacheDir();
        }
        return new File(filePath, server.countryShort + "_" + server.hostName + "_" +
                server.protocol.toUpperCase() + FILE_EXTENSION);
    }

    private static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public static int getDrawableResource(@NonNull Context context, @NonNull String resource) {
        return context.getResources()
                .getIdentifier(resource, "drawable", context.getPackageName());
    }


    public static void shareOvpnFile(@NonNull Activity activity, @NonNull Server server) {
        File file = getFile(activity, server);
        if (!file.exists()) {
            saveConfigData(activity, server);
        }

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(getFile(activity, server)));
        activity.startActivity(Intent.createChooser(intent, "Share Profile using"));
    }
}
