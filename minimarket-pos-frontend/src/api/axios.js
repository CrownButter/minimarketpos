import api from '@/api/axios';
import { useAuthStore } from '@/stores/auth';

const api = axios.create({
    baseURL: 'http://localhost:8080/api',
    headers: {
        'Content-Type': 'application/json',
    }
});

// Request Interceptor: Sisipkan Token
api.interceptors.request.use(config => {
    const authStore = useAuthStore();
    if (authStore.token) {
        config.headers.Authorization = `Bearer ${authStore.token}`;
    }
    return config;
});

// Response Interceptor: Handle Error
api.interceptors.response.use(
    response => response,
    error => {
        // Jika error 401 (Unauthorized) DAN BUKAN saat sedang Login
        if (error.response && error.response.status === 401 && !error.config.url.includes('/auth/login')) {
            const authStore = useAuthStore();
            authStore.logout();
            window.location.href = '/login';
        }
        return Promise.reject(error);
    }
);

export default api;