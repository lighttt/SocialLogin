package np.com.manishtuladhar.sociallogin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    //google sign in vars
    private GoogleSignInClient mGoogleSignInClient;
    private AppCompatButton signInButton;

    //firebase auth
    private FirebaseAuth mAuth;

    //req code
    private static final int REQ_GOOGLE_SIGN_IN = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        signInButton = findViewById(R.id.googleSignInBtn);

        //configure google sign in
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        //build a google sign client
        mGoogleSignInClient = GoogleSignIn.getClient(this, signInOptions);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });
        mAuth =  FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //check if user is signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    /**
     * Helps to start the sign in process
     */
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, REQ_GOOGLE_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //check if the intent is launched
        if (requestCode == REQ_GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                //google sign successful
                // get data and login to firebase auth
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.e(TAG, "onActivityResult: " + account.getId());
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                //google sign in failed
                Log.e(TAG, "onActivityResult: SignInFailed",e );
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * After getting the google account data we use it to create auth in firebase
     */
    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        //got the account credential
        AuthCredential authCredential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

        //use auth for sigin
        mAuth.signInWithCredential(authCredential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //sign in successful, update your UI to go to next activity
                            FirebaseUser mUser = mAuth.getCurrentUser();
                            Log.e(TAG, "onComplete: user details " +mUser.getDisplayName() );
                            Log.e(TAG, "onComplete: user details " +mUser.getEmail() );
                            Log.e(TAG, "onComplete: user details " +mUser.getUid() );
                            updateUI(mUser);
                        } else {
                            //sign in fails display a message
                            Toast.makeText(LoginActivity.this, "Sorry couldnot authenticate you!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * Change the login to home
     */
    private void updateUI(FirebaseUser currentUser) {
        if(currentUser !=null)
        {
            Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
            startActivity(intent);
        }
    }
}