using System.Net;
using System.Net.Sockets;
using System.Text;

namespace Lab4.domain;

public class CustomSocket
{
    /* ManualResetEvent
       - block and release threads manually;
       - all threads will wait until the event is signaled in which case the state will be true;
       ! reset must be done manually, otherwise the state will remain true after signaling
    */
    public readonly ManualResetEvent ConnectDone = new(false);
    public readonly ManualResetEvent SendDone = new(false);
    public readonly ManualResetEvent ReceiveDone = new(false);

    public Socket Socket = null;
    public const int Size = 1024;

    public readonly byte[] Buffer = new byte[Size];
    public readonly StringBuilder ResponseContent = new();

    public int Id;
    public string Hostname; 
    public string Endpoint;
    public IPEndPoint IpEndPoint;
}