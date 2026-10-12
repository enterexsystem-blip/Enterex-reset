
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

    private val black = Color.rgb(5, 6, 8)
    private val panel = Color.rgb(5, 6, 8)
    private val silver = Color.rgb(225, 229, 235)
    private val muted = Color.rgb(135, 143, 153)
    private val line = Color.rgb(235, 238, 242)
    private val accent = Color.rgb(180, 190, 205)

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
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            background = shape(black, Color.WHITE, 10)
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
        val message = """
            TERMO DE CIÊNCIA, AUTORIZAÇÃO E RESPONSABILIDADE — ENTEREX

            1. DISPOSIÇÕES GERAIS. O acesso ao painel do aplicativo Enterex está condicionado à leitura e à manifestação expressa de concordância com este termo.

            2. REGISTRO DA ESCOLHA. A opção selecionada será armazenada localmente neste dispositivo, associada à conta utilizada, para evitar a exibição repetida deste aviso enquanto o respectivo registro permanecer disponível no aplicativo.

            3. LIMITES DA AUTORIZAÇÃO. A concordância limita-se ao registro de ciência deste termo e à liberação de acesso ao painel. Não constitui, por si só, concessão de permissões do sistema Android, autorização de depuração USB, acesso à conta Google, vínculo com serviços de terceiros ou autorização técnica para controlar, restaurar ou apagar qualquer dispositivo.

            4. PERMISSÕES E COMPATIBILIDADE. Cada recurso dependerá de implementação efetiva, compatibilidade técnica, permissões específicas exigidas pelo sistema operacional e, quando aplicável, dos procedimentos oficiais do fabricante ou prestador do serviço. Este termo não substitui essas etapas.

            5. RISCO DE PERDA DE DADOS. Procedimentos de restauração ou apagamento, quando futuramente disponíveis e devidamente autorizados, poderão eliminar dados de forma permanente. Tais procedimentos deverão ser realizados exclusivamente em dispositivos de propriedade do usuário ou mediante autorização legítima do respectivo proprietário.

            6. MANIFESTAÇÃO DE VONTADE. A seleção de “Aceitar” indica ciência e concordância com as condições acima. A seleção de “Recusar” impede o acesso ao painel neste momento. A escolha poderá ser alterada apenas mediante os meios disponibilizados pelo aplicativo.

            Ao prosseguir, confirme uma das opções abaixo.
        """.trimIndent()

        val options = arrayOf(
            "ACEITO os termos acima e desejo continuar para o painel.",
            "RECUSO os termos e desejo retornar à tela de acesso."
        )
        var selectedOption = -1

        val dialog = AlertDialog.Builder(this)
            .setTitle("TERMO DE CIÊNCIA E AUTORIZAÇÃO")
            .setMessage(message)
            .setSingleChoiceItems(options, -1) { _, which ->
                selectedOption = which
            }
            .setPositiveButton("CONFIRMAR ESCOLHA", null)
            .setCancelable(false)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                when (selectedOption) {
                    0 -> {
                        authorizations.edit()
                            .putBoolean("consent_$accountKey", true)
                            .apply()
                        dialog.dismiss()
                        showCreatorIntro()
                    }
                    1 -> {
                        currentAccount = JSONObject()
                        dialog.dismiss()
                        Toast.makeText(
                            this,
                            "A opção foi recusada. O painel permanecerá bloqueado.",
                            Toast.LENGTH_LONG
                        ).show()
                        showLogin()
                    }
                    else -> Toast.makeText(
                        this,
                        "Marque uma das opções antes de confirmar.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        dialog.show()
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
                put("email", key)
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
                    val account = checkLogin(emailValue, passwordValue)
                    if (account == null) {
                        Toast.makeText(
                            this, "E-mail ou senha incorretos.",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        continueAfterLogin(account)
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
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
            }
            @Suppress("DEPRECATION")
            startActivityForResult(
                Intent.createChooser(intent, "Escolha sua foto"),
                1001
            )
        }

        add(form, label("APELIDO", 10f, muted, true), bottom = 7)
        val nickname = inputField(
            "Como quer ser chamado?",
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS
        )
        add(form, nickname, bottom = 14)

        add(form, label("IDADE", 10f, muted, true), bottom = 7)
        val age = inputField("Sua idade", InputType.TYPE_CLASS_NUMBER)
        add(form, age, bottom = 14)

        add(form, label("BIOGRAFIA (OPCIONAL)", 10f, muted, true), bottom = 7)
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
        add(form, bio, bottom = 14)

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
            val bioText = bio.text.toString().trim()
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
                    val saved = createAccount(
                        name, parsedAge!!, bioText, emailText, pass
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

        val nickname = currentAccount.optString("nickname", "Usuário")
            .ifBlank { "Usuário" }

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

        val profile = TextView(this).apply {
            text = "◉"
            textSize = 25f
            setTextColor(silver)
            setPadding(dp(12), dp(8), dp(4), dp(8))
            setOnClickListener { showProfile() }
        }
        header.addView(profile)
        add(root, header, bottom = 28)

        add(root, label("PAINEL DE CONTROLE", 10f, muted, true),
            bottom = 8)
        add(root, label("Olá, $nickname 👋", 27f, silver, true),
            bottom = 5)
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
                        else -> "Encerrar sessão"
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
            Triple("↪", "Sair da conta", {
                currentAccount = JSONObject()
                showLogin()
            })
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

        val logout = makeButton("SAIR DA CONTA", false)
        add(root, logout, bottom = 12)
        logout.setOnClickListener {
            currentAccount = JSONObject()
            showLogin()
        }

        add(root, label("ENTEREX SYSTEMS", 8f, muted), top = 10)
        setContentView(scroll)
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
        add(diagnostic, label("DIAGNÓSTICO DO APARELHO", 13f, silver, true), bottom = 10)
        add(diagnostic, label(
            "As ferramentas serão habilitadas quando um dispositivo compatível " +
                "for detectado. A restauração deve ser executada somente em aparelhos " +
                "seus ou com autorização do proprietário.",
            12f, muted
        ))
        add(root, diagnostic, bottom = 18)

        val back = makeButton("VOLTAR AO PAINEL", true)
        add(root, back)
        back.setOnClickListener { showDashboard() }
        setContentView(scroll)
    }

    private fun showProfile() {
        val (scroll, root) = createRoot()
        addHeader(root)
        add(root, label("MEU PERFIL", 22f, silver, true), bottom = 18)

        val profile = card()
        add(profile, label("◉", 42f, silver), bottom = 10)
        add(profile, label(currentAccount.optString("nickname", "Usuário"), 20f, silver, true), bottom = 8)
        add(profile, label("Idade: " + currentAccount.optInt("age", 0), 12f, muted), bottom = 8)
        val bio = currentAccount.optString("bio", "").ifBlank { "Nenhuma biografia informada." }
        add(profile, label(bio, 12f, muted))
        add(root, profile, bottom = 18)

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
                // Alguns seletores não oferecem permissão persistente.
            }
            try {
                profileImage?.setImageURI(uri)
            } catch (_: Exception) {
                Toast.makeText(this, "Não foi possível carregar essa imagem.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
