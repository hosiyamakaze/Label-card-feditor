using Microsoft.Extensions.Options;
using Osirase.Options;

namespace Osirase.Services;

public sealed class MessageProcessor(
    IOptions<OsiraseOptions> options,
    MailSender mail,
    ILogger<MessageProcessor> logger)
{
    private readonly OsiraseOptions _opt = options.Value;

    public async Task ProcessAsync(string path, CancellationToken ct)
    {
        var body = (await ReadSharedAsync(path, ct)).Trim();
        if (body.Length == 0)
            throw new InvalidDataException("本文が空です: " + path);

        await mail.SendAsync(body, ct);
        Move(_opt.SentDirectory, path);
        logger.LogInformation("送信済み: {Path} -> {To}", path, _opt.Mail.To);
    }

    public void MoveToError(string path)
    {
        try
        {
            Move(_opt.ErrorDirectory, path);
        }
        catch (Exception ex)
        {
            logger.LogError(ex, "エラーフォルダへの移動に失敗: {Path}", path);
        }
    }

    private static async Task<string> ReadSharedAsync(string path, CancellationToken ct)
    {
        await using var stream = new FileStream(path, FileMode.Open, FileAccess.Read, FileShare.ReadWrite);
        using var reader = new StreamReader(stream);
        return await reader.ReadToEndAsync(ct);
    }

    private static void Move(string dir, string path)
    {
        Directory.CreateDirectory(dir);
        var name = Path.GetFileName(path);
        var dest = Path.Combine(dir, $"{DateTime.Now:yyyyMMddHHmmssfff}_{name}");
        File.Move(path, dest, overwrite: false);
    }
}
