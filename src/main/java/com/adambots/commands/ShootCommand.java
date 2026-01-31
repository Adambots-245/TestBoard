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
     * Command to run shooter, uptake, and carousel together while held.
     * All stop when the command ends.
     */
    public static Command shootWithHopper(ShooterSubsystem shooter, HopperSubsystem hopper) {
        return Commands.parallel(
            shooter.runShooterCommand(),
            hopper.runHopperCommand()
        ).withName("Shoot With Hopper");
    }

    /**
     * Command to reverse shooter, uptake, and carousel together while held.
     * Useful for clearing jams.
     */
    public static Command reverseAll(ShooterSubsystem shooter, HopperSubsystem hopper) {
        return Commands.parallel(
            shooter.reverseShooterCommand(),
            hopper.reverseHopperCommand()
        ).withName("Reverse All");
    }

    /**
     * Command to stop shooter, uptake, and carousel (instant).
     */
    public static Command stopAll(ShooterSubsystem shooter, HopperSubsystem hopper) {
        return Commands.parallel(
            shooter.stopShooterCommand(),
            hopper.stopHopperCommand()
        ).withName("Stop All");
    }
}
