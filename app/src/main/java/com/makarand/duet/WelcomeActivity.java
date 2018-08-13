package com.makarand.duet;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.makarand.duet.Constants.Constants;
import com.makarand.duet.FragmentContainer.StepOne;
import com.makarand.duet.FragmentContainer.WaitingFragment;
import com.makarand.duet.FragmentContainer.WelcomeMessage;

import butterknife.BindView;
import butterknife.ButterKnife;


public class WelcomeActivity extends AppCompatActivity implements
        WelcomeMessage.OnFragmentInteractionListener,
        StepOne.OnFragmentInteractionListener,
        WaitingFragment.OnFragmentInteractionListener
        {

    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    public static String TAG = "WelcomeActivity";
    private String uid = null;

    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    /* following two relative layouts are imported just to show loading screen till async task of getting user info completes.
    * */
    @BindView(R.id.splash_screen)RelativeLayout splashScreen;
    @BindView(R.id.background)RelativeLayout background;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        ButterKnife.bind(this);
        splashScreen.setVisibility(View.VISIBLE);
        background.setVisibility(View.GONE);
        if (auth.getCurrentUser() == null) {
            /*USer is not signed in redirect to login page.*/
            startActivity(new Intent(this, LoginActivity.class));
            finish();

        } else {
            uid = auth.getCurrentUser().getUid();
        }
        preferences = getSharedPreferences(Constants.sharedPrefName, MODE_PRIVATE);
        editor = preferences.edit();
        /*At this point user is logged in but not sure if they have partner connected or
         * Chatroom is created in their name to verify that I am checking if the
         * chatroom is "undef" && the partner is "undef", here "undef" is initial default value.
         *
         * But calling to the Firebase for this simple query is expensive and takes time to load the fragment.
         * Thus to solve this problem, we'll first check if any relative data is available in the shared prefs
         * if it returns the default value then request to the firebase.
         */

        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String chatroom = documentSnapshot.getString("personal.chatroom");
                        String partner = documentSnapshot.getString("personal.partner");
                        if (chatroom != null && partner != null) {
                            /*?First write that to local storage and start the main activity
                             * The main reason to write it to disk is so that we can read it next time
                             * from launcher activity without having to request to the firebase every time
                             * the app is started*/
                            editor.putString("chatroom", chatroom);
                            editor.putString("partner", partner);
                            editor.commit();
                            if(!chatroom.equals("undef")){
                                /*Chatroom is available*/
                                if(!partner.equals("undef")){
                                    /*Chatroom as well as partner is available*/
                                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                    finish();
                                    return;
                                }
                                else {
                                    /*chatroom is available but partner is not connected,
                                    * Now show the Waiting Fragment*/
                                    openFragment(new WaitingFragment());
                                }
                            }
                            else {
                                /*The user is new that's for sure so show him/her getting started page*/
                                openFragment(new WelcomeMessage());
                            }

                            splashScreen.setVisibility(View.GONE);
                            background.setVisibility(View.VISIBLE);
                        } else {
                            /*if isNew is null then probably the userdata does not exists*/
                            Toast.makeText(WelcomeActivity.this, "Error occurred, please try again later.", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Probably user data does not exists");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(WelcomeActivity.this, "Error, please check your internet connection.", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, e.getMessage());
                    }
                });
    }

    private void openFragment(Fragment fragment) {
        getFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .add(R.id.frame, fragment)
                .commit();
    }

//    @Override
//    public  void onBackPressed(){
//        int count = getFragmentManager().getBackStackEntryCount();
//        if(count == 0)
//            finish();
//        else{
//            if (getFragmentManager().getBackStackEntryCount() > 0) {
//                getFragmentManager().popBackStack();
//            } else {
//                super.onBackPressed();
//            }
//        }
//
//    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        //you can leave it empty
    }
}
