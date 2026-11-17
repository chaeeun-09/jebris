#!/bin/bash
# PreToolUse(Bash): 되돌릴 수 없는 위험한 명령을 막는다. 막을 때는 exit 2.
input="$(cat)"

patterns=(
  'rm[[:space:]]+-[a-zA-Z]*r[a-zA-Z]*f?[[:space:]]+(/|~|\$HOME|\*|\.)([[:space:]"]|$)'  # rm -rf / ~ * .
  'rm[[:space:]]+-[a-zA-Z]*f[a-zA-Z]*r[[:space:]]+(/|~|\$HOME|\*|\.)([[:space:]"]|$)'
  'sudo[[:space:]]+rm'
  'dd[[:space:]]+if='
  'mkfs'
  '>[[:space:]]*/dev/(sd|nvme|disk)'
  'git[[:space:]]+push[[:space:]].*(--force|-f)([[:space:]"]|$)'
  'git[[:space:]]+reset[[:space:]]+--hard'
  'git[[:space:]]+clean[[:space:]]+-[a-zA-Z]*f'
  'DROP[[:space:]]+(USER|TABLESPACE|DATABASE)'
  'format[[:space:]]+[a-zA-Z]:'
  'rd[[:space:]]+/s'
  'del[[:space:]]+/[sqf]'
)

for p in "${patterns[@]}"; do
  if printf '%s' "$input" | grep -Eiq -- "$p"; then
    echo "[command-guard] 위험한 명령이라 실행을 막았어요 (패턴: $p)." >&2
    echo "꼭 필요하면 사용자에게 무엇을 지우거나 바꾸는지 설명하고 직접 실행해 달라고 하세요." >&2
    exit 2
  fi
done
exit 0
