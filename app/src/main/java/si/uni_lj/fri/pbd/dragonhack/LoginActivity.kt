package si.uni_lj.fri.pbd.dragonhack

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import android.content.DialogInterface


class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val loginButton = findViewById<Button>(R.id.loginButton)
        val registerButton = findViewById<Button>(R.id.registerButton)
        val username = findViewById<EditText>(R.id.username)
        val password = findViewById<EditText>(R.id.password)

        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Invalid Login")
        alertDialogBuilder.setMessage("Username or password is incorrect")

        alertDialogBuilder.setPositiveButton("OK") { dialog: DialogInterface, which: Int ->
            // Positive button clicked
            // Perform any necessary actions
        }

        alertDialogBuilder.setNegativeButton("Cancel") { dialog: DialogInterface, which: Int ->
            // Negative button clicked
            // Perform any necessary actions
        }

        val alertDialog = alertDialogBuilder.create()


        loginButton.setOnClickListener {
            //Toast.makeText(this, username.text, Toast.LENGTH_SHORT).show()
            alertDialog.show()
        }

        registerButton.setOnClickListener {
            Toast.makeText(this, password.text, Toast.LENGTH_SHORT).show()
        }
    }


}