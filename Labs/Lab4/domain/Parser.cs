namespace Lab4.domain;

internal static class Parser
{
    public const int Port = 80; // http port
    
    public static string GetRequestString(string hostname, string endpoint)
    {
        return "GET " + endpoint + " HTTP/1.1\r\n" +
               "Host: " + hostname + "\r\n" + 
               "Content-Length: 0\r\n\r\n";
    }
    
    public static int GetContentLen(string response)
    {
        var contentLength = new int();
        foreach (var line in response.Split('\r', '\n'))
        {
            var headerDetails = line.Split(':');

            if (string.Compare(headerDetails[0], "Content-Length", StringComparison.Ordinal) == 0)
            {
                contentLength = int.Parse(headerDetails[1]);
            }
        }
        return contentLength;
    }

    public static bool ResponseHeaderObtained(string responseContent)
    {
        return responseContent.Contains("\r\n\r\n");
    }
}