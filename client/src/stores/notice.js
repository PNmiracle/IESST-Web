import { reactive } from "vue";

export const notice = reactive({ message: "", error: false, timer: null });

export function showNotice(message, error = false) {
  clearTimeout(notice.timer);
  notice.message = message;
  notice.error = error;
  notice.timer = setTimeout(() => {
    notice.message = "";
  }, 3500);
}
