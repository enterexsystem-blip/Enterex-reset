package com.enterex.devicemanager

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.*

class MainActivity : Activity() {
    private val bg = Color.rgb(7, 7, 7)
    private val white = Color.rgb(245, 245, 245)
    private val muted = Color.rgb(165, 165, 165)

    private fun dp(v: Int) =
        (v * resources.displayMetrics.density).toInt()

    private fun params() =
        LinearLayout.LayoutParams(-1, -2)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = bg
        window.navigationBarColor = bg

        val scroll = ScrollView(this)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(28), dp(28), dp(28), dp(24))
            setBackgroundColor(bg)
        }

        scroll.addView(root)

        val emblem = TextView(this).apply {
            text = "♜"
            textSize = 56f
            gravity = Gravity.CENTER
            setTextColor(white)
        }

        root.addView(emblem, params())

        root.addView(TextView(this).apply {
            text = "ENTEREX"
            textSize = 31f
            letterSpacing = .18f
            gravity = Gravity.CENTER
            setTextColor(white)
            setTypeface(null, 1)
        }, params())

        root.addView(TextView(this).apply {
            text = "D E V I C E   M A N A G E R"
            textSize = 11f
            gravity = Gravity.CENTER
            setTextColor(muted)
            setPadding(0, dp(4), 0, dp(28))
        }, params())

        root.addView(TextView(this).apply {
            text = "Entrar na sua conta"
            textSize = 23f
            gravity = Gravity.CENTER
            setTextColor(white)
            setTypeface(null, 1)
        }, params())

        root.addView(TextView(this).apply {
            text = "Acesse sua conta para continuar."
            textSize = 14f
            gravity = Gravity.CENTER
            setTextColor(muted)
            setPadding(0, dp(8), 0, dp(22))
        }, params())

        fun field(
            hintText: String,
            password: Boolean = false
        ): EditText {
            return EditText(this).apply {
                hint = hintText
                textSize = 15f
                setTextColor(white)
                setHintTextColor(muted)
                setSingleLine(true)
                setPadding(dp(14), dp(12), dp(14), dp(12))
                setBackgroundColor(Color.rgb(25, 25, 25))

                if (password) inputType = 129
            }
        }

        val email = field("E-mail")
        email.inputType = 33
        root.addView(email, params())

        val pass = field("Senha", true)

        params().also {
            it.topMargin = dp(12)
            root.addView(pass, it)
        }

        root.addView(TextView(this).apply {
            text = "Esqueci minha senha"
            gravity = Gravity.END
            textSize = 13f
            setTextColor(white)
            setPadding(0, dp(14), 0, dp(22))

            setOnClickListener {
                Toast.makeText(
                    this@MainActivity,
                    "A recuperação será configurada na próxima etapa.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }, params())

        root.addView(Button(this).apply {
            text = "ENTRAR"
            setTextColor(Color.BLACK)
            setBackgroundColor(white)

            setOnClickListener {
                val msg =
                    if (email.text.isNullOrBlank() ||
                        pass.text.isNullOrBlank()
                    ) {
                        "Preencha e-mail e senha."
                    } else {
                        "Interface pronta. O login real ainda precisa ser conectado."
                    }

                Toast.makeText(
                    this@MainActivity,
                    msg,
                    Toast.LENGTH_LONG
                ).show()
            }
        }, params())

        root.addView(TextView(this).apply {
            text = "Não tem uma conta?  Criar conta"
            gravity = Gravity.CENTER
            textSize = 14f
            setTextColor(white)
            setPadding(0, dp(22), 0, dp(12))

            setOnClickListener {
                Toast.makeText(
                    this@MainActivity,
                    "O cadastro será criado na próxima etapa.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }, params())

        root.addView(TextView(this).apply {
            text = "◈  SEGURANÇA E PRIVACIDADE"
            gravity = Gravity.CENTER
            textSize = 11f
            setTextColor(muted)
            setPadding(0, dp(22), 0, 0)
        }, params())

        setContentView(scroll)
    }
}
