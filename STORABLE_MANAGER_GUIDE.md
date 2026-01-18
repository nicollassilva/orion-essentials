# StorableManager - Guia de Uso

## Visão Geral

`StorableManager<T>` é uma classe abstrata que fornece funcionalidade completa para gerenciar dados persistentes em arquivos JSON. Ela automatiza o processo de carregar, salvar e recarregar configurações, permitindo que você foque apenas na lógica específica do seu manager.

## Características Principais

- ✅ **Genérico**: Funciona com qualquer tipo de dados
- ✅ **Automático**: Carrega automaticamente ao instanciar
- ✅ **Seguro**: Cria arquivos padrão se não existirem
- ✅ **Flexível**: Hooks para customização
- ✅ **Simples**: Reduz código boilerplate

## Como Usar

### 1. Criar a Classe de Dados

Primeiro, crie uma classe para representar seus dados:

```java
public static class MinhaData {
    private String valor1 = "padrão";
    private int valor2 = 0;
    private List<String> lista = new ArrayList<>();
    
    // Getters e Setters...
}
```

### 2. Estender StorableManager

Crie seu manager estendendo `StorableManager<T>`:

```java
public class MeuManager extends StorableManager<MeuManager.MinhaData> {
    
    // Construtor: define o nome do arquivo
    public MeuManager(@Nonnull Path dataFolder) {
        super(dataFolder, "meuarquivo.json", MinhaData.class);
    }
    
    // Implementar método abstrato: retorna dados padrão
    @Override
    protected MinhaData createDefaultData() {
        return new MinhaData();
    }
    
    // Adicione seus métodos específicos aqui
    public String getValor1() {
        return this.data.valor1;
    }
    
    public void setValor1(String valor) {
        this.data.valor1 = valor;
        this.saveConfig(); // Salva após alterar
    }
}
```

### 3. Usar o Manager

```java
// No seu plugin
MeuManager manager = new MeuManager(plugin.getDataDirectory());

// Usar os métodos
String valor = manager.getValor1();
manager.setValor1("novo valor");

// Recarregar do arquivo
manager.reload(MeuManager.MinhaData.class);
```

## Métodos Disponíveis

### Métodos Protegidos (para suas subclasses)

#### `saveConfig()`
Salva os dados atuais no arquivo.

```java
public void adicionarItem(String item) {
    this.data.lista.add(item);
    this.saveConfig(); // Persiste a mudança
}
```

#### `getData()`
Obtém o objeto de dados atual.

```java
public MinhaData obterDados() {
    return this.getData();
}
```

### Métodos Abstratos (você deve implementar)

#### `createDefaultData()`
Cria uma nova instância com valores padrão.

```java
@Override
protected MinhaData createDefaultData() {
    MinhaData data = new MinhaData();
    data.setValor1("padrão inicial");
    return data;
}
```

### Hooks Opcionais (você pode sobrescrever)

#### `beforeSaveDefaultConfig()`
Chamado antes de salvar o arquivo padrão pela primeira vez.

```java
@Override
protected void beforeSaveDefaultConfig() {
    // Adicione lógica de inicialização aqui
    System.out.println("Criando arquivo de configuração...");
}
```

#### `afterLoadConfig(T loadedData)`
Chamado após carregar dados do arquivo com sucesso.

```java
@Override
protected void afterLoadConfig(MinhaData loadedData) {
    // Valide ou processe os dados carregados
    if (loadedData.lista.isEmpty()) {
        loadedData.lista.add("item padrão");
    }
}
```

### Métodos Públicos

#### `reload(Class<T> dataClass)`
Recarrega os dados do arquivo.

```java
manager.reload(MinhaData.class);
```

## Exemplos Completos

### Exemplo 1: ConfigManager Simples

```java
public class ConfigManager extends StorableManager<ConfigManager.ConfigData> {
    
    public ConfigManager(@Nonnull Path dataFolder) {
        super(dataFolder, "config.json", ConfigData.class);
    }
    
    @Override
    protected ConfigData createDefaultData() {
        return new ConfigData();
    }
    
    public String getDiscordLink() {
        return this.data.discordLink;
    }
    
    public void setDiscordLink(String link) {
        this.data.discordLink = link;
        this.saveConfig();
    }
    
    public static class ConfigData {
        private String discordLink = "";
    }
}
```

### Exemplo 2: WarpManager com Map

```java
public class WarpManager extends StorableManager<WarpManager.WarpData> {
    
    public WarpManager(@Nonnull Path dataFolder) {
        super(dataFolder, "warps.json", WarpData.class);
    }
    
    @Override
    protected WarpData createDefaultData() {
        return new WarpData();
    }
    
    public void setWarp(String name, Location loc) {
        this.data.warps.put(name.toLowerCase(), loc);
        this.saveConfig();
    }
    
    public Location getWarp(String name) {
        return this.data.warps.get(name.toLowerCase());
    }
    
    public boolean deleteWarp(String name) {
        boolean removed = this.data.warps.remove(name.toLowerCase()) != null;
        if (removed) {
            this.saveConfig();
        }
        return removed;
    }
    
    public Set<String> getAllWarps() {
        return this.data.warps.keySet();
    }
    
    public static class WarpData {
        private Map<String, Location> warps = new HashMap<>();
    }
    
    public static class Location {
        private double x, y, z;
        private float yaw, pitch;
        // Getters e Setters...
    }
}
```

### Exemplo 3: PlayerDataManager com Validação

```java
public class PlayerDataManager extends StorableManager<PlayerDataManager.PlayerData> {
    
    public PlayerDataManager(@Nonnull Path dataFolder) {
        super(dataFolder, "playerdata.json", PlayerData.class);
    }
    
    @Override
    protected PlayerData createDefaultData() {
        PlayerData data = new PlayerData();
        data.players = new HashMap<>();
        return data;
    }
    
    @Override
    protected void afterLoadConfig(PlayerData loadedData) {
        // Validar dados após carregar
        if (loadedData.players == null) {
            loadedData.players = new HashMap<>();
        }
        
        // Remover jogadores inativos (exemplo)
        long thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000);
        loadedData.players.entrySet().removeIf(entry -> 
            entry.getValue().lastSeen < thirtyDaysAgo
        );
    }
    
    public void updatePlayer(String uuid, PlayerInfo info) {
        this.data.players.put(uuid, info);
        this.saveConfig();
    }
    
    public PlayerInfo getPlayer(String uuid) {
        return this.data.players.get(uuid);
    }
    
    public static class PlayerData {
        private Map<String, PlayerInfo> players;
    }
    
    public static class PlayerInfo {
        private String name;
        private long lastSeen;
        private int level;
        // Getters e Setters...
    }
}
```

## Boas Práticas

### 1. ✅ Sempre salvar após modificar
```java
public void adicionarWarp(String name, Location loc) {
    this.data.warps.put(name, loc);
    this.saveConfig(); // ✅ Salva imediatamente
}
```

### 4. ✅ Use tipos imutáveis ou faça cópias defensivas
```java
public List<String> getWarps() {
    return new ArrayList<>(this.data.warps.keySet()); // ✅ Retorna cópia
}
```

### 5. ✅ Valide dados no afterLoadConfig
```java
@Override
protected void afterLoadConfig(MinhaData loadedData) {
    if (loadedData.lista == null) {
        loadedData.lista = new ArrayList<>();
    }
}
```

### 6. ✅ Use nomes de arquivo descritivos
```java
super(dataFolder, "player-homes.json", HomeData.class); // ✅ Claro
```

### 7. ✅ Documente seus métodos públicos
```java
/**
 * Adiciona uma nova warp ao sistema.
 * @param name Nome da warp (será convertido para minúsculas)
 * @param location Localização da warp
 */
public void setWarp(String name, Location location) {
    // ...
}
```

## Estrutura de Arquivos Gerada

Quando você usa `StorableManager`, os arquivos são salvos no diretório de dados do plugin:

```
plugins/
  MeuPlugin/
    config.json         ← ConfigManager
    warps.json          ← WarpManager
    playerdata.json     ← PlayerDataManager
```

Exemplo de conteúdo (warps.json):
```json
{
  "warps": {
    "spawn": {
      "world": "world",
      "x": 0.0,
      "y": 64.0,
      "z": 0.0,
      "yaw": 0.0,
      "pitch": 0.0
    },
    "shop": {
      "world": "world",
      "x": 100.0,
      "y": 65.0,
      "z": 200.0,
      "yaw": 90.0,
      "pitch": 0.0
    }
  }
}
```

## Comparação: Antes vs Depois

### ❌ Antes (sem StorableManager)

```java
public class ConfigManager {
    private final Path configFile;
    private final ConfigData configData;

    public ConfigManager(Path dataFolder) {
        this.configFile = dataFolder.resolve("config.json");
        this.configData = new ConfigData();
        this.loadConfig();
    }

    private void loadConfig() {
        if(!Files.exists(this.configFile)) {
            this.saveDefaultConfig();
            return;
        }
        try {
            String json = Files.readString(this.configFile);
            ConfigData loaded = gson.fromJson(json, ConfigData.class);
            if (loaded != null) {
                this.configData.discordLink = loaded.discordLink;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveDefaultConfig() {
        try {
            Files.createDirectories(configFile.getParent());
            Files.writeString(this.configFile, gson.toJson(configData));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public String getDiscordLink() {
        return this.configData.discordLink;
    }

    public static class ConfigData {
        private String discordLink = "";
    }
}
```

### ✅ Depois (com StorableManager)

```java
public class ConfigManager extends StorableManager<ConfigManager.ConfigData> {
    
    public ConfigManager(Path dataFolder) {
        super(dataFolder, "config.json", ConfigData.class);
    }
    
    @Override
    protected ConfigData createDefaultData() {
        return new ConfigData();
    }
    
    public String getDiscordLink() {
        return this.data.discordLink;
    }

    public static class ConfigData {
        private String discordLink = "";
    }
}
```

**Redução:** ~30 linhas → ~15 linhas (50% menos código!)

## Performance e Operações Assíncronas

### Por que saveConfig() é assíncrono?

Operações de I/O (leitura/escrita de arquivos) podem causar bloqueios na thread principal, especialmente em:
- 💾 Discos rígidos lentos (HDDs)
- 🌐 Armazenamento em rede
- 📝 Arquivos grandes
- 🔒 Sistemas de arquivos com alta latência

**Solução:** `saveConfig()` executa em background usando `CompletableFuture`, permitindo que o servidor continue processando enquanto o arquivo é salvo.

### Impacto de Performance

```java
// ❌ ANTES (síncrono) - Bloqueia a thread
public void setWarp(String name, Location loc) {
    this.data.warps.put(name, loc);
    this.saveConfigSync(); // Thread espera ~5-50ms dependendo do disco
}
// Se 100 jogadores executam isso simultaneamente = até 5 segundos de lag!

// ✅ AGORA (assíncrono) - Não bloqueia
public void setWarp(String name, Location loc) {
    this.data.warps.put(name, loc);
    this.saveConfig(); // Retorna imediatamente, salva em background
}
// 100 jogadores executam isso = zero lag, todas as operações em paralelo!
```

### Quando usar cada método?

| Método | Bloqueia? | Use quando... | Exemplo |
|--------|-----------|---------------|---------|
| `saveConfig()` | ❌ Não | Operações normais | Adicionar warp, atualizar config |
| `saveConfigSync()` | ✅ Sim | Operações críticas | Shutdown do servidor, backup |

### Exemplos Práticos

#### ✅ Operação Normal (Assíncrono)
```java
public void setDiscordLink(String link) {
    this.data.discordLink = link;
    this.saveConfig(); // Não bloqueia - perfeito para comandos
}
```

#### ✅ Com Callback
```java
public void setDiscordLink(String link, Consumer<Boolean> callback) {
    this.data.discordLink = link;
    this.saveConfig()
        .thenRun(() -> callback.accept(true))
        .exceptionally(ex -> {
            callback.accept(false);
            return null;
        });
}
```

#### ✅ Operação Crítica (Síncrono)
```java
public void onServerShutdown() {
    this.data.lastShutdown = System.currentTimeMillis();
    this.saveConfigSync(); // DEVE salvar antes de desligar
    // Servidor só desliga após salvar completamente
}
```

#### ✅ Múltiplas Alterações
```java
// Assíncrono - Uma escrita para múltiplas alterações
public void atualizarVarios(String link, String outraCfg) {
    this.data.discordLink = link;
    this.data.outraConfig = outraCfg;
    this.saveConfig(); // Salva tudo de uma vez
}
```

### Garantias de Thread-Safety

⚠️ **Importante:** Se você modificar `this.data` de múltiplas threads, considere usar sincronização:

```java
public synchronized void setWarp(String name, Location loc) {
    this.data.warps.put(name, loc);
    this.saveConfig();
}
```

Ou use estruturas thread-safe:
```java
public static class WarpData {
    // ConcurrentHashMap é thread-safe
    private Map<String, Location> warps = new ConcurrentHashMap<>();
}
```

## Conclusão

`StorableManager<T>` elimina código repetitivo e padroniza como você gerencia dados persistentes. Ao usar esta classe abstrata, você:

- ✅ Escreve menos código
- ✅ Reduz bugs
- ✅ Mantém consistência entre managers
- ✅ Facilita manutenção
- ✅ Pode focar na lógica de negócio
- ✅ **Melhora performance com I/O assíncrono**
- ✅ **Evita lag causado por operações de disco**

Para qualquer manager que precise salvar dados em JSON, basta estender `StorableManager<T>` e implementar `createDefaultData()`. O resto é automático, incluindo a otimização de performance!

