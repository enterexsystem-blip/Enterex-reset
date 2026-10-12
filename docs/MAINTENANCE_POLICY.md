# Política de severidade e manutenção — Enterex Reset

**Estado:** etapa de especificação inicial; ainda não implementado em produção.

## Objetivo
Classificar problemas por evidência técnica e decidir se a manutenção é automática ou depende do administrador (ADM). A classificação não deve depender exclusivamente de uma resposta de IA.

## Níveis e ações

| Nível | Critério | Ação padrão |
|---|---|---|
| INFO | Evento esperado ou sem impacto observável | Registrar; não alterar disponibilidade |
| MODERADO | Degradação limitada, sem risco confirmado a dados ou segurança | Notificar ADM; ADM decide se ativa manutenção |
| ALTO | Falha repetida ou recurso importante degradado, com evidência consistente | Recomendar manutenção do recurso afetado; ADM confirma, salvo regra aprovada previamente |
| CRÍTICO | Condição objetiva, validada e com risco imediato à integridade, segurança ou operação essencial | Ativar automaticamente manutenção limitada ao componente afetado, se o serviço de controle estiver saudável |

## Regras de segurança
1. **Evidência primeiro:** cada regra automática precisa de um identificador, condição verificável, janela temporal e justificativa legível.
2. **Sem diagnóstico por palpite:** texto livre gerado por IA pode sugerir investigação, mas não ativa sozinho manutenção crítica.
3. **Escopo mínimo:** colocar em manutenção apenas o recurso comprometido, não o sistema inteiro, quando possível.
4. **Persistência e histerese:** exigir uma quantidade configurada de sinais em uma janela, aplicar período de resfriamento e não alternar repetidamente entre normal/manutenção.
5. **Auditoria:** registrar regra acionada, evidências resumidas, severidade, estado anterior/novo, horário e ator (sistema ou ADM). Nunca registrar senhas, tokens ou conteúdo privado desnecessário.
6. **Override controlado:** ADM autenticado pode confirmar, prolongar ou encerrar manutenção; registrar motivo e autor.
7. **Falha do controle:** se a API central estiver indisponível, não alegar que manutenção global foi ativada. Usar último estado assinado ainda válido apenas para exibição/controles locais seguros e alertar sobre estado desatualizado.
8. **Recuperação:** sair da manutenção após verificações de saúde bem-sucedidas durante uma janela configurada, ou por ação auditada do ADM.
9. **Proteção contra abuso:** autenticação e autorização no servidor, limitação de requisições e validação de todos os eventos recebidos.
10. **Privacidade:** coletar apenas dados de diagnóstico necessários e informar claramente o usuário.

## Arquitetura proposta
- **Agente Android:** coleta apenas sinais permitidos pelas APIs do Android e mostra estado/avisos. Não promete detectar tentativas de senha da tela de bloqueio nem controlar qualquer aparelho por USB.
- **API central:** recebe eventos autenticados, executa regras determinísticas e guarda estado de manutenção.
- **Monitor externo:** verifica a disponibilidade da API central por um caminho independente; se o próprio servidor cair, o app Android sozinho não consegue comandar todos os dispositivos.
- **Painel ADM:** mostra severidade, evidências, alcance, histórico e ações permitidas conforme o papel do usuário.

## Contrato inicial de evento
Campos mínimos sugeridos: `eventId`, `deviceId` pseudonimizado, `ruleId`, `observedAt`, `severity`, `evidenceSummary`, `appVersion` e `source`. A API deve validar esquema, horário, autorização e repetição de `eventId`.

## Critérios para considerar esta etapa concluída
- [ ] Regras objetivas revisadas e aprovadas.
- [ ] Testes para cada nível, falso positivo, evento repetido e indisponibilidade da API.
- [ ] Auditoria e override ADM testados.
- [ ] Testes de recuperação e oscilação de estado.
- [ ] Documentação alinhada ao comportamento real.
