"""按 CR-025 整理既有 v2 字段；保留未涉及的源码和脚本。"""
from html.parser import HTMLParser
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
CLASS_FIELDS = ['class_name', 'grade_id', 'class_type', 'head_teacher_id', 'student_count', 'class_capacity', 'campus_id', 'classroom', 'class_status', 'actions']
CLASS_ORDER = ['campus_id', 'grade_id', 'class_name', 'class_type', 'head_teacher_id', 'classroom', 'class_capacity', 'student_count', 'class_status', 'actions']
TEACHER_FIELDS = ['_selection', 'teacher_no', 'teacher_name', 'gender', 'school_id', 'edu_role', 'subject_code', 'teaching_class_count', 'employment_status', 'teacher_phone', 'actions']

class Node:
    def __init__(self, tag, attrs, start, open_end):
        self.tag, self.attrs, self.start, self.open_end = tag, dict(attrs), start, open_end
        self.end, self.full_end, self.children = open_end, open_end, []

class Parser(HTMLParser):
    def __init__(self, source):
        super().__init__(convert_charrefs=False)
        self.source, self.nodes, self.stack = source, [], []
        self.lines = [0]
        for m in re.finditer('\n', source): self.lines.append(m.end())
        self.feed(source)
    def source_position(self):
        line, column = self.getpos()
        return self.lines[line - 1] + column
    def handle_starttag(self, tag, attrs):
        start = self.source_position()
        node = Node(tag, attrs, start, start + len(self.get_starttag_text()))
        if self.stack: self.stack[-1].children.append(node)
        self.nodes.append(node)
        if tag not in {'input', 'meta', 'link', 'br', 'hr', 'img', 'source', 'wbr'}: self.stack.append(node)
    def handle_startendtag(self, tag, attrs):
        self.handle_starttag(tag, attrs)
        if self.stack and self.stack[-1].tag == tag: self.stack.pop()
    def handle_endtag(self, tag):
        if self.stack and self.stack[-1].tag == tag:
            node = self.stack.pop()
            node.end, node.full_end = self.source_position(), self.source.index('>', self.source_position()) + 1

def group(name, chunks):
    return '<section data-layout-group="' + name + '"><h3 style="margin:12px 0;font-size:14px">' + name + '</h3><div class="form-grid">\n' + '\n'.join(chunks) + '\n</div></section>\n'

def align(path, kind):
    source = path.read_text(encoding='utf-8')
    if '<!-- CR-025 layout -->' in source: return
    parser = Parser(source)
    edits = []
    table_id = 'class-table' if kind == 'class' else 'teacher-table'
    table = next(n for n in parser.nodes if n.attrs.get('id') == table_id)
    fields = CLASS_FIELDS if kind == 'class' else TEACHER_FIELDS
    order = CLASS_ORDER if kind == 'class' else fields
    rows = [n for n in parser.nodes if n.tag == 'tr' and table.start < n.start < table.end]
    for row in rows:
        cells = [n for n in row.children if n.tag in {'td', 'th'}]
        assert len(cells) == len(fields), (path, len(cells))
        chunks = {}
        for field, cell in zip(fields, cells):
            if kind == 'class': semantic = '管理信息' if field in {'class_capacity', 'student_count', 'class_status'} else '教育信息'
            else: semantic = '基础信息' if field in {'teacher_no', 'teacher_name', 'gender'} else ('职业信息' if field == 'employment_status' else ('联系方式' if field == 'teacher_phone' else '教育信息'))
            if field in {'actions', '_selection'}: semantic = '操作' if field == 'actions' else '选择'
            opening = source[cell.start:cell.open_end]
            if 'data-field=' not in opening: opening = opening[:-1] + ' data-field="' + field + '">'
            opening = opening[:-1] + ' data-layout-group="' + semantic + '">'
            chunks[field] = opening + source[cell.open_end:cell.full_end]
        edits.append((row.open_end, row.end, '\n' + '\n'.join(chunks[f] for f in order) + '\n'))
    panel_id = 'PAGE-CLS-CREATE' if kind == 'class' else 'PAGE-TCH-EDIT'
    panel = next(n for n in parser.nodes if n.attrs.get('data-demo-panel') == panel_id)
    grid = next(n for n in parser.nodes if panel.start < n.start < panel.end and n.attrs.get('class') == 'form-grid')
    mapping, extra = {}, []
    for node in grid.children:
        if 'field' in node.attrs.get('class', '').split():
            field = node.attrs.get('data-field')
            descendants = [n for n in parser.nodes if node.start < n.start < node.end]
            if not field: field = next((n.attrs['data-field'] for n in descendants if 'data-field' in n.attrs), None)
            if not field: field = 'edu_role' if any('data-role-chip' in n.attrs for n in descendants) else 'remark'
            mapping[field] = source[node.start:node.full_end]
        else: extra.append((node.attrs.get('id'), source[node.start:node.full_end]))
    if kind == 'class':
        education = [mapping[f] for f in ['campus_id', 'term_id', 'grade_id', 'class_name']]
        education += [chunk for key, chunk in extra if key == 'class-form-conflict']
        education += [mapping[f] for f in ['class_type', 'head_teacher_id', 'classroom']]
        body = group('教育信息', education) + group('管理信息', [mapping['class_capacity']])
        body += '\n'.join(chunk for key, chunk in extra if key != 'class-form-conflict')
    else:
        body = group('基础信息', [mapping[f] for f in ['teacher_no', 'teacher_name', 'gender']])
        body += group('教育信息', [mapping[f] for f in ['school_id', 'edu_role']])
        body += group('职业信息', [mapping[f] for f in ['hire_date', 'employment_status']])
        body += group('联系方式', [mapping[f] for f in ['teacher_phone', 'email']])
        body += group('补充信息', [mapping['remark']])
    edits.append((grid.start, grid.full_end, '<div class="layout-sections">\n' + body + '\n</div>'))
    for start, end, replacement in sorted(edits, reverse=True): source = source[:start] + replacement + source[end:]
    if kind == 'class':
        source = re.sub(r'cellText\(row, (\d+)\)', lambda m: "cellText(row, '" + CLASS_FIELDS[int(m[1])] + "')", source)
        source = re.sub(r'row.children\[(\d+)\]', lambda m: "row.querySelector('td[data-field=\"" + CLASS_FIELDS[int(m[1])] + "\"]')", source)
        source = source.replace('function cellText(row, index) { return row.children[index].textContent', 'function cellText(row, field) { return row.querySelector(\'td[data-field="\' + field + \'"]\').textContent')
        source = source.replace('#class-rows tr[data-role="row"] td:first-child', '#class-rows tr[data-role="row"] td[data-field="class_name"]')
    else:
        source = re.sub(r'row.children\[(\d+)\]', lambda m: "row.querySelector('td[data-field=\"" + TEACHER_FIELDS[int(m[1])] + "\"]')", source)
    style = '<!-- CR-025 layout -->\n<style>\n[data-layout-group] h3 { color:var(--app-text-primary); }\ntable.el-table th[data-layout-group]::after { content:attr(data-layout-group); display:block; font-size:11px; color:var(--app-text-secondary); }\n.layout-sections .field { min-width:0; }\n.layout-sections .input, .layout-sections .select { width:100%; max-width:100%; min-width:0 !important; box-sizing:border-box; }\n.layout-sections .hint, .layout-sections .readonly-hint { overflow-wrap:anywhere; }\n</style>\n'
    source = source.replace('</head>', style + '</head>', 1)
    path.write_text(source, encoding='utf-8', newline='\n')

if __name__ == '__main__':
    for layer in ['functional', 'high-fidelity']:
        for page in ['class', 'teacher']:
            align(ROOT / f'prototypes/{layer}/v2/pages/{page}-list.html', page)
