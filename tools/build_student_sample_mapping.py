#!/usr/bin/env python3
"""从学生样板的浏览器元素快照生成逐元素组件映射；不会写 v1。"""
from pathlib import Path
import json
import re
import sys
import yaml

ROOT = Path(__file__).resolve().parents[1]
SNAPSHOT = ROOT / 'evidence/stage2-prototype-v2/2026-10-01_student-elements.json'
TARGET = ROOT / 'prototypes/high-fidelity/v2/component-mapping.yaml'


def component(node, panel_kinds):
    raw, props = node['component'], {}
    if node['panel']:
        return 'el-' + panel_kinds[node['panel']], props, '已声明浮层载体'
    if node['tag'] == 'tr':
        return 'el-table', {'event': 'row-click'}, '表格行入口'
    if node['type'] == 'checkbox' and any(k in node['selector'] for k in ['#student-rows', '#student-table']):
        return 'el-table-column', {'type': 'selection'}, '表格勾选列'
    if raw == 'pagination':
        return 'el-pagination', props, '分页控件'
    if raw:
        name = 'el-date-picker' if raw.startswith('el-date-picker') else raw.split('.')[0]
        parts = raw.split('.')[1:]
        if raw.endswith('.textarea'):
            props['type'] = 'textarea'
        if raw == 'el-date-picker.daterange':
            props.update(type='daterange', review_note='入学年份原型声明为日期范围，生产实现需按 PRD 年份区间核对，未在本批变更')
        if name == 'el-button':
            for value in ['primary', 'danger', 'success', 'warning', 'info']:
                if value in parts:
                    props['type'] = value
            for value in ['plain', 'link']:
                if value in parts:
                    props[value] = True
        return name, props, '原型明确声明'
    if node['tag'] == 'button':
        return 'el-button', props, '按 v1 通用按钮规则'
    if node['tag'] == 'select':
        return 'el-select', props, '原生下拉'
    if node['tag'] == 'textarea':
        return 'el-input', {'type': 'textarea'}, '多行输入'
    if node['tag'] == 'input':
        return {'checkbox': 'el-checkbox', 'file': 'el-upload', 'radio': 'el-radio'}.get(node['type'], 'el-input'), props, '原生输入类型'
    raise ValueError(f'未识别元素，停止生成：{node}')


def main():
    snapshot = json.loads(SNAPSHOT.read_text(encoding='utf-8'))
    body = (ROOT / 'prototypes/high-fidelity/v2/pages/student-list.html').read_text(encoding='utf-8')
    kinds = dict(re.findall(r'data-demo-panel="([^"]+)" data-overlay-kind="([^"]+)"', body))
    document = yaml.safe_load(TARGET.read_text(encoding='utf-8'))
    elements = []
    for index, node in enumerate(snapshot, 1):
        name, props, basis = component(node, kinds)
        item = dict(id=f'EL-STU-{index:03d}', page_id=node['page_id'], selector=node['selector'],
                    label=node['label'][:160], element_plus=name, props=props, basis=basis,
                    declared_component=node['component'])
        for key in ['action_id', 'permission', 'field', 'api', 'nav', 'role_visible', 'role_enabled']:
            item[key] = node[key]
        item['close_panel'] = node['close']
        elements.append(item)
    document['elements'] = elements
    document['coverage']['interactive_elements'] = len(elements)
    document['coverage']['page_ids'] = sorted({n['page_id'] for n in snapshot})
    if '--check' in sys.argv:
        current = yaml.safe_load(TARGET.read_text(encoding='utf-8'))
        if current != document:
            raise SystemExit('失败：映射与当前快照不一致')
        print(f'通过：{len(elements)} 个元素的映射与快照一致')
    else:
        TARGET.write_text(yaml.safe_dump(document, allow_unicode=True, sort_keys=False, width=110), encoding='utf-8', newline='\n')
        print(f'已生成：{len(elements)} 个元素')


if __name__ == '__main__':
    main()
