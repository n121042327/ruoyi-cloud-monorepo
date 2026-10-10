import { ElMessage } from 'element-plus';
import type { EduFileRefVO } from '@/api/edu/importExport/types';

/**
 * 导入导出文件引用的下载工具。
 *
 * 模板、失败明细、结果文件都由统一文件服务签发短时签名链接（`EduFileRefVo.signedUrl`），
 * 前端不接收二进制：先取引用，再跳签名链接（REQ-IMP-042 / BR-IMP-013）。
 */

/** 用后端返回的文件引用触发下载；返回是否真的发起了下载 */
export function downloadByFileRef(ref?: EduFileRefVO | null, fallbackName?: string): boolean {
  if (!ref?.signedUrl) {
    ElMessage.warning(ref?.hint || '文件链接未生成，请稍后重试');
    return false;
  }
  const link = document.createElement('a');
  link.href = ref.signedUrl;
  link.target = '_blank';
  link.rel = 'noopener';
  link.download = ref.fileName || fallbackName || '';
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  // 模板版本过期时仍可下载，但要给强提示（REQ-IMP-003）
  if (ref.hint) {
    ElMessage.warning(ref.hint);
  }
  return true;
}

export default { downloadByFileRef };
