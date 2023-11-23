package com.foo.bolme;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.foo.bolme.databinding.ActivitySpamBinding;

public class Spam extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ActivitySpamBinding spamBinding = ActivitySpamBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(spamBinding.getRoot());


    }
}