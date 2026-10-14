namespace Osirase.Options;

public sealed class OsiraseOptions
{
    public const string SectionName = "Osirase";

    public string WatchDirectory { get; set; } = "";
    public string SentDirectory { get; set; } = "";
    public string ErrorDirectory { get; set; } = "";
    public int RetentionDays { get; set; } = 30;
    public int PollIntervalSeconds { get; set; } = 10;
    public string FilePattern { get; set; } = "*.txt";
    public MailOptions Mail { get; set; } = new();
    public SmtpOptions Smtp { get; set; } = new();
}

public sealed class MailOptions
{
    public string To { get; set; } = "";
    public string Cc { get; set; } = "";
    public string Subject { get; set; } = "お知らせ";
}

public sealed class SmtpOptions
{
    public string Host { get; set; } = "";
    public int Port { get; set; } = 587;
    public bool UseStartTls { get; set; } = true;
    public string User { get; set; } = "";
    public string Password { get; set; } = "";
    public string From { get; set; } = "";
    public string FromName { get; set; } = "";
}
