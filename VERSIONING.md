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
