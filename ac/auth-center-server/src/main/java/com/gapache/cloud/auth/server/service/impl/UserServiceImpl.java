package com.gapache.cloud.auth.server.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.gapache.cloud.auth.server.dao.entity.ClientEntity;
import com.gapache.cloud.auth.server.dao.entity.ResourceEntity;
import com.gapache.cloud.auth.server.dao.entity.UserEntity;
import com.gapache.cloud.auth.server.dao.entity.UserRoleEntity;
import com.gapache.cloud.auth.server.dao.repository.client.ClientRepository;
import com.gapache.cloud.auth.server.dao.repository.resource.ResourceRepository;
import com.gapache.cloud.auth.server.dao.repository.user.RoleRepository;
import com.gapache.cloud.auth.server.dao.repository.user.UserClientRelationRepository;
import com.gapache.cloud.auth.server.dao.repository.user.UserRepository;
import com.gapache.cloud.auth.server.dao.repository.user.UserRoleRepository;
import com.gapache.cloud.auth.server.model.UserDetailsImpl;
import com.gapache.cloud.auth.server.service.UserService;
import com.gapache.cloud.auth.server.utils.DynamicPropertyUtils;
import com.gapache.commons.model.AuthConstants;
import com.gapache.commons.model.JsonResult;
import com.gapache.commons.model.SecurityException;
import com.gapache.commons.model.ThrowUtils;
import com.gapache.security.entity.AuthorizeInfoEntity;
import com.gapache.security.entity.IdTokenEntity;
import com.gapache.security.interfaces.AsyncAuthorizeInfoManager;
import com.gapache.security.interfaces.AuthorizeInfoManager;
import com.gapache.security.model.*;
import com.gapache.security.model.impl.CertificationImpl;
import com.gapache.security.properties.SecurityProperties;
import com.gapache.security.utils.JwtUtils;
import com.gapache.user.common.model.vo.UserVO;
import com.gapache.user.sdk.feign.UserServerFeign;
import com.gapache.vertx.core.VertxCreatedEvent;
import com.gapache.vertx.core.VertxManager;
import com.gapache.vertx.redis.support.SimpleRedisRepository;
import com.google.common.collect.Lists;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.security.PrivateKey;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.gapache.commons.model.AuthConstants.IS_ENABLED;
import static com.gapache.commons.model.AuthConstants.TOKEN_HEADER;

/**
 * @author HuSen
 * @since 2020/7/31 10:19 上午
 */
@Slf4j
@Service("userService")
public class UserServiceImpl implements UserService, ApplicationListener<VertxCreatedEvent> {

    @Resource
    private UserRepository userRepository;

    @Resource
    private RoleRepository roleRepository;

    @Resource
    private ResourceRepository resourceRepository;

    @Resource
    private UserRoleRepository userRoleRepository;

    @Resource
    private ClientRepository clientRepository;

    @Resource
    private UserClientRelationRepository userClientRelationRepository;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private PrivateKey privateKey;

    @Resource
    private SecurityProperties securityProperties;

    @Resource
    private UserServerFeign userServerFeign;

    @Resource
    private AuthorizeInfoManager authorizeInfoManager;

    @Resource
    private SimpleRedisRepository simpleRedisRepository;

    @Resource
    private AsyncAuthorizeInfoManager asyncAuthorizeInfoManager;

    private final AtomicBoolean atomicBoolean = new AtomicBoolean(false);

    @Override
    public void onApplicationEvent(@NonNull VertxCreatedEvent vertxCreatedEvent) {
        if (vertxCreatedEvent.isSuccess()) {
            if(atomicBoolean.compareAndSet(false, true)) {
                Handler<Message<JsonObject>> authorizeInfoUpdater = event -> {
                    log.info(">>>>>> 更新用户的授权相关信息:{}", event);
                    JsonObject body = event.body();
                    Long userId = body.getLong("userId");
                    if (userId == null) {
                        return;
                    }

                    simpleRedisRepository
                            .findById(userId.toString(), IdTokenEntity.class)
                            .onSuccess(idToken -> {
                                if (idToken == null || idToken.getToken() == null) {
                                    return;
                                }

                                simpleRedisRepository
                                        .findById(idToken.getToken(), AuthorizeInfoEntity.class)
                                        .onSuccess(authorizeInfoEntity -> {
                                            if (authorizeInfoEntity == null || authorizeInfoEntity.getId() == null) {
                                                return;
                                            }

                                            String authorizeInfo = body.getString("authorizeInfo");
                                            AuthorizeInfoDTO authorizeInfoDTO = JSON.parseObject(authorizeInfo, AuthorizeInfoDTO.class);

                                            CustomerInfo customerInfo = JSON.parseObject(authorizeInfoEntity.getCustomerInfo(), CustomerInfo.class);
                                            // 挨个挨个替换
                                            if (authorizeInfoDTO.getCustomerInfo() != null) {
                                                authorizeInfoDTO.getCustomerInfo().forEach(customerInfo::put);
                                            }

                                            // 保存
                                            asyncAuthorizeInfoManager.save(
                                                    idToken.getToken(),
                                                    0L,
                                                    customerInfo,
                                                    authorizeInfoDTO.getScopes() != null ?
                                                            authorizeInfoDTO.getScopes() :
                                                            Arrays.asList(authorizeInfoEntity.getScopes().split(",").clone())
                                            );
                                        });
                            });
                };
                log.info("<<<<<<<<<<<<");
                VertxManager.getVertx().eventBus().consumer(AuthConstants.EventBusAddress.UPDATE_AUTHORIZE_INFO_ADDRESS, authorizeInfoUpdater);
            }
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String clientId = DynamicPropertyUtils.getString("auth.center.client-id");
        String adminUsername = DynamicPropertyUtils.getString("admin.username");
        if (StringUtils.equals(username, adminUsername)) {
            return loginAdmin(username, clientId);
        } else {
            JsonResult<UserVO> result = userServerFeign.findByUsername(username, clientId);

            if (!result.requestSuccess()) {
                throw new UsernameNotFoundException("用户名或密码不正确");
            }

            UserVO userEntity = result.getData();

            UserDetailsImpl userDetails = new UserDetailsImpl();
            userDetails.setId(userEntity.getId());
            userDetails.setUsername(userEntity.getUsername());
            userDetails.setPassword(userEntity.getPassword());
            userDetails.setClientId(clientId);
            String customizeInfo = userEntity.getCustomizeInfo();
            if (StringUtils.isNotBlank(customizeInfo)) {
                userDetails.setCustomerInfo(JSON.parseObject(customizeInfo, CustomerInfo.class));
            }

            List<GrantedAuthority> authorities = resourceRepository.findAllResource(userEntity.getId()).stream()
                    .map(r -> new SimpleGrantedAuthority(r.getResourceServerName() + ":" + r.getScope()))
                    .collect(Collectors.toList());

            userDetails.setAuthorities(authorities);
            return userDetails;
        }
    }

    private UserDetails loginAdmin(String admin, String clientId) {
        String password = DynamicPropertyUtils.getString("admin.password");
        String introduction = DynamicPropertyUtils.getString("admin.introduction");
        String avatar = DynamicPropertyUtils.getString("admin.avatar");
        UserDetailsImpl userDetails = new UserDetailsImpl();
        userDetails.setId(0L);
        userDetails.setUsername(admin);
        userDetails.setPassword(passwordEncoder.encode(password));
        userDetails.setClientId(clientId);
        userDetails.setCustomerInfo(new CustomerInfo());
        userDetails.getCustomerInfo().put("introduction", introduction);
        userDetails.getCustomerInfo().put("avatar", avatar);
        // 拥有所有的权限
        userDetails.setAuthorities(resourceRepository.findAll().stream()
                .map(r -> new SimpleGrantedAuthority(r.fullScopeName()))
                .collect(Collectors.toList()));
        return userDetails;
    }

    @Override
    public UserInfoDTO login(UserLoginDTO dto) {
        String username = DynamicPropertyUtils.getString("admin.username");
        if (StringUtils.equals(username, dto.getUsername())) {
            return loginWithAdmin(dto);
        } else {
            JsonResult<UserVO> result = userServerFeign.findByUsername(dto.getUsername(), dto.getClientId());
            if (!result.requestSuccess()) {
                throw new SecurityException(SecurityError.LOGIN_FAIL);
            }

            UserVO userEntity = result.getData();

            // 检查密码是否正确
            boolean passwordMatches = passwordEncoder.matches(dto.getPassword(), userEntity.getPassword());
            if (!passwordMatches) {
                throw new SecurityException(SecurityError.LOGIN_FAIL);
            }

            // 校验client和user的关系是否正确
            if (StringUtils.isNotBlank(dto.getClientId())) {
                ClientEntity clientEntity = clientRepository.findByClientId(dto.getClientId());
                if (clientEntity == null) {
                    throw new SecurityException(SecurityError.LOGIN_FAIL);
                }

                Boolean existsByUserIdAndClientId = userClientRelationRepository.existsByUserIdAndClientId(userEntity.getId(), clientEntity.getId());
                if (!existsByUserIdAndClientId) {
                    throw new SecurityException(SecurityError.LOGIN_FAIL);
                }
            }

            String customizeInfo = userEntity.getCustomizeInfo();
            JSONObject jsonObject = StringUtils.isNotBlank(customizeInfo) ?
                    JSON.parseObject(customizeInfo) : new JSONObject();

            if (jsonObject.containsKey(AuthConstants.IS_ENABLED)) {
                if (!jsonObject.getBoolean(AuthConstants.IS_ENABLED)) {
                    throw new SecurityException(SecurityError.USER_DISABLED);
                }
            }

            UserRoleEntity userRoleEntity = userRoleRepository.findByUserId(userEntity.getId());
            CertificationImpl certification = new CertificationImpl(userEntity.getId(),
                    userRoleEntity != null ? userRoleEntity.getRoleId() : null, userEntity.getUsername(), dto.getClientId(), false);

            String content = JSON.toJSONString(certification);
            String token = JwtUtils.generateToken(content, privateKey, securityProperties.getTimeout());

            UserInfoDTO userInfoDTO = new UserInfoDTO();
            userInfoDTO.setToken(token);
            userInfoDTO.setName(userEntity.getUsername());

            // 设置描述
            userInfoDTO.setIntroduction(jsonObject.getString("introduction"));
            // 设置角色
            List<ResourceEntity> allResource = resourceRepository.findAllResource(userEntity.getId());
            if (CollectionUtils.isNotEmpty(allResource)) {
                userInfoDTO.setRoles(allResource.stream().map(ResourceEntity::fullScopeName).collect(Collectors.toList()));
            } else {
                userInfoDTO.setRoles(Lists.newArrayList("123456"));
            }
            // 设置头像
            userInfoDTO.setAvatar(jsonObject.containsKey("avatar") ? jsonObject.getString("avatar") : "https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif");
            userInfoDTO.setId(userEntity.getId());
            authorizeInfoManager.save(userEntity.getId(), token, securityProperties.getTimeout(), JSON.parseObject(customizeInfo, CustomerInfo.class), Lists.newArrayList());
            return userInfoDTO;
        }
    }

    private UserInfoDTO loginWithAdmin(UserLoginDTO dto) {
        String password = DynamicPropertyUtils.getString("admin.password");
        String introduction = DynamicPropertyUtils.getString("admin.introduction");
        String avatar = DynamicPropertyUtils.getString("admin.avatar");

        if (!StringUtils.equals(password, dto.getPassword())) {
            throw new SecurityException(SecurityError.LOGIN_FAIL);
        }

        CertificationImpl certification = new CertificationImpl(0L, null, dto.getUsername(), dto.getClientId(), false);
        String content = JSON.toJSONString(certification);
        String token = JwtUtils.generateToken(content, privateKey, securityProperties.getTimeout());

        UserInfoDTO userInfoDTO = new UserInfoDTO();
        userInfoDTO.setToken(token);
        userInfoDTO.setName(dto.getUsername());
        userInfoDTO.setAvatar(avatar);
        userInfoDTO.setIntroduction(introduction);
        userInfoDTO.setRoles(resourceRepository.findAll().stream().map(ResourceEntity::fullScopeName).collect(Collectors.toList()));
        if (CollectionUtils.isEmpty(userInfoDTO.getRoles())) {
            userInfoDTO.getRoles().add("123456");
        }
        userInfoDTO.setId(0L);
        authorizeInfoManager.save(userInfoDTO.getId(), token, securityProperties.getTimeout(), new CustomerInfo(), userInfoDTO.getRoles());
        return userInfoDTO;
    }

    @Override
    public Boolean logout(HttpServletRequest request) {
        String token = request.getHeader(TOKEN_HEADER);
        return StringUtils.isNotBlank(token);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean create(UserVO vo) {
        // 密码加密
        vo.setPassword(passwordEncoder.encode(vo.getPassword()));
        if (StringUtils.isBlank(vo.getClient())) {
            vo.setClient(AuthConstants.VEA);
        }
        JsonResult<UserVO> result = userServerFeign.create(vo);

        if (result.requestSuccess()) {
            if (vo.getRoleId() != null) {
                SetUserRoleDTO setUserRoleDTO = new SetUserRoleDTO();
                setUserRoleDTO.setUserId(result.getData().getId());
                setUserRoleDTO.setRoleId(vo.getRoleId());
                setUserRole(setUserRoleDTO);
            }
        }

        return result.requestSuccess();
    }

    @Override
    public UserDetailsImpl findById(Long id) {
        Optional<UserEntity> optional = userRepository.findById(id);
        return optional.map(userEntity -> {
            UserDetailsImpl userDetails = new UserDetailsImpl();
            userDetails.setId(userEntity.getId());
            userDetails.setUsername(userEntity.getUsername());
            userDetails.setPassword(userEntity.getPassword());
            userDetails.setAuthorities(Collections.singletonList(new SimpleGrantedAuthority("MoneyManagement:Test:checkAccessCard")));
            return userDetails;
        }).orElse(null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean setUserRole(SetUserRoleDTO dto) {
        JsonResult<Boolean> result = userServerFeign.userIsExisted(dto.getUserId());
        if (!result.requestSuccess()) {
            return false;
        }
        ThrowUtils.throwIfTrue(!result.getData(), SecurityError.USER_NOT_FOUND);

        boolean existsById = roleRepository.existsById(dto.getRoleId());
        if (!existsById) {
            return false;
        }

        UserRoleEntity entity = new UserRoleEntity();
        entity.setUserId(dto.getUserId());
        entity.setRoleId(dto.getRoleId());

        userRoleRepository.save(entity);
        return true;
    }

    @Override
    public Long findUserRoleId(Long userId) {
        UserRoleEntity userRoleEntity = userRoleRepository.findByUserId(userId);
        return userRoleEntity == null ? null : userRoleEntity.getRoleId();
    }

    @Override
    public Boolean isEnabled(Long userId) {
        if (userId == 0) {
            return true;
        }
        JsonResult<Object> value = userServerFeign.findValue(userId, AuthConstants.VEA, IS_ENABLED);
        return value.getData() != null ? (Boolean) value.getData() : true;
    }
}
