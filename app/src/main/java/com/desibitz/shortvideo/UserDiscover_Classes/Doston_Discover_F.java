package com.desibitz.shortvideo.UserDiscover_Classes;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.desibitz.shortvideo.Home_Fragement_Classes.Doston_Home_Get_Set;
import com.desibitz.shortvideo.Main_Menu_Classes.RelateToFragment_OnBack.Doston_RootFragment;
import com.desibitz.shortvideo.R;
import com.desibitz.shortvideo.SimpleClasses.Doston_ApiRequest;
import com.desibitz.shortvideo.SimpleClasses.Callback;
import com.desibitz.shortvideo.SimpleClasses.Doston_Functions;
import com.desibitz.shortvideo.SimpleClasses.Variables;
import com.desibitz.shortvideo.Doston_WatchVideos.Doston_WatchVideos_F;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class Doston_Discover_F extends Doston_RootFragment {

    View view;
    Context context;

    RecyclerView recyclerView;
    EditText search_edit;
    TextView refresh_btn_no_internet;
    LinearLayout no_internet_ll;

    SwipeRefreshLayout swiperefresh;

    public Doston_Discover_F() {
        // Required empty public constructor
    }

    ArrayList<Doston_Discover_Get_Set> datalist;
    ArrayList<Doston_Discover_user_Model> dataUserList;

    Doston_Discover_Adapter adapter;
    Doston_DiscoverByUser_Adapter adapter_user;


    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view= inflater.inflate(R.layout.fragment_discover, container, false);
        context=getContext();


        datalist=new ArrayList<>();
        dataUserList=new ArrayList<>();

        no_internet_ll = view.findViewById(R.id.no_internet_ll);
        refresh_btn_no_internet = view.findViewById(R.id.refresh_btn_no_internet);

        recyclerView = (RecyclerView) view.findViewById(R.id.recylerview);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        adapter=new Doston_Discover_Adapter(context, datalist, new Doston_Discover_Adapter.OnItemClickListener() {
            @Override
            public void onItemClick(ArrayList<Doston_Home_Get_Set> datalist, int postion) {
                OpenWatchVideo(postion,datalist);
            }
        });
        adapter_user = new Doston_DiscoverByUser_Adapter(context,getFragmentManager(),dataUserList);





        recyclerView.setAdapter(adapter);


        search_edit=view.findViewById(R.id.search_edit);
        search_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

//                String query=search_edit.getText().toString();
//                if(adapter!=null) {
//                    adapter.getFilter().filter(query);
//                }
                if(!search_edit.getText().toString().toLowerCase().equals(""))
                {
                    Call_Api_For_SerachUser(search_edit.getText().toString().toLowerCase());
                }


            }

            @Override
            public void afterTextChanged(Editable s) {
//                Call_Api_For_SerachUser(search_edit.getText().toString().toLowerCase());
            }
        });
        search_edit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if(!search_edit.getText().toString().toLowerCase().equals("")) {
                        Call_Api_For_SerachUser(search_edit.getText().toString().toLowerCase());
                    }
                    return true;
                }
                return false;
            }
        });
//test
        swiperefresh=view.findViewById(R.id.swiperefresh);
        swiperefresh.setColorSchemeResources(R.color.black);
        swiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                if(search_edit.getText().toString().equals(""))
                {
                    Call_Api_For_serachPopulerUser();
                }
                else
                {
                    Call_Api_For_SerachUser(search_edit.getText().toString().toLowerCase());
                }
            }
        });
//        Call_Api_For_get_Allvideos();
        refresh_btn_no_internet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(Doston_Functions.isInternetOn(context))
                {
                    no_internet_ll.setVisibility(View.GONE);
                    if(search_edit.getText().toString().equals(""))
                    {
                        Call_Api_For_serachPopulerUser();
                    }
                    else
                    {
                        Call_Api_For_SerachUser(search_edit.getText().toString().toLowerCase());
                    }
                }
                else
                {
                    no_internet_ll.setVisibility(View.VISIBLE);
                }

            }
        });
        if(Doston_Functions.isInternetOn(context))
        {
            no_internet_ll.setVisibility(View.GONE);
            Call_Api_For_serachPopulerUser();
        }
        else
        {
            no_internet_ll.setVisibility(View.VISIBLE);
        }

        return view;
    }



    // Bottom two function will get the Discover videos
    // from api and parse the json data which is shown in Discover tab

    private void Call_Api_For_SerachUser(String stringToFind){
//        dataUserList = new ArrayList<>();

        dataUserList.clear();
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("username", stringToFind);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        //Log.d("resp",parameters.toString());

        Doston_ApiRequest.Call_Api(context, Variables.searchByUsername, parameters, new Callback() {
            @Override
            public void Responce(String resp) {
                Log.d("Response_byUser",""+resp);
                dataUserList.clear();
                Parse_data_user(resp);
                swiperefresh.setRefreshing(false);
            }

        });


    }
    private void Call_Api_For_serachPopulerUser(){
//        dataUserList = new ArrayList<>();

        dataUserList.clear();
//        JSONObject parameters = new JSONObject();
//        try {
//            parameters.put("username", stringToFind);
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }

//        //Log.d("resp",parameters.toString());

        Doston_ApiRequest.Call_Api(context, Variables.searchPopulerUser, null, new Callback() {
            @Override
            public void Responce(String resp) {
                Log.d("Response_byUser",""+resp);

                Parse_data_user(resp);
                swiperefresh.setRefreshing(false);
            }
        });


    }

    private void Call_Api_For_get_Allvideos() {

        JSONObject parameters = new JSONObject();
        try {
            parameters.put("fb_id", Variables.sharedPreferences.getString(Variables.u_id,"0"));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        //Log.d("resp",parameters.toString());

        Doston_ApiRequest.Call_Api(context, Variables.discover, parameters, new Callback() {
            @Override
            public void Responce(String resp) {
                Parse_data(resp);
                swiperefresh.setRefreshing(false);
            }
        });



    }



    public void Parse_data_user(String responce){

//        dataUserList.clear();

        try {
            JSONObject jsonObject=new JSONObject(responce);
            String code=jsonObject.optString("code");
            Doston_Discover_user_Model dostonDiscover_user_model = null;
            if(code.equals("200")){
                JSONArray msgArray=jsonObject.getJSONArray("msg");
                for (int d=0;d<msgArray.length();d++) {

                    dostonDiscover_user_model =new Doston_Discover_user_Model();
//                    JSONObject discover_object=msgArray.optJSONObject(d);
                    JSONArray discoverArray=msgArray.getJSONArray(d);
                    for (int j=0;j<discoverArray.length();j++) {
                        JSONObject discover_object = discoverArray.optJSONObject(j);
                        dostonDiscover_user_model.setFb_id(discover_object.getString("fb_id"));
                        dostonDiscover_user_model.setUsername(discover_object.getString("username"));
                        dostonDiscover_user_model.setFirst_name(discover_object.getString("first_name"));
                        dostonDiscover_user_model.setLast_name(discover_object.getString("last_name"));
                        dostonDiscover_user_model.setProfile_pic(discover_object.getString("profile_pic"));
                        dostonDiscover_user_model.setVideoCount(discover_object.getString("videoCount"));
                        dostonDiscover_user_model.setFollowersCount(discover_object.getString("followersCount"));
                        dataUserList.add(dostonDiscover_user_model);
                    }

                    }

//                    discover_get_set.arrayList=video_list;


                    recyclerView.setAdapter(adapter_user);
                adapter_user.notifyDataSetChanged();

            }else {
                Toast.makeText(context, ""+jsonObject.optString("msg"), Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    public void Parse_data(String responce){

        datalist.clear();

        try {
            JSONObject jsonObject=new JSONObject(responce);
            String code=jsonObject.optString("code");
            if(code.equals("200")){
                JSONArray msgArray=jsonObject.getJSONArray("msg");
                for (int d=0;d<msgArray.length();d++) {

                    Doston_Discover_Get_Set dostonDiscover_get_set =new Doston_Discover_Get_Set();
                    JSONObject discover_object=msgArray.optJSONObject(d);
                    dostonDiscover_get_set.title=discover_object.optString("section_name");

                    JSONArray video_array=discover_object.optJSONArray("sections_videos");

                    ArrayList<Doston_Home_Get_Set> video_list = new ArrayList<>();
                    for (int i = 0; i < video_array.length(); i++) {
                        JSONObject itemdata = video_array.optJSONObject(i);
                        Doston_Home_Get_Set item = new Doston_Home_Get_Set();


                        JSONObject user_info = itemdata.optJSONObject("user_info");
                        item.fb_id = user_info.optString("fb_id");
                        item.first_name = user_info.optString("first_name");
                        item.last_name = user_info.optString("last_name");
                        item.profile_pic = user_info.optString("profile_pic");
                        //test
                        JSONObject count = itemdata.optJSONObject("count");
                        item.like_count = count.optString("like_count");
                        item.video_comment_count = count.optString("video_comment_count");

                        JSONObject sound_data=itemdata.optJSONObject("sound");
                        item.sound_id=sound_data.optString("id");
                        item.sound_name=sound_data.optString("sound_name");
                        item.sound_pic=sound_data.optString("thum");


                        item.video_id = itemdata.optString("id");
                        item.liked = itemdata.optString("liked");
                        item.video_url = Variables.base_url + itemdata.optString("video");
                        item.thum = Variables.base_url + itemdata.optString("thum");
                        item.gif =Variables.base_url+itemdata.optString("gif");
                        item.created_date = itemdata.optString("created");
                        item.video_description=itemdata.optString("description");

                        video_list.add(item);
                    }

                    dostonDiscover_get_set.arrayList=video_list;

                    datalist.add(dostonDiscover_get_set);

                }

                adapter.notifyDataSetChanged();

            }else {
                Toast.makeText(context, ""+jsonObject.optString("msg"), Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }



    // When you click on any Video a new activity is open which will play the Clicked video
    private void OpenWatchVideo(int postion,ArrayList<Doston_Home_Get_Set> data_list) {

        Intent intent=new Intent(getActivity(), Doston_WatchVideos_F.class);
        intent.putExtra("arraylist", data_list);
        intent.putExtra("position",postion);
        startActivity(intent);

    }




}
