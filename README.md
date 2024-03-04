<div align="center">
<img src="https://github.com/KaatoDev/NotzScoreboard/assets/107152563/e42230a1-3d52-4717-b592-23ab428467b7" alt="" height="320" >


#
NotzScoreboard é um plugin de Scoreboard completo e totalmente personalizável com suporte a placeholders (PlaceholderAPI) e scoreboard diferentes para staffs e players, display dinâmico de staffs em cada scoreboard e database via MySQL ou SQLite.

</div>

## Informações
Na scoreboard de cada cargo será listado apenas os players dos seguintes cargos:
 - `Players` - Ajudantes e Trials.
 - `Ajudantes` - Ajudantes e Trials.
 - `Trials` - Ajudantes, Trials e Moderadores.
 - `Moderadores` - Admins e Gerentes.
 - `Admins` - Gerentes e Diretores.
 - `Gerentes` e `Diretores` - Todos.

###### Quando utilizado o placeholder {staff} ou {staff_list}.

## Dependências
- PlaceholderAPI

## Spoilers

##### [!] Todas as scoreboards foram previamente setadas!
- ### Player Scoreboard
![player](https://github.com/KaatoDev/NotzScoreboard/assets/107152563/d03d92ae-b67a-4e67-a01d-30d6fc5d26c1)

- ### Scoreboard Ajudante, Trial e Moderador
![ajudante, trial, moderador](https://github.com/KaatoDev/NotzScoreboard/assets/107152563/3fb96600-77d5-4088-956d-cd542c1ca71a)

- ### Scoreboard Admin
![admin](https://github.com/KaatoDev/NotzScoreboard/assets/107152563/287a59f9-08d1-44e0-b1eb-13d9b48f763c)

- ### Scoreboard Gerente
![gerente](https://github.com/KaatoDev/NotzScoreboard/assets/107152563/2978311c-49fb-4a6b-9b44-87a30f874ad8)

- ### Scoreboard Diretor
![diretor](https://github.com/KaatoDev/NotzScoreboard/assets/107152563/888281ec-2201-4122-96b5-d830f5fc1940)

## Placeholders
 - `{staff}` - Varia os nicks dos staffs superiores online que são visíveis ao cargo ou, quando não há, indica apenas "superiores offline" (para staffs).
 - `{staff_list}` - Lista a quantidade de staffs superiores online que são visíveis ao cargo (varia de acordo com o cargo).
 - `{staff_ajd}` - Varia os nicks dos ajudantes online ou, quando não há, indica apenas "offline".
 - `{staff_trial}` - Varia os nicks dos trials online ou, quando não há, indica apenas "offline".
 - `{staff_mod}` - Varia os nicks dos moderadores online ou, quando não há, indica apenas "offline".
 - `{staff_admin}` - Varia os nicks dos admins online ou, quando não há, indica apenas "offline".
 - `{ajd_list}` - Lista a quantidade de ajudantes online.
 - `{trial_list}` - Lista a quantidade de trials online.
 - `{mod_list}` - Lista a quantidade de moderadores online.
 - `{admin_list}` - Lista a quantidade de admins online.
 - `{gerente_list}` - Lista a quantidade de gerentes online.

## Permissões

- `notzsb.admin` - Habilita o player a utilizar o comando /notzsb.

## Commandos
### `/notzsb`
 - `add` \<cargo> \<player> - Adiciona e atribui um player à um cargo.
 - `debug` - Acompanha em tempo real a atualização da scoreboard de cada staff setado.
 - `help` - Lista os comandos do plugin.
 - `list` - Lista todos os players salvos.
 - `reload` - Recarrega as configurações (.yml) do plugin.
 - `remove` \<player> - Remove um player da lista.
 - `reset` - Reseta a database salva inteira.
 - `setRole` \<cargo> (player) - Altera o cargo do player.
 - `setScore` \<cargo> (player) - Altera a scoreboard do player.

 ###### | <> argumento obrigatório. | () argumento opcional. |
 
## Scoreboard.yml
```yml
staff:
  lines:
    onl: ## quando houver staff (ajudante ou trial) online
      - ''
      - '&e⎧ Caso precise de ajuda'
      - '&e⎜ chame um &lstaff&e:'
      - '&e⎩ -&r {staff}'
    offl: ## quando não houver staff (ajudante ou trial) online
      - ''
      - '&e⎧ &eTem dúvidas?'
      - '&e⎩ Utilize /&fticket'
  enable: true
  scoreboard: ## header de staff admin+
    - ''
    - '&6⧽ &r{player_name}'
    - '&6Players online&f: {player_list}'
    - ''
    - '&2⎧ &aTps&f: {tps}'
    - '&2⎩ &aPing&f: {ping}'
scoreboard: ## scoreboard de player
  - ''
  - '&6⧽ &r{player_displayname}'
  - ''
  - '&9⎧ &bRank&f: {rank}'
  - '&9⎩ &3{status_rankup}'
  - ''
  - '&2⎧ &aMoney&f: {money}'
  - '&2⎩ &aCash&f: {cash}'
  - ''
  - '&5› &dClan&f: {clan}'
staff_sb:
  ajudante: ## footer de staff
    - ''
    - '&e⎧ &6Staffs online&f: {staff_list}'
    - '&e⎩ - &f{staff}'
  trial: ## footer de staff
    - ''
    - '&d⎧ &6Staffs online&f: {staff_list}'
    - '&d⎩ - &f{staff}'
  moderador: ## footer de staff
    - ''
    - '&2⎧ &6Superiores online&f: {staff_list}'
    - '&2⎩ - &f{staff}'
  admin: ## scoreboard de staff
    - ''
    - '&c⎧ &eAjudantes online&f: {ajd_list}'
    - '&c⎜ &e- &f{staff_ajd}'
    - '&c⎜ &dTrials online&f: {trial_list}'
    - '&c⎜ &d- &f{staff_trial}'
    - '&c⎜ &2Mods online&f: {mod_list}'
    - '&c⎜ &2- &f{staff_mod}'
    - '&c⎜ &6Superiores online&f: {staff_list}'
    - '&c⎩ &4- &f{staff}'
  gerente: ## scoreboard de staff
    - ''
    - '&4⎧ &cAdmins online&f: {adm_list}'
    - '&4⎜ &c- &f{staff_admin}'
    - '&4⎩ &6Staffs online&f: {staff_list}'
  diretor: ## scoreboard de staff
    - ''
    - '&9⎧ &eAjudantes&f: {ajd_list}'
    - '&9⎜ &dTrials&f: {trial_list}'
    - '&9⎜ &2Moderadores&f: {mod_list}'
    - '&9⎜ &cAdmins&f: {adm_list}'
    - '&9⎜ &4Gerentes&f: {gerente_list}'
    - '&9⎩ &6Staffs online&f: {staff_list}'
```

#
###### Versões testadas: 1.8
