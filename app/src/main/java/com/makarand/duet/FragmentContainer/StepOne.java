package com.makarand.duet.FragmentContainer;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.makarand.duet.Constants.Constants;
import com.makarand.duet.R;
import com.makarand.duet.model.Chatroom;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link StepOne.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link StepOne#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StepOne extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    /*If the edittext contains an id then the haveID will be true.*/
    private boolean haveID = false;
    @BindView(R.id.next_button) Button nextButton;
    @BindView(R.id.id_container) EditText idContainer;
    /*Firestore instance*/
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference chatroomRef;

    public StepOne() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment StepOne.
     */
    // TODO: Rename and change types and number of parameters
    public static StepOne newInstance(String param1, String param2) {
        StepOne fragment = new StepOne();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_step_one, container, false);
        ButterKnife.bind(this, v);
        chatroomRef = db
                .collection("chatrooms")
                .document(Constants.globalChatroomID);
        idContainer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() > 5 ){
                    nextButton.setText(R.string.connect);
                    haveID = true;
                }
                else {
                    nextButton.setText(R.string.no_id);
                    haveID = false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(haveID)
                    connect();
                else {
                    createNewID();
                }
            }
        });
//        yesButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                getFragmentManager()
//                        .beginTransaction()
//                        .setCustomAnimations(R.animator.slide_in_from_right, R.animator.slide_out_to_left, R.animator.slide_in_from_left, R.animator.slide_out_to_right)
//                        //.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
//                        .replace(R.id.frame, new WithID())
//                        .addToBackStack("WithID")
//                        .commit();
//            }
//        });
//
//        noButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                getFragmentManager()
//                        .beginTransaction()
//                        .setCustomAnimations(R.animator.slide_in_from_right, R.animator.slide_out_to_left, R.animator.slide_in_from_left, R.animator.slide_out_to_right)
//                        //.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
//                        .replace(R.id.frame, new WaitingFragment())
//                        .addToBackStack("WaitingFragment")
//                        .commit();
//            }
//        });
        return v;
    }

    private void createNewID() {
        /*Creating a new ID and starting next fragment where user will wait for partner to connect.*/
        /*Also with creating ID, creating a chatroom tree. All this will be done using Batch writes*/
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        /*creating model class object (POJO) for writing participants*/
        Chatroom chatroom = new Chatroom(uid, "undef");
        /*the following if condition prevents users from creating multiple chatrooms.
        * the Constants.globalChatroomID contains the data that is taken from shared prefs or from the firebase
        * itself. If the users hasn't created any other chatroom, if they did then the chatroom won't be "undef"
        * if its "undef" then its safe to say that the user is not associated with any chatroom*/
        if(Constants.globalChatroomID.equals("undef"))
            Constants.globalChatroomID = db.collection("chatrooms").document().getId();
        DocumentReference userRef = db.collection("users").document(uid);
        /*Writing data using batch operation.*/
        WriteBatch batch = db.batch();
        /*setting key to user personal info tree*/
        batch.update(userRef, "personal.chatroom", Constants.globalChatroomID);
        /*changing the value of newUSer to false as the has a chatroom and is no longer new.*/
        batch.update(userRef, "open.newUser", false);
        /*creating chatroom which contains description of participants.*/
        batch.set(chatroomRef, chatroom);

        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    storeToLocalStorage(Constants.globalChatroomID);
                    getFragmentManager()
                            .beginTransaction()
                            .setCustomAnimations(R.animator.slide_in_from_right, R.animator.slide_out_to_left, R.animator.slide_in_from_left, R.animator.slide_out_to_right)
                            //.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                            .replace(R.id.frame, new WaitingFragment())
                            .addToBackStack("StepOne")
                            .commit();

                }
                else {
                    Toast.makeText(getActivity().getApplicationContext(), "Error occurred, possibly network issue.", Toast.LENGTH_SHORT).show();
                    Log.e("StepOne", task.getException().toString());
                }
            }
        });
    }

    private void storeToLocalStorage(String newID) {
        /*Writing to shared preferences so that we can call the local storage
        * rather than querying online.*/
        SharedPreferences preferences = getActivity().getSharedPreferences(Constants.sharedPrefName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("chatRoomID", newID);
        editor.commit();
    }

    private void connect() {
        String currentID = idContainer.getText().toString().trim();
        /*currentID is used to get the id from the textbox. I don't know why android
        * calls textbox edittext. such a stupid move.*/
        final DocumentReference newChatroomRef = db
                .collection("chatrooms")
                .document(currentID);
        /*As I have added feature to roll back even if a chatroom is created, it is possible someone
        * might copy IF from waiting fragment and come back and paste it here, which will be like sucking up own dick.
        * To avoid it first check if the chatroom exists, then add the user to the member list.*/
        newChatroomRef
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists()) {
                            Chatroom chatroom = documentSnapshot.toObject(Chatroom.class);
                            String p1 = "undef", p2 = "undef";
                            try {
                                p1 = chatroom.getP1();
                                p2 = chatroom.getP2();
                            }
                            catch (Exception e){
                                Log.e("StepOne", e.toString());
                            }

                            if(p1.equals(Constants.myUid) || p2.equals(Constants.myUid)){
                                Toast.makeText(getActivity().getApplicationContext(), "You are already in the chatroom.", Toast.LENGTH_LONG).show();
                            }
                            else {
                                newChatroomRef.update("p2", Constants.myUid)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(getActivity().getApplicationContext(), "Success", Toast.LENGTH_SHORT).show();
                                                }
                                                else {
                                                    Toast.makeText(getActivity().getApplicationContext(), "Error occurred, please check your internet connection", Toast.LENGTH_SHORT).show();
                                                    
                                                }
                                            }
                                        });
                            }
                        }
                        else {
                            Toast.makeText(getActivity().getApplicationContext(), "Chatroom does not exists. Try creating a new one.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
/*TODO: add a destory chatroom feature [IMP]*/