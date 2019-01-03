package org.kzilla.srmkzilla

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.util.TypedValue
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_auth.*
import org.kzilla.srmkzilla.viewModel.LoginClass
import org.kzilla.srmkzilla.viewModel.SignupClass


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
        val loginClass = ViewModelProviders.of(this).get(LoginClass::class.java)
        val signupClass = ViewModelProviders.of(this).get(SignupClass::class.java)
        loginClass.loginEvent.observe(this@AuthActivity, Observer {
            if(it==null)
                return@Observer
            Log.d("loginObserver","stateChange to "+it.loginStatus)

            updateLoginUI(it)
        })
        signupClass.signupEvent.observe(this@AuthActivity, Observer {
            if(it==null)
                return@Observer
            updateSignupUI(it)
        })
        if(loginClass.loginEvent.value!=null){
            Log.d("onCreate","loginEvent not null, state="+ loginClass.loginEvent.value!!.loginStatus)
            updateLoginUI(loginClass.loginEvent.value!!)
        }
        else{
            Log.d("onCreate","loginEvent is null")
        }
        if(signupClass.signupEvent.value!=null){
            Log.d("onCreate","signupEvent not null, state="+ signupClass.signupEvent.value!!.signupStatus)
            updateSignupUI(signupClass.signupEvent.value!!)
        }
        else{
            Log.d("onCreate","signupEvent is null")
        }
        b_login.setOnClickListener {
            doAsync {
                loginClass.startLogin(et_email.text.toString(), et_password.text.toString())
            }.execute()

            //signIn(et_email.text.toString(), et_password.text.toString())
        }
        b_signup.setOnClickListener {
            doAsync {
                signupClass.startSignup(et_email.text.toString(), et_password.text.toString())
            }.execute()
            //signUp(et_email.text.toString(), et_password.text.toString())
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

    fun updateLoginUI(loginData:LoginClass.LoginEvent){
        Log.d("updateUI","updating UI")
        when(loginData.loginStatus){
            LoginClass.LoginStatus.LoginStart -> {
                b_signup.isEnabled = false
                b_login.isEnabled = false
                google_sign_in.isEnabled = false
                facebook_sign_in.isEnabled = false
                progressBar.isIndeterminate = true
                progressBar.visibility = View.VISIBLE
                error_msg.visibility = View.GONE
            }
            LoginClass.LoginStatus.LoginFailed -> {
                if(loginData.extraInfo=="err_msg"){
                    error_msg.text = loginData.errorMessage
                    error_msg.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                    b_signup.isEnabled = true
                    b_login.isEnabled = true
                    google_sign_in.isEnabled = true
                    facebook_sign_in.isEnabled = true
                }
                else if(loginData.extraInfo=="et_email"){
                    et_email.error = loginData.errorMessage
                }
            }
            LoginClass.LoginStatus.LoginOk -> {
                var intent:Intent? = null
                if(loginData.extraInfo=="MainActivity"){
                    intent = Intent(context, MainActivity::class.java)
                }
                else if(loginData.extraInfo=="AdditionalInfoActivity"){
                    intent = Intent(context,AdditionalInfoActivity::class.java)
                }
                startActivity(intent)
                finish()
            }
        }
    }

    fun updateSignupUI(signupData:SignupClass.SignupEvent){
        when(signupData.signupStatus){
            SignupClass.SignupStatus.SignupStart -> {
                b_signup.isEnabled = false
                b_login.isEnabled = false
                google_sign_in.isEnabled = false
                facebook_sign_in.isEnabled = false
                progressBar.isIndeterminate = true
                progressBar.visibility = View.VISIBLE
                error_msg.visibility = View.GONE
            }
            SignupClass.SignupStatus.SignupFailed -> {
                if(signupData.extraInfo=="err_msg"){
                    error_msg.text = signupData.errorMessage
                    error_msg.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE

                    b_signup.isEnabled = true
                    b_login.isEnabled = true
                    google_sign_in.isEnabled = true
                    facebook_sign_in.isEnabled = true
                }
                else if(signupData.extraInfo=="et_email"){
                    et_email.error=signupData.errorMessage
                }
                else if(signupData.extraInfo=="et_password"){
                    et_password.error=signupData.errorMessage
                }
            }
            SignupClass.SignupStatus.SignupOk -> {
                error_msg.text = signupData.errorMessage
                error_msg.setTextColor(getColorByThemeAttr(context,R.attr.colorSuccessText))
                error_msg.visibility = View.VISIBLE
                progressBar.visibility = View.GONE

                b_signup.isEnabled = true
                b_login.isEnabled = true
                google_sign_in.isEnabled = true
                facebook_sign_in.isEnabled = true

                //todo: show button to open email app
            }
        }
    }

    class doAsync(val handler: () -> Unit) : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void?): Void? {
            handler()
            return null
        }
    }
}
