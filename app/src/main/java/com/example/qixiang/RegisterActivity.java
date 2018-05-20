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
import org.litepal.tablemanager.Connector;

import java.util.List;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCollector.addActivity(this);
        setContentView(R.layout.activity_register);

        Button yes = findViewById(R.id.YES);
        Button no = findViewById(R.id.NO);

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText username = findViewById(R.id.username);
                EditText password = findViewById(R.id.password);
                if (username.getText().length() == 0 || password.getText().length() == 0) {
                    Toast.makeText(RegisterActivity.this, "用户名和密码不能为空", Toast.LENGTH_SHORT).show();
                } else {
                    boolean flag = false;
                    List<User> users = DataSupport.findAll(User.class);
                    for (User user : users) {
                        if (username.equals(user.getUsername())) {
                            flag = true;
                        }
                    }
                    if (flag) {
                        Toast.makeText(RegisterActivity.this, "该用户名已经注册", Toast.LENGTH_SHORT).show();
                    } else {
                        Connector.getDatabase();
                        User user = new User();
                        String strusername = username.getText().toString();
                        String strpassword = password.getText().toString();
                        user.setUsername(strusername);
                        user.setPassword(strpassword);
                        user.save();
                        Toast.makeText(RegisterActivity.this, "创建用户成功", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                }
            }
        });

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }
}