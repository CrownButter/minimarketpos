<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';

// State
const users = ref([]);
const showModal = ref(false);
const isEditing = ref(false);
const isLoading = ref(false);

// Form State (Sesuai RegisterRequest.java)
const form = ref({
  id: null,
  username: '',
  password: '', // Wajib saat create, opsional saat update
  firstname: '',
  lastname: '',
  email: '',
  phone: '',
  role: 'CASHIER' // Default role
});

// Roles yang tersedia
const roles = ['ADMIN', 'MANAGER', 'CASHIER'];

// --- API ACTIONS ---

// 1. Ambil Semua User
const fetchUsers = async () => {
  isLoading.value = true;
  try {
    // Asumsi endpoint UserController (standard REST pattern)
    // Jika UserController belum dibuat, biasanya endpoint ini ada di /api/users
    const { data } = await axios.get('http://localhost:8080/api/users');
    users.value = data;
  } catch (error) {
    console.error("Gagal memuat user", error);
    // Fallback error handling
    if (error.response?.status === 403) {
      alert("Akses ditolak. Hanya Admin yang bisa mengakses halaman ini.");
    }
  } finally {
    isLoading.value = false;
  }
};

// 2. Simpan User (Create / Update)
const saveUser = async () => {
  // Validasi sederhana
  if (!form.value.username || !form.value.firstname || !form.value.role) {
    alert("Username, Nama Depan, dan Role wajib diisi!");
    return;
  }

  if (!isEditing.value && !form.value.password) {
    alert("Password wajib diisi untuk pengguna baru!");
    return;
  }

  try {
    if (isEditing.value) {
      // Update: PUT /api/users/{id}
      // Backend UserService logic: Password hanya diupdate jika string tidak kosong
      await axios.put(`http://localhost:8080/api/users/${form.value.id}`, form.value);
      alert('Data pengguna berhasil diperbarui');
    } else {
      // Create: POST /api/auth/register atau /api/users (tergantung controller Anda)
      // Menggunakan endpoint register umum juga bisa, tapi biasanya admin punya endpoint sendiri
      await axios.post('http://localhost:8080/api/auth/register', form.value);
      alert('Pengguna baru berhasil dibuat');
    }
    closeModal();
    fetchUsers();
  } catch (error) {
    console.error(error);
    alert('Gagal menyimpan: ' + (error.response?.data?.message || error.message));
  }
};

// 3. Hapus User
const deleteUser = async (id) => {
  if (!confirm('Hapus pengguna ini? Akses login mereka akan hilang.')) return;

  try {
    await axios.delete(`http://localhost:8080/api/users/${id}`);
    alert('Pengguna dihapus');
    fetchUsers();
  } catch (error) {
    alert('Gagal menghapus pengguna.');
  }
};

// --- HELPER FUNCTIONS ---

const openModal = (user = null) => {
  if (user) {
    isEditing.value = true;
    form.value = {
      id: user.id,
      username: user.username,
      password: '', // Kosongkan password saat edit (biarkan user isi jika ingin ubah)
      firstname: user.firstname,
      lastname: user.lastname,
      email: user.email,
      phone: user.phone,
      role: user.role
    };
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
  form.value = {
    id: null,
    username: '',
    password: '',
    firstname: '',
    lastname: '',
    email: '',
    phone: '',
    role: 'CASHIER'
  };
};

const getRoleBadgeClass = (role) => {
  switch (role) {
    case 'ADMIN': return 'badge-admin';
    case 'MANAGER': return 'badge-manager';
    default: return 'badge-cashier';
  }
};

onMounted(() => {
  fetchUsers();
});
</script>

<template>
  <div class="user-page">
    <div class="page-header">
      <h1>Manajemen Pengguna</h1>
      <button @click="openModal()" class="btn-add">+ Tambah User</button>
    </div>

    <div class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th>Username</th>
            <th>Nama Lengkap</th>
            <th>Email</th>
            <th>Role</th>
            <th>Status</th>
            <th class="text-center">Aksi</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in users" :key="user.id">
            <td class="font-bold">{{ user.username }}</td>
            <td>{{ user.firstname }} {{ user.lastname }}</td>
            <td>{{ user.email || '-' }}</td>
            <td>
              <span class="badge" :class="getRoleBadgeClass(user.role)">
                {{ user.role }}
              </span>
            </td>
            <td>
              <span class="status-dot" :class="user.isActive ? 'active' : 'inactive'"></span>
              {{ user.isActive ? 'Aktif' : 'Non-Aktif' }}
            </td>
            <td class="text-center">
              <button @click="openModal(user)" class="btn-icon edit" title="Edit">✏️</button>
              <button @click="deleteUser(user.id)" class="btn-icon delete" title="Hapus">🗑️</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <div class="modal-header">
          <h2>{{ isEditing ? 'Edit Pengguna' : 'Tambah Pengguna Baru' }}</h2>
          <button @click="closeModal" class="btn-close">&times;</button>
        </div>

        <form @submit.prevent="saveUser" class="modal-form">

          <div class="form-row">
            <div class="form-group">
              <label>Username *</label>
              <input v-model="form.username" type="text" required :disabled="isEditing" />
            </div>
            <div class="form-group">
              <label>Role *</label>
              <select v-model="form.role" required>
                <option v-for="r in roles" :key="r" :value="r">{{ r }}</option>
              </select>
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>Nama Depan *</label>
              <input v-model="form.firstname" type="text" required />
            </div>
            <div class="form-group">
              <label>Nama Belakang</label>
              <input v-model="form.lastname" type="text" />
            </div>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>Email</label>
              <input v-model="form.email" type="email" />
            </div>
            <div class="form-group">
              <label>No. Telepon</label>
              <input v-model="form.phone" type="text" />
            </div>
          </div>

          <div class="form-group password-group">
            <label>Password {{ isEditing ? '(Biarkan kosong jika tidak diubah)' : '*' }}</label>
            <input v-model="form.password" type="password" :required="!isEditing" placeholder="******" />
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
.user-page { padding-bottom: 40px; }

/* Header */
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.btn-add { background: #3498db; color: white; padding: 10px 20px; border: none; border-radius: 4px; cursor: pointer; font-weight: bold; }

/* Table */
.table-container { background: white; border-radius: 8px; box-shadow: 0 2px 5px rgba(0,0,0,0.05); overflow-x: auto; }
.data-table { width: 100%; border-collapse: collapse; }
.data-table th, .data-table td { padding: 12px 15px; border-bottom: 1px solid #eee; text-align: left; }
.data-table th { background: #f8f9fa; color: #2c3e50; }
.font-bold { font-weight: bold; }
.text-center { text-align: center; }

/* Badges Role */
.badge { padding: 4px 8px; border-radius: 4px; font-size: 11px; font-weight: bold; color: white; }
.badge-admin { background-color: #e74c3c; }
.badge-manager { background-color: #f39c12; }
.badge-cashier { background-color: #27ae60; }

/* Status Dot */
.status-dot { display: inline-block; width: 8px; height: 8px; border-radius: 50%; margin-right: 5px; }
.active { background-color: #2ecc71; }
.inactive { background-color: #95a5a6; }

/* Actions */
.btn-icon { background: none; border: none; cursor: pointer; font-size: 1.1em; margin: 0 5px; }
.btn-icon.edit { color: #f39c12; }
.btn-icon.delete { color: #e74c3c; }

/* Modal */
.modal-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); display: flex; justify-content: center; align-items: center; z-index: 1000; }
.modal-content { background: white; width: 90%; max-width: 600px; border-radius: 8px; padding: 25px; box-shadow: 0 5px 15px rgba(0,0,0,0.3); }
.modal-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; border-bottom: 1px solid #eee; padding-bottom: 10px; }
.btn-close { background: none; border: none; font-size: 24px; cursor: pointer; }

.form-group { margin-bottom: 15px; }
.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 15px; }
label { display: block; margin-bottom: 5px; font-weight: bold; font-size: 14px; color: #34495e; }
input, select { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 4px; box-sizing: border-box; }
input:disabled { background-color: #f9f9f9; cursor: not-allowed; }

.password-group { background: #f9f9f9; padding: 10px; border-radius: 4px; border: 1px dashed #ddd; }

.modal-footer { display: flex; justify-content: flex-end; gap: 10px; margin-top: 20px; }
.btn-cancel { background: #95a5a6; color: white; border: none; padding: 10px 20px; border-radius: 4px; cursor: pointer; }
.btn-save { background: #3498db; color: white; border: none; padding: 10px 20px; border-radius: 4px; cursor: pointer; }

@media (max-width: 600px) {
  .form-row { grid-template-columns: 1fr; }
}
</style>