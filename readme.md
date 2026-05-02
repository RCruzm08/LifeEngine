# LifeEngine — Ecosystem+

Simulação de ecossistema evolutivo com Spring Boot (backend) e simulação standalone em JavaScript puro (frontend).

>  Código auditado e estendido manualmente.

---

## Melhorias v4.0

### Novas funcionalidades
- **3ª espécie — Onívoro** (`O`): come tanto plantas quanto herbívoros. Pode emergir espontaneamente de herbívoros sob pressão predatória.
- **Biomas** no backend Java (`Biome.java`): floresta (mais comida), deserto (mais lento, dano de calor), água (intransponível), planície (mais rápido).
- **Heatmap de atividade**: overlay visual mostrando tiles mais frequentados. Ative com o botão 🌡 Heatmap.
- **Overlay de biomas**: visualize o terreno em tempo real. Ative com o botão 🗺 Biomas.
- **Reprodução sexual**: criaturas próximas do mesmo tipo combinam genes com média + mutação. Mais pressão seletiva.
- **Painel de debug**: passe o mouse sobre uma criatura para ver todos seus genes no painel lateral.
- **Salvar snapshot**: botão 💾 exporta o estado atual em JSON (`lifeengine_ciclo_N.json`).

### Correções de arquitetura (backend Java)
- `triggerCrisis()` substituiu `getDeclaredField("alive")` por `creature.kill()` — sem mais reflexão frágil.
- `World.java` usa `CopyOnWriteArrayList` para thread-safety entre o scheduler e requests REST.
- `nearestThreat()` implementado para comportamento de fuga correto.
- Endpoint `/api/sim/snapshot` e `/api/sim/status` adicionados.
- `biomeMap` e `heatmap` transmitidos via WebSocket a cada tick.

---

## Rodando

### Standalone (sem backend)
Abra `LifeEngine_v3_preview.html` diretamente no browser.

### Com Spring Boot
```powershell
cd LifeEngine
.\run.ps1
```
Acesse: http://localhost:8080

### Build manual
```bash
mvn clean package -q
java -jar target/*.jar
```

---

## Estrutura
```
src/main/java/com/lifeengine/
├── model/
│   ├── Biome.java          ← NOVO: enum de biomas
│   ├── Creature.java       ← kill(), reproduceWith(), OMNIVORE
│   ├── CreatureType.java   ← OMNIVORE adicionado
│   └── World.java          ← CopyOnWrite, biomeGrid, heatmap, nearestThreat
├── engine/
│   └── SimulationEngine.java ← snapshot, biomeMap WS, omni stats
└── websocket/
    ├── SimulationController.java ← /snapshot, /status
    └── SimulationWebSocketHandler.java
```

---

## Genes evoluíveis
| Gene | Faixa | Efeito |
|------|-------|--------|
| `speed` | 1–5 | Passos por tick, custo de energia |
| `perception` | 1–8 | Raio de busca por comida/ameaça |
| `aggression` | 1–10 | Bonus de energia ao caçar |
| `mutationRate` | 0.05–0.6 | Probabilidade de mutação nos filhos |
| `heatTolerance` | 0.1–1.0 | Resistência a biomas extremos |
