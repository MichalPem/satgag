# satgag

https://satgag.site

Meme sharing site which uses satosis over lighting network as revad and for spam prevention. Uses lighting addresses for deposits and widrawals. Inspired by stacker.news and 9gag. Under development 

# Instalation

```
cd src/client; 
npm i;
cd ../../
./build.sh
```

# Startup

```
java -jar target/satgag-0.0.1-SNAPSHOT.jar --lnhost=<lndServerIp> --backend=<nameOfPage eg: satgag.site> --lnport=<lndGprcPort> --server.port=<portToUse> --certpath=<pathTo>/tls.cert --macaroonpath=<pathTo> --spring.datasource.url=jdbc:mysql://<dbUrl:port>/<dbName> --spring.datasource.username=<dbUserName> --spring.datasource.password=<db password>
```
