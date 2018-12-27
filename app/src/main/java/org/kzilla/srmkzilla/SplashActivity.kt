package org.kzilla.srmkzilla

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.android.synthetic.main.activity_splash.*
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket


class SplashActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private var startTime:Long = System.currentTimeMillis()
    private lateinit var mAuth:FirebaseAuth
    lateinit var context:Context
    override fun onCreate(savedInstanceState: Bundle?) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val theme: Int = if (sharedPreferences.getString("theme", "light") == "light") {
            R.style.LightLauncher
        } else {
            R.style.DarkLauncher
        }
        setTheme(theme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        context = this
        mAuth = FirebaseAuth.getInstance()
        checkInternet()
    }

    private fun checkInternet() {
        val consumer = object : Consumer {
            override fun accept(internet: Boolean?) {
                checkUser(internet!!)
            }
        }
        InternetCheck(consumer)
    }

    private fun checkUser(online: Boolean) {
        if (mAuth.currentUser != null && mAuth.currentUser!!.isEmailVerified) {
            // Already logged in
            checkUserData(online)
            Log.d("SignIn", "UID = " + mAuth.uid!!)
        } else if (mAuth.currentUser != null) {
            // Email Verification not Completed
            mAuth.currentUser!!
                .reload()
                .addOnSuccessListener {
                    if (!mAuth.currentUser!!.isEmailVerified) {
                        error_msg.text = "Please complete e-mail verification for ${mAuth.currentUser!!.email}"
                        error_msg.visibility = View.VISIBLE
                        openEmail.text = "Open Email App"
                        openEmail.setOnClickListener {
                            /*
                            val emailLauncher = Intent(Intent.ACTION_VIEW)
                            //emailLauncher.setClassName("com.google.android.gm", "com.google.android.gm.ConversationListActivityGmail")
                            try {
                                //startActivity(emailLauncher)
                            } catch (e: ActivityNotFoundException) {
                                Toast.makeText(context,"No Email app found", Toast.LENGTH_SHORT).show()
                                openEmail.visibility = View.GONE
                            }
                            */
                            searchMailApps()

                        }
                        openEmail.visibility = View.VISIBLE
                        differentAccount.text = "Proceed with different account"
                        differentAccount.setOnClickListener {
                            mAuth.signOut()
                            val intentAuth = Intent(context, AuthActivity::class.java)
                            intentAuth.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                            startActivity(intentAuth)
                            finish()
                        }
                        differentAccount.visibility = View.VISIBLE
                    }
                }
                .addOnFailureListener { e ->
                    Log.d("emailverifycheck", e.message)
                    if(online) {
                        error_msg.text = "Unable to check email verification status"
                    }
                    else{
                        error_msg.text = "Internet connection unavailable"
                    }
                    error_msg.visibility = View.VISIBLE
                }

        } else {
            // Not logged in
            if (online) {
                val endTime = System.currentTimeMillis()
                if (endTime - startTime > 1000) {
                    val intentAuth = Intent(context, AuthActivity::class.java)
                    intentAuth.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    startActivity(intentAuth)
                    finish()
                } else {
                    Handler().postDelayed({
                        val intentAuth = Intent(context, AuthActivity::class.java)
                        intentAuth.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                        startActivity(intentAuth)
                        finish()
                    }, startTime + 1000 - endTime)
                }
            } else {
                error_msg.text = "Internet connection unavailable"
                error_msg.visibility = View.VISIBLE
            }


        }
    }

    private fun checkUserData(online: Boolean) {
        val db = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .setTimestampsInSnapshotsEnabled(true)
            .build()
        db.setFirestoreSettings(settings)
        /*
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(false)
                .build();
        db.setFirestoreSettings(settings);
        */
        val docRef = db.collection("users").document(mAuth.uid!!)
        docRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()){
                    val editor = sharedPreferences.edit()
                    editor.putString("user_name", document.getString("name"))
                    editor.putString("user_regno", document.getString("register_no"))
                    editor.putString("user_phone", document.getString("phone"))
                    editor.putString("user_dept", document.getString("department"))
                    editor.putLong("user_year", document.get("year") as Long)
                    editor.apply()
                    val endTime = System.currentTimeMillis()
                    if (endTime - startTime > 1000) {
                        val intentMain = Intent(context, MainActivity::class.java)
                        intentMain.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                        startActivity(intentMain)
                        finish()
                    } else {
                        Handler().postDelayed({
                            val intentMain = Intent(context, MainActivity::class.java)
                            intentMain.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                            startActivity(intentMain)
                            finish()
                        }, startTime + 1000 - endTime)
                    }
                }
                else{
                    val endTime = System.currentTimeMillis()
                    if (endTime - startTime > 1000) {
                        val intentInfo = Intent(context, AdditionalInfoActivity::class.java)
                        intentInfo.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                        startActivity(intentInfo)
                        finish()
                    } else {
                        Handler().postDelayed({
                            val intentInfo = Intent(context, AdditionalInfoActivity::class.java)
                            intentInfo.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                            startActivity(intentInfo)
                            finish()
                        }, startTime + 1000 - endTime)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.d("checkUserData", "get failed with ${e.message}")
                if (online) {
                    error_msg.text = "Unable to login. Try again later"
                } else {
                    error_msg.text = "Internet connection unavailable"
                }
                error_msg.visibility = View.VISIBLE
            }
    }

    internal class InternetCheck (private val mConsumer: Consumer) :
        AsyncTask<Void, Void, Boolean>() {
        init {
            execute()
        }

        override fun doInBackground(vararg voids: Void): Boolean? {
            return try {
                val sock = Socket()
                sock.connect(InetSocketAddress("8.8.8.8", 53), 1500)
                sock.close()
                true
            } catch (e: IOException) {
                false
            }

        }

        override fun onPostExecute(internet: Boolean?) {
            mConsumer.accept(internet)
        }
    }

    interface Consumer {
        fun accept(internet: Boolean?)
    }

    fun searchMailApps() {
        try {
            val pm = context.packageManager
            var selectedEmailActivity: ResolveInfo? = null

            val emailDummyIntent = Intent(Intent.ACTION_SENDTO)
            emailDummyIntent.data = Uri.parse("mailto:some@emaildomain.com")

            var emailActivities: List<ResolveInfo>? = pm.queryIntentActivities(emailDummyIntent, 0)

            if (null == emailActivities || emailActivities.size == 0) {
                Log.d("email","mailto=0")
                val emailDummyIntentRFC822 = Intent(Intent.ACTION_SEND_MULTIPLE)
                emailDummyIntentRFC822.type = "message/rfc822"

                emailActivities = pm.queryIntentActivities(emailDummyIntentRFC822, 0)
            }

            if (null != emailActivities) {
                if (emailActivities.size == 1) {
                    selectedEmailActivity = emailActivities[0]
                } else {
                    for (currAvailableEmailActivity in emailActivities) {
                        if (true == currAvailableEmailActivity.isDefault) {
                            selectedEmailActivity = currAvailableEmailActivity
                        }
                    }
                }

                if (null != selectedEmailActivity) {
                    // Send email using the only/default email activity
                    launchEmailApp(selectedEmailActivity)
                } else {
                    val emailActivitiesForDialog = emailActivities

                    val availableEmailAppsName = arrayOfNulls<String>(emailActivitiesForDialog.size)
                    for (i in emailActivitiesForDialog.indices) {
                        availableEmailAppsName[i] =
                                emailActivitiesForDialog[i].activityInfo.applicationInfo.loadLabel(pm).toString()
                    }

                    val builder = AlertDialog.Builder(context)
                    builder.setTitle("Select email app")
                    builder.setItems(availableEmailAppsName
                    ) { dialog, which ->
                        launchEmailApp(emailActivitiesForDialog[which])
                    }

                    builder.create().show()
                }
            } else {

                Log.d("email","rfc822=0")
                Toast.makeText(context,"No Email app found", Toast.LENGTH_SHORT).show()
                openEmail.visibility = View.GONE
            }
        } catch (ex: Exception) {
            Log.e("email", "Can't send email", ex)
        }

    }

    fun launchEmailApp(p_selectedEmailApp: ResolveInfo) {
        val emailIntent = context.packageManager.getLaunchIntentForPackage(p_selectedEmailApp.activityInfo.packageName)
        context.startActivity(emailIntent)
    }


}
