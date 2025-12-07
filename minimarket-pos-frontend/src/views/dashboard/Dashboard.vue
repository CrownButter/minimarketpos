<template>
  <div class="flex h-screen bg-gray-100">
    <Sidebar />

    <main class="flex-1 ml-64 p-8 overflow-y-auto">
      <h1 class="text-3xl font-bold text-gray-800 mb-6">Dashboard Ringkasan</h1>

      <div v-if="loading" class="text-center py-20">
        <i class="fas fa-spinner fa-spin text-4xl text-primary"></i>
        <p class="mt-4 text-gray-500">Memuat data...</p>
      </div>

      <div v-else>
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          <div class="bg-white p-6 rounded-lg shadow border-l-4 border-blue-500">
            <div class="text-gray-500 text-sm font-bold uppercase">Penjualan Hari Ini</div>
            <div class="text-2xl font-bold text-gray-800 mt-2">{{ formatCurrency(stats.todaySales) }}</div>
          </div>
          <div class="bg-white p-6 rounded-lg shadow border-l-4 border-green-500">
            <div class="text-gray-500 text-sm font-bold uppercase">Profit Hari Ini</div>
            <div class="text-2xl font-bold text-gray-800 mt-2">{{ formatCurrency(stats.todayProfit) }}</div>
          </div>
          <div class="bg-white p-6 rounded-lg shadow border-l-4 border-purple-500">
            <div class="text-gray-500 text-sm font-bold uppercase">Total Transaksi</div>
            <div class="text-2xl font-bold text-gray-800 mt-2">{{ stats.todayOrders }}</div>
          </div>
          <div class="bg-white p-6 rounded-lg shadow border-l-4 border-red-500">
            <div class="text-gray-500 text-sm font-bold uppercase">Pengeluaran Hari Ini</div>
            <div class="text-2xl font-bold text-gray-800 mt-2">{{ formatCurrency(stats.todayCost) }}</div>
          </div>
        </div>

        <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
          
          <div class="bg-white p-6 rounded-lg shadow lg:col-span-2">
            <h3 class="text-lg font-bold text-gray-700 mb-4">Grafik Penjualan Bulanan</h3>
            <div class="h-80 relative">
              <Bar v-if="chartData.labels" :data="chartData" :options="chartOptions" />
              <div v-else class="flex items-center justify-center h-full text-gray-400">Tidak ada data grafik</div>
            </div>
          </div>

          <div class="bg-white p-6 rounded-lg shadow">
            <h3 class="text-lg font-bold text-gray-700 mb-4">5 Produk Terlaris</h3>
            <div class="overflow-y-auto h-80">
              <table class="w-full text-sm text-left">
                <thead class="text-xs text-gray-700 uppercase bg-gray-50 sticky top-0">
                  <tr>
                    <th class="px-4 py-3">Produk</th>
                    <th class="px-4 py-3 text-right">Qty</th>
                    <th class="px-4 py-3 text-right">Total</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="product in stats.topProducts" :key="product.productId" class="border-b hover:bg-gray-50">
                    <td class="px-4 py-3 font-medium text-gray-900 truncate max-w-[120px]" :title="product.productName">
                      {{ product.productName }}
                    </td>
                    <td class="px-4 py-3 text-right">{{ product.totalQuantity }}</td>
                    <td class="px-4 py-3 text-right">{{ formatCurrency(product.totalAmount) }}</td>
                  </tr>
                  <tr v-if="!stats.topProducts?.length">
                    <td colspan="3" class="text-center py-4 text-gray-500">Belum ada data</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import Sidebar from '@/components/Sidebar.vue';
import { ref, onMounted } from 'vue';
import api from '@/api/axios';
import { Chart as ChartJS, Title, Tooltip, Legend, BarElement, CategoryScale, LinearScale } from 'chart.js';
import { Bar } from 'vue-chartjs';

ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend);

const loading = ref(true);
const stats = ref({});
const chartData = ref({});
const chartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: { legend: { position: 'top' } }
};

onMounted(async () => {
  try {
    // Sesuai ReportController: GET /api/reports/dashboard
    const res = await api.get('/reports/dashboard');
    stats.value = res.data;
    setupChart(res.data.monthlySales, res.data.monthlyExpenses);
  } catch (error) {
    console.error("Gagal memuat dashboard:", error);
  } finally {
    loading.value = false;
  }
});

const setupChart = (salesMap, expenseMap) => {
  const sales = salesMap || {};
  const expenses = expenseMap || {};
  
  // Mengambil bulan unik dari kedua map dan mengurutkannya
  const allMonths = [...new Set([...Object.keys(sales), ...Object.keys(expenses)])].sort();

  const salesData = allMonths.map(m => sales[m] || 0);
  const expenseData = allMonths.map(m => expenses[m] || 0);

  chartData.value = {
    labels: allMonths,
    datasets: [
      { label: 'Pemasukan', backgroundColor: '#3498db', data: salesData },
      { label: 'Pengeluaran', backgroundColor: '#e74c3c', data: expenseData }
    ]
  };
};

const formatCurrency = (value) => {
  return new Intl.NumberFormat('id-ID', { style: 'currency', currency: 'IDR', minimumFractionDigits: 0 }).format(value || 0);
};
</script>