package com.jallier.rbuzz;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.RemoteMessage;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements AcceptContactRequestDialogFragment.PositiveContactRequestListener {
    private static final String TAG = "MainActivity";
    private final long TIME_WINDOW = 5000; // ms for how long to check times between btn pushes
    private final int LOGIN_ACTIVITY_CODE = 101;
    private DatabaseReference mDatabase;
    private Vibrator vibrator;
    private FirebaseAuth mAuth;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<Contact> contactsList = new ArrayList<>();

    // Broadcast receiver to handle the notification service passing data back to Main Activity
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "received message from server");
            RemoteMessage message = intent.getParcelableExtra("message");
            String messageType = message.getData().get("messageType");
            switch (messageType) {
                case "vibration":
                    String[] patternString = message.getData().get("pattern").split("\\[|\\]|,");
                    List<Long> pattern = new ArrayList<>();
                    for (int i = 1; i < patternString.length; i++) {
                        pattern.add(Long.parseLong(patternString[i]));
                    }
                    Log.d(TAG, "Received notif; playing pattern: " + pattern);
                    playVibration(pattern);
                    break;
                case "contactRequest":
                    Log.d(TAG, "Contact request: " + message.getData());
                    DialogFragment fragment = new AcceptContactRequestDialogFragment();
                    Bundle args = new Bundle();
                    args.putString("sender", message.getData().get("sender"));
                    fragment.setArguments(args);
                    fragment.show(getSupportFragmentManager(), "contact");
                    break;
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        // Log the firebase token. Use this as the 'to:' id token
        Log.d(TAG, "Firebase token: " + FirebaseInstanceId.getInstance().getToken());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button btnLoginActiv = (Button) findViewById(R.id.btnLoginActiv);
        btnLoginActiv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivityForResult(intent, LOGIN_ACTIVITY_CODE);
            }
        });

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        //Get reference to Firebase DB and Auth
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        final List<Long> pattern = new ArrayList<>();
        pattern.add((long) 0); // First value in array represents delay before first buzz, so set to 0
        final SimpleBool initialBtnPush = new SimpleBool(true);

        final Button btnPlayBuzz = (Button) findViewById(R.id.btnPlayBuzz);
        btnPlayBuzz.setOnClickListener(new View.OnClickListener() {
            /**
             * When the play button is pushed, write the vibration pattern to the Firebase db under /messages
             * @param v Calling view
             */
            @Override
            public void onClick(View v) {
                Log.v(TAG, "Play button clicked. Pattern: " + pattern.toString());
                String uid = mAuth.getCurrentUser().getUid();
                Message message = new Message(uid, uid, pattern); // The recipient will change once contacts are added
                mDatabase.child("messages").push().setValue(message);
                Log.d(TAG, "Added message to FB queue");
                resetPatternAndVars(pattern, initialBtnPush);
            }
        });

        final SimpleTime initialDownTime = new SimpleTime(0);
        final SimpleTime initialUpTime = new SimpleTime(0);

        final Button btnRecordBuzz = (Button) findViewById(R.id.btnRecordBuzz);
        btnRecordBuzz.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                long currentTime = System.currentTimeMillis();
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    initialDownTime.updateTime();
                    long elapsed = currentTime - initialUpTime.getTime();
                    if (elapsed > TIME_WINDOW) {
                        elapsed = 0;
                    }
                    if (!initialBtnPush.isTrue()) {
                        pattern.add(elapsed);
                    }
                    initialBtnPush.setBool(false);
                    Log.d(TAG, "record pushed down. Length between pushes: " + elapsed);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    initialUpTime.updateTime();
                    long elapsed = currentTime - initialDownTime.getTime();
                    pattern.add(elapsed);
                    Log.d(TAG, "record released. Length: " + elapsed);
                }
                return true;
            }
        });

        final Button btnAddContact = (Button) findViewById(R.id.btnAddContact);
        btnAddContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Add contact button pushed");
                String uid = mAuth.getCurrentUser().getUid();
                EditText editText = (EditText) findViewById(R.id.editTextAddContact);
                String recipient = editText.getText().toString();
                ContactRequest request = new ContactRequest("contactRequest", uid, recipient);
                mDatabase.child("contactRequests").push().setValue(request);
                Log.d(TAG, "Added contact request to FB queue");
            }
        });

        final Button btnReset = (Button) findViewById(R.id.btnReset);
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPatternAndVars(pattern, initialBtnPush);
                initialBtnPush.setBool(true);
                vibrator.cancel();
                Log.d(TAG, "Pattern cleared");
            }
        });

        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter("notif"));

        mRecyclerView = (RecyclerView) findViewById(R.id.contactsRV);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new ContactsAdapter(contactsList);
        mRecyclerView.setAdapter(mAdapter);
//        populateRecycler(contactsList);

        ValueEventListener contactsListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "Data changed"+ dataSnapshot.toString());
                populateRecycler(contactsList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error fetching contacts " + databaseError);
            }
        };
        // TODO: Check that this doesn't break the app completely when it first loads without a user being signed in.
        String user;
        try {
            user = mAuth.getCurrentUser().getUid();
        } catch (NullPointerException e) {
            e.printStackTrace();
            return;
        }
        mDatabase.child("users/" + user + "/contacts").addValueEventListener(contactsListener);

        // Stop the keyboard from appearing. REMOVE WHEN ADD CONTACT IN OWN ACTIVITY
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    /**
     * Fetch the users contacts from firebase and add them to the list attached to the recyclerview adapter
     *
     * @param contacts the list to add the contacts to.
     */
    private void populateRecycler(final List<Contact> contacts) {
        ValueEventListener contactsListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "retrieved users contacts from database");
                contacts.clear();
                Iterable<DataSnapshot> dataSnapshots = dataSnapshot.child("contacts").getChildren();
                for (DataSnapshot i : dataSnapshots) {
                    contacts.add(new Contact(i.getValue().toString()));
                }
                if (contacts.size()-1 < 1) {
                    mAdapter.notifyDataSetChanged();
                } else {
                    mAdapter.notifyItemInserted(contacts.size() - 1);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Reading contacts failed: " + databaseError);
            }
        };
        // TODO: Check that this doesn't break the app completely when it first loads without a user being signed in.
        String user;
        try {
            user = mAuth.getCurrentUser().getUid();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        mDatabase.child("users/" + user).addListenerForSingleValueEvent(contactsListener);
    }

    /**
     * @param pattern List representing the pattern of vibrations to play.
     */
    private void playVibration(List<Long> pattern) {
        long[] pt = new long[pattern.size()];
        for (int i = 0; i < pattern.size(); i++) {
            pt[i] = pattern.get(i);
        }
        vibrator.vibrate(pt, -1);
    }

    /**
     * Method to handle the result of the login activity
     *
     * @param requestCode code passed in on new activity creation
     * @param resultCode  code returned from activity
     * @param data        result from the login activity
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case (LOGIN_ACTIVITY_CODE):
                if (resultCode == Activity.RESULT_OK) {
                    Log.d(TAG, data.getParcelableExtra("googleAcct").toString());
                }
        }
    }

    /**
     * Reset the current pattern to record the next one. Adds the initial 0 value which represents initial delay.
     *
     * @param pattern
     * @param initialBtnPush
     */
    private void resetPatternAndVars(List<Long> pattern, SimpleBool initialBtnPush) {
        pattern.clear();
        pattern.add((long) 0);
        initialBtnPush.setBool(true);
    }

    /**
     * Handle the result from clicking 'yes' in the contact request confirmation dialog
     *
     * @param dialog The dialog fragment that generated the event
     */
    @Override
    public void onDialogPositiveClick(DialogFragment dialog, String recipient) {
        Log.d(TAG, "Positive button pushed, accepting request from user: " + recipient);
        String currentId = mAuth.getCurrentUser().getUid();
        ContactRequest request = new ContactRequest("contactAccept", currentId, recipient);
        mDatabase.child("contactRequests").push().setValue(request);
    }

    private class SimpleBool {
        boolean bool;

        public SimpleBool(boolean bool) {
            this.bool = bool;
        }

        public boolean isTrue() {
            return bool;
        }

        public void setBool(boolean bool) {
            this.bool = bool;
        }
    }
}


