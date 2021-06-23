package com.example.android.fillmeinfixed;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class WelcomeActivity extends AppCompatActivity {

    private ImageView imageView;
    private ImageView fillRectImageView;
    private TextView textView;
    private Animation enterAnimation;
    private Animation exitAnimation;
    private Animation fadeInAnimation;
    private Animation fadeOutAnimation;
    private boolean isClickable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // set bars at top and bottom to transparent
        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        imageView = findViewById(R.id.buttplugImageView);
        fillRectImageView = findViewById(R.id.fillRectImageView);
        textView = findViewById(R.id.textView);

        enterAnimation = AnimationUtils.loadAnimation(this, R.anim.enter);
        exitAnimation = AnimationUtils.loadAnimation(this, R.anim.exit);
        fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        fadeOutAnimation = AnimationUtils.loadAnimation(this,R.anim.fade_out);


        final Context base = this;
        enterAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                // fade in text
                textView.startAnimation(fadeInAnimation);

                isClickable = true;

                // reload animation to clear animation listener for when this is reused by fillRectImageView
                // there's gotta be a function to just clear the listener but I can't find it
                enterAnimation = AnimationUtils.loadAnimation(base, R.anim.enter);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        exitAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                isClickable = false;

                // fade out text
                // either here or onclick before this animation starts, stylistic choice
                AnimationSet as = new AnimationSet(false);
                as.addAnimation(exitAnimation);
                as.addAnimation(fadeOutAnimation);
                textView.startAnimation(as);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // start next activity
                Intent intent = new Intent(base, DeviceScanActivity.class);
                startActivity(intent);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });


        imageView.startAnimation(enterAnimation);
    }

    public void onClick(View view) {
        // check animation state
        if (isClickable) {
            imageView.startAnimation(exitAnimation);
            fillRectImageView.startAnimation(enterAnimation);
        }
    }
}