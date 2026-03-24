# 🌿 LifeEngine

> Uma simulação de cadeia alimentar que nasceu de uma brincadeira entre amigos.

## 🍺 A origem

Tudo começou em uma roda de amigos, naquele tipo de conversa que começa sem destino e termina com uma ideia mirabolante. A brincadeira era simples: *"e se a gente pudesse ver o que acontece quando coloca um predador no meio de um monte de herbívoros?"*. A partir daí, o que era só papo virou código — e o LifeEngine nasceu.

A ideia central é observar como criaturas simples interagem, evoluem e (às vezes) se extinguem em um mundo finito. Nada de física quântica, nada de machine learning — só bichos com fome tentando sobreviver.

---

## 🧬 O que é

LifeEngine é uma simulação de ecossistema em tempo real com dois tipos de criaturas:

- **Herbívoros (`H`)** — buscam fontes de comida espalhadas pelo mapa para sobreviver.
- **Predadores (`P`)** — caçam herbívoros para se alimentar e ganhar energia.

Cada criatura possui atributos próprios que podem **mutar a cada geração**:

| Atributo | Descrição |
|---|---|
| `speed` | Quantos passos a criatura dá por ciclo |
| `perception` | Raio de visão (alcance para detectar comida/presas) |
| `aggression` | Bônus de energia ao abater uma presa (predadores) |
| `mutationRate` | Probabilidade de um atributo mudar na reprodução |

As criaturas nascem, envelhecem, se reproduzem e morrem. Cada filho pode herdar atributos levemente diferentes dos pais — simulating natural selection de forma bem básica, mas surpreendentemente divertida de observar.

---

## ⚙️ Stack técnica

| Camada | Tecnologia |
|---|---|
| Backend | Java 21 + Spring Boot 3.2.5 |
| Comunicação | WebSocket (STOMP) |
| Serialização | Jackson |
| Frontend | HTML + CSS + JavaScript (vanilla) |
| Build | Maven |

A simulação roda no servidor e envia o estado do mundo a cada ciclo via WebSocket para o frontend, que renderiza tudo em tempo real em um grid ASCII.

---

## 🖥️ Sobre o frontend

O frontend **não foi desenvolvido de forma 100% orgânica**. Grande parte da interface — estrutura do grid, painéis de estatísticas, controles de simulação e o sistema de eventos — foi construída com **auxílio significativo de IA**. O backend em Java foi o foco principal do desenvolvimento humano; o frontend serviu como camada de visualização e foi iterado rapidamente com essa ajuda.

Se o CSS parecer um pouco "gerado", é porque... bem, foi. E não tem problema nenhum nisso.

---

## 🚀 Como rodar

### Pré-requisitos

- Java 21+
- Maven 3.8+

```powershell
.\run.ps1
```

Depois, acesse: **http://localhost:8080**

---

## 🎮 Controles da simulação

| Ação | Descrição |
|---|---|
| ▶ Start / ⏸ Pause | Inicia ou pausa a simulação |
| 🔄 Restart | Reinicia o mundo do zero |
| ⚡ Velocidade | Ajusta o delay entre ciclos (mais rápido ou mais devagar) |
| 🍃 Taxa de comida | Controla a frequência com que novas fontes de comida aparecem |
| 🐣 Taxa de reprodução | Controla a chance de reprodução por ciclo |
| ⚠️ Crise! | Elimina todos os predadores — veja o que acontece com a população de herbívoros |

---

## 📊 Métricas exibidas em tempo real

- Ciclo atual e geração
- Contagem de herbívoros e predadores
- Fontes de comida disponíveis
- Total de nascimentos e mortes
- Médias populacionais: energia, velocidade, percepção e taxa de mutação

---

## 🌍 Parâmetros do mundo

O mundo tem **80 × 44** células. A simulação começa com:

- **28 criaturas** (herbívoros e predadores misturados aleatoriamente)
- **45 fontes de comida** distribuídas pelo mapa

---

## 💀 Condições de fim

A simulação para automaticamente em caso de **extinção total** — quando não sobra nenhuma criatura viva. É mais comum do que parece.

---

## 🤝 Contribuindo

Esse projeto nasceu de uma brincadeira e qualquer ideia nova é bem-vinda. Quer adicionar novos tipos de criaturas? Plantas que crescem? Sazonalidade? Abre uma issue ou manda um PR.

---

*Feito com café, papo de roda e uma pitada de IA para o front.*
