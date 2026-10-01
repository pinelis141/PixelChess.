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
| The Quiet Gambit (música do menu) | Fornecida e aprovada pelo responsável | Não há documento da licença do áudio no repositório; confirmar direitos/termos de redistribuição antes de lançamento público |
| Imagens, sprites e cenários | Assets aprovados do projeto; folhas geradas com prompt registrado | Fora da concessão de licença do código; confirmar e documentar direitos de redistribuição/licença de cada conjunto antes de lançamento público |
| Dependências de teste / ferramentas Android | JUnit, Robolectric, Gradle, Android SDK/NDK | Não são relicenciadas pelo projeto; dependências de teste não são empacotadas como motor/runtime do APK |

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
