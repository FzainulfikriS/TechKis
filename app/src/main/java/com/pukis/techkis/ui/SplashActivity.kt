package com.pukis.techkis.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.pukis.techkis.BuildConfig
import com.pukis.techkis.R
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val versiAplikasi = BuildConfig.VERSION_NAME
        tv_versiAplikasi_splash.setText(versiAplikasi.toString())

        iv_aplikasiLogo_splash.apply {
            alpha = 0f
            visibility = View.VISIBLE
            animate()
                .setDuration(1500L)
                .alpha(1f)
                .setListener(object: AnimatorListenerAdapter(){
                    override fun onAnimationEnd(animation: Animator?) {
                        startActivity(Intent(this@SplashActivity,HomeActivity::class.java))
                        finish()
                    }
                })
        }
    }
}