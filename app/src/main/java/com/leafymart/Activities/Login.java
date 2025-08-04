package com.leafymart.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.leafymart.Structure.ApiConfig;
import com.leafymart.Structure.MySingleton;
import com.leafymart.Manager.FavoriteManager;
import com.leafymart.R;
import com.leafymart.Manager.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

public class Login extends AppCompatActivity {

    EditText email, password;
    Button login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        email = findViewById(R.id.user_login);
        password = findViewById(R.id.user_password);
        login = findViewById(R.id.btn_login);


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String usr_email, usr_passwrod;


                usr_email = email.getText().toString();
                usr_passwrod = password.getText().toString();

                if (usr_email.isEmpty() || usr_passwrod.isEmpty()) {

                    Toast.makeText(Login.this, "Please Compelete all the information for login", Toast.LENGTH_SHORT).show();
                    return;
                } else {

                    // Login code will be done here


                    String url = ApiConfig.BASE_URL + "/login";

                    JSONObject jsonBody = new JSONObject();
                    try {
                        jsonBody.put("email", usr_email);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        jsonBody.put("password", usr_passwrod);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                    JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url,
                            jsonBody, response -> {


                        try {
                            String message = response.getString("message");
                            if (message.equals("Login success")) {
                                int userId = response.getInt("user_id");
                                String name = response.getString("name");

                                Toast.makeText(Login.this, "Welcome " + name, Toast.LENGTH_SHORT).show();

                                // Save user ID globally (optional)
// Save user ID globally
                                SessionManager.getInstance(getApplicationContext()).setUserId(userId);
                                SessionManager.getInstance(getApplicationContext()).setUserName(name);
                                SessionManager.getInstance(getApplicationContext()).saveUserId(userId);
// ✅ Set userId in FavoriteManager (THIS USES setUserId METHOD)
                                FavoriteManager.get(getApplicationContext()).setUserId(userId);

// ✅ Pull favorites from server after login (USES pullFromServer METHOD)
                                FavoriteManager.get(getApplicationContext()).pullFromServer();

                                // Redirect to main or dashboard
                                Intent intent = new Intent(Login.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(Login.this, "Login failed", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(Login.this, "Unexpected error", Toast.LENGTH_SHORT).show();
                        }
                    },
                            error -> {
                                Toast.makeText(Login.this, "Login failed: " + error.getMessage(), Toast.LENGTH_LONG).show();
                                error.printStackTrace();
                            }
                    );

                    MySingleton.get(Login.this).add(request);

                }
            }
        });
    }
}