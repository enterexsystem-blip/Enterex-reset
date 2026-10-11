
package com.enterex.devicemanager

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.*

class MainActivity : Activity() {

    private val black = Color.rgb(5, 5, 6)
    private val panel = Color.rgb(12, 12, 13)
    private val silver = Color.rgb(230, 232, 236)
    private val muted = Color.rgb(155, 160, 170)
    private val line = Color.rgb(125, 130, 140)

    private fun dp(n: Int): Int =
        (n * resources.displayMetrics.density).toInt()

    private fun shape(
        color: Int,
        stroke: Int = Color.TRANSPARENT,
        radius: Float = 5f,
        strokeWidth: Int = 1
    ) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radius).toFloat()
        if (stroke != Color.TRANSPARENT) {
            setStroke(dp(strokeWidth), stroke)
        }
    }

    private fun label(
        value: String,
        size: Float,
        color: Int = silver,
        bold: Boolean = false
    ) = TextView(this).apply {
        text = value
        textSize = size
        setTextColor(color)
        gravity = Gravity.CENTER
        if (bold) setTypeface(Typeface.DEFAULT, Typeface.BOLD)
        letterSpacing = if (size >= 20f) 0.08f else 0.04f
    }

    private fun add(
        parent: LinearLayout,
        view: View,
        height: Int = -2,
        top: Int = 0
    ) {
        parent.addView(view, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            if (height == -2) -2 else dp(height)
        ).apply {
            topMargin = dp(top)
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = black
        window.navigationBarColor = black
        window.decorView.systemUiVisibility = 0

        val scroll = ScrollView(this).apply {
            setBackgroundColor(black)
            isFillViewport = true
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(24), dp(12), dp(24), dp(20))
            setBackgroundColor(black)
        }

        scroll.addView(root)

        val topBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val version = label(
            "ENTEREX\nDEVICE MANAGER\nv1.0.0",
            10f, muted
        ).apply {
            gravity = Gravity.START
            setLineSpacing(dp(3).toFloat(), 1f)
        }
        topBar.addView(version, LinearLayout.LayoutParams(0, -2, 1f))
        topBar.addView(label("◆  ◢  ▣", 13f, silver))
        add(root, topBar)

        // Área reservada para o emblema principal.
        // O dragão original será adicionado como recurso de imagem.
        val emblem = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(0, dp(24), 0, dp(2))
        }

        emblem.addView(label("◈", 82f, silver, true))
        emblem.addView(
            label("ENTEREX", 37f, silver, true),
            LinearLayout.LayoutParams(-1, -2)
        )
        emblem.addView(label(
            "D E V I C E   M A N A G E R", 11f, muted
        ))
        add(root, emblem, top = 5)

        add(root, label(
            "CONTROLE. SEGURANÇA.\nSEMPRE COM VOCÊ.",
            12f, silver, true
        ), top = 15)

        val form = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(18), dp(14), dp(18))
            background = shape(panel, line, 8f)
        }

        fun inputField(
            hintText: String,
            icon: String,
            type: Int
        ): EditText {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                background = shape(black, line, 5f)
                setPadding(dp(12), 0, dp(10), 0)
            }

            row.addView(label(icon, 23f, silver),
                LinearLayout.LayoutParams(dp(38), dp(54)))

            val edit = EditText(this).apply {
                hint = hintText
                setHintTextColor(muted)
                setTextColor(silver)
                textSize = 16f
                inputType = type
                singleLine = true
                background = null
                setPadding(dp(10), dp(10), dp(8), dp(10))
            }

            row.addView(edit, LinearLayout.LayoutParams(0, dp(54), 1f))
            add(form, row)
            return edit
        }

        val email = inputField(
            "E-mail",
            "♙",
            InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        )

        val password = inputField(
            "Senha",
            "▣",
            InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_VARIATION_PASSWORD
        )
        (password.parent as View).let {
            (it.layoutParams as LinearLayout.LayoutParams).topMargin = dp(12)
        }

        val forgot = label("Esqueci minha senha", 13f, silver).apply {
            gravity = Gravity.END
            paintFlags = paintFlags or 8
            setPadding(0, dp(14), dp(2), dp(18))
            setOnClickListener {
                Toast.makeText(
                    this@MainActivity,
                    "A recuperação de conta será implementada na etapa de autenticação.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        add(form, forgot)

        val login = Button(this).apply {
            text = "➜     E N T R A R"
            textSize = 16f
            setTextColor(black)
            setTypeface(Typeface.DEFAULT, Typeface.BOLD)
            background = shape(silver, Color.WHITE, 5f)
            isAllCaps = false
            setOnClickListener {
                if (email.text.isNullOrBlank() ||
                    password.text.isNullOrBlank()) {
                    Toast.makeText(
                        this@MainActivity,
                        "Preencha seu e-mail e sua senha.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "A interface está pronta; a autenticação ainda será conectada.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
        add(form, login, 58)

        val create = Button(this).apply {
            text = "♙     C R I A R   C O N T A"
            textSize = 14f
            setTextColor(silver)
            isAllCaps = false
            background = shape(black, silver, 5f)
            setOnClickListener {
                Toast.makeText(
                    this@MainActivity,
                    "A tela de cadastro será a próxima etapa.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        add(form, create, 56, 14)
        add(root, form, top = 24)

        val features = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, dp(28), 0, dp(8))
        }

        fun feature(symbol: String, title: String, subtitle: String) =
            LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                addView(label(symbol, 26f, silver))
                addView(label(title, 10f, muted, true))
                addView(label(subtitle, 10f, muted, true))
            }

        features.addView(feature("⌕", "GERENCIAMENTO", "DE DISPOSITIVOS"),
            LinearLayout.LayoutParams(0, -2, 1f))
        features.addView(View(this).apply {
            setBackgroundColor(line)
        }, LinearLayout.LayoutParams(dp(1), dp(58)))
        features.addView(feature("⬡", "RESTAURAÇÃO", "SEGURA"),
            LinearLayout.LayoutParams(0, -2, 1f))
        features.addView(View(this).apply {
            setBackgroundColor(line)
        }, LinearLayout.LayoutParams(dp(1), dp(58)))
        features.addView(feature("ϟ", "MAIS", "CONTROLE"),
            LinearLayout.LayoutParams(0, -2, 1f))

        add(root, features, top = 10)
        setContentView(scroll)
    }
}
