#!/usr/bin/env bash
set -e

# === COLORS ===
green="\033[32m"
blue="\033[34m"
purple="\033[35m"
cyan="\033[36m"
gray="\033[90m"
reset="\033[0m"

# === CUSTOM ECHO ===
function custom_echo {
  local timestamp=$(date +"%H:%M:%S")
  local level=${2:-INFO}
  local color=$cyan

  case "$level" in
    INFO) color=$cyan ;;
    WARN) color="\033[33m" ;; # jaune
    ERROR) color="\033[31m" ;; # rouge
  esac

  echo -e "${gray}[${reset}${cyan}${timestamp}${reset}${gray}] [${reset}${green}Romain${reset} ${blue}GUILLEMOT${reset} ${purple}MS-AUTH${reset}${gray}] ${color}${level}${reset} $1"
}

# === ASCII BANNER ===
echo -e "${green}▄▄▄        • ▌ ▄ ·.  ▄▄▄· ▪   ▐ ▄    ${blue}  ▄▄ • ▄• ▄▌▪  ▄▄▌  ▄▄▌  ▄▄▄ .• ▌ ▄ ·.       ▄▄▄▄▄"
echo -e "${green}▀▄ █·▪     ·██ ▐███▪▐█ ▀█ ██ •█▌▐█  ${blue}  ▐█ ▀ ▪█▪██▌██ ██•  ██•  ▀▄.▀··██ ▐███▪▪     •██  "
echo -e "${green}▐▀▀▄  ▄█▀▄ ▐█ ▌▐▌▐█·▄█▀▀█ ▐█·▐█▐▐▌  ${blue}  ▄█ ▀█▄█▌▐█▌▐█·██▪  ██▪  ▐▀▀▪▄▐█ ▌▐▌▐█· ▄█▀▄  ▐█.▪"
echo -e "${green}▐█•█▌▐█▌.▐▌██ ██▌▐█▌▐█ ▪▐▌▐█▌██▐█▌  ${blue}  ▐█▄▪▐█▐█▄█▌▐█▌▐█▌▐▌▐█▌▐▌▐█▄▄▌██ ██▌▐█▌▐█▌.▐▌ ▐█▌·"
echo -e "${green}.▀  ▀ ▀█▄▀▪▀▀  █▪▀▀▀ ▀  ▀ ▀▀▀▀▀ █▪  ${blue}  ·▀▀▀▀  ▀▀▀ ▀▀▀.▀▀▀ .▀▀▀  ▀▀▀ ▀▀  █▪▀▀▀ ▀█▄▀▪ ▀▀▀ "
echo -e "${purple}                                 MS-AUTH                                   "
custom_echo "Launching MS-AUTH Startup Script..."

# === GIT CONFIG ===
custom_echo "Checking Git configuration..."
sleep 1
if ! git config --global user.email >/dev/null; then
  custom_echo "Git configuration not found. Prompting for user details..." WARN
  read -p "Enter your Git email: " GIT_EMAIL
  git config --global user.email "$GIT_EMAIL"
  read -p "Enter your Git name: " GIT_NAME
  git config --global user.name "$GIT_NAME"
  custom_echo "Git configuration complete."
else
  GIT_NAME=$(git config --global user.name)
  GIT_EMAIL=$(git config --global user.email)
  custom_echo "Git configuration already present: Name=$GIT_NAME, Email=$GIT_EMAIL"
fi

# === ENV VARIABLES ===
custom_echo "Setting up environment variables..."
export DB_HOST=db
export DB_PORT=5432
export DB_NAME=postgres
export DB_USER=postgres
export DB_PASS=postgres

custom_echo "Environment variables set (DB_HOST=$DB_HOST, DB_PORT=$DB_PORT)."

# === KILL EXISTING SPRING BOOT ON 8080 ===
custom_echo "Killing any existing processes on port 8080..."
if lsof -ti:8080 >/dev/null 2>&1; then
    kill -9 $(lsof -ti:8080)
    custom_echo "Processes on port 8080 terminated." WARN
else
    custom_echo "No processes found on port 8080."
fi

# === TIMEZONE SETUP ===
custom_echo "Ensuring Europe/Paris timezone is set..."
if [ -w /etc/timezone ]; then
  if [ ! -f /etc/timezone ] || [ "$(cat /etc/timezone)" != "Europe/Paris" ]; then
      echo "Europe/Paris" | sudo tee /etc/timezone > /dev/null
      sudo dpkg-reconfigure -f noninteractive tzdata > /dev/null 2>&1
      custom_echo "Timezone set to Europe/Paris."
  else
      custom_echo "Timezone is already set to Europe/Paris."
  fi
else
  custom_echo "Skipping timezone update (no permission)." WARN
fi

custom_echo "Checking if PostgreSQL is ready (TCP check)..."
for i in {1..10}; do
    if (echo > /dev/tcp/$DB_HOST/$DB_PORT) >/dev/null 2>&1; then
        custom_echo "PostgreSQL is reachable on $DB_HOST:$DB_PORT"
        break
    else
        custom_echo "PostgreSQL not ready yet... retrying ($i/10)"
        sleep 2
    fi
done

# === START SPRING BOOT ===
custom_echo "Starting Spring Boot on port 8080..."
chmod +x mvnw
./mvnw spring-boot:run
