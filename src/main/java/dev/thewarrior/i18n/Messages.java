package dev.thewarrior.i18n;

import com.hypixel.hytale.server.core.Message;

public class Messages {
    public static final Message WORLD_NOT_FOUND = Message.raw("[ERRO] O mundo especificado não foi encontrado.");

    public static final Message PLAYER_NOT_FOUND = Message.raw("[ERRO] Esse usuário não foi encontrado ou é inválido.");

    public static final Message CANNOT_TELL_YOURSELF = Message.raw("[AVISO] Você não pode enviar uma mensagem privada para si mesmo!");

    public static final Message CANNOT_TELEPORT_YOURSELF = Message.raw("[AVISO] Você não pode teleportar a si mesmo!");

    public static final Message COMMAND_DISCORD_LINK = Message.raw("> Entre no nosso Discord clicando aqui.");

    public static final Message COMMAND_TELL_OFF_SUCCESS = Message.raw("As mensagens privadas foram desativadas com sucesso!");

    public static final Message COMMAND_TELL_ALREADY_OFF = Message.raw("[AVISO] As mensagens privadas já estão desativadas!");

    public static final Message COMMAND_TELL_ON_SUCCESS = Message.raw("As mensagens privadas foram ativadas com sucesso!");

    public static final Message COMMAND_TELL_ALREADY_ON = Message.raw("[AVISO] As mensagens privadas já estão ativadas!");

    public static final Message PRIMARY_TITLE_ON_LOGIN = Message.raw("Seja bem-vindo!");

    public static final Message SECOND_TITLE_ON_LOGIN = Message.raw("Orion Network");

    public static final Message COMMAND_DISCORD_INVALID_LINK = Message.raw("[ERRO] O link do Discord fornecido é inválido. Certifique-se de que começa com 'https://'.");

    public static final Message COMMAND_DISCORD_LINK_UPDATED = Message.raw("O link do Discord foi atualizado com sucesso.");

    public static final Message CANNOT_GET_OWN_PLAYER_POSITION = Message.raw("[ERRO] Não foi possível obter a sua posição atual.");

    public static final Message CANNOT_GET_TARGET_PLAYER_POSITION = Message.raw("[ERRO] Não foi possível obter a posição do jogador.");

    public static final String COMMAND_SETWARP_SUCCESS = "O warp %s foi definido com sucesso no mundo %s!";

    public static final String COMMAND_DELWARP_NOT_FOUND = "A warp '%s' não foi encontrada.";

    public static final String COMMAND_DELWARP_SUCCESS = "A warp '%s' foi removida com sucesso.";

    public static final Message COMMAND_WARP_NOT_FOUND = Message.raw("A warp '%s' não foi encontrada.");

    public static final Message TELEPORT_ALREADY_PENDING = Message.raw("[AVISO] Você já possui um teleporte pendente.");

    public static final String TELEPORTING = "Aguarde, você será teleportado em %d segundos. Não se mova!";

    public static final Message TELEPORT_FAILED_REFERENCE_INVALID = Message.raw("[ERRO] Falha ao teleportar: referência do jogador inválida.");

    public static final Message TELEPORT_FAILED_PLAYER_OFFLINE = Message.raw("[ERRO] Falha ao teleportar: o jogador não está online");

    public static final Message TELEPORT_FAILED_PLAYER_NOT_AVAILABLE = Message.raw("[ERRO] Falha ao teleportar: o jogador não está disponível para teleporte.");

    public static final Message TELEPORT_FAILED_EXCEPTION = Message.raw("[ERRO] Falha ao teleportar: ocorreu um erro inesperado.");

    public static final Message TELEPORT_FAILED_PLAYER_MOVED = Message.raw("[AVISO] Falha ao teleportar: você se moveu");

    public static final Message COMMAND_GENERIC_TELEPORT_SUCCESS = Message.raw("Você foi teleportado com sucesso!");

    public static final Message COMMAND_WARPS_EMPTY = Message.raw("O servidor ainda não possui warps definidas.");

    public static final Message COMMAND_WARPS_TITLE = Message.raw("\nWarps disponíveis: \n");

    public static final Message COMMAND_SPAWN_NOT_SET = Message.raw("[AVISO] O spawn do servidor ainda não foi definido.");

    public static final Message COMMAND_SPAWN_SUCCESS = Message.raw("Você teleportou para o spawn.");

    public static final Message COMMAND_SET_SPAWN_SUCCESS = Message.raw("O local de spawn do servidor foi definido com sucesso!");

    public static final String COMMAND_SET_HOME_SUCCESS = "A home '%s' foi definida com sucesso!";

    public static final String COMMAND_DEL_HOME_SUCCESS = "A home '%s' foi deletada com sucesso!";

    public static final Message HOME_NAME_INVALID = Message.raw("[ERRO] O nome da home é inválido. Ele deve conter entre 1 e 16 caracteres e somente caracteres alfanuméricos.");

    public static final Message CANNOT_GET_OWN_PLAYER_DATA = Message.raw("[ERRO] Não foi possível obter os dados do jogador.");

    public static final Message COMMAND_SET_HOME_FAILED = Message.raw("[ERRO] Falha ao definir a home.");

    public static final String COMMAND_HOME_NOT_EXISTS = "[AVISO] Falha no comando: a home '%s' não foi encontrada.";

    public static final Message COMMAND_HOME_TITLE = Message.raw("\nHomes disponíveis: \n");

    public static final String COMMAND_TP_HERE_SUCCESS = "Você teleportou '%s' para você.";

    public static final String COMMAND_TP_HERE_TARGET_SUCCESS = "Você foi puxado por '%s'.";

    public static final String TELEPORT_REQUEST_EXPIRED = "Seu pedido de teleporte para '%s' expirou.";

    public static final String COMMAND_TPA_FAILED = "[AVISO] Você já possui um pedido pendente para '%s'.";

    public static final String COMMAND_TPA_SUCCESS = "Você enviou um pedido de teleporte para '%s'.";

    public static final Message COMMAND_TPA_TARGET_SUCCESS = Message.raw("quer se teleportar para você! \n");

    public static final Message COMMAND_FREE_CAMERA_ENABLED = Message.raw("Modo de câmera livre ativado com sucesso! Para desativar, use o comando novamente.");

    public static final Message COMMAND_FREE_CAMERA_DISABLED = Message.raw("Modo de câmera livre desativado com sucesso!");

    public static final Message COMMAND_TPA_NO_PENDING_REQUESTS = Message.raw("[AVISO] Você não possui pedidos de teleporte pendentes.");

    public static final String COMMAND_TPA_REQUEST_ACCEPTED = "Pedido de teleporte de '%s' aceito com sucesso!";

    public static final String COMMAND_TPA_REQUEST_TARGET_ACCEPTED = "O usuário '%s' aceitou seu pedido de teleporte!";
}
