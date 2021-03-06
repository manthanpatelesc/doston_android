package com.desibitz.shortvideo.Inbox_Message_Classes;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.desibitz.shortvideo.MessageChat_Classes.Doston_Chat_Activity;
import com.desibitz.shortvideo.Main_Menu_Classes.RelateToFragment_OnBack.Doston_RootFragment;
import com.desibitz.shortvideo.R;
import com.desibitz.shortvideo.SimpleClasses.Doston_Functions;
import com.desibitz.shortvideo.SimpleClasses.Variables;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

/**
 * A simple {@link Fragment} subclass.
 */
public class Doston_Inbox_F extends Doston_RootFragment {


    View view;
    Context context;

    RecyclerView inbox_list;

    ArrayList<Doston_Inbox_Get_Set> inbox_arraylist;
    DatabaseReference root_ref;

    Doston_Inbox_Adapter dostonInbox_adapter;

    ProgressBar pbar;

    boolean isview_created=false;
    Doston_Inbox_Get_Set doston_inbox_get_set;

    public Doston_Inbox_F() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view= inflater.inflate(R.layout.fragment_inbox, container, false);
        context=getContext();

        root_ref= FirebaseDatabase.getInstance().getReference();


        pbar=view.findViewById(R.id.pbar);
        inbox_list=view.findViewById(R.id.inboxlist);

        // intialize the arraylist and and inboxlist
        inbox_arraylist=new ArrayList<>();

        inbox_list = (RecyclerView) view.findViewById(R.id.inboxlist);
        LinearLayoutManager layout = new LinearLayoutManager(context);
        inbox_list.setLayoutManager(layout);
        inbox_list.setHasFixedSize(false);
        dostonInbox_adapter =new Doston_Inbox_Adapter(context, inbox_arraylist, new Doston_Inbox_Adapter.OnItemClickListener() {
            @Override
            public void onItemClick(Doston_Inbox_Get_Set item) {
                // if user allow the stroage permission then we open the chat view
                doston_inbox_get_set =item;
                if(check_ReadStoragepermission())
                {
                    chatFragment(item.getId(),item.getName(),item.getPic());
                }



            }
        }, new Doston_Inbox_Adapter.OnLongItemClickListener() {
            @Override
            public void onLongItemClick(Doston_Inbox_Get_Set item) {

            }
        });

        inbox_list.setAdapter(dostonInbox_adapter);





        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Doston_Functions.hideSoftKeyboard(getActivity());
            }
        });



        isview_created=true;

        return view;
    }


//    AdView adView;
    @Override
    public void onStart() {
        super.onStart();
//        adView = view.findViewById(R.id.bannerad);
//        AdRequest adRequest = new AdRequest.Builder().build();
//        adView.loadAd(adRequest);
    }



    // whenever there is focus in the third tab we will get the match list
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if(view!=null) {
            if (Variables.sharedPreferences.getBoolean(Variables.islogin, false) && inbox_arraylist.isEmpty())
                getData();
             }

    }



    // on start we will get the Inbox Message of user  which is show in bottom list of third tab
    ValueEventListener eventListener2;

    Query inbox_query;
    public void getData() {

        pbar.setVisibility(View.VISIBLE);

        inbox_query=root_ref.child("Inbox").child(Variables.user_id).orderByChild("date");
        eventListener2=new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                inbox_arraylist.clear();

                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                    Doston_Inbox_Get_Set model = ds.getValue(Doston_Inbox_Get_Set.class);
                    model.setId(ds.getKey());

                    inbox_arraylist.add(model);
                }

                pbar.setVisibility(View.GONE);

                if (inbox_arraylist.isEmpty())
                    view.findViewById(R.id.no_data_layout).setVisibility(View.VISIBLE);
                else {
                    view.findViewById(R.id.no_data_layout).setVisibility(View.GONE);
                    Collections.reverse(inbox_arraylist);
                    dostonInbox_adapter.notifyDataSetChanged();
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        inbox_query.addValueEventListener(eventListener2);


    }



    // on stop we will remove the listener
    @Override
    public void onStop() {
        super.onStop();
        if(inbox_query!=null)
        inbox_query.removeEventListener(eventListener2);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Variables.permission_Read_data) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(context, "Tap again", Toast.LENGTH_SHORT).show();
                chatFragment(doston_inbox_get_set.getId(),doston_inbox_get_set.getName(),doston_inbox_get_set.getPic());
            }

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    //open the chat fragment and on item click and pass your id and the other person id in which
    //you want to chat with them and this parameter is that is we move from match list or inbox list
    public void chatFragment(String receiverid, String name, String picture){
        Doston_Chat_Activity dostonChat_activity = new Doston_Chat_Activity();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right);

        Bundle args = new Bundle();
        args.putString("user_id", receiverid);
        args.putString("user_name",name);
        args.putString("user_pic",picture);

        dostonChat_activity.setArguments(args);
        transaction.addToBackStack(null);
        transaction.replace(R.id.MainMenuFragment, dostonChat_activity).commit();
    }



    //this method will check there is a storage permission given or not
    private boolean check_ReadStoragepermission(){
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED){
            return true;
        }
        else {
            try {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        Variables.permission_Read_data );
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        }
        return false;
    }



}
