#!/bin/bash

# Script to generate Android keystore for app signing
# This will create a keystore file and provide the secrets needed for GitHub Actions

echo "🔐 Android Keystore Generator"
echo "============================="
echo ""

# Set keystore details
KEYSTORE_NAME="mymusicapp-release-key.jks"
KEY_ALIAS="mymusicapp"
VALIDITY_DAYS="10000" # ~27 years

echo "This script will generate a keystore file with the following details:"
echo "📁 Keystore file: $KEYSTORE_NAME"
echo "🔑 Key alias: $KEY_ALIAS"
echo "📅 Validity: $VALIDITY_DAYS days (~27 years)"
echo ""

# Prompt for keystore password
read -s -p "Enter keystore password (store this securely): " KEYSTORE_PASSWORD
echo ""
read -s -p "Confirm keystore password: " KEYSTORE_PASSWORD_CONFIRM
echo ""

if [ "$KEYSTORE_PASSWORD" != "$KEYSTORE_PASSWORD_CONFIRM" ]; then
    echo "❌ Passwords don't match. Exiting."
    exit 1
fi

# Prompt for key password
read -s -p "Enter key password (can be same as keystore password): " KEY_PASSWORD
echo ""
read -s -p "Confirm key password: " KEY_PASSWORD_CONFIRM
echo ""

if [ "$KEY_PASSWORD" != "$KEY_PASSWORD_CONFIRM" ]; then
    echo "❌ Key passwords don't match. Exiting."
    exit 1
fi

echo ""
echo "📝 Please provide the following information for the certificate:"

read -p "First and Last Name (CN): " CN
read -p "Organizational Unit (OU) [e.g., Development]: " OU
read -p "Organization (O) [e.g., Your Company]: " O
read -p "City or Locality (L): " L
read -p "State or Province (ST): " ST
read -p "Country Code (C) [e.g., US, BR]: " C

# Create distinguished name
DNAME="CN=$CN, OU=$OU, O=$O, L=$L, ST=$ST, C=$C"

echo ""
echo "🔨 Generating keystore..."

# Generate the keystore
keytool -genkeypair \
    -v \
    -keystore "$KEYSTORE_NAME" \
    -keyalg RSA \
    -keysize 2048 \
    -validity $VALIDITY_DAYS \
    -alias "$KEY_ALIAS" \
    -dname "$DNAME" \
    -storepass "$KEYSTORE_PASSWORD" \
    -keypass "$KEY_PASSWORD"

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Keystore generated successfully!"
    echo ""
    echo "📋 GitHub Secrets Configuration"
    echo "==============================="
    echo "Add these secrets to your GitHub repository (Settings > Secrets and variables > Actions):"
    echo ""
    
    # Convert keystore to base64
    KEYSTORE_BASE64=$(base64 -i "$KEYSTORE_NAME")
    
    echo "Secret Name: KEYSTORE_FILE"
    echo "Secret Value:"
    echo "$KEYSTORE_BASE64"
    echo ""
    
    echo "Secret Name: KEYSTORE_PASSWORD"
    echo "Secret Value: $KEYSTORE_PASSWORD"
    echo ""
    
    echo "Secret Name: KEY_ALIAS"
    echo "Secret Value: $KEY_ALIAS"
    echo ""
    
    echo "Secret Name: KEY_PASSWORD"
    echo "Secret Value: $KEY_PASSWORD"
    echo ""
    
    echo "🔒 Security Notes:"
    echo "=================="
    echo "1. Keep the keystore file ($KEYSTORE_NAME) and passwords secure"
    echo "2. Store backup copies in a secure location"
    echo "3. Never commit the keystore file to version control"
    echo "4. The base64 keystore content above is safe to store in GitHub Secrets"
    echo ""
    
    echo "📁 Files created:"
    echo "- $KEYSTORE_NAME (keep this secure!)"
    
    # Add keystore to .gitignore if not already there
    if ! grep -q "*.jks" .gitignore 2>/dev/null; then
        echo "*.jks" >> .gitignore
        echo "- Added *.jks to .gitignore"
    fi
    
else
    echo "❌ Failed to generate keystore"
    exit 1
fi
