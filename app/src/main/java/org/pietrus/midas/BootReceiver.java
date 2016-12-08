package org.pietrus.midas;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

//esta clase es para recibir el anuncio de que el dispositivo ha terminado de iniciarse
//y por tanto hay que reiniciar alarmas, si procediese
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Alarma evento = new Alarma();
            evento.setAlarm(context);

        }
    }
}
