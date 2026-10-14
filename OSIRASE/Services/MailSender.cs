using MailKit.Net.Smtp;
using MailKit.Security;
using Microsoft.Extensions.Options;
using MimeKit;
using Osirase.Options;

namespace Osirase.Services;

public sealed class MailSender(IOptions<OsiraseOptions> options)
{
    private readonly OsiraseOptions _opt = options.Value;

    public async Task SendAsync(string body, CancellationToken ct)
    {
        if (string.IsNullOrWhiteSpace(_opt.Mail.To))
            throw new InvalidOperationException("Osirase:Mail:To が未設定です。");

        var message = new MimeMessage();
        message.From.Add(new MailboxAddress(_opt.Smtp.FromName, _opt.Smtp.From));
        message.To.Add(MailboxAddress.Parse(_opt.Mail.To.Trim()));
        if (!string.IsNullOrWhiteSpace(_opt.Mail.Cc))
            message.Cc.Add(MailboxAddress.Parse(_opt.Mail.Cc.Trim()));
        message.Subject = _opt.Mail.Subject;
        message.Body = new TextPart("plain") { Text = body };

        using var client = new SmtpClient();
        client.ServerCertificateValidationCallback = (sender, certificate, chain, errors) =>
            errors == System.Net.Security.SslPolicyErrors.None
            || errors == System.Net.Security.SslPolicyErrors.RemoteCertificateNameMismatch;

        await client.ConnectAsync(
            _opt.Smtp.Host,
            _opt.Smtp.Port,
            SecureSocketOptions.StartTls,
            ct);
        if (!string.IsNullOrEmpty(_opt.Smtp.User))
            await client.AuthenticateAsync(_opt.Smtp.User, _opt.Smtp.Password, ct);
        await client.SendAsync(message, ct);
        await client.DisconnectAsync(true, ct);
    }
}
