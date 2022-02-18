/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.framework;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class ApplicationContextListener implements ApplicationListener {

    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ApplicationReadyEvent) {
            System.out.println("======================================================================================");
            System.out.println("======================================================================================");
            System.out.println("====                                                                               ===");
            System.out.println("====                             GLA OPS Started                                   ===");
            System.out.println("====                                                                               ===");
            System.out.println("======================================================================================");
            System.out.println("======================================================================================");
        }
    }

}
