package com.mwitter.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mwitter.dto.MentionResponse;
import com.mwitter.model.User;
import com.mwitter.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class MentionServiceTests {

    @Mock
    private UserRepository userRepository;

    private MentionService mentionService;

    @BeforeEach
    void setUp() {
        mentionService = new MentionService(userRepository);
    }

    @Test
    void returnsExistingUsersAndRemovesDuplicateMentions() {
        User user = new User();
        user.setId("user-1");
        user.setUsername("Ayse");
        when(userRepository.findByUsernameIgnoreCase("Ayse")).thenReturn(Optional.of(user));

        List<MentionResponse> mentions =
                mentionService.findValidMentions("Merhaba @Ayse, tekrar @ayse!");

        assertThat(mentions).containsExactly(new MentionResponse("user-1", "Ayse"));
    }

    @Test
    void ignoresUnknownUsersAndEmailAddresses() {
        when(userRepository.findByUsernameIgnoreCase("bilinmeyen")).thenReturn(Optional.empty());

        List<MentionResponse> mentions = mentionService.findValidMentions(
                "mail test@example.com ve @bilinmeyen"
        );

        assertThat(mentions).isEmpty();
    }
}
