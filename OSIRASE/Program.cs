using Osirase;
using Osirase.Options;
using Osirase.Services;

var builder = Host.CreateApplicationBuilder(args);
builder.Services.AddWindowsService(o => o.ServiceName = "Osirase");
builder.Services.Configure<OsiraseOptions>(
    builder.Configuration.GetSection(OsiraseOptions.SectionName));
builder.Services.AddSingleton<SentFileCleaner>();
builder.Services.AddSingleton<MailSender>();
builder.Services.AddSingleton<MessageProcessor>();
builder.Services.AddHostedService<Worker>();

var host = builder.Build();
host.Run();
