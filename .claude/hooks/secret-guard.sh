#!/bin/bash
# PreToolUse(Write|Edit|MultiEdit): 파일에 API Key·비밀키처럼 보이는 값을 쓰려고 하면 막는다.
# 막을 때는 exit 2 + stderr 메시지. 통과는 exit 0.
input="$(cat)"

# .env 류 파일은 검사하지 않는다.
if printf '%s' "$input" | grep -Eq '"file_path"[[:space:]]*:[[:space:]]*"[^"]*\.env(\.[a-z]+)?"'; then
  exit 0
fi

patterns=(
  'sk-[A-Za-z0-9_-]{20,}'                                   # OpenAI 형식 키
  'AKIA[0-9A-Z]{16}'                                        # AWS Access Key
  'AIza[0-9A-Za-z_-]{35}'                                   # Google API Key
  'gh[pousr]_[A-Za-z0-9]{30,}'                              # GitHub 토큰
  '-----BEGIN [A-Z ]*PRIVATE KEY-----'                      # 개인 키
  '[Bb]earer [A-Za-z0-9._-]{24,}'                           # Bearer 토큰 값
  'jeb\.apiKey[[:space:]]*=[[:space:]]*[A-Za-z0-9._-]{16,}' # config.properties 실제 제브 키
)

for p in "${patterns[@]}"; do
  if printf '%s' "$input" | grep -Eq -- "$p"; then
    # 자리표시자는 허용
    match="$(printf '%s' "$input" | grep -Eo -- "$p" | head -1)"
    case "$match" in
      *YOUR_*|*your_*|*xxxx*|*XXXX*) continue ;;
    esac
    echo "[secret-guard] 비밀 정보로 보이는 값이 있어 파일 쓰기를 막았어요 (패턴: $p)." >&2
    echo "API Key나 비밀번호는 코드에 넣지 말고, 사용자가 config.properties에 직접 넣도록 안내하세요." >&2
    exit 2
  fi
done
exit 0
