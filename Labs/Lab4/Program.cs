using Lab4.implementations;

namespace Lab4
{
    internal static class Program
    {
        private static void Main(string[] args)
        {
            var hosts = new[] { "www.cs.ubbcluj.ro/~rlupsa/edu/pdp/", "www.cs.ubbcluj.ro/~dadi/compnet/labs/lab1/"}.ToList();
            CallbackImpl.Run(hosts);
            //TaskImpl.Run(hosts);
            //TaskAsyncImpls.Run(hosts);
        }
    }    
}
