# Temas — marco 1

Base: `feature/forest-skin-test`, commit `c9d410a877fc059568e5f7cd62d914f21e852d3e`.

## Estrutura

- `BoardTheme`: definição imutável de ID, nome, recursos, margem, tintas e efeito.
- `BoardThemes`: registro dos temas disponíveis e migração do índice antigo.
- `BoardThemeRenderer`: carregamento único de bitmaps e desenho de moldura, efeitos e tabuleiro.
- `FireflyMotion`: trajetórias determinísticas com fases, alturas e velocidades diferentes.
- `MainActivity.ChessView`: fornece os limites do tabuleiro e desenha peças/HUD por cima.

A preferência `skin` antiga continua sendo lida quando `theme_id` ainda não existe.
Novos temas devem usar IDs permanentes, nunca a posição na lista.

## Floresta Ancestral

O arquivo `forest_board.webp` permanece byte a byte igual. A versão 0.15.0 usa uma nova moldura de pedra (`forest_stone_frame.webp`); o antigo `forest_frame.webp` permanece no repositório para recuperação.
A margem externa passou de 14 dp para 14,98 dp (+7%). Isso aumenta a margem,
não o tamanho do tabuleiro nem a espessura uniforme de todos os detalhes do asset.
A margem continua limitada ao espaço disponível na tela.

As duas barras luminosas foram substituídas por 11 vaga-lumes: seis de um lado,
cinco do outro. Há deriva lenta, halos sobrepostos e pulsos independentes.
O Canvas exclui explicitamente o retângulo jogável ao desenhar os efeitos.
Intensidade, velocidade e cor ficam na configuração do tema.
Intensidade zero desliga o efeito; velocidade zero mantém uma imagem estática.
A atualização decorativa usa 20 fps apenas enquanto a View está visível.

## Contrato para próximos assets

- Recursos em `app/src/main/res/drawable-nodpi`, com nomes `<id>_board.webp` e `<id>_frame.webp`.
- Board quadrado, opaco, exatamente 8×8, sem margem externa ou coordenadas gravadas.
- A primeira casa (a8) é clara. Casas têm dimensões idênticas, alinhadas à borda do arquivo.
- Frame quadrado separado, canal alpha verdadeiro, abertura central alinhada ao board.
- Usar o par atual da Floresta como referência de proporção; conservar a resolução dos originais aprovados.
- WebP lossless para novos assets; não recomprimir os atuais para esta etapa.
- Não colocar sombra/glow nem partículas dentro do board. Efeitos são desenhados em tempo de execução.
- Carregar sem escala de densidade; renderizar bitmaps sem filtragem para preservar os pixels.
- Conferir alpha, centro, recorte e alinhamento nas quatro bordas antes de registrar um tema.
- Se uma nova moldura exigir abertura/proporção diferente, explicitar esse parâmetro no modelo antes de integrá-la.

## Validação

CI: `gradle :app:testDebugUnitTest :app:assembleDebug`.
Testes cobrem migração/fallback de preferências, controle de animação e limites das trajetórias.
Não substituem os futuros testes completos de regras/Bluetooth.

Checklist no aparelho antes de congelar a Floresta:

- Floresta e Tradicional, seleção persistida após reiniciar.
- Moldura legível, sem corte, em S23 Ultra e tela menor (ex.: 360 dp de largura).
- Vaga-lumes fora das casas, sem aparência espelhada e sem disputar atenção com as peças.
- Tocar na margem não seleciona peças da primeira linha/coluna.
- Partida local e Bluetooth nas duas orientações: movimentos, captura e promoção.
- Retorno ao menu sem callback de relógio da View descartada.

A aprovação visual no aparelho ainda é necessária: esta versão não congela a skin automaticamente.

## Integração da referência aprovada — 0.15.0

Assets gerados separadamente a partir da prévia aprovada com ImageGen:

| Recurso | Dimensões | Canal alpha | Uso |
|---|---|---|---|
| forest_floor.webp | 887 × 1774 | opaco | chão contínuo, sem interface |
| forest_stone_frame.webp | 1254 × 1254 | transparente | oito trechos de pedra em volta do board |
| forest_clock_plaque.webp | 2172 × 724 | transparente | placa vazia, sem texto ou números |

A conversão PNG → WebP é lossless, conferida pixel a pixel. Os originais gerados
não foram recortados ou retocados. O centro da moldura é transparente.
Os limites de amostragem da moldura são declarados em `BoardThemes`; as oito
regiões são mapeadas exclusivamente para fora do retângulo jogável.

O fundo usa center-crop proporcional e leve escurecimento. As placas são reutilizadas
nos dois relógios, com texto dinâmico e tonalidade quente na vez do jogador.
Relógios e mensagens agora usam o centro real da tela. A skin Tradicional conserva
seu painel original. O histórico recebeu contraste maior sobre a textura.

Verificação no aparelho ainda necessária para aprovar o encaixe visual final.

## Sistema consolidado — 0.16.0

A Floresta 0.15.0 foi aprovada visualmente pelo usuário no S23 Ultra em 29/09/2026.
`forest-baseline.json` registra o commit e hashes dos quatro assets ativos; a CI
confere esses arquivos antes da compilação. Uma revisão visual futura deve atualizar
explicitamente essa referência após aprovação.

Responsabilidades:

- `BoardTheme.Builder`: configura recursos, tintas, moldura, efeito e aparência dos relógios com argumentos nomeados.
- `BoardThemes`: registro dos temas disponíveis. Adicionar um tema aqui o inclui no seletor atual.
- `BoardThemeRenderer`: compõe fundo, moldura e tabuleiro e delega relógios/efeitos.
- `ThemeClockRenderer`: placas ilustradas e fallback tradicional, com Paint próprio.
- `ThemeEffectRenderer`: efeitos decorativos com clipping fora das casas e Paint próprio.
- `ThemePreferences`: persistência pelo ID e leitura compatível da preferência antiga.

Para adicionar um tema: produzir os assets no contrato existente, declarar uma entrada
no registro com `BoardTheme.builder(...)`, configurar o efeito e a aparência dos relógios,
e incluí-la em `ALL`. Não inserir condições por nome de skin no `MainActivity`.
Novos tipos de efeito serão implementados em `ThemeEffectRenderer`; hoje existem
NONE e FIREFLIES. Lava e gelo serão desenvolvidos junto dos respectivos temas.

A extração mantém os valores visuais aprovados da Floresta. A refatoração completa
de ChessView, regras, Bluetooth e relógio da partida continua no backlog.
A próxima entrega visual é Castelo Medieval, produzido asset por asset, seguido
de seletor por miniaturas. Não há skins futuras fictícias ou bloqueios no produto atual.
