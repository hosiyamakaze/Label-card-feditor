using Microsoft.Extensions.Options;
using Osirase.Options;

namespace Osirase.Services;

public sealed class SentFileCleaner(IOptions<OsiraseOptions> options, ILogger<SentFileCleaner> logger)
{
    private readonly OsiraseOptions _opt = options.Value;

    public void DeleteExpired()
    {
        Directory.CreateDirectory(_opt.SentDirectory);
        var limit = DateTime.Now.AddDays(-Math.Max(0, _opt.RetentionDays));
        var deleted = 0;
        foreach (var path in Directory.EnumerateFiles(_opt.SentDirectory))
        {
            var info = new FileInfo(path);
            if (info.LastWriteTime >= limit) continue;
            info.Delete();
            deleted++;
            logger.LogInformation("保持期限超過のため削除: {Path}", path);
        }
        logger.LogInformation("送信済み削除: {Count} 件 (RetentionDays={Days})", deleted, _opt.RetentionDays);
    }
}
