# Consolidação autorizada — 2026-10-01

Integração do bot Stockfish e folhas aprovada pelo usuário para merge no main.
O main já continha o novo MainMenuView e o ícone de peão (5889b592); as branches
feature/stockfish-bot e feature/forest-leaves continham motor, áudio e efeitos.

A consolidação preserva o novo menu/ícone, acrescenta seu cartão de bot e mantém
Local, Bluetooth, skins, configurações e controle da música. O PR de folhas #22
foi integrado ao bot. O PR #21 entrega o conjunto ao main após CI verde.

ConsolidatedMenuTest verifica os três modos e preferências e percorre o fluxo real
bot → dificuldade → cor → tempo → partida. A suíte existente continua validando
regras, Bluetooth, relógios, recriação, UCI, perft, áudio e animações. O build nativo
continua obrigatório no CI, com os três ABIs e teste no Android 35; não se gera uma
versão leve deixando o motor de fora.

A licença do PixelChess não foi alterada. Os avisos GPL do Stockfish e sua fonte
correspondente continuam acompanhando os APKs de revisão. A decisão de licença da
aplicação e disponibilidade durável das fontes precisam ser resolvidas antes de
um lançamento público; merge no main não publica uma release nem resolve essa questão.

Os relatórios STOCKFISH.md, INTEGRATION_REVIEW.md e FOREST_LEAVES.md documentam a
origem, parâmetros e auditorias das implementações anteriores. Este documento
registra a consolidação posterior ao estado inicial de branches separadas.
