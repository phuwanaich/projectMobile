package com.example.facilityprojects.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.facilityprojects.R
import com.example.facilityprojects.model.CallProgressDialog
import com.example.facilityprojects.model.Server
import com.example.sendmail.GMailSender
import io.paperdb.Paper
import kotlinx.android.synthetic.main.activity_email_payment.*
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class EmailPaymentActivity : AppCompatActivity() {

    lateinit var progress: CallProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email_payment)

        progress = CallProgressDialog()
        Paper.init(this@EmailPaymentActivity)
        var idUser = Paper.book().read("idUser", "")
        var total = Paper.book().read("total", "") // Get from CartActivity

        button_agree.setOnClickListener {
            progress.showProgress(this@EmailPaymentActivity,
                R.style.AppCompatAlertDialogStyle, "Sendding email",
                "Please wait...")
            val handler = Handler()
            handler.postDelayed(Runnable {
                progress.dismissProgress()
                saveData(idUser, total)
                Toast.makeText(this@EmailPaymentActivity, "Mail has been sended.", Toast.LENGTH_SHORT).show()
            }, 2000)

        }
    }

    private fun saveData(idUser: String, total: String) {
        // Save data in orderform table
        var currentDate: String
        var currentTime: String

        var calForDate: Calendar = Calendar.getInstance()

        var date = SimpleDateFormat("dd/MM/yyyy")
        currentDate = date.format(calForDate.time)

        var time = SimpleDateFormat("HH:mm:ss")
        currentTime = time.format(calForDate.time)

        var requestQueue = Volley.newRequestQueue(this@EmailPaymentActivity)
        var stringRequest = object : StringRequest(Method.POST, Server.saveOrder, object : Response.Listener<String>{
            override fun onResponse(response: String?) {
                if (response.equals("Success")) {
                    // If success then save them in detailorder table
                    var requestQueue = Volley.newRequestQueue(this@EmailPaymentActivity)
                    var stringRequest = object : StringRequest(Method.POST, Server.saveDetailOrder, object : Response.Listener<String>{

                        override fun onResponse(response: String?) {
                            if (response.equals("Success")) {
                                var intent = Intent(this@EmailPaymentActivity, CompletePaymentActivity::class.java)
                                intent.putExtra("type", "email")
                                intent.putExtra("amount", total)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                startActivity(intent)
                            } else {
                                Toast.makeText(this@EmailPaymentActivity, "Order not success !", Toast.LENGTH_SHORT).show()
                            }
                        }

                    }, object: Response.ErrorListener{
                        override fun onErrorResponse(error: VolleyError?) {
                        }

                    }){
                        override fun getParams(): MutableMap<String, String> {
                            var hashMap: HashMap<String, String> = HashMap()
                            hashMap.put("idUser", idUser)
                            return hashMap
                        }
                    }

                    requestQueue.add(stringRequest)

                    // Send mail confirm to user
                    sendMailToUser(idUser)
                }
            }

        }, object: Response.ErrorListener{
            override fun onErrorResponse(error: VolleyError?) {

            }

        }){
            override fun getParams(): MutableMap<String, String> {
                var hashMap: HashMap<String, String> = HashMap()
                hashMap.put("idUser", idUser)
                hashMap.put("dateOrder", currentDate)
                hashMap.put("timeOrder", currentTime)
                hashMap.put("totalAmount", total)
                return hashMap
            }
        }

        requestQueue.add(stringRequest)
    }

    private fun sendMailToUser(idUser: String) {
        // Send email confirm to user
        var requestQueue = Volley.newRequestQueue(this@EmailPaymentActivity)
        var stringRequest = object : StringRequest(Method.POST, Server.getIdOrder, object : Response.Listener<String>{

            override fun onResponse(response: String?) {
                if (response != null) {
                    var jsonArray = JSONArray(response)

                    var idOrder: Int

                    for (i in 0 until jsonArray.length()) {

                        var jsonObject = jsonArray.getJSONObject(i)
                        idOrder = jsonObject.getInt("id")

                        // Get email user for sender email
                        var requestQueue2 = Volley.newRequestQueue(this@EmailPaymentActivity)
                        var stringRequest2 = object : StringRequest(Method.POST, Server.getEmailAddress, object : Response.Listener<String>{
                            override fun onResponse(response: String?) {
                                if (response != null) {
                                    var jsonArray = JSONArray(response)

                                    var email: String

                                    for (i in 0 until jsonArray.length()) {
                                        var jsonObject = jsonArray.getJSONObject(i)
                                        email = jsonObject.getString("email")

                                        var textBody = "<!DOCTYPE html>\n" +
                                                "<html>\n" +
                                                "<head>\n" +
                                                "\t<title>Index</title>\n" +
                                                "</head>\n" +
                                                "<body>\n" +
                                                "\t<h2>Hi, User</h2>\n" +
                                                "\t<p>Your order has been reviewed by us, click on the link below to confirm your order</p>\n" +
                                                "\t<a href=\"http://facilityshop1236.000webhostapp.com/facilityshop/user/confirm.php?idOrder=" +
                                                ""+idOrder+"&idUser="+idUser+"\">Confirm</a>\n" +
                                                "</body>\n" +
                                                "</html>"

                                        var sender = GMailSender("khanhpy8251@gmail.com", "facility12344")
                                        sender.sendMail("[Facility Shopping] Mail confirm order", textBody,"khanhpy8251@gmail.com",
                                            email)
                                        if (sender != null) {
                                            Toast.makeText(this@EmailPaymentActivity, "Mail has been sended.", Toast.LENGTH_SHORT).show()
                                        }
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
                        requestQueue2.add(stringRequest2)

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
