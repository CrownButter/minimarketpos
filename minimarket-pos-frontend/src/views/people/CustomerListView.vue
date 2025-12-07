<script setup>
import { ref, computed, onMounted } from 'vue';
import axios from 'axios';

// State
const customers = ref([]);
const searchQuery = ref('');
const showModal = ref(false);
const isEditing = ref(false);
const isLoading = ref(false);

// Form State
const form = ref({
  id: null,
  name: '',
  email: '',
  phone: '',
  address: '',
  discount: '0' // Diskon default string "0" sesuai DTO
});

// --- API ACTIONS ---

// 1. Ambil Data Pelanggan
const fetchCustomers = async () => {
  isLoading.value = true;
  try {
    const { data } = await axios.get('http://localhost:8080/api/customers');
    customers.value = data;
  } catch (error) {
    console.error(error);
    alert('Gagal memuat data pelanggan.');
  } finally {
    isLoading.value = false;
  }
};

// 2. Simpan Data (Create / Update)
const saveCustomer = async () => {
  if (!form.value.name || !form.value.phone) {
    alert("Nama dan Telepon wajib diisi!");
    return;
  }

  try {
    if (isEditing.value) {
      // Update: PUT /api/customers/{id}
      await axios.put(`http://localhost:8080/api/customers/${form.value.id}`, form.value);
      alert('Pelanggan berhasil diperbarui');
    } else {
      // Create: POST /api/customers
      await axios.post('http://localhost:8080/api/customers', form.value);
      alert('Pelanggan berhasil ditambahkan');
    }
    closeModal();
    fetchCustomers();
  } catch (error) {
    console.error(error);
    alert('Gagal menyimpan data: ' + (error.response?.data?.message || error.message));
  }
};

// 3. Hapus Data
const deleteCustomer = async (id) => {
  if (!confirm('Apakah Anda yakin ingin menghapus pelanggan ini?')) return;

  try {
    // Delete: DELETE /api/customers/{id}
    await axios.delete(`http://localhost:8080/api/customers/${id}`);
    alert('Pelanggan dihapus');
    fetchCustomers();
  } catch (error) {
    alert('Gagal menghapus pelanggan.');
  }
};

// --- HELPER FUNCTIONS ---

// Filter Pencarian (Client-side)
const filteredCustomers = computed(() => {
  if (!searchQuery.value) return customers.value;
  const lowerSearch = searchQuery.value.toLowerCase();
  return customers.value.filter(c =>
    c.name.toLowerCase().includes(lowerSearch) ||
    (c.phone && c.phone.includes(lowerSearch)) ||
    (c.email && c.email.toLowerCase().includes(lowerSearch))
  );
});

// Modal Logic
const openModal = (customer = null) => {
  if (customer) {
    isEditing.value = true;
    // Copy object agar tidak reaktif langsung ke list
    form.value = { ...customer };
  } else {
    isEditing.value = false;
    resetForm();
  }
  showModal.value = true;
};

const closeModal = () => {
  showModal.value = false;
  resetForm();
};

const resetForm = () => {
  form.value = { id: null, name: '', email: '', phone: '', address: '', discount: '0' };
};

onMounted(() => {
  fetchCustomers();
});
</script>

<template>
  <div class="customer-page">
    <div class="page-header">
      <h1>Data Pelanggan</h1>
      <button @click="openModal()" class="btn-add">+ Tambah Pelanggan</button>
    </div>

    <div class="search-bar">
      <input
        v-model="searchQuery"
        type="text"
        placeholder="Cari nama, telepon, atau email..."
        class="search-input"
      />
    </div>

    <div class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th>Nama</th>
            <th>Kontak</th>
            <th>Alamat</th>
            <th>Diskon</th>
            <th class="text-center">Aksi</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="filteredCustomers.length === 0">
            <td colspan="5" class="text-center empty-state">Tidak ada data pelanggan.</td>
          </tr>
          <tr v-for="customer in filteredCustomers" :key="customer.id">
            <td class="font-bold">{{ customer.name }}</td>
            <td>
              <div class="contact-info">
                <span>📞 {{ customer.phone }}</span>
                <span v-if="customer.email" class="email">✉️ {{ customer.email }}</span>
              </div>
            </td>
            <td>{{ customer.address || '-' }}</td>
            <td>
              <span v-if="customer.discount && customer.discount !== '0'" class="badge-discount">
                {{ customer.discount }}%
              </span>
              <span v-else>-</span>
            </td>
            <td class="text-center">
              <button @click="openModal(customer)" class="btn-icon edit" title="Edit">✏️</button>
              <button @click="deleteCustomer(customer.id)" class="btn-icon delete" title="Hapus">🗑️</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <div class="modal-header">
          <h2>{{ isEditing ? 'Edit Pelanggan' : 'Tambah Pelanggan Baru' }}</h2>
          <button @click="closeModal" class="btn-close">&times;</button>
        </div>

        <form @submit.prevent="saveCustomer" class="modal-form">
          <div class="form-group">
            <label>Nama Lengkap *</label>
            <input v-model="form.name" type="text" required placeholder="Nama Pelanggan" />
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>Nomor Telepon *</label>
              <input v-model="form.phone" type="text" required placeholder="0812..." />
            </div>
            <div class="form-group">
              <label>Email</label>
              <input v-model="form.email" type="email" placeholder="email@contoh.com" />
            </div>
          </div>

          <div class="form-group">
            <label>Alamat</label>
            <textarea v-model="form.address" rows="2" placeholder="Alamat lengkap..."></textarea>
          </div>

          <div class="form-group">
            <label>Diskon Khusus (%)</label>
            <input v-model="form.discount" type="text" placeholder="0" />
            <small>Masukkan angka persentase (misal: 10 untuk 10%)</small>
          </div>

          <div class="modal-footer">
            <button type="button" @click="closeModal" class="btn-cancel">Batal</button>
            <button type="submit" class="btn-save">Simpan</button>
          </div>
        </form>
      </div>
    </div>

  </div>
</template>

<style scoped>
.customer-page { padding-bottom: 40px; }

/* Header */
.page-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 20px;
}
.btn-add {
  background: #27ae60; color: white; padding: 10px 20px;
  border: none; border-radius: 4px; cursor: pointer; font-weight: bold;
}
.btn-add:hover { background: #219150; }

/* Search */
.search-bar { margin-bottom: 15px; }
.search-input {
  width: 100%; max-width: 400px; padding: 10px;
  border: 1px solid #ddd; border-radius: 4px;
}

/* Table */
.table-container {
  background: white; border-radius: 8px;
  box-shadow: 0 2px 5px rgba(0,0,0,0.05); overflow-x: auto;
}
.data-table { width: 100%; border-collapse: collapse; }
.data-table th, .data-table td { padding: 12px 15px; border-bottom: 1px solid #eee; text-align: left; }
.data-table th { background: #f8f9fa; color: #2c3e50; }

.contact-info { display: flex; flex-direction: column; font-size: 13px; }
.email { color: #7f8c8d; font-size: 12px; }
.font-bold { font-weight: bold; color: #2c3e50; }
.text-center { text-align: center; }
.empty-state { padding: 30px; color: #95a5a6; }

.badge-discount {
  background: #e74c3c; color: white; padding: 3px 8px;
  border-radius: 10px; font-size: 11px; font-weight: bold;
}

/* Action Buttons */
.btn-icon { background: none; border: none; cursor: pointer; font-size: 1.1em; margin: 0 5px; }
.btn-icon.edit { color: #f39c12; }
.btn-icon.delete { color: #e74c3c; }

/* Modal Styling */
.modal-overlay {
  position: fixed; top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(0,0,0,0.5); display: flex; justify-content: center; align-items: center; z-index: 1000;
}
.modal-content {
  background: white; width: 90%; max-width: 500px;
  border-radius: 8px; padding: 20px; box-shadow: 0 5px 15px rgba(0,0,0,0.3);
}
.modal-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; border-bottom: 1px solid #eee; padding-bottom: 10px; }
.btn-close { background: none; border: none; font-size: 24px; cursor: pointer; }

.form-group { margin-bottom: 15px; }
.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 15px; }
label { display: block; margin-bottom: 5px; font-weight: bold; font-size: 14px; }
input, textarea { width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 4px; box-sizing: border-box; }
small { color: #7f8c8d; font-size: 12px; }

.modal-footer { display: flex; justify-content: flex-end; gap: 10px; margin-top: 20px; }
.btn-cancel { background: #95a5a6; color: white; border: none; padding: 8px 15px; border-radius: 4px; cursor: pointer; }
.btn-save { background: #3498db; color: white; border: none; padding: 8px 15px; border-radius: 4px; cursor: pointer; }

@media (max-width: 600px) {
  .form-row { grid-template-columns: 1fr; }
}
</style>