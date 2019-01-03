package org.kzilla.srmkzilla

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthActionCodeException
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks

class DeepLinkActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var mAuth: FirebaseAuth
    lateinit var context: Context
    override fun onCreate(savedInstanceState: Bundle?) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val theme: Int = if (sharedPreferences.getString("theme", "light") == "light") {
            R.style.LightLauncher
        } else {
            R.style.DarkLauncher
        }
        setTheme(theme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deep_link)
        context = this
        mAuth = FirebaseAuth.getInstance()
        FirebaseDynamicLinks.getInstance()
            .getDynamicLink(intent)
            .addOnSuccessListener(this) { pendingDynamicLinkData ->
                // Get deep link from result (may be null if no link is found)
                var deepLink: Uri? = null
                if (pendingDynamicLinkData != null) {
                    deepLink = pendingDynamicLinkData!!.link
                }
                if (deepLink != null) {
                    Log.d("deeplinksegments", deepLink!!.pathSegments.toString())
                    Log.d("deeplink", deepLink!!.toString())
                    Log.d("deeplink host", deepLink!!.host)
                    if (deepLink!!.host == "kzilla-app.firebaseapp.com") {
                        mAuth = FirebaseAuth.getInstance()
                        mAuth.applyActionCode(deepLink!!.getQueryParameter("oobCode")!!)
                            .addOnSuccessListener {
                                val intent_info = Intent(context, AdditionalInfoActivity::class.java)
                                intent_info.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                                startActivity(intent_info)
                                finish()
                            }
                            .addOnFailureListener {
                                Log.d("emailverify",it.toString())
                                if(it is FirebaseAuthActionCodeException){
                                    Toast.makeText(context, "Verification link expired", Toast.LENGTH_LONG).show()
                                }
                                else {
                                    Toast.makeText(context, "Unknown link. Try updating app.", Toast.LENGTH_LONG).show()
                                }
                                finish()
                            }
                    } else if (deepLink.lastPathSegment == "events") run {
                        // todo: open registration activity
                        val intent = Intent(context, RegistrationActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        val event_id = deepLink.getQueryParameter("event_id")
                        intent.putExtra("event_id", event_id)
                        context.startActivity(intent)
                        finish()

                    } else {
                        Toast.makeText(context, "Unknown link. Try updating app.", Toast.LENGTH_LONG).show()
                        finish()
                    }
                }
            }
            .addOnFailureListener(this) {
                //Log.w(TAG, "getDynamicLink:onFailure", e);
                Toast.makeText(context, "Unknown link. Try updating app.", Toast.LENGTH_LONG).show()
                finish()
            }
    }
}
