package com.mikejuliet.authservice.entities;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ResourceServerManager {
    private static final Map<String, String> resourceServers = new ConcurrentHashMap<>();

    // Initialize the map with predefined values
    static {
        // Add predefined resource servers here
        addResourceServer(UUID.randomUUID().toString(),"user-service" );

    }

    // Method to add a resource server
    public static void addResourceServer(String resourceId,String resourceName) {
        resourceServers.put(resourceId, resourceName);
    }


    // Method to retrieve the name of a resource server using its ID
    public  String getResourceServerName(String resourceId) {
        return resourceServers.get(resourceId);
    }
    // Method to retrieve the name of a resource server using its name
    public  String getResourceServerNameByName(String resourceName) {
        for (Map.Entry<String, String> entry : resourceServers.entrySet()) {
            if (entry.getValue().equals(resourceName)) {
                return entry.getKey();
            }
        }
        return null; // Resource server name not found
    }
    // Method to retrieve the ID of a resource server using its name
    public String getResourceServerIdByName(String resourceName) {
        for (Map.Entry<String, String> entry : resourceServers.entrySet()) {
            if (entry.getValue().equals(resourceName)) {
                return entry.getKey();
            }
        }
        return null; // Resource server name not found
    }
    // Method to remove a resource server
    public static void removeResourceServer(String resourceId) {
        resourceServers.remove(resourceId);
    }
}
