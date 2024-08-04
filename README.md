<img src="https://static.nlrdev.top/product-icons/litemodupdater.webp" align="right" width="100px">

# LiteModUpdater

A Simple Tool to Update Your Fabric Mods for Minecraft.

# 🚩 Installation

- Download the latest release from the [Releases](https://github.com/NLR-DevTeam/LiteModUpdater/releases/) page.
- Launch the `Terminal` and type `java -jar LiteModUpdater-X.X.X.jar` to start the tool.
  
  **Warning:** Make sure to replace the filename with the actual name of the file you downloaded. You’ll need Java 17 to run this tool.
- When you run the tool for the first time, enter the directory where your mods are located and the version of your game to create a profile.
- Enter an optional alias for the profile, then go ahead and create it. Once created, you’ll receive a unique UUID for the profile.
- Then you can run the tasks `addLocalMods` or `linkMods` whenever you’re ready.

# 🚄 Usage

### Basic Usage

```powershell
java -jar LiteModUpdater.jar [command] [...subcommand or options]
```

### Commands

|Command Name|Description|
|--|--|
|`addLocalMods`|Adds new local mods to the current profile.|
|`autoRemove`|Automatically removes mods that are no more exist on the disk.|
|`help`|Show the help document.|
|`linkMods`|Automatically links local mods as online mods (Supports Modrinth & CurseForge).|
|`listMods`|Lists current profile's mods.|
|`profiles`|Manages profiles.|
|`searchMod`|Searches a mod on Modrinth or CurseForge through CLI.|
|`updateMods`|Automatically detects and updates the current profile's mods.|

### About CurseForge API Key

CurseForge's API requires an API key to access, and we don't provide it.<br>
You have to request an API key if you want to access CurseForge's services (e.g. Search API)<br>
Link: https://console.curseforge.com/

# ⭐ Feedback

If you have any suggestions, don’t hesitate to [submit a new issue](https://github.com/NLR-DevTeam/LiteModUpdater/issues) to us.

For mainland China users, you can also [join our QQ chat-group](https://join.nlrdev.top) and have a chat with us. 

We’d love to hear from you!
