package com.aics.bscs.vehiclegpstrackingapp;


import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;

public class VideoActivity extends AppCompatActivity{
    private Uri videoUri;
    private static final int REQUEST_CODE = 101;
    private StorageReference videoRef;
    TextView textStatus;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    MediaController videoButtons;

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        textStatus = (TextView) findViewById(R.id.tx_status);


        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        videoRef = storageRef.child("/Videos/testVid2.mp4");


        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {

       /* perform your actions here*/

        } else {
            mAuth.signInAnonymously();
        }
        videoButtons = new MediaController(this);


    }

    public void onClick(View view){
        if(view.getId() == R.id.btn_downloadVideo){
            downloadVideo(view);
        }
    }

    private void signInAnonymously() {
        mAuth.signInAnonymously().addOnSuccessListener(this, new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                        /* perform your actions here*/

            }
        })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.e("MainActivity", "signFailed****** ", exception);
                    }
                });
    }

    public void downloadVideo(View view){
        try{
            final File localFile = File.createTempFile("testVid2", "mp4");

            videoRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    //Toast.makeText(this, "Download Complete", Toast.LENGTH_LONG).show();
                    textStatus.setText("Success");

                    final VideoView videoView = (VideoView) findViewById(R.id.videoView);
                    videoView.setVideoURI(Uri.fromFile(localFile));
                    videoView.setMediaController(videoButtons);
                    videoButtons.setAnchorView(videoView);
                    videoView.start();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    textStatus.setText("Error 48: Check your Internet Connection.");
                }
            });
        }catch (Exception e){
            textStatus.setText("Error 69: Video is not in db!");

        }
    }
}
