package org.kzilla.srmkzilla

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    lateinit var context: Context
    lateinit var currentTheme:String
    override fun onCreate(savedInstanceState: Bundle?) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        currentTheme = sharedPreferences.getString("theme", "light")
        val theme: Int = if ( currentTheme == "light") {
            R.style.LightTheme
        } else {
            R.style.DarkTheme
        }
        setTheme(theme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        context = this
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
    override fun onResume() {
        super.onResume()
        val theme = sharedPreferences.getString("theme", "light")
        if (currentTheme != theme)
            recreate()
    }
}
