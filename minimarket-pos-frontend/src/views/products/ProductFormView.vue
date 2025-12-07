<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';

const router = useRouter();
const isLoading = ref(false);
const errorMessage = ref('');

// State Form sesuai dengan ProductRequest.java di Backend
const form = ref({
  code: '',
  name: '',
  category: 'General', // Default value
  cost: 0,
  price: 0,
  description: '',
  tax: '0',
  taxmethod: 1, // 1=Inclusive, 0=Exclusive
  alertqt: 5,
  unit: 'pcs',
  type: 'standard', // standard, combo, digital, service
  supplier: ''
});

// Submit Handler
const submitForm = async () => {
  isLoading.value = true;
  errorMessage.value = '';

  try {
    // POST ke /api/products sesuai ProductController
    await axios.post('http://localhost:8080/api/products', form.value);

    alert('Produk berhasil ditambahkan!');
    router.push('/products'); // Kembali ke daftar produk
  } catch (error) {
    console.error(error);
    if (error.response && error.response.data) {
      // Menangani pesan error dari backend (misal: kode produk duplikat)
      errorMessage.value = error.response.data.message || 'Gagal menyimpan produk.';
    } else {
      errorMessage.value = 'Terjadi kesalahan jaringan.';
    }
  } finally {
    isLoading.value = false;
  }
};
</script>

<template>
  <div class="product-form-container">
    <div class="header">
      <h1>Tambah Produk Baru</h1>
      <button @click="$router.back()" class="btn-back">Kembali</button>
    </div>

    <div v-if="errorMessage" class="alert-error">
      {{ errorMessage }}
    </div>

    <form @submit.prevent="submitForm" class="form-card">

      <div class="form-row">
        <div class="form-group">
          <label>Kode Produk *</label>
          <input v-model="form.code" type="text" placeholder="Scan atau ketik kode..." required />
        </div>
        <div class="form-group">
          <label>Nama Produk *</label>
          <input v-model="form.name" type="text" required />
        </div>
      </div>

      <div class="form-row">
        <div class="form-group">
          <label>Kategori</label>
          <select v-model="form.category">
            <option value="General">General</option>
            <option value="Makanan">Makanan</option>
            <option value="Minuman">Minuman</option>
            <option value="Elektronik">Elektronik</option>
          </select>
        </div>
        <div class="form-group">
          <label>Tipe Produk</label>
          <select v-model="form.type">
            <option value="standard">Standard</option>
            <option value="service">Jasa (Service)</option>
            <option value="combo">Paket (Combo)</option>
          </select>
        </div>
      </div>

      <div class="form-row">
        <div class="form-group">
          <label>Harga Beli (Cost) *</label>
          <input v-model.number="form.cost" type="number" min="0" required />
        </div>
        <div class="form-group">
          <label>Harga Jual (Price) *</label>
          <input v-model.number="form.price" type="number" min="0" required />
        </div>
      </div>

      <div class="form-row">
        <div class="form-group">
          <label>Peringatan Stok Rendah (Alert Qt)</label>
          <input v-model.number="form.alertqt" type="number" />
        </div>
        <div class="form-group">
          <label>Satuan (Unit)</label>
          <input v-model="form.unit" type="text" placeholder="pcs, kg, liter..." />
        </div>
      </div>

      <div class="form-row">
        <div class="form-group">
          <label>Pajak (%)</label>
          <input v-model="form.tax" type="text" placeholder="0" />
        </div>
        <div class="form-group">
          <label>Metode Pajak</label>
          <select v-model.number="form.taxmethod">
            <option value="1">Inclusive (Termasuk Harga)</option>
            <option value="0">Exclusive (Ditambah Harga)</option>
          </select>
        </div>
      </div>

      <div class="form-group">
        <label>Deskripsi</label>
        <textarea v-model="form.description" rows="3"></textarea>
      </div>

      <div class="form-actions">
        <button type="submit" class="btn-save" :disabled="isLoading">
          {{ isLoading ? 'Menyimpan...' : 'Simpan Produk' }}
        </button>
      </div>

    </form>
  </div>
</template>

<style scoped>
.product-form-container { max-width: 800px; margin: 0 auto; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.btn-back { background: #95a5a6; color: white; padding: 8px 15px; border: none; border-radius: 4px; cursor: pointer; }

.form-card { background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 5px rgba(0,0,0,0.05); }
.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-bottom: 15px; }
.form-group { margin-bottom: 15px; }
.form-group label { display: block; margin-bottom: 5px; font-weight: bold; color: #34495e; font-size: 14px; }

input, select, textarea {
  width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 4px; font-size: 14px; box-sizing: border-box;
}
input:focus, select:focus, textarea:focus { border-color: #3498db; outline: none; }

.alert-error { background: #f8d7da; color: #721c24; padding: 15px; border-radius: 4px; margin-bottom: 20px; border: 1px solid #f5c6cb; }

.form-actions { margin-top: 30px; text-align: right; }
.btn-save {
  background: #27ae60; color: white; padding: 12px 30px; border: none; border-radius: 4px; cursor: pointer; font-size: 16px; font-weight: bold; transition: background 0.3s;
}
.btn-save:hover { background: #219150; }
.btn-save:disabled { background: #95a5a6; cursor: not-allowed; }

/* Responsive untuk layar kecil */
@media (max-width: 600px) {
  .form-row { grid-template-columns: 1fr; gap: 0; }
}
</style>