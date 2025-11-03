// vite.config.ts
import { defineConfig } from 'vite';
import tailwindcss from '@tailwindcss/vite'

export default defineConfig({
    define: {
        global: {},
    },
    plugins: [
        tailwindcss(),
    ],
});