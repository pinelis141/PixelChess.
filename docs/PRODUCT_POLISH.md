# UX, testes e distribuição

## Pronto nesta etapa

- O seletor de skins agora tem estado visual e descrição acessível para temas bloqueados; temas bloqueados não recebem clique de seleção. O catálogo atual continua mostrando apenas temas disponíveis.
- Configurações persistentes para vibração, efeitos de tema, posição das pretas embaixo e sons curtos de jogadas. Sons começam desligados.
- Movimento de peça usa arco curto, easing e um anel de captura. Seleção realçada e linhas de estado/histórico ajustadas à largura disponível.
- Falhas de criação/entrada Bluetooth oferecem nova tentativa e retorno claro ao menu. A perda durante jogo termina a sessão; não há serialização/resumo do estado para retomar ainda.
- CI testa dimensões 8×8 dos tabuleiros registrados, transparência central das molduras, regras especiais, seletor e movimento; gera APK debug, APK release sem assinatura e AAB sem assinatura.
- Ícone vetorial inicial e versionamento 0.24.0 / code 27.

## Limites restantes

- `MainActivity` ainda contém a coordenação de tela e a classe `ChessView`; extração completa para `ChessView` independente precisa ser uma mudança arquitetural própria.
- Reconexão Bluetooth ainda requer ação manual e uma nova partida. Retomada exige protocolo versionado para salvar, validar e sincronizar tabuleiro, direitos de roque, en passant, histórico, relógios e autoridade.
- Testes automatizados agora incluem roque, en passant, promoção e repetição tripla, além do mate. Afogamento, xeque-mate por casos adicionais, regra de 50 lances e material insuficiente pedem posições de teste configuráveis; isso ainda não foi adicionado ao motor.
- Os pacotes release são unsigned. Para distribuir/instalar como atualização é necessário configurar uma keystore da equipe e as credenciais em GitHub Actions Secrets; nenhuma chave privada deve ir para o repositório.
- Música do menu e efeitos de jogada precisam de validação auditiva em aparelho físico; CI só verifica compilação e lógica do ciclo de vida.
- O ícone atual é uma base vetorial simples e ainda pede aprovação visual antes de uma publicação.
