package cn.xiaym.modupdater;

import cn.xiaym.modupdater.commands.Command;
import cn.xiaym.modupdater.data.Profile;
import cn.xiaym.modupdater.utils.JSONFormatter;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import org.json.JSONObject;
import sun.misc.Signal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.UUID;
import java.util.stream.Stream;

public class Main {
    public static final Path mainDirectory = Paths.get("modupdater");
    public static final Path profilesDirectory = mainDirectory.resolve("profiles");
    public static final Path configurationFile = mainDirectory.resolve("config.json");
    private static final Scanner scanner = new Scanner(System.in);
    public static JSONObject config;
    public static Profile currentProfile;

    public static void main(String[] args) {
        AnsiConsole.systemInstall();
        Signal.handle(new Signal("INT"), sig -> {
            System.out.println(Ansi.ansi().reset());
            System.exit(0);
        });

        try {
            initDataFolders();
        } catch (IOException e) {
            System.err.println("FATAL: Unable to create data directories / files;");
            System.err.println("FATAL: " + e.getMessage());
            System.exit(1);
        }

        try {
            reloadConfig();
        } catch (IOException e) {
            System.err.println("FATAL: Unable to read the configuration file;");
            System.err.println("FATAL: " + e.getMessage());
            System.exit(1);
        }

        try (Stream<Path> listed = Files.list(profilesDirectory)) {
            if (listed.findAny().isEmpty()) {
                // First launch
                System.out.println("""
                        Welcome there!
                        This program is designed for Fabric users to update mods more easily.
                        ** Other mod loaders are not supported yet. **
                                                
                        Before starting, creating a profile is required.
                        """);

                if (!createProfileAsking()) {
                    System.exit(1);
                }

                saveConfig();
                System.exit(0);
            }
        } catch (IOException e) {
            System.err.println("FATAL: An unexpected error occurred;");
            System.err.println("FATAL: " + e.getMessage());
            System.exit(1);
        }

        if (args.length == 0) {
            mainNoArgs();
            return;
        }

        mainWithArgs(args);
    }

    public static void mainNoArgs() {
        loadProfile();
        System.out.println("Loaded profile: " + config.getString("selected_profile"));
        String alias = currentProfile.alias();

        System.out.println("\n====== Profile Information ======");
        System.out.println(Ansi.ansi()
                .fgBrightBlue().a("Current profile's alias: ")
                .fgBright(Ansi.Color.WHITE).a(alias == null ? "<No Alias>" : alias));
        System.out.println(Ansi.ansi()
                .fgBrightBlue().a("Current profile's Minecraft version is: ")
                .fgBright(Ansi.Color.WHITE).a(currentProfile.gameVersion()));
        System.out.println(Ansi.ansi()
                .fgBrightBlue().a("Current profile has ")
                .fgBright(Ansi.Color.WHITE).a(currentProfile.mods().size())
                .fgBrightBlue().a(" mods.")
                .reset());

        System.out.println("\nFor more help, please run the \"help\" subcommand.");
    }

    public static void mainWithArgs(String[] args) {
        String commandName = args[0];
        if (!commandName.equals("profiles")) {
            loadProfile();
        }

        Command command = Registry.COMMANDS.getOrDefault(commandName, null);
        if (command == null) {
            System.err.println("Cannot find task or subcommand: " + commandName);
            System.err.println("Please run the \"help\" subcommand for help.");
            System.exit(1);
        }

        long timer = System.currentTimeMillis();
        command.onCommand(stripArgs(args));

        long diff = System.currentTimeMillis() - timer;
        System.out.println(Ansi.ansi()
                .fgBright(Ansi.Color.GREEN).a("Task or Subcommand ")
                .fgBright(Ansi.Color.WHITE).bold().a(commandName)
                .reset()
                .fgBright(Ansi.Color.GREEN).a(" FINISHED in " + diff / 1000 + "s " + diff % 1000 + "ms")
                .reset());
    }

    public static void initDataFolders() throws IOException {
        if (Files.notExists(profilesDirectory)) {
            Files.createDirectories(profilesDirectory);
        }

        if (Files.notExists(configurationFile)) {
            // Write default config
            JSONObject defaultConfig = new JSONObject();
            defaultConfig.put(Registry.KEY_CURSEFORGE_APIKEY, "");

            Files.writeString(configurationFile, JSONFormatter.format(defaultConfig.toString(), 4), StandardOpenOption.CREATE);
        }
    }

    public static void reloadConfig() throws IOException {
        String data = Files.readString(configurationFile);
        config = new JSONObject(data);
    }

    public static void saveConfig() {
        try {
            Files.writeString(configurationFile, JSONFormatter.format(config.toString(), 4));
        } catch (IOException e) {
            System.err.println("ERROR: Unable to save the configuration file;");
            System.err.println("ERROR: " + e.getMessage());
        }
    }

    public static void loadProfile() {
        try {
            String data = Files.readString(profilesDirectory.resolve(config.getString("selected_profile") + ".json"));
            currentProfile = Profile.deserialize(new JSONObject(data));
        } catch (Exception e) {
            System.err.println("FATAL: An unexpected error occurred while reading the profile;");
            System.err.println("FATAL: " + e.getMessage());
            System.err.println("Please try selecting / creating a profile by the \"profiles\" subcommand.");
            System.exit(1);
        }
    }

    public static void saveCurrentProfile() {
        try {
            Path path = profilesDirectory.resolve(config.getString("selected_profile") + ".json");
            Files.writeString(path, currentProfile.serialize().toString());
        } catch (Exception e) {
            System.err.println("ERROR: Unable to save the profile file;");
            System.err.println("ERROR: " + e.getMessage());
        }
    }

    public static boolean createProfileAsking() {
        System.out.println(Ansi.ansi().fgBright(Ansi.Color.WHITE).bold().a("Create a new Profile").reset());

        // Read mods dir
        String dir = readLine("Mods Directory> ");
        Path path = Paths.get(dir).toAbsolutePath();
        if (Files.notExists(path)) {
            System.err.println("FATAL: The mods directory is not exists");
            return false;
        }

        if (!Files.isDirectory(path)) {
            System.err.println("FATAL: The mods directory you typed is not a directory");
            return false;
        }

        // Read game version
        String gameVersion = readLine("Minecraft Version (1.20.1, 22w11a, etc.)> ");

        // Alias
        String alias = readLine("Alias (Optional)> ", false);

        UUID uuid = UUID.randomUUID();
        Profile profile = new Profile(path, gameVersion, new ArrayList<>(), alias.isEmpty() ? null : alias);

        config.put("selected_profile", uuid.toString());

        try {
            Files.writeString(profilesDirectory.resolve(uuid + ".json"), profile.serialize().toString(), StandardOpenOption.CREATE);
        } catch (IOException e) {
            System.err.println("FATAL: Unable to create profile file at modupdater/profiles/" + uuid + ".json;");
            System.err.println("FATAL: " + e.getMessage());
            return false;
        }

        System.out.println("\nCongratulations! You have created a profile and its uuid is: " + uuid);
        System.out.println("You can run the task \"addLocalMods\" and \"linkMods\" later.");
        return true;
    }

    public static String readLine(String prompt) {
        return readLine(prompt, true);
    }

    public static String readLine(String prompt, boolean repeatWhenEmpty) {
        try {
            String line;
            do {
                System.out.print(prompt + Ansi.ansi().fgBrightBlue());
                line = scanner.nextLine();
                System.out.print(Ansi.ansi().reset());
            } while (repeatWhenEmpty && line.isEmpty());

            return line.trim();
        } catch (Exception ex) {
            System.exit(1);
        }

        return "";
    }

    public static String[] stripArgs(String[] raw) {
        String[] args = new String[raw.length - 1];
        System.arraycopy(raw, 1, args, 0, args.length);
        return args;
    }
}
