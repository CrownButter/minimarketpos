<script setup>
import { ref, onMounted } from 'vue';
// PENTING: Import 'api' yang sudah kita buat (bukan 'axios' biasa)
import api from '@/api/axios';
import { useAuthStore } from '@/stores/auth';

const authStore = useAuthStore();

// ... variabel state lainnya ...
const stores = ref([]);
const isLoading = ref(false);

onMounted(async () => {
  await loadStores();
});

// --- FUNGSI LOAD STORES ---
const loadStores = async () => {
  isLoading.value = true;
  try {
    // vvvvv TEKAN KODE INI DI SINI vvvvv
    const { data } = await api.get('/v1/stores/active');
    // ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

    stores.value = data;
  } catch (e) {
    console.error("Gagal memuat toko", e);
    // Error handling...
  } finally {
    isLoading.value = false;
  }
};

// ... sisa kode lainnya ...
</script>