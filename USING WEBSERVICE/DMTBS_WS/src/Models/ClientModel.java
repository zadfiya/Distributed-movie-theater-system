package Models;

import Assets.StringAssets;

public class ClientModel {
    private String clientServer;
    private String clientType;
    private String clientId;

    public ClientModel(String clientId) {
        this.clientId = clientId;
        this.clientType = findClientType();
        this.clientServer = findClientServer();
    }
    private String findClientServer() {
        String serverSubstring = clientId.substring(0,3);
        if(serverSubstring.equals("ATW")){
            return StringAssets.ATWATER_SERVER;
        }else if(serverSubstring.equals("VER")){
            return StringAssets.VERDUN_SERVER;
        }else{
            return StringAssets.OUTREMONT_SERVER;
        }
    }
    private String findClientType() {
        return clientId.charAt(3)=='A'? StringAssets.ADMIN_USER:StringAssets.CUSTOMER_USER;
    }

    public String getClientServer() {
        return clientServer;
    }

    public void setClientServer(String clientServer) {
        this.clientServer = clientServer;
    }

    public String getClientType() {
        return clientType;
    }

    public void setClientType(String clientType) {
        this.clientType = clientType;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    @Override
    public String toString() {
        return "Accessing: "+clientServer+" com.Server By Client:"+clientType+" with ID:"+clientId;
    }
}
