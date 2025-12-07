<script setup>
// Tidak ada logic khusus
</script>

<template>
  <div class="not-found-container">
    <div class="content">
      <h1 class="error-code">404</h1>
      <h2>Halaman Tidak Ditemukan</h2>
      <p>Maaf, halaman yang Anda cari tidak ada atau telah dipindahkan.</p>

      <router-link to="/" class="btn-home">
        Kembali ke Dashboard
      </router-link>
    </div>
  </div>
</template>

<style scoped>
.not-found-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f4f6f9;
  text-align: center;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.content {
  background: white;
  padding: 50px;
  border-radius: 8px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.1);
  max-width: 500px;
  width: 90%;
}

.error-code {
  font-size: 100px;
  font-weight: bold;
  color: #e74c3c;
  margin: 0;
  line-height: 1;
}

h2 {
  margin: 20px 0 10px;
  color: #2c3e50;
  font-size: 24px;
}

p {
  color: #7f8c8d;
  margin-bottom: 30px;
}

.btn-home {
  display: inline-block;
  background-color: #3498db;
  color: white;
  padding: 12px 25px;
  border-radius: 5px;
  text-decoration: none;
  font-weight: bold;
  transition: background-color 0.3s;
}

.btn-home:hover {
  background-color: #2980b9;
}
</style>