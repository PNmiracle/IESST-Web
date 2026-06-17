import { reactive } from "vue";

export const consultation = reactive({
  open: false,
  seed: 0,
  payload: {},
});

export function openConsultation(payload = {}) {
  consultation.payload = payload;
  consultation.seed += 1;
  consultation.open = true;
}

export function closeConsultation() {
  consultation.open = false;
}

if (typeof window !== "undefined") {
  window.__iesstConsultationFromElement = (trigger) => {
    openConsultation({
      subject: trigger?.dataset?.consultSubject || "预约一对一沟通",
      targetType: trigger?.dataset?.consultTarget || "SCI",
    });
  };
}
