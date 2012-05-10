package org.wso2.carbon.appfactory.user.registration.services;


import org.wso2.carbon.appfactory.user.registration.beans.UserRegistrationInfoBean;

/**
 * User registering service interface
 */
public interface UserRegistrationService {
    /**
     * Add the user to user store and return a confirmation key to validate the email
     *
     * @param user
     * @return confirmation key
     * @throws UserRegistrationException
     */
    String registerUser(UserRegistrationInfoBean user) throws UserRegistrationException;

    /**
     * Activating the registered user
     *
     * @param confirmationKey
     * @param userName
     * @param email
     * @return status of the operation
     * @throws UserRegistrationException
     */
    boolean activateUser(String confirmationKey, String userName, String email) throws
                                                                                UserRegistrationException;

    /**
     * @param userName
     * @return
     * @throws UserRegistrationException
     */
    public boolean doesUserExist(String userName) throws UserRegistrationException;
}
