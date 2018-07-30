package com.makarand.duet;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Fragment;

import android.widget.Button;

import com.makarand.duet.FragmentContainer.StepOne;
import com.makarand.duet.FragmentContainer.WelcomeMessage;
import com.makarand.duet.FragmentContainer.WithID;
import com.makarand.duet.FragmentContainer.WithoutID;

import butterknife.ButterKnife;


public class WelcomeActivity extends AppCompatActivity implements
        WelcomeMessage.OnFragmentInteractionListener,
        StepOne.OnFragmentInteractionListener,
        WithID.OnFragmentInteractionListener,
        WithoutID.OnFragmentInteractionListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        ButterKnife.bind(this);

        getFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .add(R.id.frame, new WelcomeMessage())
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
    public void onFragmentInteraction(Uri uri){
        //you can leave it empty
    }
}
