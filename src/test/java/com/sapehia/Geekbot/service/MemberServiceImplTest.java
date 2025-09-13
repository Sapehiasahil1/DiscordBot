package com.sapehia.Geekbot.service;

import com.sapehia.Geekbot.model.Member;
import com.sapehia.Geekbot.repository.MemberRepository;
import com.sapehia.Geekbot.service.impl.MemberServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
public class MemberServiceImplTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberServiceImpl memberServiceImpl;

    private Member member;
    private Member member2;

    @BeforeEach
    void  setUp() {
        member = new  Member();
        member.setDiscordUserId("discord-123");
        member.setUsername("testUser");

        member2 = new Member();
        member2.setDiscordUserId("discord-456");
        member2.setUsername("anotherUser");
    }

    @Test
    void testGetAllMembers(){
        when(memberRepository.findAll()).thenReturn(List.of(member, member2));

        List<Member> members = memberServiceImpl.getAllMembers();

        assertNotNull(members);
        assertEquals(2, members.size());
        verify(memberRepository, times(1)).findAll();
    }

    @Test
    void testGetMemberById_Found(){
        when( memberRepository.findById("discord-123")).thenReturn(Optional.of(member));

        Member foundMember = memberServiceImpl.getMemberById("discord-123");

        assertNotNull(foundMember);
        assertEquals("discord-123", foundMember.getDiscordUserId());
        verify(memberRepository, times(1)).findById("discord-123");
    }

    @Test
    void testAddMember(){
        when(memberRepository.save(member)).thenReturn(member);

        Member savedMember = memberServiceImpl.addMember(member);

        assertNotNull(savedMember);
        assertEquals(member.getDiscordUserId(), savedMember.getDiscordUserId());
        verify(memberRepository, times(1)).save(member);
    }

    @Test
    void testGetMemberByDiscordUserId(){
        when(memberRepository.findByDiscordUserId("discord-123")).thenReturn(Optional.of(member));

        Member foundMember = memberServiceImpl.getMemberByDiscordUserId("discord-123");

        assertNotNull(foundMember);
        assertEquals("discord-123", foundMember.getDiscordUserId());
        verify(memberRepository, times(1)).findByDiscordUserId("discord-123");
    }

    @Test
    void testGetMemberByDiscordUserId_NotFound(){
        when(memberRepository.findByDiscordUserId("nonexistent")).thenReturn(Optional.empty());

        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> memberServiceImpl.getMemberByDiscordUserId("nonexistent")
        );

        assertEquals("Member not found with discordUserId nonexistent", thrown.getMessage());
        verify(memberRepository, times(1)).findByDiscordUserId("nonexistent");
    }

    @Test
    void testGetOrCreateMember(){
        when(memberRepository.findByDiscordUserId("discord-123")).thenReturn(Optional.of(member));

        Member resultMember = memberServiceImpl.getOrCreateMember(
                "discord-123",
                "testUser");

        assertNotNull(resultMember);
        assertEquals("discord-123",  resultMember.getDiscordUserId());
        verify(memberRepository, times(1)).findByDiscordUserId("discord-123");
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    void testGetOrCreateMemberWhenNotFound(){
        when(memberRepository.findByDiscordUserId("discord-456")).thenReturn(Optional.empty());

        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> {
            Member newMember = invocation.getArgument(0);
            return newMember;
        });

        Member resultMember = memberServiceImpl.getOrCreateMember(
                "discord-456",
                "anotherUser");

        assertNotNull(resultMember);
        assertEquals("discord-456", resultMember.getDiscordUserId());
        verify(memberRepository, times(1)).findByDiscordUserId("discord-456");
        verify(memberRepository, times(1)).save(any(Member.class));
    }
}
