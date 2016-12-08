package org.pietrus.midas;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;

/**
 * Created by pedro on 20/08/15.
 */
//TODO: testear todo esto
public class RecurCheck {
    public static final String TIPO_CAMBIO = "tipoCambio";
    public static final String CAMBIO = "cambio";
    private static Context CONTEXT;

    public RecurCheck(){} //constructor vacío, esta clase es static-like

    public static void actualizarTransaccionesRecurrentes(Context context){
        //guardamos context en una variable global
        CONTEXT = context;

        //avisamos
        Log.i("recurCheck", "iniciando RecurCheck");

        //comprobamos si hay transacciones recurrentes en la base de datos
        DBtransacciones database = new DBtransacciones(context);
        ArrayList<HashMap<String, Object>> lista = database.listaTransaccionesRecurrentes();
        if (!lista.isEmpty()) { //if lista is NOT empty

            //declaramos los modos de recurrencia
            String diario = RecurDialogActivity.DIARIO;
            String semanal = RecurDialogActivity.SEMANAL;
            String mensual = RecurDialogActivity.MENSUAL;
            String anual = RecurDialogActivity.ANUAL;

            //recorremos la lista transacción a transacción
            int largo = lista.size();
            /*esta arrayList almacena las idGenerica de las transacciones que ya han sido procesadas.La idGenerica es común a todas
            las instancias de una misma transacción recurrente, por lo que tras revisar la última instancia de una transacción, no
            se revisará ninguna otra instancia*/
            ArrayList<Integer> transaccYaProcesadas = new ArrayList<Integer>();

            for (int i = 0; i < largo; i++) {
                HashMap<String, Object> estaTransacc = lista.get(i);
                int idGenerica = (Integer) estaTransacc.get(DatabasesMidas.idGenericaN);

                if (!transaccYaProcesadas.contains(idGenerica)) { //if transaccYaProcesadas NO contiene idGenerica

                    transaccYaProcesadas.add(idGenerica);
                    long fechaTransacc = (long) estaTransacc.get(DatabasesMidas.fechaN);
                    String recurParams[] = estaTransacc.get(DatabasesMidas.modoRecurN).toString().split(",");
                    //Cogemos los parámetros de esta transacción.
                    //formato: modo,cada,lun/mar/mie

                    String modo = recurParams[0];
                    int cada = Integer.parseInt(recurParams[1]);

                    final Calendar cHoy = Calendar.getInstance();
                    Calendar cTransacc = Calendar.getInstance();
                    cTransacc.setTimeInMillis(fechaTransacc);

                    //if día de hoy no es igual al día de la transacción
                    int diaYhoy = cHoy.get(Calendar.DAY_OF_YEAR);
                    int diaYtransacc = cTransacc.get(Calendar.DAY_OF_YEAR);
                    if (diaYhoy != diaYtransacc) {

                        //DIARIO
                        if (modo.equals(diario)) { //funciona
                            int diferencia = diaYhoy - diaYtransacc;
                            int ciclos = diferencia / cada; //este es el número de transacciones que habrá que crear, si >=1

                            if (diferencia != 0) { //si no estamos en el mismo día
                                for (i = 1; i <= ciclos; i++) {
                                    int suma = i * cada;
                                    guardarTransaccRecur(estaTransacc,cTransacc,Calendar.DAY_OF_YEAR,diaYtransacc + suma);
                                }
                            }
                        }

                        //SEMANAL
                        else if (modo.equals(semanal)) {
                            ArrayList<Integer> diasSemana =
                                    new ArrayList<>(Arrays.asList(stringToIntegerArray(recurParams[1].split("/"))));

                            int semYhoy = cHoy.get(Calendar.WEEK_OF_YEAR);
                            int semYtransacc = cTransacc.get(Calendar.WEEK_OF_YEAR);

                            int diaSemHoy = cHoy.get(Calendar.DAY_OF_WEEK);

                            int diferencia = semYhoy - semYtransacc;

                            if (diferencia == 0 && diasSemana.size() > 1) {/* si estamos en la misma semana que la de la última
                             transacción, no hace falta mirar 'cada' porque es obvio que en esta semana
                             tocan transacciones, si ocurren varios días a la semana y si quedan transacciones por guardar esta semana*/
                                int diaSemTransacc = cTransacc.get(Calendar.DAY_OF_WEEK);

                                int diaSemTransaccEnLista = diasSemana.indexOf(diaSemTransacc);
                                diasSemana = (ArrayList<Integer>) diasSemana.subList(diaSemTransaccEnLista + 1, diasSemana.size() - 1);

                                if (!diasSemana.isEmpty()) { //si no está vacía quedan transacciones por hacer esta semana
                                    for (Integer item : diasSemana) {
                                        //si el día de la recurrencia no está en el futuro
                                        if (item <= diaSemHoy)
                                            guardarTransaccRecur(estaTransacc, cTransacc, Calendar.DAY_OF_WEEK, item);
                                        else
                                            break; //si hemos llegado a un día en el futuro, paramos el bucle
                                    }
                                }
                            }

                            else { //no estamos en la misma semana
                                int ciclos = diferencia / cada; //semanas que hay que crear
                                for (i = 1;i<=ciclos; i++){
                                    int semana = semYtransacc + (i * cada);
                                    HashMap<String, Integer> cambioSem = new HashMap<String, Integer>();
                                    cambioSem.put(TIPO_CAMBIO, Calendar.WEEK_OF_YEAR);
                                    cambioSem.put(CAMBIO, semana);

                                    for (Integer item : diasSemana) {
                                        if (item <= diaSemHoy) { //si el día de la recurrencia no está en el futuro
                                            HashMap<String, Integer> cambioDia = new HashMap<String, Integer>();
                                            cambioDia.put(TIPO_CAMBIO, Calendar.DAY_OF_WEEK);
                                            cambioDia.put(CAMBIO, item);

                                            HashMap<String, Integer>[] cambios = new HashMap[2];
                                            cambios[0] = cambioSem;
                                            cambios[1] = cambioDia;
                                            guardarTransaccRecur(estaTransacc, cTransacc, cambios);
                                        } else break; //si hemos llegado a un día en el futuro, paramos el bucle
                                    }
                                }
                            }

                        }

                        //MENSUAL
                        else if (modo.equals(mensual)) {
                            int mesHoy = cHoy.get(Calendar.MONTH);
                            int mesTransacc = cTransacc.get(Calendar.MONTH);

                            if(mesHoy != mesTransacc){ //estamos en meses distintos
                                int diferencia = mesHoy - mesTransacc;
                                int ciclos = diferencia / cada; //este es el número de transacciones que habrá que crear, si >=1;
                                for (i = 1; i <= ciclos; i++) {
                                    int suma = i * cada;
                                    guardarTransaccRecur(estaTransacc,cTransacc,Calendar.MONTH,mesTransacc + suma);
                                }
                            }
                        }

                        //ANUAL
                        else if (modo.equals(anual)) {
                            int yearHoy = cHoy.get(Calendar.YEAR);
                            int yearTransacc = cTransacc.get(Calendar.YEAR);

                            if(yearHoy != yearTransacc){ //estamos en años distintos
                                //vamos a asumir que ha pasado menos de dos años, que el dispositivo se ha encendido alguna vez en el último año
                                int diferencia = yearHoy - yearTransacc;

                                if(diferencia >= cada) guardarTransaccRecur(estaTransacc,cTransacc,Calendar.YEAR,yearTransacc + cada);
                            }
                        }
                    }
                }
            }
        }
    }

    //sobrecarga de método para permitir meter cambios simples sin tener que usar hashmap
    private static void guardarTransaccRecur(HashMap<String,Object> transacc, Calendar c, int tipoCambio, int cambio) {
        HashMap<String, Integer> cambioHM = new HashMap<String, Integer>();
        cambioHM.put(TIPO_CAMBIO, tipoCambio);
        cambioHM.put(CAMBIO,cambio);
        HashMap<String,Integer>[] result = new HashMap[1];
        result[0] = cambioHM;
        guardarTransaccRecur(transacc, c, result);
    }

    @SuppressWarnings("ResourceType")
    private static void guardarTransaccRecur(HashMap<String,Object> transacc, Calendar c, HashMap<String,Integer>[] cambios){

        String concepto = transacc.get(DatabasesMidas.conceptoN).toString();
        int categoriaID = (int) transacc.get(DatabasesMidas.catIdN);
        float importe = (float)transacc.get(DatabasesMidas.importeN);
        boolean gasto = (boolean) transacc.get(DatabasesMidas.gastoN);
        String modoRecur = transacc.get(DatabasesMidas.modoRecurN).toString();
        int idGenerica = (int) transacc.get(DatabasesMidas.idGenericaN);

        int listSize = cambios.length;
        for(int i = 0;i<listSize;i++){
            HashMap<String,Integer> esteMap = cambios[i];
            c.set(esteMap.get(TIPO_CAMBIO),esteMap.get(CAMBIO));
        }

        long fecha = c.getTimeInMillis();
        DBtransacciones database = new DBtransacciones(CONTEXT);

        database.guardarTransaccion(concepto, categoriaID, importe, gasto, fecha, true, modoRecur, idGenerica);
    }

    private static Integer[] stringToIntegerArray(String[] stringArray){
        int size = stringArray.length;
        Integer result[] = new Integer[size];

        for(int i = 0; i<size;i++){
            result[i] = Integer.parseInt(stringArray[i]);
        }
        return result;
    }
}

