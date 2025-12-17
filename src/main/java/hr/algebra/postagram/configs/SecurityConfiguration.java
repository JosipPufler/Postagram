package hr.algebra.postagram.configs;

import hr.algebra.postagram.services.CustomUserDetailsService;
import hr.algebra.postagram.services.OAuth2FailHandler;
import hr.algebra.postagram.services.OAuth2SuccessHandler;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@EnableAsync
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfiguration {
    private final CustomUserDetailsService userDetailsService;
    private final OAuth2SuccessHandler successHandler;
    private final OAuth2FailHandler failHandler;
    private final AuthEntryPoint unauthorizedHandler;
    private final TokenFilter tokenFilter;
    private final LoginHandler loginHandler;
    private final LogoutHandler logoutHandler;
    private final PasswordEncoder passwordEncoder;

    private static final String[] AUTH_WHITELIST = { "/auth/**", "/logout", "/mvc/public/**", "/rest/public/**", "/" };
    private static final String[] ADMIN_LIST = { "/mvc/admin/**" };

    @Bean
    @Order(1)
    public SecurityFilterChain apiChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/rest/**")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.requestMatchers("/rest/public/**").permitAll().anyRequest().authenticated())
                .authenticationProvider(authenticationProvider(passwordEncoder))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(unauthorizedHandler))
                .addFilterBefore(tokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain mvcChain(HttpSecurity http) throws Exception {
        http.userDetailsService(userDetailsService)
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
                .authorizeHttpRequests(authorize ->
                                authorize
                                        .requestMatchers(AUTH_WHITELIST).permitAll()
                                        .requestMatchers(
                                                "/js/**",
                                                "/css/**",
                                                "/images/**",
                                                "/webjars/**",
                                                "/favicon.ico"
                                        ).permitAll()
                                        .requestMatchers(ADMIN_LIST).hasRole("ADMIN")
                                        .anyRequest().hasAnyRole("ADMIN", "USER")
                        //.requestMatchers("/mvc/book/new").hasRole("ADMIN")
                )
                .formLogin(httpSecurityFormLoginConfigurer -> httpSecurityFormLoginConfigurer
                        .loginPage("/auth/login")
                        .loginProcessingUrl("/login")
                        .failureUrl("/login?error=true")
                        .successHandler(loginHandler)
                        .permitAll())
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(successHandler)
                        .failureHandler(failHandler))
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler(logoutHandler)
                        .permitAll()
                );
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http, PasswordEncoder encoder) throws Exception {

        AuthenticationManagerBuilder authBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);

        authBuilder
                .userDetailsService(userDetailsService)
                .passwordEncoder(encoder);

        return authBuilder.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(PasswordEncoder encoder) {
        DaoAuthenticationProvider auth = new DaoAuthenticationProvider();
        auth.setUserDetailsService(userDetailsService);
        auth.setPasswordEncoder(encoder);
        return auth;
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth, PasswordEncoder encoder) throws Exception {
        auth
                .userDetailsService(userDetailsService)
                .passwordEncoder(encoder);
    }

    @Bean
    public S3Client s3Client(
            @Value("${aws.region}") String region,
            @Value("${aws.access-key-id}") String accessKey,
            @Value("${aws.secret-access-key}") String secretKey
    ) {
        AwsBasicCredentials creds =
                AwsBasicCredentials.create(accessKey, secretKey);

        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(
                        StaticCredentialsProvider.create(creds)
                )
                .build();
    }
}

