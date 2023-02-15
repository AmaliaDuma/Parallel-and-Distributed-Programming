using System.Diagnostics;
using System.Net;
using System.Net.Sockets;
using System.Text;
using Lab4.domain;

namespace Lab4.implementations;

public class TaskAsyncImpls
{
    private static List<string> _hosts;
    
    public static void Run(List<string> hostnames)
    {
        _hosts = hostnames;
        var tasks = new List<Task>();

        for (var i = 0; i < hostnames.Count; i++)
        {
            tasks.Add(Task.Factory.StartNew(Start, i));   
        }

        Task.WaitAll(tasks.ToArray());
    }
    
    private static void Start(object idObject)
    {
        var id = (int)idObject;

        StartClient(_hosts[id], id);
    }
    
    private static void StartClient(string host, int id)
    {
        // Dns.GetHostEntry - resolves an host name or ip address to IpHostEntry instance from where we take the ip address
        var ipHostEntry = Dns.GetHostEntry(host.Split('/')[0]);
        
        // We take the address so we can establish an endpoint with that address and the port number
        var ipAddress = ipHostEntry.AddressList[0];
        
        // Endpoint - address:port 
        var endPoint = new IPEndPoint(ipAddress, Parser.Port);
        
        // Create a new socket using the ip address family, socket type and a protocol
        var clientSocket = new Socket(ipAddress.AddressFamily, SocketType.Stream, ProtocolType.Tcp);
        var requestSocket = new CustomSocket
        {
            Socket = clientSocket,
            Hostname = host.Split('/')[0],
            Endpoint = host.Contains('/') ? host[host.IndexOf("/", StringComparison.Ordinal)..] : "/",
            IpEndPoint = endPoint,
            Id = id
        };
        
        // Connect
        Connect(requestSocket).Wait();
            
        // Request data
        Send(requestSocket, Parser.GetRequestString(requestSocket.Hostname, requestSocket.Endpoint)).Wait();
            
        // Receive response
        Receive(requestSocket).Wait(); 

        Console.WriteLine("Connection {0} > Content length is:{1}", requestSocket.Id, Parser.GetContentLen(requestSocket.ResponseContent.ToString()));
        Console.WriteLine("Content - {0}", requestSocket.ResponseContent);

        clientSocket.Shutdown(SocketShutdown.Both);
        clientSocket.Close();
    }
    
    private static async Task Connect(CustomSocket state)
    {
        state.Socket.BeginConnect(state.IpEndPoint, ConnectCallback, state);

        // Blocks the current thread until the current WaitHandle receives a signal.
        await Task.FromResult<object>(state.ConnectDone.WaitOne()); 
    }
    
    private static void ConnectCallback(IAsyncResult ar)
    {
        var resultSocket = (CustomSocket)ar.AsyncState;
        Debug.Assert(resultSocket != null, nameof(resultSocket) + " != null");
        var clientSocket = resultSocket.Socket;
        var clientId = resultSocket.Id;
        var hostname = resultSocket.Hostname;

        clientSocket.EndConnect(ar);
        Console.WriteLine("Connection {0} > Socket connected to {1} ({2})", clientId, hostname, clientSocket.RemoteEndPoint);

        // Set the state of the event to signaled, allowing other threads to proceed.
        resultSocket.ConnectDone.Set();
    }
    
    private static async Task Send(CustomSocket state, string data)
    {
        var byteData = Encoding.ASCII.GetBytes(data);

        // Request data
        state.Socket.BeginSend(byteData, 0, byteData.Length, 0, SendCallback, state);
        await Task.FromResult<object>(state.SendDone.WaitOne());
    }
    
    private static void SendCallback(IAsyncResult ar)
    {
        var resultSocket = (CustomSocket)ar.AsyncState;
        Debug.Assert(resultSocket != null, nameof(resultSocket) + " != null");
        var clientSocket = resultSocket.Socket;
        var clientId = resultSocket.Id;

        // Ends a pending asynchronous send
        var bytesSent = clientSocket.EndSend(ar);
        Console.WriteLine("Connection {0} > Sent {1} bytes to server.", clientId, bytesSent);

        // Set the state of the event to signaled, allowing other waiting threads to proceed.
        resultSocket.SendDone.Set();
    }
    
    private static async Task Receive(CustomSocket state)
    {
        // Receive data
        state.Socket.BeginReceive(state.Buffer, 0, CustomSocket.Size, 0, ReceiveCallback, state);

        await Task.FromResult<object>(state.ReceiveDone.WaitOne());
    }
    
    private static void ReceiveCallback(IAsyncResult ar)
    {
        var resultSocket = (CustomSocket)ar.AsyncState;
        Debug.Assert(resultSocket != null, nameof(resultSocket) + " != null");
        var clientSocket = resultSocket.Socket;

        try
        {
            // Read data 
            var bytesRead = clientSocket.EndReceive(ar);

            // Read from the buffer, a number of characters <= to the buffer size, and store it in the responseContent
            resultSocket.ResponseContent.Append(Encoding.ASCII.GetString(resultSocket.Buffer, 0, bytesRead));

            // If the response header has not been fully obtained, get the next chunk of data
            if (!Parser.ResponseHeaderObtained(resultSocket.ResponseContent.ToString()))
            {
                clientSocket.BeginReceive(resultSocket.Buffer, 0, CustomSocket.Size, 0, ReceiveCallback, resultSocket);
            }
            else
            {
                // Signal that all bytes have been received  
                resultSocket.ReceiveDone.Set();      
            }
        }
        catch (Exception e)
        {
            Console.WriteLine(e.ToString());
        }

    }
}