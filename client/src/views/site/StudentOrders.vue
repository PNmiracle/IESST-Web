<script setup>
import { onMounted, reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { api } from "../../api";
import { showNotice } from "../../stores/notice";
import { studentSession } from "../../stores/studentSession";

const router = useRouter();
const orders = ref([]);
const invoices = ref([]);
const progressByOrder = ref({});
const filesByOrder = ref({});
const expandedOrderId = ref(null);
const loading = ref(true);
const error = ref("");
const filters = reactive({ status: "ALL", page: 1, size: 6 });
const pagination = reactive({ total: 0, totalPages: 0 });
const statusOptions = [
  { value: "ALL", label: "全部" },
  { value: "NEW", label: "待确认" },
  { value: "IN_PROGRESS", label: "服务中" },
  { value: "COMPLETED", label: "已完成" },
  { value: "CANCELLED", label: "已取消" },
];

async function loadOrders(loadInvoices = false) {
  loading.value = true;
  error.value = "";
  try {
    const loggedIn = await studentSession.restore(true);
    if (!loggedIn) {
      router.replace({ path: "/student/login", query: { redirect: "/student/orders" } });
      return;
    }
    const tasks = [api.studentOrders(filters)];
    if (loadInvoices) tasks.push(api.studentInvoices());
    const [orderPage, invoiceItems] = await Promise.all(tasks);
    orders.value = orderPage.items;
    pagination.total = orderPage.total;
    pagination.totalPages = orderPage.totalPages;
    if (invoiceItems) invoices.value = invoiceItems;
  } catch (exception) {
    error.value = exception.message || "订单加载失败";
  } finally {
    loading.value = false;
  }
}

async function chooseStatus(status) {
  filters.status = status;
  filters.page = 1;
  expandedOrderId.value = null;
  await loadOrders();
}

async function changePage(page) {
  if (page < 1 || page > pagination.totalPages || page === filters.page) return;
  filters.page = page;
  expandedOrderId.value = null;
  await loadOrders();
}

async function logout() {
  await studentSession.logout();
  router.replace("/student/login");
}

async function toggleDetails(orderId) {
  if (expandedOrderId.value === orderId) {
    expandedOrderId.value = null;
    return;
  }
  expandedOrderId.value = orderId;
  try {
    const [progress, files] = await Promise.all([
      progressByOrder.value[orderId] || api.studentOrderProgress(orderId),
      filesByOrder.value[orderId] || api.studentOrderFiles(orderId),
    ]);
    progressByOrder.value = { ...progressByOrder.value, [orderId]: progress };
    filesByOrder.value = { ...filesByOrder.value, [orderId]: files };
  } catch (exception) {
    showNotice(exception.message || "订单详情加载失败", true);
  }
}

async function downloadFile(orderId, file) {
  try {
    const blob = await api.downloadStudentOrderFile(orderId, file.id);
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = file.fileName;
    link.click();
    URL.revokeObjectURL(url);
  } catch (exception) {
    showNotice(exception.message || "文件下载失败", true);
  }
}

function formatDate(value) {
  return value ? value.replace("T", " ").slice(0, 16) : "";
}

function paymentLabel(status, amount) {
  if (Number(amount || 0) <= 0 && status !== "PAID") return "待报价";
  return { PAID: "已付款", UNPAID: "待付款", REFUNDED: "已退款" }[status] || "待确认";
}

function orderStatusLabel(status) {
  return { NEW: "待确认", IN_PROGRESS: "服务中", COMPLETED: "已完成", CANCELLED: "已取消" }[status] || "处理中";
}

function invoiceStatusLabel(status) {
  return { PENDING: "申请中", ISSUED: "已开票", REJECTED: "已驳回" }[status] || "待处理";
}

function invoiceTypeLabel(type) {
  return { ELECTRONIC: "电子发票", PAPER: "纸质发票", SPECIAL: "增值税专用发票" }[type] || type || "发票";
}

onMounted(() => loadOrders(true));
</script>

<template>
  <section class="student-page student-orders-page">
    <div class="student-orders-head">
      <div><span class="eyebrow">ORDER CENTER</span><h1>订单中心</h1><p>{{ studentSession.state.displayName || studentSession.state.mobile }}，在这里查看评估、服务进度、稿件文件和发票。</p></div>
      <div class="student-head-actions"><RouterLink class="primary compact" to="/submit">提交新稿件</RouterLink><button class="ghost" @click="logout">退出登录</button></div>
    </div>

    <div class="student-order-toolbar">
      <div class="segmented"><button v-for="option in statusOptions" :key="option.value" :class="{ active: filters.status === option.value }" @click="chooseStatus(option.value)">{{ option.label }}</button></div>
      <span>共 {{ pagination.total }} 个订单</span>
    </div>

    <div v-if="loading" class="empty-state">订单加载中…</div>
    <div v-else-if="error" class="empty-state error-state">{{ error }}<button class="ghost" @click="loadOrders">重新加载</button></div>
    <div v-else-if="orders.length" class="student-order-list">
      <article v-for="order in orders" :key="order.id" class="card student-order-card">
        <div class="student-order-main">
          <div class="student-order-kicker"><span>{{ order.orderNo }}</span><span>{{ order.targetType || "咨询" }}</span></div>
          <h3>{{ order.title }}</h3>
          <p>{{ order.notes || "编辑正在确认需求，后续说明会同步到这里。" }}</p>
          <small>编辑：{{ order.consultantName || "待分配" }} · 创建时间：{{ formatDate(order.createdAt) }}</small>

          <div v-if="expandedOrderId === order.id" class="student-order-details">
            <section><h4>服务进度</h4><ol v-if="progressByOrder[order.id]?.length" class="student-progress"><li v-for="item in progressByOrder[order.id]" :key="item.id"><b>{{ item.stageLabel }}</b><span>{{ item.progressNote }}</span><small>{{ item.operatorName || "系统" }} · {{ formatDate(item.createdAt) }}</small></li></ol><p v-else class="student-detail-empty">暂无公开进度</p></section>
            <section><h4>相关文件</h4><div v-if="filesByOrder[order.id]?.length" class="student-file-list"><button v-for="file in filesByOrder[order.id]" :key="file.id" type="button" @click="downloadFile(order.id, file)"><span>{{ file.fileName }}</span><small>{{ Math.max(1, Math.round(file.size / 1024)) }} KB · 下载</small></button></div><p v-else class="student-detail-empty">暂无可下载文件</p></section>
          </div>
        </div>
        <div class="student-order-meta">
          <b>{{ order.currentStage || "需求确认" }}</b>
          <small>{{ orderStatusLabel(order.orderStatus) }} · {{ paymentLabel(order.paymentStatus, order.amount) }}</small>
          <strong>{{ Number(order.amount || 0) > 0 ? `${order.currencyCode} ${Number(order.amount).toFixed(2)}` : "报价确认后显示" }}</strong>
          <button class="ghost" @click="toggleDetails(order.id)">{{ expandedOrderId === order.id ? "收起详情" : "查看详情" }}</button>
        </div>
      </article>
      <footer v-if="pagination.totalPages > 1" class="student-pagination"><span>第 {{ filters.page }} / {{ pagination.totalPages }} 页</span><div><button class="ghost" :disabled="filters.page <= 1" @click="changePage(filters.page - 1)">上一页</button><button class="ghost" :disabled="filters.page >= pagination.totalPages" @click="changePage(filters.page + 1)">下一页</button></div></footer>
    </div>
    <div v-else class="empty-state"><b>{{ filters.status === "ALL" ? "暂时没有订单" : "该状态下没有订单" }}</b><span>提交免费评估后，编辑确认的进度会显示在这里。</span><RouterLink class="primary" to="/submit">免费评估稿件</RouterLink></div>

    <section class="student-invoices">
      <div class="student-orders-head compact"><div><span class="eyebrow">INVOICES</span><h2>发票记录</h2></div></div>
      <div v-if="!loading && invoices.length" class="student-order-list"><article v-for="invoice in invoices" :key="invoice.id" class="card student-invoice-card"><div><span>{{ invoiceStatusLabel(invoice.status) }}</span><h3>{{ invoice.invoiceTitle }}</h3><p>{{ invoiceTypeLabel(invoice.invoiceType) }} · {{ invoice.receiverEmail || invoice.receiverPhone || "未填写接收方式" }}</p></div><strong>{{ invoice.invoiceAmount ? Number(invoice.invoiceAmount).toFixed(2) : "0.00" }} 元</strong></article></div>
      <div v-else-if="!loading" class="empty-state"><b>暂无发票记录</b><span>付款订单申请开票后，会在这里显示处理状态。</span></div>
    </section>
  </section>
</template>
