# Ubus - Deploy Automation Guide

## Ambiente Web (wasmJs) - GitHub Actions

O deploy da aplicação web (KMP Compose wasmJs) é automatizado via GitHub Actions.

### Arquivos de Deploy

- **`.github/workflows/deploy-web.yml`** - Pipeline CI/CD para build e deploy
- **`Dockerfile.web`** - Multi-stage build para compilar KMP wasmJs e servir com Nginx
- **`nginx.conf`** - Configurações de web server (SPA routing, compressão, cache, WASM MIME types)

### Requisitos de Setup

#### Secrets do GitHub (Settings > Secrets and variables > Actions)

| Secret | Descrição | Exemplo |
|--------|-----------|---------|
| `SSH_HOST` | IP/hostname da VM | `192.168.1.100` |
| `SSH_USERNAME` | Usuário SSH | `ubuntu` |
| `SSH_KEY` | Private key SSH (PEM) | Conteúdo da chave privada |
| `SSH_PORT` | Porta SSH (opcional, padrão 22) | `22` |

#### Configurar chave SSH

```bash
# Gerar chave (se não tiver)
ssh-keygen -t rsa -b 4096 -f ~/.ssh/github_deploy

# Copiar public key para VM
ssh-copy-id -i ~/.ssh/github_deploy.pub user@host

# No GitHub: Settings > Secrets > SSH_KEY = (conteúdo de github_deploy)
```

### Como Funciona

1. **Build (Ubuntu Latest)**
   - Checkout do código
   - Build da imagem Docker multi-arch (amd64, arm64)
   - Push para GitHub Container Registry (ghcr.io)

2. **Deploy (via SSH)**
   - Login no container registry
   - Pull da última imagem
   - Para container antigo (se existir)
   - Inicia novo container na porta 80
   - Verifica se container iniciou com sucesso
   - Limpa imagens antigas

### Estrutura do Dockerfile

```dockerfile
Stage 1 - Builder:
  - Gradle JDK 21
  - Compila `:composeApp:wasmJsReleaseDistribution`
  - Output: /app/composeApp/build/dist/wasmJs/

Stage 2 - Runtime:
  - Nginx Alpine (mínimo)
  - Copia dist/wasmJs/ para /usr/share/nginx/html
  - Expõe porta 80
```

### Nginx Configuration

- **SPA Routing**: Todas as rotas desconhecidas caem para `index.html`
- **WASM MIME Type**: `.wasm` → `application/wasm` (crítico!)
- **Caching Inteligente**:
  - `.wasm`, `.js`, `.css`: 1 ano (immutable)
  - HTML: 1 hora
- **Compressão**: gzip ativado para texto e WASM
- **Security Headers**: X-Frame-Options, Content-Type-Options, etc.

### Disparar Deploy

Simplesmente faça `git push` para `main`:

```bash
git add .
git commit -m "feat: new feature"
git push origin main
```

O GitHub Actions automaticamente:
1. Detecta o push
2. Builda a imagem Docker
3. Faz push para ghcr.io
4. Conecta na VM via SSH
5. Atualiza o container

### Monitorar Deploy

1. **GitHub Actions Dashboard**: https://github.com/seu-repo/actions
2. **VM - Logs do container**:
   ```bash
   ssh user@host
   sudo docker logs ubus-web -f
   ```
3. **Health Check**:
   ```bash
   curl http://seu-host/
   ```

### Troubleshooting

**Container não inicia:**
```bash
sudo docker logs ubus-web
```

**Erro de login no registry:**
- Verificar `SSH_KEY` no GitHub
- Testar SSH manualmente: `ssh -i key user@host`

**WASM não carrega (erro 404):**
- Verificar nginx.conf tem `application/wasm` MIME type
- Verificar pasta `/usr/share/nginx/html/` tem arquivos `.wasm`

**Build falha:**
- Verificar logs do GitHub Actions
- Rodar build localmente: `./gradlew :composeApp:wasmJsReleaseDistribution`

### Rollback

Se precisar voltar para versão anterior:

```bash
ssh user@host
sudo docker rm -f ubus-web
sudo docker run -d \
  --name ubus-web \
  -p 80:80 \
  --restart unless-stopped \
  ghcr.io/seu-user/seu-repo-web:anterior-tag
```

### Local Testing (Docker)

```bash
# Build localmente
docker build -t ubus-web:local -f Dockerfile.web .

# Run
docker run -d -p 8080:80 ubus-web:local

# Test
curl http://localhost:8080
```

### Ambiente Frontend Antigo

- **Localização**: `ubus-frontend.old/`
- **Build**: `npm run build` → output em `dist/`
- **Workflow antigo**: `ubus-frontend.old/.github/workflows/deploy-frontend.yml`
- Se precisar voltar: copiar Vite setup de `.old` e adaptar
