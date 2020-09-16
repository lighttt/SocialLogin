package np.com.manishtuladhar.sociallogin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
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
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import org.json.JSONObject;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    //google sign in vars
    private GoogleSignInClient mGoogleSignInClient;
    private AppCompatButton signInButton;

    //facebook btn
    private AppCompatButton facebookLoginBtn;
    private CallbackManager callbackManager;

    //firebase auth
    private FirebaseAuth mAuth;

    //req code
    private static final int REQ_GOOGLE_SIGN_IN = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        signInButton = findViewById(R.id.googleSignInBtn);
        facebookLoginBtn = findViewById(R.id.fbSignInBtn);

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
        mAuth = FirebaseAuth.getInstance();

        //facebook
        callbackManager = CallbackManager.Factory.create();
        facebookLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                facebookLogin();
            }
        });

    }
    // ============================ VIEW CONTROL ===========================

    @Override
    protected void onStart() {
        super.onStart();
        //check if user is signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    /**
     * Change the login to home
     */
    private void updateUI(FirebaseUser currentUser) {
        if (currentUser != null) {
            Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
            startActivity(intent);
        }
    }

    // ============================ LOGIN RESULT ===========================
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

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
                Log.e(TAG, "onActivityResult: SignInFailed", e);
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

    }

    // ============================ GOOGLE SIGN IN ===========================

    /**
     * Helps to start the sign in process
     */
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, REQ_GOOGLE_SIGN_IN);
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
                            Log.e(TAG, "onComplete: user details " + mUser.getDisplayName());
                            Log.e(TAG, "onComplete: user details " + mUser.getEmail());
                            Log.e(TAG, "onComplete: user details " + mUser.getUid());
                            saveUserData(mUser);
                            updateUI(mUser);
                        } else {
                            //sign in fails display a message
                            Toast.makeText(LoginActivity.this, "Sorry couldnot authenticate you!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // ============================ FACEBOOK SIGN IN ===========================

    private void facebookLogin()
    {
        LoginManager loginManager = LoginManager.getInstance();
        loginManager.logInWithReadPermissions(LoginActivity.this,Arrays.asList("email","public_profile"));
        // Callback registration
        loginManager.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // handle facebook token
                firebaseAuthWithFacebook(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                // App code
                Toast.makeText(LoginActivity.this, "Cancelled Login", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
                Toast.makeText(LoginActivity.this, "Cannot Login Error!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * After getting the token add the user to firebase
     */
    private void firebaseAuthWithFacebook(AccessToken token) {
        Log.e(TAG, "firebaseAuthWithFacebook: " + token);
        AuthCredential authCredential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(authCredential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //sign in successful, update your UI to go to next activity
                            FirebaseUser mUser = mAuth.getCurrentUser();
                            Log.e(TAG, "onComplete: user details " + mUser.getDisplayName());
                            Log.e(TAG, "onComplete: user details " + mUser.getEmail());
                            Log.e(TAG, "onComplete: user details " + mUser.getUid());
                            saveUserData(mUser);
                            updateUI(mUser);
                        } else {
                            //sign in fails display a message
                            Toast.makeText(LoginActivity.this, "Sorry couldnot authenticate you!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // ============================ SAVE DATa ===========================

    /**
     * Save user data to shared preferences
     */
    private void saveUserData(FirebaseUser user) {
        SharedPrefs sharedPrefs = SharedPrefs.getInstance();
        sharedPrefs.saveUserData(this,
                user.getDisplayName(),
                user.getEmail(),
                user.getPhotoUrl().toString());
    }
}