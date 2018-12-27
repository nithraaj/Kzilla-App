package org.kzilla.srmkzilla

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.util.TypedValue
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.android.synthetic.main.activity_auth.*


class AuthActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    lateinit var context: Context
    lateinit var currentTheme:String
    private lateinit var mAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        currentTheme = sharedPreferences.getString("theme", "light")
        val theme: Int = if ( currentTheme == "light") {
            R.style.LightThemeNoActionBar
        } else {
            R.style.DarkThemeNoActionBar
        }
        setTheme(theme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        context = this
        mAuth = FirebaseAuth.getInstance()
        b_login.setOnClickListener {
            signIn(et_email.text.toString(), et_password.text.toString())
        }
        b_signup.setOnClickListener {
            signUp(et_email.text.toString(), et_password.text.toString())
        }
    }

    private fun signIn(email: String, password: String) {
        b_signup.isEnabled = false
        b_login.isEnabled = false
        google_sign_in.isEnabled = false
        facebook_sign_in.isEnabled = false
        progressBar.isIndeterminate = true
        progressBar.visibility = View.VISIBLE
        error_msg.visibility = View.GONE
        if (isValidEmail(email)) {
            mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        if (!mAuth.currentUser!!.isEmailVerified) {
                            error_msg.text = "Please complete e-mail verification for ${mAuth.currentUser!!.email}"
                            error_msg.visibility = View.VISIBLE
                            progressBar.visibility = View.GONE

                            b_signup.isEnabled = true
                            b_login.isEnabled = true
                            google_sign_in.isEnabled = true
                            facebook_sign_in.isEnabled = true
                        } else {
                            Log.d("SignIn", "UID = " + mAuth.uid!!)
                            checkUserData()
                        }

                    }
                    if (!task.isSuccessful) {
                        Log.w("SignIn", "signInWithEmail:failure", task.exception)
                        if(task.exception!! is FirebaseAuthInvalidUserException){
                            error_msg.text = "Account does not exist\nPlease register"
                        }
                        else{
                            error_msg.text = "Authentication failed."
                        }
                        error_msg.visibility = View.VISIBLE
                        progressBar.visibility = View.GONE

                        b_signup.isEnabled = true
                        b_login.isEnabled = true
                        google_sign_in.isEnabled = true
                        facebook_sign_in.isEnabled = true
                    }
                }
        } else {
            et_email.error = "Invalid Email ID"
            progressBar.visibility = View.GONE
            b_signup.isEnabled = true
            b_login.isEnabled = true
            google_sign_in.isEnabled = true
            facebook_sign_in.isEnabled = true
        }

    }

    private fun signUp(email: String, password: String) {
        b_signup.isEnabled = false
        b_login.isEnabled = false
        google_sign_in.isEnabled = false
        facebook_sign_in.isEnabled = false
        progressBar.isIndeterminate = true
        progressBar.visibility = View.VISIBLE
        error_msg.visibility = View.GONE
        if (isValidEmail(email)) {
            if (isValidPassword(password)) {
                mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            verifyEmail()
                        } else {

                            Log.d("signup",task.exception!!.toString())
                            if(task.exception is FirebaseAuthUserCollisionException){
                                error_msg.text = "Email address already in use"
                            }
                            else{
                                error_msg.text = "Unable to register"
                            }
                            error_msg.visibility = View.VISIBLE
                            progressBar.visibility = View.GONE

                            b_signup.isEnabled = true
                            b_login.isEnabled = true
                            google_sign_in.isEnabled = true
                            facebook_sign_in.isEnabled = true
                        }
                    }
            } else {
                et_password.error = "Password must be atleast 8 characters long"
                progressBar.visibility = View.GONE

                b_signup.isEnabled = true
                b_login.isEnabled = true
                google_sign_in.isEnabled = true
                facebook_sign_in.isEnabled = true
            }
        } else {
            et_email.error = "Invalid Email ID"
            progressBar.visibility = View.GONE

            b_signup.isEnabled = true
            b_login.isEnabled = true
            google_sign_in.isEnabled = true
            facebook_sign_in.isEnabled = true
        }

    }

    private fun verifyEmail() {
        val user = mAuth.currentUser
        val url = "https://srmkzilla.page.link/verify"
        val actionCodeSettings = ActionCodeSettings.newBuilder()
            .setUrl(url)
            //.setIOSBundleId("com.example.ios")
            // The default for this is populated with the current android package name.
            .setAndroidPackageName("org.kzilla.srmkzilla", true, null)
            .setHandleCodeInApp(true)
            .build()

        user!!.sendEmailVerification(actionCodeSettings)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    error_msg.text = String.format("Verification email sent to %s", user.email)
                    error_msg.setTextColor(getColorByThemeAttr(context,R.attr.colorSuccessText))
                    error_msg.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE

                    b_signup.isEnabled = true
                    b_login.isEnabled = true
                    google_sign_in.isEnabled = true
                    facebook_sign_in.isEnabled = true

                } else {
                    Log.d("emailverify", "", task.exception)
                    error_msg.text = String.format("Failed to send verification email. Try Again.")
                    error_msg.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE

                    b_signup.isEnabled = true
                    b_login.isEnabled = true
                    google_sign_in.isEnabled = true
                    facebook_sign_in.isEnabled = true
                    mAuth.currentUser!!.delete()
                }
            }
    }

    private fun isValidEmail(email: String): Boolean {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidPassword(password: String): Boolean {
        return password.length >= 8
    }

    private fun checkUserData() {
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
        val docRef = db.collection("users").document(FirebaseAuth.getInstance().uid!!)
        docRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result

                if (document!!.exists()) {
                    val editor = sharedPreferences.edit()
                    editor.putString("user_name", document.getString("name"))
                    editor.putString("user_regno", document.getString("register_no"))
                    editor.putString("user_phone", document.getString("phone"))
                    editor.putString("user_dept", document.getString("department"))
                    editor.putLong("user_year", document.get("year") as Long)
                    editor.apply()
                    val intentMain = Intent(context, MainActivity::class.java)
                    startActivity(intentMain)
                    finish()
                } else {
                    val intentInfo = Intent(context, AdditionalInfoActivity::class.java)
                    startActivity(intentInfo)
                    finish()
                }
            } else {
                Log.d("checkUserData", "get failed with ", task.exception)
                error_msg.text = "Unable to login. Try again later."
                error_msg.visibility = View.VISIBLE
                progressBar.visibility = View.GONE

                b_signup.isEnabled = true
                b_login.isEnabled = true
                google_sign_in.isEnabled = true
                facebook_sign_in.isEnabled = true
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val theme = sharedPreferences.getString("theme", "light")
        if (currentTheme != theme)
            recreate()
    }

    fun getColorByThemeAttr(context: Context, attr: Int): Int {
        val typedValue = TypedValue()
        val theme = context.theme
        theme.resolveAttribute(attr, typedValue, true)
        return typedValue.data
    }
}
