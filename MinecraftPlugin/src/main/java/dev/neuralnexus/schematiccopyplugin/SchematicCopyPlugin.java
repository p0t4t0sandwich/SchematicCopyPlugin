package dev.neuralnexus.schematiccopyplugin;

import dev.neuralnexus.ampapi.auth.AuthProvider;
import dev.neuralnexus.ampapi.auth.AuthStore;
import dev.neuralnexus.ampapi.auth.BasicAuthProvider;
import dev.neuralnexus.ampapi.exceptions.AMPAPIException;
import dev.neuralnexus.ampapi.modules.ADS;

import dev.neuralnexus.ampapi.types.ActionResult;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class SchematicCopyPlugin extends JavaPlugin {
    private AuthProvider authProvider;
    private static Logger logger;

    public static Logger logger() {
        return logger;
    }

    @Override
    public void onEnable() {
        logger = this.getLogger();
        this.authProvider = new BasicAuthProvider.Builder().build();
        this.getCommand("schemcopy")
                .setExecutor(new SchematicCopyCommand(this.authProvider));
    }

    @Override
    public void onDisable() {
        this.authProvider = null;
    }

    public static class SchematicCopyCommand implements CommandExecutor, TabCompleter {
        private static ADS API;

        public SchematicCopyCommand(AuthProvider authProvider) {
            AuthStore authStore = new AuthStore();
            authProvider.Login();
            authStore.add(authProvider);
            API = new ADS(authStore, authProvider);
        }

        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
            // SchematicCopyPlugin.CopySchematic(
            //     sourceInstanceName: String,
            //     destInstanceName: String,
            //     schematicName: String,
            //     isModded: Boolean
            // ) -> ActionResult
            if (args.length < 3) {
                sender.sendMessage("Usage: /schemcopy <schematicName> <sourceInstanceName> <destInstanceName>");
                return true;
            }
            Map<String, Object> data = new HashMap<>();
            data.put("sourceInstanceName", args[1]);
            data.put("destInstanceName", args[2]);
            data.put("schematicName", args[0]);
            data.put("isModded", false);
            ActionResult<?> result;
            try {
                result = API.APICall("SchematicCopyPlugin/CopySchematic", data, ActionResult.class);
            } catch (AMPAPIException e) {
                sender.sendMessage("&cFailed to copy schematic, see console for details.");
                sender.sendMessage("&6(is there a typo in the schematic or instance names?)");
                SchematicCopyPlugin.logger().warning(e.getMessage());
                return true;
            }
            if (result != null && result.Status) {
                sender.sendMessage("&aCopied " + args[0] + " from " + args[1] + " to " + args[2]);
            } else {
                sender.sendMessage("&cFailed to copy schematic");
            }
            return true;
        }

        @Override
        public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
            try {
                if (args.length < 3) {
                    return API.ADSModule.GetInstances(false).stream()
                            .flatMap(ads -> ads.AvailableInstances.stream())
                            .map(i -> i.InstanceName)
                            .collect(Collectors.toList());
                }
            } catch (AMPAPIException e) {
                sender.sendMessage("&cFailed to get list of instances, see console for details");
                SchematicCopyPlugin.logger().warning(e.getMessage());
            }
            return null;
        }
    }
}
