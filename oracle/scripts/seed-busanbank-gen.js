#!/usr/bin/env node
// definitions/부산은행/*.bpmn -> TB_BPM_PROCDEF INSERT (docker exec로 실행할 SQL 생성)
const fs = require('fs');
const path = require('path');

const DEF_DIR = path.join(__dirname, '../../definitions/부산은행');
const CHUNK_CHARS = 800; // Oracle string literal ~4000 bytes; 800 chars safe for UTF-8

function escape(s) {
  return s.replace(/'/g, "''");
}

const files = fs.readdirSync(DEF_DIR).filter(f => /\.bpmn$/i.test(f)).sort();
const out = [];
out.push('-- TB_BPM_PROCDEF seed: definitions/부산은행/*.bpmn');
out.push('SET DEFINE OFF;');
out.push('');

for (const fname of files) {
  const pathId = '부산은행/' + fname;
  const content = fs.readFileSync(path.join(DEF_DIR, fname), 'utf8');
  const esc = escape(content);
  const parts = [];
  for (let i = 0; i < esc.length; i += CHUNK_CHARS) {
    parts.push("TO_CLOB('" + esc.slice(i, i + CHUNK_CHARS) + "')");
  }
  const snapshotSql = parts.join(' || ');
  out.push("INSERT INTO TB_BPM_PROCDEF (id, path, name, is_directory, resource_type, snapshot, created_at, updated_at)");
  out.push("VALUES ('" + pathId.replace(/'/g, "''") + "', '" + pathId.replace(/'/g, "''") + "', '" + fname.replace(/'/g, "''") + "', 0, 'bpmn', " + snapshotSql + ", '20250311000000', '20250311000000');");
  out.push('');
}

out.push('COMMIT;');
out.push("SELECT COUNT(*) AS inserted FROM TB_BPM_PROCDEF WHERE path LIKE '부산은행/%';");
fs.writeFileSync(path.join(__dirname, 'seed-busanbank.sql'), out.join('\n'), 'utf8');
console.log('Generated seed-busanbank.sql with', files.length, 'BPMN files');
