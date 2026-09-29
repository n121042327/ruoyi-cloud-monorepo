# 工程治理目录

本目录是整条交付流水线的"宪法"。任何阶段开始前，先读这里的文件；
任何阶段结束后，按 `change-control.md` 更新这里。

## 文件清单

| 文件 | 回答什么问题 | 谁消费 |
|---|---|---|
| `repo-baseline.md` | 动手前仓库到底是什么样？有哪些事实、哪些未核实？ | 所有人，阶段 0 |
| `stack-lock.md` | 用什么技术、什么版本、依据是什么、能不能换？ | 阶段 4—7 |
| `file-catalog.md` | 整个项目要产出哪些文件？现在到哪一步了？ | 所有人，每批 |
| `document-map.yaml` | 谁依赖谁？改一个文件要连带改哪些？ | 所有人，变更时 |
| `stage-inputs.yaml` | 每个阶段的输入、输出、门禁分别是什么？ | 每阶段开始 |
| `gap-register.yaml` | 有哪些缺项没解决？影响哪个阶段？ | 每批开始 |
| `decisions.md` | 已经拍板了什么？为什么？代价是什么？ | 所有人，有分歧时 |
| `change-control.md` | 要改已冻结的东西，走什么流程？ | 变更发起人 |
| `traceability.yaml` | 需求有没有一路落到表、接口、页面、测试？ | 阶段 5—8 |
| `task-packet.md` | 一个"小任务"要写成什么样才允许开工？ | 每批执行 |
| `baseline-manifest.schema.json` | 冻结产物清单的机器可校验格式 | 阶段门禁 |

## 使用顺序

1. 开工前：读 `gap-register.yaml`，有 `open` 且阻塞本阶段的项就停下问用户
2. 写任务：用 `task-packet.md` 模板，写明输入、输出、验收方法
3. 执行中：发现新缺项立即登记，不要边猜边做
4. 收尾：更新 `file-catalog.md` 状态、`traceability.yaml` 链接、`decisions.md` 裁决
5. 交付：产物 + 验收证据 + 已知缺口 + 下一批建议
