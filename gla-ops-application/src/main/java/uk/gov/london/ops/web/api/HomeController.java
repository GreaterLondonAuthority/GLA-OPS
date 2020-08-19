/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.web.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@Api(hidden = true)
public class HomeController {

    Logger log = LoggerFactory.getLogger("CLOUDWATCH");

    @RequestMapping(value="/", method = RequestMethod.GET)
    @ApiOperation(value="",hidden = true)
    ModelAndView home(ModelMap model) {
        return new ModelAndView("redirect:/index.html", model);
    }

    @RequestMapping(value="/testLog", method = RequestMethod.GET)
    @ApiOperation(value="",hidden = true)
    public void testLog(@RequestParam String line) {
        log.info("test log: {}", line);
    }
}
