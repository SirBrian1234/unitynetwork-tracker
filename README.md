# under dev

# Unity Network
Unity Network is a virtual network (VPN) capable to be deployed in any kind of IP network as a LAN network or over the Internet.

* It is based on a divide and conquer logic with distributed roles, behavior and decupoled network traffic from the network logic which allows it to serve a large number of host-clients from many platforms.
* It is based in software written in Java rather than hardware wich provides enchanced resilience and easy deployment, thanks to maven, to any kind of platform which may support Java.

## key-principles
The network is based in three key-principles:

* <bold>Identification</bold>: Each registered user or organisation may own multiple host-clients where each host-client may receive the same network address each time the device connects to the network
* <bold>Freedom</bold>: Each client may host any kind of service and transfer any kind of data towards any other host-client without limitations of any kind
* <bold>Security</bold>: The network provides authentication and encryption in order to defend its host-client privacy

## Reasoning
This software was build as part of my BSc Thesis in order to demonstare a live and tangible example of a better version of today's Internet. Inside the network, users may experience a much more vivid communication, the ability to share any kind of data or services between them and the ability to know each other. 

In order to learn/study my BSc Thesis please visit its main page found here:
[...under dev...]

### Some feasible examples of the network's behaviour are:
* Bob may send a file to David's Laptop
* Lucy may connect to an e-learning server
* Steve may video-call Jenny from his computer to her mobile phone
* May may leave a message to her home's noticeboard from her laptop
* Bill needs to send a file from his home computer to his computer to work

## Applications
UnityNetwork is composed by three software applications which may be found on their respective repositories:
* unitynetwork-tracker  [you are here] : The tracker is responsible to keep the network authentication and identification data but does not forward any network traffic.
* unitynetwork-bluenode [https://github.com/kostiskag/unitynetwork-bluenode] : Bluenode hosts are responsible to forward the network traffic from rednode to bluenode and from bluenode to bluenode. All the bluenodes use a tracker to authenticate.
* unitynetwork-rednode  [https://github.com/kostiskag/unitynetwork-rednode] : The rednode is the host-client application which is able to transfer a host to the network and exchange traffic towards the closest bluenode.

# unitynetwork-...


