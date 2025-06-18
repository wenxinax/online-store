package com.example.onlinestore.service.impl;

import com.example.onlinestore.bean.Member;
import com.example.onlinestore.dto.LoginRequest;
import com.example.onlinestore.dto.LoginResponse;
import com.example.onlinestore.dto.MemberRegistryRequest;
import com.example.onlinestore.entity.MemberEntity;
import com.example.onlinestore.enums.Gender;
import com.example.onlinestore.errors.ErrorCode;
import com.example.onlinestore.exceptions.BizException;
import com.example.onlinestore.mapper.MemberMapper;
import com.example.onlinestore.security.JwtTokenUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberServiceImpl Tests")
class MemberServiceImplTest {

    @Mock
    private MemberMapper memberMapper;

    @Mock
    private MessageSource messageSource;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenUtil jwtTokenUtil;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private MemberServiceImpl memberService;

    private MemberEntity testMemberEntity;
    private Member testMember;
    private LoginRequest validLoginRequest;
    private MemberRegistryRequest validRegistryRequest;

    @BeforeEach
    void setUp() {
        testMemberEntity = createTestMemberEntity();
        testMember = createTestMember();
        validLoginRequest = createValidLoginRequest();
        validRegistryRequest = createValidRegistryRequest();
    }

    private MemberEntity createTestMemberEntity() {
        MemberEntity entity = new MemberEntity();
        entity.setId(1L);
        entity.setName("testuser");
        entity.setPassword("encodedPassword");
        entity.setNickName("Test User");
        entity.setPhone("1234567890");
        entity.setGender("MALE");
        entity.setAge(25);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        return entity;
    }

    private Member createTestMember() {
        Member member = new Member();
        member.setId(1L);
        member.setName("testuser");
        member.setNickName("Test User");
        member.setPhone("1234567890");
        member.setAge(25);
        return member;
    }

    private LoginRequest createValidLoginRequest() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        return request;
    }

    private MemberRegistryRequest createValidRegistryRequest() {
        MemberRegistryRequest request = new MemberRegistryRequest();
        request.setName("newuser");
        request.setPassword("password123");
        request.setNickName("New User");
        request.setPhone("9876543210");
        request.setGender(Gender.MALE);
        request.setAge(30);
        return request;
    }

    @Test
    @DisplayName("Should successfully login with valid credentials")
    void shouldSuccessfullyLoginWithValidCredentials() {
        String expectedToken = "jwt.token.here";
        when(memberMapper.findByName(validLoginRequest.getUsername())).thenReturn(testMemberEntity);
        when(passwordEncoder.matches(validLoginRequest.getPassword(), testMemberEntity.getPassword())).thenReturn(true);
        when(jwtTokenUtil.generateToken(any(User.class))).thenReturn(expectedToken);

        LoginResponse response = memberService.login(validLoginRequest);

        assertNotNull(response);
        assertEquals(expectedToken, response.getToken());
        verify(memberMapper).findByName(validLoginRequest.getUsername());
        verify(passwordEncoder).matches(validLoginRequest.getPassword(), testMemberEntity.getPassword());
        verify(jwtTokenUtil).generateToken(any(User.class));
    }

    @Test
    @DisplayName("Should throw BizException when user not found")
    void shouldThrowBizExceptionWhenUserNotFound() {
        when(memberMapper.findByName(validLoginRequest.getUsername())).thenReturn(null);

        BizException exception = assertThrows(BizException.class,
            () -> memberService.login(validLoginRequest));
        assertEquals(ErrorCode.MEMBER_PASSWORD_INCORRECT, exception.getErrorCode());
        verify(memberMapper).findByName(validLoginRequest.getUsername());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtTokenUtil, never()).generateToken(any());
    }

    @Test
    @DisplayName("Should throw BizException when password is incorrect")
    void shouldThrowBizExceptionWhenPasswordIncorrect() {
        when(memberMapper.findByName(validLoginRequest.getUsername())).thenReturn(testMemberEntity);
        when(passwordEncoder.matches(validLoginRequest.getPassword(), testMemberEntity.getPassword())).thenReturn(false);

        BizException exception = assertThrows(BizException.class,
            () -> memberService.login(validLoginRequest));
        assertEquals(ErrorCode.MEMBER_PASSWORD_INCORRECT, exception.getErrorCode());
        verify(memberMapper).findByName(validLoginRequest.getUsername());
        verify(passwordEncoder).matches(validLoginRequest.getPassword(), testMemberEntity.getPassword());
        verify(jwtTokenUtil, never()).generateToken(any());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  "})
    @DisplayName("Should handle edge cases for username")
    void shouldHandleUsernameEdgeCases(String username) {
        validLoginRequest.setUsername(username);
        when(memberMapper.findByName(username)).thenReturn(null);

        assertThrows(BizException.class, () -> memberService.login(validLoginRequest));
    }

    @Test
    @DisplayName("Should successfully register new member")
    void shouldSuccessfullyRegisterNewMember() {
        when(memberMapper.findByName(validRegistryRequest.getName())).thenReturn(null);
        when(passwordEncoder.encode(validRegistryRequest.getPassword())).thenReturn("encodedPassword");
        when(memberMapper.insertMember(any(MemberEntity.class))).thenReturn(1);

        Member result = memberService.registry(validRegistryRequest);

        assertNotNull(result);
        assertEquals(validRegistryRequest.getName(), result.getName());
        assertEquals(validRegistryRequest.getNickName(), result.getNickName());
        assertEquals(validRegistryRequest.getPhone(), result.getPhone());
        assertEquals(validRegistryRequest.getAge(), result.getAge());
        verify(memberMapper).findByName(validRegistryRequest.getName());
        verify(passwordEncoder).encode(validRegistryRequest.getPassword());
        verify(memberMapper).insertMember(any(MemberEntity.class));
    }

    @Test
    @DisplayName("Should throw BizException when member already exists")
    void shouldThrowBizExceptionWhenMemberAlreadyExists() {
        when(memberMapper.findByName(validRegistryRequest.getName())).thenReturn(testMemberEntity);

        BizException exception = assertThrows(BizException.class,
            () -> memberService.registry(validRegistryRequest));
        assertEquals(ErrorCode.MEMBER_EXISTED, exception.getErrorCode());
        assertEquals(validRegistryRequest.getName(), exception.getArgs()[0]);
        verify(memberMapper).findByName(validRegistryRequest.getName());
        verify(passwordEncoder, never()).encode(anyString());
        verify(memberMapper, never()).insertMember(any());
    }

    @Test
    @DisplayName("Should throw BizException when database insert fails")
    void shouldThrowBizExceptionWhenDatabaseInsertFails() {
        when(memberMapper.findByName(validRegistryRequest.getName())).thenReturn(null);
        when(passwordEncoder.encode(validRegistryRequest.getPassword())).thenReturn("encodedPassword");
        when(memberMapper.insertMember(any(MemberEntity.class))).thenReturn(0);

        BizException exception = assertThrows(BizException.class,
            () -> memberService.registry(validRegistryRequest));
        assertEquals(ErrorCode.INTERNAL_SERVER_ERROR, exception.getErrorCode());
        verify(memberMapper).findByName(validRegistryRequest.getName());
        verify(passwordEncoder).encode(validRegistryRequest.getPassword());
        verify(memberMapper).insertMember(any(MemberEntity.class));
    }

    @Test
    @DisplayName("Should handle concurrent registration attempts")
    void shouldHandleConcurrentRegistrationAttempts() {
        when(memberMapper.findByName(validRegistryRequest.getName()))
            .thenReturn(null)
            .thenReturn(testMemberEntity);
        when(passwordEncoder.encode(validRegistryRequest.getPassword())).thenReturn("encodedPassword");

        BizException exception = assertThrows(BizException.class,
            () -> memberService.registry(validRegistryRequest));
        assertEquals(ErrorCode.MEMBER_EXISTED, exception.getErrorCode());
    }

    @Test
    @DisplayName("Should return member when found by ID")
    void shouldReturnMemberWhenFoundById() {
        Long memberId = 1L;
        when(memberMapper.findById(memberId)).thenReturn(testMemberEntity);

        Member result = memberService.getMemberById(memberId);

        assertNotNull(result);
        assertEquals(testMemberEntity.getId(), result.getId());
        assertEquals(testMemberEntity.getName(), result.getName());
        verify(memberMapper).findById(memberId);
    }

    @Test
    @DisplayName("Should throw BizException when member not found by ID")
    void shouldThrowBizExceptionWhenMemberNotFoundById() {
        Long memberId = 999L;
        when(memberMapper.findById(memberId)).thenReturn(null);

        BizException exception = assertThrows(BizException.class,
            () -> memberService.getMemberById(memberId));
        assertEquals(ErrorCode.MEMBER_NOT_FOUND, exception.getErrorCode());
        assertEquals(memberId, exception.getArgs()[0]);
        verify(memberMapper).findById(memberId);
    }

    @ParameterizedTest
    @ValueSource(longs = {-1L, 0L})
    @DisplayName("Should handle invalid member IDs")
    void shouldHandleInvalidMemberIds(Long invalidId) {
        when(memberMapper.findById(invalidId)).thenReturn(null);
        assertThrows(BizException.class, () -> memberService.getMemberById(invalidId));
    }

    @Test
    @DisplayName("Should throw exception when ID is null")
    void shouldThrowExceptionWhenIdIsNull() {
        assertThrows(Exception.class, () -> memberService.getMemberById(null));
    }

    @Test
    @DisplayName("Should return member when found by name")
    void shouldReturnMemberWhenFoundByName() {
        String memberName = "testuser";
        when(memberMapper.findByName(memberName)).thenReturn(testMemberEntity);

        Member result = memberService.getMemberByName(memberName);

        assertNotNull(result);
        assertEquals(testMemberEntity.getName(), result.getName());
        verify(memberMapper).findByName(memberName);
    }

    @Test
    @DisplayName("Should return null when member not found by name")
    void shouldReturnNullWhenMemberNotFoundByName() {
        String memberName = "nonexistent";
        when(memberMapper.findByName(memberName)).thenReturn(null);

        Member result = memberService.getMemberByName(memberName);

        assertNull(result);
        verify(memberMapper).findByName(memberName);
    }

    @Test
    @DisplayName("Should trim whitespace from member name")
    void shouldTrimWhitespaceFromMemberName() {
        String nameWithSpaces = "  testuser  ";
        String trimmedName = "testuser";
        when(memberMapper.findByName(trimmedName)).thenReturn(testMemberEntity);

        Member result = memberService.getMemberByName(nameWithSpaces);

        assertNotNull(result);
        verify(memberMapper).findByName(trimmedName);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  ", "\t", "\n"})
    @DisplayName("Should handle edge cases for member name")
    void shouldHandleMemberNameEdgeCases(String name) {
        String trimmedName = name == null ? null : name.trim();
        when(memberMapper.findByName(trimmedName)).thenReturn(null);

        Member result = memberService.getMemberByName(name);

        assertNull(result);
    }

    @Test
    @DisplayName("Should return logged in member when authenticated")
    void shouldReturnLoggedInMemberWhenAuthenticated() {
        String username = "testuser";
        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            when(SecurityContextHolder.getContext()).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn(username);
            when(memberMapper.findByName(username)).thenReturn(testMemberEntity);

            Member result = memberService.getLoginMember();

            assertNotNull(result);
            assertEquals(testMemberEntity.getName(), result.getName());
            verify(memberMapper).findByName(username);
        }
    }

    @Test
    @DisplayName("Should throw BizException when user is anonymous")
    void shouldThrowBizExceptionWhenUserIsAnonymous() {
        AnonymousAuthenticationToken anonymousAuth = mock(AnonymousAuthenticationToken.class);
        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            when(SecurityContextHolder.getContext()).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(anonymousAuth);

            BizException exception = assertThrows(BizException.class,
                () -> memberService.getLoginMember());
            assertEquals(ErrorCode.MEMBER_NOT_LOGIN, exception.getErrorCode());
        }
    }

    @Test
    @DisplayName("Should throw BizException when username is blank")
    void shouldThrowBizExceptionWhenUsernameIsBlank() {
        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            when(SecurityContextHolder.getContext()).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn("");

            BizException exception = assertThrows(BizException.class,
                () -> memberService.getLoginMember());
            assertEquals(ErrorCode.MEMBER_NOT_LOGIN, exception.getErrorCode());
        }
    }

    @Test
    @DisplayName("Should throw BizException when authenticated user not found in database")
    void shouldThrowBizExceptionWhenAuthenticatedUserNotFoundInDatabase() {
        String username = "deleteduser";
        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            when(SecurityContextHolder.getContext()).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn(username);
            when(memberMapper.findByName(username)).thenReturn(null);

            BizException exception = assertThrows(BizException.class,
                () -> memberService.getLoginMember());
            assertEquals(ErrorCode.MEMBER_NOT_LOGIN, exception.getErrorCode());
            verify(memberMapper).findByName(username);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "  ", "\t", "\n"})
    @DisplayName("Should throw BizException for blank usernames")
    void shouldThrowBizExceptionForBlankUsernames(String blankUsername) {
        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            when(SecurityContextHolder.getContext()).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn(blankUsername);

            assertThrows(BizException.class, () -> memberService.getLoginMember());
        }
    }

    @Test
    @DisplayName("Should handle null authentication gracefully")
    void shouldHandleNullAuthenticationGracefully() {
        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            when(SecurityContextHolder.getContext()).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(null);

            BizException exception = assertThrows(BizException.class,
                () -> memberService.getLoginMember());
            assertEquals(ErrorCode.MEMBER_NOT_LOGIN, exception.getErrorCode());
        }
    }

    @Test
    @DisplayName("Should verify password encoding during registration")
    void shouldVerifyPasswordEncodingDuringRegistration() {
        String rawPassword = "plainPassword";
        String encodedPassword = "encodedPassword";
        validRegistryRequest.setPassword(rawPassword);

        when(memberMapper.findByName(validRegistryRequest.getName())).thenReturn(null);
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(memberMapper.insertMember(any(MemberEntity.class))).thenReturn(1);

        memberService.registry(validRegistryRequest);

        verify(passwordEncoder).encode(rawPassword);
        verify(memberMapper).insertMember(argThat(entity ->
            encodedPassword.equals(entity.getPassword())));
    }

    @Test
    @DisplayName("Should set creation and update timestamps during registration")
    void shouldSetTimestampsDuringRegistration() {
        when(memberMapper.findByName(validRegistryRequest.getName())).thenReturn(null);
        when(passwordEncoder.encode(validRegistryRequest.getPassword())).thenReturn("encoded");
        when(memberMapper.insertMember(any(MemberEntity.class))).thenReturn(1);

        LocalDateTime beforeRegistration = LocalDateTime.now();
        memberService.registry(validRegistryRequest);

        verify(memberMapper).insertMember(argThat(entity -> {
            LocalDateTime afterRegistration = LocalDateTime.now();
            return entity.getCreatedAt() != null &&
                   entity.getUpdatedAt() != null &&
                   !entity.getCreatedAt().isBefore(beforeRegistration) &&
                   !entity.getCreatedAt().isAfter(afterRegistration) &&
                   entity.getCreatedAt().equals(entity.getUpdatedAt());
        }));
    }

    @Test
    @DisplayName("Should verify JWT token generation with correct user details")
    void shouldVerifyJwtTokenGenerationWithCorrectUserDetails() {
        String expectedToken = "generated.jwt.token";
        when(memberMapper.findByName(validLoginRequest.getUsername())).thenReturn(testMemberEntity);
        when(passwordEncoder.matches(validLoginRequest.getPassword(), testMemberEntity.getPassword())).thenReturn(true);
        when(jwtTokenUtil.generateToken(any(User.class))).thenReturn(expectedToken);

        LoginResponse response = memberService.login(validLoginRequest);

        assertEquals(expectedToken, response.getToken());
        verify(jwtTokenUtil).generateToken(argThat(user ->
            testMemberEntity.getName().equals(user.getUsername()) &&
            testMemberEntity.getPassword().equals(user.getPassword()) &&
            user.getAuthorities() != null));
    }
}