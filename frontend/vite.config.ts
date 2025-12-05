// vite.config.ts
import { defineConfig } from 'vite';
import tailwindcss from '@tailwindcss/vite'
import flowbiteReact from "flowbite-react/plugin/vite";

export default defineConfig({
    define: {
        global: {},
    },
    plugins: [tailwindcss(), flowbiteReact()],
});