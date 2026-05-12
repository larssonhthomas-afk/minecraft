package com.heavenlynr;

import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.UUID;

public final class HeavenlyTeamManager {

    static final String TEAM_NAME = "heavenly_h";

    private HeavenlyTeamManager() {}

    public static void init(MinecraftServer server) {
        Scoreboard scoreboard = server.getScoreboard();
        Team team = scoreboard.getTeam(TEAM_NAME);
        if (team == null) {
            team = scoreboard.addTeam(TEAM_NAME);
        }
        team.setSuffix(Text.literal(" Heavenly")
                .styled(s -> s.withColor(Formatting.GOLD).withBold(false).withItalic(false)));
        team.setNameTagVisibilityRule(AbstractTeam.VisibilityRule.ALWAYS);
    }

    public static void addPlayer(MinecraftServer server, ServerPlayerEntity player) {
        Scoreboard scoreboard = server.getScoreboard();
        Team team = scoreboard.getTeam(TEAM_NAME);
        if (team == null) return;
        scoreboard.addScoreHolderToTeam(player.getName().getString(), team);
    }

    public static void removePlayer(MinecraftServer server, ServerPlayerEntity player) {
        Scoreboard scoreboard = server.getScoreboard();
        Team team = scoreboard.getTeam(TEAM_NAME);
        if (team == null) return;
        scoreboard.removeScoreHolderFromTeam(player.getName().getString(), team);
    }

    public static void syncAll(MinecraftServer server, Iterable<UUID> holders) {
        for (UUID uuid : holders) {
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(uuid);
            if (player != null) {
                addPlayer(server, player);
            }
        }
    }
}
