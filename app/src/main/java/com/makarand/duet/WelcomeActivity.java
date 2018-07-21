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

import butterknife.ButterKnife;


public class WelcomeActivity extends AppCompatActivity implements
        WelcomeMessage.OnFragmentInteractionListener,
        StepOne.OnFragmentInteractionListener{

    FragmentManager manager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        ButterKnife.bind(this);
        Fragment welcomeMessage = new WelcomeMessage();
        manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.frame, welcomeMessage);
        transaction.commit();
    }

    @Override
    public void onFragmentInteraction(Uri uri){
        //you can leave it empty
    }
}
