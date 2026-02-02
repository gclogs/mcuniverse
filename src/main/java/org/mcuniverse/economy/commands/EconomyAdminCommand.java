package org.mcuniverse.economy.commands;

import net.minestom.server.entity.Player;
import net.minestom.server.utils.entity.EntityFinder;
import org.mcuniverse.economy.EconomyAccount;
import org.mcuniverse.economy.EconomyService;
import org.mcuniverse.rank.Rank;
import org.mcuniverse.rank.permission.RequiresRank;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Named;
import revxrsal.commands.annotation.Subcommand;

public class EconomyAdminCommand {

    private final EconomyService economyService;

    public EconomyAdminCommand(EconomyService economyService) {
        this.economyService = economyService;
    }

    // --- [ 관리자 명령어: /eco ] ---

    @Command("eco")
    @RequiresRank(Rank.ADMIN)
    @Subcommand({"deposit", "입금"})
    public void onDeposit(Player player, @Named("target") EntityFinder finder, long amount) {
        Player target = finder.findFirstPlayer(player);
        if (target == null) {
            player.sendMessage("해당 플레이어를 찾을 수 없습니다.");
            return;
        }
        economyService.deposit(target.getUuid(), EconomyAccount.BALANCE, amount);
        player.sendMessage(target.getUsername() + "님에게 " + amount + "원 입금이 완료되었습니다.");
    }

    @Command("eco")
    @RequiresRank(Rank.ADMIN)
    @Subcommand({"withdraw", "출금"})
    public void onWithdraw(Player player, @Named("target") EntityFinder finder, long amount) {
        Player target = finder.findFirstPlayer(player);
        if (target == null) {
            player.sendMessage("해당 플레이어를 찾을 수 없습니다.");
            return;
        }

        long targetBalance = economyService.getAccount(target.getUuid(), EconomyAccount.BALANCE);
        if (amount > targetBalance) {
            economyService.setAccount(player.getUuid(), EconomyAccount.BALANCE, amount);
            player.sendMessage(target.getUsername() + "님의 계좌를 0으로 설정 되었습니다.");
            return;
        }

        economyService.withdraw(target.getUuid(), EconomyAccount.BALANCE, amount);
        player.sendMessage(target.getUsername() + "님의 계좌에서 " + amount + "원 출금이 완료되었습니다.");
    }
}
