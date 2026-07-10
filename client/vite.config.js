import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";

export default defineConfig({
  base: process.env.VITE_BASE_PATH || "/",
  plugins: [
    vue({
      template: {
        // Keep /images/... URLs as public paths. The production bundle ships
        // those assets directly, while the development proxy below can serve them.
        transformAssetUrls: { includeAbsolute: false },
      },
    }),
  ],
  server: {
    proxy: {
      "/api": "http://127.0.0.1:8080",
      // The repository keeps images in the deployable static bundle. During local
      // development, serve them from the existing Pages deployment so that a
      // partial source checkout still renders exactly like the live site.
      "/images": {
        target: "https://pnmiracle.github.io/IESST-Web",
        changeOrigin: true,
      },
    },
  },
});
