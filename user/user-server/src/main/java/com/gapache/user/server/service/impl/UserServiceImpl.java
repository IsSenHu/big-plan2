package com.gapache.user.server.service.impl;

import com.gapache.commons.model.ThrowUtils;
import com.gapache.user.common.model.UserError;
import com.gapache.user.server.dao.entity.UserCustomizeInfoEntity;
import com.gapache.user.server.dao.entity.UserEntity;
import com.gapache.user.server.dao.repository.UserCustomizeInfoRepository;
import com.gapache.user.server.dao.repository.UserRepository;
import com.gapache.user.common.model.vo.UserVO;
import com.gapache.user.server.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @author HuSen
 * @since 2021/1/25 1:08 下午
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserCustomizeInfoRepository userCustomizeInfoRepository;

    public UserServiceImpl(UserRepository userRepository, UserCustomizeInfoRepository userCustomizeInfoRepository) {
        this.userRepository = userRepository;
        this.userCustomizeInfoRepository = userCustomizeInfoRepository;
    }

    @Override
    public UserVO create(UserVO vo) {
        ThrowUtils.throwIfTrue(userRepository.existsByUsername(vo.getUsername()), UserError.USERNAME_EXISTED);

        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(vo.getUsername());
        userEntity.setPassword(vo.getPassword());

        userRepository.save(userEntity);

        String customizeInfo = vo.getCustomizeInfo();
        if (StringUtils.isNotBlank(customizeInfo)) {
            UserCustomizeInfoEntity userCustomizeInfoEntity = new UserCustomizeInfoEntity();
            userCustomizeInfoEntity.setInfo(customizeInfo);
            userCustomizeInfoEntity.setClientId(vo.getClient());
            userCustomizeInfoEntity.setUserId(userEntity.getId());
            userCustomizeInfoRepository.save(userCustomizeInfoEntity);
        }

        vo.setId(userEntity.getId());
        vo.setCreateTime(userEntity.getCreateTime());
        vo.setCreateBy(userEntity.getCreateBy());
        return vo;
    }

    @Override
    public Boolean userIsExisted(Long id) {
        return userRepository.existsById(id);
    }

    @Override
    public UserVO findByUsername(String username, String clientId) {
        UserEntity userEntity = userRepository.findByUsername(username);
        ThrowUtils.throwIfTrue(userEntity == null, UserError.USER_NOT_FOUND);

        UserVO vo = new UserVO();
        vo.setUsername(userEntity.getUsername());
        vo.setPassword(userEntity.getPassword());
        vo.setId(userEntity.getId());
        vo.setClient(clientId);
        vo.setCreateTime(userEntity.getCreateTime());
        vo.setCreateBy(userEntity.getCreateBy());
        vo.setLastModifiedBy(userEntity.getLastModifiedBy());
        vo.setLastModifiedTime(userEntity.getLastModifiedTime());

        if (StringUtils.isNotBlank(clientId)) {
            UserCustomizeInfoEntity userCustomizeInfoEntity = userCustomizeInfoRepository.findByUserIdAndClientId(vo.getId(), clientId);
            if (userCustomizeInfoEntity != null) {
                vo.setCustomizeInfo(userCustomizeInfoEntity.getInfo());
            }
        }
        return vo;
    }
}
