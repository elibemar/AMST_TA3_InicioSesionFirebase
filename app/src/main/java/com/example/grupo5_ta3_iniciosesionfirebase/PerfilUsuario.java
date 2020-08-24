package com.example.grupo5_ta3_iniciosesionfirebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class PerfilUsuario extends AppCompatActivity {
    //Variables
     private TextView txt_id, txt_name, txt_email, txt_gender, txt_bday, txt_phone, txt_bio;
     private ImageView imv_photo;
     private Button btn_logout;
     private HashMap<String, String> info_user;
     private String photo;
     public DatabaseReference db_reference; //Variable publica de referencia a la base de datos
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_usuario);
        //Obtener el intent
        Intent intent = getIntent();
        info_user = (HashMap<String, String>)intent.getSerializableExtra("info_user");
        iniciarVariables();
        iniciarBaseDeDatos();
        leerTweets();
        escribitTweets(info_user.get("user_name"));

    }

    public void iniciarVariables(){
        txt_id = (TextView)findViewById(R.id.txt_userId);
        txt_name = (TextView)findViewById(R.id.txt_nombre);
        txt_email = (TextView)findViewById(R.id.txt_correo);
        imv_photo = (ImageView)findViewById(R.id.imv_foto);

        txt_id.setText(info_user.get("user_id"));
        txt_name.setText(info_user.get("user_name"));
        txt_email.setText(info_user.get("user_email"));
        
        photo = info_user.get("user_photo");
        Picasso.with(getApplicationContext()).load(photo).into(imv_photo);
    }
    //Iniciar base de datos
    public void iniciarBaseDeDatos(){
        db_reference = FirebaseDatabase.getInstance().getReference().child("Grupo");
    }

    //Leer TWEETS
    public void leerTweets(){
        db_reference.child("Grupo 0").child("tweets").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    System.out.println(snapshot);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println(databaseError.toException());
            }
        });
    }

    public void escribitTweets(String autor){
        String tweet = "Hola mundo firebase 2";
        String fecha = "16/06/2020";
        Map<String,String> hola_tweet = new HashMap<String,String>();
        hola_tweet.put("autor",autor);
        hola_tweet.put("fecha",fecha);
        DatabaseReference tweets = db_reference.child("Grupo 0").child("tweets");
        tweets.setValue(tweet);
        tweets.child(tweet).child("autor").setValue(autor);
        tweets.child(tweet).child("fecha").setValue(fecha);

    }

    //Cerrar secion
    public void cerrarSesion(View view){
        FirebaseAuth.getInstance().signOut();
        finish();
        Intent intent = new Intent(PerfilUsuario.this,MainActivity.class);
        intent.putExtra("msg","cerrarSesion");
        startActivity(intent);
    }

    public void registrosLab5(View view){
        Intent intent = new Intent(this, Registros.class);
        finish();
        startActivity(intent);
    }

    public void registrosLab4(View view){
        Intent intent = new Intent(this, Registros.class);
        finish();
        startActivity(intent);
    }
}