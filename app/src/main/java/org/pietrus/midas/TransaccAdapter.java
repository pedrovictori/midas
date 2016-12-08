package org.pietrus.midas;

/**
 * Created by pedro on 24/07/15.
 */

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;

public class TransaccAdapter extends BaseAdapter {
    private final Activity actividad;
    private ArrayList<HashMap<String,Object>> consultaBD;
    private final DBtransacciones basedatos;
    private ListView listView;
    private LinearLayout lEmpty;
    private int nItems;

    public TransaccAdapter(Activity actividad, int nItems, ListView listView, LinearLayout lEmpty){ //tenemos que incluir lEmpty para que lo muestre si se borran todas las transacciones y se queda vacía la lista
        //TODO: hacer que sólo se muestre el nombre de la categoría, el concepto o "notas" sólo se mostrará en el detalle de la transacción
        //TODO: poner largo de texto máximo en nombre categoría para poder poner un tamaño de texto fijo
        //TODO: si la fecha pasa de un largo máximo, pasar a formato más corto (buscar como obtener tamaño máximo de una lista de un TextView en un teléfono determinado
        //TODO: explorar la posibilidad de transferir esto a un CursorAdapter
        super();
        this.actividad = actividad;
        this.listView = listView;
        this.lEmpty = lEmpty;
        this.nItems = nItems;
        //hace una única consulta a la base de datos cuando se crea el objeto TransaccAdapter
        //asignamos la base de datos "transacciones" a la variable basedatos. Si el archivo no existe, se crea ahora.
        basedatos = new DBtransacciones(actividad);

        //hacemos consulta a base de datos, nItems últimas transacciones, y las guardamos en la variable consultaBD
        consultaBD = basedatos.listaTransacciones(nItems);

        //ponemos saldo en el TextView de la toolbar
        TextView toolbarText = (TextView) actividad.findViewById(R.id.toolbar_subtitle);
        String saldo;

        if(consultaBD.isEmpty()) saldo = "0";
        else{
            HashMap<String,Object> ultFila = consultaBD.get(0);
            saldo = String.valueOf(ultFila.get(DatabasesMidas.saldoN));
        }
        toolbarText.setText(saldo+"€");//TODO: definir divisa según preferencias
    }

    public View getView(int position, View convertView, ViewGroup parent){
        View view;
        ViewHolder viewholder;

        //Si convertView es null, crea un view nuevo y se definen sus sub-views dentro del tag como un viewholder, si no, reutiliza convertView
        if (convertView==null){
            LayoutInflater inflater = actividad.getLayoutInflater();
            view = inflater.inflate(R.layout.transacc_item, null, true);

            viewholder = new ViewHolder();
            viewholder.icono = (ImageView)view.findViewById(R.id.icono);
            viewholder.ic_recur = (ImageView) view.findViewById(R.id.ic_recur);
            viewholder.tvFecha = (TextView) view.findViewById(R.id.fecha);
            viewholder.concepto = (AutoResizeTextView) view.findViewById(R.id.concepto);
            viewholder.importe = (FontFitTextView) view.findViewById(R.id.importe);

            view.setTag(viewholder);
        }
        else view = convertView;

        HashMap<String,Object> thisItem = consultaBD.get(position);

        //recogemos el viewholder que contiene las referencias a las sub-vistas del tag
        viewholder = (ViewHolder) view.getTag();

        //procesamos la fecha
        long fecha = (long) thisItem.get(DatabasesMidas.fechaN);
        Date date = new Date(fecha);
        String fechaMostrada = Hoy.procesar(date,actividad);

        //Asignamos los items de esta fila a cada vista
        viewholder.tvFecha.setText(fechaMostrada);
        viewholder.concepto.setText(thisItem.get(DatabasesMidas.conceptoN).toString());
        viewholder.importe.setText(thisItem.get(DatabasesMidas.importeN).toString() + " €"); //TODO: decidir divisa según preferencias

        //relacionamos el iconName con un drawable y lo ponemos en la vista
        String iconName = thisItem.get(DatabasesMidas.catIconN).toString();
        int referencia = actividad.getResources().getIdentifier("ic_" + iconName, "drawable", actividad.getPackageName());
        viewholder.icono.setImageResource(referencia);

        //si es una transacción recurrente, añadimos el icono de recurrencia. Si no, tapamos el icono de recurrencia por si acaso ocupaba espacio de la vista reciclada
        boolean recurrencia = (boolean) thisItem.get(DatabasesMidas.recurN);
        if(recurrencia) viewholder.ic_recur.setVisibility(View.VISIBLE);
        else viewholder.ic_recur.setVisibility(View.GONE);

        //el importe se pone en rojo o verde dependiendo de si es un gasto o un ingreso
        boolean gasto = (boolean) thisItem.get(DatabasesMidas.gastoN);
        if (gasto) viewholder.importe.setTextColor(viewholder.importe.getContext().getResources().getColor(R.color.color_rojo));
        else viewholder.importe.setTextColor(viewholder.importe.getContext().getResources().getColor(R.color.color_verde));

        return view;
    }

    public int getCount(){
        return consultaBD.size();
    }

    public HashMap<String,Object> getItem (int pos){
        return consultaBD.get(pos);
    }

    public long getItemId (int pos){
        return pos;
    }

    //para borrar de la lista pero no de la base de datos (sólo "oculta" el item)
    public void remove (int pos){
        consultaBD.remove(pos);
        notifyDataSetChanged();
    }

    //para borrar de la base de datos
    public void dbDelete (int pos){
        basedatos.borrarTransaccion(getItemDbID(pos));
        consultaBD = basedatos.listaTransacciones(nItems); //actualizamos la info proveniente de la base de datos

        //si al borrar esta transacción, la lista se ha quedado vacía, mostramos el empty layout
        if(consultaBD.size()==0){
            listView.setVisibility(View.INVISIBLE);
            lEmpty.setVisibility(View.VISIBLE);
        }
        notifyDataSetChanged();
    }

    public void insert (int pos, HashMap<String,Object> item){
        consultaBD.add(pos,item);
        notifyDataSetChanged();
    }
    public int getItemDbID(int pos){
        HashMap<String,Object> thisItem = (HashMap<String, Object>) getItem(pos);
        return (int) thisItem.get(DatabasesMidas.idN);
    }

    static class ViewHolder{
        ImageView icono;
        TextView tvFecha;
        AutoResizeTextView concepto;
        FontFitTextView importe;
        ImageView ic_recur;

    }
}
