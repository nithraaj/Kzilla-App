package org.kzilla.srmkzilla

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_additional_info.*
import java.util.*


class AdditionalInfoActivity : AppCompatActivity() {

    internal var departments = arrayOf("Computer Science and Engineering ( CSE )", "Computer Science and Engineering - Artificial Intelligence and Machine Learning ( CSE AI )",
        "Computer Science and Engineering - Big Data Analytics ( CSE BD )","Computer Science and Engineering - Cloud Computing ( CSE CC )",
        "Computer Science and Engineering - Computer Networking ( CSE CN )","Computer Science and Engineering - Cyber Security ( CSE CS )",
        "Computer Science and Engineering - Information Technology ( CSE IT )","Computer Science and Engineering - Internet of Things ( CSE IOT )",
        "Computer Science and Engineering - Software Engineering ( CSE SWE )","Information Technology ( IT )", "Aerospace Engineering",
        "Automobile Engineering", "Biomedical Engineering", "Biotechnology","Chemical Engineering","Civil Engineering",
        "Electrical and Electronics Engineering ( EEE )","Electronics and Communication Engineering ( ECE )",
        "Electronics and Instrumentation Engineering ( EIE )", "Instrumentation and Control Engineering","Mechanical Engineering",
        "Mechatronics Engineering","Nanotechnology", "Software Engineering ( SWE )","Telecommunication Engineering")
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
        setContentView(R.layout.activity_additional_info)
        context = this
        val deptAdapter =
            object : ArrayAdapter<String>(this, R.layout.spinner_item, android.R.id.text1, departments) {
                override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val v = super.getDropDownView(position, convertView, parent)
                    v.post { (v.findViewById<View>(android.R.id.text1) as TextView).setSingleLine(false) }
                    return v
                }
            }
        deptAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dept.setAdapter(deptAdapter)
        submit.setOnClickListener {
            submit.isEnabled = false
            progressBar.isIndeterminate = true
            progressBar.visibility = View.VISIBLE
            validateData()
        }
    }

    private fun validateData() {
        var valid = true
        if (name.text.toString().trim { it <= ' ' }.isEmpty()) {
            valid = false
            name.error = "Name cannot be empty"
        }
        if (regno.text.toString().trim { it <= ' ' }.length != 15) {
            valid = false
            regno.error = "Invalid Register number"
        }
        if (phone.text.toString().trim { it <= ' ' }.length != 10) {
            valid = false
            phone.error = "Invalid phone number"
        }
        if (!Arrays.asList(*departments).contains(dept.text.toString().trim { it <= ' ' })) {
            valid = false
            dept.error = "Invalid department"
        }
        if (valid) {
            saveAdditionalInfo(
                name.text.toString().trim({ it <= ' ' }),
                regno.text.toString().trim({ it <= ' ' }).toUpperCase(),
                phone.text.toString().trim({ it <= ' ' }),
                dept.text.toString().trim({ it <= ' ' }),
                year.selectedItemPosition + 1
            )
        }
        else{
            submit.isEnabled = true
            progressBar.visibility = View.GONE
        }

    }

    private fun saveAdditionalInfo(name: String, regno: String, phone: String, dept: String, year: Int) {
        val db = FirebaseFirestore.getInstance()
        val user = HashMap<String, Any>()
        user["name"] = name
        user["register_no"] = regno
        user["phone"] = phone
        user["department"] = dept
        user["year"] = year

        db.collection("users").document(FirebaseAuth.getInstance().uid!!)
            .set(user)
            .addOnSuccessListener {
                val editor = sharedPreferences.edit()
                editor.putString("user_name", name)
                editor.putString("user_regno", regno)
                editor.putString("user_phone", phone)
                editor.putString("user_dept", dept)
                editor.putInt("user_year", year)
                editor.apply()
                val intent = Intent(context, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Unable to save information", Toast.LENGTH_LONG).show()
                submit.isEnabled = true
                progressBar.visibility = View.GONE

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

    override fun onResume() {
        super.onResume()
        val theme = sharedPreferences.getString("theme", "light")
        if (currentTheme != theme)
            recreate()
    }
}
