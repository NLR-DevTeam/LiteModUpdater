package cn.xiaym.modupdater.commands;

import cn.xiaym.modupdater.Main;
import cn.xiaym.modupdater.data.Profile;
import org.fusesource.jansi.Ansi;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class CommandProfiles implements Command {
    public static void printHelp() {
        System.out.println("""
                Showing the help for subcommand: profiles
                Usage: profiles <operation> [args]
                                
                Available operations:
                 create\t\t - Create a profile by following a guide
                 remove <uuid>\t - Remove a profile
                 select <uuid>\t - Select a profile
                 clone <uuid>\t - Clone a profile
                 info\t\t - Show a profile's information
                 list\t\t - List created profiles
                 modify <...>\t - Modify a profile's config""");
    }

    public static Profile deserializeProfile(Path path) {
        if (Files.notExists(path)) {
            return null;
        }

        try {
            String data = Files.readString(path);
            JSONObject obj = new JSONObject(data);
            return Profile.deserialize(obj);
        } catch (Exception ignored) {
            // Do nothing
        }

        return null;
    }

    public static String getIdByFile(Path path) {
        String fn = path.getFileName().toString();
        return fn.split("\\.")[0];
    }

    public static Profile checkProfileOnlyArgs(String[] args, String cmd, boolean fallbackCurrent) {
        if (args.length == 1) {
            if (fallbackCurrent) {
                return deserializeProfile(Main.profilesDirectory
                        .resolve(Main.config.getString("selected_profile") + ".json"));
            }

            System.err.println("Usage: profiles " + cmd + " <uuid>");
            return null;
        }

        String profile = args[1];
        Profile prof = deserializeProfile(Main.profilesDirectory.resolve(profile + ".json"));
        if (prof == null) {
            System.err.println("That profile (" + profile + ") is invalid.");
            return null;
        }

        return prof;
    }

    @Override
    public void onCommand(String[] args) {
        if (args.length == 0) {
            printHelp();
            return;
        }

        switch (args[0]) {
            case "create", "new" -> Main.createProfileAsking();

            case "remove", "rm" -> {
                if (checkProfileOnlyArgs(args, "remove", false) == null) {
                    return;
                }

                Path path = Main.profilesDirectory.resolve(args[1] + ".json");
                try {
                    Files.delete(path);
                    System.out.println("Removed successfully.");

                    if (Main.config.getString("selected_profile").equals(getIdByFile(path))) {
                        System.out.println("\nWarning: You have deleted a profile that was selected.");
                        System.out.println("Warning: You should create or select a profile to make ModUpdater usable.\n");
                    }
                } catch (IOException ex) {
                    System.err.println("FATAL: Remove failed;");
                    System.err.println("FATAL: " + ex.getMessage());
                }
            }

            case "select", "sel" -> {
                if (checkProfileOnlyArgs(args, "select", false) == null) {
                    return;
                }

                Main.config.put("selected_profile", args[1]);
                Main.saveConfig();

                System.out.println("OK");
            }

            case "clone", "copy" -> {
                if (checkProfileOnlyArgs(args, "clone", true) == null) {
                    return;
                }

                UUID uuid = UUID.randomUUID();
                Path oldPath = Main.profilesDirectory.resolve(args[1] + ".json");
                Path newPath = Main.profilesDirectory.resolve(uuid + ".json");

                try {
                    Files.copy(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("Cloned successfully, the new profile's uuid is: " + uuid);
                } catch (IOException ex) {
                    System.err.println("FATAL: Clone failed;");
                    System.err.println("FATAL: " + ex.getMessage());
                }
            }

            case "info", "show" -> {
                Profile profile = checkProfileOnlyArgs(args, "info", true);
                if (profile == null) {
                    return;
                }

                String alias = profile.alias();
                System.out.println("====== Profile's Information ======");
                System.out.println("Alias: " + (alias == null ? "<No Alias>" : alias));
                System.out.println("Directory: " + profile.path().toAbsolutePath());
                System.out.println("Game Version: " + profile.gameVersion());
                System.out.println("Added Mods: " + profile.mods().size());
            }

            case "list" -> {
                String selected = Main.config.getString("selected_profile");
                System.out.println("Listing created profiles...\n");

                try (Stream<Path> listed = Files.list(Main.profilesDirectory)) {
                    List<Path> list = listed.toList();
                    for (Path path : list) {
                        String id = getIdByFile(path);
                        Profile profile = deserializeProfile(path);
                        String alias = profile == null ? null : profile.alias();

                        System.out.println(Ansi.ansi()
                                .a(alias == null ? "Profile" : alias).a(": ")
                                .fgBright(profile == null ? Ansi.Color.RED : Ansi.Color.GREEN).a(id).reset()
                                .fgBrightBlue().a(profile != null ? (" [" + profile.gameVersion() + "]") : "").reset()
                                .a(selected.equals(id) ? " (Selected)" : ""));
                    }
                } catch (IOException ex) {
                    System.err.println("FATAL: An exception was thrown while listing profiles;");
                    System.err.println("FATAL: " + ex.getMessage());
                }
            }

            case "modify", "set" -> {
                if (args.length < 4) {
                    System.out.println("""
                            Modifying a profile's config
                            Usage: modify <uuid> <type> <value>
                                                        
                            Available types:
                            ver - Minecraft Version
                            dir - Mods Directory
                            alias - Profile alias
                                                        
                            If your value argument contains space, you should surround it with quotes.""");
                    return;
                }

                String profileName = args[1];
                Path path = Main.profilesDirectory.resolve(profileName + ".json");
                Profile profile = deserializeProfile(path);
                if (profile == null) {
                    System.err.println("That profile (" + profileName + ") is invalid.");
                    return;
                }

                switch (args[2]) {
                    case "ver", "version" -> profile = new Profile(profile.path(), args[3], profile.mods(), profile.alias());

                    case "dir", "directory" -> {
                        Path dir = Paths.get(args[3]).toAbsolutePath();
                        if (Files.notExists(dir)) {
                            System.err.println("FATAL: The mods directory is not exists");
                            return;
                        }

                        if (!Files.isDirectory(dir)) {
                            System.err.println("FATAL: The mods directory you typed is not a directory");
                            return;
                        }

                        profile = new Profile(dir, profile.gameVersion(), profile.mods(), profile.alias());
                    }

                    case "alias", "name" -> profile = new Profile(profile.path(), profile.gameVersion(), profile.mods(), args[3]);

                    default -> {
                        System.out.println("FATAL: Unknown config type: " + args[2]);
                        return;
                    }
                }

                try {
                    Files.writeString(path, profile.serialize().toString(), StandardOpenOption.CREATE);
                    System.out.println("OK");
                } catch (IOException ex) {
                    System.err.println("FATAL: Failed to save profile file;");
                    System.err.println("FATAL: " + ex.getMessage());
                }
            }

            default -> printHelp();
        }
    }
}
