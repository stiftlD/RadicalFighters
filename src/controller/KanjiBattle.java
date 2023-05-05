package controller;

import java.util.*;
import java.util.concurrent.TimeUnit;

import controller.Controller;
import model.radicals.RadicalFighter;

import view.BattleWindow; //TODO access via controller

public class KanjiBattle extends Observable {
    private RadicalFighter[] team1;
    private RadicalFighter[] team2;
    private int currentTurn;

    private BattleWindow battleWindow;

    private List<Observer> observers;

    public KanjiBattle(RadicalFighter[] team1, RadicalFighter[] team2) {
        this.team1 = team1;
        this.team2 = team2;
        this.currentTurn = 0;
        this.battleWindow = new BattleWindow();
    }

    public void start() {
        battleWindow.run();
        while (!isBattleOver()) {
            System.out.println("----- Turn " + (currentTurn + 1) + " -----");
            performTurn();
            currentTurn++;
        }
    }

    private void performTurn() {
        RadicalFighter attacker = getNextAttacker();
        RadicalFighter defender = getNextDefender(attacker);
        Attack attack = attacker.selectAttack();
        Defense defense = defender.selectDefense();
        int damage = attack.calculateDamage(defense);
        boolean successfulDefense = defense.isSuccessful();
        int damageDealt = successfulDefense ? damage * 2 : damage;
        defender.takeDamage(damageDealt);
        showAttackResult(attacker, defender, attack, defense, damageDealt);
    }

    private RadicalFighter getNextAttacker() {
        return currentTurn % 2 == 0 ? team1[currentTurn / 2] : team2[currentTurn / 2];
    }

    private RadicalFighter getNextDefender(RadicalFighter attacker) {
        RadicalFighter[] defenders = attacker == team1[currentTurn / 2] ? team2 : team1;
        return defenders[currentTurn / 2];
    }

    public void printBattleState() {
        System.out.println("Current Battle State:");
        System.out.println("Team 1:");
        for (int i = 0; i < 3; i++) {
            RadicalFighter fighter = team1[i];
            System.out.println(fighter.getName() + " (HP: " + fighter.getHP() + ")");
        }
        System.out.println("Team 2:");
        for (int i = 0; i < 3; i++) {
            RadicalFighter fighter = team2[i];
            System.out.println(fighter.getName() + " (HP: " + fighter.getHP() + ")");
        }
        System.out.println();
    }

    private boolean isBattleOver() {
        for (RadicalFighter fighter : team1) {
            if (fighter.getHP() > 0) {
                boolean allAlliesDefeated = true;
                for (RadicalFighter opponent : team2) {
                    if (opponent.getHP() > 0) {
                        allAlliesDefeated = false;
                        break;
                    }
                }
                if (allAlliesDefeated) {
                    System.out.println("Team 1 wins!");
                    return true;
                } else {
                    break;
                }
            }
        }
        for (RadicalFighter fighter : team2) {
            if (fighter.getHP() > 0) {
                boolean allOpponentsDefeated = true;
                for (RadicalFighter opponent : team1) {
                    if (opponent.getHP() > 0) {
                        allOpponentsDefeated = false;
                        break;
                    }
                }
                if (allOpponentsDefeated) {
                    System.out.println("Team 2 wins!");
                    return true;
                } else {
                    break;
                }
            }
        }
        return false;
    }

    // Implementation of Observable interface

    @Override
    public void registerObserver(Observer observer) {
        observers.add(observer);
    }

    @Override
    public void unregisterObserver(Observer observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers() {
        for (Observer observer : observers) {
            observer.update(this, null);
        }
    }


    public int getOpponentHealth() {
        return team1[0].getHP();
    }

    public int getPlayerHealth() {
        return team2[0].getHP();
    }
}
