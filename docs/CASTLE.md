# Castelo Medieval — versão de teste 0.17.0

A primeira skin criada sobre o sistema consolidado. ID permanente: `castle`.
Disponível no seletor existente, ao lado de Tradicional e Floresta Ancestral.

## Direção visual

Calcário marfim e ardósia azulada no tabuleiro; piso de fortaleza com detalhes de
ferro e bronze envelhecido; moldura de pedra talhada e placas de relógio sem textos.
O destaque do relógio ativo é aplicado pelo renderizador compartilhado.
O fundo recebe escurecimento maior que o da Floresta para manter contraste da UI.

## Assets

Todos gerados separadamente com ImageGen e convertidos para WebP lossless,
com identidade dos pixels conferida na conversão.

| Recurso | Dimensões | Alpha |
|---|---|---|
| castle_board.webp | 1254 × 1254 | opaco |
| castle_floor.webp | 887 × 1774 | opaco |
| castle_frame.webp | 1254 × 1254 | transparente |
| castle_clock_plaque.webp | 2172 × 724 | transparente |

Board: 8 linhas e 8 colunas conferidas visualmente; 64 amostras centrais confirmam
alternância claro/escuro, com a8 clara. A imagem é mapeada para o mesmo retângulo
jogável dos temas existentes. Arte com argamassa e lascas; o grid de jogo é exato.
Moldura: centro transparente; oito trechos definidos na configuração ficam fora das casas.
Placa: nomes e tempos são desenhados em tempo real, nunca gravados no asset.

A skin usa iluminação estática do fundo nesta versão, sem vaga-lumes da Floresta.
Não foi necessário adicionar nenhuma condição de Castelo ao MainActivity.

## Validação e próximos passos

- CI: hashes dos assets da Floresta, testes unitários e compilação do APK.
- Ainda requer aprovação visual no S23 Ultra, incluindo peças pretas sobre ardósia,
  leitura dos relógios e coordenadas, e teste em tela menor.
- Conferir alternância entre as três skins e seleção persistida após reiniciar.
- O seletor por miniaturas permanece uma entrega futura do backlog.

## Correção de cenário — 0.17.1

Feedback: o tabuleiro foi aprovado, mas o piso não comunicava um castelo.
O background ativo passou a `castle_hall.webp` (887 × 1774), salão com arcos,
estandartes, tochas e portal. Elementos reconhecíveis ficam no topo e no rodapé,
fora da área coberta pelo tabuleiro. A imagem já possui áreas escuras, por isso
seu overlay usa alpha 24 em vez de 101. Conversão WebP lossless verificada.
O piso antigo fica disponível no histórico/repositório; não é utilizado pelo tema.
Board, moldura, relógios e regras permanecem na versão anterior.
Esta correção ainda requer validação visual no aparelho.

## 0.18.0 — throne-room reference and animated lighting

The user selected the king-facing-player reference on 2026-09-29 and authorized implementation with animated torches and lighting. `castle_throne_scene.webp` is a separate environment plate; the approved `castle_board.webp`, frame and piece sprites are unchanged. The king faces the player while the playable grid remains a square orthographic 8×8.

The scene is rendered in three vertical regions, with source boundaries at y=450 and y=1294 of the 887×1774 plate. These follow the board frame instead of center-cropping the king. Torch anchors use the same mapping. The upper clock sits in the banner area, above the crown; turn/check/end status moves below the lower clock. On short portrait screens the square board shrinks to reserve space for the scene and HUD; hit testing uses the same geometry as rendering. Classic and Forest retain their original board layout.

Four pixel flames animate with independent phases and continuous bounded motion. Warm radial illumination varies with the fire, with two faint embers per brazier. All effects are clipped out of the 64 playable squares. Paints and shaders are reused; no bitmaps or shaders are created per frame. The existing visible-view redraw cadence is 50 ms. Glow color, intensity and speed remain theme parameters; speed zero freezes motion and intensity zero removes the effects. No changes to game rules, Bluetooth or timer accounting.

Validation: scene/clock/torch geometry tests at 320×480, 360×640, 393×760, 412×870 and 600×960 logical pixels; one-hour sampling of animation bounds, continuity and phase separation; original Forest geometry regression; immutable scene anchors and invalid configuration tests. Actual visual quality and device performance still require handset review.

### Asset provenance

Created with the built-in image-generation tool using the user's attached reference for style and composition. Converted to lossless WebP without creative post-processing. Final resource: `app/src/main/res/drawable-nodpi/castle_throne_scene.webp`.

Production prompt: a full-bleed 1:2 portrait pixel-art throne room, bearded crowned king with fur mantle centered facing the player above a blank charcoal tabletop, crimson lion banners, stone walls, four ember-only braziers, guards and red carpet below. No board, pieces, clocks, plaques, letters, numbers or UI baked into the environment. Warm amber edge lighting with cool shadows. Leave upper-center banner space for the runtime clock; animate the torch flames in the engine.
