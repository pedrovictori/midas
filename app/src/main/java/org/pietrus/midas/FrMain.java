/**
 * Created by pedro on 23/07/15.
 */

package org.pietrus.midas;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.util.HashMap;

import de.timroes.android.listview.EnhancedListView;

public class FrMain extends Fragment {
    /*TODO: Acordarse de cancelar la alarma cuando se borren transacciones recurrentes, si procede*/
    private EnhancedListView listViewTransacc;
    private LinearLayout lEmpty;
    private TransaccAdapter adapter;

    public FrMain() {
        // Required lEmpty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i("onCreateView","FrMain");


        // Inflate the layout for this fragment
        FrameLayout frMain;
        frMain = (FrameLayout) inflater.inflate(R.layout.fr_main, container, false);

        //guardamos la lista del layout xml en una variable ListView
        listViewTransacc = (EnhancedListView) frMain.findViewById(R.id.list);

        //guardamos el layout que mostraremos si la base de datos está vacía
        lEmpty = (LinearLayout) frMain.findViewById(R.id.empty);

        //primero vemos si la tabla está vacía
        DBtransacciones basedatos = new DBtransacciones(getActivity());

        if(basedatos.isEmpty()) toggleEmptyLayout(true, listViewTransacc, lEmpty);//mostramos el layout especial para cuando está vacía


        // CONFIGURAMOS LA LISTA
        else {
            toggleEmptyLayout(false, listViewTransacc, lEmpty); //tapamos el layout especial para vacío y mostramos la lista

            //iniciamos el adaptador
            adapter = new TransaccAdapter(getActivity(), 20, listViewTransacc,lEmpty);

            //se conecta el adaptador al ListView.
            // El adaptador va item por item asignando valores a sus vistas según lo definido en la clase TransaccAdapter
            listViewTransacc.setAdapter(adapter);

            //configuramos el resto de cosas de la lista
            init();
        }

        //FAB
        FloatingActionButton bFab = (FloatingActionButton) frMain.findViewById(R.id.fab);
        bFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //si se pulsa el botón +, lanzamos la actividad con el diálogo de nueva transacción, y esperamos resultado (justo abajo)
                Intent i;
                i = new Intent(getActivity(), NewTransaccActivity.class);
                startActivityForResult(i, 1111);
            }
        });

        return frMain;
    }

    //¿QUÉ PASA DESPUÉS DE VOLVER DE NEW TRANSACTION ACTIVITY?
    @Override
    public void onActivityResult (int requestCode, int resultCode, Intent data){
        if(requestCode==1111 && resultCode== Activity.RESULT_OK){
            boolean recargar = data.getExtras().getBoolean("recargar");

            //hay que recargar el ListView sólo si se ha añadido un nuevo item, si se ha pulsado Cancelar, no
            if(recargar) {
                if(listViewTransacc.getVisibility()==View.INVISIBLE)
                    toggleEmptyLayout(false, listViewTransacc, lEmpty); //la lista estaba invisible porque se estaba mostrando el empty layout, la hacemos visible

                //refrescamos el ListView creando un nuevo adaptador para que haga una consulta nueva a la base de datos
                adapter = new TransaccAdapter(getActivity(), 20,listViewTransacc,lEmpty);
                listViewTransacc.setAdapter(adapter);
            }
        }

    }

    //método para cambiar entre el layout vacío y el layout de lista
    private void toggleEmptyLayout(boolean empty, ListView list, LinearLayout l_empty){
        if (empty) {
            list.setVisibility(View.INVISIBLE);
            l_empty.setVisibility(View.VISIBLE);
        }
        else {
            list.setVisibility(View.VISIBLE);
            l_empty.setVisibility(View.INVISIBLE);
        }
    }

    //configuraciones iniciales de la lista
    private void init(){
//TODO: algo falla al borrar todos los items de la lista, el índice de la base de datos o algo así. Arreglar
        //¿Qué pasa si se pulsa un item del ListView?
        listViewTransacc.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //TODO: hacer que al pulsar un item vaya a una nueva actividad que muestre el detalle de esa transacción
                Toast.makeText(getActivity(), "Row " + position + " clicked", Toast.LENGTH_SHORT).show();
            }
        });

        //¿Qué pasa cuando se elimina un item?
        listViewTransacc.setDismissCallback(new EnhancedListView.OnDismissCallback() {

            @Override
            public EnhancedListView.Undoable onDismiss(EnhancedListView listView, final int position) {
                // Store the item for later undo
                final HashMap<String,Object> item = (HashMap<String,Object>) adapter.getItem(position);

                // Remove the item from the adapter
                adapter.remove(position);

                // return an Undoable
                return new EnhancedListView.Undoable() {
                    // Reinsert the item to the adapter
                    @Override
                    public void undo() {
                        adapter.insert(position, item);
                    }

                    //borramos el item definitivamente de la base de datos
                    @Override
                    public void discard() {
                        adapter.dbDelete(position);
                    }
                };

            }

        });
        listViewTransacc.setUndoStyle(EnhancedListView.UndoStyle.COLLAPSED_POPUP); //hacemos que varios undo consecutivos se agrupen en uno
        listViewTransacc.setSwipeDirection(EnhancedListView.SwipeDirection.END); //hacemos que sólo se pueda deslizar de izquierda a derecha
        listViewTransacc.enableSwipeToDismiss(); //activamos el deslizamiento
        listViewTransacc.setSwipingLayout(R.id.mainView);//hacemos que sólo se deslize la capa superior, mostrando el icono de borrado debajo
    }

}
//TODO: hacer que carguen más elementos al tirar de la lista hacia arriba.