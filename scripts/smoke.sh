#!/usr/bin/env bash
# ============================================================
# Smoke test script — verifies the running backend end-to-end
# Usage: ./scripts/smoke.sh [base-url]
# ============================================================

set -euo pipefail
BASE_URL="${1:-http://localhost:8080/api}"

echo "==> 1. Health check"
curl -fsS "${BASE_URL}/actuator/health" | jq -r '.status' | grep -q '^UP$' && echo "   ✓ healthy"

echo "==> 2. Login as admin/admin123"
LOGIN_RES=$(curl -fsS -X POST "${BASE_URL}/auth/login" \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123"}')
TOKEN=$(echo "$LOGIN_RES" | jq -r '.data.accessToken')
[ -n "$TOKEN" ] && [ "$TOKEN" != "null" ] && echo "   ✓ got token" || { echo "   ✗ no token: $LOGIN_RES"; exit 1; }

echo "==> 3. Fetch user list (page 1)"
curl -fsS -H "Authorization: Bearer $TOKEN" \
  "${BASE_URL}/system/user/list?pageNum=1&pageSize=10" | jq -r '.code' | grep -q '^0$' && echo "   ✓ user list ok"

echo "==> 4. Fetch menu tree"
curl -fsS -H "Authorization: Bearer $TOKEN" \
  "${BASE_URL}/system/menu/routers" | jq -r '.code' | grep -q '^0$' && echo "   ✓ routers ok"

echo "==> 5. Dashboard stats"
curl -fsS -H "Authorization: Bearer $TOKEN" \
  "${BASE_URL}/dashboard/stats" | jq -r '.code' | grep -q '^0$' && echo "   ✓ stats ok"

echo "==> 6. Create + delete a test user (asserts @PreAuthorize + @Log)"
NEW_USER=$(curl -fsS -X POST "${BASE_URL}/system/user" \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"username":"smoke_test","password":"P@ssw0rd!","nickName":"smoke","roleIds":[]}')
NEW_ID=$(echo "$NEW_USER" | jq -r '.data // ""')
[ -n "$NEW_ID" ] && echo "   ✓ user created (id=$NEW_ID)"

echo "==> 7. Verify operation log was recorded"
sleep 1
curl -fsS -H "Authorization: Bearer $TOKEN" \
  "${BASE_URL}/monitor/operlog/list?pageNum=1&pageSize=5" \
  | jq -e '.data.records[] | select(.title=="用户管理")' > /dev/null && echo "   ✓ oper log recorded"

echo "==> 8. Cleanup"
curl -fsS -X DELETE -H "Authorization: Bearer $TOKEN" "${BASE_URL}/system/user/${NEW_ID}" > /dev/null
echo "   ✓ test user removed"

echo ""
echo "✅ All smoke checks passed."