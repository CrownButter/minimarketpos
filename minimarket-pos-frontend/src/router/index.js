import { createRouter, createWebHistory } from 'vue-router'

// Fungsi simulasi cek auth (Gantilah dengan logic Pinia/Vuex/LocalStorage Anda)
const isAuthenticated = () => {
  return localStorage.getItem('token') !== null;
}

const routes = [
  // --- PUBLIC ROUTES ---
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/auth/LoginView.vue'),
    meta: { guest: true }
  },

  // --- PROTECTED ROUTES ---
  {
    path: '/',
    component: () => import('../layouts/MainLayout.vue'), // Layout dengan Sidebar/Header
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'Dashboard',
        component: () => import('../views/dashboard/DashboardView.vue'),
        // Sesuai ReportController: dashboard statistics
      },
      {
        path: '/stores',
        name: 'Stores',
        component: () => import('../views/stores/StoreListView.vue'),
        // Sesuai StoreController: Pilih toko sebelum masuk POS
      },
      {
        path: '/pos/:storeId?',
        name: 'POS',
        component: () => import('../views/pos/PosView.vue'),
        // Sesuai PosController: Halaman kasir utama
        // Parameter storeId opsional jika user sudah memilih toko
      },
      {
        path: '/products',
        name: 'Products',
        component: () => import('../views/products/ProductListView.vue'),
        // Sesuai ProductController: List & Manage Products
      },
      {
        path: '/products/create',
        name: 'ProductCreate',
        component: () => import('../views/products/ProductFormView.vue'),
      },
      {
        path: '/sales',
        name: 'SalesHistory',
        component: () => import('../views/sales/SalesListView.vue'),
        // Sesuai SalesController: Riwayat transaksi
      },
      {
        path: '/expenses',
        name: 'Expenses',
        component: () => import('../views/finance/ExpenseListView.vue'),
        // Sesuai ExpenseController
      },
      {
        path: '/cashflow',
        name: 'Cashflow',
        component: () => import('../views/finance/CashflowListView.vue'),
        // Sesuai CashflowController
      },
      {
        path: '/customers',
        name: 'Customers',
        component: () => import('../views/people/CustomerListView.vue'),
        // Sesuai CustomerController
      },
      {
        path: '/users',
        name: 'Users',
        component: () => import('../views/people/UserListView.vue'),
        meta: { role: 'ADMIN' } // Proteksi tambahan untuk admin
      },
      {
        path: '/settings',
        name: 'Settings',
        component: () => import('../views/settings/SettingView.vue'),
        // Sesuai SettingsController
        meta: { role: 'ADMIN' }
      }
    ]
  },

  // --- CATCH ALL (404) ---
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/errors/NotFoundView.vue')
  }
]

const router = createRouter({
  // Menggunakan WebHistory untuk URL bersih (tanpa hash #)
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

// --- NAVIGATION GUARDS ---
router.beforeEach((to, from, next) => {
  const isLoggedIn = isAuthenticated();
  const userRole = localStorage.getItem('role'); // Contoh ambil role

  // 1. Cek Login
  if (to.meta.requiresAuth && !isLoggedIn) {
    return next({ name: 'Login' });
  }

  // 2. Cek Guest (Jika sudah login, jangan boleh masuk halaman login lagi)
  if (to.meta.guest && isLoggedIn) {
    return next({ name: 'Dashboard' });
  }

  // 3. Cek Role (Opsional, sesuaikan dengan SecurityConfig.java Anda)
  if (to.meta.role && to.meta.role !== userRole) {
    // Redirect ke dashboard atau halaman 'Unauthorized' jika role tidak cocok
    return next({ name: 'Dashboard' });
  }

  next();
})

export default router