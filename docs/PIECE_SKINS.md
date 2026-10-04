# Skins de peças — sprites originais

A seleção de peças aparece na **mesma tela das skins de tabuleiro**. Ela é independente do tema e do estado de xadrez. Há três opções:

- **Tradicional:** PNGs originais aprovados, sem animações novas.
- **Guardiões:** seis personagens originais distintos (soldado com lança, guarda de torre e escudo, cavaleiro, mago, imperatriz e rei). Cada personagem existe nas cores claras e escuras, com desenho frontal e traseiro e quatro poses de animação.
- **Obsidiana:** seis esculturas minerais distintas (obelisco, fortaleza, cabeça de cavalo, espira, coroa de rainha e coroa de rei), também em duas cores e duas perspectivas. Estas esculturas não respiram.

## Perspectiva correta

As peças da fileira **mais próxima** da pessoa que joga são desenhadas pelas costas; as peças distantes são desenhadas de frente. Isso acompanha automaticamente a orientação efetiva do tabuleiro, incluindo jogador preto no Bluetooth, opção de orientação local e cor escolhida contra o Stockfish. O cálculo acontece apenas em `ChessView`; **nenhum campo novo é adicionado ao protocolo Bluetooth ou às regras de xadrez**.

## Animações

Guardiões: a pose parada alterna a altura do tronco, o piscar e um leve deslocamento dos braços em quatro quadros. Peças diferentes começam em fases ligeiramente diferentes para evitar animação sincronizada. Durante deslocamentos, as poses de passo são usadas na trajetória já aprovada de 220 ms para peças comuns e 360 ms para cavalos. O app interrompe a atualização ao sair da tela.

Obsidiana: peças esculpidas sem respiração; usam apenas a animação de deslocamento já existente. A skin Tradicional conserva seus sprites originais e comportamento anterior.

## Sprites reais e exportação para artistas

`PixelPieceArt.java` desenha cada sprite em **32 × 40 pixels nativos** (sem suavização) e guarda as imagens geradas na memória. Cada papel tem silhueta própria, não apenas um filtro de cores. A preferência fica em `PieceSkin` e as miniaturas de `PieceSkinPreview` mostram **as mesmas imagens usadas no tabuleiro**.

`PixelPieceArtTest` testa silhuetas distintas, animações, vistas frontal/traseira e salva os arquivos PNG sem perdas em `build/piece-skin-export/`. A CI publica `PixelChess-animated-piece-skins-PNG`, com **120 PNGs individuais** (96 Guardiões + 24 Obsidiana) e duas galerias que facilitam avaliação. Essas imagens são geradas dos mesmos bitmaps do jogo e podem ser editadas por artistas para uma futura substituição por atlas externo.

## Validação

Os testes automatizados verificam originalidade das silhuetas por tipo/cor, duas perspectivas para todos os desenhos, alteração real de pixels nas animações, orientação do lado próximo (brancas e pretas) e seleção com confirmação no menu. A validação visual final ainda deve ser feita em aparelho físico, especialmente em casas escuras e com o tabuleiro da Floresta Ancestral.
