<script setup>
import { useAuthStore } from '@/stores/auth';
import { useRouter } from 'vue-router';

const authStore = useAuthStore();
const router = useRouter();

const handleLogout = () => {
  authStore.logout();
  router.push('/login');
};
</script>

<template>
  <div class="app-container">
    <aside class="sidebar">
      <div class="brand">POS SYSTEM</div>
      <nav>
        <router-link to="/">Dashboard</router-link>
        <router-link to="/stores">Toko / Cabang</router-link>
        <router-link to="/pos">Point of Sale</router-link>
        <router-link to="/products">Produk</router-link>
        <router-link to="/sales">Riwayat Penjualan</router-link>
        <router-link to="/expenses">Pengeluaran</router-link>
        <router-link to="/settings">Pengaturan</router-link>
      </nav>
    </aside>

    <div class="main-content">
      <header class="top-bar">
        <span>Selamat Datang, <b>{{ authStore.user?.username }}</b></span>
        <button @click="handleLogout" class="btn-logout">Logout</button>
      </header>

      <main class="page-content">
        <router-view /> </main>
    </div>
  </div>
</template>

<style scoped>
.app-container { display: flex; height: 100vh; }
.sidebar { width: 250px; background: #2c3e50; color: white; display: flex; flex-direction: column; }
.brand { padding: 20px; font-size: 20px; font-weight: bold; text-align: center; border-bottom: 1px solid #34495e; }
.sidebar nav a { padding: 15px 20px; color: #ecf0f1; text-decoration: none; display: block; }
.sidebar nav a:hover, .sidebar nav a.router-link-active { background: #34495e; }
.main-content { flex: 1; display: flex; flex-direction: column; background: #f4f6f9; }
.top-bar { background: white; padding: 15px 20px; display: flex; justify-content: space-between; align-items: center; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
.page-content { padding: 20px; overflow-y: auto; }
.btn-logout { background: #e74c3c; color: white; border: none; padding: 8px 15px; border-radius: 4px; cursor: pointer; }
</style>