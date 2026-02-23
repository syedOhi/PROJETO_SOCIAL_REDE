📱 PROJETO_SOCIAL_REDE
🧩 Descrição Geral:

Este projeto consiste no desenvolvimento de um protótipo funcional de uma aplicação móvel de rede social no âmbito da unidade curricular de PAC.
Inicialmente focado na interface do utilizador, evoluiu para uma solução completa com backend próprio, API REST, base de dados relacional e funcionalidades dinâmicas, simulando o funcionamento de uma rede social real.

A aplicação permite interação entre utilizadores, criação de conteúdos, sistema de seguidores, mensagens em tempo real e notificações.

🎯 Objetivos:

Desenvolver uma interface móvel moderna e intuitiva;

Implementar funcionalidades essenciais de uma rede social:
registo, login, perfis, publicações, feed, likes, comentários, seguidores, mensagens e notificações;

Integrar API REST com base de dados;

Simular comunicação cliente-servidor real;

Consolidar conhecimentos em mobile, APIs e bases de dados;

🧪 Tecnologias Utilizadas
📱 Frontend

Android Studio, Kotlin, Jetpack Compose, Material 3,Navigation Component

ViewModel + StateFlow / LiveData

RoomDB (offline) e SharedPreferences

Coil (imagens)

Firebase Firestore (chat em tempo real)

🌐 Backend / API

FastAPI (Python), SQLAlchemy, MySQL

Pydantic, Uvicorn

Arquitetura REST para utilizadores, posts, comentários, likes, seguidores, mensagens e notificações

🔗 Comunicação

Retrofit + JSON

HTTP (GET, POST, DELETE)

📁 Funcionalidades Implementadas

Login e registo

Feed dinâmico

Criação de publicações (texto + imagem)

Likes e comentários

Perfil editável com estatísticas

Pesquisa de utilizadores

Follow / Unfollow

Chat privado em tempo real (typing, seen, emojis)

Notificações de interações

Modo claro/escuro

Bottom Navigation + FAB

🗄️ Base de Dados

Base de dados MySQL com tabelas para utilizadores, publicações, comentários, seguidores, mensagens e notificações.
A aplicação funciona de forma híbrida: RoomDB (offline), API REST (online) e Firebase para o chat.

🎥 Demonstração

📺 https://www.youtube.com/watch?v=WwTAKCIPXIM
