package com.example.facilityprojects.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.widget.Toast
import com.example.facilityprojects.R
import com.example.facilityprojects.model.CallProgressDialog
import com.example.sendmail.GMailSender
import kotlinx.android.synthetic.main.activity_email_forgot.*

class EmailForgotActivity : AppCompatActivity() {

    lateinit var progress: CallProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email_forgot)

        progress = CallProgressDialog()

        button_agree2.setOnClickListener {
            progress.showProgress(this@EmailForgotActivity,
                R.style.AppCompatAlertDialogStyle, "Sendding email",
                "Please wait...")
            var email = editText_email2.text.toString()
            if (TextUtils.isEmpty(email)) {
                progress.dismissProgress()
                editText_email2.setError("Please fill this field !")
            } else {
                sendMailToUser(email)
            }
        }

        setToolbar()
    }

    private fun sendMailToUser(email: String) {
        // Send email setup new password to user
        var textBody = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "\t<title>Index 2</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "\t<h2>Hi, User</h2>\n" +
                "\t<p>We have received your password change request from you. Click on the link below to set a new password.</p>\n" +
                "\t<a href=\"http://facilityshop1236.000webhostapp.com/facilityshop/user/formChangePassword.php?email="+email+"\">" +
                "Setup a new password</a>\n" +
                "</body>\n" +
                "</html>"

        var sender = GMailSender("khanhpy8251@gmail.com", "facility12344")
        sender.sendMail("[Facility Shopping] Mail setup new password", textBody,"khanhpy8251@gmail.com",
            email)
        if (sender != null) {
            // When success
            val handler = Handler()
            handler.postDelayed(Runnable {
                progress.dismissProgress()
                Toast.makeText(this@EmailForgotActivity, "Mail has been sended.", Toast.LENGTH_SHORT).show()
            }, 3000)
        }
    }

    private fun setToolbar() {
        setSupportActionBar(toolBar_email_forgot)
        toolBar_email_forgot.setNavigationIcon(R.drawable.ic_arrow_back)
        toolBar_email_forgot.setNavigationOnClickListener {
            finish()
        }
    }
}
