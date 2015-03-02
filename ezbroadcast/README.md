This repo contains the ezbroadcast core components as well as the redismq and kafka implementations.
A web module is also available to facilitate broadcasts to topics via a REST interface.

#### BUILD

To build the redismq implementation, run mvm with a '-P redismq' option.
For kafka implementation, run it with '-P kafka' option.


#### REST INTERFACE

The war from ezbroadcast-web module can be deployed readily in ezcentos' JBoss web server.
Once deployed, the rest interface can be accessed via the url -
`http://192.168.50.105:8080/ezbroadcast/api/topics/{topic_name}`

*{topic_name}* is the name of the topic you wish to broadcast to.

This should be a POST request, with multiple parts to it, 2 to be precise. 
Make sure to setup this header-- `Content-Type: multipart/form-data`
The first part called `visibility` will be a JSON representation of the Visibility Thrift struct from ezbake-base-thrift.
For e.g.
`{"formalVisibility":"FOUO","optionals":["FORMAL_VISIBILITY","ADVANCED_MARKINGS"]}`

The other part is the `payload`, which is the binary data to be broadcast.

When the request is submitted, the payload data will be broadcast to the 
topic specified in the url.
