package com.desmond.gptwake;

import android.app.KeyguardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

public final class GptLauncher {

    private static final String GPT_PACKAGE = "com.openai.chatgpt";

    private static final ComponentName GPT_VOICE_COMPONENT = new ComponentName(
            GPT_PACKAGE, "com.openai.voice.assistant.AssistantActivity");

    /**
     * A launch made from an application/FGS context can be blocked while the device is locked.
     * In that case WakeController first brings up our transparent, show-when-locked trampoline.
     */
    public static boolean isKeyguardLocked(Context context) {
        try {
            KeyguardManager keyguard = context.getSystemService(KeyguardManager.class);
            boolean locked = keyguard != null && keyguard.isKeyguardLocked();
            L.i("KEYGUARD_LOCKED=" + locked);
            return locked;
        } catch (Throwable t) {
            L.e("KEYGUARD_STATE_FAIL", t);
            return false;
        }
    }

    public static boolean launchViaShim(Context context) {
        try {
            context.startActivity(new Intent(context, ShimActivity.class)
                    .putExtra(ShimActivity.EXTRA_ACTION, "launch")
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            L.i("SHIM_LAUNCH_REQUESTED");
            return true;
        } catch (Throwable t) {
            L.e("SHIM_LAUNCH_REQUEST_FAIL", t);
            return false;
        }
    }

    public static boolean launchDirect(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            ActivityInfo info = pm.getActivityInfo(
                    GPT_VOICE_COMPONENT, 0);
            if (!info.exported) throw new IllegalStateException("AssistantActivity not exported");
            if (info.permission != null) {
                throw new IllegalStateException("AssistantActivity needs " + info.permission);
            }
            context.startActivity(new Intent()
                    .setComponent(GPT_VOICE_COMPONENT)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            L.i("DIRECT_LAUNCH_OK");
            return true;
        } catch (Throwable t) {
            L.e("DIRECT_LAUNCH_FAIL", t);
            return false;
        }
    }

    public static boolean launchDeeplink(Context context) {
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://chat.com/?mode=voice"))
                    .setPackage(GPT_PACKAGE)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            L.i("DEEPLINK_LAUNCH_OK");
            return true;
        } catch (Throwable t) {
            L.e("DEEPLINK_LAUNCH_FAIL", t);
            return false;
        }
    }

    public static boolean launch(Context context) {
        return launchDirect(context) || launchDeeplink(context);
    }
}
