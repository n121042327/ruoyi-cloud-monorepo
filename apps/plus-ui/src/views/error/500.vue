<template>
  <div class="error-wrap">
    <el-card class="error-card" shadow="always">
      <div class="err-code">500</div>
      <div class="err-title">服务异常，请稍后重试</div>
      <div class="err-desc">
        服务器处理请求时发生异常。<b>本次操作没有产生部分写入</b>：批量类写操作（导入 / 升班）都是异步任务，
        失败会整批记录失败原因，可在任务中心重试（<span class="mono">NFR-MQ-02</span>）。
      </div>
      <div class="err-actions">
        <el-button type="primary" @click="handleRetry">重试</el-button>
        <el-button v-hasPermi="['data.async_task:read']" @click="goTaskCenter">去异步任务中心</el-button>
        <el-button @click="goHome">返回工作台</el-button>
      </div>
      <div class="err-meta">
        请求编号 <span class="mono">{{ requestId }}</span> · 错误码 <span class="mono">EDU-SYS-5001</span> ·
        已自动上报（含请求编号、路径、用户与租户）
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

defineOptions({ name: 'EduError500' });

const route = useRoute();
const router = useRouter();

/** 请求编号由拦截器透传（查询串或状态），缺失时给出占位并提示上报已含上下文 */
const requestId = ref(String((route.query.requestId as string) ?? '未提供（以下方上报信息为准）'));

const goHome = () => router.push('/index');
const goTaskCenter = () => router.push('/edu/async-task/list');

/** 重试：返回上一页触发同一次请求 */
const handleRetry = () => {
  if (window.history.length > 1) {
    router.back();
  } else {
    router.push('/index');
  }
};
</script>

<style lang="scss" scoped>
.error-wrap {
  display: flex;
  min-height: 100vh;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: #f5f7fb;
}

.error-card {
  width: 520px;
  padding: 8px;
  text-align: center;
}

.err-code {
  font-size: 46px;
  font-weight: 700;
  line-height: 1;
  color: var(--el-color-primary);
}

.err-title {
  margin: 12px 0 8px;
  font-size: 18px;
  font-weight: 600;
}

.err-desc {
  font-size: 13px;
  line-height: 1.8;
  color: var(--el-text-color-secondary);
  text-align: left;
}

.err-actions {
  display: flex;
  gap: 8px;
  justify-content: center;
  margin-top: 20px;
}

.err-meta {
  margin-top: 16px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  text-align: left;
}
</style>
