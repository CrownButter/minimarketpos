<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';

const sales = ref([]);
const startDate = ref('');
const endDate = ref('');
const isLoading = ref(false);

// Format Mata Uang (IDR)
const formatCurrency = (value) => {
  if (value === null || value === undefined) return 'Rp 0';
  return new Intl.NumberFormat('id-ID', { style: 'currency', currency: 'IDR' }).format(value);
};

// Format Tanggal
const formatDate = (dateString) => {
  if (!dateString) return '-';
  return new Date(dateString).toLocaleString('id-ID');
};

// Ambil Data Penjualan
const fetchSales = async () => {
  isLoading.value = true;
  try {
    let url = 'http://localhost:8080/api/sales';

    // Jika filter tanggal diisi
    if (startDate.value && endDate.value) {
      // Backend request param: start & end (LocalDateTime ISO format)
      // Kita perlu memastikan formatnya sesuai ISO (YYYY-MM-DDTHH:mm:ss)
      const startISO = new Date(startDate.value).toISOString().split('.')[0];
      const endISO = new Date(endDate.value).toISOString().split('.')[0];
      url = `http://localhost:8080/api/sales/date-range?start=${startISO}&end=${endISO}`;
    }

    const { data } = await axios.get(url);
    sales.value = data;
  } catch (error) {
    console.error("Gagal mengambil data penjualan", error);
    alert("Gagal memuat data penjualan.");
  } finally {
    isLoading.value = false;
  }
};

// Hapus Penjualan (Hanya Admin)
const deleteSale = async (id) => {
  if (!confirm('Apakah Anda yakin ingin menghapus transaksi ini? Stok tidak akan dikembalikan otomatis (tergantung logika backend).')) return;

  try {
    await axios.delete(`http://localhost:8080/api/sales/${id}`);
    alert('Transaksi berhasil dihapus');
    fetchSales(); // Refresh data
  } catch (error) {
    alert('Gagal menghapus: ' + (error.response?.data?.message || error.message));
  }
};

// Load awal
onMounted(() => {
  fetchSales();
});
</script>

<template>
  <div class="sales-page">
    <div class="header-action">
      <h1>Riwayat Penjualan</h1>

      <div class="filter-box">
        <input type="datetime-local" v-model="startDate" class="date-input" />
        <span>s/d</span>
        <input type="datetime-local" v-model="endDate" class="date-input" />
        <button @click="fetchSales" class="btn-filter" :disabled="isLoading">
          {{ isLoading ? 'Loading...' : 'Filter' }}
        </button>
        <button @click="fetchSales" class="btn-reset" v-if="startDate || endDate">Reset</button>
      </div>
    </div>

    <div class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Tanggal</th>
            <th>Pelanggan</th>
            <th>Register</th>
            <th>Total</th>
            <th>Dibayar</th>
            <th>Status</th>
            <th>Aksi</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="sales.length === 0">
            <td colspan="8" class="text-center">Tidak ada data transaksi.</td>
          </tr>
          <tr v-for="sale in sales" :key="sale.id">
            <td>#{{ sale.id }}</td>
            <td>{{ formatDate(sale.createdAt) }}</td>
            <td>{{ sale.clientname || 'Umum' }}</td>
            <td>Reg-{{ sale.registerId || '-' }}</td> <td class="font-bold">{{ formatCurrency(sale.total) }}</td>
            <td>{{ formatCurrency(sale.paid) }}</td>
            <td>
              <span class="badge" :class="sale.status === 0 ? 'paid' : 'unpaid'">
                {{ sale.status === 0 ? 'Lunas' : 'Belum Lunas' }}
              </span>
            </td>
            <td>
              <button class="btn-action btn-view" title="Lihat Detail">👁️</button>
              <button @click="deleteSale(sale.id)" class="btn-action btn-delete" title="Hapus">🗑️</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
.sales-page { padding-bottom: 50px; }

.header-action {
  display: flex;
  flex-direction: column;
  gap: 15px;
  margin-bottom: 20px;
}

@media (min-width: 768px) {
  .header-action {
    flex-direction: row;
    justify-content: space-between;
    align-items: center;
  }
}

.filter-box {
  display: flex;
  gap: 10px;
  align-items: center;
  background: white;
  padding: 10px;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.date-input {
  padding: 5px 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.btn-filter {
  background: #3498db;
  color: white;
  border: none;
  padding: 6px 15px;
  border-radius: 4px;
  cursor: pointer;
}

.btn-reset {
  background: #95a5a6;
  color: white;
  border: none;
  padding: 6px 15px;
  border-radius: 4px;
  cursor: pointer;
}

.table-container {
  overflow-x: auto;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 5px rgba(0,0,0,0.05);
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th, .data-table td {
  padding: 12px 15px;
  text-align: left;
  border-bottom: 1px solid #eee;
}

.data-table th {
  background-color: #f8f9fa;
  font-weight: 600;
  color: #2c3e50;
}

.text-center { text-align: center; color: #7f8c8d; padding: 20px; }
.font-bold { font-weight: bold; }

/* Status Badges */
.badge {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: bold;
}
.paid { background-color: #d4edda; color: #155724; }
.unpaid { background-color: #f8d7da; color: #721c24; }

/* Action Buttons */
.btn-action {
  border: none;
  background: none;
  cursor: pointer;
  font-size: 16px;
  margin-right: 5px;
  transition: transform 0.2s;
}
.btn-action:hover { transform: scale(1.2); }
.btn-view { color: #3498db; }
.btn-delete { color: #e74c3c; }
</style>