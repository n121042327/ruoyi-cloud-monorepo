<template>
  <div class="error-wrap">
    <el-card class="error-card" shadow="always">
      <div class="err-code">403</div>
      <div class="err-title">你没有访问该页面的权限</div>
      <div class="err-desc">
        当前账号缺少该功能权限，或该页面的数据范围为空集。<b>无权限时不会降级为全量可见</b> （<span class="mono">DS-DENY-03</span
        >）：页面不会渲染空表格，接口层同样拒绝并要求租户、学校与执行人上下文 （<span class="mono">NFR-SEC-05</span>）。<br />
        如果你是平台运营账号：查看 / 修改 / 导出分别授权、分别审计（<span class="mono">DS-01</span>），请联系运营管理员开通查看权。
      </div>
      <div class="err-actions">
        <el-button type="primary" @click="goHome">返回工作台</el-button>
        <el-button @click="applyPermission">申请开通权限</el-button>
      </div>
      <div class="err-meta">本页不展示任何业务数据；缺少权限时的访问同样写入审计（<span class="mono">NFR-AUDIT-02</span>）。</div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';

defineOptions({ name: 'EduError403' });

const router = useRouter();

const goHome = () => router.push('/index');

/** 申请开通权限：本批只给提示；权限申请工单在后续批次接入 */
const applyPermission = () => {
  ElMessage.info('如需开通权限，请联系本校管理员或运营管理员；申请会记录在审计里');
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
