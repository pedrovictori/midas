/**
 * Created by pedro on 23/07/15.
 */

package org.pietrus.midas;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class FrBudget extends Fragment {

    public FrBudget() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        FrameLayout frBudget;
        frBudget = (FrameLayout) inflater.inflate(R.layout.fr_budget, container, false);

        //FAB
        FloatingActionButton floatingActionButton = (FloatingActionButton) frBudget.findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i;
                i = new Intent(getActivity(), NewBudgetActivity.class);
                startActivity(i);
            }
        });

        return frBudget;
    }
}