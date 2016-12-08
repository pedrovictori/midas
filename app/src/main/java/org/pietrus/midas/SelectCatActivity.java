package org.pietrus.midas;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;


public class SelectCatActivity extends AppCompatActivity {

    ArrayList<HashMap<String,Object>> consultaBD;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_cat);

        //Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Recogemos información de la anterior actividad
        Bundle info = getIntent().getExtras();
        boolean gasto = info.getBoolean("gasto");

        //guardamos la lista del layout xml en una variable ListView
        ListView listViewCat = (ListView) findViewById(R.id.list);

        //hacemos la consulta a la base de datos
        DBcategorias basedatos = new DBcategorias(this);
        consultaBD = basedatos.listaCategorias(gasto);

        //iniciamos el adaptador
        CatAdapter adapter = new CatAdapter(this,consultaBD);

        //se conecta el adaptador al ListView.
        // El adaptador va item por item asignando valores a sus vistas según lo definido en la clase CatAdapter
        listViewCat.setAdapter(adapter);

        //¿Qué pasa si se pulsa un item del ListView?
        listViewCat.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                view.setSelected(true);
                HashMap<String,Object> thisItem = consultaBD.get(position);
                int catID = (int) thisItem.get(DatabasesMidas.catIdN);
                String nombre = thisItem.get(DatabasesMidas.catNombreN).toString();
                String icono = thisItem.get(DatabasesMidas.catIconN).toString();

                //preparamos para volver a NewTransacc
                Intent i = new Intent();
                i.putExtra("catID",catID);
                i.putExtra("nombre",nombre);
                i.putExtra("icono",icono);
                setResult(RESULT_OK, i);
                finish();
            }
        });

    }

    //cosas generadas automáticamente - para poner opciones en la toolbar y en su menú
    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_select_cat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/
}
