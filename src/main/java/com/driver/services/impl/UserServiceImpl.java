//package com.driver.services.impl;
//
//import com.driver.model.Country;
//import com.driver.model.CountryName;
//import com.driver.model.ServiceProvider;
//import com.driver.model.User;
//import com.driver.repository.CountryRepository;
//import com.driver.repository.ServiceProviderRepository;
//import com.driver.repository.UserRepository;
//import com.driver.services.UserService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.ArrayList;
//import java.util.List;
//
//@Service
//public class UserServiceImpl implements UserService {
//
//    @Autowired
//    UserRepository userRepository3;
//    @Autowired
//    ServiceProviderRepository serviceProviderRepository3;
//    @Autowired
//    CountryRepository countryRepository3;
//
//    @Override
//    public User register(String username, String password, String countryName) throws Exception{
//        //create a user of given country. The originalIp of the user should be "countryCode.userId" and return the user.
//        // Note that right now user is not connected and thus connected would be false and maskedIp would be null
//        //Note that the userId is created automatically by the repository layer
//        User user = new User();
//        user.setUsername(username);
//        user.setPassword(password);
//        user.setConnected(false);
//        user.setConnectionList(new ArrayList<>());
//        user.setServiceProviderList(new ArrayList<>());
//        Country country = new Country();
//
//
//        countryName = countryName.toUpperCase();
//        if (countryName.equals("IND")){
//            country.setCountryName(CountryName.IND);
//            country.setCode(CountryName.IND.toCode());
//        }else if (countryName.equals("USA")){
//            country.setCountryName(CountryName.USA);
//            country.setCode(CountryName.USA.toCode());
//        }else if(countryName.equals("AUS")){/*chi,jap*/
//            country.setCountryName(CountryName.AUS);
//            country.setCode(CountryName.AUS.toCode());
//        }else if (countryName.equals("CHI")){
//            country.setCountryName(CountryName.CHI);
//            country.setCode(CountryName.CHI.toCode());
//        }else if (countryName.equals("JPN")){
//            country.setCountryName(CountryName.JPN);
//            country.setCode(CountryName.JPN.toCode());
//        }else {
//            throw new Exception("Country not found");
//        }
//        country.setUser(user);
//        user.setOriginalCountry(country);
//        user = userRepository3.save(user);
//        user.setOriginalIp(new String(user.getOriginalCountry().getCode() + "." + user.getId()));
//        user = userRepository3.save(user);
//
//        return user;
//    }
//
//    @Override
//    public User subscribe(Integer userId, Integer serviceProviderId) {
//        //subscribe to the serviceProvider by adding it to the list of providers and return updated User
//        ServiceProvider serviceProvider = serviceProviderRepository3.findById(serviceProviderId).get();
//        User user = userRepository3.findById(userId).get();
//        List<ServiceProvider> serviceProviderList = user.getServiceProviderList();
//        serviceProviderList.add(serviceProvider);
//        user.setServiceProviderList(serviceProviderList);
//        List<User> userList = serviceProvider.getUsers();
//        userList.add(user);
//        serviceProvider.setUsers(userList);
//        serviceProviderRepository3.save(serviceProvider);
//        return user;
//    }
//}




package com.driver.services.impl;

import com.driver.model.Country;
import com.driver.model.CountryName;
import com.driver.model.ServiceProvider;
import com.driver.model.User;
import com.driver.repository.CountryRepository;
import com.driver.repository.ServiceProviderRepository;
import com.driver.repository.UserRepository;
import com.driver.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserRepository userRepository3;
    @Autowired
    ServiceProviderRepository serviceProviderRepository3;
    @Autowired
    CountryRepository countryRepository3;

    @Override
    public User register(String username, String password, String countryName) throws Exception{
        String countryName1 = countryName.toUpperCase();
        if (!countryName1.equals("IND") && !countryName1.equals("USA") && !countryName1.equals("AUS") && !countryName1.equals("CHI") && !countryName1.equals("JPN")) throw new Exception("Country not found");
        Country country = new Country(CountryName.valueOf(countryName1), CountryName.valueOf(countryName1).toCode());
        User user = new User(username, password);
        country.setUser(user);
        user.setOriginalCountry(country);
        user = userRepository3.save(user);
        user.setOriginalIp(country.getCode() + "." + user.getId());
        userRepository3.save(user);
        return user;
    }

    @Override
    public User subscribe(Integer userId, Integer serviceProviderId) {
        User user = userRepository3.findById(userId).get();
        ServiceProvider serviceProvider = serviceProviderRepository3.findById(serviceProviderId).get();
        user.getServiceProviderList().add(serviceProvider);
        serviceProvider.getUsers().add(user);
        serviceProviderRepository3.save(serviceProvider);
        return user;
    }
}