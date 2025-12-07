<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth'; // Pastikan store ini ada (lihat di bawah)

const router = useRouter();
const authStore = useAuthStore();

// State Form
const username = ref('');
const password = ref('');
const errorMessage = ref('');
const isLoading = ref(false);

// State Settings (Simulasi $this->setting dari PHP)
// Nanti bisa diambil dari API /api/settings/default
const settings = ref({
  companyname: 'Minimarket POS',
  logo: '/img/logo.png', // <--- Path yang benar untuk file di folder public
  background: '/img/bg-login.jpg', // Pastikan file ini juga ada di public/img
});

// Logic Login
const handleLogin = async () => {
  isLoading.value = true;
  errorMessage.value = '';

  try {
    // Memanggil action login di Pinia Store
    await authStore.login({
      username: username.value,
      password: password.value
    });

    // Redirect ke Dashboard jika sukses
    router.push('/');
  } catch (error) {
    errorMessage.value = 'Login failed. Please check your username and password.';
  } finally {
    isLoading.value = false;
  }
};
</script>

<template>
  <div class="login-page" :style="{ backgroundImage: `url(${settings.background})` }">
    <div class="overlay">
      <div class="login-modal-container">

        <div class="logo-wrapper">
          <img v-if="settings.logo" :src="settings.logo" alt="logo" />
          <img v-else src="/img/logo.png" alt="default logo" />
        </div>

        <h1 class="digital-title">LOGIN</h1>

        <div v-if="errorMessage" class="alert-error">
          {{ errorMessage }}
        </div>

        <form @submit.prevent="handleLogin" class="login-form">
          <input
            type="text"
            v-model="username"
            placeholder="Username"
            required
            autofocus
          />
          <input
            type="password"
            v-model="password"
            placeholder="Password"
            required
          />

          <button type="submit" class="login-submit" :disabled="isLoading">
            {{ isLoading ? 'Loading...' : 'Login' }}
          </button>
        </form>

        <div class="login-help">
          &copy; {{ new Date().getFullYear() }} {{ settings.companyname }}
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Import Font Digital (Pastikan file .ttf ada di public/fonts) */
@font-face {
  font-family: "digital";
  src: url('/fonts/digital.ttf'); /* Sesuaikan path font Anda */
}

/* Layout Utama */
.login-page {
  position: fixed;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  background-position: center;
  background-repeat: no-repeat;
  background-size: cover;
  display: flex;
  justify-content: center;
  align-items: center;
}

/* Overlay Gelap agar tulisan terbaca */
.overlay {
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  justify-content: center;
  align-items: center;
}

/* Container Login (Kotak Putih) */
.login-modal-container {
  padding: 30px;
  max-width: 350px;
  width: 100%;
  background-color: #F7F7F7;
  margin: 0 auto;
  border-radius: 2px;
  box-shadow: 0px 2px 2px rgba(0, 0, 0, 0.3);
  overflow: hidden;
  text-align: center;
}

/* Logo */
.logo-wrapper img {
  max-width: 150px;
  margin: 0 auto 20px;
  display: block;
}

/* Judul Digital (Sesuai request style="color:lime" size="70") */
.digital-title {
  font-family: "digital", sans-serif;
  color: lime;
  font-size: 70px;
  margin: 0 0 20px 0;
  text-shadow: 2px 2px 4px #000000;
  line-height: 1;
}

/* Form Input Style */
.login-form input[type="text"],
.login-form input[type="password"] {
  height: 44px;
  font-size: 16px;
  width: 100%;
  margin-bottom: 10px;
  appearance: none;
  background: #fff;
  border: 1px solid #d9d9d9;
  border-top: 1px solid #c0c0c0;
  padding: 0 8px;
  box-sizing: border-box;
  border-radius: 4px;
}

.login-form input:hover {
  border: 1px solid #b9b9b9;
  border-top: 1px solid #a0a0a0;
  box-shadow: inset 0 1px 2px rgba(0,0,0,0.1);
}

/* Submit Button */
.login-submit {
  border: 0;
  width: 100%;
  padding: 10px 0;
  font-size: 16px;
  font-weight: bold;
  color: #fff;
  background-color: #4d90fe; /* Bootstrap Primary Blue ish */
  border-radius: 4px;
  cursor: pointer;
  transition: background 0.3s;
  text-shadow: 0 1px rgba(0,0,0,0.3);
}

.login-submit:hover {
  background-color: #357ae8;
}

.login-submit:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

/* Error Message */
.alert-error {
  color: #dc3545;
  background-color: #f8d7da;
  border: 1px solid #f5c6cb;
  padding: 10px;
  margin-bottom: 15px;
  border-radius: 4px;
  font-size: 14px;
}

/* Footer Help */
.login-help {
  font-size: 12px;
  color: #666;
  margin-top: 15px;
}
</style>