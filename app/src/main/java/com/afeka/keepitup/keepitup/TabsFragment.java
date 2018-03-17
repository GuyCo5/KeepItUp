package com.afeka.keepitup.keepitup;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;

public class TabsFragment extends Fragment implements SearchView.OnQueryTextListener {
    private Database db;
    public static ArrayList<Transaction> transToShow = new ArrayList<>();
    private RecyclerView rViewList;
    private FloatingActionButton newItemButton;
    private OnFragmentInteractionListener mListener;
    private CardViewAdapter cardAdapter;
    private AlertDialog.Builder builder;
    private int lastPosition = 0;
    private SearchView searchView;
    private int transType;


    public TabsFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static TabsFragment newInstance(String param1, String param2) {
        TabsFragment fragment = new TabsFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_tabs, container, false);
        db = new Database(getActivity().getBaseContext());
        getTransactionType();
        cardAdapter = new CardViewAdapter(getContext(),transToShow);
        buildRecycleView(view);

        Spinner spinner = view.findViewById(R.id.spinner_filter);

        final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.SortBy, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            spinner.setAdapter(adapter);

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                   switch(i){
                       case 0:
                           Collections.sort(transToShow, Transaction.BY_NAME);
                           break;
                       case 1:
                           Collections.sort(transToShow, Transaction.BY_START_DATE);
                           break;
                       case 2:
                           Collections.sort(transToShow, Transaction.BY_ID);
                           break;
                   }

                    cardAdapter.notifyDataSetChanged();
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                    Collections.sort(transToShow, Transaction.BY_ID);
                }
            });

            newItemButton = view.findViewById(R.id.floatingActionButton);
            newItemButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle bundle = new Bundle();
                    bundle.putInt("TYPE",transType);
                    bundle.putInt("EDIT", -1);
                    NewTransFragment newTransFragment = new NewTransFragment();
                    newTransFragment.setArguments(bundle);
                    ((MenuActivity)getActivity()).replaceFragment(newTransFragment);
                }
            });

            searchView = view.findViewById(R.id.search_view);
            searchView.setOnQueryTextListener(this);

        return view;
    }

    private void buildRecycleView(View view){
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));

        recyclerView.setAdapter(cardAdapter);
        setDialogConfirm();

        final SwipeCardController swipeCardController = new SwipeCardController(new SwipeControllerActions() {
            @Override
            public void onLeftClicked(int position) { //edit/show
                super.onLeftClicked(position);
                Bundle bundle = new Bundle();
                bundle.putInt("EDIT",transToShow.get(position).getId());
                NewTransFragment newTransFragment = new NewTransFragment();
                newTransFragment.setArguments(bundle);
                ((MenuActivity)getActivity()).replaceFragment(newTransFragment);


            }

            @Override
            public void onRightClicked(int position) { //delete
                lastPosition = position;
                builder.setTitle("Are you sure you would like to remove " +
                        transToShow.get(position).getName() + "?");
                builder.show();

            }
        });
        ItemTouchHelper itemTouchHelper= new ItemTouchHelper(swipeCardController);

        itemTouchHelper.attachToRecyclerView(recyclerView);

        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                swipeCardController.onDraw(c);
            }
        });


    }

    private void setDialogConfirm(){
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        db.removeTransaction(transToShow.get(lastPosition).getId());
                        transToShow.remove(lastPosition);
                        cardAdapter.notifyDataSetChanged();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //Do nothing..
                        break;
                }
            }
        };

        builder = new AlertDialog.Builder(getActivity(),android.R.style.Theme_Material_Light_Dialog_Alert);
        builder.setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).create();
    }

    void getTransactionType(){
        transType = getArguments().getInt("TYPE");

        switch (transType){
            case 0:
                transToShow = db.getTransactionList(Transaction.TransactionType.Insurance);
                break;
            case 1:
                transToShow = db.getTransactionList(Transaction.TransactionType.Warranty);
                break;
            case 3:
                transToShow = db.getTransactionList(Transaction.TransactionType.Provider);
                break;
            case -1:
                //general
                default:
                    //general

        }
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onNewItemClicked();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
  /*    if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }

*/

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        cardAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        cardAdapter.getFilter().filter(s);
        return false;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onNewItemClicked();
    }



}
