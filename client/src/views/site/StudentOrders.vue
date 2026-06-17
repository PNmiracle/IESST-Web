<script setup>
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { api } from "../../api";
import { studentSession } from "../../stores/studentSession";

const router = useRouter();
const orders = ref([]);
const invoices = ref([]);
const progressByOrder = ref({});
const expandedOrderId = ref(null);
const loading = ref(true);
const error = ref("");

async function loadOrders() {
  loading.value = true;
  error.value = "";
  try {
    const loggedIn = await studentSession.restore(true);
    if (!loggedIn) {
      router.replace({ path: "/student/login", query: { redirect: "/student/orders" } });
      return;
    }
    const [orderItems, invoiceItems] = await Promise.all([api.studentOrders(), api.studentInvoices()]);
    orders.value = orderItems;
    invoices.value = invoiceItems;
  } catch (exception) {
    error.value = exception.message || "订单加载失败";
  } finally {
    loading.value = false;
  }
}

async function logout() {
  await studentSession.logout();
  router.replace("/student/login");
}

async function toggleProgress(orderId) {
  if (expandedOrderId.value === orderId) {
    expandedOrderId.value = null;
    return;
  }
  expandedOrderId.value = orderId;
  if (!progressByOrder.value[orderId]) {
    progressByOrder.value = {
      ...progressByOrder.value,
      [orderId]: await api.studentOrderProgress(orderId),
    };
  }
}

function formatDate(value) {
  return value ? value.replace("T", " ").slice(0, 16) : "";
}

function paymentLabel(status) {
  return { PAID: "已付款", UNPAID: "待付款", REFUNDED: "已退款" }[status] || status || "待确认";
}

function orderStatusLabel(status) {
  return { NEW: "新订单", IN_PROGRESS: "服务中", COMPLETED: "已完成", CANCELLED: "已取消" }[status] || status || "处理中";
}

function invoiceStatusLabel(status) {
  return { PENDING: "申请中", ISSUED: "已开票", REJECTED: "已驳回" }[status] || status || "待处理";
}

onMounted(loadOrders);
</script>

<template>
  <section class="student-page">
    <div class="student-orders-head">
      <div>
        <span class="eyebrow">MY ORDERS</span>
        <h1>我的订单</h1>
        <p>{{ studentSession.state.displayName || studentSession.state.mobile }}，这里展示你的服务订单、处理进度和发票记录。</p>
      </div>
      <button class="ghost" @click="logout">退出登录</button>
    </div>
    <div v-if="loading" class="empty-state">订单加载中…</div>
    <div v-else-if="error" class="empty-state error-state">{{ error }}<button class="ghost" @click="loadOrders">重新加载</button></div>
    <div v-else-if="orders.length" class="student-order-list">
      <article v-for="order in orders" :key="order.id" class="card student-order-card">
        <div>
          <span>{{ order.orderNo }} · {{ order.targetType || "咨询" }}</span>
          <h3>{{ order.title }}</h3>
          <p>{{ order.notes || "暂无补充说明" }}</p>
          <small>顾问：{{ order.consultantName || "待分配" }} · 创建时间：{{ formatDate(order.createdAt) }}</small>
          <ol v-if="expandedOrderId === order.id" class="student-progress">
            <li v-for="item in progressByOrder[order.id] || []" :key="item.id">
              <b>{{ item.stageLabel }}</b>
              <span>{{ item.progressNote }}</span>
              <small>{{ item.operatorName || "系统" }} · {{ formatDate(item.createdAt) }}</small>
            </li>
          </ol>
        </div>
        <div class="student-order-meta">
          <b>{{ order.currentStage }}</b>
          <small>{{ orderStatusLabel(order.orderStatus) }} · {{ paymentLabel(order.paymentStatus) }}</small>
          <strong>{{ order.currencyCode }} {{ Number(order.amount || 0).toFixed(2) }}</strong>
          <button class="ghost" @click="toggleProgress(order.id)">{{ expandedOrderId === order.id ? "收起进度" : "查看进度" }}</button>
        </div>
      </article>
    </div>
    <div v-else class="empty-state"><b>暂时没有订单</b><span>提交稿件评估或预约服务后，记录会显示在这里。</span><RouterLink class="primary" to="/#submission">提交稿件信息</RouterLink></div>

    <section class="student-invoices">
      <div class="student-orders-head compact">
        <div>
          <span class="eyebrow">INVOICES</span>
          <h2>我的发票</h2>
        </div>
      </div>
      <div v-if="!loading && invoices.length" class="student-order-list">
        <article v-for="invoice in invoices" :key="invoice.id" class="card student-invoice-card">
          <div>
            <span>{{ invoiceStatusLabel(invoice.status) }}</span>
            <h3>{{ invoice.invoiceTitle }}</h3>
            <p>{{ invoice.invoiceType }} · {{ invoice.receiverEmail || invoice.receiverPhone || "未填写接收方式" }}</p>
          </div>
          <strong>{{ invoice.invoiceAmount ? Number(invoice.invoiceAmount).toFixed(2) : "0.00" }} 元</strong>
        </article>
      </div>
      <div v-else-if="!loading" class="empty-state"><b>暂无发票记录</b><span>付款订单申请开票后，会在这里显示处理状态。</span></div>
    </section>
  </section>
</template>
