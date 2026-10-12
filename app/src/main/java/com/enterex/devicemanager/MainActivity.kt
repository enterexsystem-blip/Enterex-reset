
package com.enterex.devicemanager

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.BitmapFactory
import android.graphics.drawable.GradientDrawable
import android.content.Intent
import android.net.Uri
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import android.util.Base64
import org.json.JSONObject
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class MainActivity : Activity() {

    private val black = Color.rgb(5, 6, 8)
    private val panel = Color.rgb(15, 17, 21)
    private val silver = Color.rgb(225, 229, 235)
    private val muted = Color.rgb(135, 143, 153)
    private val line = Color.rgb(58, 64, 73)

    private val accounts by lazy {
        getSharedPreferences("enterex_accounts", MODE_PRIVATE)
    }

    private var profileImage: ImageView? = null
    private var selectedPhoto: Uri? = null

    private fun dp(value: Int) =
        (value * resources.displayMetrics.density).toInt()

    private fun shape(
        color: Int,
        strokeColor: Int = Color.TRANSPARENT,
        radius: Int = 12
    ) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radius).toFloat()
        if (strokeColor != Color.TRANSPARENT) {
            setStroke(dp(1), strokeColor)
        }
    }

    private fun label(
        text: String,
        size: Float,
        color: Int = silver,
        bold: Boolean = false
    ) = TextView(this).apply {
        this.text = text
        textSize = size
        setTextColor(color)
        gravity = Gravity.CENTER
        typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
    }

    private fun add(
        parent: LinearLayout,
        view: View,
        top: Int = 0,
        bottom: Int = 0
    ) {
        parent.addView(view, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = dp(top)
            bottomMargin = dp(bottom)
        })
    }

    private fun createRoot(): Pair<ScrollView, LinearLayout> {
        val scroll = ScrollView(this).apply {
            setBackgroundColor(black)
            isFillViewport = true
            clipToPadding = false
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(24), dp(22), dp(24), dp(26))
            setBackgroundColor(black)
        }

        scroll.addView(root)
        return Pair(scroll, root)
    }

    private fun addHeader(root: LinearLayout) {
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val brand = label("ENTEREX", 15f, silver, true).apply {
            letterSpacing = 0.18f
            gravity = Gravity.CENTER_VERTICAL
        }

        header.addView(brand, LinearLayout.LayoutParams(0, dp(34), 1f))
        header.addView(label("DEVICE MANAGER • V1.0", 9f, muted))
        add(root, header, bottom = 24)
    }

    private fun addBrand(root: LinearLayout) {
        add(root, label("◈", 66f, silver), bottom = 2)
        add(root, label("ENTEREX", 31f, silver, true), bottom = 3)
        add(root, label("D E V I C E   M A N A G E R", 10f, muted), bottom = 12)

        root.addView(View(this).apply {
            setBackgroundColor(line)
        }, LinearLayout.LayoutParams(dp(76), dp(1)).apply {
            gravity = Gravity.CENTER
            bottomMargin = dp(20)
        })
    }

    private fun inputField(
        hintText: String,
        type: Int
    ) = EditText(this).apply {
        hint = hintText
        textSize = 14f
        setTextColor(silver)
        setHintTextColor(muted)
        inputType = type
        isSingleLine = true
        gravity = Gravity.CENTER_VERTICAL
        setPadding(dp(14), dp(4), dp(14), dp(4))
        background = shape(black, line, 10)
        minHeight = dp(52)
    }

    private fun makeButton(textValue: String, filled: Boolean) =
        Button(this).apply {
            text = textValue
            textSize = 13f
            isAllCaps = false
            letterSpacing = 0.04f
            setTextColor(if (filled) black else silver)
            typeface = Typeface.DEFAULT_BOLD
            background = if (filled) shape(silver, radius = 10)
                else shape(panel, line, 10)
            minHeight = dp(50)
            setPadding(dp(12), 0, dp(12), 0)
        }

    private fun normalizedEmail(email: String) =
        email.trim().lowercase(java.util.Locale.ROOT)

    private fun hashPassword(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, 120000, 256)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    private fun createAccount(
        nickname: String,
        age: Int,
        bio: String,
        email: String,
        password: String
    ): Boolean {
        val key = normalizedEmail(email)

        if (accounts.contains(key)) return false

        return try {
            val salt = ByteArray(16)
            SecureRandom().nextBytes(salt)
            val hash = hashPassword(password, salt)

            val account = JSONObject().apply {
                put("nickname", nickname)
                put("age", age)
                put("bio", bio)
                put("salt", Base64.encodeToString(salt, Base64.NO_WRAP))
                put("hash", Base64.encodeToString(hash, Base64.NO_WRAP))
            }

            accounts.edit().putString(key, account.toString()).commit()
        } catch (e: Exception) {
            false
        }
    }

    private fun checkLogin(email: String, password: String): JSONObject? {
        return try {
            val saved = accounts.getString(normalizedEmail(email), null)
                ?: return null
            val account = JSONObject(saved)
            val salt = Base64.decode(account.getString("salt"), Base64.NO_WRAP)
            val expected = Base64.decode(account.getString("hash"), Base64.NO_WRAP)
            val actual = hashPassword(password, salt)

            if (MessageDigest.isEqual(expected, actual)) account else null
        } catch (e: Exception) {
            null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = black
        window.navigationBarColor = black
        showLogin()
    }

    private fun showLogin() {
        val (scroll, root) = createRoot()
        addHeader(root)
        addBrand(root)

        add(root, label("CONTROLE. SEGURANÇA.\nSEMPRE COM VOCÊ.", 11f), bottom = 24)

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(20), dp(18), dp(20))
            background = shape(panel, line, 14)
        }
        add(root, card, bottom = 20)

        add(card, label("ACESSO AO SISTEMA", 13f, silver, true), bottom = 6)
        add(card, label("Entre com sua conta para continuar.", 11f, muted), bottom = 20)

        val email = inputField(
            "E-mail",
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        )
        add(card, email, bottom = 12)

        val password = inputField(
            "Senha",
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        )
        add(card, password, bottom = 8)

        val forgot = label("Esqueceu sua senha?", 11f, silver).apply {
            gravity = Gravity.END
            setPadding(0, dp(8), 0, dp(16))
        }
        add(card, forgot)

        val enter = makeButton("ENTRAR", true)
        add(card, enter, bottom = 10)

        val create = makeButton("CRIAR CONTA", false)
        add(card, create)

        add(root, label("TECNOLOGIA AO SEU ALCANCE", 9f, muted, true),
            top = 10, bottom = 16)

        val footer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        listOf("⌘" to "Dispositivos", "◇" to "Restauração", "◎" to "Controle")
            .forEach { item ->
                val column = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER
                }
                add(column, label(item.first, 23f, silver))
                add(column, label(item.second, 9f, muted))
                footer.addView(column, LinearLayout.LayoutParams(0, dp(58), 1f))
            }

        add(root, footer, bottom = 18)
        add(root, label("ENTEREX SYSTEMS", 8f, muted))

        create.setOnClickListener { showRegistration() }

        enter.setOnClickListener {
            val emailValue = email.text.toString().trim()
            val passwordValue = password.text.toString()

            when {
                emailValue.isEmpty() -> {
                    email.error = "Digite seu e-mail"
                    email.requestFocus()
                }
                passwordValue.isEmpty() -> {
                    password.error = "Digite sua senha"
                    password.requestFocus()
                }
                else -> {
                    val account = checkLogin(emailValue, passwordValue)
                    if (account == null) {
                        Toast.makeText(
                            this,
                            "E-mail ou senha incorretos.",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            this,
                            "Bem-vindo, ${account.optString("nickname")}!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }

        forgot.setOnClickListener {
            Toast.makeText(
                this,
                "A recuperação de senha online será implementada depois.",
                Toast.LENGTH_LONG
            ).show()
        }

        setContentView(scroll)
    }

    private fun showRegistration() {
        val (scroll, root) = createRoot()
        profileImage = null
        selectedPhoto = null

        addHeader(root)
        addBrand(root)
        add(root, label("CRIE SUA CONTA", 20f, silver, true), bottom = 6)
        add(root, label("Faça parte do sistema ENTEREX.", 12f, muted), bottom = 22)

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(20), dp(18), dp(20))
            background = shape(panel, line, 14)
        }
        add(root, card, bottom = 18)
        add(card, label("DADOS DO USUÁRIO", 12f, silver, true), bottom = 16)
        add(card, label("FOTO DE PERFIL", 10f, muted, true), bottom = 10)

        val avatar = ImageView(this).apply {
            setImageDrawable(shape(black, line, 60))
            scaleType = ImageView.ScaleType.CENTER_CROP
            background = shape(black, line, 60)
            clipToOutline = true
        }
        profileImage = avatar

        card.addView(avatar, LinearLayout.LayoutParams(dp(92), dp(92)).apply {
            gravity = Gravity.CENTER
            bottomMargin = dp(12)
        })

        val photoButton = makeButton("ADICIONAR FOTO (OPCIONAL)", false)
        add(card, photoButton, bottom = 20)

        photoButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
            }
            @Suppress("DEPRECATION")
            startActivityForResult(
                Intent.createChooser(intent, "Escolha sua foto de perfil"),
                1001
            )
        }

        add(card, label("APELIDO", 10f, muted, true), bottom = 7)
        val nickname = inputField(
            "Como quer ser chamado?",
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS
        )
        add(card, nickname, bottom = 14)

        add(card, label("IDADE", 10f, muted, true), bottom = 7)
        val age = inputField("Sua idade", InputType.TYPE_CLASS_NUMBER)
        add(card, age, bottom = 14)

        add(card, label("BIOGRAFIA (OPCIONAL)", 10f, muted, true), bottom = 7)
        val bio = EditText(this).apply {
            hint = "Conte um pouco sobre você..."
            textSize = 14f
            setTextColor(silver)
            setHintTextColor(muted)
            inputType = InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or
                InputType.TYPE_TEXT_FLAG_MULTI_LINE
            gravity = Gravity.TOP or Gravity.START
            minHeight = dp(90)
            setPadding(dp(14), dp(12), dp(14), dp(12))
            background = shape(black, line, 10)
        }
        add(card, bio, bottom = 14)

        add(card, label("E-MAIL", 10f, muted, true), bottom = 7)
        val email = inputField(
            "Digite seu e-mail",
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        )
        add(card, email, bottom = 14)

        add(card, label("SENHA", 10f, muted, true), bottom = 7)
        val password = inputField(
            "Crie uma senha",
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        )
        add(card, password, bottom = 14)

        add(card, label("CONFIRMAR SENHA", 10f, muted, true), bottom = 7)
        val confirmPassword = inputField(
            "Digite a senha novamente",
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        )
        add(card, confirmPassword, bottom = 20)

        val registerButton = makeButton("CADASTRAR", true)
        add(card, registerButton, bottom = 10)

        val backButton = makeButton("JÁ TENHO UMA CONTA — FAZER LOGIN", false)
        add(card, backButton)

        registerButton.setOnClickListener {
            val nameValue = nickname.text.toString().trim()
            val ageValue = age.text.toString().trim()
            val bioValue = bio.text.toString().trim()
            val emailValue = email.text.toString().trim()
            val passwordValue = password.text.toString()
            val confirmValue = confirmPassword.text.toString()

            when {
                nameValue.isEmpty() -> {
                    nickname.error = "Digite seu apelido"
                    nickname.requestFocus()
                }
                ageValue.isEmpty() -> {
                    age.error = "Digite sua idade"
                    age.requestFocus()
                }
                ageValue.toIntOrNull() == null || ageValue.toInt() !in 1..120 -> {
                    age.error = "Digite uma idade válida"
                    age.requestFocus()
                }
                !android.util.Patterns.EMAIL_ADDRESS.matcher(emailValue).matches() -> {
                    email.error = "Digite um e-mail válido"
                    email.requestFocus()
                }
                passwordValue.length < 8 -> {
                    password.error = "Use pelo menos 8 caracteres"
                    password.requestFocus()
                }
                passwordValue != confirmValue -> {
                    confirmPassword.error = "As senhas não são iguais"
                    confirmPassword.requestFocus()
                }
                else -> {
                    try {
                        val saved = createAccount(
                            nameValue,
                            ageValue.toInt(),
                            bioValue,
                            emailValue,
                            passwordValue
                        )

                        if (saved) {
                            Toast.makeText(
                                this,
                                "Conta criada! Faça login para continuar.",
                                Toast.LENGTH_LONG
                            ).show()
                            showLogin()
                        } else {
                            email.error = "Este e-mail já está cadastrado"
                            email.requestFocus()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(
                            this,
                            "Não foi possível criar a conta.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }

        backButton.setOnClickListener { showLogin() }

        add(root, label("ENTEREX SYSTEMS", 8f, muted), bottom = 8)
        setContentView(scroll)
    }

    @Deprecated("Usado para selecionar a foto de perfil")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1001 && resultCode == RESULT_OK) {
            val uri = data?.data ?: return

            try {
                contentResolver.openInputStream(uri)?.use { stream ->
                    val bitmap = BitmapFactory.decodeStream(stream)
                    if (bitmap != null) {
                        profileImage?.setImageBitmap(bitmap)
                        selectedPhoto = uri
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this,
                    "Não foi possível abrir essa imagem.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
