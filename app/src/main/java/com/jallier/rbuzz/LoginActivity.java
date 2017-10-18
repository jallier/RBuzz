package com.jallier.rbuzz;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.Queue;
import java.util.Stack;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    private final static String TAG = "LoginActivity";
    private static int RC_SIGN_IN = 100;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private DatabaseReference mUsersReference;
    private ValueEventListener mListener;

    @Override
    protected void onStart() {
        super.onStart();
        //Firebase DB nonsense
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mUsersReference = mDatabase.child("users");
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, dataSnapshot.getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mUsersReference.addValueEventListener(listener);
        mListener = listener;

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null) {
            Log.d(TAG, currentUser.getDisplayName());
            Log.d(TAG, currentUser.getUid());
            Log.d(TAG, "User already logged in, returning to mainActivity");
            finish();
        } else {
            Log.d(TAG, "Firebase user is not authenticated");
        }
    }

    private void writeNewUser(String uuid, String name, String fcmToken) {
        User user = new User(name, fcmToken);

        mUsersReference.child(uuid).setValue(user);
        Log.d(TAG, "New user written to FB");
    }

    private void returnUserData(){

    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            String token = FirebaseInstanceId.getInstance().getToken();
                            writeNewUser(user.getUid(), user.getDisplayName(), token);
//                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
//                            updateUI(null);
                        }

                        // ...
                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setupActionBar();
        // Set up the login form.

        //Firebase nonsense
        mAuth = FirebaseAuth.getInstance();

        //Google sign in nonsense
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(getString(R.string.default_web_client_id))
                .build();
        final GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        SignInButton btnSignIn = (SignInButton) findViewById(R.id.sign_in_button);
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(intent, RC_SIGN_IN);
            }
        });

    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, connectionResult.getErrorMessage());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleResult(result);
        }
    }

    private void handleResult(GoogleSignInResult result) {
        Log.d(TAG, "HandleSignIn is: " + result.isSuccess());
        if (result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();
            firebaseAuthWithGoogle(acct);
            Log.d(TAG, "" + acct.getDisplayName());
            Log.d(TAG, "" + acct.getEmail());
            Log.d(TAG, "" + acct.getId());
            Log.d(TAG, "" + acct.getIdToken());

            // Send the info back to the parent activity
            Intent resultIntent = new Intent();
            resultIntent.putExtra("googleAcct", acct);
            setResult(Activity.RESULT_OK, resultIntent);
//            finish();
        }
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
}

