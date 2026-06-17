import { createApp } from "vue";
import App from "./App.vue";
import router from "./router";
import "./style.css";
import "./dashboard.css";
import "./site-pages.css";
import "./service-detail.css";
import "./consultation-modal.css";
import "./home-extras.css";
import "./admin-upload.css";
import "./enterprise.css";
import "./site-polish.css";

createApp(App).use(router).mount("#app");
