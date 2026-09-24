#!/usr/bin/env python3
"""PreToolUse guard: stop Claude's own Edit/Write calls from touching secret files.
Human edits made outside Claude Code (in a normal editor) are unaffected."""
import fnmatch
import json
import sys

SECRET_PATTERNS = [
    "*/application.properties",
    "*/application-prod.properties",
    "*/application-local.properties",
    "*/src/main/resources/firebase/*.json",
    "*/.env",
    ".env",
]


def main():
    try:
        data = json.load(sys.stdin)
    except Exception:
        return

    file_path = data.get("tool_input", {}).get("file_path", "")
    if not file_path:
        return

    normalized = file_path.replace("\\", "/")
    for pattern in SECRET_PATTERNS:
        if fnmatch.fnmatch(normalized, pattern):
            reason = (
                f"시크릿 파일은 Claude가 직접 수정할 수 없습니다: {file_path}. "
                "DB/JWT/Firebase/메일 자격 증명이 담긴 파일이라 AI 세션 컨텍스트나 "
                "커밋에 노출될 위험이 있어 차단됩니다. 필요한 값은 사람이 직접 에디터에서 수정해주세요."
            )
            print(json.dumps({
                "hookSpecificOutput": {
                    "hookEventName": "PreToolUse",
                    "permissionDecision": "deny",
                    "permissionDecisionReason": reason,
                }
            }))
            return


if __name__ == "__main__":
    main()
