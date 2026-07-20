<script setup>
import { ref } from "vue";
import SmartCommaText from "../../components/SmartCommaText.vue";

const roles = [
  {
    id: "journal-expert",
    title: "期刊专家",
    tone: "purple",
    requirements: [
      "博士学位或副高级以上职称，具有稳定研究方向",
      "近年持续发表高质量成果，具备良好学术声誉",
      "有期刊编委、客座编辑或专刊组织经验者优先",
    ],
  },
  {
    id: "association-committee",
    title: "协会委员会",
    tone: "blue",
    requirements: [
      "高校或科研机构学术骨干、学科带头人",
      "具备学术组织、协会工作或国际合作经验",
      "愿意参与委员会建设及年度学术活动策划",
    ],
  },
  {
    id: "review-expert",
    title: "审稿专家",
    tone: "indigo",
    requirements: [
      "硕士及以上学历，在细分领域有持续研究积累",
      "发表过 SCI / EI 等高质量学术论文",
      "有期刊审稿、编委或科研项目评审经验者优先",
    ],
  },
];

const selectedRole = ref(null);

function closeRecruitment() {
  selectedRole.value = null;
}
</script>

<template>
  <section id="scholar-recruitment" class="section scholar-recruitment-section scholar-recruitment-page">
    <div class="shell">
      <header class="scholar-recruitment-heading">
        <div>
          <h1>学者招募</h1>
          <p><SmartCommaText text="面向全球高校、科研机构与行业研究团队，邀请具有专业积累和公共学术热情的学者加入思研学术合作网络。" /></p>
        </div>
        <strong>共建可信、开放、长期的学术共同体</strong>
      </header>
      <div class="scholar-recruitment-grid">
        <article v-for="role in roles" :key="role.id" :class="['scholar-recruitment-card', `tone-${role.tone}`]">
          <header>
            <span>招募职位</span>
            <h2>{{ role.title }}</h2>
          </header>
          <div class="scholar-recruitment-body">
            <h3>招募对象</h3>
            <ul><li v-for="item in role.requirements" :key="item">{{ item }}</li></ul>
            <button type="button" @click="selectedRole = role">立即加入</button>
          </div>
        </article>
      </div>
    </div>
  </section>

  <Teleport to="body">
    <div v-if="selectedRole" class="service-qr-backdrop" @click.self="closeRecruitment">
      <section class="service-qr-modal recruitment-qr-modal card" role="dialog" aria-modal="true" aria-labelledby="recruitment-qr-title">
        <button class="consult-close" type="button" aria-label="关闭学者招募咨询" @click="closeRecruitment">×</button>
        <span class="eyebrow">SCHOLAR RECRUITMENT</span>
        <h2 id="recruitment-qr-title">申请加入{{ selectedRole.title }}</h2>
        <p><SmartCommaText :text="`请使用微信扫描二维码，添加编辑后备注“${selectedRole.title}申请”，我们将核验研究方向与学术经历，并在核验后与您联系。`" /></p>
        <img src="/images/editor-contact-qr.png" alt="学者招募咨询二维码" />
        <small>建议同时准备个人简介、研究方向与代表性成果</small>
      </section>
    </div>
  </Teleport>
</template>
