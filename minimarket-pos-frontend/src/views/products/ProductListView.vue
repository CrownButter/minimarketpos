<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';

const products = ref([]);

onMounted(async () => {
  const { data } = await axios.get('http://localhost:8080/api/products');
  products.value = data;
});
</script>

<template>
  <div>
    <div class="header-action">
      <h1>Daftar Produk</h1>
      <router-link to="/products/create" class="btn-add">+ Tambah Produk</router-link>
    </div>
    <table class="data-table">
      <thead>
        <tr>
          <th>Kode</th>
          <th>Nama</th>
          <th>Kategori</th>
          <th>Harga</th>
          <th>Stok</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="p in products" :key="p.id">
          <td>{{ p.code }}</td>
          <td>{{ p.name }}</td>
          <td>{{ p.category }}</td>
          <td>{{ p.price }}</td>
          <td>{{ p.stock || 0 }}</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<style scoped>
.header-action { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.btn-add { background: #3498db; color: white; padding: 10px 15px; text-decoration: none; border-radius: 4px; }
.data-table { width: 100%; border-collapse: collapse; background: white; }
.data-table th, .data-table td { padding: 12px; border-bottom: 1px solid #eee; text-align: left; }
</style>