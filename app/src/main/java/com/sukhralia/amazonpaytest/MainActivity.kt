package com.sukhralia.amazonpaytest

import amazonpay.silentpay.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import com.amazon.identity.auth.device.AuthError
import com.amazon.identity.auth.device.api.Listener
import com.amazon.identity.auth.device.api.authorization.*
import com.amazon.identity.auth.device.api.workflow.RequestContext


class MainActivity : AppCompatActivity() {

    private lateinit var requestContext : RequestContext
    private lateinit var myContext: Context
    private lateinit var mHandler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestContext = RequestContext.create(this);

        myContext = this

        requestContext.registerListener(object : AuthorizeListener() {
            /* Authorization was completed successfully. */
            override fun onSuccess(result: AuthorizeResult) {
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(myContext, "Authorized", Toast.LENGTH_SHORT).show()
                }
            }

            /* There was an error during the attempt to authorize the
        application. */
            override fun onError(ae: AuthError) {
                Toast.makeText(myContext,"Error Authorizing",Toast.LENGTH_SHORT).show()
            }

            /* Authorization was cancelled before it could be completed. */
            override fun onCancel(cancellation: AuthCancellation) {
                /* Reset the UI to a ready-to-login state */
            }
        })

        val login = findViewById<Button>(R.id.login)

        login.setOnClickListener {
            AuthorizationManager.authorize(AuthorizeRequest.Builder(requestContext)
                    .addScopes(ProfileScope.profile(), ProfileScope.postalCode())
                    .build())
        }


        val loginOut = findViewById<Button>(R.id.login_out)

        loginOut.setOnClickListener {
            AuthorizationManager.signOut(applicationContext,object : Listener<Void, AuthError?>{
                override fun onSuccess(p0: Void?) {
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(myContext, "Logged Out", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onError(p0: AuthError?) {
                    Toast.makeText(myContext,"Error Logging Out",Toast.LENGTH_SHORT).show()
                }

            })
        }
        val balance = findViewById<Button>(R.id.get_balance)

        val builder = CustomTabsIntent.Builder()
            .setToolbarColor(Color.BLACK)
            .build();

        startActivityForResult(AmazonPay.getAuthorizationIntent(this , builder), 7)

        balance.setOnClickListener {
            abc()
        }

    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        if (requestCode == 7) {
            val aPayAuthResult = APayAuthorizationResult.fromIntent(intent)
        } else {
            Log.e(this.localClassName, "received no response")
        }
        super.onActivityResult(requestCode, resultCode, data)
    }


    override fun onStart() {
        super.onStart()
        val scopes = arrayOf(
                ProfileScope.profile(),
                ProfileScope.postalCode()
        )
        AuthorizationManager.getToken(this, scopes, object : Listener<AuthorizeResult?, AuthError?> {


            override fun onError(ae: AuthError?) {
                Toast.makeText(myContext,"Not Signed In",Toast.LENGTH_SHORT).show()
            }

            override fun onSuccess(p0: AuthorizeResult?) {
                if (p0?.accessToken != null) {
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(myContext, "Already Signed In", Toast.LENGTH_SHORT).show()
                    }

                }
            }

        })
    }

    fun abc(){
        val isSandbox = true //true indicates sandbox mode

        val request: GetBalanceRequest = GetBalanceRequest(
            "ARU24JXUG3S4L",
            isSandbox
        )
        AmazonPay.getBalance(this, request, object : APayCallback {
            override fun onSuccess(bundle: Bundle?) {
                val response = GetBalanceResponse.fromBundle(bundle)
                runOnUiThread {
                    // Handle the response
                }
            }

            override fun onError(aPayError: APayError?) {
                runOnUiThread {
                    // Handle the error
                }
            }
        })


    }

    override fun onResume() {
        super.onResume()
        requestContext.onResume()
    }
}