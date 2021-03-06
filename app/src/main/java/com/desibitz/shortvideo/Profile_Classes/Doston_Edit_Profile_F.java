package com.desibitz.shortvideo.Profile_Classes;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.exifinterface.media.ExifInterface;
import androidx.fragment.app.Fragment;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentTransaction;

import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.desibitz.shortvideo.Main_Menu_Classes.Doston_MainMenuActivity;
import com.desibitz.shortvideo.Main_Menu_Classes.RelateToFragment_OnBack.Doston_RootFragment;
import com.desibitz.shortvideo.R;
import com.desibitz.shortvideo.SimpleClasses.Doston_API_CallBack;
import com.desibitz.shortvideo.SimpleClasses.Doston_ApiRequest;
import com.desibitz.shortvideo.SimpleClasses.Callback;
import com.desibitz.shortvideo.SimpleClasses.Fragment_Callback;
import com.desibitz.shortvideo.SimpleClasses.Doston_Functions;
import com.desibitz.shortvideo.SimpleClasses.Variables;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;
import static com.desibitz.shortvideo.Main_Menu_Classes.Doston_MainMenuFragmentDoston.hasPermissions;


/**
 * A simple {@link Fragment} subclass.
 */
public class Doston_Edit_Profile_F extends Doston_RootFragment implements View.OnClickListener {

    View view;
    Context context;

    public Doston_Edit_Profile_F() {

    }

    Fragment_Callback fragment_callback;
    public Doston_Edit_Profile_F(Fragment_Callback fragment_callback) {
        this.fragment_callback=fragment_callback;
    }

    ImageView profile_image;
    EditText firstname_edit,lastname_edit,user_bio_edit;
    TextView delete_account;
    RadioButton male_btn,female_btn;
    boolean profile_pic_changed = false;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view= inflater.inflate(R.layout.fragment_edit_profile, container, false);
        context=getContext();


        view.findViewById(R.id.Goback).setOnClickListener(this);
        view.findViewById(R.id.save_btn).setOnClickListener(this);
        view.findViewById(R.id.upload_pic_btn).setOnClickListener(this);
        view.findViewById(R.id.delete_account).setOnClickListener(this);



        profile_image=view.findViewById(R.id.profile_image);
        firstname_edit=view.findViewById(R.id.firstname_edit);
        lastname_edit=view.findViewById(R.id.lastname_edit);
        user_bio_edit=view.findViewById(R.id.user_bio_edit);


        firstname_edit.setText(Variables.sharedPreferences.getString(Variables.f_name,""));
        lastname_edit.setText(Variables.sharedPreferences.getString(Variables.l_name,""));

        Picasso.with(context)
                .load(Variables.sharedPreferences.getString(Variables.u_pic,""))
                .placeholder(R.drawable.profile_image_placeholder)
                .resize(200,200)
                .centerCrop()
                .into(profile_image);


        male_btn=view.findViewById(R.id.male_btn);
        female_btn=view.findViewById(R.id.female_btn);



        Call_Api_For_User_Details();

        return view;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.Goback:

                getActivity().onBackPressed();
                break;

            case R.id.save_btn:

                if(Check_Validation()){

                    Call_Api_For_Edit_profile();


                }
                break;

            case R.id.delete_account:
                new android.app.AlertDialog.Builder(new ContextThemeWrapper(context, R.style.AlertDialogCustom))
                        .setTitle("Delete?")
                        .setMessage("Are you sure you want to delete your account? Your data will be lost.")
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Doston_Functions.Show_loader(context,false,false);
                                deleteUserAccount();
                                dialog.dismiss();


                            }
                        }).show();

                break;

            case R.id.upload_pic_btn:
                selectImage();
                break;
        }
    }

    private void deleteUserAccount()
    {
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("fb_id", Variables.sharedPreferences.getString(Variables.u_id,"0"));
            parameters.put("token", Doston_MainMenuActivity.token);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        Doston_ApiRequest.Call_Api(context, Variables.deleteUserAccount, parameters, new Callback() {
            @Override
            public void Responce(String resp) {
                clearDataFromApp();
                Log.d("Response_byUser",""+resp);
//                Parse_user_data(resp);
                Doston_Functions.cancel_loader();
                Toast.makeText(context, "Account deleted successfully", Toast.LENGTH_SHORT).show();


            }
        });
    }

    public void clearDataFromApp(){
        SharedPreferences.Editor editor = Variables.sharedPreferences.edit();
        editor.putString(Variables.u_id, "");
        editor.putString(Variables.u_name, "");
        editor.putString(Variables.u_pic, "");
        editor.putBoolean(Variables.islogin, false);
        editor.commit();

        GoogleSignInOptions gso = new GoogleSignInOptions.
                Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).
                build();

        GoogleSignInClient googleSignInClient= GoogleSignIn.getClient(context,gso);
        googleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    FirebaseAuth.getInstance().signOut(); // very important if you are using firebase.
                    Intent login_intent = new Intent(getActivity(),Doston_MainMenuActivity.class);
                    login_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK); // clear previous task (optional)
                    startActivity(login_intent);
                    getActivity().finish();
                }
            }
        });
    }




    // this method will show the dialog of selete the either take a picture form camera or pick the image from gallary
    private void selectImage() {

        final CharSequence[] options = { "Take Photo", "Choose from Gallery","Cancel" };



        AlertDialog.Builder builder = new AlertDialog.Builder(context,R.style.AlertDialogCustom);

        builder.setTitle("Add Photo!");

        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override

            public void onClick(DialogInterface dialog, int item) {

                if (options[item].equals("Take Photo"))

                {
                    if(check_permissions())
                        openCameraIntent();

                }

                else if (options[item].equals("Choose from Gallery"))

                {

                    if(check_permissions()) {
                        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(intent, 2);
                    }
                }

                else if (options[item].equals("Cancel")) {

                    dialog.dismiss();

                }

            }

        });

        builder.show();

    }


    public boolean check_permissions() {

        String[] PERMISSIONS = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
        };

        if (!hasPermissions(context, PERMISSIONS)) {
            requestPermissions(PERMISSIONS, 2);
        }else {

            return true;
        }

        return false;
    }




    // below three method is related with taking the picture from camera
    private void openCameraIntent() {
        Intent pictureIntent = new Intent(
                MediaStore.ACTION_IMAGE_CAPTURE);
        if(pictureIntent.resolveActivity(getActivity().getPackageManager()) != null){
            //Create a file to store the image
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(context.getApplicationContext(), getActivity().getPackageName()+".fileprovider", photoFile);
                pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(pictureIntent, 1);
            }
        }
    }

    String imageFilePath;
    private File createImageFile() throws IOException {
        String timeStamp =
                new SimpleDateFormat("yyyyMMdd_HHmmss",
                        Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir =
                getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        imageFilePath = image.getAbsolutePath();
        return image;
    }

    public  String getPath(Uri uri ) {
        String result = null;
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver( ).query( uri, proj, null, null, null );
        if(cursor != null){
            if ( cursor.moveToFirst( ) ) {
                int column_index = cursor.getColumnIndexOrThrow( proj[0] );
                result = cursor.getString( column_index );
            }
            cursor.close( );
        }
        if(result == null) {
            result = "Not found";
        }
        return result;
    }




    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            if (requestCode == 1) {
                Matrix matrix = new Matrix();
                try {
                    ExifInterface exif = new ExifInterface(imageFilePath);
                    int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
                    switch (orientation) {
                        case ExifInterface.ORIENTATION_ROTATE_90:
                            matrix.postRotate(90);
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_180:
                            matrix.postRotate(180);
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_270:
                            matrix.postRotate(270);
                            break;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                Uri selectedImage =(Uri.fromFile(new File(imageFilePath)));

                InputStream imageStream = null;
                try {
                    imageStream =getActivity().getContentResolver().openInputStream(selectedImage);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                final Bitmap imagebitmap = BitmapFactory.decodeStream(imageStream);
                Bitmap rotatedBitmap = Bitmap.createBitmap(imagebitmap, 0, 0, imagebitmap.getWidth(), imagebitmap.getHeight(), matrix, true);

                Bitmap  resized = Bitmap.createScaledBitmap(rotatedBitmap,(int)(rotatedBitmap.getWidth()*0.7), (int)(rotatedBitmap.getHeight()*0.7), true);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                resized.compress(Bitmap.CompressFormat.JPEG, 20, baos);

                image_byte_array = baos.toByteArray();
                profile_pic_changed = true;
                profile_image.setImageBitmap(resized);
//                Save_Image();

            }

            else if (requestCode == 2) {
                Uri selectedImage = data.getData();
                InputStream imageStream = null;
                try {
                    imageStream =getActivity().getContentResolver().openInputStream(selectedImage);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                final Bitmap imagebitmap = BitmapFactory.decodeStream(imageStream);

                String path=getPath(selectedImage);
                Matrix matrix = new Matrix();
                ExifInterface exif = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    try {
                        exif = new ExifInterface(path);
                        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
                        switch (orientation) {
                            case ExifInterface.ORIENTATION_ROTATE_90:
                                matrix.postRotate(90);
                                break;
                            case ExifInterface.ORIENTATION_ROTATE_180:
                                matrix.postRotate(180);
                                break;
                            case ExifInterface.ORIENTATION_ROTATE_270:
                                matrix.postRotate(270);
                                break;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                Bitmap rotatedBitmap = Bitmap.createBitmap(imagebitmap, 0, 0, imagebitmap.getWidth(), imagebitmap.getHeight(), matrix, true);


                Bitmap  resized = Bitmap.createScaledBitmap(rotatedBitmap,(int)(rotatedBitmap.getWidth()*0.5), (int)(rotatedBitmap.getHeight()*0.5), true);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                resized.compress(Bitmap.CompressFormat.JPEG, 20, baos);

                image_byte_array = baos.toByteArray();
                profile_pic_changed = true;
                profile_image.setImageBitmap(resized);
//                Save_Image();

            }

        }

    }



    // this will check the validations like none of the field can be the empty
    public boolean Check_Validation(){
        String firstname=firstname_edit.getText().toString();
        String lastname=lastname_edit.getText().toString();

        if(TextUtils.isEmpty(firstname)){
            return false;
        }
        else if(TextUtils.isEmpty(lastname)){
            return false;
        }

        return true;
    }



    byte [] image_byte_array;
    public void Save_Image(){

//        Doston_Functions.Show_loader(context,false,false);

        DatabaseReference reference= FirebaseDatabase.getInstance().getReference();
        String key=reference.push().getKey();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        final StorageReference filelocation = storageReference.child("User_image")
                .child(key + ".jpg");

        filelocation.putBytes(image_byte_array).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){
                    filelocation.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Call_Api_For_image(uri.toString());
//                            Doston_Functions.cancel_loader();
                        }
                    });
                }else {
//                    Doston_Functions.cancel_loader();
                }
            }
        });


    }


    public  void Call_Api_For_image(final String image_link) {



        JSONObject parameters = new JSONObject();
        try {
            parameters.put("fb_id", Variables.sharedPreferences.getString(Variables.u_id,"0"));
            parameters.put("image_link",image_link);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Doston_ApiRequest.Call_Api(context, Variables.uploadImage, parameters, new Callback() {
            @Override
            public void Responce(String resp) {
                Doston_Functions.cancel_loader();
                try {
                    JSONObject response=new JSONObject(resp);
                    String code=response.optString("code");
                    if(code.equals("200")){

                        Variables.sharedPreferences.edit().putString(Variables.u_pic,image_link).commit();
                        Doston_Profile_F.pic_url=image_link;
                        Variables.user_pic=image_link;

                        Picasso.with(context)
                                .load(Doston_Profile_F.pic_url)
                                .placeholder(context.getResources().getDrawable(R.drawable.profile_image_placeholder))
                                .resize(200,200).centerCrop().into(profile_image);

                        Doston_Functions.cancel_loader();
                        getActivity().onBackPressed();
//                        Toast.makeText(context, "Profile Update Successfully", Toast.LENGTH_SHORT).show();

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });



    }




    // this will update the latest info of user in database
    public  void Call_Api_For_Edit_profile() {

        Doston_Functions.Show_loader(context,false,false);

        JSONObject parameters = new JSONObject();
        try {
            parameters.put("fb_id", Variables.sharedPreferences.getString(Variables.u_id,"0"));
            parameters.put("first_name",firstname_edit.getText().toString());
            parameters.put("last_name",lastname_edit.getText().toString());

            if(male_btn.isChecked()){
                parameters.put("gender","Male");

            }else if(female_btn.isChecked()){
                parameters.put("gender","Female");
            }

            parameters.put("bio",user_bio_edit.getText().toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Doston_ApiRequest.Call_Api(context, Variables.edit_profile, parameters, new Callback() {
            @Override
            public void Responce(String resp) {

                try {
                    JSONObject response=new JSONObject(resp);
                    String code=response.optString("code");
                    if(code.equals("200")) {

                        SharedPreferences.Editor editor = Variables.sharedPreferences.edit();

                        editor.putString(Variables.f_name, firstname_edit.getText().toString());
                        editor.putString(Variables.l_name, lastname_edit.getText().toString());
                        editor.commit();

                        Variables.user_name = firstname_edit.getText().toString() + " " + lastname_edit.getText().toString();
                        if(profile_pic_changed)
                        {
                            Save_Image();
                        }
                        else
                        {
                            Doston_Functions.cancel_loader();
                            getActivity().onBackPressed();
                        }
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        ft.detach(Doston_Edit_Profile_F.this).attach(Doston_Edit_Profile_F.this).commit();
                        Toast.makeText(context, "Profile Update Successfully", Toast.LENGTH_SHORT).show();

                    }

                    } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }





    // this will get the user data and parse the data and show the data into views
    public void Call_Api_For_User_Details(){
        Doston_Functions.Show_loader(getActivity(),false,false);
        Doston_Functions.Call_Api_For_Get_User_data(getActivity(),
                Variables.sharedPreferences.getString(Variables.u_id, ""),
                new Doston_API_CallBack() {
                    @Override
                    public void ArrayData(ArrayList arrayList) {

                    }

                    @Override
                    public void OnSuccess(String responce) {
                        Doston_Functions.cancel_loader();
                        Parse_user_data(responce);
                    }

                    @Override
                    public void OnFail(String responce) {

                    }
                });
    }

    public void Parse_user_data(String responce){
        try {
            JSONObject jsonObject=new JSONObject(responce);

            String code=jsonObject.optString("code");

            if(code.equals("200")) {
                JSONArray msg = jsonObject.optJSONArray("msg");
                JSONObject data = msg.getJSONObject(0);

                firstname_edit.setText(data.optString("first_name"));
                lastname_edit.setText(data.optString("last_name"));

                String picture = data.optString("profile_pic");

                Picasso.with(context)
                        .load(picture)
                        .placeholder(R.drawable.profile_image_placeholder)
                        .into(profile_image);

                String gender = data.optString("gender");
                if (gender.equals("Male")) {
                    male_btn.setChecked(true);
                } else {
                    female_btn.setChecked(true);
                }

                user_bio_edit.setText(data.optString("bio"));
            }
            else {
                Toast.makeText(context, ""+jsonObject.optString("msg"), Toast.LENGTH_SHORT).show();

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();

        if(fragment_callback!=null)
            fragment_callback.Responce(new Bundle());
    }
}
