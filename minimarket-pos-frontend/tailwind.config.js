/** @type {import('tailwindcss').Config} */
export default {
  // Pastikan baris content ini persis seperti ini:
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: '#3498db',
        secondary: '#2c3e50',
        success: '#27ae60',
        danger: '#e74c3c'
      }
    },
  },
  plugins: [],
}