import { createRouter, createWebHashHistory, createWebHistory } from "vue-router";
import { setUnauthorizedHandler } from "../api";
import { session } from "../stores/session";
import SiteHome from "../views/site/SiteHome.vue";
import ScholarRecruitment from "../views/site/ScholarRecruitment.vue";
import SiteLayout from "../layouts/SiteLayout.vue";
import JournalCatalog from "../views/site/JournalCatalog.vue";
import JournalDetail from "../views/site/JournalDetail.vue";
import ServiceDetail from "../views/site/ServiceDetail.vue";
import AboutPage from "../views/site/AboutPage.vue";
import SubmissionPage from "../views/site/SubmissionPage.vue";
import StudentLogin from "../views/site/StudentLogin.vue";
import StudentRegister from "../views/site/StudentRegister.vue";
import StudentOrders from "../views/site/StudentOrders.vue";
import AdminLayout from "../layouts/AdminLayout.vue";
import AdminLogin from "../views/admin/AdminLogin.vue";
import AdminDashboard from "../views/admin/AdminDashboard.vue";
import BannerManagement from "../views/admin/BannerManagement.vue";
import JournalManagement from "../views/admin/JournalManagement.vue";
import SubmissionManagement from "../views/admin/SubmissionManagement.vue";
import ContentManagement from "../views/admin/ContentManagement.vue";
import GovernanceManagement from "../views/admin/GovernanceManagement.vue";
import StudentManagement from "../views/admin/StudentManagement.vue";
import OrderManagement from "../views/admin/OrderManagement.vue";

const router = createRouter({
  history: import.meta.env.VITE_STATIC_DEMO === "true" ? createWebHashHistory(import.meta.env.BASE_URL) : createWebHistory(),
  scrollBehavior(to) {
    if (to.hash) return { el: to.hash, behavior: "smooth" };
    return { top: 0 };
  },
  routes: [
    {
      path: "/",
      component: SiteLayout,
      children: [
        { path: "", name: "site-home", component: SiteHome },
        { path: "scholar-recruitment", name: "scholar-recruitment", component: ScholarRecruitment },
        { path: "SCI", name: "sci-catalog", component: JournalCatalog, props: { journalType: "SCI" } },
        { path: "SCI/:id", name: "sci-detail", component: JournalDetail, props: { journalType: "SCI" } },
        { path: "EI", name: "ei-catalog", component: JournalCatalog, props: { journalType: "EI" } },
        { path: "EI/:id", name: "ei-detail", component: JournalDetail, props: { journalType: "EI" } },
        { path: "journals", redirect: "/SCI" },
        { path: "journals/:id", redirect: (to) => `/SCI/${to.params.id}` },
        { path: "services/:kind", name: "service-detail", component: ServiceDetail },
        { path: "about", name: "about", component: AboutPage },
        { path: "submit", name: "submit", component: SubmissionPage },
        { path: "student/login", name: "student-login", component: StudentLogin },
        { path: "student/register", name: "student-register", component: StudentRegister },
        { path: "student/orders", name: "student-orders", component: StudentOrders },
      ],
    },
    { path: "/admin/login", name: "admin-login", component: AdminLogin, meta: { guestOnly: true } },
    {
      path: "/admin",
      component: AdminLayout,
      meta: { requiresAuth: true },
      children: [
        { path: "", redirect: "/admin/dashboard" },
        { path: "dashboard", name: "admin-dashboard", component: AdminDashboard },
        { path: "banners", name: "admin-banners", component: BannerManagement },
        { path: "journals", name: "admin-journals", component: JournalManagement },
        { path: "submissions", name: "admin-submissions", component: SubmissionManagement },
        { path: "content", name: "admin-content", component: ContentManagement },
        { path: "students", name: "admin-students", component: StudentManagement },
        { path: "orders", name: "admin-orders", component: OrderManagement },
        { path: "governance", name: "admin-governance", component: GovernanceManagement },
      ],
    },
    { path: "/:pathMatch(.*)*", redirect: "/" },
  ],
});

router.beforeEach(async (to) => {
  if (to.meta.requiresAuth || to.meta.guestOnly) await session.restore();
  if (to.meta.requiresAuth && !session.isLoggedIn.value) return { name: "admin-login", query: { redirect: to.fullPath } };
  if (to.meta.guestOnly && session.isLoggedIn.value) return { name: "admin-dashboard" };
});

setUnauthorizedHandler(() => {
  session.clear();
  router.replace({ name: "admin-login", query: { expired: "1" } });
});

export default router;
