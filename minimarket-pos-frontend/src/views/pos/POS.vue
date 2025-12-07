<template>
  <div class="flex h-screen bg-gray-100 overflow-hidden">
    <div class="w-2/3 flex flex-col border-r border-gray-300">
      <div class="bg-white p-4 shadow flex justify-between items-center">
        <div class="relative w-1/2">
          <input v-model="searchQuery" @keyup.enter="searchProducts" type="text" placeholder="Cari Produk / Scan Barcode..." class="w-full pl-10 pr-4 py-2 border rounded-lg focus:ring-2 focus:ring-primary">
          <i class="fas fa-search absolute left-3 top-3 text-gray-400"></i>
        </div>
        <button @click="router.push('/stores')" class="bg-gray-700 text-white px-4 py-2 rounded">Switch Toko</button>
      </div>

      <div class="flex-1 overflow-y-auto p-4 grid grid-cols-3 lg:grid-cols-4 gap-4 content-start">
        <div v-for="product in products" :key="product.id" @click="addToCart(product)" class="bg-white rounded-lg shadow p-3 cursor-pointer hover:shadow-lg transition flex flex-col h-40">
          <div class="flex-1 flex items-center justify-center bg-gray-50 rounded mb-2">
             <i class="fas fa-box text-3xl text-gray-300"></i>
          </div>
          <h4 class="font-bold text-sm truncate">{{ product.name }}</h4>
          <div class="flex justify-between items-center mt-1">
            <span class="text-primary font-bold">Rp {{ product.price }}</span>
            <span class="text-xs bg-gray-200 px-2 py-0.5 rounded">Stok: {{ product.stock }}</span>
          </div>
        </div>
      </div>
    </div>

    <div class="w-1/3 flex flex-col bg-white">
      <div class="p-4 bg-secondary text-white flex justify-between items-center">
        <h2 class="font-bold"><i class="fas fa-shopping-cart mr-2"></i> Keranjang</h2>
        <button @click="clearCart" class="text-red-300 hover:text-red-100 text-sm">Hapus Semua</button>
      </div>

      <div class="flex-1 overflow-y-auto p-4" v-html="cartHtml" @click="handleCartClick"></div>

      <div class="bg-gray-50 p-4 border-t border-gray-300">
        <div class="flex justify-between text-lg mb-2">
          <span>Total Item:</span>
          <span class="font-bold">{{ totalItems }}</span>
        </div>
        <div class="flex justify-between text-2xl font-bold text-primary mb-4">
          <span>Total:</span>
          <span>{{ subtotal }}</span>
        </div>

        <button @click="showPaymentModal = true" class="w-full bg-green-600 text-white py-3 rounded font-bold hover:bg-green-700">
          <i class="fas fa-money-bill-wave mr-2"></i> BAYAR
        </button>
      </div>
    </div>

    <div v-if="showPaymentModal" class="fixed inset-0 bg-black bg-opacity-60 flex items-center justify-center z-50">
        <div class="bg-white w-1/2 p-6 rounded shadow-lg">
            <h2 class="text-2xl font-bold mb-4">Checkout</h2>
            <div class="grid grid-cols-2 gap-4 mb-4">
                <div><label>Total Tagihan</label><input type="text" :value="subtotal" disabled class="w-full bg-gray-100 p-2 rounded"></div>
                <div><label>Bayar</label><input type="number" v-model="amountPaid" class="w-full border p-2 rounded focus:ring-2 focus:ring-green-500"></div>
            </div>
            <div class="flex justify-end gap-2">
                <button @click="showPaymentModal = false" class="px-4 py-2 bg-gray-300 rounded">Batal</button>
                <button @click="processPayment" class="px-6 py-2 bg-green-600 text-white rounded">Proses</button>
            </div>
        </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import api from '@/api/axios';
import { useAuthStore } from '@/stores/auth';
import { useRouter } from 'vue-router';
import Swal from 'sweetalert2';

const authStore = useAuthStore();
const router = useRouter();
const products = ref([]);
const searchQuery = ref('');
const cartHtml = ref('');
const subtotal = ref('0.00');
const totalItems = ref(0);
const showPaymentModal = ref(false);
const amountPaid = ref(0);

onMounted(() => {
  if (!authStore.activeRegister) {
    router.push('/stores');
    return;
  }
  fetchProducts();
  fetchCart();
});

const fetchProducts = async () => {
  try {
    const res = await api.get('/products');
    products.value = res.data;
  } catch (e) { console.error(e); }
};

const searchProducts = async () => {
    if(!searchQuery.value) return fetchProducts();
    try {
        const res = await api.get(`/products/search?term=${searchQuery.value}`);
        products.value = res.data;
        if(products.value.length === 1 && /^\d+$/.test(searchQuery.value)) {
            addToCart(products.value[0]);
            searchQuery.value = '';
        }
    } catch (e) { console.error(e); }
}

const fetchCart = async () => {
  try {
    const regId = authStore.activeRegister.id;
    [cite_start]// Backend return HTML [cite: 5888]
    const resHtml = await api.get(`/pos/load-cart?registerId=${regId}`);
    cartHtml.value = resHtml.data;

    // Get Subtotal
    const resSub = await api.get(`/pos/subtotal?registerId=${regId}`);
    subtotal.value = resSub.data;

    // Get Total Item
    const resCount = await api.get(`/pos/total-items?registerId=${regId}`);
    totalItems.value = resCount.data;
  } catch (e) { console.error(e); }
};

const addToCart = async (product) => {
  try {
    await api.post('/pos/add-product', {
      productId: product.id,
      registerId: authStore.activeRegister.id
    });
    fetchCart();
  } catch (error) {
    Swal.fire('Error', 'Gagal tambah produk', 'error');
  }
};

const clearCart = async () => {
    await api.post(`/pos/reset?registerId=${authStore.activeRegister.id}`);
    fetchCart();
}

// Logic untuk menangani tombol delete di dalam HTML string dari backend
const handleCartClick = (event) => {
    if (event.target.innerText === 'Delete') {
       const parent = event.target.closest('.cart-item');
       if(parent) {
           const id = parent.getAttribute('data-id');
           deleteCartItem(id);
       }
    }
};

const deleteCartItem = async (id) => {
    await api.delete(`/pos/delete/${id}`);
    fetchCart();
}

const processPayment = async () => {
    try {
        const payload = {
            clientId: 1,
            clientname: "Walk-in Customer",
            subtotal: parseFloat(subtotal.value),
            tax: "0", taxamount: 0, discount: "0", discountamount: 0,
            total: parseFloat(subtotal.value),
            paid: amountPaid.value,
            totalitems: totalItems.value,
            paidmethod: 'cash',
            registerId: authStore.activeRegister.id
        };

        const res = await api.post('/pos/complete-sale', payload);
        showPaymentModal.value = false;

        Swal.fire({
            title: 'Struk',
            html: `<pre class="text-left text-xs">${res.data}</pre>`,
            width: '400px'
        });
        amountPaid.value = 0;
        fetchCart();
    } catch (error) {
        Swal.fire('Error', 'Transaksi Gagal', 'error');
    }
}
</script>