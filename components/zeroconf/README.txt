zeroconf
---------------
Zero configuration networking is good for environments where administration is impractical or impossible.It is intended for small networks, ad-hoc networks or situations where devices need to communicate
with each other over a direct connection.There is no provision forrouting traffic in and out of the Zeroconf network.The beauty of Zeroconf Networking is that the protocols it defines are small
extensions of existing network protocols, and they coexist peacefully with their centrally configured brethren. Also, the Zeroconf protocols do not depend on each other: one can function in the absence of others.

component
-------------
Here server registration happens using zeroconf protocol. Zeroconf
enables the automatic configuration of network services, through the use
of multicast on local networks.

So when we register  a carbon server with JmDNS( dns server , zeroconf
java implementation) ,  the services which are deployed in that
particular server can be accessible via the  network.

Steps
-----
1. Keep the BE bundle in any carbon server.(it will register the carbon server with DNS server).
2. Keep the UI bundle where you want to access the server/services (eg:-BAM). (need to add a JSP, the deafult one tested with G-Reg 3.0.0 )

Note:- Need improvements.
1. add it as server handler(like in ws-discovery)

