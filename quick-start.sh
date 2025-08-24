#!/bin/bash

# Banking System POC - Quick Start Script
# This script will help install prerequisites and run the application

echo "🏦 Banking System POC - Quick Start"
echo "==================================="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Function to install Homebrew
install_homebrew() {
    echo -e "${BLUE}📦 Installing Homebrew...${NC}"
    /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
    
    # Add Homebrew to PATH for Apple Silicon Macs
    if [[ $(uname -m) == "arm64" ]]; then
        echo 'eval "$(/opt/homebrew/bin/brew shellenv)"' >> ~/.zshrc
        eval "$(/opt/homebrew/bin/brew shellenv)"
    fi
    
    echo -e "${GREEN}✅ Homebrew installed successfully!${NC}"
}

# Function to install Java
install_java() {
    echo -e "${BLUE}☕ Installing Java 17...${NC}"
    brew install openjdk@17
    
    # Add Java to PATH
    if [[ $(uname -m) == "arm64" ]]; then
        echo 'export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"' >> ~/.zshrc
        export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"
    else
        echo 'export PATH="/usr/local/opt/openjdk@17/bin:$PATH"' >> ~/.zshrc
        export PATH="/usr/local/opt/openjdk@17/bin:$PATH"
    fi
    
    echo -e "${GREEN}✅ Java 17 installed successfully!${NC}"
}

# Function to install Maven
install_maven() {
    echo -e "${BLUE}📚 Installing Maven...${NC}"
    brew install maven
    echo -e "${GREEN}✅ Maven installed successfully!${NC}"
}

# Check if Homebrew is installed
if ! command_exists brew; then
    echo -e "${YELLOW}⚠️  Homebrew not found. Installing...${NC}"
    install_homebrew
else
    echo -e "${GREEN}✅ Homebrew is already installed${NC}"
fi

# Check if Java is installed
if ! command_exists java; then
    echo -e "${YELLOW}⚠️  Java not found. Installing Java 17...${NC}"
    install_java
else
    java_version=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [ "$java_version" -ge 17 ]; then
        echo -e "${GREEN}✅ Java $java_version is already installed${NC}"
    else
        echo -e "${YELLOW}⚠️  Java $java_version found, but Java 17+ is required. Installing Java 17...${NC}"
        install_java
    fi
fi

# Check if Maven is installed
if ! command_exists mvn; then
    echo -e "${YELLOW}⚠️  Maven not found. Installing...${NC}"
    install_maven
else
    echo -e "${GREEN}✅ Maven is already installed${NC}"
fi

# Verify installations
echo ""
echo -e "${BLUE}🔍 Verifying installations...${NC}"
echo ""

if command_exists java && command_exists mvn; then
    echo -e "${GREEN}✅ All prerequisites are installed!${NC}"
    echo ""
    
    echo "Java version:"
    java -version
    echo ""
    
    echo "Maven version:"
    mvn -version
    echo ""
    
    # Ask if user wants to run the application
    read -p "🚀 Do you want to build and run the application now? (y/n): " -n 1 -r
    echo ""
    
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo -e "${BLUE}🏗️  Building the project...${NC}"
        mvn clean install
        
        if [ $? -eq 0 ]; then
            echo -e "${GREEN}✅ Build successful!${NC}"
            echo ""
            echo -e "${BLUE}🚀 Starting the application...${NC}"
            echo -e "${YELLOW}📝 The application will be available at: http://localhost:8080${NC}"
            echo -e "${YELLOW}📝 Press Ctrl+C to stop the application${NC}"
            echo ""
            mvn spring-boot:run
        else
            echo -e "${RED}❌ Build failed. Please check the error messages above.${NC}"
            exit 1
        fi
    else
        echo -e "${BLUE}📚 To run the application later, use:${NC}"
        echo "  mvn clean install"
        echo "  mvn spring-boot:run"
        echo ""
        echo -e "${BLUE}🌐 The application will be available at: http://localhost:8080${NC}"
    fi
    
else
    echo -e "${RED}❌ Installation verification failed. Please check the error messages above.${NC}"
    exit 1
fi

echo ""
echo -e "${GREEN}🎉 Setup completed successfully!${NC}"
echo ""
echo -e "${BLUE}📚 Next steps:${NC}"
echo "1. Access the web interface at: http://localhost:8080"
echo "2. Use demo credentials:"
echo "   - Super Admin: admin / admin123"
echo "   - Customer: john / john123"
echo "3. Test the API using the provided test-api.sh script"
echo "4. Check the README.md for detailed information"
echo ""
echo -e "${BLUE}🔧 If you need help:${NC}"
echo "- Check SETUP.md for detailed setup instructions"
echo "- Review the main README.md for system information"
echo "- Check application logs for troubleshooting" 