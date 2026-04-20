#!/bin/bash
# ─────────────────────────────────────────────────────────────────
# Hindu Wallpaper App — GitHub Auto Setup Script
# Run this in Termux or any Linux terminal
# ─────────────────────────────────────────────────────────────────

GH_TOKEN="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"
REPO_NAME="hindu-wallpaper-app"
DESCRIPTION="🕉 Hindu Wallpaper App — HD image & live wallpapers, WhatsApp status, AdMob ads. Built with Jetpack Compose + Supabase. by Mahi Info"

echo ""
echo "🕉  Hindu Wallpaper App — GitHub Setup"
echo "======================================="
echo ""

# ─── Step 1: Get username ────────────────────────────────────────
echo "📡 Connecting to GitHub..."
USERNAME=$(curl -s \
  -H "Authorization: token $GH_TOKEN" \
  -H "Accept: application/vnd.github.v3+json" \
  https://api.github.com/user | python3 -c "import sys,json; d=json.load(sys.stdin); print(d['login'])")

if [ -z "$USERNAME" ]; then
  echo "❌ Could not get GitHub username. Check your token."
  exit 1
fi
echo "✅ Logged in as: $USERNAME"

# ─── Step 2: Create repo ─────────────────────────────────────────
echo ""
echo "📦 Creating repo: $USERNAME/$REPO_NAME ..."
CREATE_RESPONSE=$(curl -s \
  -X POST \
  -H "Authorization: token $GH_TOKEN" \
  -H "Accept: application/vnd.github.v3+json" \
  https://api.github.com/user/repos \
  -d "{
    \"name\": \"$REPO_NAME\",
    \"description\": \"$DESCRIPTION\",
    \"private\": false,
    \"auto_init\": false,
    \"has_issues\": true,
    \"has_projects\": false,
    \"has_wiki\": false
  }")

REPO_URL=$(echo "$CREATE_RESPONSE" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('html_url',''))" 2>/dev/null)

if [ -z "$REPO_URL" ]; then
  # Repo might already exist
  REPO_URL="https://github.com/$USERNAME/$REPO_NAME"
  echo "⚠️  Repo may already exist, continuing..."
else
  echo "✅ Repo created: $REPO_URL"
fi

# ─── Step 3: Clone or use existing project ───────────────────────
echo ""
echo "📂 Setting up local project..."

if [ -d "$REPO_NAME" ]; then
  echo "📁 Folder $REPO_NAME already exists, using it."
  cd "$REPO_NAME"
else
  # Download the zip and extract
  echo "⬇️  Downloading project zip..."
  curl -L -o hindu-wallpaper-app.zip \
    "https://github.com/$USERNAME/$REPO_NAME/archive/refs/heads/main.zip" 2>/dev/null \
    || echo "Will create fresh..."

  mkdir -p "$REPO_NAME"
  cd "$REPO_NAME"
  git init
  git config user.email "devendrabijarania41@gmail.com"
  git config user.name "Dev Choudhary"
fi

# ─── Step 4: Set remote and push ─────────────────────────────────
echo ""
echo "🚀 Pushing to GitHub..."

REMOTE_URL="https://$GH_TOKEN@github.com/$USERNAME/$REPO_NAME.git"

git remote remove origin 2>/dev/null || true
git remote add origin "$REMOTE_URL"
git branch -M main

git push -u origin main --force

if [ $? -eq 0 ]; then
  echo ""
  echo "✅ ════════════════════════════════════════"
  echo "   PUSH SUCCESSFUL!"
  echo "   🔗 $REPO_URL"
  echo "════════════════════════════════════════"
else
  echo "❌ Push failed. Check errors above."
  exit 1
fi

# ─── Step 5: Add GitHub Secrets via API ──────────────────────────
echo ""
echo "🔑 Setting up GitHub Secrets for CI/CD..."

# Get repo public key for secret encryption
PUB_KEY_RESPONSE=$(curl -s \
  -H "Authorization: token $GH_TOKEN" \
  -H "Accept: application/vnd.github.v3+json" \
  "https://api.github.com/repos/$USERNAME/$REPO_NAME/actions/secrets/public-key")

KEY_ID=$(echo "$PUB_KEY_RESPONSE" | python3 -c "import sys,json; print(json.load(sys.stdin)['key_id'])" 2>/dev/null)
PUB_KEY=$(echo "$PUB_KEY_RESPONSE" | python3 -c "import sys,json; print(json.load(sys.stdin)['key'])" 2>/dev/null)

set_secret() {
  local SECRET_NAME="$1"
  local SECRET_VALUE="$2"

  # Encrypt secret using libsodium (requires PyNaCl)
  ENCRYPTED=$(python3 -c "
import base64
from nacl import encoding, public

pub_key = public.PublicKey(base64.b64decode('$PUB_KEY'))
box = public.SealedBox(pub_key)
encrypted = box.encrypt('$SECRET_VALUE'.encode())
print(base64.b64encode(encrypted).decode())
" 2>/dev/null)

  if [ -z "$ENCRYPTED" ]; then
    echo "   ⚠️  PyNaCl not installed — skipping secret encryption for $SECRET_NAME"
    echo "      Install with: pip install PyNaCl"
    return
  fi

  curl -s -X PUT \
    -H "Authorization: token $GH_TOKEN" \
    -H "Accept: application/vnd.github.v3+json" \
    "https://api.github.com/repos/$USERNAME/$REPO_NAME/actions/secrets/$SECRET_NAME" \
    -d "{\"encrypted_value\":\"$ENCRYPTED\",\"key_id\":\"$KEY_ID\"}" > /dev/null

  echo "   ✅ Secret set: $SECRET_NAME"
}

# Set Supabase secrets
set_secret "SUPABASE_URL" "https://vrznwyssfmelgoyzebvk.supabase.co"
set_secret "SUPABASE_ANON_KEY" "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InZyem53eXNzZm1lbGdveXplYnZrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzY2NTE1ODcsImV4cCI6MjA5MjIyNzU4N30.LOSDZJBQNloLkda2vYbS1GWtTw5yPHXRx_7Nm8nQlDU"

echo ""
echo "════════════════════════════════════════"
echo "🎉 ALL DONE!"
echo ""
echo "📱 App repo:    https://github.com/$USERNAME/$REPO_NAME"
echo "🗄️  Supabase:   https://vrznwyssfmelgoyzebvk.supabase.co"
echo "⚙️  Actions:    https://github.com/$USERNAME/$REPO_NAME/actions"
echo ""
echo "Next steps:"
echo "  1. Add keystore secrets to GitHub for signed builds"
echo "  2. Push a tag (git tag v1.0.0 && git push --tags)"
echo "     → GitHub Actions will auto-build + sign + release"
echo "  3. Update AdMob IDs in Supabase app_config table"
echo "════════════════════════════════════════"
