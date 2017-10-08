package yassour.yassom;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NetWatcher extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //Log.d("NetWatcher", "-----------------------------------");
        context.startService(new Intent(context, MainService.class));
    }
}