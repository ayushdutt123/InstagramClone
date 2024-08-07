package com.example.instagramclone

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.instagramclone.Models.User
import com.example.instagramclone.databinding.ActivitySignUpBinding
import com.example.instagramclone.utils.USER_NODE
import com.example.instagramclone.utils.USER_PROFILE_FOLDER
import com.example.instagramclone.utils.uploadImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class SignUpActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivitySignUpBinding.inflate(layoutInflater)
    }
    private lateinit var user: User
    private val launcher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            uploadImage(uri, USER_PROFILE_FOLDER) { url ->
                if (url != null) {
                    user.image = url
                    binding.profileImage.setImageURI(uri)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val text = "<font color=#FF000000>Already have an Account</font> <font color=#1E88E5>Login ?</font>"
        binding.login.setText(Html.fromHtml(text))
        user = User()

        if (intent.hasExtra("MODE")) {
            if (intent.getIntExtra("MODE", -1) == 1) {
                binding.signUpBtn.text = "Update Profile"
                Firebase.firestore.collection(USER_NODE).document(Firebase.auth.currentUser?.uid ?: return).get()
                    .addOnSuccessListener { documentSnapshot ->
                        documentSnapshot.toObject<User>()?.let {
                            user = it
                            if (!user.image.isNullOrEmpty()) {
                                Picasso.get().load(user.image).into(binding.profileImage)
                            }
                            binding.name.setText(user.name)
                            binding.email.setText(user.email)
                            binding.password.setText(user.password)
                        }
                    }
            }
        }

        binding.signUpBtn.setOnClickListener {
            if (intent.hasExtra("MODE") && intent.getIntExtra("MODE", -1) == 1) {
                Firebase.firestore.collection(USER_NODE)
                    .document(Firebase.auth.currentUser?.uid ?: return@setOnClickListener).set(user)
                    .addOnSuccessListener {
                        startActivity(Intent(this@SignUpActivity, HomeActivity::class.java))
                        finish()
                    }
            } else {
                if (binding.name.text.isNullOrEmpty() || binding.email.text.isNullOrEmpty() || binding.password.text.isNullOrEmpty()) {
                    Toast.makeText(this@SignUpActivity, "Please fill all the Information", Toast.LENGTH_SHORT).show()
                } else {
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(binding.email.text.toString(), binding.password.text.toString())
                        .addOnCompleteListener { result ->
                            if (result.isSuccessful) {
                                user.name = binding.name.text.toString()
                                user.password = binding.password.text.toString()
                                user.email = binding.email.text.toString()
                                Firebase.firestore.collection(USER_NODE)
                                    .document(Firebase.auth.currentUser?.uid ?: return@addOnCompleteListener).set(user)
                                    .addOnSuccessListener {
                                        startActivity(Intent(this@SignUpActivity, HomeActivity::class.java))
                                        finish()
                                    }
                            } else {
                                Toast.makeText(this@SignUpActivity, result.exception?.localizedMessage, Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }
        }

        binding.addImage.setOnClickListener {
            launcher.launch("image/*")
        }

        binding.login.setOnClickListener {
            startActivity(Intent(this@SignUpActivity, LoginActivity::class.java))
            finish()
        }
    }
}
