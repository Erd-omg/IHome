# IHome系统端口配置说明

## 🚀 服务端口配置

### 后端服务
- **端口**: 8080
- **地址**: http://localhost:8080
- **API地址**: http://localhost:8080/api
- **Swagger文档**: http://localhost:8080/api/swagger-ui.html
- **健康检查**: http://localhost:8080/api/health

### 前端服务
- **端口**: 5173 (Vite默认端口)
- **地址**: http://localhost:5173
- **开发服务器**: Vite
- **API代理**: /api -> http://localhost:8080

## 📋 配置文件位置

### 后端端口配置
- **文件**: `backend/src/main/resources/application.yml`
- **配置项**: `server.port: 8080`

### 前端端口配置
- **文件**: `frontend/vite.config.ts`
- **配置项**: `server.port: 5173`

## 🔧 启动脚本 / 命令（本机）

### Windows
- **完整启动**: `start-system.bat`
- **后端启动**: `cd backend && mvn clean package -DskipTests && java -jar target\ihome-0.0.1-SNAPSHOT.jar`
- **前端启动**: `cd frontend && npm install && npm run dev`

### Linux/Mac
- **后端启动**:
```bash
cd backend
mvn clean package -DskipTests
java -jar target/ihome-0.0.1-SNAPSHOT.jar
```
- **前端启动**:
```bash
cd frontend
npm install
npm run dev
```

## 🌐 访问地址

### 开发环境（本机）
- **前端界面**: http://localhost:5173
- **后端API**: http://localhost:8080/api
- **API文档**: http://localhost:8080/api/swagger-ui.html

## 🔍 端口检查命令

### Windows
```cmd
# 检查后端端口
netstat -an | findstr :8080

# 检查前端端口
netstat -an | findstr :5173

# 测试服务
curl http://localhost:8080/api/health
curl http://localhost:5173
```

### Linux/Mac
```bash
# 检查端口
lsof -i :8080
lsof -i :5173

# 测试服务
curl http://localhost:8080/api/health
curl http://localhost:5173
```

## 📝 注意事项

1. **端口一致性**: 确保所有配置文件和文档使用相同的端口号（后端8080、前端5173）
2. **防火墙**: 确保端口没有被防火墙阻止
3. **代理配置**: 前端通过Vite代理访问后端API
4. **环境变量**: 可以通过环境变量覆盖默认端口配置

---
*最后更新: 2025-09-25*
