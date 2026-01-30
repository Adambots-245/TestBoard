package com.adambots.commands;

import com.adambots.subsystems.HopperSubsystem;
import com.adambots.subsystems.ShooterSubsystem;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;

/**
 * Combo commands for coordinating shooter and hopper subsystems.
 */
public class ShootCommand {

    /**
     * Command to run both shooter and hopper together while held.
     * Both stop when the command ends.
     */
    public static Command shootWithHopper(ShooterSubsystem shooter, HopperSubsystem hopper) {
        return Commands.parallel(
            shooter.runShooterCommand(),
            hopper.runUptakeCommand()
        ).withName("Shoot With Hopper");
    }

    /**
     * Command to reverse both shooter and hopper together while held.
     * Useful for clearing jams.
     */
    public static Command reverseAll(ShooterSubsystem shooter, HopperSubsystem hopper) {
        return Commands.parallel(
            shooter.reverseShooterCommand(),
            hopper.reverseUptakeCommand()
        ).withName("Reverse All");
    }

    /**
     * Command to stop both shooter and hopper (instant).
     */
    public static Command stopAll(ShooterSubsystem shooter, HopperSubsystem hopper) {
        return Commands.parallel(
            shooter.stopShooterCommand(),
            hopper.stopUptakeCommand()
        ).withName("Stop All");
    }
}
