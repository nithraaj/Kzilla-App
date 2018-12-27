package org.kzilla.srmkzilla

import android.os.Bundle
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val theme: Int
        if (sharedPreferences.getString("theme", "light") == "light") {
            theme = R.style.LightTheme
        } else {
            theme = R.style.DarkTheme
        }
        setTheme(theme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        val context = this

        val editor = sharedPreferences.edit()
        if (sharedPreferences.getString("theme", "light") == "dark") {
            switch_theme.setChecked(true)
        }
        switch_theme.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                editor.putString("theme", "dark")
                editor.apply()
            } else {
                editor.putString("theme", "light")
                editor.apply()
            }
            this@SettingsActivity.recreate()
        }
    }
}
