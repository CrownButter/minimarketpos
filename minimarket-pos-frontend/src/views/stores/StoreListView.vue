<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
// PENTING: Ganti import axios biasa dengan api custom kita
import api from '@/api/axios';

const stores = ref([]);
const router = useRouter();
const isLoading = ref(false);

onMounted(async () => {
  await fetchStores();
});

const fetchStores = async () => {
  isLoading.value = true;
  try {
    // Gunakan 'api.get' dan hapus URL lengkap (cukup path-nya saja)
    const { data } = await api.get('/v1/stores');
    stores.value = data;
  } catch (error) {
    console.error(error);
    if (error.response && error.response.status === 403) {
      alert("Sesi berakhir. Silakan login kembali.");
    }
  } finally {
    isLoading.value = false;
  }
};

const selectStore = (storeId) => {
  router.push(`/pos/${storeId}`);
};
</script>

<template>
  <div class="store-page">
    <h1>Pilih Toko / Cabang</h1>

    <div v-if="isLoading">Memuat data...</div>

    <div v-else class="store-list">
      <div v-for="store in stores" :key="store.id" class="store-card">
        <h3>{{ store.name }}</h3>
        <p>{{ store.address }}</p>
        <p>{{ store.phone }}</p>

        <span class="badge" :class="store.status === 1 ? 'active' : 'inactive'">
          {{ store.status === 1 ? 'Aktif' : 'Tidak Aktif' }}
        </span>

        <button
          @click="selectStore(store.id)"
          class="btn-select"
          :disabled="store.status !== 1"
        >
          {{ store.status === 1 ? 'Masuk Kasir' : 'Toko Tutup' }}
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.store-page { padding: 20px; }
.store-list { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 20px; margin-top: 20px; }
.store-card { background: white; padding: 25px; border-radius: 8px; border: 1px solid #eee; box-shadow: 0 2px 5px rgba(0,0,0,0.05); text-align: center; }
.store-card h3 { margin-top: 0; color: #2c3e50; }
.btn-select { margin-top: 15px; width: 100%; padding: 12px; background: #3498db; color: white; border: none; border-radius: 4px; cursor: pointer; font-weight: bold; }
.btn-select:disabled { background: #95a5a6; cursor: not-allowed; }
.badge { display: inline-block; padding: 5px 10px; font-size: 12px; border-radius: 15px; margin: 10px 0; }
.active { background: #d4edda; color: #155724; border: 1px solid #c3e6cb; }
.inactive { background: #f8d7da; color: #721c24; border: 1px solid #f5c6cb; }
</style>