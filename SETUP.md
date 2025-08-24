# Setup Guide for Banking System POC

This guide will help you set up the development environment and run the Banking System POC.

## Prerequisites Installation

### 1. Install Java 17 or Higher

#### Option A: Using Homebrew (Recommended for macOS)
```bash
# Install Homebrew if you don't have it
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Install Java 17
brew install openjdk@17

# Add Java to your PATH
echo 'export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc
```

#### Option B: Download from Oracle
1. Visit [Oracle Java Downloads](https://www.oracle.com/java/technologies/downloads/)
2. Download Java 17 for macOS
3. Install the downloaded package
4. Add to PATH if needed

#### Option C: Using SDKMAN (Alternative)
```bash
# Install SDKMAN
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

# Install Java 17
sdk install java 17.0.9-tem
sdk use java 17.0.9-tem
```

### 2. Install Maven

#### Option A: Using Homebrew
```bash
brew install maven
```

#### Option B: Using SDKMAN
```bash
sdk install maven
```

#### Option C: Manual Installation
1. Download Maven from [Apache Maven](https://maven.apache.org/download.cgi)
2. Extract to a directory (e.g., `/opt/maven`)
3. Add to PATH:
   ```bash
   echo 'export PATH="/opt/maven/bin:$PATH"' >> ~/.zshrc
   source ~/.zshrc
   ```

### 3. Verify Installation
```bash
java -version
mvn -version
```

You should see output similar to:
```
openjdk version "17.0.9" 2023-10-17
OpenJDK Runtime Environment (build 17.0.9+9)
OpenJDK 64-Bit Server VM (build 17.0.9+9, mixed mode, sharing)

Apache Maven 3.9.5 (57804ffe001d7215b5b7c966b6a9dd7f6b8df9f9)
Maven home: /opt/homebrew/Cellar/maven/3.9.5/libexec
Java version: 17.0.9, vendor: Oracle Corporation, default locale: en_US, platform encoding: UTF-8
```

## Running the Application

### 1. Navigate to Project Directory
```bash
cd "Paytabs Task"
```

### 2. Build the Project
```bash
mvn clean install
```

### 3. Run the Application
```bash
mvn spring-boot:run
```

The application will start and you should see output similar to:
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.2.0)

2024-01-01 12:00:00.000  INFO 12345 --- [           main] c.p.b.BankingSystemApplication           : Starting BankingSystemApplication...
...
2024-01-01 12:00:00.000  INFO 12345 --- [           main] c.p.b.BankingSystemApplication           : Started BankingSystemApplication in 5.123 seconds (JVM running for 6.456)
```

### 4. Access the Application
- **Web Interface**: http://localhost:8080
- **H2 Database Console**: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:bankingdb`
  - Username: `sa`
  - Password: `password`

## Alternative: Using Docker

If you prefer to use Docker instead of installing Java and Maven:

### 1. Install Docker Desktop
Download and install [Docker Desktop for Mac](https://www.docker.com/products/docker-desktop/)

### 2. Create Dockerfile
```dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/banking-system-1.0.0.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
```

### 3. Build and Run with Docker
```bash
# Build the JAR first (requires Java/Maven)
mvn clean package

# Build Docker image
docker build -t banking-system .

# Run container
docker run -p 8080:8080 banking-system
```

## Alternative: Using IntelliJ IDEA

### 1. Install IntelliJ IDEA
Download [IntelliJ IDEA Community Edition](https://www.jetbrains.com/idea/download/) (free)

### 2. Open Project
1. Open IntelliJ IDEA
2. Select "Open" and choose the project directory
3. IntelliJ will automatically detect it as a Maven project
4. Wait for dependencies to download

### 3. Run the Application
1. Find `BankingSystemApplication.java` in the project explorer
2. Right-click on the file
3. Select "Run 'BankingSystemApplication.main()'"

## Troubleshooting

### Common Issues

#### 1. "Command not found: java"
- Java is not installed or not in PATH
- Follow the Java installation steps above
- Restart your terminal after installation

#### 2. "Command not found: mvn"
- Maven is not installed or not in PATH
- Follow the Maven installation steps above
- Restart your terminal after installation

#### 3. Port 8080 already in use
```bash
# Find process using port 8080
lsof -i :8080

# Kill the process
kill -9 <PID>
```

#### 4. Build failures
```bash
# Clean and rebuild
mvn clean install -U

# Check Java version compatibility
java -version
mvn -version
```

#### 5. Permission denied errors
```bash
# Make test script executable
chmod +x test-api.sh

# Check file permissions
ls -la
```

### Getting Help

1. **Check the logs**: Application logs are displayed in the console
2. **Verify versions**: Ensure Java 17+ and Maven 3.6+ are installed
3. **Check ports**: Ensure port 8080 is available
4. **Review README**: Check the main README.md for additional information

## Next Steps

After successful setup:

1. **Test the API**: Use the provided `test-api.sh` script or Postman collection
2. **Explore the UI**: Log in with demo credentials
3. **Review the code**: Examine the implementation details
4. **Run test cases**: Verify all functionality works as expected

## Demo Credentials

- **Super Admin**: `admin` / `admin123`
- **Customer**: `john` / `john123`

## Support

If you encounter issues:
1. Check this setup guide
2. Review the main README.md
3. Check the application logs
4. Ensure all prerequisites are met 