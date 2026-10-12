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

class MainActivity : Activity() {

    private val black = Color.rgb(5, 6, 8)
    private val panel = Color.rgb(15, 17, 21)
    private val silver = Color.rgb(225, 229, 235)
    private val muted = Color.rgb(135, 143, 153)
    private val line = Color.rgb(58, 64, 73)

    private var profileImage: ImageView? = null
    private var selectedPhoto: Uri? = null

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private fun shape(
        color: Int,
        strokeColor: Int = Color.TRANSPARENT,
        radius: Int = 12
    ): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()

            if (strokeColor != Color.TRANSPARENT) {
                setStroke(dp(1), strokeColor)
            }
        }
    }

    private fun label(
        text: String,
        size: Float,
        color: Int = silver,
        bold: Boolean = false
    ): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = size
            setTextColor(color)
            gravity = Gravity.CENTER

            typeface = if (bold) {
                Typeface.create(
                    "sans-serif",
                    Typeface.BOLD
                )
            } else {
                Typeface.create(
                    "sans-serif",
                    Typeface.NORMAL
                )
            }
        }
    }

    private fun add(
        parent: LinearLayout,
        view: View,
        top: Int = 0,
        bottom: Int = 0
    ) {
        parent.addView(
            view,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(top)
                bottomMargin = dp(bottom)
            }
        )
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
            setPadding(
                dp(24),
                dp(22),
                dp(24),
                dp(26)
            )
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

        val brand = label(
            "ENTEREX",
            15f,
            silver,
            true
        ).apply {
            letterSpacing = 0.18f
            gravity = Gravity.CENTER_VERTICAL
        }

        header.addView(
            brand,
            LinearLayout.LayoutParams(0, dp(34), 1f)
        )

        header.addView(
            label("DEVICE MANAGER  •  V1.0", 9f, muted)
        )

        add(root, header, bottom = 24)
    }

    private fun addBrand(root: LinearLayout) {
        add(
            root,
            label("◈", 66f, silver),
            bottom = 2
        )

        add(
            root,
            label("ENTEREX", 31f, silver, true),
            bottom = 3
        )

        add(
            root,
            label("D E V I C E   M A N A G E R", 10f, muted),
            bottom = 12
        )

        val divider = View(this).apply {
            setBackgroundColor(line)
        }

        root.addView(
            divider,
            LinearLayout.LayoutParams(
                dp(76),
                dp(1)
            ).apply {
                gravity = Gravity.CENTER
                bottomMargin = dp(20)
            }
        )
    }

    private fun inputField(
        hintText: String,
        inputTypeValue: Int
    ): EditText {
        return EditText(this).apply {
            hint = hintText
            textSize = 14f

            setTextColor(silver)
            setHintTextColor(muted)

            inputType = inputTypeValue
            isSingleLine = true

            gravity = Gravity.CENTER_VERTICAL

            setPadding(
                dp(14),
                dp(4),
                dp(14),
                dp(4)
            )

            background = shape(
                black,
                line,
                10
            )

            minHeight = dp(52)
        }
    }

    private fun makeButton(
        textValue: String,
        filled: Boolean
    ): Button {
        return Button(this).apply {
            text = textValue
            textSize = 13f
            isAllCaps = false

            letterSpacing = 0.08f

            setTextColor(
                if (filled) black else silver
            )

            typeface = Typeface.DEFAULT_BOLD

            background = if (filled) {
                shape(silver, radius = 10)
            } else {
                shape(panel, line, 10)
            }

            minHeight = dp(50)

            setPadding(
                dp(12),
                0,
                dp(12),
                0
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = black
        window.navigationBarColor = black

        showLogin()
    }

    // ==================================================
    // TELA 1 - LOGIN
    // ==================================================

    private fun showLogin() {
        val (scroll, root) = createRoot()

        addHeader(root)
        addBrand(root)

        add(
            root,
            label(
                "CONTROLE. SEGURANÇA.\nSEMPRE COM VOCÊ.",
                11f,
                silver
            ),
            bottom = 24
        )

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL

            setPadding(
                dp(18),
                dp(20),
                dp(18),
                dp(20)
            )

            background = shape(
                panel,
                line,
                14
            )
        }

        add(root, card, bottom = 20)

        add(
            card,
            label(
                "ACESSO AO SISTEMA",
                13f,
                silver,
                true
            ),
            bottom = 6
        )

        add(
            card,
            label(
                "Entre com sua conta para continuar.",
                11f,
                muted
            ),
            bottom = 20
        )

        val email = inputField(
            "E-mail",
            InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        )

        add(card, email, bottom = 12)

        val password = inputField(
            "Senha",
            InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_VARIATION_PASSWORD
        )

        add(card, password, bottom = 8)

        val forgot = label(
            "Esqueceu sua senha?",
            11f,
            silver
        ).apply {
            gravity = Gravity.END
            setPadding(0, dp(8), 0, dp(16))
        }

        add(card, forgot)

        val enter = makeButton(
            "ENTRAR",
            true
        )

        add(card, enter, bottom = 10)

        val create = makeButton(
            "CRIAR CONTA",
            false
        )

        add(card, create)

        add(
            root,
            label(
                "TECNOLOGIA AO SEU ALCANCE",
                9f,
                muted,
                true
            ),
            top = 10,
            bottom = 16
        )

        val footer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        val features = listOf(
            "⌘" to "Dispositivos",
            "◇" to "Restauração",
            "◎" to "Controle"
        )

        features.forEach { item ->
            val column = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
            }

            add(
                column,
                label(item.first, 23f, silver)
            )

            add(
                column,
                label(item.second, 9f, muted)
            )

            footer.addView(
                column,
                LinearLayout.LayoutParams(
                    0,
                    dp(58),
                    1f
                )
            )
        }

        add(root, footer, bottom = 18)

        add(
            root,
            label(
                "ENTEREX SYSTEMS",
                8f,
                muted
            )
        )

        create.setOnClickListener {
            showRegistration()
        }

        enter.setOnClickListener {
            when {
                email.text.isNullOrBlank() -> {
                    email.error = "Digite seu e-mail"
                    email.requestFocus()
                }

                password.text.isNullOrBlank() -> {
                    password.error = "Digite sua senha"
                    password.requestFocus()
                }

                else -> {
                    Toast.makeText(
                        this,
                        "O login ainda precisa ser conectado.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        forgot.setOnClickListener {
            Toast.makeText(
                this,
                "A recuperação de senha será implementada.",
                Toast.LENGTH_LONG
            ).show()
        }

        setContentView(scroll)
    }

    // ==================================================
    // TELA 2 - CADASTRO
    // ==================================================

    private fun showRegistration() {
        val (scroll, root) = createRoot()

        profileImage = null

        addHeader(root)
        addBrand(root)

        add(
            root,
            label(
                "CRIE SUA CONTA",
                20f,
                silver,
                true
            ),
            bottom = 6
        )

        add(
            root,
            label(
                "Faça parte do sistema ENTEREX.",
                12f,
                muted
            ),
            bottom = 22
        )

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL

            setPadding(
                dp(18),
                dp(20),
                dp(18),
                dp(20)
            )

            background = shape(
                panel,
                line,
                14
            )
        }

        add(root, card, bottom = 18)

        add(
            card,
            label(
                "DADOS DO USUÁRIO",
                12f,
                silver,
                true
            ),
            bottom = 16
        )

        // FOTO DE PERFIL

        add(
            card,
            label(
                "FOTO DE PERFIL",
                10f,
                muted,
                true
            ),
            bottom = 10
        )

        val avatar = ImageView(this).apply {
            setImageDrawable(
                shape(
                    black,
                    line,
                    60
                )
            )

            scaleType = ImageView.ScaleType.CENTER_CROP
            background = shape(black, line, 60)
            clipToOutline = true
        }

        profileImage = avatar

        card.addView(
            avatar,
            LinearLayout.LayoutParams(
                dp(92),
                dp(92)
            ).apply {
                gravity = Gravity.CENTER
                bottomMargin = dp(12)
            }
        )

        val photoButton = makeButton(
            "ADICIONAR FOTO (OPCIONAL)",
            false
        )

        add(card, photoButton, bottom = 20)

        photoButton.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_GET_CONTENT
            ).apply {
                type = "image/*"
            }

            @Suppress("DEPRECATION")
            startActivityForResult(
                Intent.createChooser(
                    intent,
                    "Escolha sua foto de perfil"
                ),
                1001
            )
        }

        // APELIDO

        add(
            card,
            label("APELIDO", 10f, muted, true),
            bottom = 7
        )

        val nickname = inputField(
            "Como quer ser chamado?",
            InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_FLAG_CAP_WORDS
        )

        add(card, nickname, bottom = 14)

        // IDADE

        add(
            card,
            label("IDADE", 10f, muted, true),
            bottom = 7
        )

        val age = inputField(
            "Sua idade",
            InputType.TYPE_CLASS_NUMBER
        )

        add(card, age, bottom = 14)

        // BIOGRAFIA OPCIONAL

        add(
            card,
            label(
                "BIOGRAFIA (OPCIONAL)",
                10f,
                muted,
                true
            ),
            bottom = 7
        )

        val bio = EditText(this).apply {
            hint = "Conte um pouco sobre você..."
            textSize = 14f

            setTextColor(silver)
            setHintTextColor(muted)

            inputType =
                InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_FLAG_CAP_SENTENCES or
                InputType.TYPE_TEXT_FLAG_MULTI_LINE

            gravity = Gravity.TOP or Gravity.START

            minHeight = dp(90)

            setPadding(
                dp(14),
                dp(12),
                dp(14),
                dp(12)
            )

            background = shape(
                black,
                line,
                10
            )
        }

        add(card, bio, bottom = 14)

        // E-MAIL

        add(
            card,
            label("E-MAIL", 10f, muted, true),
            bottom = 7
        )

        val email = inputField(
            "Digite seu e-mail",
            InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        )

        add(card, email, bottom = 14)

        // SENHA

        add(
            card,
            label("SENHA", 10f, muted, true),
            bottom = 7
        )

        val password = inputField(
            "Crie uma senha",
            InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_VARIATION_PASSWORD
        )

        add(card, password, bottom = 14)

        // CONFIRMAR SENHA

        add(
            card,
            label("CONFIRMAR SENHA", 10f, muted, true),
            bottom = 7
        )

        val confirmPassword = inputField(
            "Digite a senha novamente",
            InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_VARIATION_PASSWORD
        )

        add(card, confirmPassword, bottom = 20)

        // BOTÃO CADASTRAR

        val registerButton = makeButton(
            "CADASTRAR",
            true
        )

        add(card, registerButton, bottom = 10)

        // VOLTAR AO LOGIN

        val backButton = makeButton(
            "JÁ TENHO UMA CONTA — FAZER LOGIN",
            false
        )

        add(card, backButton)

        registerButton.setOnClickListener {
            val nameValue =
                nickname.text.toString().trim()

            val ageValue =
                age.text.toString().trim()

            val emailValue =
                email.text.toString().trim()

            val passwordValue =
                password.text.toString()

            val confirmValue =
                confirmPassword.text.toString()

            when {
                nameValue.isEmpty() -> {
                    nickname.error = "Digite seu apelido"
                    nickname.requestFocus()
                }

                ageValue.isEmpty() -> {
                    age.error = "Digite sua idade"
                    age.requestFocus()
                }

                ageValue.toIntOrNull() == null ||
                    ageValue.toInt() !in 1..120 -> {
                    age.error = "Digite uma idade válida"
                    age.requestFocus()
                }

                emailValue.isEmpty() ||
                    !android.util.Patterns.EMAIL_ADDRESS
                        .matcher(emailValue)
                        .matches() -> {
                    email.error = "Digite um e-mail válido"
                    email.requestFocus()
                }

                passwordValue.length < 8 -> {
                    password.error =
                        "Use pelo menos 8 caracteres"
                    password.requestFocus()
                }

                passwordValue != confirmValue -> {
                    confirmPassword.error =
                        "As senhas não são iguais"
                    confirmPassword.requestFocus()
                }

                else -> {
                    Toast.makeText(
                        this,
                        "Formulário validado! A criação da conta ainda precisa ser conectada.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        backButton.setOnClickListener {
            showLogin()
        }

        add(
            root,
            label(
                "ENTEREX SYSTEMS",
                8f,
                muted
            ),
            bottom = 8
        )

        setContentView(scroll)
    }

    @Deprecated("Usado para selecionar a foto de perfil")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(
            requestCode,
            resultCode,
            data
        )

        if (
            requestCode == 1001 &&
            resultCode == RESULT_OK
        ) {
            val uri = data?.data ?: return

            try {
                contentResolver.openInputStream(uri)
                    ?.use { stream ->
                        val bitmap =
                            BitmapFactory.decodeStream(stream)

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
