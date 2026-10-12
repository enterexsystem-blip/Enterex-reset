
package com.enterex.devicemanager

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.BitmapFactory
import android.graphics.drawable.GradientDrawable
import android.content.Intent
import android.net.Uri
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.text.method.HideReturnsTransformationMethod
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.CheckBox
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

    private var darkMode = true
    private val black: Int get() = if (darkMode) Color.rgb(5, 6, 8) else Color.rgb(246, 247, 249)
    private val panel: Int get() = if (darkMode) Color.rgb(5, 6, 8) else Color.WHITE
    private val silver: Int get() = if (darkMode) Color.rgb(225, 229, 235) else Color.rgb(25, 29, 35)
    private val muted: Int get() = if (darkMode) Color.rgb(135, 143, 153) else Color.rgb(95, 103, 113)
    private val line: Int get() = if (darkMode) Color.rgb(65, 73, 84) else Color.rgb(205, 211, 219)
    private val accent: Int get() = if (darkMode) Color.rgb(180, 190, 205) else Color.rgb(80, 91, 108)

    private val accounts by lazy {
        getSharedPreferences("enterex_accounts", MODE_PRIVATE)
    }

    private val authorizations by lazy {
        getSharedPreferences("enterex_authorizations", MODE_PRIVATE)
    }

    private var profileImage: ImageView? = null
    private var selectedPhoto: Uri? = null
    private var currentAccount = JSONObject()

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

    private fun space(parent: LinearLayout, height: Int) {
        parent.addView(
            View(this),
            LinearLayout.LayoutParams(1, dp(height))
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
            setPadding(dp(22), dp(20), dp(22), dp(20))
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

        val brand = label("🐉  ENTEREX", 17f, silver, true).apply {
            gravity = Gravity.CENTER_VERTICAL
            letterSpacing = 0.08f
        }

        header.addView(
            brand,
            LinearLayout.LayoutParams(0, dp(38), 1f)
        )
        header.addView(label("DEVICE MANAGER", 9f, muted))
        add(root, header, bottom = 22)
    }

    private fun addBrand(root: LinearLayout) {
        add(root, label("🐉", 52f, silver), bottom = 2)
        add(root, label("ENTEREX", 30f, silver, true), bottom = 3)
        add(
            root,
            label("D E V I C E   M A N A G E R", 10f, muted),
            bottom = 12
        )

        root.addView(
            View(this).apply { setBackgroundColor(line) },
            LinearLayout.LayoutParams(dp(76), dp(1)).apply {
                gravity = Gravity.CENTER
                bottomMargin = dp(20)
            }
        )
    }

    private fun inputField(hintText: String, type: Int) =
        EditText(this).apply {
            hint = hintText
            textSize = 14f
            setTextColor(silver)
            setHintTextColor(muted)
            inputType = type
            isSingleLine = true
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(14), dp(4), dp(14), dp(4))
            background = shape(black, Color.WHITE, 10)
            minHeight = dp(52)
        }

    private fun makeButton(textValue: String, filled: Boolean) =
        Button(this).apply {
            text = textValue
            textSize = 13f
            isAllCaps = false
            letterSpacing = 0.03f
            setTextColor(if (filled) black else Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            background = if (filled) {
                shape(silver, Color.WHITE, 10)
            } else {
                shape(black, Color.WHITE, 10)
            }
            minHeight = dp(48)
            setPadding(dp(12), 0, dp(12), 0)
        }

    private fun card(): LinearLayout =
        LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
            background = shape(panel, line, 16)
        }

    private fun addPasswordToggle(parent: LinearLayout, field: EditText, bottom: Int = 8) {
        val toggle = CheckBox(this).apply {
            text = "Mostrar senha"
            textSize = 12f
            setTextColor(silver)
            buttonTintList = android.content.res.ColorStateList.valueOf(Color.WHITE)
            setOnCheckedChangeListener { _, checked ->
                val cursor = field.selectionStart
                field.transformationMethod = if (checked) {
                    HideReturnsTransformationMethod.getInstance()
                } else {
                    PasswordTransformationMethod.getInstance()
                }
                if (cursor >= 0) field.setSelection(cursor.coerceAtMost(field.text.length))
            }
        }
        add(parent, toggle, bottom = bottom)
    }

    private fun continueAfterLogin(account: JSONObject) {
        currentAccount = account
        val key = normalizedEmail(account.optString("email", ""))
        if (authorizations.getBoolean("consent_$key", false)) {
            showDashboard()
        } else {
            showAuthorizationNotice(key)
        }
    }

    private fun showAuthorizationNotice(accountKey: String) {
        val black = Color.rgb(5, 6, 8)
        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(18), dp(20), dp(16))
            setBackgroundColor(black)
        }

        val heading = TextView(this).apply {
            text = "ENTEREX  /  TERMO DE AUTORIZAÇÃO"
            textSize = 17f
            setTextColor(silver)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(8))
        }
        panel.addView(heading)

        val subtitle = TextView(this).apply {
            text = "Leia até o final e escolha uma opção para continuar."
            textSize = 12f
            setTextColor(muted)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(14))
        }
        panel.addView(subtitle)

        val message = """
            TERMO DE CIÊNCIA, AUTORIZAÇÃO E RESPONSABILIDADE — ENTEREX

            1. DISPOSIÇÕES GERAIS
            O acesso ao painel do aplicativo Enterex depende da leitura e da manifestação expressa de concordância com este termo.

            2. REGISTRO DA ESCOLHA
            A opção selecionada será armazenada localmente neste dispositivo, associada à conta utilizada, para evitar a exibição repetida deste aviso enquanto o registro permanecer disponível.

            3. LIMITES DA AUTORIZAÇÃO
            A concordância registra ciência deste termo e libera o acesso ao painel. Não concede, por si só, permissões do Android, autorização de depuração USB, acesso à conta Google, vínculo com serviços de terceiros nem autorização técnica para controlar, restaurar ou apagar dispositivos.

            4. PERMISSÕES E COMPATIBILIDADE
            Cada recurso depende de implementação efetiva, compatibilidade técnica, permissões específicas do sistema e, quando aplicável, dos procedimentos oficiais do fabricante. Este termo não substitui essas etapas.

            5. RISCO DE PERDA DE DADOS
            Procedimentos de restauração ou apagamento, quando disponíveis e devidamente autorizados, podem eliminar dados permanentemente. Devem ser realizados somente em dispositivos próprios ou com autorização legítima do proprietário.

            6. MANIFESTAÇÃO DE VONTADE
            Ao aceitar, você confirma que leu e compreendeu as condições acima. Ao recusar, o acesso ao painel será bloqueado e você retornará à tela de acesso.

            Role esta mensagem até o final antes de escolher.
        """.trimIndent()

        val body = TextView(this).apply {
            text = message
            textSize = 13f
            setTextColor(silver)
            setLineSpacing(dp(4).toFloat(), 1.05f)
            setPadding(dp(14), dp(14), dp(14), dp(14))
            background = shape(Color.rgb(13, 15, 19), line, 12)
        }
        val termsScroll = ScrollView(this).apply {
            isFillViewport = false
            overScrollMode = View.OVER_SCROLL_IF_CONTENT_SCROLLS
            addView(body, android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
            ))
        }
        panel.addView(termsScroll, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(330)
        ))

        val accept = makeButton("ACEITAR E CONTINUAR", true)
        val decline = makeButton("RECUSAR E SAIR", false)
        val buttons = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(14), 0, 0)
        }
        buttons.addView(accept, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(48)
        ))
        buttons.addView(decline, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(46)
        ).apply { topMargin = dp(8) })
        panel.addView(buttons)

        val dialog = AlertDialog.Builder(this)
            .setView(panel)
            .setCancelable(false)
            .create()

        accept.setOnClickListener {
            authorizations.edit().putBoolean("consent_$accountKey", true).apply()
            dialog.dismiss()
            showCreatorIntro()
        }
        decline.setOnClickListener {
            currentAccount = JSONObject()
            dialog.dismiss()
            Toast.makeText(
                this,
                "Você recusou o termo. O painel continuará bloqueado.",
                Toast.LENGTH_LONG
            ).show()
            showLogin()
        }
        dialog.setOnShowListener {
            dialog.window?.setBackgroundDrawableResource(android.R.color.black)
        }
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.black)
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.94f).toInt(),
            android.view.WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    private fun showCreatorIntro() {
        val (scroll, root) = createRoot()
        root.gravity = Gravity.CENTER
        root.setPadding(dp(24), dp(24), dp(24), dp(24))
        val intro = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            alpha = 1f
        }
        add(intro, label("🐉", 58f, silver, true), bottom = 12)
        add(intro, label("KYODAI // 4CHAN", 25f, silver, true), bottom = 14)
        add(intro, label("CRIADOR DO ENTEREX", 11f, muted, true), bottom = 10)
        add(intro, label("OBRIGADO POR USAR", 10f, silver), bottom = 26)
        val progress = View(this).apply { background = shape(silver, Color.TRANSPARENT, 2) }
        intro.addView(progress, LinearLayout.LayoutParams(dp(96), dp(2)).apply { gravity = Gravity.CENTER })
        root.addView(intro, LinearLayout.LayoutParams(-1, -2))
        setContentView(scroll)
        Handler(Looper.getMainLooper()).postDelayed({
            if (isFinishing || isDestroyed) return@postDelayed
            intro.animate().alpha(0f).setDuration(1100L).withEndAction {
                if (!isFinishing && !isDestroyed) showDashboard()
            }.start()
        }, 1500L)
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
        password: String,
        photoUri: String = ""
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
                put("email", key)
                if (photoUri.isNotBlank()) put("photoUri", photoUri)
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
            val salt = Base64.decode(
                account.getString("salt"), Base64.NO_WRAP
            )
            val expected = Base64.decode(
                account.getString("hash"), Base64.NO_WRAP
            )
            val actual = hashPassword(password, salt)

            if (MessageDigest.isEqual(expected, actual)) account else null
        } catch (e: Exception) {
            null
        }
    }

    private val screenHistory = java.util.ArrayDeque<() -> Unit>()
    private var handlingBack = false

    private fun navigateTo(screen: () -> Unit) {
        if (!handlingBack) screenHistory.addLast(screen)
        screen()
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        if (screenHistory.isNotEmpty()) {
            handlingBack = true
            try { screenHistory.removeLast().invoke() } finally { handlingBack = false }
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        darkMode = getSharedPreferences("enterex_settings", MODE_PRIVATE)
            .getBoolean("dark_mode", true)
        window.statusBarColor = black
        window.navigationBarColor = black
        showSplash()
    }

    private fun showSplash() {
        val (scroll, root) = createRoot()
        root.gravity = Gravity.CENTER
        val title = label("ENTEREX", 34f, silver, true).apply {
            typeface = Typeface.create("sans-serif-black", Typeface.BOLD)
            letterSpacing = 0.16f
            alpha = 0f
        }
        val subtitle = label("DEVICE MANAGEMENT", 10f, muted, true).apply { alpha = 0f }
        add(root, title, bottom = 8)
        add(root, subtitle)
        setContentView(scroll)
        title.animate().alpha(1f).setDuration(450L).start()
        subtitle.animate().alpha(1f).setDuration(450L).start()
        Handler(Looper.getMainLooper()).postDelayed({
            if (!isFinishing && !isDestroyed) {
                title.animate().alpha(0f).setDuration(450L).start()
                subtitle.animate().alpha(0f).setDuration(450L).withEndAction {
                    if (!isFinishing && !isDestroyed) showLogin()
                }.start()
            }
        }, 1800L)
    }

    private fun showOperationProgress(message: String): AlertDialog {
        val progress = android.widget.ProgressBar(this).apply {
            isIndeterminate = true
            indeterminateTintList = android.content.res.ColorStateList.valueOf(accent)
        }
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(24), dp(20), dp(24), dp(20))
            addView(progress, LinearLayout.LayoutParams(dp(32), dp(32)))
            addView(label(message, 14f, silver).apply {
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(16), 0, 0, 0)
            }, LinearLayout.LayoutParams(0, dp(48), 1f))
        }
        return AlertDialog.Builder(this)
            .setView(container)
            .setCancelable(false)
            .create()
            .also { it.show() }
    }

    private fun showLogin() {
        val (scroll, root) = createRoot()
        addHeader(root)
        addBrand(root)

        add(
            root,
            label("CONTROLE. SEGURANÇA.\nSEMPRE COM VOCÊ.", 11f),
            bottom = 24
        )

        val form = card()
        add(root, form, bottom = 20)

        add(form, label("ACESSO AO SISTEMA", 13f, silver, true), bottom = 6)
        add(form, label("Entre com sua conta para continuar.", 11f, muted),
            bottom = 20)

        val email = inputField(
            "E-mail",
            InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        )
        add(form, email, bottom = 12)

        val password = inputField(
            "Senha",
            InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_VARIATION_PASSWORD
        )
        add(form, password, bottom = 2)
        addPasswordToggle(form, password, bottom = 8)

        val forgot = label("Esqueceu sua senha?", 11f, silver).apply {
            gravity = Gravity.END
            setPadding(0, dp(8), 0, dp(16))
        }
        add(form, forgot)

        val enter = makeButton("ENTRAR", true)
        add(form, enter, bottom = 10)

        val create = makeButton("CRIAR CONTA", false)
        add(form, create)

        add(root, label("TECNOLOGIA AO SEU ALCANCE", 9f, muted, true),
            top = 10, bottom = 16)

        add(
            root,
            label("⌘  Dispositivos       ◇  Restauração       ◎  Controle",
                10f, silver),
            bottom = 20
        )
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
                    enter.isEnabled = false
                    create.isEnabled = false
                    val progress = showOperationProgress("Verificando acesso…")
                    Thread {
                        val account = checkLogin(emailValue, passwordValue)
                        runOnUiThread {
                            progress.dismiss()
                            enter.isEnabled = true
                            create.isEnabled = true
                            if (isFinishing || isDestroyed) return@runOnUiThread
                            if (account == null) {
                                Toast.makeText(
                                    this, "E-mail ou senha incorretos.",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                continueAfterLogin(account)
                            }
                        }
                    }.start()
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
        add(root, label("Faça parte do sistema ENTEREX.", 12f, muted),
            bottom = 22)

        val form = card()
        add(root, form, bottom = 18)

        add(form, label("DADOS DO USUÁRIO", 12f, silver, true), bottom = 16)
        add(form, label("FOTO DE PERFIL", 10f, muted, true), bottom = 10)

        val avatar = ImageView(this).apply {
            setImageDrawable(shape(black, line, 60))
            scaleType = ImageView.ScaleType.CENTER_CROP
            background = shape(black, line, 60)
            clipToOutline = true
        }
        profileImage = avatar

        form.addView(
            avatar,
            LinearLayout.LayoutParams(dp(92), dp(92)).apply {
                gravity = Gravity.CENTER
                bottomMargin = dp(12)
            }
        )

        val photoButton = makeButton("ADICIONAR FOTO (OPCIONAL)", false)
        add(form, photoButton, bottom = 20)

        photoButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "image/*"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            }
            @Suppress("DEPRECATION")
            startActivityForResult(intent, 1001)
        }

        add(form, label("APELIDO", 10f, muted, true), bottom = 7)
        val nickname = inputField(
            "Como quer ser chamado?",
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS
        )
        add(form, nickname, bottom = 14)

        add(form, label("IDADE", 10f, muted, true), bottom = 7)
        val age = inputField("Idade em anos", InputType.TYPE_CLASS_NUMBER)
        add(form, age, bottom = 14)

        add(form, label("E-MAIL", 10f, muted, true), bottom = 7)
        val email = inputField(
            "Digite seu e-mail",
            InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        )
        add(form, email, bottom = 14)

        add(form, label("SENHA", 10f, muted, true), bottom = 7)
        val password = inputField(
            "Crie uma senha",
            InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_VARIATION_PASSWORD
        )
        add(form, password, bottom = 2)
        addPasswordToggle(form, password, bottom = 12)

        add(form, label("CONFIRMAR SENHA", 10f, muted, true), bottom = 7)
        val confirm = inputField(
            "Digite a senha novamente",
            InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_VARIATION_PASSWORD
        )
        add(form, confirm, bottom = 2)
        addPasswordToggle(form, confirm, bottom = 20)

        val register = makeButton("CADASTRAR", true)
        add(form, register, bottom = 10)

        val back = makeButton("JÁ TENHO CONTA — FAZER LOGIN", false)
        add(form, back)

        register.setOnClickListener {
            val name = nickname.text.toString().trim()
            val ageText = age.text.toString().trim()
            val emailText = email.text.toString().trim()
            val pass = password.text.toString()
            val confirmText = confirm.text.toString()

            val parsedAge = ageText.toIntOrNull()
            when {
                name.isEmpty() -> {
                    nickname.error = "Digite seu apelido"
                    nickname.requestFocus()
                }
                parsedAge == null || parsedAge !in 1..120 -> {
                    age.error = "Digite uma idade válida"
                    age.requestFocus()
                }
                !android.util.Patterns.EMAIL_ADDRESS
                    .matcher(emailText).matches() -> {
                    email.error = "Digite um e-mail válido"
                    email.requestFocus()
                }
                pass.length < 8 -> {
                    password.error = "Use pelo menos 8 caracteres"
                    password.requestFocus()
                }
                pass != confirmText -> {
                    confirm.error = "As senhas não são iguais"
                    confirm.requestFocus()
                }
                else -> {
                    register.isEnabled = false
                    back.isEnabled = false
                    val progress = showOperationProgress("Criando sua conta…")
                    Thread {
                        val saved = createAccount(
                            name, parsedAge!!, "", emailText, pass,
                            selectedPhoto?.toString().orEmpty()
                        )
                        runOnUiThread {
                            progress.dismiss()
                            register.isEnabled = true
                            back.isEnabled = true
                            if (isFinishing || isDestroyed) return@runOnUiThread
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
                        }
                    }.start()
                }
            }
        }

        back.setOnClickListener { showLogin() }
        add(root, label("ENTEREX SYSTEMS", 8f, muted), bottom = 8)
        setContentView(scroll)
    }

    // PAINEL PRINCIPAL

    private fun showDashboard() {
        val (scroll, root) = createRoot()
        root.setPadding(dp(18), dp(18), dp(18), dp(14))

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val brand = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        add(brand, label("🐉  ENTEREX", 21f, silver, true))
        add(brand, label("DEVICE MANAGER", 9f, muted))
        header.addView(brand, LinearLayout.LayoutParams(0, -2, 1f))

        val actions = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val notifications = TextView(this).apply {
            text = "♧"
            textSize = 25f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            background = shape(Color.rgb(24, 27, 33), Color.rgb(115, 125, 140), 12)
            contentDescription = "Notificações"
            setOnClickListener { showNotifications() }
        }
        actions.addView(notifications, LinearLayout.LayoutParams(dp(44), dp(44)).apply {
            marginEnd = dp(8)
        })
        val profile = TextView(this).apply {
            text = "♙"
            textSize = 25f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            background = shape(Color.rgb(24, 27, 33), Color.rgb(115, 125, 140), 22)
            contentDescription = "Meu perfil"
            setOnClickListener { showProfile() }
        }
        actions.addView(profile, LinearLayout.LayoutParams(dp(44), dp(44)))
        header.addView(actions)
        add(root, header, bottom = 24)

        add(root, label("PAINEL DE CONTROLE", 10f, muted, true),
            bottom = 8)
        add(root, label("Gerencie seus dispositivos em um só lugar.",
            13f, muted), bottom = 22)

        val status = card()
        add(status, label("STATUS DO SISTEMA", 10f, muted, true),
            bottom = 12)

        val statusRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val dot = TextView(this).apply {
            text = "●"
            textSize = 16f
            setTextColor(accent)
        }
        statusRow.addView(dot)
        val statusText = label("  Aguardando dispositivo", 15f, silver, true)
        statusText.gravity = Gravity.CENTER_VERTICAL
        statusRow.addView(statusText)
        add(status, statusRow, bottom = 8)
        add(status, label(
            "Nenhum aparelho USB foi identificado.",
            12f, muted
        ).apply { gravity = Gravity.START })
        add(root, status, bottom = 22)

        add(root, label("ACESSO RÁPIDO", 12f, muted, true), bottom = 12)

        val quickGrid = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        fun quickRow(vararg items: Triple<String, String, () -> Unit>) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }
            items.forEachIndexed { index, entry ->
                val tile = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER_VERTICAL
                    isClickable = true
                    isFocusable = true
                    setPadding(dp(16), dp(16), dp(12), dp(16))
                    background = shape(black, Color.WHITE, 14)
                    setOnClickListener { entry.third.invoke() }
                }
                val iconBox = TextView(this).apply {
                    text = entry.first
                    textSize = 24f
                    gravity = Gravity.CENTER
                    setTextColor(Color.WHITE)
                    background = shape(black, Color.WHITE, 10)
                    minWidth = dp(52)
                    minHeight = dp(52)
                    setPadding(dp(8), dp(8), dp(8), dp(8))
                }
                add(tile, iconBox, bottom = 12)
                add(tile, label(entry.second, 14f, Color.WHITE, true), bottom = 4)
                add(tile, label(
                    when (entry.second) {
                        "Dispositivos" -> "Conexão USB"
                        "Ferramentas" -> "Diagnóstico"
                        "Meu perfil" -> "Conta e dados"
                        "Configurações" -> "Preferências do app"
                        "Nuvem Enterex" -> "Arquivos privados"
                        else -> "Ajuda e informações"
                    }, 11f, muted
                ))
                row.addView(tile, LinearLayout.LayoutParams(0, dp(142), 1f).apply {
                    if (index == 0) marginEnd = dp(7) else marginStart = dp(7)
                })
            }
            quickGrid.addView(row, LinearLayout.LayoutParams(-1, -2).apply {
                bottomMargin = dp(14)
            })
        }

        quickRow(
            Triple("▣", "Dispositivos", { showDevices() }),
            Triple("⌘", "Ferramentas", { showTools() })
        )
        quickRow(
            Triple("◉", "Meu perfil", { showProfile() }),
            Triple("♧", "Notificações", { showNotifications() })
        )
        quickRow(
            Triple("☁", "Nuvem Enterex", { showCloudStorage() }),
            Triple("⚙", "Configurações", { showSettings() })
        )
        quickRow(
            Triple("ⓘ", "Sobre o sistema", { showSettings() }),
            Triple("◌", "Planos", { showPlans() })
        )
        add(root, quickGrid, bottom = 8)

        add(root, label("DISPOSITIVOS", 12f, muted, true), bottom = 12)

        val empty = card()
        add(empty, label("▱", 36f, silver), bottom = 8)
        add(empty, label("Nenhum dispositivo conectado", 15f, silver, true),
            bottom = 6)
        add(empty, label(
            "Os aparelhos identificados aparecerão nesta área.",
            12f, muted
        ).apply { gravity = Gravity.CENTER })
        add(root, empty, bottom = 22)

        add(root, label("ENTEREX SYSTEMS", 8f, muted), top = 10)
        setContentView(scroll)
    }


    private fun showNotifications() {
        val (scroll, root) = createRoot()
        addHeader(root)
        add(root, label("NOTIFICAÇÕES", 20f, silver, true), bottom = 8)
        add(root, label("Avisos e atualizações da sua conta aparecerão aqui.", 13f, muted), bottom = 18)
        val empty = card()
        add(empty, label("♧", 34f, silver), bottom = 8)
        add(empty, label("Tudo tranquilo por aqui", 15f, silver, true), bottom = 5)
        add(empty, label("Você ainda não tem notificações.", 12f, muted))
        add(root, empty, bottom = 18)
        addBottomNavigation(root, "Notificações")
        setContentView(scroll)
    }

    private fun showCloudStorage() {
        val (scroll, root) = createRoot()
        addHeader(root)
        add(root, label("NUVEM ENTEREX", 21f, silver, true), bottom = 6)
        add(root, label("Seus arquivos organizados em um só lugar.", 13f, muted), bottom = 18)

        fun storageTile(icon: String, title: String, detail: String, action: () -> Unit, locked: Boolean = false) {
            val tile = card().apply {
                background = shape(Color.rgb(10, 12, 15), Color.rgb(75, 83, 96), 18)
                isClickable = true
                isFocusable = true
                elevation = dp(2).toFloat()
            }
            val iconView = label(icon, 28f, silver, true).apply {
                background = shape(Color.rgb(25, 29, 35), Color.rgb(90, 100, 115), 14)
                setPadding(dp(8), dp(6), dp(8), dp(6))
            }
            val copy = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(14), 0, 0, 0)
            }
            add(copy, label(title + if (locked) "  🔒" else "", 15f, silver, true), bottom = 4)
            add(copy, label(detail, 11f, muted).apply { gravity = Gravity.START })
            val plus = makeButton("+", true).apply {
                setOnClickListener { animateCloudTile(tile, iconView, action) }
            }
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }
            row.addView(iconView, LinearLayout.LayoutParams(dp(52), dp(52)))
            row.addView(copy, LinearLayout.LayoutParams(0, -2, 1f))
            row.addView(plus, LinearLayout.LayoutParams(dp(48), dp(44)))
            add(tile, row)
            tile.setOnClickListener { animateCloudTile(tile, iconView, action) }
            add(root, tile, bottom = 12)
        }


        storageTile("▧", "Imagens", "Free: até 500 MB • Pro: ilimitado", { pickCloudFile("image/*", "Imagens") })
        storageTile("▶", "Vídeos", "Free: até 500 MB • Pro: ilimitado", { pickCloudFile("video/*", "Vídeos") })
        val premium = currentAccount.optString("plan", "free").equals("premium", true)
        storageTile("♫", "Músicas", if (premium) "Disponível no Premium" else "Exclusivo Premium", {
            if (premium) pickCloudFile("audio/*", "Músicas") else showPlans()
        }, locked = !premium)
        storageTile("▤", "Anotações", "Disponível nos planos Free e Pro", { showNotes() })
        add(root, label("Aviso: envio e armazenamento em nuvem ainda dependem do serviço online e não estão ativos nesta versão.", 11f, muted), bottom = 16)
        addBottomNavigation(root, "Nuvem")
        setContentView(scroll)
    }

    private fun animateCloudTile(tile: View, iconView: View, action: () -> Unit) {
        tile.animate().cancel()
        iconView.animate().cancel()
        tile.animate()
            .scaleX(0.97f).scaleY(0.97f)
            .translationZ(dp(6).toFloat())
            .setDuration(90L)
            .withEndAction {
                tile.animate()
                    .scaleX(1f).scaleY(1f)
                    .translationZ(0f)
                    .setDuration(160L)
                    .start()
                iconView.animate()
                    .scaleX(1.12f).scaleY(1.12f)
                    .setDuration(100L)
                    .withEndAction {
                        iconView.animate().scaleX(1f).scaleY(1f).setDuration(140L).start()
                        action.invoke()
                    }.start()
            }.start()
    }

    private fun pickCloudFile(mimeType: String, category: String) {
        Toast.makeText(this, "O envio de $category será ativado quando a nuvem estiver disponível.", Toast.LENGTH_LONG).show()
        // Picker remains disabled until secure cloud upload and quota enforcement are implemented.
    }

    private fun showNotes() {
        val input = EditText(this).apply {
            hint = "Escreva uma anotação"
            setTextColor(silver)
            setHintTextColor(muted)
            background = shape(black, Color.WHITE, 10)
            setPadding(dp(12), dp(12), dp(12), dp(12))
            minHeight = dp(120)
        }
        AlertDialog.Builder(this).setTitle("Anotações").setView(input)
            .setPositiveButton("SALVAR") { _, _ ->
                val notes = getSharedPreferences("enterex_notes", MODE_PRIVATE)
                notes.edit().putString("note_" + System.currentTimeMillis(), input.text.toString()).apply()
                Toast.makeText(this, "Anotação salva neste dispositivo.", Toast.LENGTH_SHORT).show()
            }.setNegativeButton("CANCELAR", null).show()
    }

    private fun addBottomNavigation(root: LinearLayout, selected: String) {
        val nav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            background = shape(Color.rgb(16, 18, 22), Color.rgb(65, 73, 84), 22)
            setPadding(dp(5), dp(6), dp(5), dp(6))
            elevation = dp(8).toFloat()
        }
        val items = listOf(
            Triple("⌂", "Início", { showDashboard() }),
            Triple("☁", "Nuvem", { showCloudStorage() }),
            Triple("♧", "Avisos", { showNotifications() }),
            Triple("⚙", "Ajustes", { showSettings() })
        )
        items.forEach { item ->
            val active = item.second == selected
            val slot = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                background = if (active) shape(Color.rgb(43, 48, 57), Color.rgb(125, 137, 154), 16)
                    else shape(Color.TRANSPARENT, Color.TRANSPARENT, 16)
                setPadding(dp(3), dp(5), dp(3), dp(4))
                isClickable = true
                isFocusable = true
                contentDescription = item.second
                setOnClickListener {
                    animate().cancel()
                    animate().scaleX(0.96f).scaleY(0.96f).setDuration(70).withEndAction {
                        animate().scaleX(1f).scaleY(1f).setDuration(140).start()
                        item.third.invoke()
                    }.start()
                }
            }
            val icon = TextView(this).apply {
                text = item.first
                textSize = if (active) 25f else 22f
                gravity = Gravity.CENTER
                setTextColor(if (active) Color.WHITE else Color.rgb(132, 141, 154))
                scaleX = if (active) 1.08f else 1f
                scaleY = if (active) 1.08f else 1f
            }
            val caption = TextView(this).apply {
                text = item.second
                textSize = if (active) 10f else 9f
                gravity = Gravity.CENTER
                setTextColor(if (active) Color.WHITE else Color.rgb(132, 141, 154))
                typeface = if (active) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
                setPadding(0, dp(1), 0, 0)
            }
            slot.addView(icon, LinearLayout.LayoutParams(-1, dp(28)))
            slot.addView(caption, LinearLayout.LayoutParams(-1, dp(17)))
            nav.addView(slot, LinearLayout.LayoutParams(0, dp(54), 1f).apply {
                marginStart = dp(2)
                marginEnd = dp(2)
            })
            if (active) {
                slot.alpha = 0.65f
                slot.animate().alpha(1f).scaleX(1.04f).scaleY(1.04f)
                    .setDuration(240).start()
            }
        }
        add(root, nav, top = 16, bottom = 8)
    }

    private fun showPlans() {
        val offers = listOf(
            Triple("ENTEREX ESSENCIAL", "R$ 15,00 • 7 dias", "Acesso temporário às ferramentas compatíveis e orientações para recuperação."),
            Triple("ENTEREX PLUS", "R$ 30,00 • 15 dias", "Mais tempo para diagnóstico e acompanhamento organizado dos procedimentos."),
            Triple("ENTEREX PREMIUM", "R$ 50,00 • 1 mês", "Plano pensado para quem busca atendimento prioritário e suporte ampliado.")
        )
        val offer = offers[java.security.SecureRandom().nextInt(offers.size)]
        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(18), dp(20), dp(16))
            background = shape(Color.rgb(5, 6, 8), Color.rgb(120, 130, 145), 16)
        }
        add(panel, label("ENTEREX  /  PLANOS", 10f, muted, true), bottom = 12)
        add(panel, label(offer.first, 21f, silver, true), bottom = 8)
        add(panel, label(offer.second, 25f, Color.WHITE, true), bottom = 14)
        add(panel, label(offer.third, 13f, silver), bottom = 14)
        add(panel, label("Armazenamento privado de fotos, contatos, vídeos e músicas, recuperação de arquivos e suporte 24 horas são recursos planejados para planos pagos, mas ainda não estão implementados.", 12f, muted), bottom = 12)
        add(panel, label("A compra e a ativação online ainda não estão disponíveis. Nenhuma cobrança será feita por esta tela.", 12f, muted), bottom = 14)
        val dialog = AlertDialog.Builder(this).setView(panel).setNegativeButton("FECHAR", null).create()
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout((resources.displayMetrics.widthPixels * 0.92f).toInt(), android.view.WindowManager.LayoutParams.WRAP_CONTENT)
    }

    private fun showDevices() {
        val (scroll, root) = createRoot()
        addHeader(root)
        add(root, label("DISPOSITIVOS", 22f, silver, true), bottom = 8)
        add(root, label("Conecte um aparelho USB compatível.", 12f, muted), bottom = 20)

        val info = card()
        add(info, label("NENHUM DISPOSITIVO DETECTADO", 13f, silver, true), bottom = 10)
        add(info, label(
            "A versão atual ainda não possui detecção USB/ADB ativa. " +
                "Conecte o aparelho com um cabo de dados e habilite a depuração USB " +
                "nas opções de desenvolvedor para a futura integração.",
            12f, muted
        ))
        add(root, info, bottom = 18)

        val back = makeButton("VOLTAR AO PAINEL", true)
        add(root, back)
        back.setOnClickListener { showDashboard() }
        setContentView(scroll)
    }

    private fun showTools() {
        val (scroll, root) = createRoot()
        addHeader(root)
        add(root, label("FERRAMENTAS", 22f, silver, true), bottom = 8)
        add(root, label("Diagnóstico e manutenção", 12f, muted), bottom = 20)

        val diagnostic = card()
        add(diagnostic, label("TRIAGEM DE INCIDENTES", 13f, silver, true), bottom = 10)
        add(diagnostic, label(
            "Informe um código de incidente conhecido. A triagem é local e demonstrativa; ela não detecta falhas reais nem altera o aparelho.",
            12f, muted
        ), bottom = 12)

        val incidentCode = inputField("Código (ex.: APP_CRASH_LOOP)", InputType.TYPE_CLASS_TEXT)
        add(diagnostic, incidentCode, bottom = 10)

        val result = label(
            "Aguardando análise. Códigos desconhecidos ficam para revisão do ADM.",
            12f, muted
        )
        result.gravity = Gravity.START
        add(diagnostic, result, bottom = 12)

        val analyze = makeButton("ANALISAR INCIDENTE", true)
        add(diagnostic, analyze)
        analyze.setOnClickListener {
            val code = incidentCode.text.toString().trim()
            if (code.isBlank()) {
                incidentCode.error = "Informe um código"
                incidentCode.requestFocus()
            } else {
                val severity = PolicyIncidentSeverityPolicy.classify(code)
                val decision = PolicyIncidentSeverityPolicy.decisionForCode(code)
                val severityText = severity?.name ?: "UNKNOWN"
                val decisionText = when (decision) {
                    MaintenanceDecision.AUTOMATIC_SAFE_MAINTENANCE ->
                        "MANUTENÇÃO SEGURA AUTOMÁTICA (somente estado preventivo do app)"
                    MaintenanceDecision.ADMIN_REVIEW_REQUIRED ->
                        "AGUARDAR DECISÃO DO ADM"
                    MaintenanceDecision.NO_ACTION ->
                        "NENHUMA AÇÃO"
                }
                result.text = "Código: ${code.uppercase(java.util.Locale.ROOT)}\nGravidade: $severityText\nDecisão: $decisionText\n\nNenhuma restauração ou apagamento será executado."
            }
        }
        add(root, diagnostic, bottom = 18)

        val note = card()
        add(note, label("LIMITES DE SEGURANÇA", 12f, silver, true), bottom = 8)
        add(note, label(
            "Falhas graves conhecidas são sinalizadas para manutenção preventiva segura. Falhas leves, médias ou desconhecidas aguardam o ADM. Esta versão ainda não tem monitoramento real do aparelho nem backend administrativo.",
            12f, muted
        ))
        add(root, note, bottom = 18)

        val back = makeButton("VOLTAR AO PAINEL", true)
        add(root, back)
        back.setOnClickListener { showDashboard() }
        setContentView(scroll)
    }

    private fun showSettings() {
        val (scroll, root) = createRoot()
        root.setPadding(dp(18), dp(18), dp(18), dp(18))
        addHeader(root)
        add(root, label("⚙", 42f, silver, true), bottom = 6)
        add(root, label("CONFIGURAÇÕES", 22f, silver, true), bottom = 6)
        add(root, label("Personalize sua experiência no Enterex.", 12f, muted), bottom = 22)

        val appearance = card()
        add(appearance, label("APARÊNCIA", 12f, silver, true), bottom = 8)
        val themeRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val themeCopy = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        add(themeCopy, label("Modo escuro", 15f, silver, true).apply { gravity = Gravity.START }, bottom = 4)
        add(themeCopy, label("Alternar entre tema escuro e claro.", 12f, muted).apply { gravity = Gravity.START })
        themeRow.addView(themeCopy, LinearLayout.LayoutParams(0, -2, 1f))
        val themeToggle = CheckBox(this).apply {
            text = if (darkMode) "ON" else "OFF"
            isChecked = darkMode
            textSize = 12f
            setTextColor(silver)
            buttonTintList = android.content.res.ColorStateList.valueOf(accent)
            setOnCheckedChangeListener { _, checked ->
                darkMode = checked
                text = if (checked) "ON" else "OFF"
                getSharedPreferences("enterex_settings", MODE_PRIVATE).edit()
                    .putBoolean("dark_mode", checked).apply()
                window.statusBarColor = black
                window.navigationBarColor = black
                showSettings()
            }
        }
        themeRow.addView(themeToggle)
        add(appearance, themeRow, bottom = 4)
        add(appearance, label("A preferência fica salva neste dispositivo.", 12f, muted))
        add(root, appearance, bottom = 14)

        val privacy = card()
        add(privacy, label("PRIVACIDADE E AUTORIZAÇÃO", 12f, silver, true), bottom = 8)
        add(privacy, label("Termo de ciência", 15f, silver, true), bottom = 4)
        add(privacy, label("Revise sua escolha registrada localmente neste dispositivo.", 12f, muted), bottom = 12)
        val reviewTerms = makeButton("REVISAR TERMO", false)
        add(privacy, reviewTerms)
        reviewTerms.setOnClickListener {
            val key = normalizedEmail(currentAccount.optString("email", ""))
            if (key.isBlank()) {
                Toast.makeText(this, "Entre na sua conta para revisar o termo.", Toast.LENGTH_SHORT).show()
            } else {
                authorizations.edit().remove("consent_$key").apply()
                showAuthorizationNotice(key)
            }
        }
        add(root, privacy, bottom = 14)

        val system = card()
        add(system, label("SISTEMA", 12f, silver, true), bottom = 8)
        add(system, label("ENTEREX DEVICE MANAGER", 14f, silver, true), bottom = 4)
        add(system, label("Ferramentas USB/ADB e restauração ainda não estão implementadas nesta versão.", 12f, muted))
        add(root, system, bottom = 20)

        val back = makeButton("VOLTAR AO PAINEL", true)
        add(root, back)
        back.setOnClickListener { showDashboard() }
        add(root, label("ENTEREX SYSTEMS • KYODAI 4CHAN", 8f, muted), top = 18)
        setContentView(scroll)
    }

    private fun showProfile() {
        val (scroll, root) = createRoot()
        addHeader(root)
        add(root, label("MEU PERFIL", 22f, silver, true), bottom = 18)
        val profile = card()
        val avatar = ImageView(this).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            background = shape(Color.rgb(24, 27, 33), line, 48)
            clipToOutline = true
            contentDescription = "Foto de perfil"
        }
        val photoUri = currentAccount.optString("photoUri", "")
        if (photoUri.isNotBlank()) {
            try { avatar.setImageURI(Uri.parse(photoUri)) } catch (_: Exception) {}
        } else {
            avatar.setImageDrawable(android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.OVAL
                setColor(Color.rgb(24, 27, 33))
                setStroke(dp(1), line)
            })
        }
        profileImage = avatar
        profile.addView(avatar, LinearLayout.LayoutParams(dp(96), dp(96)).apply { gravity = Gravity.CENTER; bottomMargin = dp(12) })
        val changePhoto = makeButton("ALTERAR FOTO", false)
        add(profile, changePhoto, bottom = 16)
        changePhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "image/*"
                addCategory(Intent.CATEGORY_OPENABLE)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            }
            startActivityForResult(intent, 1001)
        }
        add(profile, label(currentAccount.optString("nickname", "Usuário"), 20f, silver, true), bottom = 8)
        add(profile, label("Idade: " + currentAccount.optInt("age", 0), 12f, muted), bottom = 8)
        val bio = currentAccount.optString("bio", "").ifBlank { "Nenhuma biografia informada." }
        add(profile, label(bio, 12f, muted))
        add(root, profile, bottom = 18)
        val planCard = card()
        add(planCard, label("STATUS DO PLANO", 12f, silver, true), bottom = 8)
        add(planCard, label(currentAccount.optString("plan", "Plano gratuito"), 16f, silver, true), bottom = 5)
        add(planCard, label("O plano e seus recursos aparecerão aqui quando a ativação estiver integrada.", 11f, muted))
        add(root, planCard, bottom = 16)
        val privacy = card()
        add(privacy, label("CONTA E PRIVACIDADE", 12f, silver, true), bottom = 8)
        add(privacy, label("Nesta versão, conta e foto ficam salvas localmente neste dispositivo. Sincronização e armazenamento privado na nuvem ainda não estão disponíveis.", 11f, muted), bottom = 12)
        val logout = makeButton("SAIR DA CONTA", false)
        add(privacy, logout)
        logout.setOnClickListener { currentAccount = JSONObject(); showLogin() }
        add(root, privacy, bottom = 18)
        val back = makeButton("VOLTAR AO PAINEL", true)
        add(root, back)
        back.setOnClickListener { showDashboard() }
        setContentView(scroll)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            val uri = data?.data ?: return
            selectedPhoto = uri
            try {
                contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {
                // Se o provedor não oferecer permissão persistente, mantém a seleção da sessão.
            }
            try {
                profileImage?.setImageURI(uri)
            } catch (_: Exception) {
                Toast.makeText(this, "Não foi possível carregar essa imagem.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
