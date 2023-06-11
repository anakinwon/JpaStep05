package study.querydsl.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback(value = false)
class MemberTestRepositoryTest {

    @Autowired
    EntityManager em;
    @Autowired MemberTestRepository memberTestRepository;
    JPAQueryFactory queryFactory;

    @BeforeEach
    public void before() {
        queryFactory = new JPAQueryFactory(em);

        // Given
        Team teamA = new Team("ATEAM");
        Team teamB = new Team("BTEAM");
        Team teamC = new Team("CTEAM");

        em.persist(teamA);
        em.persist(teamB);
        em.persist(teamC);

        Member member1 = new Member("Yoda"  , 224, teamB);
        Member member2 = new Member("Qwigon",125, teamC);
        Member member3 = new Member("Obiwan", 83 , teamB);
        Member member4 = new Member("Anakin", 28 , teamA);
        Member member5 = new Member("AsoKa" ,22, teamC);
        Member member6 = new Member("Padme" , 32 , teamA);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
        em.persist(member5);
        em.persist(member6);

        // 회원이름과 팀이름이 같은 사람 생성.
        em.persist(new Member("ATEAM", 120, teamA));
        em.persist(new Member("BTEAM", 130, teamB));
        em.persist(new Member("CTEAM", 140, teamC));

        // 영속성컨텍스트에 있는 쿼리를 즉시 수행한다.
        em.flush();
        // 저장 후 영속성컨텍스트에 있는 캐시 삭제
        em.clear();

    }


    @Test
    void basicSelect() {
        List<Member> members = memberTestRepository.basicSelect();
        for (Member member : members) {
            System.out.println(" \t 01. basicSelect = " + member);
        }
    }

    @Test
    void basicSelectFrom() {
        List<Member> members = memberTestRepository.basicSelectFrom();
        for (Member member : members) {
            System.out.println(" \t 02. basicSelectFrom = " + member);
        }
    }

    @Test
    void searchPageByApplyPage() {
        MemberSearchCondition condition = new MemberSearchCondition();
//        condition.setUsername(null);
//        condition.setTeamName("CTEAM");
        condition.setAgeGoe(20);
        condition.setAgeLoe(50);

        PageRequest pageRequest = PageRequest.of(0,2);

        Page<Member> members = memberTestRepository.searchPageByApplyPage(condition, pageRequest);
        for (Member member : members) {
            System.out.println(" \t 03. searchPageByApplyPage = " + member);
        }
    }

    @Test
    void applyPagination() {
        MemberSearchCondition condition = new MemberSearchCondition();
//        condition.setUsername(null);
//        condition.setTeamName("CTEAM");
        condition.setAgeGoe(20);
        condition.setAgeLoe(50);

        PageRequest pageRequest = PageRequest.of(0,2);

        Page<Member> members = memberTestRepository.applyPagination(condition, pageRequest);
        for (Member member : members) {
            System.out.println(" \t 04. applyPagination = " + member);
        }
    }

    @Test
    void applyPagination2() {
        MemberSearchCondition condition = new MemberSearchCondition();
//        condition.setUsername(null);
//        condition.setTeamName("CTEAM");
        condition.setAgeGoe(20);
        condition.setAgeLoe(50);

        PageRequest pageRequest = PageRequest.of(0,2);

        Page<Member> members = memberTestRepository.applyPagination2(condition, pageRequest);
        for (Member member : members) {
            System.out.println(" \t 05. applyPagination2 = " + member);
        }
    }
}