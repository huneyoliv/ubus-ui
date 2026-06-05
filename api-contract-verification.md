# Contrato de API - Sistema de Verificação por Código

O frontend agora utiliza um sistema unificado para solicitação e verificação de códigos via E-mail ou WhatsApp.

## 1. Enviar Código de Verificação

Solicita o envio de um código de verificação de 6 dígitos.

- **Rota:** `POST /v1/auth/send-verification-code`
- **Corpo da Requisição (JSON):**
  ```json
  {
    "identifier": "string (e-mail ou número de telefone)",
    "channel": "EMAIL | WHATSAPP",
    "context": "CHANGE_EMAIL | RESET_PASSWORD | REGISTER"
  }
  ```
- **Resposta Esperada:**
  ```json
  {
    "message": "Código enviado com sucesso!"
  }
  ```

---

## 2. Verificar Código

Valida se o código inserido pelo usuário é correto para o contexto correspondente.

- **Rota:** `POST /v1/auth/verify`
- **Corpo da Requisição (JSON):**
  ```json
  {
    "identifier": "string (e-mail ou número de telefone)",
    "code": "string (6 dígitos)",
    "channel": "EMAIL | WHATSAPP",
    "context": "CHANGE_EMAIL | RESET_PASSWORD | REGISTER"
  }
  ```
- **Resposta Esperada:**
  ```json
  {
    "verified": true,
    "token": "string (opcional - token de autorização de reset de senha)",
    "message": "string (opcional - mensagem de erro caso verificação falhe)"
  }
  ```
