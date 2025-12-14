# 🚀 Quick Start Guide

> Get Document AI Hub running in 5 minutes!

## Prerequisites Check

```bash
# Check Java version (need 17+)
java -version

# Check Maven version (need 3.6+)
mvn -version

# Check MySQL version (need 8.0+)
mysql --version
```

## Setup in 5 Steps

### 1️⃣ Database Setup (2 minutes)

```sql
# Login to MySQL
mysql -u root -p

# Create database and user
CREATE DATABASE doc_db;
CREATE USER 'doc_user'@'localhost' IDENTIFIED BY 'doc_password';
GRANT ALL PRIVILEGES ON doc_db.* TO 'doc_user'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

### 2️⃣ Get API Keys (3 minutes)

**Groq API Key** (for AI features):
1. Visit: https://console.groq.com
2. Sign up / Login
3. Go to API Keys section
4. Create new API key
5. Copy the key

**Jina AI API Key** (for semantic search):
1. Visit: https://jina.ai
2. Sign up / Login
3. Get API key from dashboard
4. Copy the key

### 3️⃣ Configure Environment (1 minute)

Create a `.env` file in the project root:

```bash
# Database
DB_URL=jdbc:mysql://localhost:3306/doc_db
DB_USERNAME=doc_user
DB_PASSWORD=doc_password

# AI API Keys
GROQ_API_KEY=your_groq_key_here
JINA_API_KEY=your_jina_key_here

# Security
JWT_SECRET=your-super-secret-key-change-this-to-something-random

# Upload Directory
UPLOAD_DIR=uploads
```

Or set environment variables directly:

```bash
export DB_URL=jdbc:mysql://localhost:3306/doc_db
export DB_USERNAME=doc_user
export DB_PASSWORD=doc_password
export GROQ_API_KEY=your_groq_key_here
export JINA_API_KEY=your_jina_key_here
export JWT_SECRET=your-super-secret-key-change-this-to-something-random
```

### 4️⃣ Build & Run (2 minutes)

```bash
# Clone the repository
git clone https://github.com/yourusername/Document-AI-Hub.git
cd Document-AI-Hub

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

### 5️⃣ Test It! (1 minute)

```bash
# Backend should be running on:
http://localhost:8080

# Test signup endpoint
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test123!"
  }'

# Response should include a JWT token
```

---

## 🎯 Quick API Test Flow

### 1. Register a User

```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"email": "demo@example.com", "password": "Demo123!"}'
```

Save the returned `token` from the response.

### 2. Upload a PDF

```bash
# Replace YOUR_JWT_TOKEN with the token from step 1
curl -X POST http://localhost:8080/api/documents \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@/path/to/your/document.pdf"
```

Save the returned `id` from the response.

### 3. Extract Text

```bash
# Replace DOCUMENT_ID with the id from step 2
curl -X POST http://localhost:8080/api/documents/DOCUMENT_ID/extract-text \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 4. Generate Summary

```bash
curl -X POST http://localhost:8080/api/documents/DOCUMENT_ID/summarize \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 5. Ask a Question

```bash
curl -X POST http://localhost:8080/api/documents/DOCUMENT_ID/qa \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"question": "What is this document about?"}'
```

---

## 📱 Using Postman

### Import Collection

1. Create a new Collection in Postman
2. Add environment variables:
   - `baseUrl`: `http://localhost:8080`
   - `token`: (will be set after signup/login)

### Pre-configured Requests

**1. Signup**
```
POST {{baseUrl}}/api/auth/signup
Body (JSON):
{
  "email": "test@example.com",
  "password": "Test123!"
}

Tests Script:
pm.environment.set("token", pm.response.json().token);
```

**2. Upload Document**
```
POST {{baseUrl}}/api/documents
Authorization: Bearer {{token}}
Body (form-data):
file: [Select your PDF file]

Tests Script:
pm.environment.set("documentId", pm.response.json().id);
```

**3. Extract Text**
```
POST {{baseUrl}}/api/documents/{{documentId}}/extract-text
Authorization: Bearer {{token}}
```

**4. Summarize**
```
POST {{baseUrl}}/api/documents/{{documentId}}/summarize
Authorization: Bearer {{token}}
```

**5. Q&A**
```
POST {{baseUrl}}/api/documents/{{documentId}}/qa
Authorization: Bearer {{token}}
Body (JSON):
{
  "question": "What are the main points?"
}
```

---

## 🐛 Troubleshooting

### Database Connection Error

```bash
# Check MySQL is running
sudo systemctl status mysql  # Linux
brew services list | grep mysql  # macOS

# Test connection
mysql -h localhost -u doc_user -p doc_db
```

### Port 8080 Already in Use

```bash
# Find process using port 8080
lsof -i :8080  # macOS/Linux
netstat -ano | findstr :8080  # Windows

# Kill the process or change Spring Boot port
export SERVER_PORT=8081
```

### API Key Issues

```bash
# Verify environment variables are set
echo $GROQ_API_KEY
echo $JINA_API_KEY

# If empty, reload your environment
source ~/.bashrc  # or ~/.zshrc
```

### Build Failures

```bash
# Clean and rebuild
mvn clean install -U

# Skip tests if needed (not recommended)
mvn clean install -DskipTests
```

---

## 🎨 Frontend Setup (Optional)

If you want to run the React frontend:

```bash
# Navigate to frontend directory
cd frontend  # or wherever your React app is

# Install dependencies
npm install

# Start development server
npm run dev
```

Frontend will run on `http://localhost:5173`

---

## 📊 Verify Everything Works

### Check Application Health

```bash
# If Spring Actuator is enabled
curl http://localhost:8080/actuator/health
```

### Database Check

```sql
mysql -u doc_user -p doc_db

-- Check tables were created
SHOW TABLES;

-- Should see: users, documents, document_chunk, document_summary

-- Check user count
SELECT COUNT(*) FROM users;
```

### API Endpoints

```bash
# List all available endpoints (if Spring Actuator is enabled)
curl http://localhost:8080/actuator/mappings
```

---

## 🎯 Next Steps

1. ✅ Application is running
2. ✅ Test all API endpoints
3. ✅ Upload your first document
4. ✅ Try the AI features (summarize, Q&A)
5. ✅ Explore the codebase
6. ⭐ Star the repository if you found it useful!

---

## 💡 Pro Tips

- Use **Postman** or **Insomnia** for API testing
- Check **logs** if something doesn't work: `tail -f logs/spring.log`
- **JWT tokens** expire after 24 hours by default
- The app uses **H2 console** in dev mode (if configured)
- Vector embeddings take time - be patient with first-time processing

---

## 📞 Need Help?

- 📖 Check the full [README.md](README.md)
- 🐛 Found a bug? [Open an issue](https://github.com/yourusername/Document-AI-Hub/issues)
- 💬 Questions? [Start a discussion](https://github.com/yourusername/Document-AI-Hub/discussions)

---

**Happy Coding! 🚀**
