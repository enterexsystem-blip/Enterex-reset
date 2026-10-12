# Enterex Reset — recuperação do código-fonte

## O que a automação faz

- Compila o APK de depuração em cada push para `main`, quando executada manualmente e semanalmente.
- Cria um arquivo de origem rastreado pelo Git e gera SHA-256 para detectar corrupção acidental do arquivo.
- Guarda o arquivo de origem e o APK como artefatos do GitHub Actions.
- Registra o commit associado ao backup para identificar exatamente a versão.

## Como recuperar

1. Abra **Actions** neste repositório.
2. Abra uma execução bem-sucedida de **Source Backup and Integrity**.
3. Baixe o artefato `Enterex-source-backup-...`.
4. Extraia o arquivo `enterex-source-<commit>.tar.gz`.
5. Confira a integridade no Linux/macOS com `sha256sum -c SHA256SUMS.txt` (execute no diretório onde está o arquivo; se necessário, ajuste o caminho no arquivo de checksum).
6. Confira o commit no arquivo `RECOVERY-INFO.txt` e valide a compilação antes de usar a versão recuperada.

## Limites e próximos passos

- Artefatos têm retenção de 90 dias para o código-fonte e 30 dias para o APK. Eles não substituem um backup independente sob controle separado.
- SHA-256 detecta alterações no arquivo quando o checksum confiável também está protegido; não impede um invasor com acesso suficiente de substituir ambos.
- Para uma recuperação realmente independente, mantenha periodicamente uma cópia local/offline ou em outra conta/serviço seguro.
- Ative autenticação de dois fatores na conta GitHub e proteja os códigos de recuperação fora do celular.
- Configure proteção de branch e revisão obrigatória nas configurações do repositório, se disponível.
- Nunca coloque tokens, senhas, chaves privadas ou segredos de assinatura dentro do repositório.
- Esta automação não adiciona criptografia ao código-fonte: o repositório é público. O objetivo aqui é histórico, verificação de integridade e recuperação.
