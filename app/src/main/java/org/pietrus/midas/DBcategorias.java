package org.pietrus.midas;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;

/**
 * Created by pedro on 30/07/15.
 */
public class DBcategorias extends SQLiteAssetHelper implements DatabasesMidas{
    //TODO: habrá que traducir las tablas, hacer una tabla para cada idioma y seleccionar su nombre según el locale
    private static final String DB_NAME = "categorias.sqlite";
    private static final int DB_VERSION = 1;
    private final String TABLE_NAME = "categorias";
    //Columnas de la tabla: CatID, Nombre, Icono, Gasto

    //Constructor
    public DBcategorias(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onUpgrade (SQLiteDatabase db, int oldV, int newV){
        //TODO: implementar algo aquí
    }

    //TODO: este método se usará para crear nuevas categorías desde la sección Categorías
    public void guardarCategoria (String nombre,String icono, boolean gasto){
        SQLiteDatabase db = getWritableDatabase();
        String comaespacio = ", ";
        String comilla = "'"; //para envolver Strings
        String gastoString = String.valueOf(gasto);
        db.execSQL("INSERT INTO "+TABLE_NAME+" VALUES (null"+comaespacio+
                comilla+nombre+comilla+comaespacio+ //nombre de la categoría
                comilla+icono+comilla+comaespacio+ //referencia al icono de la categoría
                comilla+gastoString+comilla+")"); //boolean gasto convertido en String
    }

    //A continuación se sobrecarga el método listaCategorias para permitir varias opciones de consulta:

    //este es para consultar o sólo categorías de gastos o sólo de ingresos
    public ArrayList<HashMap<String,Object>> listaCategorias (boolean gasto) {
        if (gasto) return listaCategorias("Gasto IS 'true'");
        else return listaCategorias("Gasto IS 'false'");
    }

    //este es para consultar todas las categorías existentes
    public ArrayList<HashMap<String,Object>> listaCategorias () {
        return listaCategorias(null);
    }

    //este implementa el código que es común a todos los métodos sobrecargados (sólo se usa dentro de la clase)
    private ArrayList<HashMap<String,Object>> listaCategorias (String gasto) {

        //TODO ordenar por veces usadas
        ArrayList<HashMap<String,Object>> result = new ArrayList<HashMap<String,Object>>();
        //lista de listas, cada item es un diccionario CatID - [info categoría]
        //[info categoría] es un diccionario nombreVariable - valorVariable
        //nombreVariable declarados al principio de la interfaz DatabasesMidas con la terminación __N

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor;

        cursor = db.query(TABLE_NAME, null,gasto, null, null, null, "CatID", null);

        while (cursor.moveToNext()){

            HashMap<String,Object> item = new HashMap<String,Object>();

            item.put(catIdN,cursor.getInt(0)); //ID
            item.put(catNombreN, cursor.getString(1)); //Nombre
            item.put(catIconN, cursor.getString(2)); //Icono

            result.add(item); //añadimos la fila a la lista de listas result
        }
        cursor.close();
        return result;
    }

}



