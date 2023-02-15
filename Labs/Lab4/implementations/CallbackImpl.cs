using System.Net;
using System.Net.Sockets;
using System.Text;
using Lab4.domain;

namespace Lab4.implementations;

public static class CallbackImpl
{
    public static void Run(List<string> hostnames)
    {
        for (var i = 0; i < hostnames.Count; i++)
        {
            StartClient(hostnames[i], i);
            Thread.Sleep(1000);
        }
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
        
        /* Connect to the endpoint
             BeginConnect - begins an async request for a remote host connection
         */
        requestSocket.Socket.BeginConnect(requestSocket.IpEndPoint, Connected, requestSocket);
    }

    private static void Connected(IAsyncResult ar)
    {
        var resultSocket = (CustomSocket) ar.AsyncState;
        if (resultSocket == null) return;

        var clientSocket = resultSocket.Socket;
        var clientId = resultSocket.Id;
        
        // Complete the connection
        clientSocket.EndConnect(ar);
        Console.WriteLine("Connection {0} - Socket Connected", clientId);
        
        var byteData =
            Encoding.ASCII.GetBytes(Parser.GetRequestString(resultSocket.Hostname, resultSocket.Endpoint));

        // Sends data asynchronously to a connected socket
        resultSocket.Socket.BeginSend(byteData, 0, byteData.Length, 0, Sent, resultSocket);
    }
    
    private static void Sent(IAsyncResult ar) {
        var resultSocket = (CustomSocket) ar.AsyncState;
        
        if (resultSocket == null) return; // null check
        var clientSocket = resultSocket.Socket;
        var clientId = resultSocket.Id;

        // Complete the sending
        var bytesSent = clientSocket.EndSend(ar);
        Console.WriteLine("Connection {0} - Sent {1} bytes to server.", clientId, bytesSent);

        // Server response (data)
        resultSocket.Socket.BeginReceive(resultSocket.Buffer, 0, CustomSocket.Size, 0, Receiving, resultSocket);
    }
    
    private static void Receiving(IAsyncResult ar)
    {
        var resultSocket = (CustomSocket) ar.AsyncState;
            
        if (resultSocket == null) return;
        var clientSocket = resultSocket.Socket;

        try
        {
            // Complete the receiving and returns the no of bytes read
            var bytesRead = clientSocket.EndReceive(ar);

            resultSocket.ResponseContent.Append(Encoding.ASCII.GetString(resultSocket.Buffer, 0, bytesRead));

            // If the response header has not been fully obtained, get the next chunk of data
            if (!Parser.ResponseHeaderObtained(resultSocket.ResponseContent.ToString()))
            {
                clientSocket.BeginReceive(resultSocket.Buffer, 0, CustomSocket.Size, 0, Receiving,
                    resultSocket);
            }
            else
            {
                Console.WriteLine("Connection {0} > Content length is:{1}", resultSocket.Id, Parser.GetContentLen(resultSocket.ResponseContent.ToString()));
                Console.WriteLine("Content is - {0}", resultSocket.ResponseContent);
                    
                // Disables sends and receives on clientSocket
                clientSocket.Shutdown(SocketShutdown.Both); 
                
                // Close clientSocket connection and releases all resources
                clientSocket.Close(); 
            }
        }
        catch (Exception e)
        {
            Console.WriteLine(e.ToString());
        }
    }
}