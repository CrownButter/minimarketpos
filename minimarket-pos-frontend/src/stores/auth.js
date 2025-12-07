import { defineStore } from 'pinia';
import axios from 'axios';

// Sesuaikan URL Backend Anda
const API_URL = 'http://localhost:8080/api/auth';

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: JSON.parse(localStorage.getItem('user')) || null,
    token: localStorage.getItem('token') || null,
  }),

  getters: {
    isAuthenticated: (state) => !!state.token,
  },

  actions: {
    async login(credentials) {
          try {
            const response = await axios.post(`${API_URL}/login`, credentials);

            // --- DEBUGGING START ---
            console.log("Response dari Backend:", response.data);
            // Cek di Console browser, apakah ada properti 'token'?
            // --- DEBUGGING END ---

            const { token, ...userData } = response.data;

            // Pastikan token tidak undefined
            if (!token) {
                throw new Error("Token tidak ditemukan dalam response backend!");
            }

            this.token = token;
            this.user = userData;

            localStorage.setItem('token', token);
            localStorage.setItem('user', JSON.stringify(userData));

            // Update header axios global (opsional, karena kita sudah punya interceptor di api/axios.js)
            axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;

          } catch (error) {
            console.error("Login Error:", error);
            throw error;
      }
    },

    logout() {
      this.user = null;
      this.token = null;
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      delete axios.defaults.headers.common['Authorization'];
    }
  }
});