package com.meze.service;

import com.meze.domains.*;
import com.meze.domains.enums.RoleType;
import com.meze.domains.enums.UserStatus;
import com.meze.dto.FavoriteProductDTO;
import com.meze.dto.UserDTO;
import com.meze.dto.UserDeleteDTO;
import com.meze.dto.request.*;
import com.meze.dto.response.LoginResponse;
import com.meze.dto.response.ResponseMessage;
import com.meze.exception.BadRequestException;
import com.meze.exception.ConflictException;
import com.meze.exception.ResourceNotFoundException;
import com.meze.exception.message.ErrorMessage;
import com.meze.mapper.ProductMapper;
import com.meze.mapper.UserMapper;
import com.meze.repository.ShoppingCartItemRepository;
import com.meze.repository.ShoppingCartRepository;
import com.meze.repository.UserRepository;
import com.meze.reusableMethods.DiscountCalculator;
import com.meze.security.SecurityUtils;
import com.meze.security.jwt.JwtUtils;
import com.meze.service.email.EmailSender;
import com.meze.service.email.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleService roleService ;
    private final UserMapper userMapper;
    private final ProductMapper productMapper;
    private final PasswordEncoder passwordEncoder;
    private final ConfirmationTokenService confirmationTokenService;
    private final PasswordResetService passwordResetService;
    private final AuthenticationManager authenticationManager;
    private final EntityManager entityManager;
    private final JwtUtils jwtUtils;
    private final DiscountCalculator discountCalculator;
    private final EmailSender emailSender;
    private final EmailService emailService;
    private final ShoppingCartRepository shoppingCartRepository;
    private final ShoppingCartItemRepository shoppingCartItemRepository;
    @Value("${meze.app.backendLink}")
    private String backendLink;
    @Value("${meze.app.resetPasswordLink}")
    private String frontendLink;

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(()->
                new ResourceNotFoundException(ErrorMessage.USER_NOT_FOUND_MESSAGE));
    }

    public void save(User user){
        userRepository.save(user);
    }

    public User getUserByEmailResetPassword(String email) {
        return userRepository.findByEmail(email).orElseThrow(()->
                new ResourceNotFoundException(ResponseMessage.RESET_MAIL_SENT_RESPONSE)
        );
    }

    public User getCurrentUser() {
        String email = SecurityUtils.getCurrentUserLogin().orElseThrow(()->
                new ResourceNotFoundException(ErrorMessage.PRINCIPAL_FOUND_MESSAGE));
        return getUserByEmail(email);

    }

    public String saveUser(RegisterRequest registerRequest) {
        Role role = roleService.findByRoleName(RoleType.ROLE_CUSTOMER);
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        User user = null;
        String encodedPassword =  passwordEncoder.encode(registerRequest.getPassword());
        if(userRepository.existsByEmail(registerRequest.getEmail())) {
            user =getUserByEmail(registerRequest.getEmail());
            if(user.getStatus()!= UserStatus.ANONYMOUS) {
                throw new ConflictException(String.format(ErrorMessage.EMAIL_ALREADY_EXIST_MESSAGE,
                        registerRequest.getEmail()));
            }else{
                user.setFirstName(registerRequest.getFirstName());
                user.setLastName(registerRequest.getLastName());
                user.setEmail(registerRequest.getEmail());
                user.setBirthDate(registerRequest.getBirthDate());
                user.setPhone(registerRequest.getPhone());
                user.setStatus(UserStatus.PENDING);
                user.setPassword(encodedPassword);
                userRepository.save(user);
            }
        }else {
            user = new User();
            user.setFirstName(registerRequest.getFirstName());
            user.setLastName(registerRequest.getLastName());
            user.setEmail(registerRequest.getEmail());
            user.setBirthDate(registerRequest.getBirthDate());
            user.setPassword(encodedPassword);
            user.setPhone(registerRequest.getPhone());
            user.setRoles(roles);
            user.setStatus(UserStatus.PENDING);
            userRepository.save(user);
        }


        String token = UUID.randomUUID().toString();
        ConfirmationToken confirmationToken = new ConfirmationToken(token, LocalDateTime.now(),LocalDateTime.now().plusDays(1),user);
        confirmationTokenService.saveConfirmationToken(confirmationToken);
        String link = frontendLink+"confirm?token="+token;
        emailSender.send(
                registerRequest.getEmail(),
                emailService.buildRegisterEmail(registerRequest.getFirstName(),link));
        return token;
    }

    @Transactional
    public UserDTO confirmToken(String token) {
        ConfirmationToken confirmationToken = confirmationTokenService
                .getToken(token)
                .orElseThrow(() ->
                        new ResourceNotFoundException(ErrorMessage.RESOURCE_NOT_FOUND_MESSAGE));
        if (confirmationToken.getConfirmedAt() != null) {
            throw new IllegalStateException(ErrorMessage.EMAIL_ALREADY_CONFIRMED_MESSAGE);
        }
        LocalDateTime expiredAt = confirmationToken.getExpiresAt();
        if (expiredAt.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException(ErrorMessage.TOKEN_EXPIRED_MESSAGE);
        }
        confirmationTokenService.setConfirmedAt(token);
        User user = getUserByEmail(confirmationToken.getUser().getEmail());
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setCartUUID(UUID.randomUUID().toString());
        shoppingCartRepository.save(shoppingCart);
        user.setShoppingCart(shoppingCart);
        activateUser(user.getEmail());
        return userMapper.userToUserDTO(user);
    }

    @Transactional
    public String confirmResetToken(String token, PasswordResetRequest passwordResetRequest) {
        PasswordResetToken passwordResetToken = passwordResetService
                .getToken(token)
                .orElseThrow(() ->
                        new ResourceNotFoundException(ErrorMessage.RESOURCE_NOT_FOUND_MESSAGE));
        if (passwordResetToken.getUsedAt() != null) {
            throw new IllegalStateException(ErrorMessage.RESET_TOKEN_ALREADY_USED_MESSAGE);
        }
        LocalDateTime expiredAt = passwordResetToken.getExpiresAt();
        if (expiredAt.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException(ErrorMessage.TOKEN_EXPIRED_MESSAGE);
        }
        User user = getUserByEmail(passwordResetToken.getUser().getEmail());
        passwordResetService.setUsedAt(token);
        updatePasswordWithResetCode(user,passwordResetRequest);
        return ResponseMessage.PASSWORD_CHANGED_RESPONSE_MESSAGE;
    }


    public UserDTO getPrincipal() {
        User user=getCurrentUser();
        return userMapper.userToUserDTO(user);
    }

    public UserDTO updateUser(UserUpdateRequest userUpdateRequest) {
        User user = getCurrentUser();
        if (user.getBuiltIn()) {
            throw new BadRequestException(ErrorMessage.NOT_PERMITTED_METHOD_MESSAGE);
        }
        boolean emailExist = userRepository.existsByEmail(userUpdateRequest.getEmail());
        if (emailExist && !userUpdateRequest.getEmail().equals(user.getEmail())) {
            throw new ConflictException(String.format(ErrorMessage.EMAIL_ALREADY_EXIST_MESSAGE, userUpdateRequest.getEmail()));
        }
        user.setEmail(userUpdateRequest.getEmail());
        user.setFirstName(userUpdateRequest.getFirstName());
        user.setLastName(userUpdateRequest.getLastName());
        user.setPhone(userUpdateRequest.getPhone());
        user.setBirthDate(userUpdateRequest.getBirthDate());
        user.setUpdateAt(LocalDateTime.now());
        userRepository.save(user);
        return userMapper.userToUserDTO(user);
    }

    public void updatePassword(PasswordUpdateRequest passwordUpdateRequest) {
        User user=getCurrentUser();
        if (user.getBuiltIn()) {
            throw new BadRequestException(ErrorMessage.NOT_PERMITTED_METHOD_MESSAGE);
        }

        if (!passwordEncoder.matches(passwordUpdateRequest.getOldPassword(), user.getPassword())) {
            throw new BadRequestException(ErrorMessage.PASSWORD_NOT_MATCHED);
        }

        String hashedPassword= passwordEncoder.encode(passwordUpdateRequest.getNewPassword());
        user.setPassword(hashedPassword);
        userRepository.save(user);
    }

    public void updatePasswordWithResetCode(User user,PasswordResetRequest passwordResetRequest) {
        String hashedPassword= passwordEncoder.encode(passwordResetRequest.getNewPassword());
        user.setPassword(hashedPassword);
        userRepository.save(user);
    }

    //dikkatli incele
    public UserDeleteDTO removeUserById() {
        User user=getCurrentUser();
        if (user.getBuiltIn()) {
            throw new BadRequestException(ErrorMessage.NOT_PERMITTED_METHOD_MESSAGE);
        }
        user.setStatus(UserStatus.ANONYMOUS);
        user.setPassword("");
        Long shoppingCartId=user.getShoppingCart().getId();
        user.setShoppingCart(null);
        userRepository.save(user);
        shoppingCartRepository.deleteById(shoppingCartId);
        userRepository.delete(user);
        return userMapper.userToUserDeleteDTO(user);
    }
    public List<UserDTO> getAllUser(String query, RoleType role, boolean birthday, boolean anniversary) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> criteriaQuery = cb.createQuery(User.class);
        Root<User> root = criteriaQuery.from(User.class);
        root.alias("generatedAlias0");

        List<Predicate> predicates = new ArrayList<>();

        if (query != null && !query.isEmpty()) {
            String likeSearchText = "%" + query.toLowerCase(Locale.US) + "%";
            Predicate searchByUserFirstName = cb.like(cb.lower(root.get("firstName")), likeSearchText);
            Predicate searchByUserLastName = cb.like(cb.lower(root.get("lastName")), likeSearchText);
            Predicate searchByUserEmail = cb.like(cb.lower(root.get("email")), likeSearchText);
            predicates.add(cb.or(searchByUserFirstName,searchByUserLastName,searchByUserEmail));
        }
        if (role != null){
            Join<User, Role> joinRoles = root.join("roles");
            joinRoles.alias("generatedAlias1");
            predicates.add(cb.equal(joinRoles.get("roleName"), role));
        }
        if (birthday){
            LocalDate currentDate = LocalDate.now();
            int currentMonth = currentDate.getMonthValue();
            int currentDay = currentDate.getDayOfMonth();
            Expression<Integer> birthMonth = cb.function("month", Integer.class, root.get("birthDate"));
            Expression<Integer> birthDay = cb.function("day", Integer.class, root.get("birthDate"));

            Predicate matchMonth = cb.equal(birthMonth, currentMonth);
            Predicate matchDay = cb.equal(birthDay, currentDay);
            predicates.add(cb.and(matchMonth,matchDay));
        }
        if (anniversary){
            LocalDateTime currentDate = LocalDateTime.now();

            LocalDateTime yearAgo = currentDate.minusYears(1);
            int currentMonth = currentDate.getMonthValue();
            int currentDay = currentDate.getDayOfMonth();
            int oneYearAgo = yearAgo.getYear();

            Expression<Integer> registrationMonth = cb.function("month", Integer.class, root.get("createAt"));
            Expression<Integer> registrationDay = cb.function("day", Integer.class, root.get("createAt"));
            Expression<Integer> registrationYear = cb.function("year", Integer.class, root.get("createAt"));

            Predicate matchMonth = cb.equal(registrationMonth, currentMonth);
            Predicate matchDay = cb.equal(registrationDay, currentDay);
            Predicate registrationBeforeOneYearAgo = cb.lessThanOrEqualTo(registrationYear, oneYearAgo);

            predicates.add(cb.and(matchMonth, matchDay, registrationBeforeOneYearAgo));
        }
        Predicate finalPredicate = cb.and(predicates.toArray(new Predicate[0]));
        criteriaQuery.select(root);
        criteriaQuery.where(finalPredicate);
        TypedQuery<User> typedQuery = entityManager.createQuery(criteriaQuery);
        return userMapper.map(typedQuery.getResultList());
    }

        public PageImpl<UserDTO> getAllUserPage(String query,RoleType role,Pageable pageable) {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<User> criteriaQuery = cb.createQuery(User.class);
            Root<User> root = criteriaQuery.from(User.class);
            root.alias("generatedAlias0");
            CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
            Root<User> countRoot = countQuery.from(User.class);
            List<Predicate> predicates = new ArrayList<>();

            if (query != null && !query.isEmpty()) {
                String likeSearchText = "%" + query.toLowerCase(Locale.US) + "%";
                Predicate searchByUserFirstName = cb.like(cb.lower(root.get("firstName")), likeSearchText);
                Predicate searchByUserLastName = cb.like(cb.lower(root.get("lastName")), likeSearchText);
                Predicate searchByUserEmail = cb.like(cb.lower(root.get("email")), likeSearchText);
                predicates.add(cb.or(searchByUserFirstName,searchByUserLastName,searchByUserEmail));
            }
            if (role != null){
                Join<User, Role> joinRoles = root.join("roles");
                Join<User, Role> countRoles = countRoot.join("roles");
                joinRoles.alias("generatedAlias1");
                predicates.add(cb.equal(joinRoles.get("roleName"), role));
            }

            Predicate finalPredicate = cb.and(predicates.toArray(new Predicate[0]));
            criteriaQuery.orderBy(pageable.getSort().stream()
                    .map(order -> {
                        if (order.isAscending()) {
                            return cb.asc(root.get(order.getProperty()));
                        } else {
                            return cb.desc(root.get(order.getProperty()));
                        }
                    })
                    .collect(Collectors.toList()));

            criteriaQuery.select(root);
            criteriaQuery.where(finalPredicate);
            countQuery.select(cb.count(countRoot));
            countQuery.where(finalPredicate);
            Long totalRecords = entityManager.createQuery(countQuery).getSingleResult();

            TypedQuery<User> typedQuery = entityManager.createQuery(criteriaQuery);
            typedQuery.setFirstResult((int)pageable.getOffset());
            typedQuery.setMaxResults(pageable.getPageSize());

            List<UserDTO> userDTOList = userMapper.map(typedQuery.getResultList());
            return new PageImpl<>(userDTOList, pageable, totalRecords);
    }

    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException(String.format(ErrorMessage.RESOURCE_NOT_FOUND_MESSAGE, id)));
        return userMapper.userToUserDTO(user);
    }

    public UserDTO updateUserAuth(Long id, AdminUserUpdateRequest adminUserUpdateRequest) {
        User user=getById(id);
        if (user.getBuiltIn()) {
            throw new BadRequestException(ErrorMessage.NOT_PERMITTED_METHOD_MESSAGE);
        }
        boolean emailExist=userRepository.existsByEmail(adminUserUpdateRequest.getEmail());
        if (emailExist && !adminUserUpdateRequest.getEmail().equals(user.getEmail())) {
            throw new ConflictException(String.format(ErrorMessage.EMAIL_ALREADY_EXIST_MESSAGE, adminUserUpdateRequest.getEmail()));
        }

        if (adminUserUpdateRequest.getPassword()==null) {
            adminUserUpdateRequest.setPassword(user.getPassword());
        }else {
            String encodedPassword= passwordEncoder.encode(adminUserUpdateRequest.getPassword());
            adminUserUpdateRequest.setPassword(encodedPassword);
        }
        Set<String> userStrRoles=adminUserUpdateRequest.getRoles();
        Set<Role> roles=convertRoles(userStrRoles);

        user.setFirstName(adminUserUpdateRequest.getFirstName());
        user.setLastName(adminUserUpdateRequest.getLastName());
        user.setEmail(adminUserUpdateRequest.getEmail());
        user.setPassword(adminUserUpdateRequest.getPassword());
        user.setPhone(adminUserUpdateRequest.getPhone());
        user.setBirthDate(adminUserUpdateRequest.getBirthDate());
        user.setUpdateAt(LocalDateTime.now());
        user.setRoles(roles);
        userRepository.save(user);
        return userMapper.userToUserDTO(user);
    }

    public UserDeleteDTO adminRemoveUserById(Long id) {
        User user=getById(id);
        if (user.getBuiltIn()) {
            throw new BadRequestException(ErrorMessage.NOT_PERMITTED_METHOD_MESSAGE);
        }
        user.setStatus(UserStatus.ANONYMOUS);
        user.setPassword("");
        Long shoppingCartId=user.getShoppingCart().getId();
        user.setShoppingCart(null);
        userRepository.save(user);
        shoppingCartRepository.deleteById(shoppingCartId);
        return userMapper.userToUserDeleteDTO(user);
    }

    public Set<Role> convertRoles(Set<String> pRoles){
        Set<Role> roles=new HashSet<>();
        if (pRoles==null) {
            Role userRole= roleService.findByRoleName(RoleType.ROLE_CUSTOMER);
            roles.add(userRole);
        }else {
            pRoles.forEach(roleStr->{
                if (roleStr.equals(RoleType.ROLE_ADMIN.getName())) {
                    Role adminRole= roleService.findByRoleName(RoleType.ROLE_ADMIN);
                    roles.add(adminRole);
                }else if (roleStr.equals(RoleType.ROLE_MANAGER.getName())) {
                    Role managerRole = roleService.findByRoleName(RoleType.ROLE_MANAGER);
                    roles.add(managerRole);
                }else {
                    Role userRole=roleService.findByRoleName(RoleType.ROLE_CUSTOMER);
                    roles.add(userRole);
                }
            });
        }
        return roles;
    }

    public User getById(Long id) {
        return userRepository.findUserById(id).orElseThrow(()->
                new ResourceNotFoundException(String.format(ErrorMessage.RESOURCE_NOT_FOUND_MESSAGE,id)));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void activateUser(String email) {
        UserStatus status = UserStatus.ACTIVATED;
        userRepository.enableUser(status, email);
    }

    public LoginResponse loginUser(String cartUUID, LoginRequest loginRequest) {
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword());
        Authentication authentication  =  authenticationManager.
                authenticate(usernamePasswordAuthenticationToken);
        UserDetails userDetails  =  (UserDetails) authentication.getPrincipal() ;
        User user = getUserByEmail(userDetails.getUsername());
        ShoppingCart anonymousCart = shoppingCartRepository.findByCartUUID(cartUUID).orElseThrow(()->
                new ResourceNotFoundException(String.format(ErrorMessage.RESOURCE_NOT_FOUND_MESSAGE,cartUUID)));
        ShoppingCart userCart = user.getShoppingCart();
        if (!anonymousCart.getShoppingCartItem().isEmpty()){
            if (userCart.getShoppingCartItem().isEmpty()) {
                for (ShoppingCartItem anonymousCartItem: anonymousCart.getShoppingCartItem()) {
                    ShoppingCartItem shoppingCartItem = new ShoppingCartItem();
                    shoppingCartItem.setProduct(anonymousCartItem.getProduct());
                    shoppingCartItem.setQuantity(anonymousCartItem.getQuantity());
                    shoppingCartItem.setTotalPrice(anonymousCartItem.getTotalPrice());
                    shoppingCartItem.setShoppingCart(userCart);
                    userCart.setGrandTotal(anonymousCart.getGrandTotal());
                    shoppingCartItemRepository.save(shoppingCartItem);
                }
                shoppingCartRepository.save(userCart);
            }else{
                for (ShoppingCartItem anonymousCartItem: anonymousCart.getShoppingCartItem()) {
                    boolean merged = false;
                    for (ShoppingCartItem userCartItem: userCart.getShoppingCartItem()) {
                        if (anonymousCartItem.getProduct().getId().longValue()==userCartItem.getProduct().getId().longValue()) {
                            if ((userCartItem.getQuantity()+anonymousCartItem.getQuantity())>userCartItem.getProduct().getStockAmount()){
                                int tempQuantity = userCartItem.getQuantity();
                                int stockAmount = userCartItem.getProduct().getStockAmount();
                                userCartItem.setQuantity(stockAmount);
                                if (!Character.isLetter(userCartItem.getProduct().getPrice().charAt(0))){
                                    userCartItem.setTotalPrice(stockAmount*Double.parseDouble(userCartItem.getProduct().getPrice()));
                                    userCart.setGrandTotal(userCart.getGrandTotal()+((stockAmount-tempQuantity)*Double.parseDouble(userCartItem.getProduct().getPrice())));
                                }

                            }else{
                                userCartItem.setQuantity(userCartItem.getQuantity() + anonymousCartItem.getQuantity());
                                userCartItem.setTotalPrice(userCartItem.getTotalPrice() + anonymousCartItem.getTotalPrice());
                                userCart.setGrandTotal(userCart.getGrandTotal()+anonymousCartItem.getTotalPrice());
                            }
                            merged = true;
                            break;
                        }
                    }
                    if (!merged){
                        ShoppingCartItem unmergedItem = new ShoppingCartItem();
                        unmergedItem.setQuantity(anonymousCartItem.getQuantity());
                        unmergedItem.setProduct(anonymousCartItem.getProduct());
                        unmergedItem.setTotalPrice(anonymousCartItem.getTotalPrice());
                        unmergedItem.setShoppingCart(userCart);
                        userCart.setGrandTotal(userCart.getGrandTotal()+unmergedItem.getTotalPrice());
                        shoppingCartItemRepository.save(unmergedItem);
                    }
                }
            }
        }
            shoppingCartRepository.save(userCart);
            shoppingCartRepository.delete(anonymousCart);
        if (user.getStatus().equals(UserStatus.PENDING)){
            throw new BadRequestException(String.format(ErrorMessage.EMAIL_NOT_CONFIRMED_MESSAGE,user.getEmail()));
        }
        String jwtToken =   jwtUtils.generateJwtToken(userDetails);
        String userCartUUID = user.getShoppingCart().getCartUUID();
        return new LoginResponse(jwtToken,userCartUUID);
    }

    public String forgotPassword(ForgotPasswordRequest forgotPasswordRequest) {
        User user = getUserByEmailResetPassword(forgotPasswordRequest.getEmail());
        if (user.getBuiltIn()){
            return ResponseMessage.RESET_MAIL_SENT_RESPONSE;
        }
        String token = (UUID.randomUUID().toString());
        PasswordResetToken passwordResetToken = new PasswordResetToken(token, LocalDateTime.now(),LocalDateTime.now().plusDays(1),user);
        passwordResetService.savePasswordResetToken(passwordResetToken);
        userRepository.save(user);
        String link = frontendLink +"reset-password?token="+token;
        emailSender.send(
                user.getEmail(),
                emailService.buildForgotPasswordEmail(user.getFirstName(),link,forgotPasswordRequest));
        return ResponseMessage.RESET_MAIL_SENT_RESPONSE;
    }

    public List<FavoriteProductDTO> getAuthFavorites() {
        User user = getCurrentUser();
        return productMapper.productListToFavoriteProductDTOList(user.getFavoriteList());
    }

    public void deleteAuthFavorites() {
        User user = getCurrentUser();
        user.getFavoriteList().removeAll(user.getFavoriteList());
        userRepository.save(user);
    }

    public void removeAllBuiltInFalseUsers() {
        userRepository.deleteAllByBuiltInFalse();

    }

    public long countUserRecords() {
        return userRepository.count();
    }

    public List<User> findUserByRole(RoleType role) {
        return userRepository.findByRole(role);
    }

}
