#!/usr/bin/env bash

CREDENTIALS_DIR="credentials"
CREDENTIALS_FILE="$CREDENTIALS_DIR/credentials.txt"

mkdir -p "$CREDENTIALS_DIR"

# --- BCrypt hashing helper (cross-platform) ---
bcrypt_hash() {
  if command -v htpasswd >/dev/null 2>&1; then
    # Apache utils: works if available (e.g. Linux/macOS with apache2-utils)
    htpasswd -nbB user "$1" | cut -d: -f2
  elif command -v python3 >/dev/null 2>&1; then
    python3 - "$1" <<'EOF'
import bcrypt, sys
pw = sys.argv[1].encode()
print(bcrypt.hashpw(pw, bcrypt.gensalt()).decode())
EOF
  elif command -v python >/dev/null 2>&1; then
    python - "$1" <<'EOF'
import bcrypt, sys
pw = sys.argv[1].encode()
print(bcrypt.hashpw(pw, bcrypt.gensalt()).decode())
EOF
  else
    echo "Error: No bcrypt tool found (need htpasswd or Python with bcrypt)." >&2
    exit 1
  fi
}

# --- User management ---
add_user() {
  local user=$1
  local pass=$2
  local bcrypt_pw
  bcrypt_pw=$(bcrypt_hash "$pass")

  # Overwrite JSON with single user for now
  echo "{\"username\":\"$user\",\"password\":\"$bcrypt_pw\"}" > "$CREDENTIALS_FILE"
  echo "User $user added."
}

update_user() {
  local user=$1
  local pass=$2
  local bcrypt_pw
  bcrypt_pw=$(bcrypt_hash "$pass")

  echo "{\"username\":\"$user\",\"password\":\"$bcrypt_pw\"}" > "$CREDENTIALS_FILE"
  echo "Password updated for user $user."
}

delete_user() {
  local uname="$1"

  if [[ ! -f "$CREDENTIALS_FILE" ]]; then
    echo "No credentials file yet."
    return
  fi

  grep -v "\"username\":\"$uname\"" "$CREDENTIALS_FILE" > "$CREDENTIALS_FILE.tmp" && mv "$CREDENTIALS_FILE.tmp" "$CREDENTIALS_FILE"
  echo "User $uname deleted (if existed)."
}

list_users() {
  if [[ ! -f "$CREDENTIALS_FILE" ]]; then
    echo "No users yet."
    return
  fi

  echo "Users:"
  grep -o '"username":"[^"]*"' "$CREDENTIALS_FILE" | cut -d':' -f2 | tr -d '"'
}

show_password() {
  local uname="$1"

  if [[ ! -f "$CREDENTIALS_FILE" ]]; then
    echo "No credentials file yet."
    return
  fi

  local stored_pw
  stored_pw=$(grep "\"username\":\"$uname\"" "$CREDENTIALS_FILE" | sed -E 's/.*"password":"([^"]*)".*/\1/')
  if [[ -z "$stored_pw" ]]; then
    echo "User $uname not found."
    return
  fi

  echo "Stored BCrypt hash for $uname:"
  echo "$stored_pw"
}

# --- Startup ---
[[ ! -f "$CREDENTIALS_FILE" ]] && touch "$CREDENTIALS_FILE"

# --- Interactive Menu ---
while true; do
  echo ""
  echo "Choose an action:"
  echo "  1) Add user"
  echo "  2) Update user"
  echo "  3) Delete user"
  echo "  4) List users"
  echo "  5) Show password"
  echo "  6) Exit"
  read -rp "Enter choice [1-6]: " choice

  case "$choice" in
    1) read -rp "Username: " uname
       read -s -rp "Password: " pw; echo
       read -s -rp "Confirm: " pw2; echo
       [[ "$pw" == "$pw2" ]] || { echo "Mismatch"; continue; }
       add_user "$uname" "$pw"
       ;;
    2) read -rp "Username: " uname
       read -s -rp "New Password: " pw; echo
       read -s -rp "Confirm: " pw2; echo
       [[ "$pw" == "$pw2" ]] || { echo "Mismatch"; continue; }
       update_user "$uname" "$pw"
       ;;
    3) read -rp "Username: " uname
       delete_user "$uname"
       ;;
    4) list_users ;;
    5) read -rp "Username: " uname
       show_password "$uname"
       ;;
    6) echo "Exiting."; break ;;
    *) echo "Invalid choice." ;;
  esac
done

# Prevent auto-close on Windows
if [[ -n "$ComSpec" ]] || [[ -n "$PROMPT" ]]; then
  echo ""
  read -rp "Press Enter to exit..."
fi
