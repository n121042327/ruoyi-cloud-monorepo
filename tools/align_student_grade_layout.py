"""按 CR-027 整理学生、年级 v2 的表格列序与表单分区。

只处理 prototypes/{functional,high-fidelity}/v2/pages/{student-list,grade-list}.html，
检测到 CR-027 标记时直接跳过，保证重复执行安全。
"""
from html.parser import HTMLParser
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
VOID = {'input', 'meta', 'link', 'br', 'hr', 'img', 'source', 'wbr'}

STUDENT_FIELDS = ['_selection', 'student_no', 'student_name', 'gender', 'stage_code', 'grade_name',
                  'class_name', 'enrollment_status', 'enroll_year', 'student_phone', 'update_time', 'actions']
STUDENT_OLD_ORDER = STUDENT_FIELDS
STUDENT_ORDER = ['_selection', 'student_no', 'student_name', 'gender', 'enroll_year', 'stage_code',
                 'grade_name', 'class_name', 'enrollment_status', 'student_phone', 'update_time', 'actions']
STUDENT_GROUPS = {'_selection': '选择', 'student_no': '基础信息', 'student_name': '基础信息', 'gender': '基础信息',
                  'enroll_year': '教育信息', 'stage_code': '教育信息', 'grade_name': '教育信息',
                  'class_name': '教育信息', 'enrollment_status': '教育信息',
                  'student_phone': '联系方式', 'update_time': '管理信息', 'actions': '操作'}

GRADE_FIELDS = ['grade_name', 'stage_code', 'enroll_year', 'grade_level', 'class_count',
                'student_count', 'leader_user_id', 'grade_status', 'actions']
GRADE_OLD_ORDER = GRADE_FIELDS
GRADE_ORDER = ['stage_code', 'enroll_year', 'grade_name', 'grade_level', 'leader_user_id',
               'class_count', 'student_count', 'grade_status', 'actions']
GRADE_GROUPS = {'stage_code': '教育信息', 'enroll_year': '教育信息', 'grade_name': '教育信息',
                'grade_level': '教育信息', 'leader_user_id': '教育信息',
                'class_count': '管理信息', 'student_count': '管理信息',
                'grade_status': '管理信息', 'actions': '操作'}

STUDENT_STEP1 = [('基础信息', ['__学号', '__全国学籍号', '__姓名', '__性别']),
                 ('教育信息', ['__入学年份', '__学段', '__年级', '__行政班', '__学籍状态', '__入学日期']),
                 ('补充信息', ['__学生照片'])]
STUDENT_STEP2 = [('证件信息', ['__证件类型', '__证件号码', '__出生日期']),
                 ('联系方式', ['__学生手机号', '__联系地址'])]
STUDENT_EDIT_STEP1 = [('基础信息', ['__学号', '__全国学籍号', '__姓名', '__性别']),
                      ('教育信息', ['__入学年份', '__学段 / 年级', '__行政班', '__学籍状态'])]

SECTION_CSS = ('\n.layout-sections .field { min-width:0; }\n'
               '.layout-sections .input, .layout-sections .select { width:100%; max-width:100%;'
               ' min-width:0 !important; box-sizing:border-box; }\n'
               '.layout-sections .hint, .layout-sections .readonly-hint { overflow-wrap:anywhere; }\n'
               '.layout-sections > h3 { margin:12px 0 8px; font-size:14px; color:var(--app-text-primary); }\n'
               'table.el-table th[data-layout-group]::after { content:attr(data-layout-group); display:block;'
               ' font-size:11px; color:var(--app-text-secondary); }\n')


class Node:
    def __init__(self, tag, attrs, start, open_end):
        self.tag, self.attrs, self.start, self.open_end = tag, dict(attrs), start, open_end
        self.end, self.full_end, self.children = open_end, open_end, []


class Parser(HTMLParser):
    def __init__(self, source):
        super().__init__(convert_charrefs=False)
        self.source, self.nodes, self.stack = source, [], []
        self.lines = [0]
        for m in re.finditer('\n', source):
            self.lines.append(m.end())
        self.feed(source)

    def position(self):
        line, column = self.getpos()
        return self.lines[line - 1] + column

    def handle_starttag(self, tag, attrs):
        start = self.position()
        node = Node(tag, attrs, start, start + len(self.get_starttag_text()))
        if self.stack:
            self.stack[-1].children.append(node)
        self.nodes.append(node)
        if tag not in VOID:
            self.stack.append(node)

    def handle_startendtag(self, tag, attrs):
        self.handle_starttag(tag, attrs)
        if self.stack and self.stack[-1].tag == tag:
            self.stack.pop()

    def handle_endtag(self, tag):
        if self.stack and self.stack[-1].tag == tag:
            node = self.stack.pop()
            node.end = self.position()
            node.full_end = self.source.index('>', node.end) + 1


def indent_of(source, node):
    line_start = source.rfind('\n', 0, node.start) + 1
    return source[line_start:node.start]


def find(nodes, **attrs):
    for node in nodes:
        if all(node.attrs.get(key) == value for key, value in attrs.items()):
            return node
    raise LookupError(attrs)


def decorate(opening, field, groups, with_field=True):
    extra = ' data-layout-group="' + groups[field] + '"'
    if with_field and field not in ('_selection', 'actions') and 'data-field=' not in opening:
        extra = ' data-field="' + field + '"' + extra
    return opening[:-1] + extra + '>'


def rewrite_table(source, parser, table_id, fields, order, groups):
    table = find(parser.nodes, id=table_id)
    edits = []
    for row in [n for n in parser.nodes if n.tag == 'tr' and table.start < n.start < table.end]:
        cells = [n for n in row.children if n.tag in ('td', 'th')]
        assert len(cells) == len(fields), (table_id, len(cells), len(fields))
        chunks = {}
        for field, cell in zip(fields, cells):
            chunks[field] = decorate(source[cell.start:cell.open_end], field, groups) + \
                source[cell.open_end:cell.full_end]
        indent = indent_of(source, cells[0])
        tail = source[cells[-1].full_end:row.end]
        inner = '\n' + '\n'.join(indent + chunks[field] for field in order) + '\n' + tail.strip('\n')
        edits.append((row.open_end, row.end, inner))
    for start, end, replacement in sorted(edits, reverse=True):
        source = source[:start] + replacement + source[end:]
    return source


def rewrite_cell_calls(source, old_order):
    """把 cell(x, N) 的列号调用换成字段名，避免列序变化后读错列。"""
    def repl(match):
        index = int(match.group(2))
        assert index < len(old_order), index
        return "cell(%s, '%s')" % (match.group(1), old_order[index])
    return re.sub(r'cell\(([A-Za-z_$][\w$]*), *(\d+)\)', repl, source)


def field_key(node, source):
    if 'data-field' in node.attrs:
        return node.attrs['data-field']
    for child in node.children:
        if child.tag == 'label':
            text = re.sub(r'<[^>]+>', '', source[child.open_end:child.end])
            return '__' + text.strip().rstrip('*').strip()
    return None


def wrap_groups(source, grid, groups):
    """把一个 form-grid 拆成语义分区；groups 为 (组名, [字段 key]) 列表。"""
    fields = [child for child in grid.children if 'field' in child.attrs.get('class', '').split()]
    if not fields:
        raise AssertionError('no .field in grid')
    chunks = {}
    for node in fields:
        key = field_key(node, source)
        chunks[key] = source[node.start:node.full_end]
    base = indent_of(source, grid)
    seen = set()
    blocks = []
    for name, keys in groups:
        cells = []
        for key in keys:
            assert key in chunks, (name, key, list(chunks))
            assert key not in seen, key
            seen.add(key)
            cells.append(base + '    ' + chunks[key])
        blocks.append(base + '<section data-layout-group="' + name + '">\n' +
                      base + '  <h3>' + name + '</h3>\n' +
                      base + '  <div class="form-grid">\n' + '\n'.join(cells) + '\n' +
                      base + '  </div>\n' + base + '</section>')
    assert seen == set(chunks), set(chunks) - seen
    return '\n'.join(blocks) + '\n'


def find_grid(parser, panel_id, step):
    panel = find(parser.nodes, **{'data-demo-panel': panel_id})
    step_node = find([n for n in parser.nodes if n.attrs.get('data-step-content') == step and
                      panel.start < n.start < panel.end], **{'data-step-content': step})
    grid = next(n for n in step_node.children if n.attrs.get('class') == 'form-grid')
    return panel, step_node, grid


def add_heading(source, parser, panel_id, step, name):
    panel = find(parser.nodes, **{'data-demo-panel': panel_id})
    step_node = find([n for n in parser.nodes if n.attrs.get('data-step-content') == step and
                      panel.start < n.start < panel.end], **{'data-step-content': step})
    card = next(n for n in step_node.children if n.tag == 'div' and 'card' in n.attrs.get('class', '').split())
    indent = indent_of(source, card)
    heading = indent + '<h3 data-layout-group="' + name + '">' + name + '</h3>\n'
    return source[:card.start] + heading + source[card.start:]


def wrap_flat_fields(source, parser, panel_id, name, keys):
    panel = find(parser.nodes, **{'data-demo-panel': panel_id})
    fields = [n for n in parser.nodes
              if panel.start < n.start < panel.end and 'field' in n.attrs.get('class', '').split()]
    chosen = [n for n in fields if field_key(n, source) in keys]
    assert len(chosen) == len(keys), (panel_id, [field_key(n, source) for n in chosen])
    chosen.sort(key=lambda n: n.start)
    base = indent_of(source, chosen[0])
    body = '\n'.join(base + '    ' + source[n.start:n.full_end] for n in chosen)
    block = (base + '<section data-layout-group="' + name + '">\n' +
             base + '  <h3>' + name + '</h3>\n' +
             base + '  <div class="form-grid">\n' + body + '\n' +
             base + '  </div>\n' + base + '</section>')
    return source[:chosen[0].start] + block + source[chosen[-1].full_end:]


def align(path, kind):
    source = path.read_text(encoding='utf-8')
    if '<!-- CR-027 layout -->' in source:
        return
    parser = Parser(source)
    if kind == 'student':
        source = rewrite_table(source, parser, 'student-table', STUDENT_FIELDS, STUDENT_ORDER, STUDENT_GROUPS)
        parser = Parser(source)
        for panel_id, groups in [('PAGE-STU-CREATE', STUDENT_STEP1), ('PAGE-STU-EDIT', STUDENT_EDIT_STEP1)]:
            _, _, grid = find_grid(parser, panel_id, '1')
            source = source[:grid.start] + wrap_groups(source, grid, groups) + source[grid.full_end:]
            parser = Parser(source)
            _, _, grid = find_grid(parser, panel_id, '2')
            source = source[:grid.start] + wrap_groups(source, grid, STUDENT_STEP2) + source[grid.full_end:]
            parser = Parser(source)
            source = add_heading(source, parser, panel_id, '3', '监护人信息')
            parser = Parser(source)
        source = source.replace(
            "function cell(tr, i) { return tr.children[i] ? tr.children[i].textContent.replace(/\\s+/g, ' ').trim() : ''; }",
            "function cell(tr, field) {\n"
            "    var td = tr.querySelector('td[data-field=\"' + field + '\"]');\n"
            "    return td ? td.textContent.replace(/\\s+/g, ' ').trim() : '';\n"
            "  }")
        source = source.replace(
            "return { no: cell(tr, 1), name: cell(tr, 2), gender: cell(tr, 3), stage: cell(tr, 4),\n"
            "             grade: cell(tr, 5), klass: cell(tr, 6), status: cell(tr, 7) };",
            "return { no: cell(tr, 'student_no'), name: cell(tr, 'student_name'), gender: cell(tr, 'gender'),\n"
            "             year: cell(tr, 'enroll_year'), stage: cell(tr, 'stage_code'), grade: cell(tr, 'grade_name'),\n"
            "             klass: cell(tr, 'class_name'), status: cell(tr, 'enrollment_status') };")
        source = rewrite_cell_calls(source, STUDENT_OLD_ORDER)
        source = source.replace('#student-rows tr[data-role="row"] td:first-child',
                                '#student-rows tr[data-role="row"] td[data-field="student_name"]')
    else:
        source = rewrite_table(source, parser, 'grade-table', GRADE_FIELDS, GRADE_ORDER, GRADE_GROUPS)
        parser = Parser(source)
        source = wrap_flat_fields(source, parser, 'PAGE-GRD-CREATE', '教育信息',
                                  ['school_id', 'stage_code', 'enroll_year', 'grade_level', 'grade_name'])
        parser = Parser(source)
        source = wrap_flat_fields(source, parser, 'PAGE-GRD-BATCH', '教育信息',
                                  ['stage_code', 'enroll_year', '__将生成的年级'])
        parser = Parser(source)
        source = re.sub(r"row\.children\[(\d+)\]", lambda m: "row.querySelector('td[data-field=\"%s\"]')"
                        % GRADE_FIELDS[int(m[1])], source)
        source = source.replace("function cell(row, index) { return row.children[index].textContent.trim(); }",
                                "function cell(row, field) {\n"
                                "    var td = row.querySelector('td[data-field=\"' + field + '\"]');\n"
                                "    return td ? td.textContent.trim() : '';\n"
                                "  }")
        source = rewrite_cell_calls(source, GRADE_OLD_ORDER)
        source = source.replace('#grade-rows tr[data-role="row"] td:first-child',
                                '#grade-rows tr[data-role="row"] td[data-field="grade_name"]')
    style = '<!-- CR-027 layout -->\n<style>' + SECTION_CSS + '</style>\n'
    source = source.replace('</head>', style + '</head>', 1)
    path.write_text(source, encoding='utf-8', newline='\n')


if __name__ == '__main__':
    for layer in ['functional', 'high-fidelity']:
        align(ROOT / f'prototypes/{layer}/v2/pages/student-list.html', 'student')
        align(ROOT / f'prototypes/{layer}/v2/pages/grade-list.html', 'grade')
