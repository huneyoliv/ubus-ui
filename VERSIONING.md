# Diretrizes de Versionamento do Aplicativo (SemVer)

Este documento define o padrão de versionamento para o aplicativo Ubus. Qualquer desenvolvedor ou agente de IA que realize alterações no projeto deve seguir estas instruções antes de gerar um build ou publicar uma nova versão.

---

## 1. O Padrão SemVer (Semantic Versioning)

O versionamento segue o formato `MAJOR.MINOR.PATCH` para o `versionName` no arquivo [build.gradle.kts](file:///home/huneyoliv/Projetos/ubus-ui/composeApp/build.gradle.kts):

- **MAJOR (Maior)**: Incrementado quando há alterações incompatíveis com versões anteriores (ex: mudanças estruturais grandes, reescrita de fluxos principais).
- **MINOR (Menor)**: Incrementado quando novas funcionalidades compatíveis com versões anteriores são adicionadas (ex: novas telas, novos fluxos opcionais).
- **PATCH (Correção)**: Incrementado quando são feitas apenas correções de bugs compatíveis com versões anteriores (ex: correções de layout, ajustes de textos, correções de crashes).

---

## 2. Como Atualizar as Versões do App

As configurações de versão estão localizadas no bloco `defaultConfig` do arquivo [build.gradle.kts](file:///home/huneyoliv/Projetos/ubus-ui/composeApp/build.gradle.kts#L83-L89):

```kotlin
    defaultConfig {
        applicationId = "com.ubusmobilidade.ubus"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 2 // <-- Deve ser incrementado em +1
        versionName = "1.1.0" // <-- Deve seguir o padrão MAJOR.MINOR.PATCH
    }
```

### Regras para a IA / Desenvolvedores:
1. **Identificar o Tipo de Alteração**: Antes de finalizar uma tarefa, classifique-a como PATCH, MINOR ou MAJOR.
2. **Atualizar o `versionCode`**: Sempre incremente o `versionCode` em exatamente `1` em relação ao valor atual (ex: de `2` para `3`). Este número deve ser sempre um inteiro sequencial e nunca deve ser diminuído ou mantido igual em builds de produção.
3. **Atualizar o `versionName`**: Modifique a string de acordo com a regra SemVer baseando-se na versão anterior (ex: de `1.1.0` para `1.1.1` em caso de PATCH).
4. **Registrar no Git**:
   - Faça o commit das alterações incluindo o arquivo `build.gradle.kts`.
   - Crie uma tag do git no formato `v<versionName>` (ex: `v1.1.1`).
   - Faça o push dos commits e da tag para o repositório remoto para disparar o pipeline de CI/CD.

---

## 3. Histórico de Versões

### v1.4.0 (versionCode: 8) - 2026-07-05
- **Grade Horária por IA e Prioridade**: Substituição da rota preferencial manual por grade horária com IA no cadastro e na renovação semestral.
- **Painel Administrativo de Alunos (Web)**: Nova seção Alunos no `ubus-web` com busca, visualização de documentos (grade/comprovante de residência/laudo), histórico paginado de viagens e editor de grade.
- **Turnos de Viagem Automáticos (Web)**: Geração de viagens na escala derivando o turno automaticamente (Manhã, Tarde, Noite) dos horários de ida/volta da rota.
- **Documentação de Transição**: Criação do guia `backend_instructions.md` com prompt injection para sincronizar a API com o novo comportamento.

### v1.3.0 (versionCode: 7) - 2026-07-04
- **Remoção da Rota Preferencial no Cadastro**: Redução para 5 etapas de cadastro, passando `preferredRouteId = null`.
- **Limpeza na Renovação Semestral**: Remoção da rota preferencial da tela `RenovarSemestreScreen` e desativação do botão se o status for `PENDING` ou `APPROVED` (evitando erro 403).
- **Gerenciamento de Dropoff Points (Pontos de Desembarque)**: Fluxo completo de CRUD para o Manager gerenciar os pontos de desembarque associados a rotas.
- **Integração de APIs e Navegação**: Criação de `CreateDropoffPointPayload` e `UpdateDropoffPointPayload`, mapeamento no `FleetRepository`, novos destinos no Decompose (`ManagerDropoffPointFormScreen`).

### v1.2.0 (versionCode: 6)
- Versão de produção anterior.
