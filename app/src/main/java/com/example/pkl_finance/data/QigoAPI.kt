package com.example.pkl_finance.data

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface PklFinanceApi {
    
    // Get all merchants
    @GET("api/merchants")
    suspend fun getMerchants(): List<MerchantResponse>

    // Get specific merchant profile by ID
    @GET("api/merchants/{id}")
    suspend fun getMerchantById(
        @Path("id") id: Int
    ): MerchantResponse

    // Get balances by merchant ID
    @GET("api/balances/{merchantId}")
    suspend fun getBalance(
        @Path("merchantId") merchantId: Int
    ): BalanceResponse

    // Get transaction history by merchant ID
    @GET("api/transactions/{merchantId}")
    suspend fun getTransactions(
        @Path("merchantId") merchantId: Int
    ): List<TransactionResponse>

    // Get active installment configuration by merchant ID
    @GET("api/installments/{merchantId}")
    suspend fun getActiveInstallment(
        @Path("merchantId") merchantId: Int
    ): InstallmentResponse?

    // Configure a new gold installment autosplit setup
    @POST("api/installments")
    suspend fun createInstallment(
        @Body request: InstallmentRequest
    ): InstallmentResponse

    // Simulate incoming QRIS payment
    @POST("api/transactions/simulate-qris")
    suspend fun simulateQrisPayment(
        @Body request: SimulateQrisRequest
    ): Map<String, Any>

    // Submit cash withdrawal checkout
    @POST("api/transactions/withdraw")
    suspend fun withdrawBalance(
        @Body request: WithdrawRequest
    ): TransactionResponse

    // Submit digital gold purchase checkout
    @POST("api/transactions/buy-gold")
    suspend fun buyGold(
        @Body request: BuyGoldRequest
    ): TransactionResponse

    // Register a new merchant
    @POST("api/auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): AuthResponse

    // Login merchant
    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): AuthResponse

    // Verify KYC data
    @PUT("api/merchants/verify")
    suspend fun verifyKyc(
        @Body request: VerifyRequest
    ): AuthResponse

    // Update bank account for disbursement
    @PUT("api/merchants/bank-account")
    suspend fun updateBankAccount(
        @Body request: UpdateBankAccountRequest
    ): AuthResponse

    // Get current gold price from database
    @GET("api/gold-price")
    suspend fun getGoldPrice(): GoldPriceResponse

    // ─── Midtrans QRIS ─────────────────────────────────────────────
    // Buat transaksi QRIS baru via Midtrans Sandbox
    @POST("api/qris/create")
    suspend fun createQrisPayment(
        @Body request: CreateQrisRequest
    ): CreateQrisResponse

    // Cek status pembayaran QRIS berdasarkan orderId
    @GET("api/qris/status/{orderId}")
    suspend fun checkQrisStatus(
        @Path("orderId") orderId: String
    ): QrisStatusResponse
}

data class GoldPriceResponse(
    val price: Double,
    val change: Double = 0.0,
    val percent: Double = 0.0,
    val trend: String = "up",
    val updated_at: String? = null
)

// Auth Data Classes
data class RegisterRequest(
    val username: String,
    val password: String,
    val ownerName: String,
    val shopName: String,
    val businessType: String
)

data class LoginRequest(
    val username: String,
    val password: String
)

data class VerifyRequest(
    val merchantId: Int,
    val fullName: String,
    val nik: String,
    val shopAddress: String
    // In a real app, photos would be sent via MultipartFormData
)

data class AuthResponse(
    val success: Boolean,
    val message: String,
    val merchant: MerchantResponse?,
    val token: String?
)
