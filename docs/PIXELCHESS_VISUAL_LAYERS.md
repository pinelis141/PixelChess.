# PixelChess — Plano técnico de camadas visuais

Branch de implementação: `feature/board-themes`

## Regra principal
A `main` é estável. Não fazer merge desta branch sem aprovação explícita.

## Ordem de renderização
1. Background do cenário
2. Ground / terreno
3. Frame / moldura
4. Board / tabuleiro
5. Coordenadas
6. Peças
7. Highlights
8. Relógios / UI
9. Textos auxiliares

## Matriz de assets

| Arquivo / camada | Função | Status atual | Próxima ação |
| --- | --- | --- | --- |
| `forest_scene_bg.webp` | Background vertical da Floresta | ATIVO, drawable real | Manter; renderizar com center-crop |
| `forest_ground_ring.webp` | Faixa de terreno entre floresta e frame | SUPORTADO, ainda sem asset dedicado | Criar asset transparente e registrar em `ThemeAssets.groundRes` |
| `stone_frame.webp` | Moldura de pedra/musgo | SUPORTADO, fallback procedural ativo | Substituir fallback por asset transparente final |
| `stone_board_pixel.png` | Tabuleiro mármore verde/creme | ATIVO / aprovado | Preservar |
| `forest_clock_panel.webp` | Painel de relógio em madeira | ATIVO, drawable real | Preservar e ajustar escala se necessário |
| 12 sprites das peças | Peças brancas/pretas | ATIVOS / aprovados | Preservar; cavalo em 90% |
| Highlights | Movimento/captura/xeque | ATIVOS por código | Preservar |

## Arquitetura
A classe `ThemeAssets` define recursos independentes:
- `backgroundRes`
- `groundRes`
- `frameRes`
- `boardRes`
- `clockRes`

O renderer não deve conhecer um único PNG “com tudo pronto”. Cada camada deve poder ser substituída sem alterar a lógica do xadrez.

## Tema Forest Marble
Atualmente:
- background: `forest_scene_bg`
- ground: fallback procedural isolado
- frame: fallback procedural isolado
- board: `stone_board_pixel`
- clock: `forest_clock_panel`

Quando os assets finais de ground/frame forem adicionados, basta preencher `groundRes` e `frameRes` no `ThemeAssets`; a ordem de renderização não muda.

## Regras visuais
- Não esticar background: usar center-crop.
- Árvores ficam fora da zona do tabuleiro.
- Deve existir respiro de terreno entre vegetação e moldura.
- Coordenadas ficam na moldura.
- Tabuleiro e peças têm prioridade visual.
- Cenário nunca deve cobrir casas ou highlights.
