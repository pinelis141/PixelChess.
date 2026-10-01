# Licenciamento — decisão de 2026-10-01

O responsável autorizou publicar o código e adotar licença aberta compatível com
Stockfish. O código original do PixelChess (Java, scripts e configuração de build)
é **GNU GPL versão 3 ou posterior (GPL-3.0-or-later)**. A concessão está em NOTICE;
o texto integral está em LICENSE. A autorização não é uma atribuição de autoria
sobre obras de terceiros nem uma mudança das licenças próprias deles.

## Inventário

| Componente | Origem | Licença / situação |
|---|---|---|
| Código original PixelChess | Repositório e histórico de contribuições | GPL-3.0-or-later |
| Stockfish 19 / NNUE incorporado | official-stockfish/Stockfish, sf_19, edb0d9db6731067ec50ce619ff372b463bc4dd5d | GPL-3.0-or-later; upstream inalterado; AUTHORS/COPYING/NOTICE preservados |
| Som de peças de alabastro | mh2o, Freesound 351518; preview público convertido | CC0 1.0; origem e alterações em docs/PIECE_AUDIO.md |
| The Quiet Gambit (música do menu) | Gerada com IA no Gemini, em plano pago, conforme declaração do responsável em 2026-10-01 | Fornecida para uso no PixelChess; separada da GPL do código; não declarada CC0 ou domínio público; análise dos termos abaixo |
| Biblioteca Real — menu | Referência fornecida pelo responsável e aprovada em 2026-10-01; imagem em assets/royal_library_menu.b64, animação e moldura vetorial em Java | Mídia separada da GPL do código, conforme demais imagens do projeto; referência convertida para WebP qualidade 78 e codificada em base64, sem alteração de composição |
| Imagens, sprites e cenários | Gerados com IA conforme declaração do responsável em 2026-10-01; folhas com prompt registrado | Fora da concessão de licença do código; origem por IA não equivale a domínio público; preservar referências e termos da ferramenta aplicável |
| Dependências de teste / ferramentas Android | JUnit, Robolectric, Gradle, Android SDK/NDK | Não são relicenciadas pelo projeto; dependências de teste não são empacotadas como motor/runtime do APK |

## Origem da mídia e termos consultados em 2026-10-01

O responsável informou que a mídia original restante foi criada com IA e que
The Quiet Gambit foi gerada no Gemini em plano pago. Esta declaração registra
proveniência; não comprova exclusividade autoral nem identifica a versão exata
do modelo. Stockfish e a gravação mh2o/Freesound são componentes de terceiros,
com licenças próprias, e não estão abrangidos por essa declaração.

Os Termos de Serviço do Google, versão Brasil em vigor desde 2026-07-30,
seção “Conteúdo nos serviços do Google / Seu conteúdo”, dizem que o Google
não reivindica propriedade sobre conteúdo original gerado. A seção
“Permissão para usar seu conteúdo” preserva os direitos que o usuário tiver.
Essas cláusulas são fundamento da interpretação de uso da faixa no projeto;
não são uma licença específica de redistribuição de música, uma declaração
de domínio público ou garantia de ausência de direitos de terceiros.
Não foi identificada nas páginas oficiais de música consultadas uma cláusula
específica de autorização de redistribuição comercial em APK/repositório.
O pagamento do plano, por si só, não resolve essa distinção.

A política de IA do Google exige respeitar direitos de terceiros e proíbe
representações enganosas de origem humana. Os créditos identificam a origem
por IA. O áudio permanece fora da licença GPL do código; não é marcado CC0.
Para preservar o registro, guardar arquivo original, prompt, data de geração
e identificação do serviço/plano, quando disponíveis. Os termos aplicáveis
na data da geração e eventuais termos específicos também devem ser preservados.

Referências oficiais consultadas:
- https://policies.google.com/terms?gl=BR&hl=pt-BR
- https://policies.google.com/terms/generative-ai/use-policy?hl=pt-BR
- https://support.google.com/gemini/answer/16901237?hl=en
- https://gemini.google/overview/music-generation/

## Fonte correspondente e builds

O CI arquiva o código rastreado do PixelChess no commit exato, com metadados da revisão,
e produz em separado a fonte exata do Stockfish, NNUE, receita e checksums. O diálogo
Licenças e créditos mostra o commit do aplicativo quando construído pelo CI. Para
recompilar o aplicativo a partir do arquivo, exporte PIXELCHESS_SOURCE_REVISION com
SHA informado no BUILD.txt; instale JDK 17, Gradle 8.7, Android SDK 35 e NDK fixado.
A fonte do motor contém a receita de reconstrução offline dos três ABIs; copie os
binários resultantes para app/src/main/jniLibs antes de compilar o aplicativo.

Ao distribuir APK/AAB sob a GPL, preserve LICENSE/NOTICE e avisos de terceiros,
forneça fonte correspondente com scripts/configurações/modificações necessários,
e preserve o direito de modificar/redistribuir. Para downloads, apresente as fontes
com acesso equivalente junto ao binário. Não acrescente restrições incompatíveis.
Avalie informações de instalação quando a distribuição se enquadrar em GPL seção 6.

Artifacts do GitHub Actions não são armazenamento permanente. Antes de publicar uma
release, mantenha os arquivos de fonte acessíveis em local durável, com links claros
junto aos binários e os metadados/checksums da versão. Nenhuma release pública é criada
por esta mudança. A decisão da licença do código está resolvida; o inventário de mídia
não documentado e a hospedagem durável continuam como preparação de distribuição.

A escolha GPL para o código evita depender da suposição de que UCI/processo separado
por si só excluiria a aplicação da GPL. Os assets com outras licenças conservam seus
termos próprios; esta decisão não declara toda a mídia como GPL sem comprovação.

Fontes oficiais verificadas:
- https://stockfishchess.org/about/ (GPL, redistribuição e fonte exata)
- README e Copying.txt do Stockfish na revisão fixada (GPL e créditos NNUE/Leela)
- LICENSE, GPL v3, seções 4–6 (avisos, obra combinada e fonte correspondente)
- https://creativecommons.org/publicdomain/zero/1.0/ (áudio CC0)
