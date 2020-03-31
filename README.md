# Nefroapp

Bem vindo meu amigo (Renan, Pedro, são vcs aí?). Esse é o maravilhoso projeto NefroApp. A ideia inicial é facilitar a vida do Nefrologista Rodrigo em prescrever medicamentos para seus pacientes.

Esse incrível projeto usa a linguagem de programação [Clojurescript](https://clojurescript.org), se quem quer que esteja lendo isso precisa manter esse projeto e não conhece Clojure, você vai ter que aprender, mas não se preocupe, é uma ótima linguagem.

Aqui estão alguns recursos para aprender Clojure e seu ambiente:
- [Clojure for the Brave and True](https://www.braveclojure.com/clojure-for-the-brave-and-true/) - Um livro focado para iniciantes, de graça e online.
- [Documentação de funções](http://clojuredocs.org/) - Boa documentação e examplos de uso das funções core.
- [4Clojure](http://4clojure.com/) - Bons exercícios básicos para praticar.
- [O Valor de Valores](http://www.infoq.com/presentations/Value-Values) - Ótima palestra do criador do Clojure, Rich Hickey, explicando algumas filosofias por trás da linguagem.
- [Um guia de Clojure para iniciantes](http://www.clojurenewbieguide.com/) - Uma página que reune muito mais páginas úteis.

Esse projeto também usa [Re-Frame](https://github.com/Day8/re-frame#derived-values-flowing) como um framework do front-end e [Devcards](https://github.com/bhauman/devcards#devcards) para ajudar a checar componentes visualmente em tempo de desenvolvimento. Mas não vai muito fundo na stack de uma vez, reserve um tempo pra aprender Clojure primeiro.

## Essential Command Lines

`npm install` - Need when running the code for the first time. It install node packages dependencies.

`npx webpack --mode=development` - Need when running the code for the first time. Bundle the external javascript code into the index.bundle.js.

`lein fig:cards` - It runs the project in the dev mode, with auto reload and re-frame-10x debug features. You can check the page http://penguin.linux.test:9500/cards.html

`lein fig:dev` - It's like lein fig:cards, but it also make the production view available in http://penguin.linux.test:9500

## To production

After compiling all the js files, being ready to deploy. You should use the tool [Workbox](https://developers.google.com/web/tools/workbox) to generate a Service Worker, so the files are cached for the app to work offline. To generate the Service Worker follow this [guide](https://developers.google.com/web/tools/workbox/guides/generate-service-worker/cli), but is basically

```
npm install workbox-cli --global
workbox wizard
workbox generateSW workbox-config.js
```
