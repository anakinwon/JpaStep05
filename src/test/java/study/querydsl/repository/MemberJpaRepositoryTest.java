package study.querydsl.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;
import java.util.List;

@SpringBootTest
@Transactional
@Rollback(value = false)
class MemberJpaRepositoryTest {

    @Autowired EntityManager em;
    @Autowired MemberJpaRepository memberJpaRepository;

    JPAQueryFactory queryFactory;


    @BeforeEach
    public void before() {
        queryFactory = new JPAQueryFactory(em);

        // Given
        Team teamA = new Team("ATEAM");
        Team teamB = new Team("BTEAM");
        Team teamC = new Team("CTEAM");
//        Team teamD = new Team("DTEAM");

        em.persist(teamA);
        em.persist(teamB);
        em.persist(teamC);
//        em.persist(teamD);

        Member member1 = new Member("Yoda"  , 224, teamB);
        Member member2 = new Member("Qwigon",125, teamC);
        Member member3 = new Member("Obiwan", 83 , teamB);
        Member member4 = new Member("Anakin", 28 , teamA);
        Member member5 = new Member("AsoKa" ,22, teamC);
        Member member6 = new Member("Padme" , 32 , teamA);
//        Member member7 = new Member("Duke", 128, teamD);
//        Member member8 = new Member("Grievous", 92, teamD);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
        em.persist(member5);
        em.persist(member6);
//        em.persist(member7);
//        em.persist(member8);

        // 회원이름과 팀이름이 같은 사람 생성.
        em.persist(new Member("ATEAM", 120, teamA));
        em.persist(new Member("BTEAM", 130, teamB));
        em.persist(new Member("CTEAM", 140, teamC));
//        em.persist(new Member("DTEAM", 150, teamD));

        // 영속성컨텍스트에 있는 쿼리를 즉시 수행한다.
        em.flush();
        // 저장 후 영속성컨텍스트에 있는 캐시 삭제
        em.clear();

    }


    /**
     * 1. JPA 저장하기 & 조회하기 테스트
     *      : em.save()
     * */
    @Test
    void testBasic() {

        Team dteam = new Team("DTEAM");
        em.persist(dteam);

        // 1. 저장 테스트
        Member member  = new Member("Duke", 128, dteam);
        Member member2 = new Member("Grievous", 92, dteam);
        memberJpaRepository.save(member);
        memberJpaRepository.save(member2);

        // 2. PK ID조회 테스트
        Member findMember = memberJpaRepository.findById(member.getId()).get();
        Assertions.assertThat(findMember).isEqualTo(member);
        System.out.println("\t2. findMember = " + findMember);

        // 3.1 전체 조회 테스트
        List<Member> findAll = memberJpaRepository.findAll();
//        Assertions.assertThat(findAll.get(0).getUsername()).isEqualTo(member.getUsername());
        System.out.println("\t\t3.1 findAll = " + findAll);

        // 3.2 전체 조회 테스트 (Query_DSL버전)
        List<Member> findAll_Querydsl = memberJpaRepository.findAll_Querydsl();
//        Assertions.assertThat(findAll_querydsl.get(0).getUsername()).isEqualTo(member.getUsername());
        System.out.println("\t\t3.2 findAll_Querydsl = " + findAll_Querydsl);

        // 4.1 회원명으로 조회 테스트
        List<Member> findByUsername = memberJpaRepository.findByUsername("Duke");
//        Assertions.assertThat(findBuUsername.get(0).getUsername()).isEqualTo(member.getUsername());
        System.out.println("\t\t\t4.1 findBuUsername = " + findByUsername);

        // 4.2 회원명으로 조회 테스트 (Query_DSL버전)
        List<Member> findByUsername_Querydsl = memberJpaRepository.findByUsername_Querydsl("Grievous");
        Assertions.assertThat(findByUsername_Querydsl.get(0).getUsername()).isEqualTo(member2.getUsername());
        System.out.println("\t\t\t4.2 findBuUsername = " + findByUsername_Querydsl);

    }


    /**
     *   <동적 쿼리와 성능 최적화 조회>
     *       1. Builder 사용 테스트
     *          - MemberTeamDto.java : 조회 최적화용 DTO 추가
     *          - MemberSearchCondition.java : Builder를 사용한 예
     *
     * */

    @Test
    public void searchTest() {

        // 빌더 컨디션 만들기.
        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setUsername(null);      // 회원명이 null이면, 조건에서 빠짐
        condition.setTeamName("BTEAM");   // 팀명이 null이면, 조건에서 빠짐
        condition.setAgeGoe(100);         // 나이가 100보다 큰 회원만 조회
        condition.setAgeLoe(0);           // 1보다 작으면 조건에서 빠짐.

        List<MemberTeamDto> memberTeamDtos = memberJpaRepository.searchByBuilder(condition);

        for (MemberTeamDto memberTeamDto : memberTeamDtos) {
            System.out.println("memberTeamDto = " + memberTeamDto);
        }


        /**
         *   <동적 쿼리와 성능 최적화 조회>
         *       1. Builder 사용 테스트
         *          - MemberTeamDto.java : 조회 최적화용 DTO 추가
         *          - MemberSearchCondition.java : Builder를 사용한 예
         *
         * */
    }
        @Test
        public void searchWhereParams() {

            // Where Params 컨디션 만들기.
            MemberSearchCondition condition = new MemberSearchCondition();
            condition.setUsername(null);      // 회원명이 null이면, 조건에서 빠짐
            condition.setTeamName("BTEAM");   // 팀명이 null이면, 조건에서 빠짐
            condition.setAgeGoe(100);         // 나이가 100보다 큰 회원만 조회
            condition.setAgeLoe(0);           // 1보다 작으면 조건에서 빠짐.

            List<MemberTeamDto> memberTeamDtos = memberJpaRepository.search(condition);

            for (MemberTeamDto memberTeamDto : memberTeamDtos) {
                System.out.println("memberTeamDto = " + memberTeamDto);
            }

        }

}