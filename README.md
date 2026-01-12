📱 PROJETO_SOCIAL_REDE
🧩 Descrição Geral

Este projeto consiste no desenvolvimento de um protótipo funcional de uma aplicação móvel de rede social, criado no âmbito da unidade curricular de PAC.
Inicialmente focado apenas na interface do utilizador (UI), o projeto evoluiu progressivamente para uma aplicação completa, integrando backend próprio, API REST, base de dados relacional e funcionalidades dinâmicas, aproximando-se do funcionamento real de uma rede social moderna.

A aplicação permite a interação entre utilizadores, criação e visualização de conteúdos, sistema de seguidores, mensagens em tempo real e notificações, simulando um ambiente social realista para fins académicos e de aprendizagem.

🎯 Objetivos do Projeto

Desenvolver uma interface intuitiva e moderna para uma aplicação móvel;

Implementar as funcionalidades essenciais de uma rede social, tais como:

Registo e autenticação de utilizadores;

Perfis de utilizador editáveis;

Publicações com imagem e texto;

Feed dinâmico;

Gostos (likes) e comentários;

Sistema de seguidores (follow/unfollow);

Sistema de mensagens privadas;

Notificações de interações;

Integrar uma API REST própria com base de dados;

Simular comunicação cliente-servidor real entre Android e backend;

Consolidar conhecimentos de desenvolvimento mobile, APIs e bases de dados.

🧪 Tecnologias Utilizadas
📱 Frontend (Aplicação Mobile)

Android Studio – Ambiente de desenvolvimento;

Kotlin – Linguagem principal;

Jetpack Compose – Criação da interface do utilizador;

Material 3 – Componentes visuais modernos;

Navigation Component – Navegação entre ecrãs;

ViewModel + StateFlow / LiveData – Gestão de estado;

Room Database – Persistência local (offline);

SharedPreferences – Sessão do utilizador;

Coil – Carregamento de imagens;

Firebase (Firestore) – Sincronização em tempo real do chat (mensagens, typing, seen).

🌐 Backend / API

FastAPI (Python) – Desenvolvimento da API REST;

SQLAlchemy – ORM para acesso à base de dados;

MySQL – Base de dados relacional;

Pydantic – Validação e schemas de dados;

Uvicorn – Servidor da API;

Arquitetura REST com endpoints para:

Utilizadores;

Publicações;

Comentários;

Gostos;

Seguidores;

Mensagens;

Notificações;

Autenticação.

🔗 Comunicação

Retrofit (Android) – Comunicação com a API;

JSON – Troca de dados entre frontend e backend;

HTTP Requests (GET, POST, DELETE).

📁 Funcionalidades e Ecrãs Implementados

🔐 Login e Registo de Utilizador

🏠 Feed Principal (posts dinâmicos)

➕ Criar Nova Publicação (texto + imagem da galeria)

❤️ Sistema de Likes

💬 Comentários em publicações

👤 Perfil do Utilizador

Foto de perfil

Bio

Estatísticas (posts, seguidores, a seguir)

Edição de perfil

🔎 Pesquisa de Utilizadores

➕ Sistema de Follow / Unfollow

💌 Mensagens Privadas

Chat em tempo real

Indicador de “typing…”

Mensagens lidas (seen)

Reações com emojis

🔔 Notificações

Likes

Comentários

Seguidores

🌙 Modo Claro / Escuro

📱 Bottom Navigation + Floating Action Button expansível

🗄️ Base de Dados

O sistema utiliza uma base de dados MySQL, com tabelas para:

Utilizadores;

Publicações;

Comentários;

Seguidores;

Mensagens;

Notificações.

A aplicação Android funciona de forma híbrida, combinando:

RoomDB (offline/local);

API REST (online);

Firebase para sincronização de mensagens.

🎥 Demonstração

📺 Vídeo demonstrativo do projeto:
👉 https://www.youtube.com/watch?v=WwTAKCIPXIM


O projeto foi desenvolvido exclusivamente para fins académicos, servindo como base de aprendizagem prática em desenvolvimento de aplicações móveis e sistemas distribuídos.
