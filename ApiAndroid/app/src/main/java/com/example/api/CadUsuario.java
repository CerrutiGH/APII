package com.example.api;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.api.database.DBHelper;
import com.example.api.interfaces.UserInterface;
import com.example.api.models.UserClass;

import java.security.cert.CertificateException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CadUsuario extends AppCompatActivity {

    private EditText nameEdt, emailEdt, senhaEdt;
    private Button postDataBtn;

    DBHelper db = new DBHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cad_usuario);

        getSupportActionBar().hide();

        nameEdt = findViewById(R.id.idNomeUsuario);
        emailEdt = findViewById(R.id.idEmailUsuario);
        senhaEdt = findViewById(R.id.idSenhaUsuario);
        postDataBtn = findViewById(R.id.sendUserToAPI);

        postDataBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (nameEdt.getText().toString().isEmpty() || emailEdt.getText().toString().isEmpty()
                    || senhaEdt.getText().toString().isEmpty()) {
                    Toast.makeText(CadUsuario.this, "Insira todos os valores", Toast.LENGTH_SHORT).show();
                    return;
                }  else if (db.ValidacaoEmail(emailEdt.getText().toString())) {
                    Toast.makeText(CadUsuario.this, "Email já utilizado", Toast.LENGTH_SHORT).show();
                }else {
                    postUser(nameEdt.getText().toString(), emailEdt.getText().toString(), senhaEdt.getText().toString());
                    db.addUser(new UserClass(nameEdt.getText().toString(), emailEdt.getText().toString(), senhaEdt.getText().toString()));
                    Toast.makeText(CadUsuario.this, "adicionado com sucesso", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(CadUsuario.this, Login.class);
                    startActivity(intent);

                    finish();

                }
            }
        });
    }

    //------Método para o erro de segurança
    public static OkHttpClient.Builder getUnsafeOkHttpClient() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
            return builder;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void postUser(String name, String email, String senha) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://192.168.0.6:1078/api/V1/")
                .client(getUnsafeOkHttpClient().build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        UserInterface retrofitAPI = retrofit.create(UserInterface.class);
        UserClass modal = new UserClass(name, email, senha);

        Call<UserClass> call = retrofitAPI.createPost(modal);

        call.enqueue(new Callback<UserClass>() {
            @Override
            public void onResponse(Call<UserClass> call, Response<UserClass> response) {

                Toast.makeText(CadUsuario.this, "Data added to API", Toast.LENGTH_SHORT).show();

                emailEdt.setText("");
                nameEdt.setText("");
                UserClass responseFromAPI = response.body();


                String responseString = " id: " + "name : " + responseFromAPI.getName() + "\n" + "email : " + responseFromAPI.getEmail() +
                        "\n" + "password : " + responseFromAPI.getPassword();

                Toast.makeText(CadUsuario.this, responseString, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(Call<UserClass> call, Throwable t) {

                Toast.makeText(CadUsuario.this, "Error found is : " + t.getMessage(), Toast.LENGTH_LONG).show();

            }
        });
    }

}