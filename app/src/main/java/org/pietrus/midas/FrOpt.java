/**
 * Created by pedro on 23/07/15.
 */

package org.pietrus.midas;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.util.Calendar;

public class FrOpt extends Fragment {
    Button botonBor;
    Button botonFill;

    public FrOpt() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FrameLayout frOpt;
        frOpt = (FrameLayout) inflater.inflate(R.layout.fr_opt, container, false);

        final DBtransacciones db = new DBtransacciones(getActivity());

        botonBor = (Button) frOpt.findViewById(R.id.botonbor);
        botonBor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int filasBorradas = db.borrarTabla();
                Toast.makeText(getActivity(), "filas borradas " + filasBorradas, Toast.LENGTH_LONG).show();
            }
        });

        botonFill = (Button) frOpt.findViewById(R.id.botonfil);
        botonFill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                long fecha = cal.getTimeInMillis();

                db.guardarTransaccion("prueba", 1, (float) 40.0, true, fecha, false, "");
                db.guardarTransaccion("pruebssdfsdfsdfsdfsdfa", 1, (float) 40.0, true, fecha, false, "");
                db.guardarTransaccion("prusdfsdfsdfsdfsdeba", 1, (float) 40.0, true, fecha, false, "");
                db.guardarTransaccion("prueba", 4, (float) 40.0, true, fecha, false, "");
                db.guardarTransaccion("prusdfsdfsdfsdfsdfsdfeba", 1, (float) 40.0, true, fecha, false, "");
                db.guardarTransaccion("prueba", 1, (float) 40.0, true, fecha, false, "");
                db.guardarTransaccion("prueba", 7, (float) 4340.0, true, fecha, false, "");
                db.guardarTransaccion("prusdfsdfsdfsdfsdfeba", 1, (float) 40.0, true, fecha, false, "");
                db.guardarTransaccion("prueba", 7, (float) 4340.0, false, fecha, false, "");
                db.guardarTransaccion("prueba", 1, (float) 4340.0, true, fecha, false, "");
                db.guardarTransaccion("prueba", 5, (float) 4560.0, false, fecha, false, "");
                db.guardarTransaccion("pruesdfsdfsdfsfsdfba", 1, (float) 40.0, true, fecha, false, "");
                db.guardarTransaccion("prueba", 1, (float) 4430.0, true, fecha, false, "");
                db.guardarTransaccion("prueba", 8, (float) 4430.0, true, fecha, false, "");
                db.guardarTransaccion("prueba", 9, (float) 47560.0, true, fecha, false, "");
                db.guardarTransaccion("prueba", 11, (float) 42340.0, false, fecha, false, "");
                db.guardarTransaccion("prueba", 14, (float) 40.0, true, fecha, false, "");
                db.guardarTransaccion("prueba", 2, (float) 4032.0, true, fecha, false, "");
                db.guardarTransaccion("prueba", 2, (float) 40.0, true, fecha, false, "");
                db.guardarTransaccion("prueba", 6, (float) 46770.0, false, fecha, false, "");
                db.guardarTransaccion("prueba", 6, (float) 40.0, true, fecha, false, "");
                db.guardarTransaccion("prueba", 1, (float) 4430.0, false, fecha, false, "");

                Toast.makeText(getActivity(), "22 filas creadas", Toast.LENGTH_LONG).show();
            }
        });

        return frOpt;
    }
}