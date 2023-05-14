package si.uni_lj.fri.pbd.dragonhack
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Switch
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar

class SettingsActivity : AppCompatActivity() {

    public var notification: Boolean = false
    private lateinit var switchButton: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        switchButton = findViewById<Switch>(R.id.notificationsSwitch)

        switchButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                showEnablePushNotificationDialog()
            } else {
                switchButton.isChecked = false
            }
        }


    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun showEnablePushNotificationDialog() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Enable Push Notifications")
        alertDialogBuilder.setMessage("Do you agree to receive push notifications?")
        alertDialogBuilder.setPositiveButton("Agree") { _: DialogInterface, _: Int ->
            switchButton.isChecked = true
            // User agreed to receive push notifications
            // Enable push notifications or perform necessary actions
        }
        alertDialogBuilder.setNegativeButton("Disagree") { _: DialogInterface, _: Int ->
            // User disagreed to receive push notifications
            // Disable push notifications or perform necessary actions
            switchButton.isChecked = false // Uncheck the Switch button
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

}
