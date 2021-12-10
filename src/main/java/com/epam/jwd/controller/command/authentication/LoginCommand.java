package com.epam.jwd.controller.command.authentication;

import com.epam.jwd.controller.command.Attributes;
import com.epam.jwd.controller.command.Command;
import com.epam.jwd.controller.context.RequestContext;
import com.epam.jwd.controller.context.ResponseContext;
import com.epam.jwd.dao.exception.DAOException;
import com.epam.jwd.service.dto.UserDTO;
import com.epam.jwd.service.impl.LoginService;
import com.epam.jwd.service.impl.ValidationService;
import jakarta.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LoginCommand implements Command {
    private static final Logger logger = LogManager.getLogger(LoginCommand.class);
    private static final int RESULT_MESSAGE_CODE = 100;
    private static final Command INSTANCE = new LoginCommand();
    public static String LOGIN_JSP = "/controller?command=SHOW_LOGIN_PAGE";
    private static final ResponseContext LOGIN_COMMAND_CONTEXT = new ResponseContext() {
        @Override
        public String getPage() {
            return LOGIN_JSP;
        }

        @Override
        public boolean isRedirect() {
            return false;
        }
    };

    public static Command getInstance() {
        return INSTANCE;
    }

    @Override
    public ResponseContext execute(RequestContext requestContext) {
        logger.debug("execute method");
        UserDTO userDTO = new UserDTO();
        List<Integer> errors = new ArrayList<>();
        String username = requestContext.getParamFromJSP(Attributes.JSP_USERNAME_INPUT_FILED_NAME).trim();
        String password = requestContext.getParamFromJSP(Attributes.JSP_PASSWORD_INPUT_FILED_NAME).trim();

        userDTO.setUsername(username);
        userDTO.setPassword(password);
        ValidationService validationService = new ValidationService();
        LoginService loginService = new LoginService();
        try {
            if (validationService.validateLogin(userDTO, errors)) {
                UserDTO loggedinUser = loginService.authenticate(userDTO, errors);
                if (Objects.nonNull(loggedinUser)) {
                    HttpSession session = requestContext.getHttpSession(true);
                    session.setAttribute(Attributes.SESSION_USER_ATTRIBUTE, loggedinUser);
                    session.setAttribute(Attributes.SESSION_LOGIN_STATE_ATTRIBUTE, true);
                    requestContext.addAttributeToJSP(Attributes.COMMAND_RESULT_ATTRIBUTE, RESULT_MESSAGE_CODE);
                } else {
                    requestContext.addAttributeToJSP(Attributes.COMMAND_ERRORS_ATTRIBUTE, errors);
                }
            } else {
                requestContext.addAttributeToJSP(Attributes.COMMAND_ERRORS_ATTRIBUTE, errors);
            }
        } catch (DAOException e) {
            logger.error(e);
            requestContext.addAttributeToJSP(Attributes.EXCEPTION_ATTRIBUTE, e.getMessage());
        }
        return LOGIN_COMMAND_CONTEXT;
    }
}

