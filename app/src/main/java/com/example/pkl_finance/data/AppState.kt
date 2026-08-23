package com.example.pkl_finance.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.text.NumberFormat
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class Screen {
    Home,
    Transaksi,
    Qris,
    Emas,
    Profile,
    ConfigCicilEmas,
    RiwayatEmasDetail,
    CairkanMain,
    TarikSaldo,
    LaporanEmas,
    BeliEmas,
    JualEmas,
    CairkanPilihBank,
    CairkanInputRekening,
    CairkanKonfirmasi,
    ReceiptDetail,
    Laporan,
    Login,
    Register,
    VerifikasiKyc
}

data class GoldInstallment(
    val targetWeight: Double, // 0.5 or 1.0
    val totalGoldPrice: Double, // 550000.0 or 1100000.0
    val serviceFee: Double, // 50000.0
    val totalInstallmentAmount: Double, // 600000.0 or 1150000.0
    val splitPercentage: Int, // 10, 15, 20, 30, 35
    var accumulatedAmount: Double = 0.0,
    var accumulatedGoldWeight: Double = 0.0
) {
    val remainingAmount: Double
        get() = (totalInstallmentAmount - accumulatedAmount).coerceAtLeast(0.0)

    val progress: Float
        get() = (accumulatedAmount / totalInstallmentAmount).toFloat().coerceIn(0f, 1f)
}

data class Transaction(
    val id: String,
    val type: String, // "QRIS_IN", "GOLD_BUY", "WITHDRAW"
    val title: String,
    val date: String,
    val totalAmount: Double,
    val mainAmount: Double, // amount added to balance
    val goldAmount: Double, // amount used for gold installment
    val goldWeightAdded: Double = 0.0, // gold weight equivalent added
    val mdrFee: Double = 0.0
)

class AppState {
    // Auth State
    var isLoggedIn by mutableStateOf(false)
    var authToken by mutableStateOf<String?>(null)

    // Current Login Info (Overrides default mock)
    var merchantInfo by mutableStateOf<MerchantResponse?>(null)

    // Current Active Merchant ID
    var merchantId by mutableStateOf(-1)

    // Navigation state
    var currentScreen by mutableStateOf(Screen.Login)
    var previousScreen by mutableStateOf(Screen.Login)
    var isBottomBarVisible by mutableStateOf(true)

    // Balances & Rates
    var mainBalance by mutableStateOf(0.0)
    var goldBalance by mutableStateOf(0.0)
    var goldPriceRate by mutableStateOf(2725000.0) // Harga Beli Antam per Gram
    var goldBuyPrice by mutableStateOf(2725000.0)
    var goldSellPrice by mutableStateOf(2585000.0) // Harga Buyback Antam per Gram (Selisih Rp 140.000)
    var goldPriceChange by mutableStateOf(0.0)
    var goldPricePercent by mutableStateOf(0.0)
    var goldPriceTrend by mutableStateOf("up")
    var goldPriceHistory by mutableStateOf<List<GoldPricePoint>>(emptyList())

    // MDR & Volume status
    var isUMI by mutableStateOf(false)
    var annualQrisVolume by mutableStateOf(0.0)

    // Verification Info
    var isVerified by mutableStateOf(false)
    var ownerName by mutableStateOf("")
    var shopName by mutableStateOf("")
    var shopAddress by mutableStateOf("")
    var businessType by mutableStateOf("")
    var ktpNumber by mutableStateOf("")
    var phoneNumber by mutableStateOf("")
    var email by mutableStateOf("")
 
    // Disbursement bank account info
    var disbursementBankName by mutableStateOf("BCA")
    var disbursementAccountNumber by mutableStateOf("1234561711")
    var disbursementAccountName by mutableStateOf("Rara Zulaikha Indah")
 
    // Temporary variables for bank change draft flow
    var tempBankName by mutableStateOf("")
    var tempAccountNumber by mutableStateOf("")
    var tempAccountName by mutableStateOf("")

    // Active Gold Installment
    var activeInstallment by mutableStateOf<GoldInstallment?>(null)

    // Active transaction receipt selected from history
    var selectedTransactionReceipt by mutableStateOf<Transaction?>(null)

    // Transaction History (Pre-populate with 5 items)
    var transactionHistory by mutableStateOf(emptyList<Transaction>())

    // Completion Dialog trigger
    var showCompletionDialog by mutableStateOf(false)
    var completedInstallmentWeight by mutableStateOf(0.5)

    // Capture background refresh errors to surface to UI
    var refreshError by mutableStateOf<String?>(null)

    // Helper to format ISO timestamp from Supabase into indonesian readable date
    private fun formatIsoToReadable(isoString: String?): String {
        if (isoString.isNullOrEmpty()) return ""
        return try {
            val cleaned = isoString.substringBefore(".").substringBefore("Z")
            val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            val date = inputFormat.parse(cleaned) ?: return isoString
            val outputFormat = java.text.SimpleDateFormat("dd MMM yyyy HH:mm", Locale("id", "ID"))
            outputFormat.format(date) + " WIB"
        } catch (e: Exception) {
            isoString
        }
    }

    // Refresh app data from backend Supabase REST APIs
    fun checkInitialAuth() {
        isLoggedIn = false
        authToken = null
        RetrofitClient.authToken = null
    }

    fun logout() {
        isLoggedIn = false
        authToken = null
        RetrofitClient.authToken = null
        merchantInfo = null
        mainBalance = 0.0
        goldBalance = 0.0
        transactionHistory = emptyList()
        activeInstallment = null
        navigateTo(Screen.Login)
    }

    private fun handleNetworkError(e: Exception, defaultMsg: String): String {
        if (e is retrofit2.HttpException) {
            val code = e.code()
            val errorBodyMsg = try {
                val errorJson = e.response()?.errorBody()?.string()
                if (!errorJson.isNullOrEmpty()) {
                    val gson = com.google.gson.Gson()
                    val jsonObj = gson.fromJson(errorJson, com.google.gson.JsonObject::class.java)
                    if (jsonObj.has("message")) jsonObj.get("message").asString else null
                } else null
            } catch (_: Exception) {
                null
            }

            if (!errorBodyMsg.isNullOrEmpty()) {
                return errorBodyMsg
            }

            return when (code) {
                401 -> "Username atau password salah"
                400 -> "Data yang dimasukkan tidak valid"
                403 -> "Akses ditolak"
                404 -> "Data tidak ditemukan"
                409 -> "Username atau data sudah terdaftar"
                500, 502, 503 -> "Terjadi kesalahan pada server"
                else -> defaultMsg
            }
        }
        return "Terjadi kesalahan jaringan"
    }

    fun login(request: LoginRequest, onSuccess: () -> Unit, onError: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.api.login(request)
                if (response.success && response.merchant != null && response.token != null) {
                    withContext(Dispatchers.Main) {
                        isLoggedIn = true
                        authToken = response.token
                        RetrofitClient.authToken = response.token
                        merchantInfo = response.merchant
                        merchantId = response.merchant.id
                        ownerName = response.merchant.owner_name
                        shopName = response.merchant.shop_name
                        isVerified = response.merchant.isVerified
                        onSuccess()
                        refreshData()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onError(response.message ?: "Username atau password salah")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(handleNetworkError(e, "Username atau password salah"))
                }
            }
        }
    }

    fun register(request: RegisterRequest, onSuccess: () -> Unit, onError: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.api.register(request)
                if (response.success) {
                    withContext(Dispatchers.Main) {
                        onSuccess()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onError(response.message ?: "Registrasi gagal")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(handleNetworkError(e, "Registrasi gagal"))
                }
            }
        }
    }

    fun verifyKyc(request: VerifyRequest, onSuccess: () -> Unit, onError: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.api.verifyKyc(request)
                if (response.success && response.merchant != null) {
                    withContext(Dispatchers.Main) {
                        merchantInfo = response.merchant
                        ownerName = response.merchant.owner_name
                        isVerified = response.merchant.isVerified
                        onSuccess()
                        refreshData()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onError(response.message ?: "Verifikasi gagal")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(handleNetworkError(e, "Verifikasi gagal"))
                }
            }
        }
    }

    fun refreshData() {
        if (!isLoggedIn || merchantId == -1) return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val merchant = RetrofitClient.api.getMerchantById(merchantId)
                val balance = RetrofitClient.api.getBalance(merchantId)
                val txList = RetrofitClient.api.getTransactions(merchantId)
                val activeInst = try { RetrofitClient.api.getActiveInstallment(merchantId) } catch (e: Exception) { null }
                val goldPriceResp = try { RetrofitClient.api.getGoldPrice() } catch (e: Exception) { null }
                val goldHistoryResp = try { RetrofitClient.api.getGoldPriceHistory(6) } catch (e: Exception) { null }

                withContext(Dispatchers.Main) {
                    ownerName = merchant.owner_name
                    shopName = merchant.shop_name
                    shopAddress = merchant.shop_address
                    businessType = merchant.business_type
                    isVerified = merchant.isVerified
                    isUMI = merchant.isUmi

                    // Load bank account data from DB
                    merchant.bankName?.takeIf { it.isNotEmpty() }?.let { disbursementBankName = it }
                    merchant.bankAccountNumber?.takeIf { it.isNotEmpty() }?.let { disbursementAccountNumber = it }
                    merchant.bankAccountName?.takeIf { it.isNotEmpty() }?.let { disbursementAccountName = it }

                    mainBalance = balance.mainBalance
                    goldBalance = balance.goldBalance
                    goldPriceResp?.let {
                        goldPriceRate = it.price
                        goldBuyPrice = it.buyPrice ?: it.price
                        goldSellPrice = it.sellPrice ?: (it.price * 0.97)
                        goldPriceChange = it.change
                        goldPricePercent = it.percent
                        goldPriceTrend = it.trend
                    }
                    goldHistoryResp?.let {
                        goldPriceHistory = it.history
                    }

                    transactionHistory = txList.map { tx ->
                        Transaction(
                            id = tx.id,
                            type = tx.type,
                            title = tx.title,
                            date = formatIsoToReadable(tx.createdAt),
                            totalAmount = tx.totalAmount,
                            mainAmount = tx.mainAmount,
                            goldAmount = tx.goldAmount,
                            goldWeightAdded = tx.goldWeightAdded,
                            mdrFee = tx.mdrFee
                        )
                    }

                    val wasActive = activeInstallment
                    val newActive = if (activeInst != null && activeInst.isActive) {
                        GoldInstallment(
                            targetWeight = activeInst.targetWeight,
                            totalGoldPrice = activeInst.targetWeight * goldPriceRate,
                            serviceFee = activeInst.serviceFee ?: 0.0,
                            totalInstallmentAmount = activeInst.totalInstallmentAmount,
                            splitPercentage = activeInst.splitPercentage,
                            accumulatedAmount = activeInst.accumulatedAmount,
                            accumulatedGoldWeight = activeInst.accumulatedGoldWeight
                        )
                    } else {
                        null
                    }

                    if (wasActive != null && newActive == null) {
                        completedInstallmentWeight = wasActive.targetWeight
                        showCompletionDialog = true
                    }
                    activeInstallment = newActive
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    refreshError = e.localizedMessage ?: e.message ?: "Unknown refresh error"
                }
            }
        }
    }

    fun navigateTo(screen: Screen) {
        previousScreen = currentScreen
        currentScreen = screen
    }

    fun saveBankAccount(
        bankName: String,
        accountNumber: String,
        accountName: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val req = UpdateBankAccountRequest(
                    merchantId = merchantId,
                    bankName = bankName,
                    bankAccountNumber = accountNumber,
                    bankAccountName = accountName
                )
                val response = RetrofitClient.api.updateBankAccount(req)
                withContext(Dispatchers.Main) {
                    if (response.success) {
                        disbursementBankName = bankName
                        disbursementAccountNumber = accountNumber
                        disbursementAccountName = accountName
                        onSuccess()
                    } else {
                        onError(response.message ?: "Gagal menyimpan rekening")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(handleNetworkError(e, "Gagal menyimpan rekening"))
                }
            }
        }
    }

    fun activateInstallment(weight: Double, splitPct: Int) {
        val price = weight * goldPriceRate
        val fee = 50000.0
        val total = price + fee

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val req = InstallmentRequest(
                    merchantId = merchantId,
                    targetWeight = weight,
                    totalInstallmentAmount = total,
                    serviceFee = fee,
                    splitPercentage = splitPct
                )
                RetrofitClient.api.createInstallment(req)
                refreshData()
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    // Remove offline fallback
                }
            }
        }
    }

    fun withdraw(amount: Double, onCompleted: (Transaction) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val req = WithdrawRequest(merchantId = merchantId, amount = amount)
                val response = RetrofitClient.api.withdrawBalance(req)
                refreshData()

                val formattedTx = Transaction(
                    id = response.id,
                    type = response.type,
                    title = response.title,
                    date = formatIsoToReadable(response.createdAt),
                    totalAmount = response.totalAmount,
                    mainAmount = response.mainAmount,
                    goldAmount = response.goldAmount,
                    goldWeightAdded = response.goldWeightAdded,
                    mdrFee = response.mdrFee
                )

                withContext(Dispatchers.Main) {
                    onCompleted(formattedTx)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    // Remove offline fallback
                }
            }
        }
    }

    fun buyGold(amount: Double, onError: ((String) -> Unit)? = null, onCompleted: (Transaction) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val req = BuyGoldRequest(merchantId = merchantId, amount = amount)
                val response = RetrofitClient.api.buyGold(req)
                refreshData()

                val formattedTx = Transaction(
                    id = response.id,
                    type = response.type,
                    title = response.title,
                    date = formatIsoToReadable(response.createdAt),
                    totalAmount = response.totalAmount,
                    mainAmount = response.mainAmount,
                    goldAmount = response.goldAmount,
                    goldWeightAdded = response.goldWeightAdded,
                    mdrFee = response.mdrFee
                )

                withContext(Dispatchers.Main) {
                    onCompleted(formattedTx)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError?.invoke(handleNetworkError(e, "Gagal melakukan pembelian emas"))
                }
            }
        }
    }

    fun sellGold(goldWeight: Double, onError: ((String) -> Unit)? = null, onCompleted: (Transaction) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val req = SellGoldRequest(merchantId = merchantId, goldWeight = goldWeight)
                val response = RetrofitClient.api.sellGold(req)
                refreshData()

                val formattedTx = Transaction(
                    id = response.id,
                    type = response.type,
                    title = response.title,
                    date = formatIsoToReadable(response.createdAt),
                    totalAmount = response.totalAmount,
                    mainAmount = response.mainAmount,
                    goldAmount = response.goldAmount,
                    goldWeightAdded = response.goldWeightAdded,
                    mdrFee = response.mdrFee
                )

                withContext(Dispatchers.Main) {
                    onCompleted(formattedTx)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError?.invoke(handleNetworkError(e, "Gagal melakukan penjualan emas"))
                }
            }
        }
    }

    fun submitVerification(owner: String, shop: String, type: String, ktp: String) {
        ownerName = owner
        shopName = shop.ifEmpty { "Toko Anda" }
        shopAddress = "Jl. Utama No. 1, Sragen"
        businessType = type
        ktpNumber = ktp
        isVerified = true
    }

    fun generateTransactionId(type: String): String {
        val prefix = when (type) {
            "GOLD_BUY" -> "BE"
            "GOLD_SELL" -> "JE"
            "AUTOSPLIT_GOLD" -> "CE"
            "QRIS_IN" -> "QS"
            "WITHDRAW" -> "TD"
            "GOLD_PRINT" -> "CG"
            else -> "TX"
        }
        val year = java.text.SimpleDateFormat("yy", Locale.US).format(java.util.Date())
        val random6 = String.format(Locale.US, "%06d", (0..999999).random())
        return "$prefix$year${random6}QG"
    }

    fun simulateIncomingQrisPayment(totalAmount: Double, onResult: (Transaction) -> Unit, onError: ((String) -> Unit)? = null) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val req = SimulateQrisRequest(
                    merchantId = merchantId,
                    amount = totalAmount
                )
                val response = RetrofitClient.api.simulateQrisPayment(req)
                refreshData()

                val txMap = response["transaction"] as? Map<*, *>
                val formattedTx = if (txMap != null) {
                    val id = txMap["id"] as? String ?: ""
                    val type = txMap["type"] as? String ?: "QRIS_IN"
                    val title = txMap["title"] as? String ?: ""
                    val total = (txMap["total_amount"] as? Number)?.toDouble() ?: totalAmount
                    val main = (txMap["main_amount"] as? Number)?.toDouble() ?: totalAmount
                    val gold = (txMap["gold_amount"] as? Number)?.toDouble() ?: 0.0
                    val weight = (txMap["gold_weight_added"] as? Number)?.toDouble() ?: 0.0
                    val fee = (txMap["mdr_fee"] as? Number)?.toDouble() ?: 0.0
                    val date = formatIsoToReadable(txMap["created_at"] as? String)

                    Transaction(
                        id = id,
                        type = type,
                        title = title,
                        date = date,
                        totalAmount = total,
                        mainAmount = main,
                        goldAmount = gold,
                        goldWeightAdded = weight,
                        mdrFee = fee
                    )
                } else {
                    Transaction(
                        id = "SIM-${(1000..9999).random()}",
                        type = "QRIS_IN",
                        title = "Pembayaran QRIS",
                        date = "Sekarang",
                        totalAmount = totalAmount,
                        mainAmount = totalAmount,
                        goldAmount = 0.0,
                        goldWeightAdded = 0.0
                    )
                }

                withContext(Dispatchers.Main) {
                    onResult(formattedTx)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onError?.invoke(e.localizedMessage ?: e.message ?: "Unknown network error")
                }
                // Return local fallback on failure
                val txId = generateTransactionId("QRIS_IN")
                val dateStr = java.text.SimpleDateFormat("dd MMM yyyy HH:mm", Locale("id", "ID")).format(java.util.Date()) + " WIB"
                val fallbackTx = Transaction(
                    id = txId,
                    type = "QRIS_IN",
                    title = "Pembayaran QRIS - ${if(shopName.isNotEmpty()) shopName else "Toko Anda"}",
                    date = dateStr,
                    totalAmount = totalAmount,
                    mainAmount = totalAmount,
                    goldAmount = 0.0,
                    goldWeightAdded = 0.0
                )
                withContext(Dispatchers.Main) {
                    mainBalance += totalAmount
                    transactionHistory = listOf(fallbackTx) + transactionHistory
                    onResult(fallbackTx)
                }
            }
        }
    }

    // ─── Midtrans QRIS: Buat QR baru ──────────────────────────────
    fun createMidtransQris(
        amount: Double,
        onReady: (orderId: String, qrImageUrl: String) -> Unit,
        onError: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val req = CreateQrisRequest(
                    merchantId = merchantId,
                    amount = amount,
                    shopName = shopName
                )
                val response = RetrofitClient.api.createQrisPayment(req)

                val url = response.qrImageUrl
                if (response.success && !url.isNullOrEmpty()) {
                    withContext(Dispatchers.Main) {
                        onReady(response.orderId, url)
                    }
                } else {
                    val errMsg = response.error ?: "Gagal mendapatkan URL QR dari Midtrans"
                    withContext(Dispatchers.Main) { onError(errMsg) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onError(handleNetworkError(e, "Gagal membuat QRIS Midtrans"))
                }
            }
        }
    }

    // ─── Midtrans QRIS: Polling status pembayaran ──────────────────
    // Dipanggil dari coroutine di UI. Polling tiap 3 detik, max 5 menit.
    // Return true jika paid, false jika expired/timeout.
    suspend fun pollQrisStatus(
        orderId: String,
        onPaid: (Transaction?) -> Unit,
        onExpired: () -> Unit
    ) {
        val maxAttempts = 100  // 100 x 3 detik = 5 menit
        var attempts = 0

        while (attempts < maxAttempts) {
            attempts++
            kotlinx.coroutines.delay(3000L)

            try {
                val statusResp = withContext(Dispatchers.IO) {
                    RetrofitClient.api.checkQrisStatus(orderId)
                }

                when (statusResp.status) {
                    "paid" -> {
                        refreshData()

                        // Parse transaction dari response jika ada
                        val txMap = statusResp.transaction as? Map<*, *>
                        val tx = if (txMap != null) {
                            val id = txMap["id"] as? String ?: orderId
                            val type = txMap["type"] as? String ?: "QRIS_IN"
                            val title = txMap["title"] as? String ?: "Pembayaran QRIS"
                            val total = (txMap["total_amount"] as? Number)?.toDouble() ?: 0.0
                            val main = (txMap["main_amount"] as? Number)?.toDouble() ?: 0.0
                            val gold = (txMap["gold_amount"] as? Number)?.toDouble() ?: 0.0
                            val weight = (txMap["gold_weight_added"] as? Number)?.toDouble() ?: 0.0
                            val fee = (txMap["mdr_fee"] as? Number)?.toDouble() ?: 0.0
                            val date = formatIsoToReadable(txMap["created_at"] as? String)
                            Transaction(
                                id = id, type = type, title = title, date = date,
                                totalAmount = total, mainAmount = main,
                                goldAmount = gold, goldWeightAdded = weight, mdrFee = fee
                            )
                        } else null

                        withContext(Dispatchers.Main) { onPaid(tx) }
                        return
                    }
                    "expired" -> {
                        withContext(Dispatchers.Main) { onExpired() }
                        return
                    }
                    // "pending" → lanjut polling
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Network error = lanjut coba lagi
            }
        }

        // Timeout 5 menit
        withContext(Dispatchers.Main) { onExpired() }
    }


    fun formatRupiah(amount: Double): String {
        val isNegative = amount < 0
        val absVal = kotlin.math.abs(amount).toLong()
        val formattedNumber = NumberFormat.getNumberInstance(Locale("id", "ID")).format(absVal)
        val prefix = if (isNegative) "-Rp " else "Rp "
        return "$prefix$formattedNumber"
    }
}
