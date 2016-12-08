package org.pietrus.midas;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;

/**
 * Created by pedro on 5/08/15.
 */
public class RecurDialogActivity extends Activity {
    private Resources resources;
    private String[] DIA = new String[2];
    private String[] SEM = new String[2];
    private String[] MES = new String[2];
    private String[] YEAR = new String[2];

    private String[] stringRecur;

    public static final String DIARIO = "DIARIO";
    public static final String SEMANAL = "SEMANAL";
    public static final String MENSUAL = "MENSUAL";
    public static final String ANUAL = "ANUAL";

    private String modoRecur = DIARIO; //por defecto TODO:quizás sería más lógico que el por defecto fuera mensual

    private TextView tvCada;
    private EditText etCada;

    private ToggleButton[] botonesSemana = new ToggleButton[7];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recurrence_dialog);
        tvCada = (TextView) findViewById(R.id.tvCada);
        etCada = (EditText) findViewById(R.id.etCadaN);

        resources = getResources();
        DIA = resources.getStringArray(R.array.dia);
        SEM = resources.getStringArray(R.array.semana);
        MES = resources.getStringArray(R.array.mes);
        YEAR = resources.getStringArray(R.array.year);

        Spinner spinner = (Spinner) findViewById(R.id.spinner);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.spinner_dialog, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);


        // Set the list's click listener
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            FrameLayout huecoSem = (FrameLayout) findViewById(R.id.semanaFr);

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {

                huecoSem.removeAllViews();
                switch (pos) {
                    case 0:
                        stringRecur = DIA;
                        modoRecur = DIARIO;
                        break;
                    case 1:
                        stringRecur = SEM;
                        modoRecur = SEMANAL;
                        LayoutInflater.from(getApplicationContext()).inflate(R.layout.dialog_fr_semana, huecoSem, true);
                        botonesSemana = prepararSemana();
                        break;
                    case 2:
                        stringRecur = MES;
                        modoRecur = MENSUAL;
                        break;
                    case 3:
                        stringRecur = YEAR;
                        modoRecur = ANUAL;
                        break;
                }
                tvCada.setText(stringRecur[0]);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //hacer algo cuando no se selecciona nada
            }

        });
        //escuchamos el EditText de "Cada X días/semanas/meses/años para cambiar el plural
        etCada.setOnEditorActionListener(
                new EditText.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                                actionId == EditorInfo.IME_ACTION_DONE ||
                                event.getAction() == KeyEvent.ACTION_DOWN &&
                                        event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                            if (!event.isShiftPressed()) {
                                // the user is done typing.
                                int num = Integer.getInteger(etCada.getText().toString());
                                if (num>1) tvCada.setText(stringRecur[1]);
                                else if (num==1) tvCada.setText(stringRecur[0]);
                                return true; // consume.
                            }
                        }
                        return false; // pass on to other listeners.
                    }
                }
        );
    }
    private ToggleButton[] prepararSemana(){
        String[] diasDeLaSemana = Hoy.diasDeLaSemana();
        ToggleButton result[] = new ToggleButton[7];
        LinearLayout fila1 = (LinearLayout) findViewById(R.id.week_group1_4);
        LinearLayout fila2 = (LinearLayout) findViewById(R.id.week_group2_3);


        //miramos ambas filas de botones, le ponemos el texto y lo añadimos al array
        for(int i=0;i<7;i++){
            String texto = diasDeLaSemana[i];

            final ToggleButton esteBoton;
            if(i<4) esteBoton = (ToggleButton) fila1.getChildAt(i);
            else esteBoton = (ToggleButton) fila2.getChildAt(i-4);

            esteBoton.setTextOn(texto);
            esteBoton.setTextOff(texto);
            esteBoton.setText(texto);

            esteBoton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) esteBoton.setTextColor(resources.getColor(R.color.primary_text));
                    else esteBoton.setTextColor(resources.getColor(R.color.secondary_text_default_material_light));
                }
            });

            result[i] = esteBoton;
        }

        return result;
    }
    public void sePulsaBoton (View view) {
        //Los dos botones llaman a este método desde el atributo onClick

        //preparamos el intent para salir de esta actividad cuando se pulse un botón
        Intent i = new Intent();

        //Enlazamos las vistas del layout a las distintas variables
        Button aceptar = (Button) findViewById(R.id.bAceptar);

        if (view == aceptar) {
            int cada = Integer.parseInt(etCada.getText().toString()); // cogemos el número que ha introducido el usuario


            //Si la recurrencia es semanal tenemos que coger los días de la semana en que se repite
            //los guardamos en una lista de Integer del 1 (domingo) al 7 (sábado)
            if(modoRecur==SEMANAL){
                ArrayList<Integer> diasActivados = new ArrayList<Integer>();

                for(int index=0;index<7;index++){
                    ToggleButton esteBoton = botonesSemana[index];
                    if(esteBoton.isChecked()) {
                        int a = index+2;
                        if (a==8) diasActivados.add(0,1); //si el domingo está seleccionado, lo pone el primero
                        else diasActivados.add(a);

                        //index [0123456]
                        //todos +2 = [2345678]
                        // 2= lunes, 8= domingo
                    }
                }

                //añadimos el resultado a lo que vamos a devolver a NewTransaccActivity
                i.putExtra("diasSiSemanal",diasActivados);
            }

            //guardamos los datos que queremos devolver a NewTransaccActivity
            i.putExtra("modo", modoRecur);
            i.putExtra("cada", cada);
        }

        setResult(RESULT_OK, i);
        finish();
    }
}