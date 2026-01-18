package dev.thewarrior.i18n;

import com.hypixel.hytale.server.core.Message;

public class Messages {
    public static final Message WORLD_NOT_FOUND = Message.raw("[ERRO] O mundo especificado não foi encontrado.");

    public static final Message PLAYER_NOT_FOUND = Message.raw("[ERRO] Esse usuário não foi encontrado ou é inválido.");

    public static final Message CANNOT_TELL_YOURSELF = Message.raw("[AVISO] Você não pode enviar uma mensagem privada para si mesmo!");

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

    public static final Message TELEPORT_FAILED_PLAYER_MOVED = Message.raw("[ERRO] Falha ao teleportar: você se moveu");

    public static final Message COMMAND_WARP_SUCCESS = Message.raw("Teleporte para a warp realizado com sucesso!");
}
