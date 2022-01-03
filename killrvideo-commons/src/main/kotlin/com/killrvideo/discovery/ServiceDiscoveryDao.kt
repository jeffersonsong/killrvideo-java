package com.killrvideo.discovery

/**
 * Work with service registry (ETCD, Consul..)
 *
 * @author Cedrick LUNVEN (@clunven)
 */
interface ServiceDiscoveryDao {
    /**
     * Register new endpoint for a service.
     * @param serviceName
     * unique service identifier
     * @param hostName
     * current hostname
     * @param portNumber
     * current port number
     * @return
     * service key (service name + namespace)
     */
    fun register(serviceName: String, hostName: String, portNumber: Int): String

    /**
     * List endpoints available for a service.
     *
     * @param serviceName
     * service identifier
     * @return
     * list of endpoints like hostname1:port1, hostname2:port2
     */
    fun lookup(serviceName: String): List<String>

    /**
     * Unregister all endpoints for a service.
     *
     * @param serviceName
     * service unique identifier
     */
    fun unregister(serviceName: String)

    /**
     * Unregister one endpoint for a service.
     *
     * @param serviceName
     * service unique identifier
     * @param hostName
     * current hostname
     * @param portNumber
     * current port number
     */
    fun unregisterEndpoint(serviceName: String, hostName: String, portNumber: Int)
}
