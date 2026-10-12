
package com.enterex.devicemanager

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
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
                Typeface.create("sans-serif", Typeface.BOLD)
            } else {
                Typeface.create("sans-serif", Typeface.NORMAL)
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = black
        window.navigationBarColor = black
        window.decorView.systemUiVisibility = 0

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
        setContentView(scroll)

        // Top bar
        val topBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val topTitle = TextView(this).apply {
            text = "ENTEREX"
            textSize = 15f
            letterSpacing = 0.18f
            setTextColor(silver)
            typeface = Typeface.DEFAULT_BOLD
        }

        topBar.addView(
            topTitle,
            LinearLayout.LayoutParams(0, dp(34), 1f)
        )

        val version = label("SYSTEM ONLINE  •  V1.0", 9f, muted)
        version.gravity = Gravity.CENTER_VERTICAL or Gravity.END
        topBar.addView(version)

        add(root, topBar, bottom = 28)

        // Emblem placeholder
        val emblem = TextView(this).apply {
            text = "◈"
            textSize = 76f
            gravity = Gravity.CENTER
            setTextColor(silver)
            typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
        }

        add(root, emblem, bottom = 4)

        // Brand
        val brand = label("ENTEREX", 32f, silver, true).apply {
            letterSpacing = 0.22f
        }
        add(root, brand)

        val subtitle = label("DEVICE MANAGER", 11f, muted).apply {
            letterSpacing = 0.30f
        }
        add(root, subtitle, bottom = 12)

        val divider = View(this).apply {
            setBackgroundColor(line)
        }

        root.addView(
            divider,
            LinearLayout.LayoutParams(dp(76), dp(1)).apply {
                gravity = Gravity.CENTER
                bottomMargin = dp(14)
            }
        )

        val motto = label(
            "CONTROLE. SEGURANÇA.\nSEMPRE COM VOCÊ.",
            11f,
            silver
        ).apply {
            letterSpacing = 0.08f
            setLineSpacing(dp(4).toFloat(), 1f)
        }

        add(root, motto, bottom = 28)

        // Login panel
        val loginPanel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(20), dp(18), dp(20))
            background = shape(panel, line, 14)
        }

        add(root, loginPanel, bottom = 16)

        val welcome = label("ACESSO AO SISTEMA", 13f, silver, true)
        welcome.gravity = Gravity.CENTER_VERTICAL
        add(loginPanel, welcome, bottom = 6)

        val description = label(
            "Entre com sua conta para continuar.",
            11f,
            muted
        )
        description.gravity = Gravity.CENTER_VERTICAL
        add(loginPanel, description, bottom = 20)

        val emailInput = inputField(
            "E-mail",
            InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        )
        add(loginPanel, emailInput, bottom = 12)

        val passwordInput = inputField(
            "Senha",
            InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_VARIATION_PASSWORD
        )
        add(loginPanel, passwordInput, bottom = 8)

        val forgot = TextView(this).apply {
            text = "Esqueceu sua senha?"
            textSize = 11f
            setTextColor(silver)
            gravity = Gravity.END
            setPadding(0, dp(8), 0, dp(8))
        }
        add(loginPanel, forgot, bottom = 14)

        // Enter button
        val enterButton = Button(this).apply {
            text = "ENTRAR"
            textSize = 13f
            letterSpacing = 0.12f
            setTextColor(black)
            typeface = Typeface.DEFAULT_BOLD
            isAllCaps = false
            background = shape(silver, radius = 10)
            minHeight = dp(50)
            setPadding(dp(12), 0, dp(12), 0)
        }

        add(loginPanel, enterButton, bottom = 10)

        // Create account button
        val createButton = Button(this).apply {
            text = "CRIAR CONTA"
            textSize = 12f
            letterSpacing = 0.08f
            setTextColor(silver)
            isAllCaps = false
            background = shape(panel, line, 10)
            minHeight = dp(48)
            setPadding(dp(12), 0, dp(12), 0)
        }

        add(loginPanel, createButton)

        // Footer features
        val footerTitle = label(
            "TECNOLOGIA AO SEU ALCANCE",
            9f,
            muted,
            true
        ).apply {
            letterSpacing = 0.12f
        }

        add(root, footerTitle, top = 10, bottom = 16)

        val features = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        fun feature(symbol: String, title: String): LinearLayout {
            val column = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(dp(4), 0, dp(4), 0)
            }

            val icon = label(symbol, 23f, silver)
            column.addView(
                icon,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(34)
                )
            )

            val caption = label(title, 9f, muted)
            column.addView(caption)

            return column
        }

        features.addView(
            feature("⌘", "Dispositivos"),
            LinearLayout.LayoutParams(0, dp(58), 1f)
        )

        features.addView(
            feature("◇", "Restauração"),
            LinearLayout.LayoutParams(0, dp(58), 1f)
        )

        features.addView(
            feature("◎", "Controle"),
            LinearLayout.LayoutParams(0, dp(58), 1f)
        )

        add(root, features, bottom = 18)

        val footer = label(
            "ENTEREX SYSTEMS  •  TODOS OS DIREITOS RESERVADOS",
            8f,
            muted
        )
        add(root, footer)

        // Temporary interactions: authentication is not connected yet.
        enterButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()

            when {
                email.isEmpty() -> {
                    emailInput.error = "Digite seu e-mail"
                    emailInput.requestFocus()
                }

                password.isEmpty() -> {
                    passwordInput.error = "Digite sua senha"
                    passwordInput.requestFocus()
                }

                else -> {
                    Toast.makeText(
                        this,
                        "Login ainda não conectado ao sistema.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        createButton.setOnClickListener {
            Toast.makeText(
                this,
                "Cadastro ainda será implementado.",
                Toast.LENGTH_LONG
            ).show()
        }

        forgot.setOnClickListener {
            Toast.makeText(
                this,
                "Recuperação de senha ainda será implementada.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun inputField(
        hintText: String,
        inputTypeValue: Int
    ): EditText {
        return EditText(this).apply {
            hint = hintText
            textSize = 13f
            setTextColor(silver)
            setHintTextColor(muted)
            inputType = inputTypeValue
            isSingleLine = true
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(14), 0, dp(14), 0)
            background = shape(black, line, 10)
            minHeight = dp(52)
        }
    }
}
