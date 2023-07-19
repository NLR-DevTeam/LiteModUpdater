package cn.xiaym.modupdater.commands;

import cn.xiaym.modupdater.Main;

public class CommandProfiles implements Command {
    public static void printHelp() {
        System.out.println("""
                Showing the help for subcommand: profiles
                Usage: profiles <operation> [args]
                                
                Available operations:
                 create\t\t - Create a profile by following a guide
                 remove <uuid>\t - Remove a profile
                 select <uuid>\t - Select a profile
                 list\t\t - List created profiles""");
    }

    @Override
    public void onCommand(String[] args) {
        if (args.length == 0) {
            printHelp();
            return;
        }

        switch (args[0]) {
            case "create" -> Main.createProfileAsking();
            case "remove" -> {
            }
            case "select" -> {
            }
            case "list" -> {
            }
            default -> printHelp();
        }
    }
}
