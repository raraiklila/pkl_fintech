package com.example.pkl_finance

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit Test Pengujian Fungsionalitas Logika Bisnis QiGold
 * Menguji kalkulasi potongan MDR, Auto-Split cicilan emas, dan proteksi Auto-Cap.
 */
class QrisAutoSplitUnitTest {

    private val mdrLimit = 500000.0
    private val mdrUmiRate = 0.003 // 0.3%
    private val mdrNonUmiRate = 0.007 // 0.7%

    // 1. PENGUJIAN KALKULASI MDR (MERCHANT DISCOUNT RATE)

    @Test
    fun testMdrCalculation_UmiMerchant_BelowLimit_ShouldBeFree() {
        val amount = 100000.0
        val isUmi = true

        val mdrRate = if (isUmi) (if (amount <= mdrLimit) 0.0 else mdrUmiRate) else mdrNonUmiRate
        val mdrFee = Math.round(amount * mdrRate).toDouble()
        val netAmount = amount - mdrFee

        assertEquals(0.0, mdrFee, 0.01)
        assertEquals(100000.0, netAmount, 0.01)
    }

    @Test
    fun testMdrCalculation_UmiMerchant_AboveLimit_ShouldCharge0Point3Percent() {
        val amount = 1000000.0 // 1 Juta
        val isUmi = true

        val mdrRate = if (isUmi) (if (amount <= mdrLimit) 0.0 else mdrUmiRate) else mdrNonUmiRate
        val mdrFee = Math.round(amount * mdrRate).toDouble()
        val netAmount = amount - mdrFee

        assertEquals(3000.0, mdrFee, 0.01) // 0.3% dari 1,000,000 = 3,000
        assertEquals(997000.0, netAmount, 0.01)
    }

    @Test
    fun testMdrCalculation_NonUmiMerchant_ShouldAlwaysCharge0Point7Percent() {
        val amount = 100000.0
        val isUmi = false

        val mdrRate = if (isUmi) (if (amount <= mdrLimit) 0.0 else mdrUmiRate) else mdrNonUmiRate
        val mdrFee = Math.round(amount * mdrRate).toDouble()
        val netAmount = amount - mdrFee

        assertEquals(700.0, mdrFee, 0.01) // 0.7% dari 100,000 = 700
        assertEquals(99300.0, netAmount, 0.01)
    }

    // 2. PENGUJIAN ALOKASI AUTO-SPLIT DANA (RUPIAH VS EMAS)

    @Test
    fun testAutoSplitAllocation_50PercentSplit() {
        val netAmount = 100000.0
        val splitPercentage = 50.0 // 50% untuk tabungan emas
        val remainingInstallment = 500000.0

        val rawGoldCut = netAmount * (splitPercentage / 100.0)
        val goldCut = if (rawGoldCut > remainingInstallment) remainingInstallment else rawGoldCut
        val balanceCut = netAmount - goldCut

        assertEquals(50000.0, goldCut, 0.01)
        assertEquals(50000.0, balanceCut, 0.01)
    }

    // 3. PENGUJIAN PROTEKSI AUTO-CAP LIMIT (MENCEGAH POTONGAN OVER-LIMIT)

    @Test
    fun testAutoCapProtection_WhenCutExceedsRemainingTarget() {
        val netAmount = 200000.0
        val splitPercentage = 50.0 // Potongan normal = 100,000
        val remainingInstallment = 30000.0 // Sisa target emas tinggal 30,000

        val rawGoldCut = netAmount * (splitPercentage / 100.0)
        // Auto-Cap Logic: potong PAS sebesar sisa tagihan (30,000), bukan 100,000!
        val goldCut = if (rawGoldCut > remainingInstallment) remainingInstallment else rawGoldCut
        val balanceCut = netAmount - goldCut

        assertEquals(30000.0, goldCut, 0.01) // Dipotong pas 30.000
        assertEquals(170000.0, balanceCut, 0.01) // Sisa 170.000 otomatis masuk ke Saldo Utama
    }

    // 4. PENGUJIAN KONVERSI NOMINAL KE BERAT EMAS (GRAM)

    @Test
    fun testGoldWeightConversion() {
        val goldCut = 272500.0
        val goldPricePerGram = 2725000.0

        val goldWeightGrams = goldCut / goldPricePerGram

        assertEquals(0.1, goldWeightGrams, 0.0001) // 272,500 / 2,725,000 = 0.1 Gram
    }
}
