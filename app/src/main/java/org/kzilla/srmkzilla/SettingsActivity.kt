package org.kzilla.srmkzilla

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {

    lateinit var context: Context
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
        context = this

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
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.toolbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId


        if (id == R.id.settings) {
            val settingIntent = Intent(context, SettingsActivity::class.java)
            startActivity(settingIntent)
        } else if (id == R.id.logout) {
            val mAuth = FirebaseAuth.getInstance()
            mAuth.signOut()
            val intent = Intent(context, AuthActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}
