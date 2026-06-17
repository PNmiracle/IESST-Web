let unauthorizedHandler = null;
let csrfToken = "";

export function setUnauthorizedHandler(handler) {
  unauthorizedHandler = handler;
}

async function ensureCsrf() {
  if (csrfToken) return csrfToken;
  const response = await fetch("/api/auth/csrf", { credentials: "include" });
  if (!response.ok) throw new Error("安全令牌初始化失败");
  const result = await response.json();
  csrfToken = result.token;
  return csrfToken;
}

async function request(url, options = {}) {
  const method = (options.method || "GET").toUpperCase();
  const mutating = !["GET", "HEAD", "OPTIONS"].includes(method);
  const headers = { ...options.headers };

  if (options.body && !(options.body instanceof FormData)) {
    headers["Content-Type"] = "application/json";
  }
  if (mutating) {
    headers["X-XSRF-TOKEN"] = await ensureCsrf();
  }

  const response = await fetch(url, {
    ...options,
    method,
    credentials: "include",
    headers,
  });

  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: "请求失败" }));
    if (response.status === 401 && options.admin) unauthorizedHandler?.();
    throw new Error(error.message || "请求失败");
  }

  if (response.status === 204) return null;
  return response.json();
}

async function upload(url, file) {
  const formData = new FormData();
  formData.append("file", file);
  return request(url, { admin: true, method: "POST", body: formData });
}

async function download(url) {
  const response = await fetch(url, { credentials: "include" });
  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: "下载失败" }));
    if (response.status === 401) unauthorizedHandler?.();
    throw new Error(error.message || "下载失败");
  }
  return response.blob();
}

function queryString(parameters) {
  const query = new URLSearchParams();
  Object.entries(parameters).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== "") query.set(key, value);
  });
  return query.toString();
}

export const api = {
  publicBanners: () => request("/api/public/banners"),
  publicJournals: () => request("/api/public/journals"),
  publicJournal: (id) => request(`/api/public/journals/${id}`),
  publicServices: () => request("/api/public/services"),
  publicExperts: () => request("/api/public/experts"),
  submit: (body) => request("/api/public/submissions", { method: "POST", body: JSON.stringify(body) }),
  login: (body) => request("/api/auth/login", { method: "POST", body: JSON.stringify(body) }),
  logout: () => request("/api/auth/logout", { method: "POST", admin: true }),
  currentUser: () => request("/api/auth/me"),
  studentLogin: (body) => request("/api/student/login", { method: "POST", body: JSON.stringify(body) }),
  studentRegister: (body) => request("/api/student/register", { method: "POST", body: JSON.stringify(body) }),
  studentLogout: () => request("/api/student/logout", { method: "POST" }),
  currentStudent: () => request("/api/student/me"),
  studentOrders: () => request("/api/student/orders"),
  studentOrderProgress: (id) => request(`/api/student/orders/${id}/progress`),
  studentInvoices: () => request("/api/student/invoices"),
  dashboard: () => request("/api/admin/dashboard", { admin: true }),
  adminBanners: () => request("/api/admin/banners", { admin: true }),
  saveBanner: (body) => request(body.id ? `/api/admin/banners/${body.id}` : "/api/admin/banners", { admin: true, method: body.id ? "PUT" : "POST", body: JSON.stringify(body) }),
  deleteBanner: (id) => request(`/api/admin/banners/${id}`, { admin: true, method: "DELETE" }),
  adminJournals: () => request("/api/admin/journals", { admin: true }),
  saveJournal: (body) => request(body.id ? `/api/admin/journals/${body.id}` : "/api/admin/journals", { admin: true, method: body.id ? "PUT" : "POST", body: JSON.stringify(body) }),
  deleteJournal: (id) => request(`/api/admin/journals/${id}`, { admin: true, method: "DELETE" }),
  submissions: (parameters = {}) => request(`/api/admin/submissions?${queryString(parameters)}`, { admin: true }),
  exportSubmissions: (parameters = {}) => download(`/api/admin/submissions/export?${queryString(parameters)}`),
  updateStatus: (id, status) => request(`/api/admin/submissions/${id}/status`, { admin: true, method: "PUT", body: JSON.stringify({ status }) }),
  changePassword: (body) => request("/api/admin/account/password", { admin: true, method: "PUT", body: JSON.stringify(body) }),
  auditLogs: (parameters = {}) => request(`/api/admin/audit-logs?${queryString(parameters)}`, { admin: true }),
  adminServices: () => request("/api/admin/services", { admin: true }),
  saveService: (body) => request(body.id ? `/api/admin/services/${body.id}` : "/api/admin/services", { admin: true, method: body.id ? "PUT" : "POST", body: JSON.stringify(body) }),
  deleteService: (id) => request(`/api/admin/services/${id}`, { admin: true, method: "DELETE" }),
  adminExperts: () => request("/api/admin/experts", { admin: true }),
  saveExpert: (body) => request(body.id ? `/api/admin/experts/${body.id}` : "/api/admin/experts", { admin: true, method: body.id ? "PUT" : "POST", body: JSON.stringify(body) }),
  deleteExpert: (id) => request(`/api/admin/experts/${id}`, { admin: true, method: "DELETE" }),
  adminStudents: () => request("/api/admin/students", { admin: true }),
  saveStudent: (body) => request(body.id ? `/api/admin/students/${body.id}` : "/api/admin/students", { admin: true, method: body.id ? "PUT" : "POST", body: JSON.stringify(body) }),
  deleteStudent: (id) => request(`/api/admin/students/${id}`, { admin: true, method: "DELETE" }),
  adminOrders: () => request("/api/admin/orders", { admin: true }),
  updateOrderStatus: (id, status) => request(`/api/admin/orders/${id}/status`, { admin: true, method: "PUT", body: JSON.stringify({ status }) }),
  adminInvoices: () => request("/api/admin/invoices", { admin: true }),
  updateInvoiceStatus: (id, status) => request(`/api/admin/invoices/${id}/status`, { admin: true, method: "PUT", body: JSON.stringify({ status }) }),
  uploadImage: (file) => upload("/api/admin/uploads/image", file),
  uploadDocument: (file) => upload("/api/admin/uploads/document", file),
};
