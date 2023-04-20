package com.driver.services.impl;

import com.driver.model.Admin;
import com.driver.model.Country;
import com.driver.model.CountryName;
import com.driver.model.ServiceProvider;
import com.driver.repository.AdminRepository;
import com.driver.repository.CountryRepository;
import com.driver.repository.ServiceProviderRepository;
import com.driver.services.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdminServiceImpl implements AdminService {
    @Autowired
    AdminRepository adminRepository1;

    @Autowired
    ServiceProviderRepository serviceProviderRepository1;

    @Autowired
    CountryRepository countryRepository1;

    @Override
    public Admin register(String username, String password) {
        Admin admin = new Admin();
        admin.setUsername(username);
        admin.setPassword(password);
        admin.setServiceProviders(new ArrayList<>());
        adminRepository1.save(admin);
        return admin;

    }

    @Override
    public Admin addServiceProvider(int adminId, String providerName) throws Exception {
        Admin admin;
        try {
            admin = adminRepository1.findById(adminId).get();
        }catch (Exception e){
            throw new Exception("Admin not present");
        }
        List<ServiceProvider> serviceProviderList = admin.getServiceProviders();
        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setName(providerName);
        serviceProvider.setAdmin(admin);
        serviceProvider.setCountryList(new ArrayList<>());
        serviceProvider.setUsers(new ArrayList<>());
        serviceProviderList.add(serviceProvider);
        admin.setServiceProviders(serviceProviderList);
        adminRepository1.save(admin);
        return admin;
    }

    @Override
    public ServiceProvider addCountry(int serviceProviderId, String countryName) throws Exception{
//add a country under the serviceProvider and return respective service provider
// country name would be a 3-character string out of ind, aus, usa, chi, jpn. Each character can be in uppercase or lowercase.
// You should create a new Country object based on the given country name and add it to the country list of the service provider.
// Note that the user attribute of the country in this case would be null.
// In case country name is not amongst the above mentioned strings, throw "Country not found" exception
        ServiceProvider serviceProvider;
        try {
            serviceProvider = serviceProviderRepository1.findById(serviceProviderId).get();
        }catch (Exception e){
            throw new Exception("service provider not present");
        }
        List<Country> countryList = serviceProvider.getCountryList();
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
        country.setServiceProvider(serviceProvider);
        countryList.add(country);
        serviceProvider.setCountryList(countryList);
        serviceProviderRepository1.save(serviceProvider);

        return serviceProvider;

    }
}
