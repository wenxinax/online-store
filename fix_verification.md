# 用户登录次数逻辑修复总结

## 发现的问题

1. **[getRemainingAttempts](file:///Users/luxwxin/myspace/workspace/online-store/src/main/java/com/example/onlinestore/service/impl/LoginSecurityServiceImpl.java#L77-L83)方法逻辑缺陷**
   - 当用户已被锁定时，该方法仍返回基于失败次数计算的剩余尝试次数
   - 正确行为：被锁定的用户剩余尝试次数应该为0

2. **异常处理不当**
   - 在[UserServiceImpl.login](file:///Users/luxwxin/myspace/workspace/online-store/src/main/java/com/example/onlinestore/service/impl/UserServiceImpl.java#L66-L142)中捕获所有异常并记录为登录失败
   - 系统异常不应该被当作登录失败

3. **密码验证缺失**
   - 非管理员用户缺少密码验证逻辑
   - 所有存在的用户都能登录成功，存在严重安全漏洞

## 修复方案

### 1. 修复[getRemainingAttempts](file:///Users/luxwxin/myspace/workspace/online-store/src/main/java/com/example/onlinestore/service/impl/LoginSecurityServiceImpl.java#L77-L83)方法

```java
@Override
public int getRemainingAttempts(String username) {
    // 如果用户已被锁定，则剩余尝试次数为0
    if (isLocked(USER_LOCK_KEY_PREFIX + hashUsername(username))) {
        return 0;
    }
    
    String key = USER_FAIL_KEY_PREFIX + hashUsername(username);
    String countStr = redisTemplate.opsForValue().get(key);
    int currentFailures = countStr != null ? Integer.parseInt(countStr) : 0;
    return Math.max(0, securityProperties.getMaxAttempts() - currentFailures);
}
```

### 2. 修复异常处理逻辑

```java
} catch (UserLockedException e) {
    // 重新抛出锁定异常，不记录为登录失败
    throw e;
} catch (IllegalArgumentException e) {
    // 认证失败的异常，已在上面的逻辑中记录过了
    throw e;
} catch (Exception e) {
    // 系统异常，不应记录为登录失败
    logger.error("登录过程中发生系统异常", e);
    throw new RuntimeException("系统异常，请稍后再试");
}
```

### 3. 添加密码验证功能

**更新[User模型](file:///Users/luxwxin/myspace/workspace/online-store/src/main/java/com/example/onlinestore/model/User.java)**：
- 添加了`password`字段
- 添加了相应的getter和setter方法

**更新数据库schema**：
```sql
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255),  -- 新增字段
    token VARCHAR(100),
    token_expire_time DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

**在[UserServiceImpl](file:///Users/luxwxin/myspace/workspace/online-store/src/main/java/com/example/onlinestore/service/impl/UserServiceImpl.java)中添加密码验证**：
```java
// 验证密码
if (!passwordService.matches(request.getPassword(), user.getPassword())) {
    logger.warn("用户密码错误: {}", request.getUsername());
    loginSecurityService.recordLoginFailure(request.getUsername(), clientIp);
    
    int remainingAttempts = loginSecurityService.getRemainingAttempts(request.getUsername());
    String baseMessage = messageSource.getMessage(
        "error.invalid.credentials", null, LocaleContextHolder.getLocale());
    String attemptsMessage = messageSource.getMessage(
        "security.remaining.attempts", 
        new Object[]{remainingAttempts}, 
        LocaleContextHolder.getLocale());
    
    throw new IllegalArgumentException(baseMessage + ". " + attemptsMessage);
}
```

### 4. 添加测试用例

为被锁定用户的剩余尝试次数添加了测试：
```java
@Test
void testGetRemainingAttempts_UserLocked_ShouldReturnZero() {
    // Arrange
    String username = "testuser";
    when(redisTemplate.hasKey(contains("lock:user"))).thenReturn(true);

    // Act
    int result = loginSecurityService.getRemainingAttempts(username);

    // Assert
    assertEquals(0, result); // 被锁定的用户应该返回0
}
```

## 修复后的行为

1. **锁定状态检查**：被锁定的用户获取剩余尝试次数时会正确返回0
2. **异常处理优化**：只有真正的认证失败才会记录为登录失败，系统异常不会影响登录计数
3. **密码验证**：非管理员用户现在需要正确的密码才能登录
4. **安全性提升**：完整的登录安全机制，包括密码验证和失败次数控制

## 技术改进

- 添加了Spring Security依赖和BCrypt密码编码器
- 使用[PasswordService](file:///Users/luxwxin/myspace/workspace/online-store/src/main/java/com/example/onlinestore/service/PasswordService.java)进行密码加密和验证
- 保持向后兼容性，管理员登录逻辑保持不变
- 增强的错误消息，包含剩余尝试次数信息

这些修复确保了登录安全机制的完整性和正确性。