<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';

const expenses = ref([]);
const startDate = ref('');
const endDate = ref('');
const isLoading = ref(false);

// State untuk Ringkasan
const summary = ref({
  monthlyTotal: 0,
  unpaidTotal: 0
});

// Format Mata Uang
const formatCurrency = (value) => {
  if (value === null || value === undefined) return 'Rp 0';
  return new Intl.NumberFormat('id-ID', { style: 'currency', currency: 'IDR' }).format(value);
};

// Format Tanggal
const formatDate = (dateArrayOrString) => {
  if (!dateArrayOrString) return '-';
  // Backend Spring Boot kadang mengembalikan array [YYYY, MM, DD] untuk LocalDate
  if (Array.isArray(dateArrayOrString)) {
    return new Date(dateArrayOrString[0], dateArrayOrString[1] - 1, dateArrayOrString[2]).toLocaleDateString('id-ID');
  }
  return new Date(dateArrayOrString).toLocaleDateString('id-ID');
};

// Ambil Ringkasan Data (Total Bulanan & Unpaid)
const fetchSummary = async () => {
  try {
    const today = new Date();
    // Endpoint: /api/expenses/monthly-total?year=...&month=...
    const monthlyRes = await axios.get(`http://localhost:8080/api/expenses/monthly-total?year=${today.getFullYear()}&month=${today.getMonth() + 1}`);
    summary.value.monthlyTotal = monthlyRes.data;

    // Endpoint: /api/expenses/unpaid/total
    const unpaidRes = await axios.get('http://localhost:8080/api/expenses/unpaid/total');
    summary.value.unpaidTotal = unpaidRes.data;
  } catch (e) {
    console.error("Gagal memuat ringkasan", e);
  }
};

// Ambil Daftar Pengeluaran
const fetchExpenses = async () => {
  isLoading.value = true;
  try {
    let url = 'http://localhost:8080/api/expenses';

    // Logic Filter Date Range
    if (startDate.value && endDate.value) {
      // Endpoint: /api/expenses/date-range?start=...&end=...
      url = `http://localhost:8080/api/expenses/date-range?start=${startDate.value}&end=${endDate.value}`;
    }

    const { data } = await axios.get(url);
    expenses.value = data;
  } catch (error) {
    console.error("Error fetching expenses", error);
    alert("Gagal memuat data pengeluaran.");
  } finally {
    isLoading.value = false;
  }
};

// Hapus Pengeluaran
const deleteExpense = async (id) => {
  if (!confirm('Hapus data pengeluaran ini?')) return;

  try {
    // Endpoint: DELETE /api/expenses/{id}
    await axios.delete(`http://localhost:8080/api/expenses/${id}`);
    alert('Data berhasil dihapus');
    fetchExpenses(); // Refresh List
    fetchSummary();  // Refresh Summary
  } catch (error) {
    alert('Gagal menghapus: ' + (error.response?.data?.message || error.message));
  }
};

onMounted(() => {
  fetchExpenses();
  fetchSummary();
});
</script>

<template>
  <div class="expense-page">

    <div class="header-section">
      <h1>Data Pengeluaran</h1>

      <div class="summary-cards">
        <div class="card bg-blue">
          <span>Total Bulan Ini</span>
          <h3>{{ formatCurrency(summary.monthlyTotal) }}</h3>
        </div>
        <div class="card bg-orange">
          <span>Belum Lunas</span>
          <h3>{{ formatCurrency(summary.unpaidTotal) }}</h3>
        </div>
      </div>
    </div>

    <div class="action-bar">
      <div class="filter-group">
        <input type="date" v-model="startDate" class="date-input" />
        <span>s/d</span>
        <input type="date" v-model="endDate" class="date-input" />
        <button @click="fetchExpenses" class="btn-primary" :disabled="isLoading">
          {{ isLoading ? 'Loading...' : 'Filter' }}
        </button>
        <button @click="fetchExpenses" class="btn-secondary" v-if="startDate || endDate">Reset</button>
      </div>

      <button class="btn-add" @click="$router.push('/expenses/create')">+ Catat Pengeluaran</button>
    </div>

    <div class="table-responsive">
      <table class="data-table">
        <thead>
          <tr>
            <th>Tanggal</th>
            <th>Referensi</th>
            <th>Kategori</th>
            <th>Toko</th>
            <th>Catatan</th>
            <th>Jumlah</th>
            <th>Status</th>
            <th>Aksi</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="expenses.length === 0">
            <td colspan="8" class="text-center">Belum ada data pengeluaran.</td>
          </tr>
          <tr v-for="expense in expenses" :key="expense.id">
            <td>{{ formatDate(expense.date) }}</td>
            <td>{{ expense.reference }}</td>
            <td>{{ expense.categoryName || '-' }}</td> <td>{{ expense.storeName || 'Pusat' }}</td> <td class="note-cell">{{ expense.note || '-' }}</td>
            <td class="amount-cell">{{ formatCurrency(expense.amount) }}</td>
            <td>
              <span class="badge" :class="expense.lunas ? 'lunas' : 'hutang'">
                {{ expense.lunas ? 'Lunas' : 'Hutang' }}
              </span>
            </td>
            <td>
              <button @click="deleteExpense(expense.id)" class="btn-icon delete" title="Hapus">🗑️</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
.expense-page { padding-bottom: 40px; }

/* Header & Summary */
.header-section { margin-bottom: 25px; }
.summary-cards { display: flex; gap: 15px; margin-top: 15px; flex-wrap: wrap; }
.card {
  flex: 1; min-width: 200px;
  padding: 20px; border-radius: 8px; color: white;
  box-shadow: 0 4px 6px rgba(0,0,0,0.1);
}
.bg-blue { background: linear-gradient(135deg, #3498db, #2980b9); }
.bg-orange { background: linear-gradient(135deg, #e67e22, #d35400); }
.card span { font-size: 14px; opacity: 0.9; }
.card h3 { font-size: 24px; margin: 5px 0 0; }

/* Action Bar */
.action-bar {
  display: flex; justify-content: space-between; align-items: center;
  background: white; padding: 15px; border-radius: 8px; margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05); flex-wrap: wrap; gap: 15px;
}
.filter-group { display: flex; gap: 10px; align-items: center; }
.date-input { padding: 8px; border: 1px solid #ddd; border-radius: 4px; }

/* Buttons */
.btn-primary { background: #3498db; color: white; border: none; padding: 8px 15px; border-radius: 4px; cursor: pointer; }
.btn-secondary { background: #95a5a6; color: white; border: none; padding: 8px 15px; border-radius: 4px; cursor: pointer; }
.btn-add { background: #27ae60; color: white; border: none; padding: 10px 20px; border-radius: 4px; cursor: pointer; font-weight: bold; }
.btn-icon { background: none; border: none; cursor: pointer; font-size: 1.1em; transition: transform 0.2s; }
.btn-icon:hover { transform: scale(1.2); }
.btn-icon.delete { color: #e74c3c; }

/* Table */
.table-responsive { overflow-x: auto; background: white; border-radius: 8px; box-shadow: 0 2px 5px rgba(0,0,0,0.05); }
.data-table { width: 100%; border-collapse: collapse; min-width: 800px; }
.data-table th { background: #f8f9fa; text-align: left; padding: 15px; color: #2c3e50; border-bottom: 2px solid #eee; }
.data-table td { padding: 12px 15px; border-bottom: 1px solid #eee; vertical-align: middle; }
.amount-cell { font-weight: bold; color: #2c3e50; }
.note-cell { max-width: 200px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; color: #7f8c8d; }
.text-center { text-align: center; padding: 30px; color: #95a5a6; }

/* Badges */
.badge { padding: 5px 10px; border-radius: 20px; font-size: 12px; font-weight: bold; }
.lunas { background: #d4edda; color: #155724; }
.hutang { background: #f8d7da; color: #721c24; }

@media (max-width: 768px) {
  .action-bar { flex-direction: column; align-items: stretch; }
  .filter-group { flex-direction: column; }
  .date-input { width: 100%; box-sizing: border-box; }
}
</style>