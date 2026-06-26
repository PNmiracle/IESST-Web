<script setup>
import { computed, onMounted, ref } from "vue";
import { api } from "../../api";
import { showNotice } from "../../stores/notice";

const orders = ref([]);
const invoices = ref([]);
const loading = ref(true);
const loadError = ref("");
const orderStatusFilter = ref("全部");
const updatingOrderId = ref(null);
const updatingInvoiceId = ref(null);

const orderStatusOptions = [
  { value: "NEW", label: "新订单" },
  { value: "IN_PROGRESS", label: "服务中" },
  { value: "COMPLETED", label: "已完成" },
  { value: "CANCELLED", label: "已取消" },
];

const invoiceStatusOptions = [
  { value: "PENDING", label: "申请中" },
  { value: "ISSUED", label: "已开票" },
  { value: "REJECTED", label: "已驳回" },
];

const filteredOrders = computed(() => {
  if (orderStatusFilter.value === "全部") return orders.value;
  return orders.value.filter((order) => order.orderStatus === orderStatusFilter.value);
});

const invoiceByOrderId = computed(() => {
  const result = new Map();
  invoices.value.forEach((invoice) => result.set(invoice.orderId, invoice));
  return result;
});

function formatDate(value) {
  return value ? value.replace("T", " ").slice(0, 16) : "";
}

function orderStatusLabel(status) {
  return orderStatusOptions.find((item) => item.value === status)?.label || status || "处理中";
}

function invoiceStatusLabel(status) {
  return invoiceStatusOptions.find((item) => item.value === status)?.label || status || "待处理";
}

function paymentLabel(status) {
  return { PAID: "已付款", UNPAID: "待付款", REFUNDED: "已退款" }[status] || status || "待确认";
}

async function load() {
  loading.value = true;
  loadError.value = "";
  try {
    const [orderItems, invoiceItems] = await Promise.all([api.adminOrders(), api.adminInvoices()]);
    orders.value = orderItems;
    invoices.value = invoiceItems;
  } catch (error) {
    loadError.value = error.message;
  } finally {
    loading.value = false;
  }
}

async function updateOrderStatus(id, status) {
  updatingOrderId.value = id;
  try {
    await api.updateOrderStatus(id, status);
    showNotice("订单状态已更新");
    await load();
  } catch (error) {
    showNotice(error.message, true);
  } finally {
    updatingOrderId.value = null;
  }
}

async function updateInvoiceStatus(id, status) {
  updatingInvoiceId.value = id;
  try {
    await api.updateInvoiceStatus(id, status);
    showNotice("发票状态已更新");
    await load();
  } catch (error) {
    showNotice(error.message, true);
  } finally {
    updatingInvoiceId.value = null;
  }
}

onMounted(load);
</script>

<template>
  <section class="admin-page">
    <header class="page-title">
      <div>
        <span>ORDER CENTER</span>
        <h1>订单与发票</h1>
        <p>集中查看学生订单、服务状态和发票申请，后台更新后学生端会同步展示。</p>
      </div>
      <div class="submission-tools">
        <div class="segmented">
          <button
            v-for="status in ['全部', ...orderStatusOptions.map((item) => item.value)]"
            :key="status"
            :class="{ active: orderStatusFilter === status }"
            @click="orderStatusFilter = status"
          >
            {{ status === "全部" ? "全部" : orderStatusLabel(status) }}
          </button>
        </div>
        <button class="ghost" :disabled="loading" @click="load">刷新</button>
      </div>
    </header>

    <div v-if="loading" class="empty-state">订单数据加载中…</div>
    <div v-else-if="loadError" class="empty-state error-state">{{ loadError }}<button class="ghost" @click="load">重新加载</button></div>

    <section v-else-if="filteredOrders.length" class="card table-wrap order-table">
      <table>
        <thead>
          <tr>
            <th>订单</th>
            <th>学生/服务</th>
            <th>金额</th>
            <th>当前状态</th>
            <th>发票</th>
            <th>创建时间</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="order in filteredOrders" :key="order.id">
            <td>
              <b>{{ order.orderNo }}</b>
              <small>{{ order.title }}</small>
            </td>
            <td>
              <span class="tag">{{ order.targetType || "咨询" }}</span>
              <small>学生 ID：{{ order.studentUserId }} · 编辑：{{ order.consultantName || "待分配" }}</small>
            </td>
            <td>
              <b>{{ order.currencyCode }} {{ Number(order.amount || 0).toFixed(2) }}</b>
              <small>{{ paymentLabel(order.paymentStatus) }}</small>
            </td>
            <td>
              <select :disabled="updatingOrderId === order.id" :value="order.orderStatus" @change="updateOrderStatus(order.id, $event.target.value)">
                <option v-for="item in orderStatusOptions" :key="item.value" :value="item.value">{{ item.label }}</option>
              </select>
              <small>{{ order.currentStage }}</small>
            </td>
            <td>
              <template v-if="invoiceByOrderId.get(order.id)">
                <select
                  :disabled="updatingInvoiceId === invoiceByOrderId.get(order.id).id"
                  :value="invoiceByOrderId.get(order.id).status"
                  @change="updateInvoiceStatus(invoiceByOrderId.get(order.id).id, $event.target.value)"
                >
                  <option v-for="item in invoiceStatusOptions" :key="item.value" :value="item.value">{{ item.label }}</option>
                </select>
                <small>{{ invoiceByOrderId.get(order.id).invoiceTitle }} · {{ invoiceStatusLabel(invoiceByOrderId.get(order.id).status) }}</small>
              </template>
              <small v-else>暂无发票申请</small>
            </td>
            <td>{{ formatDate(order.createdAt) }}</td>
          </tr>
        </tbody>
      </table>
    </section>
    <div v-else class="empty-state"><b>没有匹配的订单</b><span>切换状态筛选或等待学生提交服务后查看。</span></div>

    <section class="card table-wrap invoice-table" v-if="!loading && invoices.length">
      <header class="mini-table-head">
        <div>
          <span>INVOICE REQUESTS</span>
          <h2>发票申请</h2>
        </div>
      </header>
      <table>
        <thead>
          <tr>
            <th>抬头</th>
            <th>金额</th>
            <th>接收方式</th>
            <th>状态</th>
            <th>申请时间</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="invoice in invoices" :key="invoice.id">
            <td><b>{{ invoice.invoiceTitle }}</b><small>{{ invoice.taxNumber || "未填写税号" }}</small></td>
            <td>{{ Number(invoice.invoiceAmount || 0).toFixed(2) }} 元</td>
            <td><small>{{ invoice.receiverEmail || invoice.receiverPhone || invoice.receiverAddress || "未填写接收方式" }}</small></td>
            <td>
              <select :disabled="updatingInvoiceId === invoice.id" :value="invoice.status" @change="updateInvoiceStatus(invoice.id, $event.target.value)">
                <option v-for="item in invoiceStatusOptions" :key="item.value" :value="item.value">{{ item.label }}</option>
              </select>
            </td>
            <td>{{ formatDate(invoice.createdAt) }}</td>
          </tr>
        </tbody>
      </table>
    </section>
  </section>
</template>
