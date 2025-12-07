<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { Line } from 'vue-chartjs'; // Pastikan sudah install library
import { Chart as ChartJS, Title, Tooltip, Legend, LineElement, CategoryScale, LinearScale, PointElement } from 'chart.js';

ChartJS.register(Title, Tooltip, Legend, LineElement, CategoryScale, LinearScale, PointElement);

// State Data
const stats = ref({ customers: 0, products: 0, todaySales: 0, monthlyExp: 0 });
const chartData = ref({
  labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'],
  datasets: []
});
const chartOptions = { responsive: true, maintainAspectRatio: false };
const loaded = ref(false);

onMounted(async () => {
  try {
    // Ambil Data Dashboard Ringkas
    const { data } = await axios.get('/api/reports/dashboard');
    stats.value = data;

    // Ambil Data Chart (Monthly Sales & Expense)
    // Sesuai PDF: getyearstats
    const salesRes = await axios.get('/api/reports/sales/monthly?year=2024');
    const expRes = await axios.get('/api/reports/expenses/monthly?year=2024');

    // Format Data untuk Chart.js
    chartData.value.datasets = [
      {
        label: 'Revenue',
        backgroundColor: '#3498db',
        borderColor: '#3498db',
        data: Object.values(salesRes.data) // Asumsi backend return object {Jan: 100, Feb: 200...}
      },
      {
        label: 'Expense',
        backgroundColor: '#e74c3c',
        borderColor: '#e74c3c',
        data: Object.values(expRes.data)
      }
    ];
    loaded.value = true;

  } catch (e) { console.error("Error loading stats"); }
});
</script>

<template>
  <div class="dashboard-page">
    <h1>Dashboard Overview</h1>

    <div class="cards-container">
      <div class="stat-card bg-orange">
        <i class="icon">👥</i>
        <div class="info">
          <h3>{{ stats.todayOrders || 0 }}</h3> <span>Customers</span>
        </div>
      </div>
      <div class="stat-card bg-green">
        <i class="icon">📦</i>
        <div class="info">
          <h3>{{ stats.products || 0 }}</h3>
          <span>Products</span>
        </div>
      </div>
      <div class="stat-card bg-blue">
        <i class="icon">💰</i>
        <div class="info">
          <h3>Rp {{ (stats.todaySales || 0).toLocaleString() }}</h3>
          <span>Today Sales</span>
        </div>
      </div>
      <div class="stat-card bg-red">
        <i class="icon">📉</i>
        <div class="info">
          <h3>Rp {{ (stats.todayCost || 0).toLocaleString() }}</h3> <span>Today Expense</span>
        </div>
      </div>
    </div>

    <div class="chart-section">
      <h3>Monthly Statistics</h3>
      <div class="chart-container" v-if="loaded">
        <Line :data="chartData" :options="chartOptions" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.cards-container { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 20px; margin-bottom: 40px; }
.stat-card { padding: 20px; border-radius: 8px; color: white; display: flex; align-items: center; gap: 20px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }
.bg-orange { background: #f39c12; }
.bg-green { background: #27ae60; }
.bg-blue { background: #2980b9; }
.bg-red { background: #c0392b; }
.stat-card h3 { font-size: 28px; margin: 0; }
.icon { font-size: 40px; opacity: 0.8; }

.chart-section { background: white; padding: 20px; border-radius: 8px; height: 400px; }
.chart-container { height: 300px; }
</style>