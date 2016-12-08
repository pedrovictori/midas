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
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class CatAdapter extends BaseAdapter {
    private final Activity actividad;
    private final ArrayList<HashMap<String,Object>> consultaBD;

    //en el constructor nos pasan la consulta ya hecha a la base de datos
    public CatAdapter(Activity actividad, ArrayList<HashMap<String,Object>> consultaBD) {
        super();
        this.actividad = actividad;
        this.consultaBD = consultaBD;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        ViewHolder viewholder;

        //Si convertView es null, crea un view nuevo y se definen sus sub-views dentro del tag como un viewholder, si no, reutiliza convertView
        if (convertView == null) {
            LayoutInflater inflater = actividad.getLayoutInflater();
            view = inflater.inflate(R.layout.cat_item, null, true);

            viewholder = new ViewHolder();
            viewholder.icono = (ImageView) view.findViewById(R.id.icono);
            viewholder.nombre = (TextView) view.findViewById(R.id.catName);
            view.setTag(viewholder);
        }
        else view = convertView;

        HashMap<String,Object> thisItem = consultaBD.get(position);

        //conectamos el String icono al recurso correspondiente
        String iconName = thisItem.get(DatabasesMidas.catIconN).toString();
        int referencia = actividad.getResources().getIdentifier("ic_" + iconName, "drawable", actividad.getPackageName());

        //recogemos el viewholder que contiene las referencias a las sub-vistas del tag
        viewholder = (ViewHolder) view.getTag();

        //Asignamos los items de esta fila a cada sub-vista
        viewholder.nombre.setText(thisItem.get(DatabasesMidas.catNombreN).toString());
        viewholder.icono.setImageResource(referencia);


        return view;
    }

    public int getCount() {
        return consultaBD.size();
    }

    public Object getItem(int pos) {
        return consultaBD.get(pos);
    }

    public long getItemId(int pos) {
        return pos; //esto lo cogí del libro de android pero parece redundante
    }

    static class ViewHolder {
        ImageView icono;
        TextView nombre;
    }
}