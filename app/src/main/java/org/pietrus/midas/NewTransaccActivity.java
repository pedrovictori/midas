package org.pietrus.midas;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class NewTransaccActivity extends AppCompatActivity {
    //TODO:poner largo máximo en todos los campos
    //TODO: impedir que se deje vacío el importe o la categoría. Ver http://www.google.com/design/spec/patterns/errors.html
    //TODO: poner botón GUARDAR en toolbar
    //TODO: investigar toolbars, support, poder usar la toolbar por defecto, botón atrás (up)

    private boolean gasto = true;
    private static long fecha;
    private static EditText etFecha;

    //códigos de actividades lanzadas
    private final int codeCatAct = 2222;
    private final int codeDiaAct = 3333;

    //variables de categorías
    private int catID;
    private String catName;
    private ImageView ivIconoCat;
    private EditText etNombreCat;

    //variables de recurrencia
    private boolean recurrencia = false;
    private String recurResult; //formato: modo,cada,lun/mar/mie


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_transacc);

        //Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //categorías
        etNombreCat = (EditText) findViewById(R.id.catName);
        ivIconoCat = (ImageView) findViewById(R.id.iconoCat);

        //fecha
        //cogemos fecha actual la guardamos, la procesamos y la ponemos en el textview
        Calendar cal = Calendar.getInstance();
        fecha = cal.getTimeInMillis();
        Date date = cal.getTime();
        etFecha = (EditText) findViewById(R.id.tvFecha);
        etFecha.setText(this.getString(R.string.hoy) + ", " + Hoy.diaSemanaFormat.format(date));

        //toogle button
        final ToggleButton toggle = (ToggleButton) findViewById(R.id.btTipoTransacc);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) gasto = false; // The toggle is enabled
                else gasto = true; // The toggle is disabled

                if (catID != 0){
                    //resetear CatSelector
                    catID = 0;
                    catName = null;
                    ivIconoCat.setImageResource(R.drawable.ic_interrogante);
                    etNombreCat.setText("");
                }
            }
        });

        //Selector de fecha
        etFecha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerFragment(); //clase estática definida al final de esta misma clase
                newFragment.show(getSupportFragmentManager(), "datePicker");
            }
        });

        //Selector de categorías
        EditText selectCat = (EditText) findViewById(R.id.catName);
        selectCat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(v.getContext(), SelectCatActivity.class);

                //pasamos a SelectCatActivity el valor de gasto
                i.putExtra("gasto", gasto);

                //lanzamos actividad y esperamos resultado (se recoge en el método onActivityResult)
                startActivityForResult(i, codeCatAct);
            }
        });
    }

    //Recurrencia de fecha
    public void setRecur(View v){
        //lanzamos actividad RecurDialogActivity y esperamos resultados en on ActivityResult
        Intent d = new Intent(this,RecurDialogActivity.class);
        startActivityForResult(d,codeDiaAct);
    }

    //recogemos resultados de actividades lanzadas (SelectCatActivity o RecurDialogActivity)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode==RESULT_OK) {
            Bundle info = data.getExtras();

            //si nos devuelve resultados de SelectCat
            if (requestCode == codeCatAct) {
                //Recogemos el catID para devolverlo a FrMain como parte del resultado
                catID = info.getInt("catID");

                //recogemos el nombre y el icono de la categoría para mostrarlos en el layout
                catName = info.getString("nombre");
                String icono = info.getString("icono");

                //conectamos el String icono al recurso correspondiente
                int referencia = this.getResources().getIdentifier("ic_" + icono, "drawable", this.getPackageName());

                //mostramos en el layout
                ivIconoCat.setImageResource(referencia);
                etNombreCat.setText(catName);
            }
            //si nos devuelve resultados de ReccurrenceDialog
            else if (requestCode == codeDiaAct){
                if(info != null){
                    recurrencia = true;
                    String modo = info.getString("modo");
                    String cada = String.valueOf(info.getInt("cada"));

                    //pasamos todos los datos recogidos a una String CSV para guardarlo en la base de datos
                    //formato: modo,cada,lun/mar/mie
                    recurResult = modo+","+cada;

                    if(info.containsKey("diasSiSemanal")){
                        ArrayList<Integer> dias = info.getIntegerArrayList("diasSiSemanal");
                        String diasSemana = dias.toString().replace("[", "").replace("]", "").replace(", ", "/");
                        recurResult += ","+diasSemana;
                    }
                    Log.i("recurrencia",recurResult);
                }
            }
        }
    }

    public void sePulsaBoton (View view) {
        //Los dos botones llaman a este método desde el atributo onClick
        //Lo importante del código lo pongo en este evento porque de momento sólo pasan cosas cuando se pulsa un botón

        //preparamos el intent para salir de esta actividad cuando se pulse un botón
        Intent i = new Intent();

        boolean recargar = false;

        //Enlazamos las vistas del layout a las distintas variables
        Button aceptar = (Button) findViewById(R.id.bAceptar);

        if(view== aceptar){
            EditText etImporte = (EditText) findViewById(R.id.editImporte);
            EditText etConcepto = (EditText) findViewById(R.id.editConcepto);

            DBtransacciones basedatos = new DBtransacciones(this);

            float importe = Float.parseFloat(etImporte.getText().toString().trim());

            String concepto = etConcepto.getText().toString().trim();
            //si el concepto está vacío, lo sustituimos por el nombre de la categoría
            if (concepto.equals("")) concepto = catName;

            //guardamos toda la información en la base de datos
            basedatos.guardarTransaccion(concepto, catID, importe, gasto, fecha,recurrencia,recurResult);

            //si la transacción creada es una transacción recurrente, actualizamos ahora todas las instancias de esta.
            if(recurrencia) RecurCheck.actualizarTransaccionesRecurrentes(this);

            //para indicar que hay que recargar el ListView sólo si se ha añadido un nuevo item, si se pulsa Cancelar, no
            recargar = true;
        }
        i.putExtra("recargar", recargar);
        setResult(RESULT_OK, i);
        finish();
    }

    //Clase para el fragmento de calendario que sirve para escoger una fecha
    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog
            DatePickerDialog dialog = new DatePickerDialog(getActivity(), R.style.theme_dialog_date,this, year, month, day);

            dialog.getDatePicker().setMaxDate(new Date().getTime()); //fecha máxima: hoy
            c.set(Calendar.YEAR,year-1);
            dialog.getDatePicker().setMinDate(c.getTime().getTime()); //fecha mínima: hace un año

            return dialog;
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            //ya se ha escogido una fecha

            //formateamos y guardamos la fecha:
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(0);
            cal.set(year, month, day);

            fecha = cal.getTimeInMillis(); //esta es la que se guarda en la base de datos

            Date date = cal.getTime();
            String fechamostrada = Hoy.procesar(date,getActivity());

            //la mostramos en el TextView
            etFecha.setText(fechamostrada);

        }
    }

    //cosas generadas automáticamente - para poner opciones en la toolbar y en su menú
/*    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_transacc, menu);
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
