<script setup>
import { openConsultation } from "../../stores/consultation";

function openAboutConsultation() {
  openConsultation({
    subject: "预约一对一沟通",
    targetType: "SCI",
  });
}
</script>

<template>
  <section class="about-page">
    <div class="shell about-content">
      <header class="about-title">
        <span class="about-title-mark" aria-hidden="true">
          <i></i><i></i><i></i>
        </span>
        <h1>关于我们</h1>
        <strong>/ ABOUT US</strong>
      </header>

      <div class="about-summary">
        <article>
          <b>150+</b>
          <span>国际编委成员</span>
        </article>
        <article>
          <b>60+</b>
          <span>SCI 及 ESCI 特刊授权</span>
        </article>
        <article>
          <b>2019</b>
          <span>机构成立年份</span>
        </article>
      </div>

      <section class="about-section">
        <h2>协会简介</h2>
        <div class="about-copy">
          <p>思研学术（IESST）成立于 2019 年，是一家拥有自主出版资质的学术出版协会，总部设于英国伦敦。中国办公室位于浙江省杭州市。旗下设国际编委会，现有在册编委 150 余人，均为来自欧美及亚洲主要国家科研机构的现职学者。编委团队学科覆盖理工、工农、医及交叉领域，其中含教授职称核心编委多名，具备直接向 SCI 期刊方申请并主导特刊的法定学术资格。</p>
          <p>本协会专注于 SCI 及 ESCI 期刊特刊的策划、申报与出版服务，致力于为全球科研人员提供具备学术确定性、成本可控且流程高效的特刊发表解决方案。</p>
          <p class="english">IESST (International Elite Scientific Publishing) was founded in 2019 as an academic publishing association with independent publishing qualifications, headquartered in London, United Kingdom, with its China office located in Hangzhou, Zhejiang Province.</p>
          <p class="english">IESST specializes in the planning, application, and publication services of special issues in SCI and ESCI journals, and is committed to providing global researchers with academically reliable, cost-controlled, and efficient special issue publication solutions.</p>
        </div>
      </section>

      <section class="about-section">
        <h2>特刊资源</h2>
        <div class="about-copy">
          <p>截至目前，思研学术已正式获得 60 余种 SCI 及 ESCI 期刊的特刊出版授权。所有特刊均为对应期刊的正刊特辑，严格遵循 Web of Science 收录规范，不存在预警或违规出版风险。</p>
          <p class="english">To date, IESST has officially obtained special issue publication authorizations from more than 60 SCI and ESCI journals.</p>
          <p class="english">All special issues are genuine special issues of their respective parent journals, strictly adhering to Web of Science indexing standards, with no risk of being on warning lists or involving irregular publishing practices.</p>
        </div>
      </section>

      <section class="about-section">
        <h2>服务定位</h2>
        <div class="about-copy">
          <p>本机构采用出版方直营模式，去除中间环节，在确保学术质量的前提下实现显著低于行业均值的发表成本。同时，通过编委内部快速审稿及定向推荐机制，大幅压缩从投稿到录用的周期，实现特刊发表的高度时间确定性。</p>
          <p class="english">The institution adopts a publisher-direct model, eliminating intermediary links, and achieves publication costs significantly below the industry average while ensuring academic quality.</p>
          <p class="english">At the same time, through rapid internal review by the editorial board and targeted recommendation mechanisms, the period from submission to acceptance is greatly shortened, achieving a high degree of temporal certainty for special issue publication.</p>
        </div>
      </section>
    </div>
  </section>

  <section class="section pale">
    <div class="shell callout">
      <div>
        <span class="eyebrow">CONTACT US</span>
        <h2>让我们从稿件初评开始</h2>
        <p>提交基础信息，编辑会根据目标方向和当前阶段提供初步建议。</p>
      </div>
      <button class="primary" type="button" @click="openAboutConsultation">预约一对一沟通</button>
    </div>
  </section>
</template>

<style scoped>
.about-page {
  background: #fff;
}

.about-content {
  width: min(calc(100% - 48px), 1120px);
  padding: 72px 0 94px;
}

.about-title {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 54px;
}

.about-title-mark {
  position: relative;
  display: inline-flex;
  width: 54px;
  height: 28px;
  align-items: center;
}

.about-title-mark i {
  position: absolute;
  width: 22px;
  height: 22px;
  background: #70439a;
  border-radius: 999px;
  opacity: 0.46;
}

.about-title-mark i:nth-child(1) {
  left: 0;
}

.about-title-mark i:nth-child(2) {
  left: 13px;
  opacity: 0.66;
}

.about-title-mark i:nth-child(3) {
  left: 26px;
  opacity: 0.86;
}

.about-title h1 {
  margin: 0;
  color: #3b2458;
  font-size: 28px;
  line-height: 1.1;
}

.about-title strong {
  color: #b7b4bd;
  font-size: 18px;
  font-weight: 400;
}

.about-summary {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
  margin: -18px 0 58px;
}

.about-summary article {
  padding: 22px 24px;
  background: #faf8fc;
  border: 1px solid #ebe5f1;
  border-radius: 8px;
}

.about-summary b {
  display: block;
  color: #70439a;
  font-size: 30px;
  line-height: 1;
}

.about-summary span {
  display: block;
  margin-top: 8px;
  color: #687188;
  font-size: 13px;
}

.about-section {
  margin-top: 64px;
}

.about-section:first-of-type {
  margin-top: 0;
}

.about-section h2 {
  position: relative;
  margin: 0 0 22px;
  padding-left: 24px;
  color: #16181d;
  font-size: 25px;
  line-height: 1.35;
}

.about-section h2::before {
  position: absolute;
  top: 0.3em;
  left: 0;
  width: 9px;
  height: 24px;
  content: "";
  background: linear-gradient(90deg, #4b2a74, #9b7fc3);
  border-radius: 999px;
}

.about-copy {
  max-width: 940px;
  padding-left: 24px;
}

.about-copy p {
  margin: 0 0 9px;
  color: #202329;
  font-size: 17px;
  line-height: 1.92;
}

.about-copy .english {
  color: #252932;
}

@media (max-width: 650px) and (hover: none) and (pointer: coarse) {
  .about-content {
    width: min(calc(100% - 28px), 1120px);
    padding: 46px 0 68px;
  }

  .about-title {
    flex-wrap: wrap;
    margin-bottom: 34px;
  }

  .about-title h1 {
    font-size: 25px;
  }

  .about-title strong {
    width: 100%;
    margin-left: 64px;
    font-size: 14px;
  }

  .about-summary {
    grid-template-columns: 1fr;
    margin: 0 0 38px;
  }

  .about-section {
    margin-top: 44px;
  }

  .about-section h2 {
    font-size: 22px;
  }

  .about-copy {
    padding-left: 0;
  }

  .about-copy p {
    font-size: 15px;
    line-height: 1.82;
  }
}
</style>
