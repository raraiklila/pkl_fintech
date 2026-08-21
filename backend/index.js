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

// -------------------------------------------------------------
// 6. API ENDPOINTS
// -------------------------------------------------------------

// Test Route
app.get('/api/test', (req, res) => {
  res.json({ message: 'Backend PKL Finance is running successfully!' });
});

// GET Current Gold Price & Trend from database
app.get('/api/gold-price', async (req, res) => {
  try {
    const { data, error } = await supabase
      .from('gold_price')
      .select('*')
      .order('updated_at', { ascending: false })
      .limit(2);

    if (error) return handleDatabaseError(res, error);

    let price = 2605000.0;
    let change = 0.0;
    let percent = 0.0;
    let trend = 'up';

    if (data && data.length > 0) {
      price = parseFloat(data[0].price);
      if (data.length > 1) {
        const prevPrice = parseFloat(data[1].price);
        change = price - prevPrice;
        percent = prevPrice > 0 ? (change / prevPrice) * 100 : 0.0;
        trend = change >= 0 ? 'up' : 'down';
      }
    }

    res.json({
      price,
      change,
      percent,
      trend,
      updated_at: data && data.length > 0 ? data[0].updated_at : null
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// -------------------------------------------------------------
// FETCH MERCHANTS
// -------------------------------------------------------------
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

// Perform Gold Sale (Transactional RPC)
app.post('/api/transactions/sell-gold', authenticateMerchant, validateSellGold, async (req, res) => {
  const { merchantId, goldWeight } = req.body;
  try {
    const { data, error } = await supabase
      .rpc('sell_gold_tx', {
        p_merchant_id: merchantId,
        p_gold_weight: goldWeight
      });

    if (error) return handleDatabaseError(res, error);
    res.json(data);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// Simulate Incoming QRIS Payment (Transactional RPC)
app.post('/api/transactions/simulate-qris', validateSimulateQris, async (req, res) => {
  const { merchantId, amount } = req.body;
  try {
    const { data, error } = await supabase
      .rpc('simulate_qris_payment_tx', {
        p_merchant_id: merchantId,
        p_amount: amount,
        p_mdr_umi_rate: MDR_UMI_RATE,
        p_mdr_non_umi_rate: MDR_NON_UMI_RATE,
        p_mdr_limit: MDR_LIMIT
      });

    if (error) return handleDatabaseError(res, error);
    res.json({
      success: true,
      message: 'Simulasi transaksi berhasil diproses oleh server!',
      transaction: data
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
  const { price } = req.body;

  if (!price || typeof price !== 'number' || price <= 0) {
    return res.status(400).json({ error: 'Harga emas harus berupa angka positif' });
  }

  try {
    const { data, error } = await supabase
      .from('gold_price')
      .insert([{ price: price }])
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

        // Update saldo merchant via Supabase RPC (sama seperti simulate-qris)
        try {
          const { data: txData, error: txError } = await supabase
            .rpc('simulate_qris_payment_tx', {
              p_merchant_id: merchantId,
              p_amount: amount,
              p_mdr_umi_rate: MDR_UMI_RATE,
              p_mdr_non_umi_rate: MDR_NON_UMI_RATE,
              p_mdr_limit: MDR_LIMIT
            });

          if (txError) {
            console.error('[QRIS] Supabase RPC Error:', txError);
            // Tetap return paid, biar client tahu sudah bayar
          }

          return res.json({
            status: 'paid',
            transaction: txData || null,
            midtransStatus: status
          });

        } catch (rpcErr) {
          console.error('[QRIS] RPC Exception:', rpcErr);
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
