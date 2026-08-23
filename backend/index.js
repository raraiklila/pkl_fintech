const express = require('express');
const cors = require('cors');
const dotenv = require('dotenv');
const { createClient } = require('@supabase/supabase-js');
const axios = require('axios');

// Load environment variables
dotenv.config();

const app = express();
const PORT = process.env.PORT || 3000;

app.use(cors());
app.use(express.json());

// -------------------------------------------------------------
// 1. APP CONSTANTS
// -------------------------------------------------------------
const MDR_UMI_RATE = 0.003;
const MDR_NON_UMI_RATE = 0.007;
const MDR_LIMIT = 500000.0;
const MOCK_AUTH_TOKEN = 'mock-jwt-token-for-merchant-1';

// Midtrans Sandbox Config
const MIDTRANS_SERVER_KEY = process.env.MIDTRANS_SERVER_KEY || '';
const MIDTRANS_BASE_URL = process.env.MIDTRANS_BASE_URL || 'https://api.sandbox.midtrans.com';
const midtransAuth = Buffer.from(MIDTRANS_SERVER_KEY + ':').toString('base64');

// In-memory map: orderId -> merchantId (untuk dipakai saat webhook/status check)
const qrisOrderMap = {};

// -------------------------------------------------------------
// 2. SUPABASE INITIALIZATION
// -------------------------------------------------------------
const supabaseUrl = process.env.SUPABASE_URL;
const supabaseKey = process.env.SUPABASE_SERVICE_ROLE_KEY || process.env.SUPABASE_KEY;

if (!supabaseUrl || !supabaseKey) {
  console.warn('Warning: SUPABASE_URL or SUPABASE_ANON_KEY is missing in your .env file!');
}

const supabase = createClient(supabaseUrl || 'https://placeholder.supabase.co', supabaseKey || 'placeholder');

// -------------------------------------------------------------
// 3. SECURE AUTHENTICATION MIDDLEWARE
// -------------------------------------------------------------
const authenticateMerchant = (req, res, next) => {
  const authHeader = req.headers.authorization;
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return res.status(401).json({ error: 'Akses tidak sah: Token otentikasi tidak disediakan' });
  }
  const token = authHeader.substring(7);

  if (token === MOCK_AUTH_TOKEN) {
    req.merchantId = 1;
    return next();
  }

  if (token.startsWith('merchant-token-')) {
    const id = parseInt(token.replace('merchant-token-', ''), 10);
    if (!isNaN(id)) {
      req.merchantId = id;
      return next();
    }
  }

  return res.status(403).json({ error: 'Akses ditolak: Token otentikasi tidak valid' });
};

// -------------------------------------------------------------
// 4. ROBUST INPUT VALIDATION MIDDLEWARE
// -------------------------------------------------------------
const validateWithdraw = (req, res, next) => {
  const { merchantId, amount } = req.body;
  if (!merchantId || typeof merchantId !== 'number' || merchantId <= 0) {
    return res.status(400).json({ error: 'merchantId harus berupa angka integer positif' });
  }
  if (amount === undefined || typeof amount !== 'number' || amount <= 0) {
    return res.status(400).json({ error: 'amount harus berupa angka positif' });
  }
  next();
};

const validateBuyGold = (req, res, next) => {
  const { merchantId, amount } = req.body;
  if (!merchantId || typeof merchantId !== 'number' || merchantId <= 0) {
    return res.status(400).json({ error: 'merchantId harus berupa angka integer positif' });
  }
  if (amount === undefined || typeof amount !== 'number' || amount <= 0) {
    return res.status(400).json({ error: 'amount harus berupa angka positif' });
  }
  next();
};

const validateSellGold = (req, res, next) => {
  const { merchantId, goldWeight } = req.body;
  if (!merchantId || typeof merchantId !== 'number' || merchantId <= 0) {
    return res.status(400).json({ error: 'merchantId harus berupa angka integer positif' });
  }
  if (goldWeight === undefined || typeof goldWeight !== 'number' || goldWeight <= 0) {
    return res.status(400).json({ error: 'goldWeight harus berupa angka positif' });
  }
  next();
};

const validateSimulateQris = (req, res, next) => {
  const { merchantId, amount } = req.body;
  if (!merchantId || typeof merchantId !== 'number' || merchantId <= 0) {
    return res.status(400).json({ error: 'merchantId harus berupa angka integer positif' });
  }
  if (amount === undefined || typeof amount !== 'number' || amount <= 0) {
    return res.status(400).json({ error: 'amount harus berupa angka positif' });
  }
  next();
};

const validateSetupInstallment = (req, res, next) => {
  const { merchantId, targetWeight, totalInstallmentAmount, splitPercentage } = req.body;
  if (!merchantId || typeof merchantId !== 'number' || merchantId <= 0) {
    return res.status(400).json({ error: 'merchantId harus berupa angka integer positif' });
  }
  if (targetWeight === undefined || typeof targetWeight !== 'number' || targetWeight <= 0) {
    return res.status(400).json({ error: 'targetWeight harus berupa angka positif' });
  }
  if (totalInstallmentAmount === undefined || typeof totalInstallmentAmount !== 'number' || totalInstallmentAmount <= 0) {
    return res.status(400).json({ error: 'totalInstallmentAmount harus berupa angka positif' });
  }
  if (splitPercentage === undefined || typeof splitPercentage !== 'number' || splitPercentage < 1 || splitPercentage > 100) {
    return res.status(400).json({ error: 'splitPercentage harus berada di antara 1 dan 100' });
  }
  next();
};

// -------------------------------------------------------------
// 5. DETAILED ERROR HANDLING HELPER
// -------------------------------------------------------------
const handleDatabaseError = (res, error) => {
  console.error('Database Error:', error);
  const msg = error.message || '';
  if (msg.includes('tidak ditemukan') || msg.includes('not found') || msg.includes('tidak ada')) {
    return res.status(404).json({ error: msg });
  }
  if (msg.includes('tidak mencukupi') || msg.includes('insufficient') || msg.includes('harga emas tidak tersedia')) {
    return res.status(400).json({ error: msg });
  }
  return res.status(500).json({ error: 'Database Internal Error: ' + msg });
};

// 6. API ENDPOINTS

// Test Route
app.get('/api/test', (req, res) => {
  res.json({ message: 'Backend PKL Finance is running successfully!' });
});

// GET Current Gold Price & Trend 
app.get('/api/gold-price', async (req, res) => {
  try {
    const { data, error } = await supabase
      .from('gold_price')
      .select('*')
      .order('updated_at', { ascending: false })
      .limit(2);

    if (error) return handleDatabaseError(res, error);

    if (!data || data.length === 0) {
      return res.status(404).json({ error: 'Data harga emas belum tersedia di database' });
    }

    const price = parseFloat(data[0].price);
    const buyPrice = price;
    const sellPrice = data[0].buyback_price
      ? parseFloat(data[0].buyback_price)
      : (data[0].sell_price ? parseFloat(data[0].sell_price) : Math.round(price * 0.9486));

    let change = 0.0;
    let percent = 0.0;
    let trend = 'up';

    if (data.length > 1) {
      const prevPrice = parseFloat(data[1].price);
      change = price - prevPrice;
      percent = prevPrice > 0 ? (change / prevPrice) * 100 : 0.0;
      trend = change >= 0 ? 'up' : 'down';
    }

    const spreadDiff = buyPrice - sellPrice;
    const spreadPercent = parseFloat(((spreadDiff / buyPrice) * 100).toFixed(2));

    res.json({
      price,
      buyPrice,
      sellPrice,
      spreadDiff,
      spreadPercent,
      change,
      percent,
      trend,
      updated_at: data[0].updated_at
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// GET Gold Price History (last N entries) for chart display
app.get('/api/gold-price/history', async (req, res) => {
  const limit = parseInt(req.query.limit) || 6;
  try {
    const { data, error } = await supabase
      .from('gold_price')
      .select('price, updated_at')
      .order('updated_at', { ascending: false })
      .limit(limit);

    if (error) return handleDatabaseError(res, error);

    // Return in chronological order (oldest first) for chart
    const history = (data || []).reverse().map(row => ({
      price: parseFloat(row.price),
      updated_at: row.updated_at
    }));

    res.json({ history });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});


// FETCH MERCHANTS
app.get('/api/merchants', async (req, res) => {
  try {
    const { data, error } = await supabase
      .from('merchant')
      .select('*');

    if (error) return handleDatabaseError(res, error);
    res.json(data);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Fetch Single Merchant by ID
app.get('/api/merchants/:id', async (req, res) => {
  try {
    const { data, error } = await supabase
      .from('merchant')
      .select('*')
      .eq('id', req.params.id)
      .single();

    if (error) return handleDatabaseError(res, error);
    res.json(data);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Fetch Balances by Merchant ID
app.get('/api/balances/:merchantId', async (req, res) => {
  try {
    const { data, error } = await supabase
      .from('balance')
      .select('*')
      .eq('merchant_id', req.params.merchantId)
      .single();

    if (error) return handleDatabaseError(res, error);
    res.json(data);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Fetch Transaction Ledger History by Merchant ID
app.get('/api/transactions/:merchantId', async (req, res) => {
  try {
    const { data, error } = await supabase
      .from('transaksi')
      .select('*')
      .eq('merchant_id', req.params.merchantId)
      .order('created_at', { ascending: false });

    if (error) return handleDatabaseError(res, error);
    res.json(data);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Fetch Active Installment by Merchant ID
app.get('/api/installments/:merchantId', async (req, res) => {
  try {
    const { data, error } = await supabase
      .from('installment')
      .select('*')
      .eq('merchant_id', req.params.merchantId)
      .eq('is_active', true)
      .maybeSingle();

    if (error) return handleDatabaseError(res, error);
    res.json(data);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Setup Installment (Transactional RPC)
app.post('/api/installments', authenticateMerchant, validateSetupInstallment, async (req, res) => {
  const { merchantId, targetWeight, totalInstallmentAmount, splitPercentage } = req.body;
  try {
    const { data, error } = await supabase
      .rpc('create_installment_tx', {
        p_merchant_id: merchantId,
        p_target_weight: targetWeight,
        p_total_installment_amount: totalInstallmentAmount,
        p_split_percentage: splitPercentage
      });

    if (error) return handleDatabaseError(res, error);
    res.json(data);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Perform Cash Withdrawal (Transactional RPC)
app.post('/api/transactions/withdraw', authenticateMerchant, validateWithdraw, async (req, res) => {
  const { merchantId, amount } = req.body;
  try {
    const { data, error } = await supabase
      .rpc('withdraw_balance_tx', {
        p_merchant_id: merchantId,
        p_amount: amount
      });

    if (error) return handleDatabaseError(res, error);
    res.json(data);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Perform Gold Purchase (Transactional RPC)
app.post('/api/transactions/buy-gold', authenticateMerchant, validateBuyGold, async (req, res) => {
  const { merchantId, amount } = req.body;
  try {
    const { data, error } = await supabase
      .rpc('buy_gold_tx', {
        p_merchant_id: merchantId,
        p_amount: amount
      });

    if (error) return handleDatabaseError(res, error);
    res.json(data);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Perform Gold Sale (Direct Reliable Transaction)
app.post('/api/transactions/sell-gold', authenticateMerchant, validateSellGold, async (req, res) => {
  const { merchantId, goldWeight } = req.body;
  try {
    // 1. Ambil harga emas & buyback terbaru langsung dari database
    const { data: priceData, error: priceErr } = await supabase
      .from('gold_price')
      .select('*')
      .order('updated_at', { ascending: false })
      .limit(1);

    if (priceErr || !priceData || priceData.length === 0) {
      return res.status(404).json({ error: 'Harga emas belum tersedia di database' });
    }

    const sellRate = priceData[0].buyback_price
      ? parseFloat(priceData[0].buyback_price)
      : (priceData[0].sell_price ? parseFloat(priceData[0].sell_price) : Math.round(parseFloat(priceData[0].price) * 0.9486));

    const totalRupiah = Math.round(goldWeight * sellRate);

    // 2. Ambil saldo merchant
    const { data: balanceData, error: balErr } = await supabase
      .from('balance')
      .select('*')
      .eq('merchant_id', merchantId)
      .single();

    if (balErr || !balanceData) {
      return res.status(404).json({ error: 'Saldo merchant tidak ditemukan' });
    }

    const currentGoldBal = parseFloat(balanceData.gold_balance);
    if (currentGoldBal < goldWeight) {
      return res.status(400).json({ error: 'Saldo emas tidak mencukupi untuk penjualan ini' });
    }

    const newGoldBalance = Math.max(0, currentGoldBal - goldWeight);
    const newMainBalance = parseFloat(balanceData.main_balance) + totalRupiah;

    // 3. Update saldo merchant di database
    const { error: updateErr } = await supabase
      .from('balance')
      .update({
        gold_balance: newGoldBalance,
        main_balance: newMainBalance
      })
      .eq('merchant_id', merchantId);

    if (updateErr) return handleDatabaseError(res, updateErr);

    // 4. Buat ID transaksi unik
    const randomNum = Math.floor(100000 + Math.random() * 900000);
    const yearSuffix = new Date().getFullYear().toString().slice(-2);
    const txId = `JE${yearSuffix}${randomNum}QG`;

    // 5. Catat transaksi penjualan ke tabel transaksi
    const { data: txData, error: txErr } = await supabase
      .from('transaksi')
      .insert([{
        id: txId,
        merchant_id: merchantId,
        type: 'GOLD_SELL',
        title: 'Jual Emas Digital',
        total_amount: totalRupiah,
        main_amount: totalRupiah,
        gold_amount: 0.0,
        gold_weight_added: -goldWeight,
        mdr_fee: 0.0
      }])
      .select()
      .single();

    if (txErr) return handleDatabaseError(res, txErr);

    res.json(txData);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// -------------------------------------------------------------
// HELPER: Proses Transaksi QRIS dengan Proteksi Auto-Cap Cicilan
// -------------------------------------------------------------
async function processQrisPayment(merchantId, amount) {
  // 1. Ambil data merchant untuk cek status UMI / Non-UMI
  const { data: merchant, error: mErr } = await supabase
    .from('merchant')
    .select('*')
    .eq('id', merchantId)
    .single();

  if (mErr || !merchant) {
    throw new Error('Merchant tidak ditemukan');
  }

  // 2. Hitung MDR Fee
  let mdrRate = merchant.is_umi
    ? (amount <= MDR_LIMIT ? 0.0 : MDR_UMI_RATE)
    : MDR_NON_UMI_RATE;
  const mdrFee = Math.round(amount * mdrRate);
  const netAmount = amount - mdrFee;

  // 3. Ambil harga emas terkini dari database
  const { data: priceData } = await supabase
    .from('gold_price')
    .select('price')
    .order('updated_at', { ascending: false })
    .limit(1);

  const goldPrice = priceData && priceData.length > 0 ? parseFloat(priceData[0].price) : 2725000.0;

  // 4. Cek Cicilan Emas Aktif
  const { data: installments } = await supabase
    .from('installment')
    .select('*')
    .eq('merchant_id', merchantId)
    .eq('is_active', true)
    .limit(1);

  let goldCut = 0.0;
  let balanceCut = netAmount;
  let goldWeightAdded = 0.0;
  let additionalGoldReward = 0.0;

  if (installments && installments.length > 0) {
    const inst = installments[0];
    const totalInstAmount = parseFloat(inst.total_installment_amount);
    const currentAccAmount = parseFloat(inst.accumulated_amount);
    const remainingInstallment = Math.max(0, totalInstAmount - currentAccAmount);

    // Potongan normal sesuai persentase split
    const rawGoldCut = netAmount * (inst.split_percentage / 100.0);

    // 🌟 AUTO-CAP LOGIC (Kasus 2):
    // Jika potongan normal melebihi sisa tagihan, potong PAS sebesar sisa tagihan!
    if (rawGoldCut > remainingInstallment) {
      goldCut = remainingInstallment;
    } else {
      goldCut = rawGoldCut;
    }

    // Kelebihan uang otomatis masuk ke Saldo Merchant (Rupiah)
    balanceCut = netAmount - goldCut;
    goldWeightAdded = goldCut / goldPrice;

    const newAccumulatedAmount = currentAccAmount + goldCut;
    const newAccumulatedWeight = parseFloat(inst.accumulated_gold_weight) + goldWeightAdded;
    const isCompleted = newAccumulatedAmount >= (totalInstAmount - 1.0); // toleransi pembulatan

    // Update status cicilan
    await supabase
      .from('installment')
      .update({
        accumulated_amount: newAccumulatedAmount,
        accumulated_gold_weight: newAccumulatedWeight,
        is_active: !isCompleted
      })
      .eq('id', inst.id);

    // Jika lunas, seluruh target gram emas diberikan ke saldo emas merchant
    if (isCompleted) {
      additionalGoldReward = parseFloat(inst.target_weight);
    }
  }

  // 5. Update Saldo Merchant & Saldo Emas
  const { data: currentBal } = await supabase
    .from('balance')
    .select('*')
    .eq('merchant_id', merchantId)
    .single();

  const newMainBal = (currentBal ? parseFloat(currentBal.main_balance) : 0.0) + balanceCut;
  const newGoldBal = (currentBal ? parseFloat(currentBal.gold_balance) : 0.0) + additionalGoldReward;

  await supabase
    .from('balance')
    .update({
      main_balance: newMainBal,
      gold_balance: newGoldBal
    })
    .eq('merchant_id', merchantId);

  // 6. Buat Record Transaksi QRIS_IN
  const randomNum = Math.floor(100000 + Math.random() * 900000);
  const yearSuffix = new Date().getFullYear().toString().slice(-2);
  const txId = `QRIS${yearSuffix}${randomNum}QG`;

  const { data: txRecord, error: txErr } = await supabase
    .from('transaksi')
    .insert([{
      id: txId,
      merchant_id: merchantId,
      type: 'QRIS_IN',
      title: 'Pembayaran QRIS - Pelanggan',
      total_amount: amount,
      main_amount: balanceCut,
      gold_amount: goldCut,
      gold_weight_added: goldWeightAdded,
      mdr_fee: mdrFee
    }])
    .select()
    .single();

  if (txErr) throw txErr;
  return txRecord;
}

// Simulate Incoming QRIS Payment
app.post('/api/transactions/simulate-qris', validateSimulateQris, async (req, res) => {
  const { merchantId, amount } = req.body;
  try {
    const txData = await processQrisPayment(merchantId, amount);
    res.json({
      success: true,
      message: 'Simulasi transaksi berhasil diproses oleh server!',
      transaction: txData
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// -------------------------------------------------------------
// GET: Ambil Harga Emas Terbaru (Untuk dipakai Frontend)
// -------------------------------------------------------------
app.get('/api/gold-price/current', async (req, res) => {
  try {
    const { data, error } = await supabase
      .from('gold_price')
      .select('*')
      .order('updated_at', { ascending: false })
      .limit(1)
      .single();

    if (error) return handleDatabaseError(res, error);
    res.json(data);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// -------------------------------------------------------------
// POST: Tambah Harga Emas Baru (Opsi 2 - Admin)
// -------------------------------------------------------------
app.post('/api/gold-price', authenticateMerchant, async (req, res) => {
  const { price, buyback_price } = req.body;

  if (!price || typeof price !== 'number' || price <= 0) {
    return res.status(400).json({ error: 'Harga emas harus berupa angka positif' });
  }

  try {
    const payload = { price: price };
    if (buyback_price && typeof buyback_price === 'number') {
      payload.buyback_price = buyback_price;
    }

    const { data, error } = await supabase
      .from('gold_price')
      .insert([payload])
      .select();

    if (error) return handleDatabaseError(res, error);
    res.json({
      success: true,
      message: 'Harga emas berhasil diperbarui (History baru ditambahkan)',
      data: data[0]
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// -------------------------------------------------------------
// AUTH & KYC ENDPOINTS
// -------------------------------------------------------------
app.post('/api/auth/register', async (req, res) => {
  const { username, password, ownerName, shopName } = req.body;
  if (!username || !password || !ownerName || !shopName) {
    return res.status(400).json({ success: false, message: 'Semua kolom wajib diisi' });
  }

  try {
    const { data: existing } = await supabase
      .from('merchant')
      .select('id')
      .eq('username', username)
      .maybeSingle();

    if (existing) {
      return res.status(400).json({ success: false, message: 'Username sudah digunakan' });
    }

    const { data, error } = await supabase
      .from('merchant')
      .insert([{
        username,
        password,
        owner_name: ownerName,
        shop_name: shopName,
        shop_address: 'Alamat belum diatur',
        business_type: 'Perdagangan',
        is_verified: false,
        is_umi: true
      }])
      .select()
      .single();

    if (error) return res.status(500).json({ success: false, message: error.message });

    // Insert Initial Balance
    await supabase.from('balance').insert([{
      merchant_id: data.id,
      main_balance: 0.0,
      gold_balance: 0.0
    }]);

    res.json({
      success: true,
      message: 'Registrasi berhasil',
      merchant: data,
      token: `merchant-token-${data.id}`
    });
  } catch (err) {
    res.status(500).json({ success: false, message: err.message });
  }
});

app.post('/api/auth/login', async (req, res) => {
  const { username, password } = req.body;
  if (!username || !password) {
    return res.status(400).json({ success: false, message: 'Username dan password wajib diisi' });
  }

  try {
    const { data, error } = await supabase
      .from('merchant')
      .select('*')
      .eq('username', username)
      .eq('password', password)
      .maybeSingle();

    if (error) return res.status(500).json({ success: false, message: error.message });

    if (!data) {
      return res.status(401).json({ success: false, message: 'Username atau password salah' });
    }

    res.json({
      success: true,
      message: 'Login berhasil',
      merchant: data,
      token: `merchant-token-${data.id}`
    });
  } catch (err) {
    res.status(500).json({ success: false, message: err.message });
  }
});

app.put('/api/merchants/verify', async (req, res) => {
  const { merchantId, fullName, nik, shopAddress } = req.body;
  if (!merchantId || !fullName || !nik) {
    return res.status(400).json({ success: false, message: 'Data verifikasi tidak lengkap' });
  }

  try {
    const updateData = {
      owner_name: fullName,
      ktp_number: nik,
      is_verified: true
    };

    if (shopAddress) {
      updateData.shop_address = shopAddress;
    }

    const { data, error } = await supabase
      .from('merchant')
      .update(updateData)
      .eq('id', merchantId)
      .select()
      .single();

    if (error) return res.status(500).json({ success: false, message: error.message });

    res.json({
      success: true,
      message: 'Verifikasi berhasil',
      merchant: data
    });
  } catch (err) {
    res.status(500).json({ success: false, message: err.message });
  }
});

// -------------------------------------------------------------
// UPDATE BANK ACCOUNT
// -------------------------------------------------------------
app.put('/api/merchants/bank-account', async (req, res) => {
  const { merchantId, bankName, bankAccountNumber, bankAccountName } = req.body;
  if (!merchantId || !bankName || !bankAccountNumber || !bankAccountName) {
    return res.status(400).json({ success: false, message: 'Data rekening tidak lengkap' });
  }

  try {
    const { data, error } = await supabase
      .from('merchant')
      .update({
        bank_name: bankName,
        bank_account_number: bankAccountNumber,
        bank_account_name: bankAccountName
      })
      .eq('id', merchantId)
      .select()
      .single();

    if (error) return res.status(500).json({ success: false, message: error.message });

    res.json({
      success: true,
      message: 'Rekening bank berhasil disimpan',
      merchant: data
    });
  } catch (err) {
    res.status(500).json({ success: false, message: err.message });
  }
});

// -------------------------------------------------------------
// MIDTRANS QRIS ENDPOINTS
// -------------------------------------------------------------

// POST: Buat transaksi QRIS baru via Midtrans Sandbox
app.post('/api/qris/create', async (req, res) => {
  const { merchantId, amount, shopName } = req.body;

  if (!merchantId || typeof merchantId !== 'number' || merchantId <= 0) {
    return res.status(400).json({ error: 'merchantId harus berupa angka positif' });
  }
  if (!amount || typeof amount !== 'number' || amount <= 0) {
    return res.status(400).json({ error: 'amount harus berupa angka positif' });
  }
  if (!MIDTRANS_SERVER_KEY || MIDTRANS_SERVER_KEY.includes('GANTI_DENGAN')) {
    return res.status(500).json({
      error: 'MIDTRANS_SERVER_KEY belum diisi di file .env backend. Daftar di https://dashboard.sandbox.midtrans.com'
    });
  }

  // Buat order ID unik: QRIS-merchantId-timestamp
  const orderId = `QRIS-${merchantId}-${Date.now()}`;

  try {
    const response = await axios.post(
      `${MIDTRANS_BASE_URL}/v2/charge`,
      {
        payment_type: 'qris',
        transaction_details: {
          order_id: orderId,
          gross_amount: Math.round(amount)
        },
        qris: {
          acquirer: 'gopay'
        },
        custom_field1: String(merchantId)
      },
      {
        headers: {
          'Accept': 'application/json',
          'Content-Type': 'application/json',
          'Authorization': `Basic ${midtransAuth}`
        }
      }
    );

    const data = response.data;

    // Ambil URL gambar QR dari actions Midtrans
    const qrAction = data.actions?.find(a => a.name === 'generate-qr-code');
    const qrImageUrl = qrAction ? qrAction.url : null;

    // Simpan mapping orderId -> merchantId untuk status check
    qrisOrderMap[orderId] = { merchantId, amount };

    console.log(`[QRIS BARU DIBUAT]`);
    console.log(`Order ID    : ${orderId}`);
    console.log(`Nominal     : Rp ${amount}`);
    console.log(`URL QR Code : ${qrImageUrl} \n`);

    return res.json({
      success: true,
      orderId: orderId,
      transactionId: data.transaction_id,
      qrImageUrl: qrImageUrl,
      qrString: data.qr_string,
      expiryTime: data.expiry_time,
      status: data.transaction_status
    });

  } catch (err) {
    console.error('[QRIS Create Error]', err.response?.data || err.message);
    const errMsg = err.response?.data?.status_message || err.message || 'Gagal membuat transaksi QRIS';
    return res.status(500).json({ error: errMsg });
  }
});

// GET: Cek status pembayaran QRIS by orderId
app.get('/api/qris/status/:orderId', async (req, res) => {
  const { orderId } = req.params;

  if (!orderId) {
    return res.status(400).json({ error: 'orderId diperlukan' });
  }
  if (!MIDTRANS_SERVER_KEY || MIDTRANS_SERVER_KEY.includes('GANTI_DENGAN')) {
    return res.status(500).json({ error: 'MIDTRANS_SERVER_KEY belum diisi di file .env' });
  }

  try {
    const response = await axios.get(
      `${MIDTRANS_BASE_URL}/v2/${orderId}/status`,
      {
        headers: {
          'Accept': 'application/json',
          'Authorization': `Basic ${midtransAuth}`
        }
      }
    );

    const data = response.data;
    const status = data.transaction_status;

    console.log(`[QRIS] Status check for ${orderId}: ${status}`);

    // Kalau sudah settlement atau capture = pembayaran berhasil
    if (status === 'settlement' || status === 'capture') {
      const orderInfo = qrisOrderMap[orderId];

      if (orderInfo && !orderInfo.processed) {
        // Tandai sudah diproses agar tidak double-update saldo
        qrisOrderMap[orderId].processed = true;

        const { merchantId, amount } = orderInfo;

        // Update saldo merchant dengan auto-cap cicilan
        try {
          const txData = await processQrisPayment(merchantId, amount);

          return res.json({
            status: 'paid',
            transaction: txData || null,
            midtransStatus: status
          });
        } catch (procErr) {
          console.error('[QRIS] Process Payment Error:', procErr);
          return res.json({ status: 'paid', transaction: null, midtransStatus: status });
        }

      } else {
        // Sudah diproses sebelumnya
        return res.json({ status: 'paid', transaction: null, midtransStatus: status });
      }

    } else if (status === 'expire') {
      return res.json({ status: 'expired', midtransStatus: status });
    } else {
      // pending, dll
      return res.json({ status: 'pending', midtransStatus: status });
    }

  } catch (err) {
    console.error('[QRIS Status Error]', err.response?.data || err.message);
    // Kalau 404 dari Midtrans (transaksi tidak ditemukan) = pending
    if (err.response?.status === 404) {
      return res.json({ status: 'pending', midtransStatus: 'not_found' });
    }
    return res.status(500).json({ error: err.response?.data?.status_message || err.message });
  }
});

// Start Server
app.listen(PORT, '0.0.0.0', () => {
  console.log(`Server PKL Finance is running on http://0.0.0.0:${PORT}`);
});
