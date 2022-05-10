/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.user;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;


public class UserUtils {

    public static User currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = null;
        if (authentication != null && authentication.getPrincipal() instanceof User) {
           user = (User) authentication.getPrincipal();
        }
        return user;
    }

    public static void mockCurrentUser(User user) {
        Authentication authentication = new MockAuthenticationWithPrincipal(user);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    static class MockAuthenticationWithPrincipal implements Authentication {

        User user;

        MockAuthenticationWithPrincipal(User user) {
            this.user = user;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return null;
        }

        @Override
        public Object getCredentials() {
            return null;
        }

        @Override
        public Object getDetails() {
            return null;
        }

        @Override
        public Object getPrincipal() {
            return user;
        }

        @Override
        public boolean isAuthenticated() {
            return false;
        }

        @Override
        public void setAuthenticated(boolean b) throws IllegalArgumentException { }

        @Override
        public String getName() {
            return user.getUsername();
        }
    }
}
