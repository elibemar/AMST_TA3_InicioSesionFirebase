package com.example.grupo5_ta3_iniciosesionfirebase;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class PerfilUsuario extends AppCompatActivity {
    //Variables
     private TextView txt_id, txt_name, txt_email;
     private ImageView imv_photo;
     private Button btn_logout;
     private HashMap<String, String> info_user;
     private String photo;
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_usuario);
        //Obtener el intent
        Intent intent = getIntent();
        info_user = (HashMap<String, String>)intent.getSerializableExtra("info_user");
        iniciarVariables();

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

    //Cerrar secion
    public void cerrarSesion(View view){
        FirebaseAuth.getInstance().signOut();
        finish();
        Intent intent = new Intent(PerfilUsuario.this,MainActivity.class);
        intent.putExtra("msg","cerrarSesion");
        startActivity(intent);
    }
}