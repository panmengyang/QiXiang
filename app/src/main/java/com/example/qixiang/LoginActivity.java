package com.example.qixiang;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.qixiang.db.User;
import com.example.qixiang.util.ActivityCollector;

import org.litepal.crud.DataSupport;

import java.util.List;

public class LoginActivity extends AppCompatActivity {
    private EditText username;
    private EditText password;
    private Button login, register;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCollector.addActivity(this);
        setContentView(R.layout.activity_login);
        login = findViewById(R.id.btn_login);
        register = findViewById(R.id.btn_register);
        login.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                username = findViewById(R.id.et_1);
                password = findViewById(R.id.et_2);
                boolean flag = false;
                List<User> users = DataSupport.findAll(User.class);
                for (User user : users) {
                    if (user.getUsername().equals(username.getText().toString()) &&
                            user.getPassword().equals(password.getText().toString())) {

                        flag = true;
                    }
                }
                if (flag) {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    String USERNAME = username.getText().toString();
                    intent.putExtra("userName", USERNAME);
                    startActivity(intent);
                } else {
                    Toast.makeText(LoginActivity.this, "用户登录信息错误", Toast.LENGTH_SHORT).show();
                }
            }
        });
        register.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }
}