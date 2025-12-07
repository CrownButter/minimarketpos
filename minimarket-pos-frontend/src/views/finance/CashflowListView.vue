<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';

const cashflows = ref([]);
const startDate = ref('');
const endDate = ref('');
const isLoading = ref(false);

// State untuk Ringkasan Keuangan
const summary = ref({
  inflow: 0,
  outflow: 0,
  net: 0
});

// Format Mata Uang
const formatCurrency = (value) => {
  if (value === null || value === undefined) return 'Rp 0';
  return new Intl.NumberFormat('id-ID', { style: 'currency', currency: 'IDR' }).format(value);
};

// Format Tanggal
const formatDate = (dateInput) => {
  if (!dateInput) return '-';
  // Handle jika backend kirim array [yyyy, mm, dd]
  if (Array.isArray(dateInput)) {
    return new Date(dateInput[0], dateInput[1] - 1, dateInput[2]).toLocaleDateString('id-ID');
  }
  return new Date(dateInput).toLocaleDateString('id-ID');
};

// Set Default Date (Bulan Ini)
const setDefaultDates = () => {
  const date = new Date();
  const firstDay = new Date(date.getFullYear(), date.getMonth(), 1);
  const lastDay = new Date(date.getFullYear(), date.getMonth() + 1, 0);

  // Format YYYY-MM-DD untuk input date HTML
  startDate.value = firstDay.toISOString().split('T')[0];
  endDate.value = lastDay.toISOString().split('T')[0];
};

// Ambil Data Ringkasan (In, Out, Net)
const fetchSummary = async () => {
  if (!startDate.value || !endDate.value) return;

  try {
    const params = `?start=${startDate.value}&end=${endDate.value}`;

    // Panggil 3 Endpoint Statistik dari CashflowController
    const [inRes, outRes, netRes] = await Promise.all([
      axios.get(`http://localhost:8080/api/cashflows/inflow-total${params}`),
      axios.get(`http://localhost:8080/api/cashflows/outflow-total${params}`),
      axios.get(`http://localhost:8080/api/cashflows/net-cashflow${params}`)
    ]);

    summary.value = {
      inflow: inRes.data || 0,
      outflow: outRes.data || 0,
      net: netRes.data || 0
    };
  } catch (e) {
    console.error("Gagal memuat ringkasan", e);
  }
};

// Ambil List Data Cashflow
const fetchCashflows = async () => {
  isLoading.value = true;
  try {
    let url = 'http://localhost:8080/api/cashflows';

    // Gunakan filter date-range jika tersedia
    if (startDate.value && endDate.value) {
      url = `http://localhost:8080/api/cashflows/date-range?start=${startDate.value}&end=${endDate.value}`;
    }

    const { data } = await axios.get(url);
    cashflows.value = data;

    // Update summary setiap kali fetch data list
    if (startDate.value && endDate.value) {
      fetchSummary();
    }
  } catch (error) {
    console.error(error);
    alert("Gagal memuat data arus kas.");
  } finally {
    isLoading.value = false;
  }
};

// Hapus Cashflow
const deleteCashflow = async (id) => {
  if (!confirm('Hapus data transaksi ini?')) return;

  try {
    await axios.delete(`http://localhost:8080/api/cashflows/${id}`);
    alert('Data berhasil dihapus');
    fetchCashflows();
  } catch (error) {
    alert('Gagal menghapus data.');
  }
};

onMounted(() => {
  setDefaultDates();
  fetchCashflows();
});
</script>

<template>
  <div class="cashflow-page">

    <div class="header-section">
      <div class="title-row">
        <h1>Arus Kas (Cashflow)</h1>
        </div>

      <div class="summary-cards">
        <div class="card bg-green">
          <span>Pemasukan (Inflow)</span>
          <h3>{{ formatCurrency(summary.inflow) }}</h3>
        </div>
        <div class="card bg-red">
          <span>Pengeluaran (Outflow)</span>
          <h3>{{ formatCurrency(summary.outflow) }}</h3>
        </div>
        <div class="card bg-blue">
          <span>Saldo Bersih (Net)</span>
          <h3>{{ formatCurrency(summary.net) }}</h3>
        </div>
      </div>
    </div>

    <div class="action-bar">
      <div class="filter-group">
        <label>Periode:</label>
        <input type="date" v-model="startDate" class="date-input" />
        <span>s/d</span>
        <input type="date" v-model="endDate" class="date-input" />
        <button @click="fetchCashflows" class="btn-primary" :disabled="isLoading">
          {{ isLoading ? 'Loading...' : 'Terapkan Filter' }}
        </button>
      </div>
    </div>

    <div class="table-responsive">
      <table class="data-table">
        <thead>
          <tr>
            <th>Tanggal</th>
            <th>Ref</th>
            <th>Kategori</th>
            <th>Toko</th>
            <th>Tipe</th>
            <th class="text-right">Jumlah</th>
            <th>Status</th>
            <th>Aksi</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="cashflows.length === 0">
            <td colspan="8" class="text-center">Tidak ada data pada periode ini.</td>
          </tr>
          <tr v-for="cf in cashflows" :key="cf.id">
            <td>{{ formatDate(cf.date) }}</td>
            <td>{{ cf.reference }}</td>
            <td>{{ cf.categoryName || '-' }}</td>
            <td>{{ cf.storeName || 'Pusat' }}</td>
            <td>
              <span class="type-badge" :class="cf.amount >= 0 ? 'in' : 'out'">
                {{ cf.amount < 0 ? 'OUT' : 'IN' }}
              </span>
            </td>
            <td class="text-right font-bold" :class="cf.amount < 0 ? 'text-red' : 'text-green'">
              {{ formatCurrency(cf.amount) }}
            </td>
            <td>
              <span class="status-badge" :class="cf.lunas ? 'lunas' : 'hutang'">
                {{ cf.lunas ? 'Selesai' : 'Pending' }}
              </span>
            </td>
            <td>
              <button @click="deleteCashflow(cf.id)" class="btn-icon delete" title="Hapus">🗑️</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
.cashflow-page { padding-bottom: 40px; }

/* Summary Cards */
.summary-cards { display: flex; gap: 15px; margin-top: 15px; flex-wrap: wrap; }
.card {
  flex: 1; min-width: 200px;
  padding: 20px; border-radius: 8px; color: white;
  box-shadow: 0 4px 6px rgba(0,0,0,0.1);
}
.bg-green { background: linear-gradient(135deg, #2ecc71, #27ae60); }
.bg-red { background: linear-gradient(135deg, #e74c3c, #c0392b); }
.bg-blue { background: linear-gradient(135deg, #3498db, #2980b9); }

.card span { font-size: 13px; text-transform: uppercase; letter-spacing: 1px; opacity: 0.9; }
.card h3 { font-size: 26px; margin: 10px 0 0; font-weight: bold; }

/* Action Bar */
.action-bar {
  background: white; padding: 15px; border-radius: 8px; margin: 20px 0;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}
.filter-group { display: flex; gap: 10px; align-items: center; flex-wrap: wrap; }
.date-input { padding: 8px; border: 1px solid #ddd; border-radius: 4px; }
.btn-primary { background: #2c3e50; color: white; border: none; padding: 8px 15px; border-radius: 4px; cursor: pointer; }

/* Table Styling */
.table-responsive { overflow-x: auto; background: white; border-radius: 8px; box-shadow: 0 2px 5px rgba(0,0,0,0.05); }
.data-table { width: 100%; border-collapse: collapse; min-width: 900px; }
.data-table th { background: #f8f9fa; padding: 15px; color: #2c3e50; border-bottom: 2px solid #eee; text-align: left; }
.data-table td { padding: 12px 15px; border-bottom: 1px solid #eee; }

/* Text Utils */
.text-right { text-align: right; }
.text-center { text-align: center; color: #95a5a6; padding: 30px; }
.font-bold { font-weight: bold; }
.text-green { color: #27ae60; }
.text-red { color: #e74c3c; }

/* Badges */
.type-badge { padding: 3px 8px; border-radius: 4px; font-size: 11px; font-weight: bold; }
.in { background: #d4edda; color: #155724; }
.out { background: #f8d7da; color: #721c24; }

.status-badge { padding: 4px 10px; border-radius: 12px; font-size: 11px; }
.lunas { background: #e8f8f5; color: #1abc9c; border: 1px solid #1abc9c; }
.hutang { background: #fef9e7; color: #f1c40f; border: 1px solid #f1c40f; }

.btn-icon { background: none; border: none; cursor: pointer; }
.btn-icon.delete:hover { transform: scale(1.2); color: #e74c3c; }

@media (max-width: 600px) {
  .filter-group { flex-direction: column; align-items: stretch; }
  .date-input { width: 100%; box-sizing: border-box; }
}
</style>