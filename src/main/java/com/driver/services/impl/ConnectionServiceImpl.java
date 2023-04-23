package com.driver.services.impl;

import com.driver.model.*;
import com.driver.repository.ConnectionRepository;
import com.driver.repository.ServiceProviderRepository;
import com.driver.repository.UserRepository;
import com.driver.services.ConnectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConnectionServiceImpl implements ConnectionService {
    @Autowired
    UserRepository userRepository2;
    @Autowired
    ServiceProviderRepository serviceProviderRepository2;
    @Autowired
    ConnectionRepository connectionRepository2;

    @Override
    public User connect(int userId, String countryName) throws Exception{
        //Connect the user to a vpn by considering the following priority order.
        //1. If the user is already connected to any service provider, throw "Already connected" exception.
        //2. Else if the countryName corresponds to the original country of the user, do nothing. This means that the user wants to connect to its original country, for which we do not require a connection. Thus, return the user as it is.
        //3. Else, the user should be subscribed under a serviceProvider having option to connect to the given country.
        //If the connection can not be made (As user does not have a serviceProvider or serviceProvider does not have given country, throw "Unable to connect" exception.
        //Else, establish the connection where the maskedIp is "updatedCountryCode.serviceProviderId.userId" and return the updated user. If multiple service providers allow you to connect to the country, use the service provider having smallest id.

        User user = userRepository2.findById(userId).get();
        if (user.getConnected()){
            throw new Exception("Already connected");
        }



        Country country = new Country();
        String countryName1 = countryName.toUpperCase();
        if (!countryName1.equals("IND") && !countryName1.equals("USA") && !countryName1.equals("CHI") && !countryName1.equals("JPN")) throw new Exception("Country not found");

        if (user.getOriginalCountry().getCountryName().equals(country.getCountryName())){
            return user;
        }

        List<ServiceProvider> serviceProviderList = user.getServiceProviderList();
        ServiceProvider serviceProviderWithLowestId = null;
        Integer lowestId = null;
        for (ServiceProvider serviceProvider : serviceProviderList){
            List<Country> countryList = serviceProvider.getCountryList();
            for (Country country1 : countryList){
                if (country1.getCode().equals(country.getCode())){
                    if (serviceProviderWithLowestId == null || serviceProvider.getId()<lowestId){
                        serviceProviderWithLowestId = serviceProvider;
                        lowestId = serviceProviderWithLowestId.getId();
                    }
                }
            }
        }

        if (serviceProviderWithLowestId == null){
            throw new Exception("Unable to connect");
        }


        Connection connection = new Connection();
        connection.setUser(user);
        user.setConnected(true);
        List<Connection> connectionList = user.getConnectionList();
        connectionList.add(connection);
        user.setConnectionList(connectionList);
        user.setMaskedIp(new String(country.getCode() + "." + serviceProviderWithLowestId + userId));
        connection.setServiceProvider(serviceProviderWithLowestId);
        List<Connection> serviceProviderConnectionList = serviceProviderWithLowestId.getConnectionList();
        serviceProviderConnectionList.add(connection);
        serviceProviderWithLowestId.setConnectionList(serviceProviderConnectionList);
        userRepository2.save(user);
        serviceProviderRepository2.save(serviceProviderWithLowestId);
        return user;
    }
    @Override
    public User disconnect(int userId) throws Exception {
        //If the given user was not connected to a vpn, throw "Already disconnected" exception.
        //Else, disconnect from vpn, make masked Ip as null, update relevant attributes and return updated user.
        User user = userRepository2.findById(userId).get();
        if (!user.getConnected()){
            throw new Exception("Already disconnected");
        }
        user.setConnected(false);
        user.setMaskedIp(null);
        userRepository2.save(user);
        return user;
    }
    @Override
    public User communicate(int senderId, int receiverId) throws Exception {

        User sender = userRepository2.findById(senderId).get();
        User receiver = userRepository2.findById(receiverId).get();
        if (receiver.getMaskedIp()!=null){
            String maskedIp = receiver.getMaskedIp();
            String code = maskedIp.substring(0,3);
            code = code.toUpperCase();
            if (code.equals(sender.getOriginalCountry().getCode())) return sender;
            String countryName = "";
            CountryName[] countryNames = CountryName.values();
            for(CountryName countryName1 : countryNames){
                if (countryName1.toCode().toString().equals(code)){
                    countryName = countryName1.toString();
                }
            }
            try {
                sender = connect(senderId,countryName);
            }catch (Exception e){
                throw new Exception("Cannot establish communication");
            }
            if (!sender.getConnected()){
                throw new Exception("Cannot establish communication");
            }
            return sender;
        }
        if (sender.getOriginalCountry().equals(receiver.getOriginalCountry())){
            return sender;
        }
        String countryName = receiver.getOriginalCountry().getCountryName().toString();
        try {
            sender = connect(senderId,countryName);
        }catch (Exception e){
            if (!sender.getConnected()) throw new Exception("Cannot establish communication");
        }
        return sender;




    }
}




//package com.driver.services.impl;
//
//import com.driver.model.*;
//import com.driver.repository.ConnectionRepository;
//import com.driver.repository.ServiceProviderRepository;
//import com.driver.repository.UserRepository;
//import com.driver.services.ConnectionService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
//@Service
//public class ConnectionServiceImpl implements ConnectionService {
//    @Autowired
//    UserRepository userRepository2;
//    @Autowired
//    ServiceProviderRepository serviceProviderRepository2;
//    @Autowired
//    ConnectionRepository connectionRepository2;
//
//    @Override
//    public User connect(int userId, String countryName) throws Exception {
//        User user = userRepository2.findById(userId).get();
//        if (user.getMaskedIp() != null) throw new Exception("Already connected");
//        if (countryName.equalsIgnoreCase(user.getOriginalCountry().getCountryName().toString())) return user;
//        if (user.getServiceProviderList() == null) {
//            throw new Exception("Unable to connect");
//        }
//        List<ServiceProvider> serviceProviderList = user.getServiceProviderList();
//        int a = Integer.MAX_VALUE;
//        ServiceProvider serviceProvider = null;
//        Country country = null;
//        for (ServiceProvider serviceProvider1 : serviceProviderList) {
//            List<Country> countryList = serviceProvider1.getCountryList();
//            for (Country country1 : countryList) {
//                if (countryName.equalsIgnoreCase(country1.getCountryName().toString()) && a > serviceProvider1.getId()) {
//                    a = serviceProvider1.getId();
//                    serviceProvider = serviceProvider1;
//                    country = country1;
//                }
//            }
//        }
//        if (serviceProvider != null) {
//            Connection connection = new Connection();
//            connection.setUser(user);
//            connection.setServiceProvider(serviceProvider);
//            user.setMaskedIp(country.getCode() + "." + serviceProvider.getId() + "." + userId);
//            user.setConnected(true);
//            user.getConnectionList().add(connection);
//            serviceProvider.getConnectionList().add(connection);
//            userRepository2.save(user);
//            serviceProviderRepository2.save(serviceProvider);
//        }
//        else throw new Exception("Unable to connect");
//        return user;
//    }
//
//    @Override
//    public User disconnect(int userId) throws Exception {
//        User user = userRepository2.findById(userId).get();
//        if (user.getConnected() == false) {
//            throw new Exception("Already disconnected");
//        }
//        user.setMaskedIp(null);
//        user.setConnected(false);
//        userRepository2.save(user);
//        return user;
//    }
//
//    @Override
//    public User communicate(int senderId, int receiverId) throws Exception {
//
//
//        //Establish a connection between sender and receiver users
//        //To communicate to the receiver, sender should be in the current country of the receiver.
//        //If the receiver is connected to a vpn, his current country is the one he is connected to.
//        //If the receiver is not connected to vpn, his current country is his original country.
//        //The sender is initially not connected to any vpn. If the sender's original country does not match receiver's current country, we need to connect the sender to a suitable vpn. If there are multiple options, connect using the service provider having smallest id
//        //If the sender's original country matches receiver's current country, we do not need to do anything as they can communicate. Return the sender as it is.
//        //If communication can not be established due to any reason, throw "Cannot establish communication" exception
//
//        User sender = userRepository2.findById(senderId).get();
//        User receiver = userRepository2.findById(receiverId).get();
//        if (receiver.getMaskedIp()!=null){
//            String maskedIp = receiver.getMaskedIp();
//            String code = maskedIp.substring(0,3);
//            code = code.toUpperCase();
//            if (code.equals(sender.getOriginalCountry().getCode())) return sender;
//            String countryName = "";
//            CountryName[] countryNames = CountryName.values();
//            for(CountryName countryName1 : countryNames){
//                if (countryName1.toCode().toString().equals(code)){
//                    countryName = countryName1.toString();
//                }
//            }
//            try {
//                sender = connect(senderId,countryName);
//            }catch (Exception e){
//                throw new Exception("Cannot establish communication");
//            }
//            if (!sender.getConnected()){
//                throw new Exception("Cannot establish communication");
//            }
//            return sender;
//        }
//        if (sender.getOriginalCountry().equals(receiver.getOriginalCountry())){
//            return sender;
//        }
//        String countryName = receiver.getOriginalCountry().getCountryName().toString();
//        try {
//            sender = connect(senderId,countryName);
//        }catch (Exception e){
//            if (!sender.getConnected()) throw new Exception("Cannot establish communication");
//        }
//        return sender;
//    }
//}