package org.pietrus.midas;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

//esta clase es para comprobar una vez al día si hay transacciones recurrentes que crear
public class Alarma extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, ""); //TODO investigar qué hace esto y ver si es necesario
        wl.acquire();

        // Actualizamos las transacciones recurrentes mediante la clase 'estática' RecurCheck
        RecurCheck.actualizarTransaccionesRecurrentes(context);

        wl.release();
    }
    //ALARMAS
    public void setAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, Alarma.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() +1000*60
                ,AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent); // Millisec * Second * Minute

        //configuramos el BootReceiver para que reinicie la alarma si se reinicia el dispositivo
        ComponentName bootReceiver = new ComponentName(context, BootReceiver.class);
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(bootReceiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
        Log.i("RecurAlarm", "alarma creada");
    }

    public void cancelAlarm(Context context) {
        Intent intent = new Intent(context, Alarma.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);

        //cancelamos también el BootReceiver
        ComponentName bootReceiver = new ComponentName(context, BootReceiver.class);
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(bootReceiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);

        Log.i("RecurAlarm", "alarma cancelada");
    }
}
