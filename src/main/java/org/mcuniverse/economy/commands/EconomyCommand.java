package org.mcuniverse.economy.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.command.builder.arguments.number.ArgumentDouble;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.entity.EntityFinder;
import org.mcuniverse.economy.EconomyService;

public class EconomyCommand extends Command {

    private final EconomyService economyService;

    public EconomyCommand(EconomyService economyService) {
        super("eco", "돈");
        this.economyService = economyService;

        setDefaultExecutor((sender, context) -> {
            if (sender instanceof Player player) {
                // BigDecimal을 문자열과 결합하여 String으로 변환
                player.sendMessage("현재 잔액: " + economyService.getBalance(player.getUuid()));
            }
        });

        // 인자 정의: 동작, 대상 플레이어, 금액
        ArgumentWord actionArg = ArgumentType.Word("action").from("입금", "차감", "출금");
        ArgumentEntity playerArg = ArgumentType.Entity("target").onlyPlayers(true).singleEntity(true);
        ArgumentDouble amountArg = ArgumentType.Double("amount");

        // 구문 등록: /돈 <동작> <플레이어> <금액>
        addSyntax((sender, context) -> {
            String action = context.get(actionArg);
            EntityFinder finder = context.get(playerArg);
            Player target = finder.findFirstPlayer(sender);
            double amount = context.get(amountArg);

            if (target == null) {
                sender.sendMessage("해당 플레이어를 찾을 수 없습니다.");
                return;
            }

            if (amount <= 0) {
                sender.sendMessage("금액은 0보다 커야 합니다.");
                return;
            }

            switch (action) {
                case "입금" -> {
                    economyService.deposit(target.getUuid(), amount);
                    sender.sendMessage(target.getUsername() + "님에게 " + amount + "원을 입금했습니다.");
                    target.sendMessage(amount + "원이 입금되었습니다.");
                }
                case "차감", "출금" -> {
                    if (economyService.withdraw(target.getUuid(), amount)) {
                        sender.sendMessage(target.getUsername() + "님의 계좌에서 " + amount + "원을 차감했습니다.");
                        target.sendMessage(amount + "원이 차감되었습니다.");
                    } else {
                        sender.sendMessage(target.getUsername() + "님의 잔액이 부족합니다.");
                    }
                }
            }
            // 관리자나 실행자에게 대상의 최종 잔액 표시
            sender.sendMessage(target.getUsername() + "님의 현재 잔액: " + economyService.getBalance(target.getUuid()));
        }, actionArg, playerArg, amountArg);
    }
}
