/**
 * Created by pedro on 27/07/15.
 */

package org.pietrus.midas;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
//TODO actualizar saldo de todas las transacciones cuando la que se guarda no es la última en el tiempo
public class DBtransacciones extends SQLiteOpenHelper implements DatabasesMidas{

    private static final String DB_NAME = "transacciones";
    private static final int DB_VERSION = 1;
    private final String TABLE_NAME = DB_NAME;
    private final String seleccionRecur = "recur IS 'true'";
    private final String dondeID = "_id IS ";
    private Context context;

//Sentencia SQL para crear la tabla de transacciones
    String sqlCreate = "CREATE TABLE "+TABLE_NAME+
        " (_id INTEGER PRIMARY KEY AUTOINCREMENT, concepto TEXT, "+
        "categoriaID INTEGER, importe FLOAT, gasto TEXT, fecha INTEGER, saldo FLOAT, recur TEXT, modoRecur TEXT, idGenerica INTEGER)";


    //Métodos de SQLiteOpenHelper
    //Constructor
    public DBtransacciones (Context context){
        //invocamos al método de la clase padre, atributos: contexto, nombre BD, cursor, versión
        super(context, DB_NAME, null, DB_VERSION);

        //guardamos context en una variable global
        this.context = context;
    }

    //Se ejecuta cuando la tabla aún no existe (primer uso)
    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL(sqlCreate);
    }

    @Override
    public void onUpgrade (SQLiteDatabase db, int oldV, int newV){
        //TODO: implementar algo aquí
    }

    //Métodos propios
    public boolean isEmpty(){
        SQLiteDatabase db = getReadableDatabase();
        //hacemos una consulta a la base de datos que devuelva sólo la última fila
        Cursor ultFila = db.query(TABLE_NAME,null,null,null,null,null,"_id DESC","1");

        //comprobamos que la fila exista (es decir, que la tabla no estuviera completamente vacía)
        boolean empty;
        if(ultFila.moveToFirst()) empty = false;
        else empty = true;

        return empty;
    }
    //Sobrecargamos guardarTransacción para elegir si meter idGenerica manualmente o automáticamente (ver RecurCheck)

    //Esta versión del método introduce una idGenerica nula
    public void guardarTransaccion
    (String concepto, int categoriaID, float importe, boolean gasto, long fecha, boolean recur, String modoRecur) {

        guardarTransaccion(concepto,categoriaID,importe,gasto,fecha,recur,modoRecur,0);
    }

    //esta versión introduce una idGenerica concreta
    public void guardarTransaccion
    (String concepto, int categoriaID, float importe, boolean gasto, long fecha, boolean recur, String modoRecur,int idGenerica) {
        SQLiteDatabase db = getWritableDatabase();

        /* cuando se añade una transacción recurrente por primera vez, activamos la alarma para que se compruebe periódicamente
            si hay que crear una nueva instancia de dicha transacción recurrente. Además, definimos la idGenerica*/
        if(recur&&idGenerica==0){ //TODO: esto es un poco espaghetti, arreglarlo
            ArrayList<HashMap<String,Object>> lista = listaTransaccionesRecurrentes();
            if(lista.isEmpty()){
                idGenerica = 100;
                Alarma alarma = new Alarma();
                alarma.setAlarm(context);
            }
            else{
                HashMap<String,Object> ultTransaccRecur = lista.get(lista.size()-1);
                idGenerica = (Integer) ultTransaccRecur.get(idGenericaN)+1;
            }
        }

        /*Tenemos que guardar el saldo en ese momento, el cual hay que calcular ahora a partir del importe de la transacción
        y del saldo en el momento de la transacción anterior*/
        float saldoAnterior;
        float saldoActual;

       //hacemos una consulta a la base de datos que devuelva sólo la última fila
        String[] columnas = {"saldo"};
        Cursor ultFila = db.query(TABLE_NAME,columnas,null,null,null,null,"_id DESC","1");

        //comprobamos que la fila exista (es decir, que la tabla no estuviera completamente vacía)
        if(ultFila.moveToFirst()) saldoAnterior = ultFila.getFloat(0);
        else saldoAnterior = 0;

        if(gasto) saldoActual = saldoAnterior - importe;
        else saldoActual = saldoAnterior + importe;
        Log.i("saldo","saldo actual "+saldoActual);
        Log.i("saldo","saldo anterior "+saldoAnterior);

        //cerramos el cursor
        ultFila.close();

        //Resto de cosas necesarias para la operación
        String comaespacio = ", ";
        String comilla = "'"; //para envolver Strings
        String gastoString = String.valueOf(gasto);
        String recurString = String.valueOf(recur);
        db.execSQL("INSERT INTO "+TABLE_NAME+" VALUES (null"+comaespacio+
                comilla+concepto+comilla+comaespacio+ //concepto
                categoriaID+comaespacio+ //CatID
                importe+comaespacio+ //importe
                comilla+gastoString+comilla+comaespacio+ //boolean gasto convertido a String
                comilla+fecha+comilla+comaespacio+ //fecha
                saldoActual+comaespacio+ //saldo en el momento
                comilla+recurString+comilla+comaespacio+ //boolean de recurrencia convertido en String
                comilla+modoRecur+comilla+comaespacio+ //modo de recurrencia
                idGenerica+ //idGenerica (ver RecurCheck)
                ")");
    }
    public void borrarTransaccion(int id){ //TODO: aquí es donde hay que actualizar el saldo
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME,dondeID+id,null);
    }

    //borra toda la tabla y devuelve el número de filas que se borraron
    public Integer borrarTabla(){
        SQLiteDatabase db = getWritableDatabase();
        int result = db.delete(TABLE_NAME,"1",null); //1 equivale a TRUE, se pasa como valor del parámetro que permite poner condiciones
        return result;
    }

    //Sobrecargamos el método listaTransacciones para adaptar la consulta a la base de datos a nuestras necesidades

    //esta es la petición estandar, no filtra nada. Es la que usamos para listar las transacciones
    public ArrayList<HashMap<String,Object>> listaTransacciones(int cantidad) {
        return listaTransacciones(Integer.toString(cantidad),null);
    }
    //esta es la que devuelve sólo las transacciones recurrentes
    public ArrayList<HashMap<String,Object>> listaTransaccionesRecurrentes() {
        return listaTransacciones(null, seleccionRecur);
    }

    //esta es la que contiene el código común a todas. No se usa fuera de la clase
    private ArrayList<HashMap<String,Object>> listaTransacciones(String cantidad, String seleccion) {
        ArrayList<HashMap<String,Object>>  result = new ArrayList<HashMap<String,Object>> ();
        //lista de listas, cada item es un diccionario nombreVariable - ValorVariable
        //nombreVariable declarados en la interfaz DatabasesMidas con la terminación __N

        //hacemos consulta a categorias para sacar el icono y el nombre de la categoria de cada transaccion a partir de su ID
        DBcategorias dbCategorias = new DBcategorias(context);
        ArrayList<HashMap<String,Object>> consultaCat = dbCategorias.listaCategorias();

        //código de la consulta a la base de datos de transacciones
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME,null,seleccion,null,null,null,"fecha DESC",cantidad);
        while (cursor.moveToNext()){

            HashMap<String,Object> item = new HashMap<String,Object>();
            item.put(idN, cursor.getInt(0)); //id
            item.put(conceptoN,cursor.getString(1)); //concepto
            item.put(importeN, cursor.getFloat(3));//importe
            item.put(gastoN, Boolean.valueOf(cursor.getString(4)));//gasto? - lo devolvemos como Boolean
            item.put(fechaN, cursor.getLong(5));//fecha
            item.put(saldoN, cursor.getFloat(6)); //saldo
            item.put(recurN,Boolean.valueOf(cursor.getString(7))); //recurrente? - lo devolvemos como Boolean

            //añadimos las cosas relacionadas con la categoría
            int catID = cursor.getInt(2);
            int index = catID-1;
            if (index==-1)index=0; /* no tengo ni idea de por qué esto es necesario
            (es imposible que catID fuera cero y por tanto index -1, pero aún así me da error si no incluyo esta línea */

            HashMap<String,Object> estaCategoria = consultaCat.get(index); /*consultaCat empieza por 0, catID por 1, el primer item
            de catID es 1, el primero de consultaCat es 0, por lo tanto el segundo de catID será 1,el segundo de consultaCat 2,
            y así sucesivamente */
            item.put(catIdN,catID); //catID
            item.put(catIconN,estaCategoria.get(catIconN)); //catIcon

            //si se están consultando las transacciones recurrentes, añadimos el modo de recurrencia y la id genérica
            if(seleccion!= null && seleccion.equals(seleccionRecur)){
                item.put(modoRecurN,cursor.getString(8));
                item.put(idGenericaN,cursor.getInt(9));
            }

            result.add(item); //añadimos la fila a la lista de listas result
        }
        cursor.close();
        return result;
    }
}
