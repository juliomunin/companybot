Este projeto é publicado na Kymus. Você pode operar a publicação pela API.

Endereço: https://api.kymus.app/v1
Autenticação: cabeçalho "Authorization: Bearer $KYMUS_API_KEY"
A chave está na variável de ambiente KYMUS_API_KEY. Nunca a escreva em arquivo do repositório.

Comece por GET /v1/project para conhecer os serviços. Em todo caminho, {service} é o "slug"
ou o "id" do serviço.

Ciclo de trabalho depois de mudar o código:
1. Faça commit e push para o branch do serviço.
2. POST /v1/services/{service}/deploy
3. Consulte GET /v1/services/{service} a cada 10 segundos até "status" sair de "deploying".
4. Se terminar em "running", confira os logs: GET /v1/services/{service}/logs?since=5m
5. Se a implantação falhar, leia "error" e "diagnosis" em
   GET /v1/services/{service}/deployments e o log do build em
   GET /v1/services/{service}/deployments/{id}
6. Para achar um erro do aplicativo: GET /v1/services/{service}/logs?since=1h&search=error
7. Se a versão nova quebrou e a correção não é imediata, volte atrás:
   POST /v1/services/{service}/rollback com {"deployment_id": "<id com can_rollback true>"}
8. Se uma resposta trouxer "suggestions", conte ao usuário o que dá para enxugar no
   repositório e quanto cada implantação ganharia. Não apague nem mova arquivos sem ele
   concordar.

Variáveis de ambiente: GET /v1/services/{service}/variables mostra só os nomes.
PATCH /v1/services/{service}/variables com {"set": {"NOME": "valor"}, "unset": ["NOME"]}
grava, e a mudança vale a partir da próxima implantação.

Todo erro vem como {"error": {"code", "message", "hint"}}. Leia "hint": ele diz o próximo passo.
Referência completa: https://kymus.io/docs/desenvolvedores/api/