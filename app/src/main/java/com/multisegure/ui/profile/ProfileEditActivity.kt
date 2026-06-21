package com.multisegure.ui.profile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.multisegure.ui.theme.MultiSegureTheme

class ProfileEditActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val profileId = intent.getIntExtra("profile_id", 0)
        setContent {
            MultiSegureTheme {
                ProfileEditScreen(
                    profileId = profileId,
                    onFinish = { finish() }
                )
            }
        }
    }
}
