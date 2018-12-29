package org.kzilla.srmkzilla.viewModel

import android.app.Application
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class LoginClass(application: Application): AndroidViewModel(application){
    var loginEvent = SingleLiveEvent<LoginEvent>()
    val mAuth = FirebaseAuth.getInstance()
    lateinit var sharedPreferences:SharedPreferences
    fun startLogin(email: String, password: String)
    {
        Log.d("loginEvent", loginEvent.value.toString())
        loginEvent.postValue(LoginEvent(LoginStatus.LoginStart, null, null))
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplication())
        if (isValidEmail(email)) {
            mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    run {
                        if (task.isSuccessful) {
                            if (!mAuth.currentUser!!.isEmailVerified) {
                                loginEvent.postValue(
                                    LoginEvent(
                                        LoginStatus.LoginFailed,
                                        "Please complete e-mail verification for ${mAuth.currentUser!!.email}",
                                        "err_msg"
                                    )
                                )
                            } else {
                                Log.d("SignIn", "UID = " + mAuth.uid!!)
                                checkUserData()
                            }

                        }
                        if (!task.isSuccessful) {
                            Log.w("SignIn", "signInWithEmail:failure", task.exception)
                            if (task.exception!! is FirebaseAuthInvalidUserException) {
                                loginEvent.postValue(
                                    LoginEvent(
                                        LoginStatus.LoginFailed,
                                        "Account does not exist\nPlease register",
                                        "err_msg"
                                    )
                                )
                            } else {
                                Log.d("loginclass", "authentication failed")
                                loginEvent.postValue(
                                    LoginEvent(
                                        LoginStatus.LoginFailed,
                                        "Authentication failed",
                                        "err_msg"
                                    )
                                )
                            }
                        }
                    }
                }
        } else {
            loginEvent.postValue(LoginEvent(LoginStatus.LoginFailed, "Invalid Email ID", "et_email"))
        }
        Log.d("startLogin", "startLogin complete")

    }
    data class LoginEvent(var loginStatus: LoginStatus, var errorMessage: String?, var extraInfo: String?)
    enum class LoginStatus
    {
        LoginStart,
        LoginOk,
        LoginFailed
    }

    private fun isValidEmail(email: String): Boolean {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
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
                    loginEvent.postValue(LoginEvent(LoginStatus.LoginOk,null,"MainActivity"))
                } else {
                    loginEvent.postValue(LoginEvent(LoginStatus.LoginOk,null,"AdditionalInfoActivity"))
                }
            } else {
                Log.d("checkUserData", "get failed with ", task.exception)
                loginEvent.postValue(LoginEvent(LoginStatus.LoginFailed,"Unable to login. Try again later","err_msg"))
            }
        }
    }

}