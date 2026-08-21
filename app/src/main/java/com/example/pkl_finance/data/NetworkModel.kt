package com.example.pkl_finance.data

import com.google.gson.annotations.SerializedName

// Merchant Response
data class MerchantResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("username") val username: String,
    @SerializedName("owner_name") val owner_name: String,
    @SerializedName("shop_name") val shop_name: String,
    @SerializedName("shop_address") val shop_address: String,
    @SerializedName("business_type") val business_type: String,
    @SerializedName("is_verified") val isVerified: Boolean,
    @SerializedName("is_umi") val isUmi: Boolean,
    @SerializedName("bank_name") val bankName: String?,
    @SerializedName("bank_account_number") val bankAccountNumber: String?,
    @SerializedName("bank_account_name") val bankAccountName: String?
)

// Balance Response
data class BalanceResponse(
    @SerializedName("merchant_id") val merchantId: Int,
    @SerializedName("main_balance") val mainBalance: Double,
    @SerializedName("gold_balance") val goldBalance: Double
)

// Transaction Response
data class TransactionResponse(
    @SerializedName("id") val id: String,
    @SerializedName("merchant_id") val merchantId: Int,
    @SerializedName("type") val type: String,
    @SerializedName("title") val title: String,
    @SerializedName("total_amount") val totalAmount: Double,
    @SerializedName("main_amount") val mainAmount: Double,
    @SerializedName("gold_amount") val goldAmount: Double,
    @SerializedName("gold_weight_added") val goldWeightAdded: Double,
    @SerializedName("mdr_fee") val mdrFee: Double,
    @SerializedName("created_at") val createdAt: String?
)

// Installment Response
data class InstallmentResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("merchant_id") val merchantId: Int,
    @SerializedName("target_weight") val targetWeight: Double,
    @SerializedName("total_installment_amount") val totalInstallmentAmount: Double,
    @SerializedName("service_fee") val serviceFee: Double,
    @SerializedName("split_percentage") val splitPercentage: Int,
    @SerializedName("accumulated_amount") val accumulatedAmount: Double,
    @SerializedName("accumulated_gold_weight") val accumulatedGoldWeight: Double,
    @SerializedName("is_active") val isActive: Boolean
)

// Requests
data class SimulateQrisRequest(
    @SerializedName("merchantId") val merchantId: Int,
    @SerializedName("amount") val amount: Double
)

data class InstallmentRequest(
    @SerializedName("merchantId") val merchantId: Int,
    @SerializedName("targetWeight") val targetWeight: Double,
    @SerializedName("totalInstallmentAmount") val totalInstallmentAmount: Double,
    @SerializedName("serviceFee") val serviceFee: Double,
    @SerializedName("splitPercentage") val splitPercentage: Int
)

data class WithdrawRequest(
    @SerializedName("merchantId") val merchantId: Int,
    @SerializedName("amount") val amount: Double
)

data class BuyGoldRequest(
    @SerializedName("merchantId") val merchantId: Int,
    @SerializedName("amount") val amount: Double
)

data class UpdateBankAccountRequest(
    @SerializedName("merchantId") val merchantId: Int,
    @SerializedName("bankName") val bankName: String,
    @SerializedName("bankAccountNumber") val bankAccountNumber: String,
    @SerializedName("bankAccountName") val bankAccountName: String
)

// ─── Midtrans QRIS ─────────────────────────────────────────────
data class CreateQrisRequest(
    @SerializedName("merchantId") val merchantId: Int,
    @SerializedName("amount") val amount: Double,
    @SerializedName("shopName") val shopName: String
)

data class CreateQrisResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("orderId") val orderId: String,
    @SerializedName("transactionId") val transactionId: String?,
    @SerializedName("qrImageUrl") val qrImageUrl: String?,
    @SerializedName("qrString") val qrString: String?,
    @SerializedName("expiryTime") val expiryTime: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("error") val error: String?
)

data class QrisStatusResponse(
    @SerializedName("status") val status: String,         // "pending", "paid", "expired"
    @SerializedName("midtransStatus") val midtransStatus: String?,
    @SerializedName("transaction") val transaction: Any?  // raw data dari Supabase RPC
)
