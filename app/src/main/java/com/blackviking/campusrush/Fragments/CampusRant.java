package com.blackviking.campusrush.Fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blackviking.campusrush.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class CampusRant extends Fragment {


    public CampusRant() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_campus_rant, container, false);
    }

}
