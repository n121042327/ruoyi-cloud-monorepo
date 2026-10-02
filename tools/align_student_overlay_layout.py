"""按 CR-028 整理学生 v2 三个浮层（学籍异动 / 调班 / 异动登记）的字段分组。

只处理 prototypes/{functional,high-fidelity}/v2/pages/student-list.html：
把每个弹窗的字段按语义分组重排为 .layout-sections > section[data-layout-group]，
字段本身的 HTML 原样保留（label、hint、error-text、id、data-component 都不改），
因此页面脚本与外壳事件委托不受影响。

检测到 CR-028 标记时直接跳过，保证重复执行安全。
"""
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
TARGETS = [
    ROOT / 'prototypes/functional/v2/pages/student-list.html',
    ROOT / 'prototypes/high-fidelity/v2/pages/student-list.html',
]
MARKER = '<!-- CR-028 layout -->'

TRANSFER_EFFECTIVE_DATE = '''<div class="field" data-field="effective_date">
  <label>生效日期<span class="required">*</span></label>
  <input class="input" style="min-width:100%" type="date" id="st-effective" value="2026-09-01"
         data-component="el-date-picker" data-required data-required-message="请选择生效日期" />
  <div class="error-text"></div>
  <div class="hint">默认取当前学年学期开始日；生效日期之前的归属按历史关系查询（与班级模块调班口径一致）。</div>
</div>'''

SPEC = {
    'PAGE-STU-STATUS': {
        'sections': [
            ('异动信息', ['change_type', 'effective_date']),
            ('复学 / 报到安排', ['class_id']),
            ('异动说明', ['reason']),
        ],
        'extras': {'ss-approve-alert': '异动信息'},
        'notes': {},
        'span2': ['reason'],
    },
    'PAGE-STU-TRANSFER': {
        'sections': [
            ('班级关系', ['class_id', 'effective_date']),
            ('调班说明', ['remark']),
        ],
        'extras': {},
        'notes': {},
        'span2': ['remark'],
        'rename': {'reason': 'remark'},
        'inject': {'effective_date': TRANSFER_EFFECTIVE_DATE},
    },
    'PAGE-PRM-CHANGE': {
        'sections': [
            ('异动信息', ['change_type', 'effective_date']),
            ('异动说明', ['reason']),
        ],
        'extras': {},
        'notes': {},
        'span2': [],
    },
}

DIV_TAG = re.compile(r'<div\b[^>]*>|</div\s*>', re.I)
DIV_OPEN = re.compile(r'<div\b', re.I)
START_TAG = re.compile(r'\s*<div\b([^>]*)>', re.I)
FIELD_ATTR = re.compile(r'data-field="([^"]+)"')
ID_ATTR = re.compile(r'id="([^"]+)"')


def div_span(text, start):
    """text[start:] 以 '<div' 开头，返回该 div 结束（含 </div>）后的下标。"""
    depth = 0
    for m in DIV_TAG.finditer(text, start):
        if m.group(0).lower().startswith('</'):
            depth -= 1
            if depth == 0:
                return m.end()
        else:
            depth += 1
    raise ValueError('未闭合的 div：' + text[start:start + 40])


def children(text):
    """把一段 HTML 拆成顶层节点：('div', 源码) 或 ('text', 源码)。"""
    out, i = [], 0
    while True:
        m = DIV_OPEN.search(text, i)
        if not m:
            break
        # 把标签所在行首的缩进一起带进块，避免首行缩进被吃掉
        line_start = text.rfind('\n', i, m.start()) + 1
        start = line_start if not text[line_start:m.start()].strip() else m.start()
        out.append(('text', text[i:start]))
        end = div_span(text, m.start())
        out.append(('div', text[start:end]))
        i = end
    out.append(('text', text[i:]))
    return out


def classify(block):
    """返回 ('field', 名) / ('grid', 子节点) / ('extra', id) / ('summary', None) / ('other', None)。"""
    attrs = START_TAG.match(block).group(1)
    field = FIELD_ATTR.search(attrs)
    if field:
        return ('field', field.group(1)), block
    if 'data-validate-summary' in attrs:
        return ('summary', None), block
    if 'id="ss-approve-alert"' in attrs:
        return ('extra', 'ss-approve-alert'), block
    if 'form-grid' in attrs or 'layout-sections' in attrs:
        inner = block[block.index('>') + 1:block.rindex('</div>')]
        return ('grid', children(inner)), block
    return ('other', None), block


def reindent(block, indent):
    lines = block.strip('\n').split('\n')
    while lines and not lines[0].strip():
        lines.pop(0)
    while lines and not lines[-1].strip():
        lines.pop()
    # 块的末行是它自己的闭合标签，缩进与块首行一致，用它当基准最稳
    last = lines[-1]
    last_lead = len(last) - len(last.lstrip(' '))
    if last.strip().startswith('</'):
        base = last_lead
    elif len(lines) > 1:
        base = min(len(l) - len(l.lstrip(' ')) for l in lines[1:] if l.strip())
    else:
        base = len(lines[0]) - len(lines[0].lstrip(' '))
    out = []
    for line in lines:
        if not line.strip():
            out.append('')
            continue
        lead = len(line) - len(line.lstrip(' '))
        out.append(indent + line[min(base, lead):])
    return '\n'.join(out)


def add_span2(block):
    if 'class="field span-2"' in block:
        return block
    return block.replace('class="field"', 'class="field span-2"', 1)


def collect(body, rename=None):
    rename = rename or {}
    fields, extras, others, summary = {}, {}, [], None

    def walk(nodes):
        nonlocal summary
        for kind, raw in nodes:
            if kind != 'div':
                continue
            (tag, value), block = classify(raw)
            if tag == 'field':
                key = rename.get(value, value)
                if key != value:
                    block = block.replace('data-field="%s"' % value, 'data-field="%s"' % key, 1)
                fields[key] = block
            elif tag == 'extra':
                extras[value] = block
            elif tag == 'summary':
                summary = block
            elif tag == 'grid':
                walk(value)
            else:
                others.append(block)

    walk(children(body))
    return fields, extras, others, summary


def compose(spec, fields, extras, others, summary):
    inject = spec.get('inject', {})
    lines = []
    for block in others:
        lines.append(reindent(block, '      '))
        lines.append('')
    lines.append('      <div class="layout-sections">')
    for group, names in spec['sections']:
        lines.append('        <section data-layout-group="%s">' % group)
        lines.append('          <h3>%s</h3>' % group)
        lines.append('          <div class="form-grid">')
        for name in names:
            block = fields.get(name) or inject[name]
            if name in spec['span2']:
                block = add_span2(block)
            lines.append(reindent(block, '            '))
        for key, target in spec['extras'].items():
            if target == group:
                lines.append(reindent(extras[key], '            '))
        if group in spec['notes']:
            lines.append('            ' + spec['notes'][group])
        lines.append('          </div>')
        lines.append('        </section>')
    lines.append('      </div>')
    if summary:
        lines.append('')
        lines.append(reindent(summary.replace(' span-2"', '"'), '      '))
    return '\n'.join(lines)


def transform(text, panel):
    spec = SPEC[panel]
    anchor = text.index('data-demo-panel="%s"' % panel)
    body_start = text.index('<div class="dialog-body">', anchor)
    body_end = div_span(text, body_start)
    inner_start = body_start + len('<div class="dialog-body">')
    inner_end = body_end - len('</div>')
    fields, extras, others, summary = collect(text[inner_start:inner_end], spec.get('rename'))
    inject = spec.get('inject', {})
    missing = [name for _, names in spec['sections'] for name in names
               if name not in fields and name not in inject]
    if missing:
        raise ValueError('%s 缺少字段：%s' % (panel, ','.join(missing)))
    used = {name for _, names in spec['sections'] for name in names}
    unused = sorted(set(fields) - used)
    if unused:
        raise ValueError('%s 有未登记进分组的字段：%s' % (panel, ','.join(unused)))
    new_body = '\n' + compose(spec, fields, extras, others, summary) + '\n    '
    return text[:inner_start] + new_body + text[inner_end:]


# v1 原型的历史写法与需要随字段集一起收敛的文案
API_RENAMES = {
    'data-api="transferStudentClass"': 'data-api="transferClass"',
    'data-validate-summary>表单校验未通过：请先选择目标班级。</div>':
        'data-validate-summary>表单校验未通过：请修正下方标红的字段后重试。</div>',
}


def main():
    for path in TARGETS:
        text = path.read_text(encoding='utf-8')
        for panel in SPEC:
            text = transform(text, panel)
        for old, new in API_RENAMES.items():
            text = text.replace(old, new)
        if MARKER not in text:
            text = text.replace('<!-- CR-027 layout -->', '<!-- CR-027 layout -->\n' + MARKER, 1)
        path.write_text(text, encoding='utf-8', newline='\n')
        print('done：%s' % path.relative_to(ROOT))


if __name__ == '__main__':
    main()
