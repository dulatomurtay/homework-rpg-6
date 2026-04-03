package com.narxoz.rpg.tournament;

import com.narxoz.rpg.arena.ArenaFighter;
import com.narxoz.rpg.arena.ArenaOpponent;
import com.narxoz.rpg.arena.TournamentResult;
import com.narxoz.rpg.chain.ArmorHandler;
import com.narxoz.rpg.chain.BlockHandler;
import com.narxoz.rpg.chain.DefenseHandler;
import com.narxoz.rpg.chain.DodgeHandler;
import com.narxoz.rpg.chain.HpHandler;
import com.narxoz.rpg.command.ActionQueue;
import com.narxoz.rpg.command.AttackCommand;
import com.narxoz.rpg.command.DefendCommand;
import com.narxoz.rpg.command.HealCommand;
import java.util.Random;

public class TournamentEngine {
    private final ArenaFighter hero;
    private final ArenaOpponent opponent;
    private Random random = new Random(1L);

    public TournamentEngine(ArenaFighter hero, ArenaOpponent opponent) {
        this.hero = hero;
        this.opponent = opponent;
    }

    public TournamentEngine setRandomSeed(long seed) {
        this.random = new Random(seed);
        return this;
    }

    public TournamentResult runTournament() {
        TournamentResult result = new TournamentResult();
        int round = 0;
        final int maxRounds = 20;
        ActionQueue actionQueue = new ActionQueue();

        while (hero.isAlive() && opponent.isAlive() && round < maxRounds) {
            round++;
            AttackCommand attackCommand = new AttackCommand(opponent, hero.getAttackPower());
            HealCommand healCommand = new HealCommand(hero, 20);
            DefendCommand defendCommand = new DefendCommand(hero, 0.10);

            actionQueue.enqueue(attackCommand);
            actionQueue.enqueue(healCommand);
            actionQueue.enqueue(defendCommand);

            result.addLine("[Round " + round + "] Queue: " + actionQueue.getCommandDescriptions());
            actionQueue.executeAll();

            if (opponent.isAlive()) {
                DefenseHandler dodge = new DodgeHandler(hero.getDodgeChance(), random.nextLong());
                DefenseHandler block = new BlockHandler(hero.getBlockRating() / 100.0);
                DefenseHandler armor = new ArmorHandler(hero.getArmorValue());
                DefenseHandler hp = new HpHandler();
                dodge.setNext(block).setNext(armor).setNext(hp);

                dodge.handle(opponent.getAttackPower(), hero);
            }
            defendCommand.undo();

            result.addLine("[Round " + round + "] Opponent HP: " + opponent.getHealth()
                    + " | Hero HP: " + hero.getHealth());
        }

        result.setWinner(hero.isAlive() ? hero.getName() : opponent.getName());
        result.setRounds(round);
        return result;
    }
}