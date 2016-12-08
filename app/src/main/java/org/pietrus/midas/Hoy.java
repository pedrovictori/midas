package org.pietrus.midas;

import android.content.Context;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by pedro on 1/08/15.
 */
public class Hoy {
    public static SimpleDateFormat formatoFecha = new SimpleDateFormat("EEEE, dd/MMM/yy"); //el formato de nuestras fechas
    public static SimpleDateFormat diaSemanaFormat  = new SimpleDateFormat("EEEE");

    public Hoy (){} //constructor vacío, esta clase es static-like


    //El siguiente método está sobrecargado para que se pueda pasar una string fecha formateada o un Date como parámetro
    public static String procesar (Date date,Context context){

        Calendar hoyCal = Calendar.getInstance(); // hoy
        Calendar dateCal = Calendar.getInstance(); //mi fecha
        dateCal.setTime(date);
        //si ambas fechas son del mismo año
        if (hoyCal.get(Calendar.YEAR) == dateCal.get(Calendar.YEAR)) {
            int hoy = hoyCal.get(Calendar.DAY_OF_YEAR);
            int tomorrow = hoy + 1;
            int ayer = hoy - 1;
            int fecha = dateCal.get(Calendar.DAY_OF_YEAR);
            String diaSemana = diaSemanaFormat.format(date);

            //necesitamos context para acceder a los strings en R
            if (fecha == hoy) return context.getString(R.string.hoy) + ", " + diaSemana;
            else if (fecha == tomorrow) return context.getString(R.string.tomorrow) + ", " + diaSemana;
            else if (fecha == ayer) return context.getString(R.string.ayer) + ", " + diaSemana;
            else return formatoFecha.format(date);
        }
        else return formatoFecha.format(date);
    }

    public static String procesar (String fechaSt,Context context) throws ParseException {
        Date date = formatoFecha.parse(fechaSt);
        return procesar (date,context);
    }

    //devuelve lista con los nombres acortados de los días de la semana
    public static String[] diasDeLaSemana(){
        String[] bruto = diaSemanaFormat.getDateFormatSymbols().getShortWeekdays();
        String[] result = new String[7];

        //empezamos la lista por el lunes
        for(int i = 0;i<6;i++){
            String esteItem = bruto[i+2]; //sumamos 2 porque el primer item (pos 0) de bruto no es un día, es info no necesaria
            esteItem = esteItem.replace(".",""); //quitamos el punto final
            result[i] = esteItem;
        }
        result[6] = bruto[1].replace(".", ""); //ponemos el domingo en la última posición

        return result;
    }
}