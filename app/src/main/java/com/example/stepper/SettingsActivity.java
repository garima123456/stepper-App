package com.example.stepper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import pub.devrel.easypermissions.EasyPermissions;

public class SettingsActivity extends AppCompatActivity {
    private Button saveBtn;
    private EditText userNameET, userBioET;
    private ImageView profileImageView;
    private static int GalleyPick=1;
    private Uri ImageUri;
    private StorageReference userProfileImageRef;
    private String downloadUri;
    private DatabaseReference userRef;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        userProfileImageRef= FirebaseStorage.getInstance().getReference().child("Profile Images");
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        userNameET=findViewById(R.id.username_settings);
        userBioET=findViewById(R.id.bio_settings);
        profileImageView=findViewById(R.id.settings_profile_image);
        saveBtn=findViewById(R.id.save_settings_btn);
        progressBar = findViewById(R.id.progressBar);
        //progressBar=new ProgressBar(this);

        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent galleryIntent=new Intent();
//                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
//                galleryIntent.setType("image/*");
//                startActivityForResult(galleryIntent,GalleyPick);
                Intent openGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                openGalleryIntent.setType("image/*");
                startActivityForResult(openGalleryIntent,1000);
            }
        });
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveUserData();

            }
        });
        Log.d("retrieveUserInfo","retrieveUserInfo");
        retrieveUserInfo();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode==1000 && requestCode==RESULT_OK && data!=null)
//        {
//            ImageUri=data.getData();
//            profileImageView.setImageURI(ImageUri);
//        }
//    }
        if (requestCode == 1000){
            //String[] galleryPermissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
            Log.d("request code 1000","request code 1000");
            if(resultCode == Activity.RESULT_OK){
                ImageUri = data.getData();
                profileImageView.setImageURI(ImageUri);
                //uploadImageToFirebase(imageUri);
            }
        }
    }

    private void saveUserData()
    {
        final String getUserName=userNameET.getText().toString();
        final String getUserStatus=userBioET.getText().toString();
        if (ImageUri==null)
        {
            Log.d("saveUserData 1","saveUserData 1");
           userRef.addValueEventListener(new ValueEventListener() {
               @Override
               public void onDataChange(@NonNull DataSnapshot snapshot) {
                   if(snapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).hasChild("image")){
                       //Toast.makeText(SettingsActivity.this, "image is mandatory", Toast.LENGTH_SHORT).show();
                        saveInfoOnlyWithoutAnyName();
                   }
                   else
                       {
                           Toast.makeText(SettingsActivity.this, "Plaese select image first", Toast.LENGTH_SHORT).show();
                       }
               }
               @Override
               public void onCancelled(@NonNull DatabaseError error) {
               }
           }) ;
        }
        else  if (getUserName.equals("")){
            Log.d("saveUserData 2","saveUserData 2");
            Toast.makeText(this, "user name is mandatory", Toast.LENGTH_SHORT).show();
        }
        else  if (getUserStatus.equals("")){
            Log.d("saveUserData 3","saveUserData 3");
            Toast.makeText(this, "bio is mandatory", Toast.LENGTH_SHORT).show();
        }
        else{
            //progressBar.setAccessibilityPaneTitle("Account Settings");
            //progressBar.setProgress(Integer.parseInt("Please Wait"));
            //progressBar.showContextMenu();
            Log.d("saveUserData 4","saveUserData 4");
            Log.d("getUserName",getUserName);
            Log.d("status",getUserStatus);

            final StorageReference filePath=userProfileImageRef.
                    child(FirebaseAuth.getInstance().getCurrentUser().getUid());
             UploadTask uploadTask=filePath.putFile(ImageUri);
            downloadUri=filePath.getDownloadUrl().toString() ;
            Log.d("downUri",downloadUri);

            Log.d("saveUserData 4","saveUserData %$RGFdghjkl,");
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception
                {

                    if (!task.isSuccessful()){
                        Log.d("statement","above filePath.getDownloadUrl");
                        throw task.getException();
                    }
                    //downloadUri=filePath.getDownloadUrl().toString() ;
                    Log.d("statement","below filePath.getDownloadUrl");
                return filePath.getDownloadUrl();
                }

            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                private Object Error;

                @Override
                public void onComplete(@NonNull Task<Uri> task)
                {Log.d("saveUserData 4","task successful outer");
                    Log.d("getUserName",getUserName);
                    Log.d("status",getUserStatus);
                    Log.d("image",downloadUri);

                  if (task.isSuccessful()) {
                      Log.d("saveUserData 4","task successful");
                    downloadUri = task.getResult().toString();
                    HashMap<String,Object > profileMap=new HashMap<>();
                    profileMap.put("uid",FirebaseAuth.getInstance().getCurrentUser().getUid());
                    profileMap.put("name",getUserName);
                    Log.d("getUserName",getUserName);
                      profileMap.put("status",getUserStatus);
                      profileMap.put("image",downloadUri);
                      userRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).
                              updateChildren(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                          @Override
                          public void onComplete(@NonNull Task<Void> task)
                          {
                              if (task.isSuccessful())
                              {
                                  Toast.makeText(SettingsActivity.this, "Image Uploaded", Toast.LENGTH_SHORT).show();
                                  Intent intent=new Intent(SettingsActivity.this,ContactsActivity.class);
                                  startActivity(intent);
                                  Toast.makeText(SettingsActivity.this, "Profile settings has been updated", Toast.LENGTH_SHORT).show();
                              }
                              else {
                                  Toast.makeText(SettingsActivity.this, "Error :  Profile settings has not been updated", Toast.LENGTH_SHORT).show();
                              }
                          }
                      });
                }
                  else{
                      Log.d("task unsuccessful","error"+Error);
                  }
                }
            });
        }
        progressBar.setVisibility(View.VISIBLE);
    }

    //@RequiresApi(api = Build.VERSION_CODES.P)
    private void saveInfoOnlyWithoutAnyName() {
        final String getUserName=userNameET.getText().toString();
        final String getUserStatus=userBioET.getText().toString();


        if (getUserName.equals("")){
            Toast.makeText(this, "user name is mandatory", Toast.LENGTH_SHORT).show();
        }
        else  if (getUserStatus.equals("")){
            Toast.makeText(this, "bio is mandatory", Toast.LENGTH_SHORT).show();
        }
        else{
            //progressBar.setVisibility(View.VISIBLE);
            //progressBar.setAccessibilityPaneTitle("Account Settings");
            //progressBar.setProgress(Integer.parseInt("Please Wait"));
            Log.d("hashmap start","inside else");
            HashMap<String,Object > profileMap=new HashMap<>();
            profileMap.put("uid",FirebaseAuth.getInstance().getCurrentUser().getUid());
            profileMap.put("name",getUserName);
            profileMap.put("status",getUserStatus);
            Log.d("hashmap start","inside else ending");
            userRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).
                    updateChildren(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task)
                {
                    if (task.isSuccessful())
                    {
                        Intent intent=new Intent(SettingsActivity.this,ContactsActivity.class);
                        startActivity(intent);
                        finish();
                        //progressBar.setVisibility(View.GONE);
                        Toast.makeText(SettingsActivity.this, "Profile settings has been updated", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(SettingsActivity.this, "Error :  Profile settings has not been updated", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
    private void retrieveUserInfo()
    {
        userRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            private Object Error;

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    if (snapshot.child("image")==null){Log.d("Error","image not available");

                    }
                    else{
                    String imageDb=snapshot.child("image").getValue().toString();
                        Picasso.get().load(imageDb).placeholder(R.drawable.profile_image).into(profileImageView);}
                String nameDb=snapshot.child("name").getValue().toString();
                String bioDb=snapshot.child("status").getValue().toString();

                userNameET.setText(nameDb);
                userBioET.setText(bioDb);
                //T item = getItem(position);
//                if (item instanceof CharSequence) {
//                    text.setText((CharSequence)item);
//                } else {
//                    text.setText(item.toString());
//                }

                        }
                else{
                    Toast.makeText(SettingsActivity.this, "Snapshot doesnt exist", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}