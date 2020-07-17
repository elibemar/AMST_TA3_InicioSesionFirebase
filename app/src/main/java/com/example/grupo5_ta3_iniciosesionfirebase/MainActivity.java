package com.example.grupo5_ta3_iniciosesionfirebase;

import androidx.appcompat.app.AppCompatActivity;

import android.accounts.Account;
import android.content.Intent;
//import android.media.session.MediaSession;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.Biography;
import com.google.api.services.people.v1.model.Birthday;
import com.google.api.services.people.v1.model.Date;
import com.google.api.services.people.v1.model.Gender;
import com.google.api.services.people.v1.model.Person;
import com.google.api.services.people.v1.model.PhoneNumber;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    //Varibales públicas
    private WeakReference<MainActivity> weakAct = new WeakReference<>(this);
    static final private String TAG = "hole";
    HashMap<String, String> info_user = new HashMap<String, String>();
    static final int GOOGLE_SIGN_IN = 123;
    FirebaseAuth mAuth;
    GoogleSignInClient mGoogleSignInClient;
    Button btn_login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();

        Scope birthday = new Scope("https://www.googleapis.com/auth/user.birthday.read");
        Scope phone = new Scope("https://www.googleapis.com/auth/user.phonenumbers.read");
        Scope gender = new Scope("https://www.googleapis.com/auth/user.gender.read");

        //GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).
                                      requestIdToken(getString(R.string.default_web_client_id)).
                                      requestEmail().
                                      requestProfile().
                                      requestScopes(birthday, phone, gender).build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        //Borrar datos de inicio de sesion
        Intent intent = getIntent();
        String msg = intent.getStringExtra("msg");
        if(msg != null){
            if(msg.equals("cerrarSesion")){
                cerrarSesion();
            }
        }
    }

    private void cerrarSesion(){
        mGoogleSignInClient.signOut().addOnCompleteListener(this,task -> toastALogin());
    }


    public void iniciarSesion(View view) {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN);
    }

    private void iniciarSesion() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null){
                    handleOK(account);
                }
                } catch (ApiException e) {
                   Log.w("TAG", "Fallo el inicio de sesión con google.", e);
                }
        }
    }

    private void handleOK(GoogleSignInAccount acct) {
        Log.d("TAG", "firebaseAuthWithGoogle:" + acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                    new GetProfileDetails(acct, weakAct, TAG).execute();
                } else {
                    toastALogin();
                }
            });
    }

    private void  toastALogin(){
        Toast.makeText(MainActivity.this,"Login", Toast.LENGTH_SHORT).show();
    }

    //Se crea la funcion updateUI() con el fin de pasar la informacion del usuario
    //por medio de un Intent hacia la nueva actividad
    private void updateUI(Person meProfile) {
        FirebaseUser user = mAuth.getCurrentUser();
        info_user.put("user_name",user.getDisplayName());
        info_user.put("user_email",user.getEmail());
        info_user.put("user_photo",String.valueOf(user.getPhotoUrl()));
        info_user.put("user_id", user.getUid());

        List<Gender> genders = meProfile.getGenders();
        if (genders != null && genders.size() > 0) {
            String gender = genders.get(0).getValue();
            info_user.put("user_gender", gender);
        } else {
            Log.d(TAG, "onPostExecute no gender if set to private ");
        }

        List<Birthday> birthdays = meProfile.getBirthdays();
        if (birthdays != null && birthdays.size() > 0) {
            for (Birthday b : birthdays) { //birthday still able to get even private, unlike gender
                Date bdate = b.getDate();
                if (bdate != null) {
                    String bday, bmonth, byear;
                    if (bdate.getDay() != null) bday = bdate.getDay().toString();
                    else bday = "";
                    if (bdate.getMonth() != null) bmonth = bdate.getMonth().toString();
                    else bmonth = "";
                    if (bdate.getYear() != null) byear = bdate.getYear().toString();
                    else byear = "";
                    info_user.put("user_birthday", bday + "/" +  bmonth + "/" + byear);
                }
            }
        } else {
            Log.w(TAG, "saveAdvanced no birthday");
        }

        //phoneNumbers
        List<PhoneNumber> phoneNumbers = meProfile.getPhoneNumbers();
        String pn = "";
        if (phoneNumbers != null && phoneNumbers.size() > 0) {
            PhoneNumber phone = phoneNumbers.get(0);
            Log.w(TAG, "PHONE OBTAINED" + phone.toString());
            pn = phone.getValue();
        }
        if(pn.isEmpty()) pn = "No se encontraron Números telefónicos";
        info_user.put("user_phone", pn);

        //biographies
        List<Biography> biographies = meProfile.getBiographies();
        String bio = "";
        if (biographies != null && biographies.size() > 0) {
            bio = phoneNumbers.get(0).getValue();
        }
        if(bio.isEmpty()) bio = "No se encontraron Biografías";
        info_user.put("user_bio", bio);

        Toast.makeText(MainActivity.this,"Bienvenido", Toast.LENGTH_SHORT).show();
        finish();
        Intent intent = new Intent(MainActivity.this,PerfilUsuario.class);
        intent.putExtra("info_user",info_user);
        startActivity(intent);
        finish();
    }


    static class GetProfileDetails extends AsyncTask<Void, Void, Person> {
        private PeopleService ps;
        private int authError = -1;
        private WeakReference<MainActivity> weakAct;
        private String TAG;

        GetProfileDetails(GoogleSignInAccount account, WeakReference<MainActivity> weakAct, String TAG) {
            this.TAG = TAG;
            this.weakAct = weakAct;
            GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                    this.weakAct.get(), Collections.singleton(Scopes.PROFILE));
            credential.setSelectedAccount(
                    new Account(account.getEmail(), "com.google"));
            HttpTransport HTTP_TRANSPORT = AndroidHttp.newCompatibleTransport();
            JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
            ps = new PeopleService.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                    .setApplicationName("Google Sign In Quickstart")
                    .build();
        }

        @Override
        protected Person doInBackground(Void... params) {
            Person meProfile = null;
            try {
                meProfile = ps
                        .people()
                        .get("people/me")
                        .setPersonFields("names,genders,birthdays,phoneNumbers,biographies")
                        .execute();
            } catch (UserRecoverableAuthIOException e) {
                e.printStackTrace();
                authError = 0;
            } catch (GoogleJsonResponseException e) {
                e.printStackTrace();
                authError = 1;
            } catch (IOException e) {
                e.printStackTrace();
                authError = 2;
            }
            return meProfile;
        }

        @Override
        protected void onPostExecute(Person meProfile) {
            MainActivity mainAct = weakAct.get();
            if (mainAct != null) {
                if (authError == 0) { //app has been revoke, re-authenticated required.
                    mainAct.iniciarSesion();
                } else if (authError == 1) {
                    Log.w(TAG, "People API might not enable at" +
                            " https://console.developers.google.com/apis/library/people.googleapis.com/?project=<project name>");
                } else if (authError == 2) {
                    Log.w(TAG, "API io error");
                } else {
                    if (meProfile != null) {
                        mainAct.updateUI(meProfile);
                    }
                }
            }
        }
    }

}