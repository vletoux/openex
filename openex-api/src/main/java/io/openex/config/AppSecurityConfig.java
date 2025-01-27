package io.openex.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import io.openex.database.model.User;
import io.openex.database.repository.UserRepository;
import io.openex.rest.user.form.user.CreateUserInput;
import io.openex.security.OAuthRefererAuthenticationSuccessHandler;
import io.openex.security.TokenAuthenticationFilter;
import io.openex.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static org.springframework.util.StringUtils.hasLength;

@EnableWebSecurity
public class AppSecurityConfig extends WebSecurityConfigurerAdapter {

    private static final Logger LOGGER = Logger.getLogger(AppSecurityConfig.class.getName());

    private UserRepository userRepository;
    private UserService userService;
    private OpenExConfig openExConfig;
    private Environment env;

    @Resource
    protected ObjectMapper mapper;

    @Autowired
    public void setEnv(Environment env) {
        this.env = env;
    }

    @Autowired
    public void setOpenExConfig(OpenExConfig openExConfig) {
        this.openExConfig = openExConfig;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .requestCache()
                /**/.requestCache(new HttpSessionRequestCache())
                .and()
                .csrf()
                /**/.disable()
                .formLogin()
                /**/.disable()
                .authorizeRequests()
                /**/.antMatchers("/api/comcheck/**").permitAll()
                /**/.antMatchers("/api/player/**").permitAll()
                /**/.antMatchers("/api/settings").permitAll()
                /**/.antMatchers("/api/login").permitAll()
                /**/.antMatchers("/api/reset/**").permitAll()
                /**/.antMatchers("/api/**").authenticated()
                .and()
                .logout()
                /**/.invalidateHttpSession(true)
                /**/.deleteCookies("JSESSIONID", openExConfig.getCookieName())
                /**/.logoutSuccessUrl("/");

        if (openExConfig.isAuthOpenidEnable()) {
            http.oauth2Login().successHandler(new OAuthRefererAuthenticationSuccessHandler());
        }

        // Rewrite 403 code to 401
        http.exceptionHandling().authenticationEntryPoint((request, response, authException)
                -> response.setStatus(HttpStatus.UNAUTHORIZED.value()));
    }

    @Bean
    public TokenAuthenticationFilter tokenAuthenticationFilter() {
        return new TokenAuthenticationFilter();
    }

    private List<String> extractRolesFromToken(OAuth2AccessToken accessToken, ClientRegistration clientRegistration) {
        ObjectReader listReader = mapper.readerFor(new TypeReference<List<String>>() {
        });
        if (accessToken != null) {
            String registrationId = clientRegistration.getRegistrationId();
            String rolesPathConfig = "openex.provider." + registrationId + ".roles_path";
            List<String> rolesPath = env.getProperty(rolesPathConfig, List.class, new ArrayList<String>());
            try {
                String[] chunks = accessToken.getTokenValue().split("\\.");
                Base64.Decoder decoder = Base64.getUrlDecoder();
                String payload = new String(decoder.decode(chunks[1]));
                JsonNode jsonNode = mapper.readTree(payload);
                return rolesPath.stream().map(path -> "/" + path.replaceAll("\\.", "/"))
                        .flatMap(path -> {
                            JsonNode arrayRoles = jsonNode.at(path);
                            try {
                                List<String> roles = listReader.readValue(arrayRoles);
                                return roles.stream();
                            } catch (IOException e) {
                                return Stream.empty();
                            }
                        }).toList();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        }
        return new ArrayList<>();
    }

    public User oauthUserManagement(OAuth2AccessToken accessToken, ClientRegistration clientRegistration, OAuth2User user) {
        String emailAttribute = user.getAttribute("email");
        String email = ofNullable(emailAttribute).orElseThrow();
        String registrationId = clientRegistration.getRegistrationId();
        String rolesAdminConfig = "openex.provider." + registrationId + ".roles_admin";
        List<String> rolesAdmin = env.getProperty(rolesAdminConfig, List.class, new ArrayList<String>());
        List<String> rolesFromToken = extractRolesFromToken(accessToken, clientRegistration);
        boolean isAdmin = rolesAdmin.stream().anyMatch(rolesFromToken::contains);
        if (hasLength(email)) {
            String firstName = user.getAttribute("given_name");
            String lastName = user.getAttribute("family_name");
            Optional<User> optionalUser = userRepository.findByEmail(email);
            // If user not exists, create it
            if (optionalUser.isEmpty()) {
                CreateUserInput createUserInput = new CreateUserInput();
                createUserInput.setEmail(email);
                createUserInput.setFirstname(firstName);
                createUserInput.setLastname(lastName);
                createUserInput.setAdmin(isAdmin);
                return userService.createUser(createUserInput, 0);
            } else {
                // If user exists, update it
                User currentUser = optionalUser.get();
                currentUser.setFirstname(firstName);
                currentUser.setLastname(lastName);
                currentUser.setAdmin(isAdmin);
                return userService.updateUser(currentUser);
            }
        }
        OAuth2Error authError = new OAuth2Error("invalid_token", "User conversion fail", "");
        throw new OAuth2AuthenticationException(authError);
    }

    @Bean
    public OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
        OidcUserService delegate = new OidcUserService();
        return request -> oauthUserManagement(request.getAccessToken(), request.getClientRegistration(), delegate.loadUser(request));
    }

    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService() {
        DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
        return request -> oauthUserManagement(request.getAccessToken(), request.getClientRegistration(), delegate.loadUser(request));
    }
}
