package com.adambots.commands;

import com.adambots.subsystems.UptakeSubsystem;
import com.adambots.subsystems.ShooterSubsystem;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;

/**
 * Combo commands for coordinating shooter and uptake subsystems.
 */
public class ShootCommand {

    /**
     * Command to run shooter and uptake together while held.
     * All stop when the command ends.
     */
    public static Command shootWithUptake(ShooterSubsystem shooter, UptakeSubsystem uptake) {
        return Commands.parallel(
            shooter.runShooterCommand(),
            uptake.runUptakeCommand()
        ).withName("Shoot With Uptake");
    }

    /**
     * Command to reverse shooter and uptake together while held.
     * Useful for clearing jams.
     */
    public static Command reverseAll(ShooterSubsystem shooter, UptakeSubsystem uptake) {
        return Commands.parallel(
            shooter.reverseShooterCommand(),
            uptake.reverseUptakeCommand()
        ).withName("Reverse All");
    }

    /**
     * Command to stop shooter and uptake (instant).
     */
    public static Command stopAll(ShooterSubsystem shooter, UptakeSubsystem uptake) {
        return Commands.parallel(
            shooter.stopShooterCommand(),
            uptake.stopUptakeCommand()
        ).withName("Stop All");
    }
}
