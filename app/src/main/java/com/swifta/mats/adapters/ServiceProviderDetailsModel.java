package com.swifta.mats.adapters;

/**
 * Created by moyinoluwa on 11/30/15.
 */
public class ServiceProviderDetailsModel {

    private final String serviceName;
    private final String serviceAmount;

    public ServiceProviderDetailsModel(String serviceName, String serviceAmount) {
        this.serviceName = serviceName;
        this.serviceAmount = serviceAmount;
    }

    public String getServiceName() {
        return this.serviceName;
    }

    public String getServiceAmount() {
        return this.serviceAmount;
    }
}
