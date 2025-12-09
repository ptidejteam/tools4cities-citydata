#!/usr/bin/env bash
set -euo pipefail

CREDENTIALS_DIR="credentials"
CREDENTIALS_FILE="$CREDENTIALS_DIR/credentials.txt"

mkdir -p "$CREDENTIALS_DIR"

# --- Ensure file exists and is a JSON array ---
if [[ ! -f "$CREDENTIALS_FILE" ]]; then
  echo "[]" > "$CREDENTIALS_FILE"
else
  python3 - <<'PY' || true
import json, sys, os
path = "credentials/credentials.txt"
try:
    data = json.load(open(path))
    if isinstance(data, dict):
        # convert single object -> array
        json.dump([data], open(path, "w"), indent=2)
except Exception:
    pass
PY
fi

# --- BCrypt hashing helper ---
bcrypt_hash() {
  if command -v htpasswd >/dev/null 2>&1; then
    htpasswd -nbB user "$1" | cut -d: -f2
  elif command -v python3 >/dev/null 2>&1; then
    python3 - "$1" <<'PY'
import bcrypt, sys
pw = sys.argv[1].encode()
print(bcrypt.hashpw(pw, bcrypt.gensalt()).decode())
PY
  else
    echo "Error: No bcrypt tool found (need htpasswd or python3 with bcrypt)." >&2
    exit 1
  fi
}

# --- JSON update helper using Python ---
py_update() {
  python3 - "$CREDENTIALS_FILE" <<'PY'
import json, sys
path = sys.argv[1]
code = sys.stdin.read()
with open(path) as f:
    try:
        data = json.load(f)
    except Exception:
        data = []
if isinstance(data, dict):
    data = [data]
# execute injected code block
locals_dict = {"data": data}
exec(code, {}, locals_dict)
with open(path, "w") as f:
    json.dump(locals_dict["data"], f, indent=2)
PY
}

# --- Check if user exists ---
user_exists() {
  local user=$1
  
  python3 - "$CREDENTIALS_FILE" "$user" <<'PY'
import json, sys, os

path, uname = sys.argv[1:3]

# Read existing data
if os.path.exists(path) and os.path.getsize(path) > 0:
    with open(path, 'r') as f:
        data = json.load(f)
else:
    data = []

# Ensure data is a list
if isinstance(data, dict):
    data = [data]

# Check for duplicate username
# Exit 0 (success) if user EXISTS, exit 1 (failure) if user does NOT exist
if any(u.get("username") == uname for u in data):
    sys.exit(0)
else:
    sys.exit(1)
PY
  
  return $?
}

# --- Add user ---
add_user() {
  local user=$1 pass=$2
  local bcrypt_pw
  bcrypt_pw=$(bcrypt_hash "$pass")

  python3 - "$CREDENTIALS_FILE" "$user" "$bcrypt_pw" <<'PY'
import json, sys, os

path, uname, pwhash = sys.argv[1:4]

# Read existing data
if os.path.exists(path) and os.path.getsize(path) > 0:
    with open(path, 'r') as f:
        data = json.load(f)
else:
    data = []

# Ensure data is a list
if isinstance(data, dict):
    data = [data]

# Add new user (duplicate check already done before this function)
data.append({"username": uname, "password": pwhash})

# Write back to file
with open(path, 'w') as f:
    json.dump(data, f, indent=2)

sys.exit(0)
PY
  
  local exit_code=$?
  if [[ $exit_code -eq 0 ]]; then
    echo "User $user added."
    return 0
  else
    echo "Error: Failed to add user." >&2
    return 1
  fi
}

# --- Update user ---
update_user() {
  local user=$1 pass=$2
  local bcrypt_pw
  bcrypt_pw=$(bcrypt_hash "$pass")

  python3 - "$CREDENTIALS_FILE" "$user" "$bcrypt_pw" <<'PY'
import json, sys
path, uname, pwhash = sys.argv[1:4]
data = json.load(open(path))
if isinstance(data, dict):
    data = [data]
found = False
for u in data:
    if u.get("username") == uname:
        u["password"] = pwhash
        found = True
        break
json.dump(data, open(path, "w"), indent=2)
if not found:
    print("User not found", file=sys.stderr)
    sys.exit(3)
PY
  if [[ $? -eq 0 ]]; then
    echo "Password updated for user $user."
  else
    echo "User not found."
  fi
}

# --- Delete user ---
delete_user() {
  local uname=$1
  python3 - "$CREDENTIALS_FILE" "$uname" <<'PY'
import json, sys
path, uname = sys.argv[1:3]
data = json.load(open(path))
if isinstance(data, dict):
    data = [data]
data = [u for u in data if u.get("username") != uname]
json.dump(data, open(path, "w"), indent=2)
PY
  echo "User $uname deleted (if existed)."
}

# --- List users ---
list_users() {
  python3 - "$CREDENTIALS_FILE" <<'PY'
import json, sys
path = sys.argv[1]
try:
    data = json.load(open(path))
except Exception:
    print("No users yet."); sys.exit(0)
if isinstance(data, dict):
    data = [data]
if not data:
    print("No users yet."); sys.exit(0)
print("Users:")
for u in data:
    if isinstance(u, dict) and "username" in u:
        print(u["username"])
PY
}

# --- Show password hash ---
show_password() {
  local uname=$1
  python3 - "$CREDENTIALS_FILE" "$uname" <<'PY'
import json, sys
path, uname = sys.argv[1], sys.argv[2]
try:
    data = json.load(open(path))
except Exception:
    print("User not found", file=sys.stderr); sys.exit(1)
if isinstance(data, dict):
    data = [data]
for u in data:
    if isinstance(u, dict) and u.get("username") == uname:
        print(u.get("password")); sys.exit(0)
print("User not found", file=sys.stderr); sys.exit(1)
PY
}

# --- Interactive Menu ---
while true; do
  echo ""
  echo "Choose an action:"
  echo "  1) Add user"
  echo "  2) Update user"
  echo "  3) Delete user"
  echo "  4) List users"
  echo "  5) Show password hash"
  echo "  6) Exit"
  read -rp "Enter choice [1-6]: " choice

  case "$choice" in
    1) read -rp "Username: " uname
       # Validate username is not empty
       if [[ -z "$uname" ]]; then
         echo "Error: Username cannot be empty."
         continue
       fi
       # Check if user already exists BEFORE asking for password
       if user_exists "$uname"; then
         echo "Error: User '$uname' already exists."
         continue
       fi
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
       echo "Stored BCrypt hash for $uname:"
       show_password "$uname" || true
       ;;
    6) echo "Exiting."; break ;;
    *) echo "Invalid choice." ;;
  esac
done

# --- Prevent auto-close on Windows ---
if [[ -n "${ComSpec:-}" ]] || [[ -n "${PROMPT:-}" ]]; then
  echo ""
  read -rp "Press Enter to exit..."
fi