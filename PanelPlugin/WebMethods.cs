using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using AMPAPI;
using ModuleShared;
using FileManagerPlugin;
using InstanceManagerPlugin;

namespace SchematicCopyPlugin
{
    internal class WebMethods : WebMethodsBase
    {
        private readonly IFeatureManager _features;
        private readonly ILogger _log;

        public WebMethods(IFeatureManager features, ILogger log)
        {
            _features = features;
            _log = log;
        }

        public enum SchematicPluginPermissions
        {
            CopySchematicPermission
        }

        [JSONMethod(
            "Copies a schematic file from one server to another, searching first in the WorldEdit folder, then in the FastAsyncWorldEdit folder.",
            "An ActionResult dictating whether the file operation was successful.")]
        [RequiresPermissions(SchematicPluginPermissions.CopySchematicPermission)]
        public ActionResult CopySchematic(string sourceInstanceName, string destInstanceName, string schematicName, bool isModded)
        {
            LocalFileBackupPlugin.WebMethods
            var instanceManager = _features.RequestFeature<LocalInstanceManager>();
            var fileManager = (IVirtualFileService) _features.RequestFeature<IWSTransferHandler>();
            
            var sourceInstance = instanceManager.Instances
                .FirstOrDefault(i => i.InstanceName.Equals(sourceInstanceName)
                                     || i.FriendlyName.Equals(sourceInstanceName));
            if (sourceInstance == null)
            {
                return ActionResult.FailureReason("Source instance does not exist.");
            }
            
            var destInstance = instanceManager.Instances
                .FirstOrDefault(i => i.InstanceName.Equals(destInstanceName) 
                                     || i.FriendlyName.Equals(destInstanceName));
            if (destInstance == null)
            {
                return ActionResult.FailureReason("Destination instance does not exist.");
            }
            
            var platformDir = isModded ? "config" : "plugins";
            
            List<string> sources = new List<string>
            {
                $"__VDS__{sourceInstance.InstanceName}/Minecraft/{platformDir}/WorldEdit/schematics/{schematicName}"
            };
            if (!isModded)
            {
                sources.Add($"__VDS__{sourceInstance.InstanceName}/Minecraft/{platformDir}/FastAsyncWorldEdit/schematics/{schematicName}");
            }

            List<String> sourcePaths;
            if (!schematicName.Contains(".schem") && !schematicName.Contains(".schematic"))
            {
                sourcePaths = sources.Select(s => s + ".schem").ToList();
                sourcePaths.AddRange(sources.Select(s => s + ".schematic"));
            } else
            {
                sourcePaths = sources;
            }
            
            FileInfo sourceFile = null;
            foreach (var sourcePath in sourcePaths)
            {
                sourceFile = fileManager.GetFile(sourcePath);
                if (sourceFile.Exists)
                {
                    break;
                }
            }
            if (sourceFile == null || !sourceFile.Exists)
            {
                return ActionResult.FailureReason("Schematic file not found.");
            }
            
            DirectoryInfo baseDir = fileManager.GetDirectory($"__VDS__{destInstance.InstanceName}/Minecraft/{platformDir}/");
            
            ActionResult result;
            if (!fileManager.GetDirectory(baseDir.FullName).Exists)
            {
                result = fileManager.CreateDirectory(baseDir.FullName);
                if (!result.Status) return result;
            }
            
            if (!isModded && fileManager.GetDirectory(baseDir.FullName + "FastAsyncWorldEdit/schematics/").Exists)
            { 
                return fileManager.CopyFile(sourceFile.FullName, baseDir.FullName + "FastAsyncWorldEdit/schematics/");
            }
            
            if (!fileManager.GetDirectory(baseDir.FullName + "WorldEdit/").Exists)
            {
                result = fileManager.CreateDirectory(baseDir.FullName + "WorldEdit/");
                if (!result.Status) return result;
            }
            if (!fileManager.GetDirectory(baseDir.FullName + "WorldEdit/schematics/").Exists)
            {
                result = fileManager.CreateDirectory(baseDir.FullName + "WorldEdit/schematics/");
                if (!result.Status) return result;
            }
            return fileManager.CopyFile(sourceFile.FullName, baseDir.FullName + "WorldEdit/schematics/");
        }
    }
}
