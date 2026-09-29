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
