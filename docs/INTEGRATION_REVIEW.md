# PixelChess 0.27.1 — revisão da integração offline

Branch: `feature/stockfish-bot`. PR: https://github.com/pinelis141/PixelChess./pull/21
Base auditada: `98ce90877cca707db0842e0f9f06aa7dd327f2da`. O main não foi alterado.

## Resultado da auditoria do diff

- ChessGame continua validando e executando todas as jogadas. Seu código não mudou.
- GameClock e os componentes/protocolo Bluetooth não mudaram. Os caminhos de modo
  Local e Bluetooth permanecem, com os testes de regras, sincronização, relógio e
  restauração existentes. O perft inicial depth 4 verifica 197.281 nós.
- ChessView ganhou apenas a integração de turno, cor/orientação, validação do estado
  atual, cancelamento e apresentação do bot. MainActivity ganhou seleção do bot,
  documentação do motor e tratamento do ciclo de vida. Não houve redesenho do menu.
- BotController mantém uma única solicitação em worker. StockfishEngine usa UCI em
  processo separado, com limites de busca, leitura e watchdog. Respostas ilegais,
  inválidas, tardias ou falhas não alteram ChessGame e oferecem volta ao menu.
- Pensamento e espera de apresentação contam no relógio monotônico do bot. Timeout
  mantém a regra de empate quando o potencial vencedor tem apenas o rei. Saída,
  pausa, término e destruição cancelam respostas e encerram o motor.
- Promoções Q/R/B/N, roque, en passant, mate, empate, histórico, animação, vibração,
  temas e preferências passam pelo caminho existente. A cor humana orienta o tabuleiro.
- Som gravado de alabastro substitui a síntese. Movimento/captura são byte a byte
  iguais, em ambas as cores, com ganho comum de 65%; final usa dois toques iguais.
  Arquivos PCM têm pico de 45%, duração limitada e término em silêncio.
- Ritmo e Fácil aprovados no celular foram preservados nesta atualização de áudio.

## Validação automatizada

A revisão deve usar o workflow verde do HEAD da branch, com as etapas:
`testDebugUnitTest`, `lintDebug`, `assembleDebug`, `assembleRelease`, `bundleRelease`.
Além disso, o CI instala o APK debug no emulador Android 35 e executa o Stockfish
empacotado sob o UID do aplicativo: cinco configurações, roque, en passant,
subpromoção, posição terminal e encerramento sem processo restante.

Os testes específicos incluem ilegalidade, bloqueio da cor do bot, orientação nas
duas cores, promoção nas quatro peças, roque/en passant, mate/empate/timeout,
solicitação única, falha, saída durante busca e durante espera de apresentação,
recriação da Activity, limites UCI diferentes e encerramento de processo/leitor.
Os testes de áudio cobrem arquivos reais, igualdade por toque, duas cores, bot,
mute, capturas, en passant, mate, pausa, release e falha do SoundPool.

O ambiente desta revisão não tem SDK Android/JDK de compilação instalado; os testes,
Lint e os três builds são executados no GitHub Actions. A preparação dos WAVs foi
executada e repetida localmente com checksums iguais, sem acesso à rede.

## Teste pendente no celular

Este checklist precisa de validação humana; emulador não confirma audição, percepção
de dificuldade, bateria/temperatura ou conectividade Bluetooth entre dois aparelhos.

1. Ativar **Configurações → Sons de jogadas**. Com volume de mídia fixo, comparar
   movimentos e capturas de brancas e pretas; conferir os dois toques finais.
2. Comparar Normal, Difícil, Especialista e Máximo com o Fácil aprovado. Os alvos
   de força não são Elo medido; não mudar parâmetros apenas pelo tempo de resposta.
3. Jogar com ambas as cores e Aleatória; testar promoção, roque, en passant, histórico,
   mate, timeout, temas, vibração e música ao voltar ao menu.
4. Durante o pensamento, sair, alternar apps e recriar a Activity; conferir a partida
   restaurada, o relógio e ausência de jogadas atrasadas/entrada pela cor do bot.
5. Fazer uma partida Local e outra Bluetooth entre dois aparelhos, incluindo
   sincronização/reconexão e relógios. Confirmar consumo aceitável no Máximo.

## Motor, parâmetros e licenças

Arquitetura: ChessView → BotController → ChessEngine → StockfishEngine → Stockfish.
Versão oficial: **Stockfish 19**, tag `sf_19`, revisão
`edb0d9db6731067ec50ce619ff372b463bc4dd5d`, sem alterações no código upstream.
NDK 28.2.13676358, API 26, arm64-v8a / armeabi-v7a / x86_64. Execução totalmente offline.

Todas as dificuldades: Threads=1, Hash=32 MB, Ponder=false; limites simultâneos:

| Nível | Limitação de força | Depth | Nós | Movetime máximo |
|---|---|---|---|---|
| Fácil | Skill 0; em 25% das buscas fora de xeque com mais de 3 jogadas, 3 candidatas legais aleatórias via searchmoves | 1 | 500 | 150 ms |
| Normal | Skill 4 | 6 | 10.000 | 350 ms |
| Difícil | UCI_LimitStrength=true, UCI_Elo=1800 | 14 | 100.000 | 700 ms |
| Especialista | UCI_LimitStrength=true, UCI_Elo=2400 | 20 | 500.000 | 1.500 ms |
| Máximo | Skill 20, UCI_LimitStrength=false | 64 | 2.000.000 | 2.500 ms |

Movetime também é limitado a tempo restante/20 (mínimo 1 ms). A espera visual é de
2 segundos desde o início da solicitação, limitada a restante/4; busca mais lenta
não recebe 2 segundos adicionais. Animação do bot: 500 ms, ou 650 ms para cavalo.
A espera não define a dificuldade. Detalhes e fontes: [STOCKFISH.md](STOCKFISH.md).

Stockfish é GPL-3.0-or-later. APK inclui GPL, autores e aviso. O artifact de fonte
correspondente inclui revisão exata, NNUE, receita e parâmetros. Ao distribuir
binários, disponibilizar essa fonte com acesso equivalente no mesmo local, preservar
avisos e direitos da GPL e avaliar requisitos de instalação aplicáveis. Processo
UCI separado não resolve automaticamente se a aplicação inteira é obra combinada.
PixelChess não foi relicenciado; a decisão de licença compatível, se necessária,
continua pendente antes de distribuição pública. Não foi feito merge nem release.

Áudio: mh2o/Freesound, CC0 1.0, preview público; conversão, fades e normalização
registrados em [PIECE_AUDIO.md](PIECE_AUDIO.md) e aviso empacotado em assets/audio.
CC0 permite adaptação e redistribuição comercial. Nenhum áudio do vídeo/Lichess foi copiado.

## Arquivos da integração

- Jogo: `ChessView.java`, `MainActivity.java`, `ChessSounds.java`.
- Nova abstração: `bot/ChessEngine.java`, `BotController.java`, `StockfishEngine.java`,
  `EnginePosition.java`, `EngineMove.java`, `BotDifficulty.java`, `EasyMovePolicy.java`.
- Áudio: `res/raw/stone_{move,capture,terminal}.wav`, `assets/audio/NOTICE.txt`,
  `tools/audio_sources/{alabaster_preview.mp3,README.md}`, `tools/prepare_piece_audio.py`.
- GPL: `assets/stockfish/{AUTHORS,COPYING.txt,NOTICE.txt}`.
- Build/CI: `.github/workflows/android.yml`, `.gitignore`, `app/build.gradle`,
  `tools/{build_stockfish.sh,compile_stockfish.sh,verify_android_stockfish.sh,verify_android_stockfish.py}`.
- Testes: `BotIntegrationTest.java`, `BotActivityRecreationTest.java`,
  `EngineContractTest.java`, `ChessSoundsTest.java`, `StoneAudioTest.java`,
  `bot/EasyMovePolicyTest.java`; asserção depth 4 em `PerftAuditTest.java`.
- Documentação: `docs/{STOCKFISH.md,PIECE_AUDIO.md,INTEGRATION_REVIEW.md}`.
- O gerador sintético intermediário `tools/generate_stone_sounds.mjs` foi removido;
  não faz parte do diff final em relação ao main.
