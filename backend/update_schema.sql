-- 1. Tambahkan password ke merchant (default 123456 agar data lama tidak error)
ALTER TABLE merchant ADD COLUMN IF NOT EXISTS password TEXT DEFAULT '123456';

-- 2. Hapus fungsi lama agar tidak bentrok signature-nya
DROP FUNCTION IF EXISTS buy_gold_tx;

-- 3. Buat ulang fungsi Beli Emas yang jauh lebih aman (tanpa menerima p_gold_weight dari client)
CREATE OR REPLACE FUNCTION buy_gold_tx(
    p_merchant_id INT,
    p_amount NUMERIC
) RETURNS JSON AS $$
DECLARE
    v_balance RECORD;
    v_installment RECORD;
    v_new_accumulated_amount NUMERIC;
    v_new_accumulated_weight NUMERIC;
    v_is_completed BOOLEAN := false;
    v_transaction_id TEXT;
    v_new_tx RECORD;
    v_additional_gold_balance_reward NUMERIC := 0;
    v_gold_price NUMERIC;
    v_gold_weight_added NUMERIC;
BEGIN
    -- Validate inputs
    IF p_amount <= 0 THEN
        RAISE EXCEPTION 'Nominal pembelian emas harus lebih besar dari nol';
    END IF;

    -- Fetch current Gold Price from the database
    SELECT price INTO v_gold_price FROM gold_price ORDER BY updated_at DESC LIMIT 1;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Harga emas tidak tersedia'; 
    END IF;
    
    -- Hitung berat emas dengan aman di backend
    v_gold_weight_added := p_amount / v_gold_price;

    -- Lock balance record to prevent race conditions
    SELECT * INTO v_balance FROM balance WHERE merchant_id = p_merchant_id FOR UPDATE;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Saldo merchant tidak ditemukan';
    END IF;

    IF v_balance.main_balance < p_amount THEN
        RAISE EXCEPTION 'Saldo tidak mencukupi';
    END IF;

    -- Fetch and Lock Active Installment (if any)
    SELECT * INTO v_installment FROM installment 
    WHERE merchant_id = p_merchant_id AND is_active = true FOR UPDATE;

    IF NOT FOUND THEN
        -- No active installment
    ELSE
        v_new_accumulated_amount := v_installment.accumulated_amount + p_amount;
        v_new_accumulated_weight := v_installment.accumulated_gold_weight + v_gold_weight_added;
        v_is_completed := v_new_accumulated_amount >= v_installment.total_installment_amount;

        UPDATE installment
        SET accumulated_amount = v_new_accumulated_amount,
            accumulated_gold_weight = v_new_accumulated_weight,
            is_active = NOT v_is_completed
        WHERE id = v_installment.id;

        IF v_is_completed THEN
            v_additional_gold_balance_reward := v_installment.target_weight;
        END IF;
    END IF;

    -- Update balances (deduct money, add gold)
    UPDATE balance
    SET main_balance = main_balance - p_amount,
        gold_balance = gold_balance + v_gold_weight_added + v_additional_gold_balance_reward
    WHERE merchant_id = p_merchant_id;

    -- Generate transaction ID (BE prefix, QG suffix)
    v_transaction_id := 'BE' || to_char(CURRENT_TIMESTAMP, 'YY') || lpad((random() * 900000 + 100000)::INT::TEXT, 6, '0') || 'QG';

    -- Insert Transaction
    INSERT INTO transaksi (
        id, merchant_id, type, title, total_amount, main_amount, gold_amount, gold_weight_added, mdr_fee
    ) VALUES (
        v_transaction_id, p_merchant_id, 'GOLD_BUY', 'Beli Emas Digital',
        -p_amount, -p_amount, p_amount, v_gold_weight_added, 0.0
    ) RETURNING * INTO v_new_tx;

    RETURN row_to_json(v_new_tx);
END;
$$ LANGUAGE plpgsql;
