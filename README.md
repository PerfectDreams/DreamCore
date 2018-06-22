<p align="center">
<br>
<img src="https://perfectdreams.net/assets/img/perfectdreams_logo.png">
<br>
  <a href="https://perfectdreams.net"><img src="https://img.shields.io/badge/website-perfectdreams-blue.svg"></a>
<a href="https://perfectdreams.net/discord"><img src="https://img.shields.io/badge/discord-perfectdreams-7289da.svg"></a>
<a href="https://perfectdreams.net/loja"><img src="https://img.shields.io/badge/support-perfectdreams-yellow.svg"></a>
<a href="https://circleci.com/gh/PerfectDreams/DreamCore"><img src="https://circleci.com/gh/PerfectDreams/DreamCore.svg?style=shield"></a>
<a href="https://mrpowergamerbr.com/"><img src="https://img.shields.io/badge/website-mrpowergamerbr-blue.svg"></a>
<a href="https://github.com/PerfectDreams/DreamCore/LICENSE"><img src="https://img.shields.io/badge/license-AGPL%20v3-orange.svg"></a>
</p>
<h1 align="center">DreamCore</h1>
<p align="center">🐱‍💻 O núcleo que contém todos os sonhos que nossos plugins precisam!</p>

## Como Ajudar?
Existem vários repositórios [na nossa organização](https://github.com/PerfectDreams) de várias partes do PerfectDreams, caso você queria contribuir em outras partes do PerfectDreams, siga as instruções no `README.md` de cada repositório!

### Como Doar?

Mesmo que você não saiba programar, você pode ajudar no desenvolvimento do PerfectDreams comprando vantagens em nossos servidores! https://perfectdreams.net/loja

Você também pode doar para a [Loritta](https://loritta.website/support), a mascote do PerfectDreams! 😊

### Como Usar?

Você também pode compilar o projeto e usar em outros lugares, mas lembrando:
* Eu não irei dar suporte caso você queria usar o plugin no seu servidor sem dar nada em troca para o PerfectDreams, lembre-se, a licença do projeto é [AGPL v3](https://github.com/PerfectDreams/DreamCore/LICENSE), você é **obrigado a deixar todas as suas alterações no projeto públicas**.
* Eu não irei ficar explicando como arrumar problemas no seu projeto, você está por sua conta e risco.
* Eu irei dar suporte caso você queria compilar o projeto para ajudar o PerfectDreams.
* Lembrando que nossos projetos precisam de setups e workflows específicos, você **não irá conseguir ysar** nossos projetos apenas compilando e usando.
* Existem várias coisas "hard coded" no projeto, ou seja, você terá que editar o código-fonte dela e recompilar, afinal, o projeto foi criado apenas para ser utilizado no PerfectDreams então você terá que fazer algumas modificações no código-fonte dela para funcionar. ;)
* Você não pode utilizar o nome "PerfectDreams" ou o nome do projeto na sua versão

Mas se você quiser mesmo hospedar a Loritta, siga os seguintes passos:
1. Tenha o MongoDB instalado na sua máquina.
2. Tenha o JDK 8 (ou superior) na sua máquina.
3. Tenha o Git Bash instalado na sua máquina.
4. Tenha o Maven instalado na sua máquina com o `PATH` configurado corretamente. (para que você possa usar `mvn install` em qualquer pasta e o `JAVA_HOME`, para que o `mvn install` funcione)
5. Tenha o IntelliJ IDEA instalado na sua máquina.
6. Tenha um servidor de Minecraft rodando [Paper](https://github.com/PaperMC/Paper) na última versão disponível, para transformar sonhos em realidade, nossos projetos sempre utilizam a última versão disponível no momento que o projeto foi criado.
6. Faça ```git clone https://github.com/PerfectDreams/DreamCore.git``` em alguma pasta no seu computador.
7. Agora, usando o PowerShell (ou o próprio Git Bash), entre na pasta criada e utilize `mvn install`
8. Após terminar de compilar, vá na pasta `target` e pegue a JAR do projeto.
9. Coloque na pasta de plugins junto com todas as dependências que o projeto precise.
10. Após terminar de configurar, inicie o servidor e, se tudo der certo, ela irá iniciar e você poderá usar os comandos dela! 🎉

### Pull Requests
No seu Pull Request, você deverá seguir o meu estilo de código bonitinho que eu faço, é recomendado que você coloque comentários nas partes do seu código para que seja mais fácil na hora da leitura.

Caso o seu código possua texto, você é obrigado a utilizar o sistema de localização da Loritta, para que o seu Pull Request possa ser traduzido para outras linguagens, ou seja, após criar o seu Pull Request, crie um Pull Request no repositório de linguagens da Loritta com as keys necessárias.

O seu código não pode ser algo "gambiarra", meu código pode ter algumas gambiarras mas isto não significa que você também deve encher o PerfectDreams com mais gambiarras no seu Pull Request.

Você precisa pensar "será que alguém iria utilizar isto?", se você criar um comando que só seja útil para você, provavelmente eu irei negar o seu Pull Request.

## Licença

O código-fonte deste projeto está licenciado sob a [GNU Affero General Public License v3.0](https://github.com/LorittaBot/Loritta/blob/master/LICENSE)

PerfectDreams é © MrPowerGamerBR — Todos os direitos reservados

A personagem Loritta é © MrPowerGamerBR & PerfectDreams — Todos os direitos reservados

Ao utilizar o projeto você aceita os [termos de uso da Loritta](https://loritta.website/privacy) e os [termos de uso do PerfectDreams](https://perfectdreams.net/privacy).