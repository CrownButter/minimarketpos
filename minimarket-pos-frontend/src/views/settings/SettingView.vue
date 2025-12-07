<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';

// Components (Asumsi Anda nanti memecah komponen, tapi disini saya gabung biar mudah)
import UserListView from '@/views/people/UserListView.vue';
import StoreListView from '@/views/stores/StoreListView.vue';

const activeTab = ref('settings'); // settings, users, stores, warehouses
const form = ref({});
const isLoading = ref(false);

const fetchSettings = async () => {
  isLoading.value = true;
  try {
    const { data } = await axios.get('/api/settings/default');
    form.value = data || {};
  } catch (e) { console.error(e); }
  isLoading.value = false;
};

const saveSettings = async () => {
  // Logic save seperti sebelumnya
  try {
    if(form.value.id) await axios.put(`/api/settings/${form.value.id}`, form.value);
    else await axios.post('/api/settings', form.value);
    alert("Saved!");
  } catch(e) { alert("Error saving"); }
};

onMounted(() => fetchSettings());
</script>

<template>
  <div class="setting-wrapper">
    <div class="tabs-header">
      <button :class="{active: activeTab==='settings'}" @click="activeTab='settings'">⚙️ General Settings</button>
      <button :class="{active: activeTab==='users'}" @click="activeTab='users'">👥 Users</button>
      <button :class="{active: activeTab==='stores'}" @click="activeTab='stores'">🏪 Stores</button>
      </div>

    <div class="tab-content">

      <div v-if="activeTab === 'settings'" class="settings-tab">
        <h2>System Settings</h2>
        <div class="form-grid">
          <div class="form-group">
            <label>Company Name</label>
            <input v-model="form.companyname" class="form-input">
          </div>
          <div class="form-group">
            <label>Currency</label>
            <input v-model="form.currency" class="form-input">
          </div>
          <div class="form-group">
            <label>Tax (%)</label>
            <input v-model="form.tax" class="form-input">
          </div>
          <div class="form-group full">
            <label>Receipt Header</label>
            <textarea v-model="form.receiptheader" class="form-input" rows="3"></textarea>
          </div>
          <div class="form-group full">
            <label>Receipt Footer</label>
            <textarea v-model="form.receiptfooter" class="form-input" rows="3"></textarea>
          </div>
        </div>
        <button @click="saveSettings" class="btn-save">Update Settings</button>
      </div>

      <div v-if="activeTab === 'users'">
        <UserListView />
      </div>

      <div v-if="activeTab === 'stores'">
        <StoreListView />
      </div>

    </div>
  </div>
</template>

<style scoped>
.setting-wrapper { padding: 20px; background: white; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.05); }
.tabs-header { display: flex; border-bottom: 2px solid #eee; margin-bottom: 20px; }
.tabs-header button { padding: 15px 25px; background: none; border: none; font-size: 16px; cursor: pointer; color: #7f8c8d; border-bottom: 3px solid transparent; }
.tabs-header button.active { color: #2c3e50; border-bottom-color: #3498db; font-weight: bold; }

.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-bottom: 20px; }
.form-group.full { grid-column: 1 / -1; }
.form-input { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 4px; }
.btn-save { background: #2980b9; color: white; padding: 10px 20px; border: none; border-radius: 4px; cursor: pointer; }
</style>