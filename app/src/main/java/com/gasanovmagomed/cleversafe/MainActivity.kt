package com.gasanovmagomed.cleversafe


import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import net.objecthunter.exp4j.ExpressionBuilder


class MainActivity : AppCompatActivity() {

    private lateinit var countLabel: TextView
    private lateinit var resultLabel: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var authError: String
    private lateinit var userPin: String
    private val savedUserPin = "usersPin"
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        auth = FirebaseAuth.getInstance()
        countLabel = findViewById(R.id.CountLable)
        resultLabel = findViewById(R.id.ResultLable)
        userPin = ""
        prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if(currentUser == null) showWelcomeDialog()
    }

    @SuppressLint("CommitPrefEdits")
    override fun onPause() {
        super.onPause()
        //auth.signOut()
        val editor = prefs.edit()
        editor.putString(savedUserPin, userPin).apply()
    }

    override fun onResume() {
        super.onResume()
        if(prefs.contains(savedUserPin)) userPin = prefs.getString(savedUserPin, null).toString()
    }

    private fun showWelcomeDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Welcome to Clever Storage")
        builder.setMessage("To use secret storage please enter your 6 symbols pin code, then click to '+/-' button")
        builder.setPositiveButton("OK"){ dialog, which ->
            Log.d("GOOD", "Accepted")
        }
        builder.show()
    }


    // Отрисовка кнопок в поля
    fun showButtons(view: View) {
        when(view.id){
            //numbers
            R.id.one -> appendOnExpression("1", true)
            R.id.two -> appendOnExpression("2", true)
            R.id.three -> appendOnExpression("3", true)
            R.id.four -> appendOnExpression("4", true)
            R.id.five -> appendOnExpression("5", true)
            R.id.six -> appendOnExpression("6", true)
            R.id.seven -> appendOnExpression("7", true)
            R.id.eight -> appendOnExpression("8", true)
            R.id.nine -> appendOnExpression("9", true)
            R.id.zero -> appendOnExpression("0", true)
            R.id.dot -> appendOnExpression(".", true)

            // operators
            R.id.plus -> appendOnExpression("+", false)
            R.id.minus -> appendOnExpression("-", false)
            R.id.multiple -> appendOnExpression("*", false)
            R.id.divide -> appendOnExpression("/", false)
            R.id.percent -> appendOnExpression("%", false)
        }
    }


    // функция отрисовки текста кнопок в поля
    private fun appendOnExpression(string: String, canClear: Boolean){

        if(resultLabel.text.isNotEmpty()) countLabel.text= ""
        if(canClear){
            resultLabel.text = ""
            countLabel.append(string)
        } else {
            countLabel.append(resultLabel.text)
            countLabel.append(string)
            resultLabel.text = ""
        }
    }

    fun onChangerClicked(view: View) {
        if(countLabel.text.length >= 6 && userPin == countLabel.text.toString()){
            showLoginDialog()
            countLabel.text = ""
        } else if (countLabel.text.length >= 6 && userPin == ""){
            userPin = countLabel.text.toString()
            countLabel.text = ""
            showRegisterDialog()
        }else changeSign()
    }

    // calculate result
    fun onEquals(view: View) {
        try {
            val expression = ExpressionBuilder(countLabel.text.toString()).build()
            val result = expression.evaluate()
            val longResult = result.toLong()
            if(result == longResult.toDouble())
                resultLabel.text = longResult.toString()
            else
                resultLabel.text = result.toString()


        } catch (e: Exception){
            Log.d("Exception", " message: " + e.message)
        }
    }

    // clear one symbol
    fun onBack(view: View) {
        val string = countLabel.text.toString()
        if(string.isNotEmpty()){
            countLabel.text = string.substring(0, string.length - 1)
        }
        resultLabel.text = ""
    }

    // clear all in label
    fun onClean(view: View) {
        countLabel.text = ""
        resultLabel.text = ""
    }

    private fun changeSign(){
        if(countLabel.text != null && countLabel.text.toString() != ""){
            val expression = ExpressionBuilder(countLabel.text.toString()).build()
            val  changed = expression.evaluate()
            val longChanged = changed.toLong();
            if(changed == longChanged.toDouble()){
                val result = longChanged * - 1;
                resultLabel.text = result.toString()
            }else{
                val result = changed * -1;
                resultLabel.text = result.toString()
            }
        } else countLabel.text = ""
    }

    private fun showRegisterDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater: LayoutInflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.register_window, null)
        val haveAccount = dialogLayout.findViewById<TextView>(R.id.haveAccount)
        val userEmail: EditText = dialogLayout.findViewById(R.id.email)
        val userPassword: EditText = dialogLayout.findViewById((R.id.second_pass))

        haveAccount.setOnClickListener {
            showLoginDialog()
            closeContextMenu()
        }

        with(builder){
            setTitle("Register in system")
            setMessage(
                "If you want, you may register in our system to save your data in storage." +
                        "But you of course can skip this step" +
                        "Just Click 'skip' button"
            )
            setPositiveButton("Next"){ dialog, which ->
                val userEmailText = userEmail.text.toString()
                val userPasswordText = userPassword.text.toString()

                if(userEmailText.isEmpty()){
                    userEmail.error = "Поле 'username' обязательно для заполнения"
                    return@setPositiveButton
                }
                if(userPasswordText.isEmpty()){
                    userPassword.error = "Поле 'password' обязательно для заполнения"
                    return@setPositiveButton
                }

                auth.createUserWithEmailAndPassword(userEmailText, userPasswordText)
                    .addOnCompleteListener(OnCompleteListener<AuthResult>{ task ->
                        if(task.isSuccessful){
                            firebaseUser = task.result!!.user!!
                            successfulRegMsg()

                            val intent = Intent(this@MainActivity, StorageActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            intent.putExtra("user_id", firebaseUser.uid)
                            intent.putExtra("email_id", userEmailText)
                            startActivity(intent)
                            finish()
                        } else{
                            authError = task.exception!!.message.toString()
                            failedRegisterMsg()
                        }
                    })
            }
            setNegativeButton("Cancel"){ dialog, which ->
                showCancelMsg()
            }
            setNeutralButton("Skip"){ dialog, which ->
                showSkipAction()
                val intent = Intent(this@MainActivity, StorageActivity::class.java)
                startActivity(intent)
                finish()
            }
            setView(dialogLayout)
            show()
        }
    }

    private fun showLoginDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater: LayoutInflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.login_window, null)
        val forgotPass = dialogLayout.findViewById<TextView>(R.id.forgotPass)
        val userEmail: EditText = dialogLayout.findViewById(R.id.email)
        val userPassword: EditText = dialogLayout.findViewById(R.id.second_pass)

        forgotPass.setOnClickListener {
            val intent = Intent(this, ForgotActivity::class.java)
            startActivity(intent)
            finish()
        }

        with(builder){
            setTitle("Login in system")
            setMessage("To continue please login in system with your password and email address")
            setPositiveButton("Next"){ dialog, which ->
                val userEmailText = userEmail.text.toString()
                val userPasswordText = userPassword.text.toString()

                if(userEmailText.isEmpty()){
                    userEmail.error = "Поле 'username' обязательно для заполнения"
                    return@setPositiveButton
                }
                if(userPasswordText.isEmpty()){
                    userPassword.error = "Поле 'password' обязательно для заполнения"
                    return@setPositiveButton
                }

                auth.signInWithEmailAndPassword(userEmailText, userPasswordText)
                    .addOnCompleteListener { task->
                        if (task.isSuccessful){
                            val intent = Intent(this@MainActivity, StorageActivity::class.java)
                            intent.putExtra("user_id", auth.currentUser!!.uid)
                            intent.putExtra("email_id", userEmailText)
                            startActivity(intent)
                            finish()
                        } else{
                            authError = task.exception!!.message.toString()
                            failedLoginMsg()
                        }
                    }
            }
            setNegativeButton("Cancel"){ dialog, which ->
                showCancelMsg()
            }
            setView(dialogLayout)
            show()
        }
    }

    private fun showCancelMsg() {
        clearPin()
        Toast.makeText(this, "Action is canceled", Toast.LENGTH_SHORT).show()
    }

    private fun successfulRegMsg() { Toast.makeText(this, "You are registered successfully", Toast.LENGTH_SHORT).show() }
    private fun failedRegisterMsg() { Toast.makeText(this, authError, Toast.LENGTH_SHORT).show() }
    private fun failedLoginMsg() { Toast.makeText(this, authError, Toast.LENGTH_SHORT).show() }

    private fun showSkipAction() {
        clearPin()
        Toast.makeText(this, "You are skipped this step.", Toast.LENGTH_SHORT).show()
    }

    private fun clearPin(){
        userPin = ""
        countLabel.text = null
    }

    private fun reload(){

    }
}
