package com.desibitz.shortvideo.Profile_Classes;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.desibitz.shortvideo.Doston_See_Full_Image_F;
import com.desibitz.shortvideo.Following_Classes.Doston_Following_F;
import com.desibitz.shortvideo.Main_Menu_Classes.RelateToFragment_OnBack.Doston_RootFragment;
import com.desibitz.shortvideo.MessageChat_Classes.Doston_Chat_Activity;
import com.desibitz.shortvideo.Profile_Classes.Liked_Videos.Doston_Liked_Video_F;
import com.desibitz.shortvideo.Profile_Classes.UserVideos.Doston_UserVideo_F;
import com.desibitz.shortvideo.R;
import com.desibitz.shortvideo.SimpleClasses.Callback;
import com.desibitz.shortvideo.SimpleClasses.Doston_API_CallBack;
import com.desibitz.shortvideo.SimpleClasses.Doston_ApiRequest;
import com.desibitz.shortvideo.SimpleClasses.Doston_Functions;
import com.desibitz.shortvideo.SimpleClasses.Fragment_Callback;
import com.desibitz.shortvideo.SimpleClasses.Variables;
import com.google.android.material.tabs.TabLayout;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


// This is the profile screen which is show in 5 tab as well as it is also call
// when we see the profile of other users

public class Doston_Profile_F extends Doston_RootFragment implements View.OnClickListener {

    public static String pic_url;
    public TextView username, fullname, video_count_txt;
    public ImageView imageView;
    public TextView follow_count_txt, fans_count_txt, heart_count_txt, user_bios;
    public boolean isdataload = false;
    public String follow_status = "0";
    protected TabLayout tabLayout;
    protected ViewPager pager;
    View view;
    Context context;
    //   public Button follow_unfollow_btn;
//LinearLayout follow_unfollow_ll;
//    ImageView follow_unfollow_img;
    Button follow_unfollow_txt;
    ImageView back_btn, setting_btn;
    String user_id, user_name, user_pic;
    Bundle bundle;
    RelativeLayout tabs_main_layout;
    LinearLayout top_layout;
    Fragment_Callback fragment_callback;
    boolean is_run_first_time = false;
    private ViewPagerAdapter adapter;


    public Doston_Profile_F() {

    }


    @SuppressLint("ValidFragment")
    public Doston_Profile_F(Fragment_Callback fragment_callback) {
        this.fragment_callback = fragment_callback;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_profile, container, false);
        context = getContext();


        bundle = getArguments();
        if (bundle != null) {
            user_id = bundle.getString("user_id");
            user_name = bundle.getString("user_name");
            user_pic = bundle.getString("user_pic");
        }


        return init();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.user_image:
                OpenfullsizeImage(pic_url);
                break;

            case R.id.follow_unfollow_txt:

                if (Variables.sharedPreferences.getBoolean(Variables.islogin, false))
                    Follow_unFollow_User();
                else
                    Toast.makeText(context, "Please login in to app", Toast.LENGTH_SHORT).show();

                break;

            case R.id.setting_btn:
                Open_Setting();
                break;

            case R.id.following_layout:
                Open_Following();
                break;

            case R.id.fans_layout:
                Open_Followers();
                break;

            case R.id.back_btn:
                getActivity().onBackPressed();
                break;
        }
    }

    public View init() {

        username = view.findViewById(R.id.username);
        imageView = view.findViewById(R.id.user_image);
        imageView.setOnClickListener(this);
        fullname = view.findViewById(R.id.fullname);
        video_count_txt = view.findViewById(R.id.video_count_txt);

        follow_count_txt = view.findViewById(R.id.follow_count_txt);
        fans_count_txt = view.findViewById(R.id.fan_count_txt);
        heart_count_txt = view.findViewById(R.id.heart_count_txt);


        setting_btn = view.findViewById(R.id.setting_btn);
        setting_btn.setOnClickListener(this);

        back_btn = view.findViewById(R.id.back_btn);
        back_btn.setOnClickListener(this);

//        follow_unfollow_btn=view.findViewById(R.id.follow_unfollow_btn);
//        follow_unfollow_ll.setOnClickListener(this);

        user_bios = view.findViewById(R.id.user_bios);
//        follow_unfollow_ll=view.findViewById(R.id.follow_unfollow_ll);
//        follow_unfollow_img=view.findViewById(R.id.follow_unfollow_img);
        follow_unfollow_txt = view.findViewById(R.id.follow_unfollow_txt);
//        follow_unfollow_ll.setOnClickListener(this);
        follow_unfollow_txt.setOnClickListener(this);

        tabLayout = (TabLayout) view.findViewById(R.id.tabs);
        pager = view.findViewById(R.id.pager);
        pager.setOffscreenPageLimit(2);

        adapter = new ViewPagerAdapter(getResources(), getChildFragmentManager());
        pager.setAdapter(adapter);
        tabLayout.setupWithViewPager(pager);

        setupTabIcons();


        tabs_main_layout = view.findViewById(R.id.tabs_main_layout);
        top_layout = view.findViewById(R.id.top_layout);


        ViewTreeObserver observer = top_layout.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {

                final int height = top_layout.getMeasuredHeight();

                top_layout.getViewTreeObserver().removeGlobalOnLayoutListener(
                        this);

                ViewTreeObserver observer = tabs_main_layout.getViewTreeObserver();
                observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

                    @Override
                    public void onGlobalLayout() {

                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) tabs_main_layout.getLayoutParams();
                        params.height = (int) (tabs_main_layout.getMeasuredHeight() + height);
                        tabs_main_layout.setLayoutParams(params);
                        tabs_main_layout.getViewTreeObserver().removeGlobalOnLayoutListener(
                                this);

                    }
                });

            }
        });


        view.findViewById(R.id.following_layout).setOnClickListener(this);
        view.findViewById(R.id.fans_layout).setOnClickListener(this);

        isdataload = true;


        Call_Api_For_get_Allvideos();


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (is_run_first_time) {

            Call_Api_For_get_Allvideos();

        }

    }

    private void setupTabIcons() {

        View view1 = LayoutInflater.from(context).inflate(R.layout.item_tabs_profile_menu, null);
        TextView imageView1 = view1.findViewById(R.id.image);
        imageView1.setText("Videos");
        imageView1.setTextColor(Color.parseColor("#FFFFFF"));
//        imageView1.setImageDrawable(getResources().getDrawable(R.drawable.ic_my_video_color));
        tabLayout.getTabAt(0).setCustomView(view1);

        View view2 = LayoutInflater.from(context).inflate(R.layout.item_tabs_profile_menu, null);
        TextView imageView2 = view2.findViewById(R.id.image);
        imageView2.setText("Liked Videos");
        imageView2.setTextColor(Color.parseColor("#848484"));
//        imageView2.setImageDrawable(getResources().getDrawable(R.drawable.ic_liked_video_gray));
        tabLayout.getTabAt(1).setCustomView(view2);


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {


            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                View v = tab.getCustomView();
                TextView image = v.findViewById(R.id.image);

                switch (tab.getPosition()) {
                    case 0:

                        image.setTextColor(Color.parseColor("#FFFFFF"));
                        break;

                    case 1:
                        image.setTextColor(Color.parseColor("#FFFFFF"));
                        break;
                }
                tab.setCustomView(v);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                View v = tab.getCustomView();
                TextView image = v.findViewById(R.id.image);

                switch (tab.getPosition()) {
                    case 0:
                        image.setTextColor(Color.parseColor("#848484"));
                        break;
                    case 1:
                        image.setTextColor(Color.parseColor("#848484"));
                        break;
                }

                tab.setCustomView(v);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }

        });


    }

    private void Call_Api_For_get_Allvideos() {

        if (bundle == null) {
            user_id = Variables.sharedPreferences.getString(Variables.u_id, "0");
        }

        JSONObject parameters = new JSONObject();
        try {
            parameters.put("my_fb_id", Variables.sharedPreferences.getString(Variables.u_id, ""));
            parameters.put("fb_id", user_id);

        } catch (JSONException e) {
            e.printStackTrace();
        }


        Doston_ApiRequest.Call_Api(context, Variables.showMyAllVideos, parameters, new Callback() {
            @Override
            public void Responce(String resp) {
                is_run_first_time = true;
                Parse_data(resp);
            }
        });


    }

    public void Parse_data(String responce) {


        try {
            JSONObject jsonObject = new JSONObject(responce);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {
                JSONArray msgArray = jsonObject.getJSONArray("msg");

                JSONObject data = msgArray.getJSONObject(0);
                JSONObject user_info = data.optJSONObject("user_info");
                fullname.setText(user_info.optString("first_name") + " " + user_info.optString("last_name"));
                username.setText(user_info.optString("username"));
                user_bios.setText(user_info.optString("bio"));
                Doston_Profile_F.pic_url = user_info.optString("profile_pic");
                Picasso.with(context)
                        .load(Doston_Profile_F.pic_url)
                        .placeholder(context.getResources().getDrawable(R.drawable.profile_image_placeholder))
                        .resize(200, 200).centerCrop().into(imageView);

                follow_count_txt.setText(data.optString("total_following"));
                fans_count_txt.setText(data.optString("total_fans"));
                heart_count_txt.setText(data.optString("total_heart"));

                Log.d("UserPrefdata",""+data.optString("fb_id")+"_____________________"+Variables.sharedPreferences.getString(Variables.u_id, ""));
                if (!data.optString("fb_id").
                        equals(Variables.sharedPreferences.getString(Variables.u_id, ""))) {
                    follow_unfollow_txt.setVisibility(View.VISIBLE);
//                    follow_unfollow_btn.setVisibility(View.VISIBLE);
                    JSONObject follow_Status = data.optJSONObject("follow_Status");
                    follow_unfollow_txt.setText(follow_Status.optString("follow_status_button"));
                    if(follow_Status.optString("follow_status_button").equalsIgnoreCase("unfollow"))
                    {
                        follow_unfollow_txt.setBackgroundResource(R.drawable.unfollow_btn_bg);
                    }
                    follow_status = follow_Status.optString("follow");
//                    if(follow_Status.equals("1"))
////                    {
////                        follow_unfollow_txt.setText("Follow");
////                        follow_unfollow_txt.setBackgroundResource(R.drawable.follow_btn_bg);
////                        follow_unfollow_txt.setTextColor(ContextCompat.getColor(context, R.color.white));//                            follow_unfollow_txt
////                    }
////                    else {
////                        follow_unfollow_txt.setText("Unfollow");
////                        follow_unfollow_txt.setBackgroundResource(R.drawable.unfollow_btn_bg);
////                        follow_unfollow_txt.setTextColor(ContextCompat.getColor(context, R.color.white));//
////                    }
                }
                else
                {
                    follow_unfollow_txt.setVisibility(View.GONE);
                }


                JSONArray user_videos = data.getJSONArray("user_videos");
                if (!user_videos.toString().equals("[" + "0" + "]")) {
                    video_count_txt.setText(Doston_Functions.prettyCount(user_videos.length() + ""));

                }


            } else {
                Toast.makeText(context, "" + jsonObject.optString("msg"), Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    public void Open_Setting() {

        Open_Chat_F();

    }

    public void Follow_unFollow_User() {

        final String send_status;
        if (follow_status.equals("0")) {
            send_status = "1";
        } else {
            send_status = "0";

        }

        Doston_Functions.Call_Api_For_Follow_or_unFollow(getActivity(),
                Variables.sharedPreferences.getString(Variables.u_id, ""),
                user_id,
                send_status,
                new Doston_API_CallBack() {
                    @Override
                    public void ArrayData(ArrayList arrayList) {


                    }

                    @Override
                    public void OnSuccess(String responce) {

                        if (send_status.equals("1")) {
//                            follow_unfollow_txt.setText("Unfollow");
//                            follow_unfollow_txt.setTextColor(Color.parseColor("#a4a4a4"));
                            follow_unfollow_txt.setText("Unfollow");
                            follow_unfollow_txt.setBackgroundResource(R.drawable.unfollow_btn_bg);
                            follow_unfollow_txt.setTextColor(ContextCompat.getColor(context, R.color.white));
//                            follow_unfollow_img.setImageResource(R.drawable.ic_unfollow);
//                            follow_unfollow_btn.setTextColor(Color.parseColor("#a4a4a4"));
                            //a4a4a4
                            follow_status = "1";

                        } else if (send_status.equals("0")) {
                            follow_unfollow_txt.setText("Follow");
                            follow_unfollow_txt.setBackgroundResource(R.drawable.follow_btn_bg);
                            follow_unfollow_txt.setTextColor(ContextCompat.getColor(context, R.color.white));//                            follow_unfollow_txt.setText("Follow");
//                            follow_unfollow_txt.setTextColor(Color.parseColor("#FFFFFF"));


//                            follow_unfollow_img.setImageResource(R.drawable.ic_follow);
//                            follow_unfollow_btn.setTextColor(Color.parseColor("#000000"));
                            //000000
                            follow_status = "0";
                        }

                        Call_Api_For_get_Allvideos();
                    }

                    @Override
                    public void OnFail(String responce) {

                    }

                });


    }

    //this method will get the big size of profile image.
    public void OpenfullsizeImage(String url) {
        Doston_See_Full_Image_F see_image_f = new Doston_See_Full_Image_F();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        Bundle args = new Bundle();
        args.putSerializable("image_url", url);
        see_image_f.setArguments(args);
        transaction.addToBackStack(null);

        View view = getActivity().findViewById(R.id.MainMenuFragment);
        if (view != null)
            transaction.replace(R.id.MainMenuFragment, see_image_f).commit();
        else
            transaction.replace(R.id.Profile_F, see_image_f).commit();


    }

    public void Open_Chat_F() {

        Doston_Chat_Activity dostonChat_activity = new Doston_Chat_Activity();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.in_from_bottom, R.anim.out_to_top, R.anim.in_from_top, R.anim.out_from_bottom);
        Bundle args = new Bundle();
        args.putString("user_id", user_id);
        args.putString("user_name", user_name);
        args.putString("user_pic", user_pic);
        dostonChat_activity.setArguments(args);
        transaction.addToBackStack(null);

        View view = getActivity().findViewById(R.id.MainMenuFragment);
        if (view != null)
            transaction.replace(R.id.MainMenuFragment, dostonChat_activity).commit();
        else
            transaction.replace(R.id.Profile_F, dostonChat_activity).commit();


    }

    public void Open_Following() {

        Doston_Following_F dostonFollowing_f = new Doston_Following_F();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.in_from_bottom, R.anim.out_to_top, R.anim.in_from_top, R.anim.out_from_bottom);
        Bundle args = new Bundle();
        args.putString("id", user_id);
        args.putString("from_where", "following");
        dostonFollowing_f.setArguments(args);
        transaction.addToBackStack(null);


        View view = getActivity().findViewById(R.id.MainMenuFragment);

        if (view != null)
            transaction.replace(R.id.MainMenuFragment, dostonFollowing_f).commit();
        else
            transaction.replace(R.id.Profile_F, dostonFollowing_f).commit();


    }

    public void Open_Followers() {
        Doston_Following_F dostonFollowing_f = new Doston_Following_F();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.in_from_bottom, R.anim.out_to_top, R.anim.in_from_top, R.anim.out_from_bottom);
        Bundle args = new Bundle();
        args.putString("id", user_id);
        args.putString("from_where", "fan");
        dostonFollowing_f.setArguments(args);
        transaction.addToBackStack(null);


        View view = getActivity().findViewById(R.id.MainMenuFragment);

        if (view != null)
            transaction.replace(R.id.MainMenuFragment, dostonFollowing_f).commit();
        else
            transaction.replace(R.id.Profile_F, dostonFollowing_f).commit();


    }

    @Override
    public void onDetach() {
        super.onDetach();


        if (fragment_callback != null)
            fragment_callback.Responce(new Bundle());

        Doston_Functions.deleteCache(context);

    }

    class ViewPagerAdapter extends FragmentPagerAdapter {

        private final Resources resources;

        SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();


        public ViewPagerAdapter(final Resources resources, FragmentManager fm) {
            super(fm);
            this.resources = resources;
        }

        @Override
        public Fragment getItem(int position) {
            final Fragment result;
            switch (position) {
                case 0:
                    result = new Doston_UserVideo_F(user_id);
                    break;
                case 1:
                    result = new Doston_Liked_Video_F(user_id);
                    break;

                default:
                    result = null;
                    break;
            }

            return result;
        }

        @Override
        public int getCount() {
            return 2;
        }


        @Override
        public CharSequence getPageTitle(final int position) {
            return null;
        }


        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            registeredFragments.put(position, fragment);
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            registeredFragments.remove(position);
            super.destroyItem(container, position, object);
        }


        /**
         * Get the Fragment by position
         *
         * @param position tab position of the fragment
         * @return
         */
        public Fragment getRegisteredFragment(int position) {
            return registeredFragments.get(position);
        }


    }


}
