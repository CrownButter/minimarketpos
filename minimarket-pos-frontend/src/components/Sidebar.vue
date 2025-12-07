<template>
  <aside class="w-64 bg-gray-900 text-white flex flex-col h-screen fixed left-0 top-0 overflow-y-auto z-10">
    <div class="p-6 text-2xl font-bold border-b border-gray-700 flex items-center gap-2">
      <i class="fas fa-cash-register text-green-500"></i>
      <span>MiniPOS</span>
    </div>

    <nav class="flex-1 p-4 space-y-2">
      <router-link to="/" class="flex items-center gap-3 py-3 px-4 hover:bg-gray-700 rounded transition" active-class="bg-primary">
        <i class="fas fa-tachometer-alt w-6"></i> Dashboard
      </router-link>

      <router-link to="/pos" class="flex items-center gap-3 py-3 px-4 hover:bg-gray-700 rounded transition" active-class="bg-primary">
        <i class="fas fa-shopping-cart w-6"></i> Kasir (POS)
      </router-link>

      <router-link to="/stores" class="flex items-center gap-3 py-3 px-4 hover:bg-gray-700 rounded transition" active-class="bg-primary">
        <i class="fas fa-store w-6"></i> Ganti Toko
      </router-link>

      <router-link to="/settings" class="flex items-center gap-3 py-3 px-4 hover:bg-gray-700 rounded transition" active-class="bg-primary">
        <i class="fas fa-cog w-6"></i> Pengaturan
      </router-link>
    </nav>

    <div class="p-4 border-t border-gray-700">
      <button @click="handleLogout" class="flex items-center gap-3 py-2 px-4 w-full text-left hover:bg-red-600 rounded transition text-red-300 hover:text-white">
        <i class="fas fa-sign-out-alt w-6"></i> Logout
      </button>
    </div>
  </aside>
</template>

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