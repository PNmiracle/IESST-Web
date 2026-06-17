import { computed, reactive } from "vue";
import { api } from "../api";

const state = reactive({
  authenticated: false,
  initialized: false,
  mobile: "",
  displayName: "",
});

let restorePromise = null;

function applyStudent(result) {
  state.authenticated = Boolean(result?.authenticated);
  state.mobile = result?.mobile || "";
  state.displayName = result?.displayName || "";
}

export const studentSession = {
  state,
  isLoggedIn: computed(() => state.authenticated),
  async restore(force = false) {
    if (state.initialized && !force) return state.authenticated;
    if (restorePromise && !force) return restorePromise;
    restorePromise = api.currentStudent()
      .then((result) => {
        applyStudent(result);
        return state.authenticated;
      })
      .catch(() => {
        applyStudent(null);
        return false;
      })
      .finally(() => {
        state.initialized = true;
        restorePromise = null;
      });
    return restorePromise;
  },
  login(result) {
    applyStudent(result);
    state.initialized = true;
  },
  async logout() {
    try {
      await api.studentLogout();
    } finally {
      applyStudent(null);
      state.initialized = true;
    }
  },
};
