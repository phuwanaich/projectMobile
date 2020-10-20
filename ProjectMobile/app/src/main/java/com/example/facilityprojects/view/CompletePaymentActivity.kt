package com.example.facilityprojects.view

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.facilityprojects.R
import com.example.facilityprojects.model.Server
import io.paperdb.Paper
import kotlinx.android.synthetic.main.activity_complete_payment.*
import kotlin.collections.HashMap

class CompletePaymentActivity : AppCompatActivity() {

    var phone = ""
    var id = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_complete_payment)

        Paper.init(this@CompletePaymentActivity)
        var idUser = Paper.book().read("idUser", "")

        var intent = intent
        var type = intent.getStringExtra("type") // Get from PaymentActivity
        if (type.equals("paypal")) {
            var amount = intent.getStringExtra("amount")
            textView_notify.text = "You have successfully paid for this order !"
            textView_amount.text = amount + " $"
        } else if (type.equals("email")) {
            var amount = intent.getStringExtra("amount")
            textView_notify.text = "You have successfully placed an order for this order. " +
                    "Please check your email address to complete your order !"
            textView_amount.text = amount + " $"
        }

        // Get SharePreferences for reload data when order success
        var pref: SharedPreferences = getSharedPreferences("dataPref", Context.MODE_PRIVATE)
        var type2 =  pref.getString("type2", "null")// Get from HomeActivity
        if (type2 == "normal") {
            phone = pref.getString("phoneCP", "null")!!
        } else if (type2 == "facebook") {
            id = pref.getString("id", "null")!!
        } else if (type2 == "google") {
            id = pref.getString("id", "null")!!
        }

        textView_continue_buying.setOnClickListener {
            continueBuying(type2!!, idUser)
        }
    }

    private fun continueBuying(type2: String, idUser: String) {
        // remove cart from idUser
        var requestQueue = Volley.newRequestQueue(this@CompletePaymentActivity)
        var stringRequest = object : StringRequest(Method.POST, Server.removeAllCart, object : Response.Listener<String>{

            override fun onResponse(response: String?) {
                if (response == "Success") {
                    // Send info to HomeActivity for reload data
                    var intent = Intent(this@CompletePaymentActivity, HomeActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    if (type2 == "normal") {
                        intent.putExtra("type", type2)
                        intent.putExtra("phoneNumber", phone)
                        startActivity(intent)
                    } else if (type2 == "facebook") {
                        intent.putExtra("type", type2)
                        intent.putExtra("idFb", id)
                        startActivity(intent)
                    } else if (type2 == "google") {
                        intent.putExtra("type", type2)
                        intent.putExtra("idGoogle", id)
                        startActivity(intent)
                    }
                }
            }

        }, object: Response.ErrorListener{
            override fun onErrorResponse(error: VolleyError?) {
                Log.d("Error", error.toString())
            }

        }){
            override fun getParams(): MutableMap<String, String> {
                var hashMap: HashMap<String, String> = HashMap()
                hashMap.put("idUser", idUser)
                return hashMap
            }
        }
        requestQueue.add(stringRequest)
    }


}
