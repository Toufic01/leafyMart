package com.leafymart.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.*;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.leafymart.R;
import com.leafymart.Structure.ApiConfig;
import com.leafymart.Structure.MySingleton;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Registration extends AppCompatActivity {

    EditText name, email, password, repassword;
    Button submit, already_exit_account;
    ImageView profileImage;
    Bitmap bitmap;
    final int IMAGE_REQUEST = 1;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registration);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        name = findViewById(R.id.user_name);
        email = findViewById(R.id.user_email);
        password = findViewById(R.id.user_password);
        repassword = findViewById(R.id.retype_password);
        submit = findViewById(R.id.btn_login);
        already_exit_account = findViewById(R.id.btn_already_have_account);
        profileImage = findViewById(R.id.profile_image_preview);

        // Open gallery to pick image
        profileImage.setOnClickListener(v -> openGallery());

        // Already have account
        already_exit_account.setOnClickListener(v -> {
            Intent intent = new Intent(Registration.this, Login.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        // Submit registration
        submit.setOnClickListener(v -> {
            String usr_name = name.getText().toString().trim();
            String usr_email = email.getText().toString().trim();
            String usr_password = password.getText().toString().trim();
            String usr_repassword = repassword.getText().toString().trim();

            if (usr_name.isEmpty() || usr_email.isEmpty() || usr_password.isEmpty() || usr_repassword.isEmpty()) {
                Toast.makeText(Registration.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!usr_password.equals(usr_repassword)) {
                Toast.makeText(Registration.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            if (bitmap == null) {
                Toast.makeText(Registration.this, "Please select a profile image", Toast.LENGTH_SHORT).show();
                return;
            }

            // Convert image to Base64
            String encodedImage = imageToString(bitmap);

            // Prepare JSON
            JSONObject jsonBody = new JSONObject();
            try {
                jsonBody.put("name", usr_name);
                jsonBody.put("email", usr_email);
                jsonBody.put("password", usr_password);
                jsonBody.put("profile_image", encodedImage);
            } catch (Exception e) {
                Toast.makeText(Registration.this, "Error forming request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            // Send request
            String url = ApiConfig.BASE_URL + "/register";
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                    response -> {
                        Toast.makeText(Registration.this, "Registration successful", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), Login.class));
                        finish();
                    },
                    error -> Toast.makeText(Registration.this, "Registration failed: " + error.getMessage(), Toast.LENGTH_SHORT).show()
            );

            MySingleton.get(Registration.this).add(request);
        });
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Profile Image"), IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                profileImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String imageToString(Bitmap bmp) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
        byte[] imageBytes = outputStream.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.NO_WRAP);
    }
}
