/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.framework.security;

import org.apache.commons.net.util.SubnetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.london.ops.framework.exception.ForbiddenAccessException;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Basic implementation of a client IP address whitelist.
 *
 * Whitelist entries can be individual IPv4 addresses or subnet ranges in CIDR format.
 *
 * @author Steve Leach
 */
@Component
public class IpWhitelist {

    Logger log = LoggerFactory.getLogger(getClass());

    public static final String MATCH_ANY = "0.0.0.0/0";

    private final Set<String> whiteList = new HashSet<>();

    @Autowired
    public void setWhitelist(@Value("${sql.whitelist}") String[] whiteListValues) {
        whiteList.addAll(Arrays.asList(whiteListValues));
        log.info("IP whitelist set to {}", whiteList);
    }

    @Value("${whitelist.mismatch.error}")
    boolean throwErrorOnWhitelistFailure = false;

    /**
     * Checks that the client for the specified http request is whitelisted.
     * <p>
     * If throwErrorOnWhitelistFailure is false then a warning is logged, otherwise an exception is thrown.
     *
     * @throws ForbiddenAccessException if the client is not whitelisted
     */
    public void checkClientIsWhitelisted(HttpServletRequest request) throws ForbiddenAccessException {

        String xffHeader = request.getHeader("X-Forwarded-For");
        log.debug("XFF header: {}", xffHeader);

        // TODO: use value from XFF header if it is provided
        String clientAddress = request.getRemoteAddr().trim();

        if (whiteListContains(clientAddress)) {
            log.debug("Client IP address is in whitelist: {} ({})", clientAddress, request.getRequestURI());
        } else {
            log.warn("Client IP address is not in whitelist: {} ({})", clientAddress, request.getRequestURI());
            if (throwErrorOnWhitelistFailure) {
                throw new ForbiddenAccessException();
            }
        }
    }

    public boolean whiteListContains(String clientAddress) {
        for (String whitelistEntry : whiteList) {
            if (addressMatchesWhitelistEntry(clientAddress, whitelistEntry)) {
                return true;
            }
        }
        return false;
    }

    boolean addressMatchesWhitelistEntry(String clientAddress, String whitelistEntry) {
        if ((whitelistEntry == null) || (clientAddress == null)) {
            return false;
        }

        if (whitelistEntry.contains("/")) {
            return addressInSubnet(clientAddress, whitelistEntry);
        } else {
            return whitelistEntry.trim().equals(clientAddress.trim());
        }
    }

    boolean addressInSubnet(String clientAddress, String cidrSubnet) {
        return new SubnetUtils(cidrSubnet).getInfo().isInRange(clientAddress.trim());
    }
}
