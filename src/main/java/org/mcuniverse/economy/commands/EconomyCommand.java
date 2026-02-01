package org.mcuniverse.economy.commands;

import net.minestom.server.entity.Player;
import net.minestom.server.utils.entity.EntityFinder;
import org.mcuniverse.economy.EconomyService;
import org.mcuniverse.rank.Rank;
import org.mcuniverse.rank.permission.RequiresRank;
import revxrsal.commands.annotation.*;

import java.math.BigDecimal;

public class EconomyCommand {

    private final EconomyService economyService;

    public EconomyCommand(EconomyService economyService) {
        this.economyService = economyService;
    }

    // --- [ 일반 유저 명령어: /돈 ] ---

    @Command("돈")
    @Description("경제 시스템 명령어입니다.")
    @RequiresRank(Rank.NEWBIE)
    public void onHelp(Player player) {
        player.sendMessage("재화 명령어 안내");
        player.sendMessage("/돈 잔액 - 내 잔액을 확인합니다.");
        player.sendMessage("/돈 보내기 [닉네임] [금액] - 다른 플레이어에게 돈을 보냅니다.");
    }

    @Command("돈")
    @Subcommand("잔액")
    @RequiresRank(Rank.NEWBIE)
    public void onBalance(Player player) {
        BigDecimal balance = economyService.getBalance(player.getUuid());
        player.sendMessage("잔액: " + balance + "원");
    }

    @Command("돈")
    @Subcommand("보내기")
    @RequiresRank(Rank.NEWBIE)
    public void onPay(Player player, @Named("닉네임") EntityFinder finder, @Named("금액") double amount) {
        if (amount <= 0) {
            player.sendMessage("금액은 0보다 커야 합니다.");
            return;
        }

        Player target = finder.findFirstPlayer(player);
        if (target == null) {
            player.sendMessage("해당 플레이어를 찾을 수 없습니다.");
            return;
        }

        if (target.getUuid().equals(player.getUuid())) {
            player.sendMessage("자신에게 돈을 보낼 수 없습니다.");
            return;
        }

        BigDecimal myBalance = economyService.getBalance(player.getUuid());
        BigDecimal sendAmount = BigDecimal.valueOf(amount);

        // 잔액 부족 확인 (compareTo가 0보다 작으면 sendAmount보다 적은 것)
        if (myBalance.compareTo(sendAmount) < 0) {
            player.sendMessage("잔액이 부족합니다.");
            return;
        }

        economyService.withdraw(player.getUuid(), amount);
        economyService.deposit(target.getUuid(), amount);
        
        player.sendMessage(target.getUsername() + "님에게 " + amount + "원을 보냈습니다.");
        target.sendMessage(player.getUsername() + "님으로부터 " + amount + "원을 받았습니다.");
    }

    // --- [ 관리자 명령어: /eco ] ---

    @Command("eco")
    @RequiresRank(Rank.ADMIN)
    @Subcommand({"deposit", "입금"})
    public void onDeposit(Player player, @Named("target") EntityFinder finder, double amount) {
        Player target = finder.findFirstPlayer(player);
        if (target == null) {
            player.sendMessage("해당 플레이어를 찾을 수 없습니다.");
            return;
        }
        economyService.deposit(target.getUuid(), amount);
        player.sendMessage(target.getUsername() + "님에게 " + amount + "원 입금이 완료되었습니다.");
    }

    @Command("eco")
    @RequiresRank(Rank.ADMIN)
    @Subcommand({"withdraw", "출금"})
    public void onWithdraw(Player player, @Named("target") EntityFinder finder, double amount) {
        Player target = finder.findFirstPlayer(player);
        if (target == null) {
            player.sendMessage("해당 플레이어를 찾을 수 없습니다.");
            return;
        }

        var targetBalance = economyService.getBalance(player.getUuid()).doubleValue();
        if (amount > targetBalance) {
            economyService.setBalance(target.getUuid(), 0);
            player.sendMessage(target.getUsername() + "님의 계좌를 0으로 설정 되었습니다.");
            return;
        }

        economyService.withdraw(target.getUuid(), amount);
        player.sendMessage(target.getUsername() + "님의 계좌에서 " + amount + "원 출금이 완료되었습니다.");
    }
}
