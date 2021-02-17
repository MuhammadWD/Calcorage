package com.gasanovmagomed.cleversafe.ui.notifications

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.gasanovmagomed.cleversafe.MainActivity
import com.gasanovmagomed.cleversafe.R
import com.gasanovmagomed.cleversafe.StorageActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class NotificationsFragment : Fragment(), View.OnClickListener {

    private lateinit var auth: FirebaseAuth
    private lateinit var emailEntry: TextView
    private lateinit var email: String
    private lateinit var userPin: String
    private val savedUserPin = "usersPin"
    private lateinit var prefs: SharedPreferences

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_notifications, container, false)
        val changeMailBtn = root.findViewById<Button>(R.id.changeEmail)
        val changePassBtn = root.findViewById<Button>(R.id.changePassword)
        val changePinBtn = root.findViewById<Button>(R.id.changePin)
        val deleteUserBtn = root.findViewById<Button>(R.id.deleteAccount)
        emailEntry = root.findViewById(R.id.emailEntry)
        userPin = ""
        prefs = requireActivity().getPreferences(Context.MODE_PRIVATE)
        changeMailBtn.setOnClickListener(this)
        changePassBtn.setOnClickListener(this)
        changePinBtn.setOnClickListener(this)
        deleteUserBtn.setOnClickListener(this)
        auth = FirebaseAuth.getInstance()
        displayUsersInformation()
        return root
    }

    override fun onClick(v: View?) {
        when(setButtonIndex(v!!.id)){
            1 -> changePin()
            2 -> changePassword()
            3 -> changeEmail()
            4 -> deleteAccount()
        }
    }

    private fun changePin() {
        val builder = activity?.let { AlertDialog.Builder(it) }
        val inflater: LayoutInflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.change_pincode_window, null)
        val newPin: EditText = dialogLayout.findViewById((R.id.newPincode))

        with(builder) {
            this?.setTitle("Change your pin code")
            this?.setMessage("write here your new pin code to change it")
            this?.setPositiveButton("Change") { dialog, which ->
                val user = auth.currentUser
                val newPinText = newPin.text.toString()

                if (newPinText.isEmpty()) {
                    newPin.error = "Поле 'pin' обязательно для заполнения"
                    return@setPositiveButton
                }

                user!!.updatePassword(newPinText)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                userPin = newPinText
                                val editor = prefs.edit()
                                editor.putString(savedUserPin, userPin)
                            }
                        }

            }
            this?.setNegativeButton("Cancel") { dialog, which ->
                showCancelMsg()
            }
            this?.setView(dialogLayout)
            this?.show()
        }
    }

    private fun changePassword() {
        val builder = activity?.let { AlertDialog.Builder(it) }
        val inflater: LayoutInflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.change_password_window, null)
        val newPassword: EditText = dialogLayout.findViewById((R.id.newPassword))

        with(builder){
            this?.setTitle("Change your password")
            this?.setMessage("write here your new password to change it")
            this?.setPositiveButton("Change"){ dialog, which ->
                val user = auth.currentUser
                val newPasswordText = newPassword.text.toString()

                if(newPasswordText.isEmpty()){
                    newPassword.error = "Поле 'password' обязательно для заполнения"
                    return@setPositiveButton
                }

                user!!.updatePassword(newPasswordText)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d("Changing password", "User password updated.")
                            }
                        }

            }
            this?.setNegativeButton("Cancel"){ dialog, which ->
                showCancelMsg()
            }
            this?.setView(dialogLayout)
            this?.show()
        }
    }

    private fun changeEmail() {
        val builder = activity?.let { AlertDialog.Builder(it) }
        val inflater: LayoutInflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.change_email_window, null)
        val newEmail: EditText = dialogLayout.findViewById((R.id.newEmail))

        with(builder){
            this?.setTitle("Change your email address")
            this?.setMessage("write here your new email address to change it")
            this?.setPositiveButton("Change"){ dialog, which ->
                val user = auth.currentUser
                val newEmailText = newEmail.text.toString()

                if(newEmailText.isEmpty()){
                    newEmail.error = "Поле 'email' обязательно для заполнения"
                    return@setPositiveButton
                }

                user!!.updateEmail(newEmailText)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d("Update", "User email address updated.")
                            }
                        }

            }
            this?.setNegativeButton("Cancel"){ dialog, which ->
                showCancelMsg()
            }
            this?.setView(dialogLayout)
            this?.show()
        }
    }

    private fun deleteAccount() {
        val user = auth.currentUser!!

        user.delete()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val editor = prefs.edit()
                        editor.remove(savedUserPin);
                        editor.apply()
                        val intent = Intent(activity, MainActivity::class.java)
                        startActivity(intent)
                    } else{
                        Toast.makeText(activity, "Somthing was wrong", Toast.LENGTH_LONG).show()
                    }
                }
    }

    private fun displayUsersInformation() {
        val user = auth.currentUser

        user?.let {
            email = user.email.toString()
        }
        emailEntry.text = email
    }


    private fun showCancelMsg() { Toast.makeText(activity, "Action is canceled", Toast.LENGTH_SHORT).show() }
    private fun setButtonIndex(id: Int): Int {
        var index = -1
        when(id){
            R.id.changePin -> index = 1
            R.id.changePassword -> index = 2
            R.id.changeEmail -> index = 3
            R.id.deleteAccount -> index = 4
        }
        return index
    }

    private fun confirmAccount() {
        val user = auth.currentUser!!

        val builder = activity?.let { AlertDialog.Builder(it) }
        val inflater: LayoutInflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.confirm_account_window, null)
        val confirmedEmail: EditText = dialogLayout.findViewById((R.id.confirmedEmail))
        val confirmedPassword: EditText = dialogLayout.findViewById((R.id.confirmedPassword))

        with(builder){
            this?.setTitle("Confirm your account")
            this?.setMessage("write here your password and email to confirm your account")
            this?.setPositiveButton("Change"){ dialog, which ->
                val confirmedEmailText = confirmedEmail.text.toString()
                val confirmedPasswordText = confirmedPassword.text.toString()

                if(confirmedEmailText.isEmpty()){
                    confirmedEmail.error = "Поле 'email' обязательно для заполнения"
                    return@setPositiveButton
                }

                if(confirmedPasswordText.isEmpty()){
                    confirmedPassword.error = "Поле 'password' обязательно для заполнения"
                    return@setPositiveButton
                }

                val credential = EmailAuthProvider
                        .getCredential(confirmedEmailText, confirmedPasswordText)
                user.reauthenticate(credential)
                        .addOnCompleteListener { Toast.makeText(activity, "Account has been confirmed", Toast.LENGTH_SHORT).show() }

            }
            this?.setNegativeButton("Cancel"){ dialog, which ->
                showCancelMsg()
            }
            this?.setView(dialogLayout)
            this?.show()
        }

    }

}