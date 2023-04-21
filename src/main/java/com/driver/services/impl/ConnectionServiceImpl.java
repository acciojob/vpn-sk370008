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
        countryName = countryName.toUpperCase();
        if (countryName.equals("IND")){
            country.setCountryName(CountryName.IND);
            country.setCode(CountryName.IND.toCode());
        }else if (countryName.equals("USA")){
            country.setCountryName(CountryName.USA);
            country.setCode(CountryName.USA.toCode());
        }else if(countryName.equals("AUS")){/*chi,jap*/
            country.setCountryName(CountryName.AUS);
            country.setCode(CountryName.AUS.toCode());
        }else if (countryName.equals("CHI")){
            country.setCountryName(CountryName.CHI);
            country.setCode(CountryName.CHI.toCode());
        }else if (countryName.equals("JPN")){
            country.setCountryName(CountryName.JPN);
            country.setCode(CountryName.JPN.toCode());
        }else {
            throw new Exception("Country not found");
        }

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





        User user = userRepository2.findById(senderId).get();
        User user1 = userRepository2.findById(receiverId).get();

        if(user1.getMaskedIp()!=null){
            String str = user1.getMaskedIp();
            String cc = str.substring(0,3); //chopping country code = cc

            if(cc.equals(user.getOriginalCountry().getCode()))
                return user;
            else {
                String countryName = "";

                if (cc.equalsIgnoreCase(CountryName.IND.toCode()))
                    countryName = CountryName.IND.toString();
                if (cc.equalsIgnoreCase(CountryName.USA.toCode()))
                    countryName = CountryName.USA.toString();
                if (cc.equalsIgnoreCase(CountryName.JPN.toCode()))
                    countryName = CountryName.JPN.toString();
                if (cc.equalsIgnoreCase(CountryName.CHI.toCode()))
                    countryName = CountryName.CHI.toString();
                if (cc.equalsIgnoreCase(CountryName.AUS.toCode()))
                    countryName = CountryName.AUS.toString();

                User user2 = connect(senderId,countryName);
                if (!user2.getConnected()){
                    throw new Exception("Cannot establish communication");

                }
                else return user2;
            }

        }
        else{
            if(user1.getOriginalCountry().equals(user.getOriginalCountry())){
                return user;
            }
            String countryName = user1.getOriginalCountry().getCountryName().toString();
            User user2 =  connect(senderId,countryName);
            if (!user2.getConnected()){
                throw new Exception("Cannot establish communication");
            }
            else return user2;

        }
    }
}
