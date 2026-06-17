import { computed, reactive } from "vue";
import { api } from "../api";

const state = reactive({
  authenticated: false,
  initialized: false,
  username: "",
  displayName: "IESST 管理员",
});

let restorePromise = null;

function applyUser(result) {
  state.authenticated = Boolean(result?.authenticated);
  state.username = result?.username || "";
  state.displayName = result?.displayName || "IESST 管理员";
}

export const session = {
  state,
  isLoggedIn: computed(() => state.authenticated),
  async restore(force = false) {
    if (state.initialized && !force) return state.authenticated;
    if (restorePromise && !force) return restorePromise;
    restorePromise = api.currentUser()
      .then((result) => {
        applyUser(result);
        return state.authenticated;
      })
      .catch(() => {
        applyUser(null);
        return false;
      })
      .finally(() => {
        state.initialized = true;
        restorePromise = null;
      });
    return restorePromise;
  },
  login(result) {
    applyUser(result);
    state.initialized = true;
  },
  async logout() {
    try {
      await api.logout();
    } finally {
      applyUser(null);
      state.initialized = true;
    }
  },
  clear() {
    applyUser(null);
    state.initialized = true;
  },
};
