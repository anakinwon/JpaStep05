package study.querydsl.entity;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MemberTest {

    @Autowired EntityManager em;
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

        Member member1 = new Member("Anakin", 28 , teamA);
        Member member2 = new Member("Padme" , 32 , teamA);
        Member member3 = new Member("Obiwan", 83 , teamB);
        Member member4 = new Member("Yoda"  , 224, teamB);
        Member member5 = new Member("Qwigon",125, teamC);
        Member member6 = new Member("AsoKa" ,22, teamC);
        em.persist(new Member("ATEAM", 120, teamA));
        em.persist(new Member("BTEAM", 130, teamB));


        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
        em.persist(member5);
        em.persist(member6);

        // 영속성컨텍스트에 있는 쿼리를 즉시 수행한다.
        em.flush();
        // 저장 후 영속성컨텍스트에 있는 캐시 삭제
        em.clear();

    }

    @Test
    @Rollback(value = false)
    public void testEntity() {

        // "Anakin" 찾는 SELECT 쿼리
        Member findMember = em.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username", "Anakin")
                .getSingleResult();

        // 결과 검증
        assertThat(findMember.getUsername()).isEqualTo("Anakin");

    }
}