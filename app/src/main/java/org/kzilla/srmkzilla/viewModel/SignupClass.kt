package org.kzilla.srmkzilla.viewModel

import android.app.Application
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException

class SignupClass(application: Application): AndroidViewModel(application) {
    var signupEvent = SingleLiveEvent<SignupEvent>()

    val mAuth = FirebaseAuth.getInstance()
    lateinit var sharedPreferences: SharedPreferences

    fun startSignup(email:String,password:String){
        Log.d("signupEvent",signupEvent.value.toString())
        signupEvent.postValue(SignupEvent(SignupStatus.SignupStart,null,null))
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplication())
        if (isValidEmail(email)) {
            if (isValidPassword(password)) {
                mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        run {
                            if (task.isSuccessful) {
                                verifyEmail()
                            } else {

                                Log.d("signup",task.exception!!.toString())
                                if(task.exception is FirebaseAuthUserCollisionException){
                                    signupEvent.postValue(SignupEvent(SignupStatus.SignupFailed,"Email address already in use","err_msg"))
                                }
                                else{
                                    signupEvent.postValue(SignupEvent(SignupStatus.SignupFailed,"Unable to register","err_msg"))

                                }
                            }
                        }

                    }
            } else {
                signupEvent.postValue(SignupEvent(SignupStatus.SignupFailed,"Password must be atleast 8 characters long","et_password"))


            }
        } else {
            signupEvent.postValue(SignupEvent(SignupStatus.SignupFailed,"Invalid Email ID","et_email"))

        }
    }

    data class SignupEvent(var signupStatus: SignupStatus, var errorMessage: String?, var extraInfo: String?)
    enum class SignupStatus
    {
        SignupStart,
        SignupOk,
        SignupFailed
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
            .addOnCompleteListener { task ->
                run {
                    if (task.isSuccessful) {
                        signupEvent.postValue(SignupEvent(SignupStatus.SignupOk,String.format("Verification email sent to %s", user.email),null))

                    } else {
                        Log.d("emailverify", "", task.exception)
                        signupEvent.postValue(SignupEvent(SignupStatus.SignupFailed,"Failed to send verification email. Try Again.","err_msg"))
                        mAuth.currentUser!!.delete()
                    }
                }

            }
    }

    private fun isValidEmail(email: String): Boolean {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidPassword(password: String): Boolean {
        return password.length >= 8
    }

}