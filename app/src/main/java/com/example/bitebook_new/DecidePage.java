package com.example.bitebook_new;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.*;
import java.util.ArrayList;

public class DecidePage extends Fragment {

    RecyclerView decideRecycler;
    RecyclerView.Adapter adapter;
    Spinner areaSpinner;
    Spinner cuisineSpinner;
    Spinner priceSpinner;
    ArrayList<Entry> entries;

    public DecidePage(){
        // require a empty public constructor
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_decide_page, container, false);

        areaSpinner = view.findViewById(R.id.areaSpinner);
        cuisineSpinner = view.findViewById(R.id.cuisineSpinner);
        priceSpinner = view.findViewById(R.id.priceSpinner);

        // set up the spinners
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(
                getContext(), R.array.areaSpinner, android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        areaSpinner.setAdapter(adapter1);

        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(
                getContext(), R.array.cuisineSpinner, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cuisineSpinner.setAdapter(adapter2);

        ArrayAdapter<CharSequence> adapter3 = ArrayAdapter.createFromResource(
                getContext(), R.array.priceSpinner, android.R.layout.simple_spinner_item);
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        priceSpinner.setAdapter(adapter3);

        entries = new ArrayList<>();

        Entry item1 = new Entry("Rsname", "filet", 12, "east", 2, "This is amazing", "Western", "url");

        entries.add(item1);

        decideRecycler = (RecyclerView) view.findViewById(R.id.decideRecycler);

        Log.i("asdf","ASDFASDFASDFASFD");

        Context context = container.getContext();

        LinearLayoutManager layoutManager =new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        Log.i("asdf", String.valueOf(layoutManager));
        decideRecycler.setLayoutManager(layoutManager);
        adapter = new DecideCardAdapter(context, entries);
        decideRecycler.setAdapter(adapter);

        return view;
    }
}
