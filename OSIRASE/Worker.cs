using Microsoft.Extensions.Options;
using Osirase.Options;
using Osirase.Services;

namespace Osirase;

public sealed class Worker(
    IOptions<OsiraseOptions> options,
    SentFileCleaner cleaner,
    MessageProcessor processor,
    ILogger<Worker> logger) : BackgroundService
{
    private readonly OsiraseOptions _opt = options.Value;
    private DateOnly _lastCleanup = DateOnly.MinValue;

    protected override async Task ExecuteAsync(CancellationToken stoppingToken)
    {
        Directory.CreateDirectory(_opt.WatchDirectory);
        Directory.CreateDirectory(_opt.SentDirectory);
        Directory.CreateDirectory(_opt.ErrorDirectory);
        logger.LogInformation(
            "監視開始: {Dir} To={To} Cc={Cc} 保持日数={Days}",
            _opt.WatchDirectory, _opt.Mail.To, _opt.Mail.Cc, _opt.RetentionDays);

        while (!stoppingToken.IsCancellationRequested)
        {
            try
            {
                CleanupIfDue();
                foreach (var path in ListReadyFiles())
                {
                    try
                    {
                        await processor.ProcessAsync(path, stoppingToken);
                    }
                    catch (Exception ex)
                    {
                        logger.LogError(ex, "送信失敗: {Path}", path);
                        processor.MoveToError(path);
                    }
                }
            }
            catch (Exception ex) when (ex is not OperationCanceledException)
            {
                logger.LogError(ex, "監視周期でエラー");
            }

            await Task.Delay(TimeSpan.FromSeconds(Math.Max(1, _opt.PollIntervalSeconds)), stoppingToken);
        }
    }

    private void CleanupIfDue()
    {
        var today = DateOnly.FromDateTime(DateTime.Now);
        if (_lastCleanup == today) return;
        cleaner.DeleteExpired();
        _lastCleanup = today;
    }

    private IEnumerable<string> ListReadyFiles()
    {
        return Directory.EnumerateFiles(_opt.WatchDirectory, _opt.FilePattern)
            .Where(path =>
            {
                var name = Path.GetFileName(path);
                return name.Length > 0
                    && name[0] != '_'
                    && name[0] != '.';
            })
            .OrderBy(path => File.GetLastWriteTimeUtc(path));
    }
}
