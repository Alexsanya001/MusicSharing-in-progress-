#!/bin/bash
#=====================================================================================================================
#             This script creates a certificate for Minio and adds it to Java-trusted-certificates
#=====================================================================================================================
# Detect OS and set specific variables
case "$(uname -s)" in
    Linux*)     OS=LINUX;;
    Darwin*)    OS=MAC;;
    *)          OS=UNKNOWN;;
esac

# Function for cross-platform readlink
function get_realpath() {
    if [ "$OS" = "MAC" ]; then
        # macOS version (requires coreutils)
        if command -v greadlink >/dev/null 2>&1; then
            greadlink -f "$1" 2>/dev/null
        else
            echo >&2 "ERROR: On macOS please install coreutils: brew install coreutils"
            exit 1
        fi
    else
        # Linux version
        readlink -f "$1" 2>/dev/null
    fi
}

# Function to find Java installations
find_java_installations() {
    local java_homes=()

    # Common paths for both OS
    local common_paths=(
        "$JAVA_HOME"
        "$(dirname "$(dirname "$(get_realpath "$(which java 2>/dev/null)" 2>/dev/null)" 2>/dev/null)" 2>/dev/null)"
    )

    # OS-specific paths
    if [ "$OS" = "MAC" ]; then
        local mac_paths=(
            "$(/usr/libexec/java_home 2>/dev/null)"
            "/Library/Java/JavaVirtualMachines/*/Contents/Home"
            "${HOME}/.sdkman/candidates/java/*"
        )
        possible_paths=("${common_paths[@]}" "${mac_paths[@]}")
    else
        local linux_paths=(
            "/usr/lib/jvm/*"
            "/usr/java/*"
            "/etc/alternatives/java_sdk"
            "${HOME}/.sdkman/candidates/java/*"
        )
        possible_paths=("${common_paths[@]}" "${linux_paths[@]}")
    fi

    # Find all valid Java homes with cacerts
    for path in "${possible_paths[@]}"; do
        # Expand glob patterns safely
        for expanded_path in $path; do
            if [ -d "$expanded_path" ] && [ -f "$expanded_path/lib/security/cacerts" ]; then
                java_homes+=("$expanded_path")
            fi
        done
    done

    # Remove duplicates
    if [ ${#java_homes[@]} -gt 0 ]; then
        printf "%s\n" "${java_homes[@]}" | awk '!seen[$0]++'
    fi
}

# Main script
echo "=== MinIO Certificate Setup ==="
echo "Detected OS: $OS"

# Get host function
get_minio_host() {
	read -rp 'Set host where Minio will work (IPV4, IPV6 or domain name) or press ENTER for default value "localhost": ' HOST
	MINIO_HOST=${HOST:-localhost}
	validate_host "$MINIO_HOST"
}

# Check address function
validate_host() {
    local host="$1"

    # Regex for IPv4, IPv6 and domain
    local regex="^([a-zA-Z0-9]([a-zA-Z0-9-]*[a-zA-Z0-9])?\.)*[a-zA-Z]{2,63}$|^([0-9]{1,3}\.){3}[0-9]{1,3}$|^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$|^::1$|^([0-9a-fA-F]{1,4}:){1,7}:$"

    if [[ ! $host =~ $regex ]]; then
        echo "Error: '$host' is not a valid hostname or IP-address (domain.com, IPv4 or IPv6)"
        get_minio_host
    fi

    # Additional check for IPv4 (excluding, for example, 256.256.256.256)
    if [[ $host =~ ^([0-9]{1,3}\.){3}[0-9]{1,3}$ ]]; then
        IFS='.' read -r a b c d <<< "$host"
        if ((a > 255 || b > 255 || c > 255 || d > 255)); then
            echo "Error: '$host' is not a valid IPv4-address!"
            get_minio_host
        fi
    fi

    return 0
}

# Get minio host
get_minio_host

# Find Java installations
echo "Searching for Java installations..."
mapfile -t java_homes < <(find_java_installations)

# If Java not found - ask user to insert manually
if [ ${#java_homes[@]} -eq 0 ]; then
    echo "ERROR: No Java installations found!"
    while true; do
        read -rp "Enter the path to your Java installation (or press ENTER to install Java): " custom_java
        if [[ -z "$custom_java" ]]; then
            # Suggest to install Java
            echo "No Java installation found."
            if [ "$OS" = "MAC" ]; then
                echo "On macOS you can install Java using:"
                echo "1. brew install openjdk"
                echo "2. Download from https://adoptium.net/"
            else
                echo "On Linux you can install Java using:"
                echo "sudo apt install openjdk-11-jdk  # For Debian/Ubuntu"
                echo "sudo yum install java-11-openjdk  # For RHEL/CentOS"
            fi
            exit 1
        elif [ -d "$custom_java" ] && [ -f "$custom_java/bin/java" ] && [ -f "$custom_java/lib/security/cacerts" ]; then
            java_homes+=("$custom_java")
            break
        else
            echo "Invalid Java path: $custom_java"
            echo "Make sure the directory contains 'bin/java' and 'lib/security/cacerts'."
        fi
    done
fi

# Let user select Java installation
if [ ${#java_homes[@]} -gt 1 ]; then
    echo "Found multiple Java installations:"
    for i in "${!java_homes[@]}"; do
        java_version=$("${java_homes[$i]}/bin/java" -version 2>&1 | head -n 1 | awk -F '"' '{print $2}')
        printf "%2d) %s (Java %s)\n" "$((i+1))" "${java_homes[$i]}" "$java_version"
    done

    while true; do
        read -rp "Select Java installation (1-${#java_homes[@]}): " selection
        if [[ "$selection" =~ ^[0-9]+$ ]] && [ "$selection" -ge 1 ] && [ "$selection" -le ${#java_homes[@]} ]; then
            selected_java_home="${java_homes[$((selection-1))]}"
            break
        fi
        echo "Invalid selection, please try again."
    done
else
    selected_java_home="${java_homes[0]}"
fi

JAVA_CACERTS="$selected_java_home/lib/security/cacerts"
echo "Using Java at: $selected_java_home"
echo "Using cacerts at: $JAVA_CACERTS"

# Check sudo access
if ! sudo -v; then
    echo "ERROR: Need sudo access to update cacerts"
    exit 1
fi

# Create directories
mkdir -p .{minio,mc}/certs

# Formatting address for SAN
if [[ $MINIO_HOST =~ ^([0-9]{1,3}\.){3}[0-9]{1,3}$ ]]; then
    SAN_ENTRY="IP:$MINIO_HOST"
else
    SAN_ENTRY="DNS:$MINIO_HOST, DNS:music.$MINIO_HOST"
fi

# Create certificate
echo "Generating certificate for $MINIO_HOST..."
openssl req -x509 -nodes \
    -days 365 \
    -newkey rsa:2048 \
    -keyout .minio/certs/private.key \
    -out .minio/certs/public.crt \
    -subj "/CN=$MINIO_HOST" \
    -addext "subjectAltName = $SAN_ENTRY 2>/dev/null"

# shellcheck disable=SC2181
if [ $? -ne 0 ]; then
    echo "ERROR: Failed to generate certificate"
    exit 1
fi

cp .minio/certs/* .mc/certs

# Try default password first
STOREPASS="changeit"
if ! keytool -list -keystore "$JAVA_CACERTS" -storepass "$STOREPASS" >/dev/null 2>&1; then
    echo "Default keystore password did not work"
    while true; do
        read -rsp "Enter keystore password for $JAVA_CACERTS: " STOREPASS
        echo
        if keytool -list -keystore "$JAVA_CACERTS" -storepass "$STOREPASS" >/dev/null 2>&1; then
            break
        fi
        echo "Invalid password, please try again."
    done
fi

# Import certificate
echo "Adding certificate to $JAVA_CACERTS..."
sudo keytool -importcert -trustcacerts \
    -keystore "$JAVA_CACERTS" \
    -storepass "$STOREPASS" \
    -alias "minio_cert_${MINIO_HOST}" \
    -file .minio/certs/public.crt \
    -noprompt

# shellcheck disable=SC2181
if [ $? -ne 0 ]; then
    echo "ERROR: Failed to import certificate"
    exit 1
fi

# Run docker compose
echo "Starting Docker containers..."
docker compose up -d
